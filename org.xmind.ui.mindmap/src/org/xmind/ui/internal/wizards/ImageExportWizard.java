/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.image.ImageExportUtils;
import org.xmind.gef.image.ImageWriter;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.DialogUtils;
import org.xmind.ui.io.MonitoredOutputStream;
import org.xmind.ui.mindmap.GhostShellProvider;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.viewers.ImagePreviewViewer;
import org.xmind.ui.wizards.AbstractMindMapExportPage;
import org.xmind.ui.wizards.AbstractMindMapExportWizard;

public class ImageExportWizard extends AbstractMindMapExportWizard {

    private static final String IMAGE_EXPORT_PAGE_NAME = "imageExportPage"; //$NON-NLS-1$

    private static final String DIALOG_SETTINGS_SECTION_ID = "org.xmind.ui.export.image"; //$NON-NLS-1$

    private static final String PROP_FORMAT = "FORMAT"; //$NON-NLS-1$

    private static final int LARGE_SIZE = 1280 * 1024;

    private static enum PreviewState {
        Showing(null, SWT.COLOR_DARK_GRAY, SWT.BOTTOM | SWT.RIGHT) {
            public String getTitle(Image image, boolean largeImage) {
                if (image == null)
                    return super.getTitle(image, largeImage);
                Rectangle r = image.getBounds();
                return String.format("%d x %d", r.width, r.height); //$NON-NLS-1$
            }
        }, //
        Generating(WizardMessages.ImageExportPage_GeneratingPreview,
                SWT.COLOR_DARK_GRAY, SWT.NONE), //
        Error(WizardMessages.ImageExportPage_FailedToGeneratePreview,
                SWT.COLOR_DARK_RED, SWT.NONE) {
            public String getTitle(Image image, boolean largeImage) {
                return makeErrorMessage(super.getTitle(image, largeImage),
                        largeImage);
            }
        };

        private int colorId;

        private String title;

        private int titlePlacement;

        private PreviewState(String title, int colorId, int titlePlacement) {
            this.title = title;
            this.colorId = colorId;
            this.titlePlacement = titlePlacement;
        }

        public String getTitle(Image image, boolean largeImage) {
            return title;
        }

        public void setColor(Control control) {
            control.setForeground(control.getDisplay().getSystemColor(colorId));
        }

        public int getTitlePlacement() {
            return titlePlacement;
        }
    }

    private class ImageExportPage extends AbstractMindMapExportPage {

        private class GeneratePreviewJob extends Job {

            private String destPath;

            private Display display;

//            private Shell parentShell;

            public GeneratePreviewJob(String destPath, ImageFormat format,
                    Display display) {
                super(NLS.bind(
                        WizardMessages.ImageExportPage_GeneratePreview_jobName,
                        format.getName()));
                this.destPath = destPath;
                this.display = display;
            }

