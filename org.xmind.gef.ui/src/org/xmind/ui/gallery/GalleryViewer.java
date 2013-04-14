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
package org.xmind.ui.gallery;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.part.GraphicalRootEditPart;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;

public class GalleryViewer extends GraphicalViewer {

    /**
     * Viewer property key indicating whether frames are laid out horizontally
     * or vertically.
     * <p>
     * Values: true, false
     * </p>
     */
    public static final String Horizontal = "org.xmind.ui.gallery.horizontal"; //$NON-NLS-1$

    /**
     * Viewer property key indicating whether frames can wrap.
     * <p>
     * Values: true, false
     * </p>
     */
    public static final String Wrap = "org.xmind.ui.gallery.wrap"; //$NON-NLS-1$

    /**
     * Values: true, false
     */
    public static final String HideTitle = "org.xmind.ui.gallery.hideTitle"; //$NON-NLS-1$

    /**
     * Values: GalleryLayout
     */
    public static final String Layout = "org.xmind.ui.gallery.layout"; //$NON-NLS-1$

    /**
     * Values: true, false
     */
    public static final String ImageStretched = "stretched"; //$NON-NLS-1$

    /**
     * Values: true, false
     */
    public static final String ImageConstrained = "constrained"; //$NON-NLS-1$

    /**
     * Values: {@link org.eclipse.draw2d.geometry.Dimension}
     */
    public static final String FrameContentSize = "org.xmind.ui.gallery.frameContentSize"; //$NON-NLS-1$

    /**
     * Values: true, false
     */
    public static final String FlatFrames = "org.xmind.ui.gallery.flatFrames"; //$NON-NLS-1$

    /**
     * Values: true, false
     */
    public static final String SolidFrames = "org.xmind.ui.gallery.solidFrames"; //$NON-NLS-1$

    /**
     * Values: TITLE_TOP, TITLE_BOTTOM, TITLE_LEFT, TITLE_RIGHT
     */
    public static final String TitlePlacement = "org.xmind.ui.gallery.titlePlacement"; //$NON-NLS-1$

    /**
     * Values: true, false
     */
    public static final String SingleClickToOpen = "org.xmind.ui.gallery.singleClickToOpen"; //$NON-NLS-1$

    /**
     * Value for title placement 'top'.
     */
    public static final Integer TITLE_TOP = new Integer(PositionConstants.TOP);

    /**
     * Value for title placement 'bottom'.
     */
    public static final Integer TITLE_BOTTOM = new Integer(
            PositionConstants.BOTTOM);

    /**
     * Value for title placement 'left'.
     */
    public static final Integer TITLE_LEFT = new Integer(PositionConstants.LEFT);

    /**
     * Value for title placement 'right'.
     */
    public static final Integer TITLE_RIGHT = new Integer(
            PositionConstants.RIGHT);

    public static final String POLICY_NAVIGABLE = "org.xmind.ui.gallery.editPolicy.navigable"; //$NON-NLS-1$

    private class GalleryLabelProviderListener implements
            ILabelProviderListener {
        public void labelProviderChanged(LabelProviderChangedEvent event) {
            update(event.getElements());
        }
    }

    private IBaseLabelProvider labelProvider = null;

    private ILabelProviderListener labelProviderListener = null;

    private List<IOpenListener> openListeners = null;

    public GalleryViewer() {
//        setContentProvider(new GalleryModelContentProvider());
        setPartFactory(GalleryPartFactory.getDefault());
//        setGenre(GalleryGenre.getDefault());
        setRootPart(new GraphicalRootEditPart());
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IBaseLabelProvider.class)
            return getLabelProvider();
        return super.getAdapter(adapter);
    }

    protected Control internalCreateControl(Composite parent, int style) {
        Control control = super.internalCreateControl(parent, style);
        control.setBackground(parent.getDisplay().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND));
        return control;
    }

    public void update() {
        update(null);
    }

    public void update(Object[] elements) {
        if (elements == null) {
            IPart contents = getRootPart().getContents();
            if (contents.getStatus().isActive())
                contents.refresh();
            for (IPart p : contents.getChildren()) {
                if (p.getStatus().isActive()) {
                    ((IGraphicalPart) p).refresh();
                }
            }
        } else {
            for (Object element : elements) {
                IPart p = findPart(element);
                if (p != null && p.getStatus().isActive()) {
                    ((IGraphicalPart) p).refresh();
                }
            }
        }
    }

    public IBaseLabelProvider getLabelProvider() {
        if (labelProvider == null) {
            labelProvider = new LabelProvider();
        }
        return labelProvider;
    }

    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        if (labelProvider == null)
            return;

        IBaseLabelProvider oldLabelProvider = this.labelProvider;
        if (labelProvider == oldLabelProvider)
            return;

        if (oldLabelProvider != null) {
            if (labelProviderListener != null) {
                oldLabelProvider.removeListener(labelProviderListener);
            }
        }
        this.labelProvider = labelProvider;
        if (labelProviderListener == null)
            labelProviderListener = new GalleryLabelProviderListener();
        labelProvider.addListener(labelProviderListener);
        refresh();

        if (oldLabelProvider != null) {
            oldLabelProvider.dispose();
        }
    }

    protected void handleDispose(DisposeEvent e) {
        if (labelProvider != null) {
            if (labelProviderListener != null)
                labelProvider.removeListener(labelProviderListener);
            labelProvider.dispose();
            labelProvider = null;
        }
        super.handleDispose(e);
    }

    public void centerHorizontal() {
        FigureCanvas fc = getCanvas();
        if (fc.isDisposed())
            return;
        RangeModel horizontal = fc.getViewport().getHorizontalRangeModel();
        int h = (horizontal.getMaximum() - horizontal.getExtent() + horizontal
                .getMinimum()) / 2;
        fc.scrollToX(h);
    }

    public void addOpenListener(IOpenListener listener) {
        if (openListeners == null)
            openListeners = new ArrayList<IOpenListener>();
        openListeners.add(listener);
    }

    public void removeOpenListener(IOpenListener listener) {
        if (openListeners == null)
            return;
        openListeners.remove(listener);
    }

    protected void fireOpen(final OpenEvent event) {
        if (openListeners == null)
            return;
        for (final Object l : openListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IOpenListener) l).open(event);
                }
            });
        }
    }

    protected void fireOpen() {
        fireOpen(new OpenEvent(this, getSelection()));
    }

    private List<? extends IPart> toReveal;

    protected void revealParts(List<? extends IPart> parts) {
        if (toReveal != null) {
            toReveal = parts;
        } else {
            toReveal = parts;
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    if (toReveal == null || getControl() == null
                            || getControl().isDisposed())
                        return;

                    Rectangle r = null;
                    for (IPart p : toReveal) {
                        if (p instanceof IGraphicalPart) {
                            r = Geometry.union(r, ((IGraphicalPart) p)
                                    .getFigure().getBounds());
                        }
                    }
                    if (r != null) {
                        ensureVisible(r);
                    }
                    toReveal = null;
                }
            });
        }
    }
}