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
package org.xmind.ui.resources;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.internal.ToolkitPlugin;

/**
 * @author Frank Shaka
 */
public class FontUtils {

    public static interface IFontNameListCallback {
        void setAvailableFontNames(List<String> fontNames);
    }

    private static List<String> availableFontNames = null;

    private static List<IFontNameListCallback> callbacks = null;

    private FontUtils() {
    }

    /**
     * Get all font names available in the current graphics environment.
     * <p>
     * <b>IMPORTANT</b>:This method is NOT recommended because it may cause UI
     * thread delay. To avoid that, use
     * {@link #fetchAvailableFontNames(IFontNameListCallback)} instead.
     * </p>
     * 
     * @return A string list containing all available font names
     * @see #fetchAvailableFontNames(IFontNameListCallback)
     */
    public static List<String> getAvailableFontNames() {
        if (availableFontNames == null) {
            availableFontNames = findAvailableFontNames();
        }
        return availableFontNames;
    }

    private static ArrayList<String> findAvailableFontNames() {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        ArrayList<String> list = new ArrayList<String>(names.length);
        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
        }
        for (String name : names) {
            FontData[] fontList = display.getFontList(name, true);
            if (fontList != null && fontList.length > 0) {
                list.add(name);
            }
        }
        list.trimToSize();
        return list;
    }

    /**
     * Fetch all available font names from the current graphics environment.
     * <p>
     * Since SWT doesn't provide any convenient API for just getting current
     * system's all available font names, we have to try acquiring a basic font
     * name list from AWT and then filter it by removing names that SWT doesn't
     * support. This process may cause some delay (maybe more than 2 seconds).
     * So a <i>callback</i> is provided to avoid making the UI thread wait too
     * long for this method to return a complete result. In this way, the whole
     * process will be performed in a new job thread, so that this method
     * returns immediately to let clients continue handling other events, and
     * when the process is over, the result is passed to the callback and cached
     * for quick access in the future.
     * </p>
     * <p>
     * If you insist in getting a result right now and care not much about the
     * delay issue, you may call {@link #getAvailableFontNames()} instead, but
     * that is NOT recommended.
     * </p>
     * 
     * @param display
     *            The display from which font names are fetched
     * @param callback
     *            A callback to handle with the result list after the 'fetching'
     *            process is over
     */
    public static void fetchAvailableFontNames(final Display display,
            final IFontNameListCallback callback) {
        if (callback == null)
            return;

        if (availableFontNames != null) {
            callback.setAvailableFontNames(availableFontNames);
            return;
        }

        if (display == null || display.isDisposed()) {
            return;
        }

        if (callbacks != null) {
            if (callbacks.contains(callback)) {
                return;
            }
            callbacks.add(callback);
            return;
        }

        callbacks = new ArrayList<IFontNameListCallback>();
        callbacks.add(callback);

        Job fetch = new Job(Messages.FetchFontList_jobName) {
            protected IStatus run(IProgressMonitor monitor) {
                fetchAvailableFontNames(display, callback, monitor);
                return new Status(IStatus.OK, ToolkitPlugin.PLUGIN_ID,
                        "Font Name Fetched"); //$NON-NLS-1$
            }
        };
        fetch.schedule();
    }

    private static void fetchAvailableFontNames(final Display display,
            final IFontNameListCallback callback,
            final IProgressMonitor progress) {
        if (display.isDisposed()) {
            progress.done();
            return;
        }

        if (availableFontNames != null) {
            progress.done();
            display.asyncExec(new Runnable() {
                public void run() {
                    callback.setAvailableFontNames(availableFontNames);
                }
            });
            return;
        }

        new Runnable() {
            public void run() {
                progress.beginTask(null, 10);
                progress.subTask(Messages.FetchFontNames);
                String[] names = getAllFontNames(progress);
                if (names.length == 0) {
                    availableFontNames = Collections.emptyList();
                    notifyCallbacks();
                    return;
                }
                progress.worked(1);

                progress.subTask(Messages.FilterFontList);
                filterFontList(new SubProgressMonitor(progress, 90), names);
            }

            /**
             * @param progress
             * @return
             */
            private String[] getAllFontNames(final IProgressMonitor progress) {
                final String[][] nameList = new String[1][];
                Thread th = new Thread(new Runnable() {
                    public void run() {
                        nameList[0] = GraphicsEnvironment
                                .getLocalGraphicsEnvironment()
                                .getAvailableFontFamilyNames();
                    }
                },
                        "Get Available Font Family Names From AWT-GraphicsEnvironment"); //$NON-NLS-1$
                th.setDaemon(true);
                th.start();
                while (nameList[0] == null) {
                    if (progress.isCanceled()) {
                        nameList[0] = new String[0];
                        break;
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        nameList[0] = new String[0];
                        break;
                    }
                }
                return nameList[0];
            }

            private void filterFontList(IProgressMonitor monitor, String[] names) {
                monitor.beginTask(null, names.length);
                final ArrayList<String> list = new ArrayList<String>(
                        names.length);
                final Iterator<String> it = Arrays.asList(names).iterator();

                while (it.hasNext()) {
                    if (display.isDisposed()) {
                        progress.done();
                        return;
                    }
                    if (availableFontNames != null) {
                        notifyCallbacks();
                        return;
                    }
                    final String name = it.next();
                    progress.subTask(name);
                    display.syncExec(new Runnable() {
                        public void run() {
                            FontData[] fontList = display.getFontList(name,
                                    true);
                            if (fontList != null && fontList.length > 0) {
                                list.add(name);
                            }
                        }
                    });
                    Thread.yield();
                    progress.worked(1);
                }
                list.trimToSize();
                availableFontNames = list;
                notifyCallbacks();
            }

            private synchronized void notifyCallbacks() {
                progress.done();
                if (callbacks == null)
                    return;
                if (display.isDisposed())
                    return;
                for (int i = 0; i < callbacks.size(); i++) {
                    final IFontNameListCallback callback = callbacks.get(i);
                    if (callback != null) {
                        display.asyncExec(new Runnable() {
                            public void run() {
                                callback.setAvailableFontNames(availableFontNames);
                            }
                        });
                    }
                }
                callbacks = null;
            }

        }.run();
    }

    public static Font getFont(String key, FontData[] fontData) {
        if (key == null) {
            key = toString(fontData);
            if (key == null)
                return null;
        }

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (!reg.hasValueFor(key) && !isDefaultKey(key)) {
            if (fontData == null) {
                fontData = toFontData(key);
                if (fontData == null)
                    return null;
            }
            reg.put(key, fontData);
        }
        return reg.get(key);
    }

    public static Font getBold(String key, FontData[] fontData) {
        if (key == null) {
            key = toString(fontData);
            if (key == null)
                return null;
        }

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (!reg.hasValueFor(key) && !isDefaultKey(key)) {
            if (fontData == null) {
                fontData = toFontData(key);
                if (fontData == null)
                    return null;
            }
            reg.put(key, fontData);
        }
        return reg.getBold(key);
    }

    public static Font getItalic(String key, FontData[] fontData) {
        if (key == null) {
            key = toString(fontData);
            if (key == null)
                return null;
        }

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (!reg.hasValueFor(key) && !isDefaultKey(key)) {
            if (fontData == null) {
                fontData = toFontData(key);
                if (fontData == null)
                    return null;
            }
            reg.put(key, fontData);
        }
        return reg.getItalic(key);
    }

    /**
     * Parses a description to a set of font data.
     * 
     * @param string
     *            a string of the description of a set of font data, e.g.
     *            "(Arial,12,bi)"
     * @return
     */
    public static FontData[] toFontData(String string) {
        if (string == null)
            return null;

        string = string.trim();
        if (string.startsWith("(") && string.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            String[] eles = string.substring(1, string.length() - 1).split(","); //$NON-NLS-1$
            if (eles.length > 0) {
                String name = eles[0].trim();
                if ("".equals(name)) { //$NON-NLS-1$
                    name = JFaceResources.getFontRegistry().getFontData(
                            JFaceResources.DEFAULT_FONT)[0].getName();
                }

                int size = -1;
                if (eles.length > 1) {
                    try {
                        size = Integer.parseInt(eles[1].trim());
                    } catch (Exception e) {
                    }
                }
                if (size < 0) {
                    size = JFaceResources.getFontRegistry().getFontData(
                            JFaceResources.DEFAULT_FONT)[0].getHeight();
                }

                int style = -1;
                if (eles.length > 2) {
                    style = SWT.NORMAL;
                    String styles = eles[2].trim().toLowerCase();
                    if (!"".equals(styles)) { //$NON-NLS-1$
                        if (styles.indexOf('b') >= 0)
                            style |= SWT.BOLD;
                        if (styles.indexOf('i') >= 0)
                            style |= SWT.ITALIC;
                    }
                }
                if (style < 0) {
                    style = JFaceResources.getFontRegistry().getFontData(
                            JFaceResources.DEFAULT_FONT)[0].getStyle();
                }
                FontData[] fontData = new FontData[] { new FontData(name, size,
                        style) };
                return fontData;
            }
        }
        return null;
    }

    public static String toString(FontData[] fontData) {
        if (fontData == null || fontData.length == 0)
            return null;

        return toString(fontData[0]);
    }

    public static String toString(FontData fontData) {
        int style = fontData.getStyle();
        return toString(fontData.getName(), fontData.getHeight(),
                (style & SWT.BOLD) != 0, (style & SWT.ITALIC) != 0);
    }

    public static String toString(String name, int height, boolean bold,
            boolean italic) {
        StringBuilder sb = new StringBuilder(10);
        sb.append("("); //$NON-NLS-1$
        sb.append(name);
        sb.append(","); //$NON-NLS-1$
        sb.append(height);

        if (bold || italic) {
            sb.append(","); //$NON-NLS-1$
            if (bold)
                sb.append("b"); //$NON-NLS-1$
            if (italic)
                sb.append("i"); //$NON-NLS-1$
        }
        sb.append(")"); //$NON-NLS-1$
        return sb.toString();
    }

    public static Font getFont(String name, int size, boolean bold,
            boolean italic) {
        int style = SWT.NORMAL;
        if (bold)
            style |= SWT.BOLD;
        if (italic)
            style |= SWT.ITALIC;
        FontData fd = new FontData(name, size, style);
        String key = toString(name, size, bold, italic);
        return getFont(key, new FontData[] { fd });
    }

    public static Font getFont(FontData fontData) {
        return getFont(null, new FontData[] { fontData });
    }

    public static Font getFont(String key) {
        return getFont(key, null);
    }

    public static Font getFont(FontData[] fontData) {
        return getFont(null, fontData);
    }

    public static FontData[] newName(FontData[] fontData, String name) {
        if (name == null || fontData == null)
            return fontData;

        FontData[] newFontData = new FontData[fontData.length];
        for (int i = 0; i < fontData.length; i++) {
            FontData old = fontData[i];
            newFontData[i] = new FontData(name, old.getHeight(), old.getStyle());
        }
        return newFontData;
    }

    public static FontData[] newHeight(FontData[] fontData, int height) {
        if (height < 0 || fontData == null)
            return fontData;
        FontData[] newFontData = new FontData[fontData.length];
        for (int i = 0; i < fontData.length; i++) {
            FontData old = fontData[i];
            newFontData[i] = new FontData(old.getName(), height, old.getStyle());
        }
        return newFontData;
    }

    public static FontData[] relativeHeight(FontData[] fontData, int deltaHeight) {
        if (deltaHeight == 0 || fontData == null)
            return fontData;
        FontData[] newFontData = new FontData[fontData.length];
        for (int i = 0; i < fontData.length; i++) {
            FontData old = fontData[i];
            newFontData[i] = new FontData(old.getName(), old.getHeight()
                    + deltaHeight, old.getStyle());
        }
        return newFontData;
    }

    public static FontData[] style(FontData[] fontData, Boolean bold,
            Boolean italic) {
        FontData[] newFontData = new FontData[fontData.length];
        for (int i = 0; i < fontData.length; i++) {
            FontData old = fontData[i];
            int newStyle = old.getStyle();
            if (bold != null) {
                if (bold.booleanValue()) {
                    newStyle |= SWT.BOLD;
                } else {
                    newStyle &= ~SWT.BOLD;
                }
            }
            if (italic != null) {
                if (italic.booleanValue()) {
                    newStyle |= SWT.ITALIC;
                } else {
                    newStyle &= ~SWT.ITALIC;
                }
            }
            newFontData[i] = new FontData(old.getName(), old.getHeight(),
                    newStyle);
        }
        return newFontData;
    }

    public static FontData[] bold(FontData[] fontData, boolean bold) {
        return style(fontData, Boolean.valueOf(bold), null);
    }

    public static FontData[] italic(FontData[] fontData, boolean italic) {
        return style(fontData, null, Boolean.valueOf(italic));
    }

    public static Font getNewName(Font font, String name) {
        if (font == null || name == null)
            return font;
        return getNewName(toString(font.getFontData()), name);
    }

    public static Font getNewHeight(Font font, int height) {
        if (font == null || height < 0)
            return font;
        return getNewHeight(toString(font.getFontData()), height);
    }

    public static Font getRelativeHeight(Font font, int deltaHeight) {
        if (font == null || deltaHeight == 0)
            return font;
        return getRelativeHeight(toString(font.getFontData()), deltaHeight);
    }

    public static Font getNewName(String key, String name) {
        if (key == null)
            return null;

        String newKey;
        if (name == null)
            newKey = key;
        else
            newKey = key + "@name=" + name; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (name == null)
                return reg.get(key);

            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, newName(fontData, name));
        }
        return null;
    }

    public static Font getNewHeight(String key, int height) {
        if (key == null)
            return null;

        String newKey;
        if (height < 0)
            newKey = key;
        else
            newKey = key + "@height=" + height; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (height < 0)
                return reg.get(key);
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, newHeight(fontData, height));
        }
        return null;
    }

    public static Font getRelativeHeight(String key, int deltaHeight) {
        if (key == null)
            return null;

        String newKey;
        if (deltaHeight == 0)
            newKey = key;
        else
            newKey = key + "@height+=" + deltaHeight; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (deltaHeight == 0)
                return reg.get(key);
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, relativeHeight(fontData, deltaHeight));
        }
        return null;
    }

    private static boolean isDefaultKey(String key) {
        return JFaceResources.DEFAULT_FONT.equals(key)
                || JFaceResources.DIALOG_FONT.equals(key)
                || JFaceResources.HEADER_FONT.equals(key)
                || JFaceResources.TEXT_FONT.equals(key)
                || JFaceResources.BANNER_FONT.equals(key);
    }

    public static Font getBold(FontData[] fontData) {
        return getBold(null, fontData);
    }

    public static Font getBold(Font font) {
        if (font == null)
            return font;
        return getBold(font.getFontData());
    }

    public static Font getBold(String key) {
        return getBold(key, null);
    }

    public static Font getBold(String key, int newHeight) {
        if (key == null)
            return null;

        String newKey;
        if (newHeight < 0)
            newKey = key + "@bold"; //$NON-NLS-1$
        else
            newKey = key + "@bold,height=" + newHeight; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (newHeight < 0)
                return reg.get(key);
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, bold(newHeight(fontData, newHeight), true));
        }
        return null;
    }

    public static Font getBoldRelative(String key, int relativeHeight) {
        if (key == null)
            return null;

        String newKey;
        if (relativeHeight == 0)
            newKey = key + "@bold"; //$NON-NLS-1$
        else
            newKey = key + "@bold,height+=" + relativeHeight; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey,
                    bold(relativeHeight(fontData, relativeHeight), true));
        }
        return null;
    }

    public static Font getNewStyle(String key, int newStyle) {
        if (key == null)
            return null;

        String newKey;
        if (newStyle < 0)
            newKey = key;
        else
            newKey = key + "@style=" + newStyle; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (newStyle < 0)
                return reg.get(key);
            FontData[] fontData = reg.getFontData(key);
            return getFont(
                    newKey,
                    style(fontData, ((newStyle & SWT.BOLD) != 0),
                            ((newStyle & SWT.ITALIC) != 0)));
        }
        return null;
    }

    public static Font getStyled(Font font, int newStyle) {
        if (font == null)
            return null;
        return getNewStyle(toString(font.getFontData()), newStyle);
    }

    public static Font getItalic(FontData[] fontData) {
        return getItalic(null, fontData);
    }

    public static Font getItalic(Font font) {
        if (font == null)
            return font;
        return getItalic(font.getFontData());
    }

    public static Font getItalic(String key) {
        return getItalic(key, null);
    }

}