            protected IStatus run(IProgressMonitor monitor) {
                monitor.beginTask(null, 100);

                monitor.subTask(WizardMessages.ImageExportPage_GeneratePreview_CreatingSourceImage);
                MindMapImageExporter exporter = getImageExporter(display);

                Image image;
                try {
                    image = exporter.createImage();
                } catch (Throwable e) {
                    if (generatePreviewJob == this)
                        generatePreviewJob = null;
                    asyncUpdateViewer(display, null, PreviewState.Error, false,
                            null);
                    return new Status(
                            IStatus.ERROR,
                            MindMapUIPlugin.PLUGIN_ID,
                            WizardMessages.ImageExportPage_GeneratePreview_CouldNotCreateSourceImage,
                            e);
                }

                try {
                    if (monitor.isCanceled()) {
                        if (generatePreviewJob == this)
                            generatePreviewJob = null;
                        return new Status(
                                IStatus.CANCEL,
                                MindMapUIPlugin.PLUGIN_ID,
                                WizardMessages.ImageExportPage_GeneratePreview_Canceled);
                    }
                    monitor.worked(40);

                    boolean largeImage = isImageLarge(image);
                    Point origin = null;

                    monitor.subTask(WizardMessages.ImageExportPage_GeneratePreview_SavingTempFile);
                    try {
                        writeImage(image, destPath, monitor);
                        origin = exporter.calcRelativeOrigin();
                    } catch (InterruptedIOException e) {
                        deleteTemporaryPath(destPath);
                        if (generatePreviewJob == this)
                            generatePreviewJob = null;
                        return new Status(
                                IStatus.CANCEL,
                                MindMapUIPlugin.PLUGIN_ID,
                                WizardMessages.ImageExportPage_GeneratePreview_Canceled);
                    } catch (Throwable e) {
                        deleteTemporaryPath(destPath);
                        if (monitor.isCanceled()) {
                            if (generatePreviewJob == this)
                                generatePreviewJob = null;
                            return new Status(
                                    IStatus.CANCEL,
                                    MindMapUIPlugin.PLUGIN_ID,
                                    WizardMessages.ImageExportPage_GeneratePreview_Canceled);
                        }
                        asyncUpdateViewer(display, null, PreviewState.Error,
                                largeImage, origin);
                        return new Status(
                                IStatus.CANCEL,
                                MindMapUIPlugin.PLUGIN_ID,
                                makeErrorMessage(
                                        WizardMessages.ImageExportPage_GeneratePreview_CouldNotSavePreviewImage,
                                        largeImage), e);
                    }
                    if (monitor.isCanceled()) {
                        deleteTemporaryPath(destPath);
                        if (generatePreviewJob == this)
                            generatePreviewJob = null;
                        return new Status(
                                IStatus.CANCEL,
                                MindMapUIPlugin.PLUGIN_ID,
                                WizardMessages.ImageExportPage_GeneratePreview_Canceled);
                    }
                    monitor.worked(40);

                    monitor.subTask(WizardMessages.ImageExportPage_GeneratePreview_LoadingTempFile);
                    File previewFile = new File(destPath);
                    if (!previewFile.exists() || !previewFile.canRead()) {
                        if (generatePreviewJob == this)
                            generatePreviewJob = null;
                        asyncUpdateViewer(display, null, PreviewState.Error,
                                largeImage, origin);
                        return new Status(
                                IStatus.CANCEL,
                                MindMapUIPlugin.PLUGIN_ID,
                                WizardMessages.ImageExportPage_GeneratePreview_CouldNotLoadPreviewImage);
                    }

                    try {
                        previewImage = new Image(Display.getCurrent(), destPath);
                    } catch (Throwable e) {
                        if (generatePreviewJob == this)
                            generatePreviewJob = null;
                        asyncUpdateViewer(display, null, PreviewState.Error,
                                largeImage, origin);
                        return new Status(
                                IStatus.CANCEL,
                                MindMapUIPlugin.PLUGIN_ID,
                                WizardMessages.ImageExportPage_GeneratePreview_CouldNotLoadPreviewImage,
                                e);
                    }

                    asyncUpdateViewer(display, previewImage,
                            PreviewState.Showing, false, origin);
                    if (generatePreviewJob == this)
                        generatePreviewJob = null;
                    monitor.done();
                    return new Status(
                            IStatus.OK,
                            MindMapUIPlugin.PLUGIN_ID,
                            WizardMessages.ImageExportPage_GeneratePreview_Completed);
                } finally {
                    image.dispose();
                }

            }
        }

        private Combo formatCombo;

        private ImagePreviewViewer viewer;

        private Image previewImage;

        private String previewPath;

        private GeneratePreviewJob generatePreviewJob;

        private PreviewState previewState;

        protected ImageExportPage() {
            super(IMAGE_EXPORT_PAGE_NAME, WizardMessages.ImageExportPage_title);
            setDescription(WizardMessages.ImageExportPage_description);
        }

        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.verticalSpacing = 15;
            composite.setLayout(layout);
            setControl(composite);

            createFormatControls(composite);
            createPreviewControl(composite);

