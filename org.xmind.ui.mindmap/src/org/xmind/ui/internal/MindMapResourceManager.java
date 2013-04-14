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
package org.xmind.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.xmind.core.Core;
import org.xmind.core.IManifest;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookBuilder;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.MarkerGroup;
import org.xmind.core.internal.dom.StyleSheetImpl;
import org.xmind.core.internal.event.CoreEventSupport;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.marker.AbstractMarkerResource;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerResourceProvider;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.mindmap.IResourceManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.util.Logger;
import org.xmind.ui.util.ResourceFinder;

public class MindMapResourceManager implements IResourceManager {

    private static final String PATH_MARKERS = "markers/"; //$NON-NLS-1$

    private static final String PATH_USER_MARKERS = "markers/"; //$NON-NLS-1$

    private static final String PATH_STYLES = "styles/"; //$NON-NLS-1$

    private static final String USER_STYLES_TEMP_LOCATION = PATH_STYLES
            + "userStyles"; //$NON-NLS-1$

    private static final String USER_THEME_TEMP_LOCATION = PATH_STYLES
            + "userThemes"; //$NON-NLS-1$

    private static final String MARKER_SHEET_XML = "markerSheet.xml"; //$NON-NLS-1$

    private static final String MARKER_SHEET = "markerSheet"; //$NON-NLS-1$

    private static final String DEFAULT_STYLES_XML = "defaultStyles.xml"; //$NON-NLS-1$

    private static final String STYLES_XML = "styles.xml"; //$NON-NLS-1$

    private static final String THEMES_XML = "themes.xml"; //$NON-NLS-1$

    private static final String STYLES = "styles"; //$NON-NLS-1$

    private static final String THEMES = "themes"; //$NON-NLS-1$

    private static final String EXT_PROPERTIES = ".properties"; //$NON-NLS-1$

    private static class SystemMarkerResourceProvider implements
            IMarkerResourceProvider {

        public IMarkerResource getMarkerResource(IMarker marker) {
            return new SystemMarkerResource(marker);
        }

        public boolean isPermanent() {
            return true;
        }

    }

    private static class SystemMarkerResource extends AbstractMarkerResource {

        public SystemMarkerResource(IMarker marker) {
            super(marker, PATH_MARKERS);
        }

        public InputStream getInputStream() {
            URL url = getURL();
            if (url == null)
                return null;

            try {
                return url.openStream();
            } catch (IOException e) {
            }
            return null;
        }

        public OutputStream getOutputStream() {
            return null;
        }

