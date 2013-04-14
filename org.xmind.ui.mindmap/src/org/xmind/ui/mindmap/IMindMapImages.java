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
package org.xmind.ui.mindmap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.ITopic;

public interface IMindMapImages {

    abstract class CursorFactory {

        private String id;

        public CursorFactory(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public abstract Cursor createCursor(IMindMapImages images);
    }

    String PATH = "icons/"; //$NON-NLS-1$
    String PATH_E = PATH + "e/"; //$NON-NLS-1$
    String PATH_D = PATH + "d/"; //$NON-NLS-1$
    String PATH_POINTERS = PATH + "pointers/"; //$NON-NLS-1$
    String PATH_WIZ = PATH + "wizban/"; //$NON-NLS-1$
    String PATH_PROP = PATH + "prop/"; //$NON-NLS-1$

    String XMIND_ICON = PATH + "xmind.16.gif"; //$NON-NLS-1$

    String ACTUAL_SIZE = "actualsize.gif"; //$NON-NLS-1$
    String ACTUAL_SIZE_SMALL = "actualsize_small.gif"; //$NON-NLS-1$
    String ADD = "add.gif"; //$NON-NLS-1$
    String ADD_COLUMN = "add_column.gif"; //$NON-NLS-1$
    String ADD_MARKER = "add_marker.gif"; //$NON-NLS-1$
    String ADD_THEME = "add_theme.gif"; //$NON-NLS-1$
    String ATTACHMENT = "attachment.gif"; //$NON-NLS-1$
    String BOUNDARY = "boundary.gif"; //$NON-NLS-1$
    String CENTRAL = "central.gif"; //$NON-NLS-1$
    String DELETE = "delete_edit.gif"; //$NON-NLS-1$
    String DONE = "step_done.gif"; //$NON-NLS-1$
    String DOWN_TRIANGLE = "down_triangle.gif"; //$NON-NLS-1$
    String DRILL_UP = "drill_up.gif"; //$NON-NLS-1$
    String DRILL_DOWN = "drill_down.gif"; //$NON-NLS-1$
    String FILTER = "filter.gif"; //$NON-NLS-1$
    String FIT_SELECTION = "fitselection.gif"; //$NON-NLS-1$
    String FIT_SIZE = "fitsize.gif"; //$NON-NLS-1$
    String FLOATING = "floating.gif"; //$NON-NLS-1$
    String HELP = "linkto_help.gif"; //$NON-NLS-1$
    String HYPERLINK = "discovery.gif"; //$NON-NLS-1$
    String INSERT_AFTER = "insertafter.gif"; //$NON-NLS-1$
    String INSERT_BEFORE = "insertbefore.gif"; //$NON-NLS-1$
    String INSERT_FLOATING_CENTRAL = "insertfloating_central.gif"; //$NON-NLS-1$
    String INSERT_FLOATING_MAIN = "insertfloating_main.gif"; //$NON-NLS-1$
    String INSERT_IMAGE = "insertimage.gif"; //$NON-NLS-1$
    String INSERT_PARENT = "insertparent.gif"; //$NON-NLS-1$
    String INSERT_SUB = "insertsub.gif"; //$NON-NLS-1$
    String LABEL = "label.gif"; //$NON-NLS-1$
    String LEGEND = "legend.gif"; //$NON-NLS-1$
    String LEGEND_RESET = "legend_reset.gif"; //$NON-NLS-1$
    String LINK = "link.gif"; //$NON-NLS-1$
    String LOCK = "lock.gif"; //$NON-NLS-1$
    String MAIN = "main.gif"; //$NON-NLS-1$
    String MAP = "map.gif"; //$NON-NLS-1$
    String MARKERS = "markers.gif"; //$NON-NLS-1$
    String NEW = "new.gif"; //$NON-NLS-1$
    String NEW_DIALOG = "new_dialog.gif"; //$NON-NLS-1$
    String NEW_SHEET_AS = "new_sheet_as.gif"; //$NON-NLS-1$
    String NEWMAP = "newmap.gif"; //$NON-NLS-1$
    String NEWMAP_DIALOG = "newmap_dialog.gif"; //$NON-NLS-1$
    String NOTES = "notes.gif"; //$NON-NLS-1$
    String OPEN = "fldr_obj.gif"; //$NON-NLS-1$