            Control fileGroup = createFileControls(composite);
            fileGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                    false));

            updateStatus();

            generatePreview(getFormat());
        }

        private void createPreviewControl(Composite parent) {
            viewer = new ImagePreviewViewer(true);
            viewer.setPrefWidth(400);
            viewer.createControl(parent);
            viewer.getControl().setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));
            viewer.getControl().setFont(
                    FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT, 8));
            hookWidget(viewer.getControl(), SWT.Resize);
            parent.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    updateViewerSize();
                }
            });
        }

        private void createFormatControls(Composite parent) {
            Group group = new Group(parent, SWT.NONE);
            group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            group.setLayout(new GridLayout());
            group.setText(WizardMessages.ImageExportPage_FormatGroup_title);

            Label label = new Label(group, SWT.WRAP);
            GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
            gd.widthHint = 400;
            label.setLayoutData(gd);
            label.setText(WizardMessages.ImageExportPage_FormatGroup_description);

            formatCombo = new Combo(group, SWT.READ_ONLY | SWT.SIMPLE
                    | SWT.DROP_DOWN | SWT.BORDER);
            formatCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                    false));
            for (ImageFormat format : ImageFormat.values()) {
                formatCombo.add(format.getDescription());
            }
            if (getFormat() != null) {
                formatCombo.select(getFormat().ordinal());
            }
            hookWidget(formatCombo, SWT.Selection);
        }

        public void dispose() {
            super.dispose();
            viewer = null;
            disposePreview();
        }

        private void disposePreview() {
            cancel();
            if (previewImage != null) {
                previewImage.dispose();
                previewImage = null;
            }
            if (previewPath != null) {
                deleteTemporaryPath(previewPath);
                previewPath = null;
            }
        }

        protected void handleWidgetEvent(Event event) {
            if (event.widget == formatCombo) {
                int selection = formatCombo.getSelectionIndex();
                ImageFormat oldFormat = getFormat();
                if (selection < 0) {
                    setFormat(null);
                } else {
                    setFormat(ImageFormat.values()[selection]);
                }
                if (oldFormat != getFormat()) {
                    formatChanged();
                }
            } else if (viewer != null && event.widget == viewer.getControl()) {
                updateViewerSize();
            } else {
                super.handleWidgetEvent(event);
            }
        }

        private void updateViewerSize() {
            if (viewer == null || viewer.getControl().isDisposed())
                return;

            Canvas canvas = viewer.getCanvas();
            if (canvas == null || canvas.isDisposed())
                return;

            Rectangle bounds = canvas.getBounds();
            viewer.setPrefWidth(bounds.width);
            viewer.setPrefHeight(bounds.height);
        }

        protected boolean isPageCompletable() {
            return super.isPageCompletable() && hasFormat();
        }

        protected String generateWarningMessage() {
            if (previewState == PreviewState.Error && viewer != null) {
                return viewer.getTitle();
            }
            return super.generateWarningMessage();
        }

        public PreviewState getPreviewState() {
            return previewState;
        }

