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
package org.xmind.ui.internal.notes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.IFileEntry;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.internal.AttachmentImageDescriptor;
import org.xmind.ui.internal.dialogs.DialogMessages;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.ImagePlaceHolder;

public class RichDocumentNotesAdapter implements IAdaptable {

    private IWorkbook workbook;

    private IRichDocument document;

    private INotesContent content;

    private Map<String, Image> images = null;

    public RichDocumentNotesAdapter(ITopic topic) {
        this.workbook = topic.getOwnedWorkbook();
        INotes notes = topic.getNotes();
        INotesContent content = notes.getContent(INotes.HTML);
        if (content == null)
            content = notes.getContent(INotes.PLAIN);
        this.content = content;
    }

    public RichDocumentNotesAdapter(IWorkbook workbook, INotesContent content) {
        this.workbook = workbook;
        this.content = content;
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IRichDocument.class)
            return getDocument();
        return null;
    }

    public IWorkbook getWorkbook() {
        return workbook;
    }

    public INotesContent getInitContent() {
        return content;
    }

    public IRichDocument getDocument() {
        if (document == null) {
            document = buildDocument();
        }
        return document;
    }

    public String getImageUrl(Image image) {
        if (image != null && images != null) {
            for (Entry<String, Image> en : images.entrySet()) {
                if (en.getValue() == image) {
                    return HyperlinkUtils.toAttachmentURL(en.getKey());
                }
            }
        }
        return null;
    }

    public Image getImageFromUrl(String uri) {
        return getImageFromEntryPath(HyperlinkUtils.toAttachmentPath(uri));
    }

    private Image getImageFromEntryPath(String path) {
        Image image = null;
        if (images != null) {
            image = images.get(path);
            if (image != null)
                return image;
        }
        IFileEntry entry = workbook.getManifest().getFileEntry(path);
        if (entry != null) {
            image = AttachmentImageDescriptor.createFromEntry(workbook, entry)
                    .createImage(false);
            if (image != null) {
                if (images == null) {
                    images = new HashMap<String, Image>();
                }
                images.put(path, image);
            }
        }
        return image;
    }

    public Image createImageFromFile(String absolutePath) {
        IFileEntry entry = createFileEntry(absolutePath);
        if (entry != null) {
            return getImageFromEntryPath(entry.getPath());
        }
        return null;
    }

    private IFileEntry createFileEntry(final String absolutePath) {
        final IFileEntry[] entryRef = new IFileEntry[1];
        String message = NLS.bind(DialogMessages.ConfirmOverwrite_message,
                absolutePath);
        SafeRunner.run(new SafeRunnable(message) {
            public void run() throws Exception {
                entryRef[0] = workbook.getManifest()
                        .createAttachmentFromFilePath(absolutePath);
            }
        });
        return entryRef[0];
    }

    public void dispose() {
        if (images != null) {
            for (Image image : images.values()) {
                image.dispose();
            }
            images = null;
        }
    }

    private IRichDocument buildDocument() {
        RichDocumentBuilder builder = new RichDocumentBuilder(this);
        builder.build(content);
        return builder.getResult();
    }

    public INotesContent makeNewHtmlContent() {
        HtmlNotesContentBuilder builder = new HtmlNotesContentBuilder(this);
        IRichDocument doc = getDocument();
        builder.build(doc);
        return builder.getResult();
    }

    public INotesContent makeNewPlainContent() {
        String string = getDocument().get();
        if ("".equals(string)) //$NON-NLS-1$
            return null;
        StringBuilder sb = new StringBuilder(string);
        int index = 0;
        while ((index = sb.indexOf(ImagePlaceHolder.PLACE_HOLDER, index)) >= 0) {
            sb.deleteCharAt(index);
        }
        IPlainNotesContent content = (IPlainNotesContent) getWorkbook()
                .createNotesContent(INotes.PLAIN);
        content.setTextContent(sb.toString());
        return content;
    }
}