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
package org.xmind.ui.internal.print;

import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.PositionConstants;

public class PrintConstants {

    public static final String PRINT_DIALOG_ID = "org.xmind.ui.PrintDialog"; //$NON-NLS-1$

    public static final String NO_BACKGROUND = "noBackground"; //$NON-NLS-1$

    public static final String NO_BORDER = "noBorder"; //$NON-NLS-1$

    public static final String LEFT_MARGIN = "leftMargin"; //$NON-NLS-1$

    public static final String RIGHT_MARGIN = "rightMargin"; //$NON-NLS-1$

    public static final String TOP_MARGIN = "topMargin"; //$NON-NLS-1$

    public static final String BOTTOM_MARGIN = "bottomMargin"; //$NON-NLS-1$

    public static final String MARGIN_UNIT = "marginUnit"; //$NON-NLS-1$

    public static final String HEADER_TEXT = "headerText"; //$NON-NLS-1$

    public static final String HEADER_ALIGN = "headerAlign"; //$NON-NLS-1$

    public static final String HEADER_FONT = "headerFont"; //$NON-NLS-1$

    public static final String FOOTER_TEXT = "footerText"; //$NON-NLS-1$

    public static final String FOOTER_ALIGN = "footerAlign"; //$NON-NLS-1$

    public static final String FOOTER_FONT = "footerFont"; //$NON-NLS-1$

    public static final String LEFT = "left"; //$NON-NLS-1$

    public static final String CENTER = "center"; //$NON-NLS-1$

    public static final String RIGHT = "right"; //$NON-NLS-1$

    public static final double DEFAULT_MARGIN = 0.2d;

    public static final String INCH = "inch"; //$NON-NLS-1$

    public static final String MILLIMETER = "milliter"; //$NON-NLS-1$

    public static final List<String> UNITS = Arrays.asList(INCH, MILLIMETER);

    public static final String DEFAULT_HEADER_ALIGN = CENTER;

    public static final String DEFAULT_FOOTER_ALIGN = RIGHT;

    public static final String DEFAULT_HEADER_TEXT = ""; //$NON-NLS-1$

    public static final String DEFAULT_FOOTER_TEXT = ""; //$NON-NLS-1$

    public static final String ORIENTATION = "orientation"; //$NON-NLS-1$

    public static int toDraw2DAlignment(String alignValue, int defaultAlignment) {
        if (LEFT.equals(alignValue))
            return PositionConstants.LEFT;
        if (CENTER.equals(alignValue))
            return PositionConstants.CENTER;
        if (RIGHT.equals(alignValue))
            return PositionConstants.RIGHT;
        return defaultAlignment;
    }

}