//        protected String generateWarningMessage() {
//            if (hasTargetPath()) {
//                if (!isExtensionCompatible(getTargetPath(), FileUtils
//                        .getExtension(getTargetPath()))) {
//                    String formatName = getFormatName();
//                    return String
//                            .format(
//                                    WizardMessages.ExportPage_UncompatibleFormat_warning,
//                                    formatName);
//                }
//            }
//            return super.generateWarningMessage();
//        }

        protected FileDialog createBrowseDialog() {
            FileDialog dialog = super.createBrowseDialog();
            DialogUtils.makeImageSelectorDialog(dialog, false, getFormat());
            return dialog;
        }

        protected String getSuggestedFileName() {
            return super.getSuggestedFileName()
                    + getFormat().getExtensions().get(0);
        }

        private void formatChanged() {
            if (hasTargetPath() && hasFormat()) {
                List<String> exts = getFormat().getExtensions();
                String ext = FileUtils.getExtension(getTargetPath());
                if (!exts.contains(ext)) {
                    setTargetPath(replaceExtension(getTargetPath(), ext,
                            exts.get(0)));
                }
            }
            updateStatus();
            generatePreview(getFormat());
        }

        private String replaceExtension(String path, String oldExt,
                String newExt) {
            return path.substring(0, path.length() - oldExt.length()) + newExt;
        }

        private void generatePreview(ImageFormat format) {
            if (format == null)
                return;

            if (viewer != null && !viewer.getControl().isDisposed()) {
                updateViewer(null, PreviewState.Generating, false, null);
            }
            disposePreview();

            previewPath = requestTemporaryPath(null, format.getExtensions()
                    .get(0), true);
            if (previewPath == null) {
                updateViewer(null, PreviewState.Error, false, null);
                return;
            }

            Display display = Display.getCurrent();
            generatePreviewJob = new GeneratePreviewJob(previewPath, format,
                    display);
            generatePreviewJob.schedule();
        }

        private void asyncUpdateViewer(Display display, final Image image,
                final PreviewState state, final boolean largeImage,
                final Point origin) {
            if (Thread.currentThread() != display.getThread()) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        updateViewer(image, state, largeImage, origin);
                    }
                });
            } else {
                updateViewer(image, state, largeImage, origin);
            }
        }

        private void updateViewer(final Image image, PreviewState state,
                boolean largeImage, Point origin) {
            this.previewState = state;
            if (viewer == null || viewer.getControl().isDisposed())
                return;

            if (image != null && origin != null) {
                viewer.setImage(image, origin.x, origin.y);
            } else {
                viewer.setImage(image);
            }
            if (viewer.getRatio() > 1) {
                viewer.changeRatio(1);
            }
            viewer.setTitle(state.getTitle(image, largeImage));
            viewer.setTitlePlacement(state.getTitlePlacement());
            state.setColor(viewer.getControl());
            updateStatus();
        }

        public void cancel() {
            if (generatePreviewJob != null) {
                generatePreviewJob.cancel();
                generatePreviewJob = null;
            }
        }

    }

    private ImageFormat format;

    private ImageExportPage page;