    String PROPERTIES = "prop_ps.gif"; //$NON-NLS-1$
    String REGISTER = "prop_ps.gif"; //$NON-NLS-1$
    String REMOVE = "trash.gif"; //$NON-NLS-1$
    String RELATIONSHIP = "relationship.gif"; //$NON-NLS-1$
    String SHEET = "sheet.gif"; //$NON-NLS-1$
    String SHOWALL = "showall.gif"; //$NON-NLS-1$
    String SHOWOTHER = "showother.gif"; //$NON-NLS-1$
    String SMART_MODE = "smartmode_co.gif"; //$NON-NLS-1$
    String STOP = "nav_stop.gif"; //$NON-NLS-1$
    String STRIKETHROUGH = "strikethrough.gif"; //$NON-NLS-1$
    String STYLES = "javaassist_co.gif"; //$NON-NLS-1$
    String SUMMARY = "summary.gif"; //$NON-NLS-1$
    String SUMMARY_TOPIC = "summary_topic.gif"; //$NON-NLS-1$
    String SYNCED = "synced.gif"; //$NON-NLS-1$
    String ZOOMOUT = "zoomout.gif"; //$NON-NLS-1$
    String ZOOMIN = "zoomin.gif"; //$NON-NLS-1$
    String ZOOMOUT_SMALL = "zoomout_small.gif"; //$NON-NLS-1$
    String ZOOMIN_SMALL = "zoomin_small.gif"; //$NON-NLS-1$
    String TEMPLATE = "template.gif"; //$NON-NLS-1$
    String THEME = "theme.gif"; //$NON-NLS-1$
    String TOPIC = "topic.gif"; //$NON-NLS-1$
    String WORKBOOK = "workbook.gif"; //$NON-NLS-1$
    String URL = "link_obj.gif"; //$NON-NLS-1$

    String ALPHA = "alpha.gif"; //$NON-NLS-1$

    String UNLOCK = "unlock.gif"; //$NON-NLS-1$
    String NUMBERING_INHERIT = "inherited.gif"; //$NON-NLS-1$

    String ALIGN_CENTER = "align_center.gif"; //$NON-NLS-1$
    String ALIGN_LEFT = "align_left.gif"; //$NON-NLS-1$
    String ALIGN_RIGHT = "align_right.gif"; //$NON-NLS-1$

    String FONT = "text_font.gif"; //$NON-NLS-1$
    String TEXT_BACKGROUND = "text_background.gif"; //$NON-NLS-1$
    String TEXT_FOREGROUND = "text_foreground.gif"; //$NON-NLS-1$
    String TEXT_BOLD = "text_bold.gif"; //$NON-NLS-1$
    String TEXT_FONT = "text_font.gif"; //$NON-NLS-1$
    String TEXT_ITALIC = "text_italic.gif"; //$NON-NLS-1$
    String TEXT_STRIKEOUT = "text_strikeout.gif"; //$NON-NLS-1$
    String TEXT_UNDERLINE = "text_underline.gif"; //$NON-NLS-1$
    String LINE_INDENT = "text_indent.gif"; //$NON-NLS-1$
    String LINE_OUTDENT = "text_outdent.gif"; //$NON-NLS-1$

    String UNDO = "undo.gif"; //$NON-NLS-1$
    String REDO = "redo.gif"; //$NON-NLS-1$

    String DEFAULT_THEME = "defaultTheme.gif"; //$NON-NLS-1$

    String STAR = "star.gif"; //$NON-NLS-1$

//    String BRAINY = "brainy.16.gif"; //$NON-NLS-1$
//    String BRAINY_FILE = "xmap_file.gif"; //$NON-NLS-1$
//    String ABOUT = "about2.png"; //$NON-NLS-1$

    String IMPORT = "import_wiz.gif"; //$NON-NLS-1$
    String EXPORT = "export_wiz.gif"; //$NON-NLS-1$

    String WIZ_NEW = "new_wiz.png"; //$NON-NLS-1$
    String WIZ_IMPORT = "import_wiz.png"; //$NON-NLS-1$
    String WIZ_EXPORT = "export_wiz.png"; //$NON-NLS-1$
    String WIZ_REGISTER = "extstr_wiz.png"; //$NON-NLS-1$
    //    String   WIZ_WELCOME             = "new_wiz.png";                                 //$NON-NLS-1$
    String WIZ_EXTRACTTEMP = "product_wiz.png"; //$NON-NLS-1$
    String WIZ_SELECTMAP = "saveas_wiz.png"; //$NON-NLS-1$

    String OPAQUE = PATH_PROP + "opaque.gif"; //$NON-NLS-1$