        private URL getURL() {
            return find(getFullPath());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof SystemMarkerResource))
                return false;
            return super.equals(obj);
        }
    }

    private static class UserMarkerResourceProvider implements
            IMarkerResourceProvider {

        public IMarkerResource getMarkerResource(IMarker marker) {
            return new UserMarkerResource(marker);
        }

        public boolean isPermanent() {
            return false;
        }

    }

    private static class UserMarkerResource extends AbstractMarkerResource {

        public UserMarkerResource(IMarker marker) {
            super(marker, PATH_USER_MARKERS);
        }

        private File getFile() {
            String path = Core.getWorkspace().getAbsolutePath(getFullPath());
            if (path == null)
                return null;
            return new File(path);
        }

        public InputStream getInputStream() {
            File file = getFile();
            if (file != null)
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException e) {
                }
            return null;
        }

        public OutputStream getOutputStream() {
            File file = FileUtils.ensureFileParent(getFile());
            if (file != null)
                try {
                    return new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                }
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof UserMarkerResource))
                return false;
            return super.equals(obj);
        }

    }

    protected static class RecentMarkerGroup extends MarkerGroup implements
            ICoreEventSource {

        public static final RecentMarkerGroup instance = new RecentMarkerGroup();

        private static final int CAPACITY = 6;

        private List<IMarker> markers = new ArrayList<IMarker>(CAPACITY);

        private ICoreEventSupport eventSupport = new CoreEventSupport();

        private RecentMarkerGroup() {
        }

        public void addMarker(IMarker marker) {
            if (markers.contains(marker))
                return;

            while (markers.size() >= CAPACITY) {
                markers.remove(markers.size() - 1);
            }
            markers.add(0, marker);
            eventSupport.dispatchTargetChange(this, Core.MarkerAdd, marker);
        }

        public List<IMarker> getMarkers() {
            return markers;
        }

        public String getName() {
            return MindMapMessages.RecentUsed;
        }

        public void setSingleton(boolean singleton) {
        }

        public IMarkerSheet getOwnedSheet() {
            return null;
        }

        public IMarkerSheet getParent() {
            return null;
        }

        public boolean isSingleton() {
            return false;
        }

        public void removeMarker(IMarker marker) {
        }

        public void setName(String name) {
        }

        public String getId() {
            return "org.xmind.ui.RecentMarkerGroup"; //$NON-NLS-1$
        }

        public Object getRegisterKey() {
            return getId();
        }

        public ICoreEventRegistration registerCoreEventListener(String type,
                ICoreEventListener listener) {
            return eventSupport.registerCoreEventListener(this, type, listener);
        }

        public int hashCode() {
            return super.hashCode();
        }

        public ICoreEventSupport getCoreEventSupport() {
            return eventSupport;
        }

    }

    private IMarkerSheet systemMarkerSheet = null;

    private IMarkerSheet userMarkerSheet = null;

    private IMarkerGroup recentMarkerGroup = null;

    private IStyleSheet defaultStyleSheet = null;

    private IStyleSheet systemStyleSheet = null;

    private IWorkbook userStylesContainer = null;

    private IStyle blankTheme = null;

    private IStyle defaultTheme = null;

    private IStyleSheet systemThemeSheet = null;

    private IWorkbook userThemesContainer = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.IMarkerSheetManager#getSystemMarkerSheet()
     */
    public IMarkerSheet getSystemMarkerSheet() {
        if (systemMarkerSheet == null) {
            systemMarkerSheet = createSystemMarkerShet();
        }
        return systemMarkerSheet;
    }

    private IMarkerSheet createSystemMarkerShet() {
        URL url = find(PATH_MARKERS, MARKER_SHEET_XML);
        if (url != null) {
            try {
                IMarkerSheet sheet = Core.getMarkerSheetBuilder().loadFromURL(
                        url, new SystemMarkerResourceProvider());
                URL propUrl = find(PATH_MARKERS, MARKER_SHEET, EXT_PROPERTIES);
                if (propUrl != null) {
                    try {
                        InputStream propStream = propUrl.openStream();
                        if (propStream != null) {
                            try {
                                Core.getMarkerSheetBuilder().loadProperties(
                                        propStream, sheet);
                            } finally {
                                try {
                                    propStream.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.log(e,
                                "Failed to load system marker properties: " //$NON-NLS-1$
                                        + propUrl);
                    }
                }
                return sheet;
            } catch (Exception e) {
                Logger.log(e, "Failed to load system marker from: " + url); //$NON-NLS-1$
            }
        }
        return Core.getMarkerSheetBuilder().createMarkerSheet(
                new SystemMarkerResourceProvider());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.internal.IMarkerSheetManager#getUserMarkerSheet()
     */
    public IMarkerSheet getUserMarkerSheet() {
        if (userMarkerSheet == null) {
            userMarkerSheet = createUserMarkerSheet();
            initUserMarkerSheet(userMarkerSheet);
        }
        return userMarkerSheet;
    }

    public void saveUserMarkerSheet() {
        if (userMarkerSheet != null) {
            String path = Core.getWorkspace().getAbsolutePath(
                    PATH_USER_MARKERS + MARKER_SHEET_XML);
            File file = FileUtils.ensureFileParent(new File(path));
            try {
                userMarkerSheet.save(new FileOutputStream(file));
            } catch (Exception e) {
                Logger.log(e);
            }
        }
    }

    private void initUserMarkerSheet(IMarkerSheet sheet) {
        sheet.setParentSheet(getSystemMarkerSheet());
    }

    private IMarkerSheet createUserMarkerSheet() {
        String path = Core.getWorkspace().getAbsolutePath(
                PATH_USER_MARKERS + MARKER_SHEET_XML);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    return Core.getMarkerSheetBuilder().loadFromFile(file,
                            new UserMarkerResourceProvider());
                } catch (Exception e) {
                    Logger.log(e, "Failed to load user marker from: " + file); //$NON-NLS-1$
                }
            }
        }
        return Core.getMarkerSheetBuilder().createMarkerSheet(
                new UserMarkerResourceProvider());
    }

    public IMarkerGroup getRecentMarkerGroup() {
        if (recentMarkerGroup == null) {
            recentMarkerGroup = new RecentMarkerGroup();
        }
        return recentMarkerGroup;
    }

    public IStyleSheet getDefaultStyleSheet() {
        if (defaultStyleSheet == null) {
            defaultStyleSheet = createDefaultStyleSheet();
        }
        return defaultStyleSheet;
    }

    private IStyleSheet createDefaultStyleSheet() {
        URL url = find(PATH_STYLES, DEFAULT_STYLES_XML);
        if (url != null) {
            try {
                return Core.getStyleSheetBuilder().loadFromUrl(url);
            } catch (Exception e) {
                Logger.log(e, "Failed to load default styles: " + url); //$NON-NLS-1$
            }
        }
        return Core.getStyleSheetBuilder().createStyleSheet();
    }

    public IStyleSheet getSystemStyleSheet() {
        if (systemStyleSheet == null) {
            systemStyleSheet = createSystemStyleSheet();
        }
        return systemStyleSheet;
    }

    private IStyleSheet createSystemStyleSheet() {
        URL url = find(PATH_STYLES, STYLES_XML);
        if (url != null) {
            try {
                IStyleSheet sheet = Core.getStyleSheetBuilder()
                        .loadFromUrl(url);
                URL propUrl = find(PATH_STYLES, STYLES, EXT_PROPERTIES);
                if (propUrl != null) {
                    try {
                        InputStream propStream = propUrl.openStream();
                        if (propStream != null) {
                            try {
                                Core.getStyleSheetBuilder().loadProperties(
                                        propStream, sheet);
                            } finally {
                                try {
                                    propStream.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.log(e, "Failed to load saved style properties: " //$NON-NLS-1$
                                + propUrl);
                    }
                }
                return sheet;
            } catch (Exception e) {
                Logger.log(e, "Falied to load saved styles: " + url); //$NON-NLS-1$
            }
        }
        return Core.getStyleSheetBuilder().createStyleSheet();
    }

    public IStyleSheet getUserStyleSheet() {
        if (userStylesContainer == null) {
            userStylesContainer = createUserStylesContainer();
        }
        return userStylesContainer.getStyleSheet();
    }

    private IWorkbook createUserStylesContainer() {
        IWorkbook stylesContainer = null;
        String path = Core.getWorkspace().getAbsolutePath(
                USER_STYLES_TEMP_LOCATION);
        File file = new File(path);
        if (file.exists() && file.isDirectory()
                && new File(file, ArchiveConstants.CONTENT_XML).exists()) {
            try {
                stylesContainer = Core.getWorkbookBuilder()
                        .loadFromTempLocation(path);
            } catch (Exception e) {
                Logger.log(e, "Failed to load user styles from: " + file); //$NON-NLS-1$
            }
        }
        if (stylesContainer == null) {
            FileUtils.ensureDirectory(file);
            stylesContainer = Core.getWorkbookBuilder().createWorkbook();
        }
        stylesContainer.setTempLocation(path);
        return stylesContainer;
    }

    public void saveUserStyleSheet() {
        if (userStylesContainer != null) {
            try {
                userStylesContainer.saveTemp();
            } catch (Exception e) {
                Logger.log(e);
            }
        }
    }

    public IStyle getBlankTheme() {
        if (blankTheme == null) {
            blankTheme = Core.getStyleSheetBuilder().createStyleSheet()
                    .createStyle(IStyle.THEME);
            blankTheme.setName(MindMapMessages.DefaultTheme_title);
        }
        return blankTheme;
    }

    public IStyle getDefaultTheme() {
        if (defaultTheme == null) {
            defaultTheme = findDefaultTheme();
        }
        return defaultTheme;
    }

    private IStyle findDefaultTheme() {
        if (Platform.isRunning()) {
            String defaultId = MindMapUIPlugin.getDefault()
                    .getPreferenceStore()
                    .getString(PrefConstants.DEFUALT_THEME);
            if (defaultId != null && !"".equals(defaultId)) { //$NON-NLS-1$
                IStyle theme = getSystemThemeSheet().findStyle(defaultId);
                if (theme == null) {
//                    theme = getUserStyleSheet().findStyle(defaultId);
                    theme = getUserThemeSheet().findStyle(defaultId);
                }
                if (theme != null)
                    return theme;
            }
        }
        return getBlankTheme();
    }

    public void setDefaultTheme(String id) {
        IStyle theme = null;
        if (id != null && !"".equals(id)) { //$NON-NLS-1$
            theme = getBlankTheme();
            if (!id.equals(theme.getId())) {
                theme = getSystemThemeSheet().findStyle(id);
                if (theme == null) {
                    theme = getUserThemeSheet().findStyle(id);
                }
            }
        }
        if (theme == null)
            id = null;
        this.defaultTheme = theme;
        MindMapUIPlugin.getDefault().getPreferenceStore().setValue(
                PrefConstants.DEFUALT_THEME, id);
    }

    public IStyleSheet getSystemThemeSheet() {
        if (systemThemeSheet == null) {
            systemThemeSheet = createSystemThemeSheet();
        }
        return systemThemeSheet;
    }

    private IStyleSheet createSystemThemeSheet() {
        URL url = find(PATH_STYLES, THEMES_XML);
        if (url != null) {
            try {
                IStyleSheet sheet = Core.getStyleSheetBuilder()
                        .loadFromUrl(url);
                URL propUrl = find(PATH_STYLES, THEMES, EXT_PROPERTIES);
                if (propUrl != null) {
                    try {
                        InputStream propStream = propUrl.openStream();
                        if (propStream != null) {
                            try {
                                Core.getStyleSheetBuilder().loadProperties(
                                        propStream, sheet);
                            } finally {
                                try {
                                    propStream.close();
                                } catch (IOException e) {
                                }
                            }
                        }
                    } catch (Exception e) {
                        Logger.log(e, "Failed to load theme properties: " //$NON-NLS-1$
                                + propUrl);
                    }
                }
                return sheet;
            } catch (Exception e) {
                Logger.log(e, "Falied to load system themes: " + url); //$NON-NLS-1$
            }
        }
        return Core.getStyleSheetBuilder().createStyleSheet();
    }

    public IStyleSheet getUserThemeSheet() {
        if (userThemesContainer == null) {
            userThemesContainer = createUserThemeContainer();
        }
        IStyleSheet styleSheet = userThemesContainer.getStyleSheet();
        IManifest manifest = userThemesContainer.getManifest();
        ((StyleSheetImpl) styleSheet).setManifest(manifest);
        return styleSheet;
    }

    private IWorkbook createUserThemeContainer() {
        IWorkbook stylesContainer = null;
        String path = Core.getWorkspace().getAbsolutePath(
                USER_THEME_TEMP_LOCATION); //styles/userThemes
        File file = new File(path);
        IWorkbookBuilder workbookBuilder = Core.getWorkbookBuilder();
        if (file.exists() && file.isDirectory()
                && new File(file, ArchiveConstants.CONTENT_XML).exists()) {
            try {
                stylesContainer = workbookBuilder.loadFromTempLocation(path);
            } catch (Exception e) {
                Logger.log(e, "Failed to load user themes from: " + file); //$NON-NLS-1$
            }
        }
        if (stylesContainer == null) {
            FileUtils.ensureDirectory(file);
            stylesContainer = workbookBuilder.createWorkbook();
        }
        stylesContainer.setTempLocation(path);
        return stylesContainer;
    }

    public void saveUserThemeSheet() {
        if (userThemesContainer != null) {
            try {
                userThemesContainer.saveTemp();
            } catch (Exception e) {
            }
        }
    }

    private static URL find(String fullPath) {
        Bundle bundle = Platform.getBundle(MindMapUI.PLUGIN_ID);
        if (bundle != null) {
            return FileLocator.find(bundle, new Path(fullPath), null);
        }
        return null;
    }

    private static URL find(String mainPath, String subPath) {
        return find(mainPath + subPath);
    }

    private static URL find(String mainPath, String prefix, String suffix) {
        return ResourceFinder.findResource(MindMapUI.PLUGIN_ID, mainPath,
                prefix, suffix);
    }

}