//    private MindMapImageExtractor imageExtractor;
    private MindMapImageExporter exporter = null;

    private GhostShellProvider shell = null;

    public ImageExportWizard() {
        setWindowTitle(WizardMessages.ImageExportWizard_windowTitle);
        setDefaultPageImageDescriptor(MindMapUI.getImages().getWizBan(
                IMindMapImages.WIZ_EXPORT));
        setDialogSettings(MindMapUIPlugin.getDefault().getDialogSettings(
                DIALOG_SETTINGS_SECTION_ID));
        setNeedsProgressMonitor(true);
    }

    protected void loadDialogSettings(IDialogSettings settings) {
        super.loadDialogSettings(settings);
        String mediaType = settings.get(PROP_FORMAT);
        ImageFormat lastFormat = ImageFormat.findByMediaType(mediaType,
                ImageFormat.BMP);
        setFormat(lastFormat);
    }

    protected void saveDialogSettings(IDialogSettings settings) {
        super.saveDialogSettings(settings);
        if (format != null) {
            settings.put(PROP_FORMAT, format.getMediaType());
        }
    }

    protected void addValidPages() {
        addPage(page = new ImageExportPage());
    }

    public void setFormat(ImageFormat format) {
        this.format = format;
    }

    public ImageFormat getFormat() {
        return format;
    }

    public boolean hasFormat() {
        return this.format != null;
    }

    @Override
    public boolean performFinish() {
        if (page.getPreviewState() == PreviewState.Error) {
            if (!MessageDialog.openConfirm(getShell(),
                    WizardMessages.ImageExport_ConfirmProceedWithError_title,
                    WizardMessages.ImageExport_ConfirmProceedWithError_message))
                return false;
        }
        if (page != null) {
            page.cancel();
        }
        return super.performFinish();
    }

    protected String getFormatName() {
        return hasFormat() ? "'" + getFormat().getName() + "'" : ""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    protected boolean isExtensionCompatible(String path, String extension) {
        return super.isExtensionCompatible(path, extension)
                && hasFormat()
                && getFormat().getExtensions()
                        .contains(extension.toLowerCase());
    }

    private boolean isImageLarge(Image image) {
        Rectangle r = image.getBounds();
        return r.width * r.height > LARGE_SIZE;
    }

//    private Shell findParentShell(Display display) {
//        Shell shell = getContainer().getShell();
//        if (shell != null) {
//            Composite shellParent = shell.getParent();
//            if (shellParent instanceof Shell) {
//                Shell parentShell = (Shell) shellParent;
//                if (!parentShell.isDisposed()
//                        && parentShell.getDisplay() == display)
//                    return parentShell;
//            }
//        }
//        return null;
//    }

    protected MindMapImageExporter getImageExporter(Display display) {
        if (this.exporter == null) {
            this.exporter = createImageExporter(display);
        }
        return this.exporter;
    }

    /**
     * @param display
     * @return
     */
    private MindMapImageExporter createImageExporter(Display display) {
        MindMapImageExporter exporter = new MindMapImageExporter(display);
        exporter.setSource(getSourceMindMap(), getShellProvider(display), null,
                null);
        return exporter;
    }

//    protected void releaseImageExtractor(MindMapImageExtractor imageExtractor) {
//        if (imageExtractor == this.imageExtractor) {
//            // dispose our own image extractor when wizard dispose
//            return;
//        }
//        imageExtractor.dispose();
//    }
//
//    protected MindMapImageExtractor getImageExtractor(Display display,
//            Shell parentShell) {
//        if (imageExtractor == null) {
//            imageExtractor = createImageExtractor(display, parentShell);
//        }
//        return imageExtractor;
//    }
//
//    private MindMapImageExtractor createImageExtractor(Display display,
//            Shell parentShell) {
//        if (parentShell != null && !parentShell.isDisposed()) {
//            return new MindMapImageExtractor(parentShell, getSourceMindMap()
//                    .getSheet(), getSourceMindMap().getCentralTopic());
//        } else {
//            return new MindMapImageExtractor(display, getSourceMindMap()
//                    .getSheet(), getSourceMindMap().getCentralTopic());
//        }
//    }

    protected GhostShellProvider getShellProvider(Display display) {
        if (shell == null) {
            shell = new GhostShellProvider(display);
        }
        return shell;
    }

    public void dispose() {
//        if (imageExtractor != null) {
//            imageExtractor.dispose();
//            imageExtractor = null;
//        }
        if (shell != null) {
            shell.dispose();
            shell = null;
        }
        exporter = null;
        super.dispose();
    }

    protected void writeImage(Image image, String destPath,
            IProgressMonitor monitor) throws IOException {
        OutputStream output = new MonitoredOutputStream(new FileOutputStream(
                destPath), monitor);
        ImageWriter writer = createImageWriter(image, output);
        try {
            writer.write();
        } finally {
            output.close();
        }
    }

    protected ImageWriter createImageWriter(Image image, OutputStream output) {
        return ImageExportUtils.createImageWriter(image, getFormat()
                .getSWTFormat(), output);
    }

    protected void doExport(IProgressMonitor monitor, final Display display,
            final Shell parentShell) throws InvocationTargetException,
            InterruptedException {
        monitor.beginTask(null, 100);

        monitor.subTask(WizardMessages.ImageExport_CreatingSourceImage);
//        MindMapImageExtractor imageExtractor = getImageExtractor(display,
//                parentShell);
        MindMapImageExporter exporter = getImageExporter(display);
        Image image;
        try {
//            image = imageExtractor.getImage();
            image = exporter.createImage();
        } catch (Throwable e) {
            monitor.setCanceled(true);
            throw new InvocationTargetException(e);
        }
        try {
            if (monitor.isCanceled()) {
                throw new InterruptedException();
            }
            monitor.worked(50);

            String path = getTargetPath();
            monitor.subTask(NLS.bind(
                    WizardMessages.ImageExport_WritingTargetFile, path));
            try {
                writeImage(image, path, monitor);
            } catch (IOException e) {
                if (monitor.isCanceled()) {
                    throw new InterruptedException();
                }
                throw new InvocationTargetException(e);
            }
        } finally {
            image.dispose();
        }

        monitor.worked(49);

        launchTargetFile(true, monitor, display, parentShell);
        monitor.done();
    }

    private static String makeErrorMessage(String originalMessage,
            boolean largeImage) {
        if (largeImage) {
            return originalMessage + " " //$NON-NLS-1$
                    + WizardMessages.ImageExportPage_ImageTooLarge;
        }
        return originalMessage;
    }

    protected void handleExportException(Throwable e) {
        super.handleExportException(e);
        page.setErrorMessage(e.getLocalizedMessage());
    }

}