    String LINE_THINNEST = PATH_PROP + "line_thinnest.gif"; //$NON-NLS-1$
    String LINE_THIN = PATH_PROP + "line_thin.gif"; //$NON-NLS-1$
    String LINE_MEDIUM = PATH_PROP + "line_medium.gif"; //$NON-NLS-1$
    String LINE_FAT = PATH_PROP + "line_fat.gif"; //$NON-NLS-1$
    String LINE_FATTEST = PATH_PROP + "line_fattest.gif"; //$NON-NLS-1$

    String PATTERN_SOLID = PATH_PROP + "pattern_solid.gif"; //$NON-NLS-1$
    String PATTERN_DOT = PATH_PROP + "pattern_dot.gif"; //$NON-NLS-1$
    String PATTERN_DASH = PATH_PROP + "pattern_dash.gif"; //$NON-NLS-1$
    String PATTERN_DASHDOT = PATH_PROP + "pattern_dashdot.gif"; //$NON-NLS-1$
    String PATTERN_DASHDOTDOT = PATH_PROP + "pattern_dashdotdot.gif"; //$NON-NLS-1$

    String DIRECTORY = PATH + "misc/directory.gif"; //$NON-NLS-1$
    String RIGHT = PATH + "misc/complete_status.gif"; //$NON-NLS-1$
    String WRONG = PATH + "misc/close_view.gif"; //$NON-NLS-1$
    String BLANK = PATH + "misc/blank.gif"; //$NON-NLS-1$
    String ENCRYPTED_THUMBNAIL = PATH + "misc/encrypted_thumbnail.jpg"; //$NON-NLS-1$
    String DEFAULT_THUMBNAIL = PATH + "misc/default_thumbnail.jpg"; //$NON-NLS-1$
    String MISSING_IMAGE = PATH + "misc/missing_image.png"; //$NON-NLS-1$

    String UNKNOWN_FILE = "unknownfile.gif"; //$NON-NLS-1$

    CursorFactory CURSOR_ADD = new CursorFactory("add") { //$NON-NLS-1$
        public Cursor createCursor(IMindMapImages images) {
            return images.createCursor(getId(), 4, 3);
        }
    };

    CursorFactory CURSOR_BROWSE = new CursorFactory("browse") { //$NON-NLS-1$
        public Cursor createCursor(IMindMapImages images) {
            return images.createCursor(getId(), 11, 9);
        }
    };

    CursorFactory CURSOR_FORBID = new CursorFactory("forbid") { //$NON-NLS-1$
        public Cursor createCursor(IMindMapImages images) {
            return images.createCursor(getId(), 6, 3);
        }
    };

    CursorFactory CURSOR_MOVE = new CursorFactory("move") { //$NON-NLS-1$
        public Cursor createCursor(IMindMapImages images) {
            return images.createCursor(getId(), 9, 8);
        }
    };

    CursorFactory CURSOR_RELATIONSHIP = new CursorFactory("relationship") { //$NON-NLS-1$
        public Cursor createCursor(IMindMapImages images) {
            return images.createCursor(getId(), 6, 3);
        }
    };

    CursorFactory CURSOR_EMPTY = new CursorFactory("empty") { //$NON-NLS-1$
        public Cursor createCursor(IMindMapImages images) {
            int w = 16;
            int h = 16;
            Display display = Display.getCurrent();
            if (display != null) {
                Point[] sizes = display.getCursorSizes();
                if (sizes != null && sizes.length > 0) {
                    Point size = sizes[0];
                    w = size.x;
                    h = size.y;
                }
            }
            ImageData source = new ImageData(w, h, 16, new PaletteData(
                    0xff0000, 0xff00, 0xff));
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    source.setPixel(x, y, 0xffffff);
                }
            }
            ImageData mask = new ImageData(w, h, 16, new PaletteData(0xff0000,
                    0xff00, 0xff));
            return new Cursor(display, source, mask, w / 2, h / 2);
        }
    };

    ImageDescriptor get(String fullPath);

    ImageDescriptor get(String fileName, String parentPath);

    ImageDescriptor get(String fileName, boolean enabled);

    ImageDescriptor getElementIcon(Object element, boolean enabled);

    ImageDescriptor getTopicIcon(ITopic topic, boolean enabled);

    ImageDescriptor getWizBan(String fileName);

    ImageDescriptor getFileIcon(String adaptedPath);

    ImageDescriptor getFileIcon(String adaptedPath,
            boolean returnNullIfUnidentifiable);

    Cursor getCursor(CursorFactory factory);

    Cursor createCursor(String name, int hotX, int hotY);

}