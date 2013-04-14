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
package org.xmind.ui.internal.properties;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.ColorSelection;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.font.FontDialog;
import org.xmind.ui.font.IFontChooser;
import org.xmind.ui.font.IFontChooserListener;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.resources.FontUtils.IFontNameListCallback;
import org.xmind.ui.richtext.AlignmentGroup;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.style.TextStyleData;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.viewers.MComboViewer;

public class FontPropertySectionPart extends StyledPropertySectionPart {

    private class FontLabelProvider extends LabelProvider {

    }

    private class FontSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof String) {
                changeFontName((String) o);
            }
        }

    }

    private class ChooseFontAction extends Action {

        public ChooseFontAction() {
            super(null, MindMapUI.getImages().get(IMindMapImages.TEXT_FONT,
                    true));
            setToolTipText(PropertyMessages.ChooseFont_toolTip);
        }

        public void run() {
            if (currentTextStyle == null)
                return;

            FontDialog dialog = new FontDialog(getContainer()
                    .getContainerSite().getShell());

            if (currentTextStyle != null) {
                if (currentTextStyle.name != null)
                    dialog.setFontName(currentTextStyle.name);
                if (currentTextStyle.height > 0)
                    dialog.setFontHeight(currentTextStyle.height);
                dialog.setBold(currentTextStyle.bold);
                dialog.setItalic(currentTextStyle.italic);
                if (currentTextStyle.color != null)
                    dialog.setColor(currentTextStyle.color);
                dialog.setUnderline(currentTextStyle.underline);
                dialog.setStrikeout(currentTextStyle.strikeout);
            }

            dialog.addFontChooserListener(new IFontChooserListener() {
                public void fontChanged(IFontChooser source) {
                    showTempFont(source);
                }

            });

            int ret = dialog.open();
            showTempFont(null);
            if (ret == FontDialog.OK) {
                changeFont(dialog.getFontName(), dialog.getFontHeight(), dialog
                        .getBold(), dialog.getItalic(), dialog.getUnderline(),
                        dialog.getStrikeout(), dialog.getColor());
            }
        }

    }

    private class SizeLabelProvider extends LabelProvider {

    }

    private class SizeSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof String) {
                try {
                    int i = Integer.parseInt((String) o);
                    FONT_SIZE_LIST.add(i);
                    changeFontSize(i);
                } catch (NumberFormatException e) {
                }
            } else if (o instanceof Integer) {
                changeFontSize(((Integer) o).intValue());
            }
        }

    }

    private class BoldAction extends Action {
        public BoldAction() {
            super(null, MindMapUI.getImages().get(IMindMapImages.TEXT_BOLD,
                    true));
            setToolTipText(PropertyMessages.Bold_toolTip);
            setChecked(false);
        }

        public void run() {
            super.run();
            changeBold(isChecked());
        }
    }

    private class ItalicAction extends Action {
        public ItalicAction() {
            super(null, MindMapUI.getImages().get(IMindMapImages.TEXT_ITALIC,
                    true));
            setToolTipText(PropertyMessages.Italic_toolTip);
            setChecked(false);
        }

        public void run() {
            super.run();
            changeItalic(isChecked());
        }
    }

    private class StrikeoutAction extends Action {
        public StrikeoutAction() {
            super(null, MindMapUI.getImages().get(
                    IMindMapImages.TEXT_STRIKEOUT, true));
            setToolTipText(PropertyMessages.Strikeout_toolTip);
            setChecked(false);
        }

        public void run() {
            super.run();
            changeStrikeout(isChecked());
        }
    }

    private class AlignLeftAction extends Action {
        public AlignLeftAction() {
            super(PropertyMessages.TextAlignLeft_text, MindMapUI.getImages()
                    .get(IMindMapImages.ALIGN_LEFT, true));
            setToolTipText(PropertyMessages.TextAlignLeft_toolTip);
            setChecked(false);
        }

        public void run() {
            changeAlignLeft();
        }
    }

    private class AlignCenterAction extends Action {
        public AlignCenterAction() {
            super(PropertyMessages.TextAlignCenter_text, MindMapUI.getImages()
                    .get(IMindMapImages.ALIGN_CENTER, true));
            setToolTipText(PropertyMessages.TextAlignCenter_toolTip);
            setChecked(false);
        }

        public void run() {
            changeAlignCenter();
        }
    }

    private class AlignRightAction extends Action {
        public AlignRightAction() {
            super(PropertyMessages.TextAlignRight_text, MindMapUI.getImages()
                    .get(IMindMapImages.ALIGN_RIGHT, true));
            setToolTipText(PropertyMessages.TextAlignRight_toolTip);
            setChecked(false);
        }

        public void run() {
            changeAlignRight();
        }
    }

    private class ColorOpenListener implements IOpenListener {

        public void open(OpenEvent event) {
            changeTextColor((IColorSelection) event.getSelection());
        }

    }

    private static Set<Integer> FONT_SIZE_LIST = new TreeSet<Integer>();

    private IAction boldAction;

    private IAction italicAction;

    private IAction strikeoutAction;

    private MComboViewer fontViewer;

    private MComboViewer sizeViewer;

    private TextStyleData currentTextStyle;

    private ColorPicker textColorPicker;

    private AlignmentGroup alignGroup;

    private IAction alignLeftAction;

    private IAction alignCenterAction;

    private IAction alignRightAction;

    protected void createContent(Composite parent) {
        Composite line1 = new Composite(parent, SWT.NONE);
        line1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout1 = new GridLayout(2, false);
        layout1.marginWidth = 0;
        layout1.marginHeight = 0;
        layout1.horizontalSpacing = 3;
        layout1.verticalSpacing = 3;
        line1.setLayout(layout1);
        createLineContent1(line1);

        Composite line2 = new Composite(parent, SWT.NONE);
        line2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout2 = new GridLayout(2, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = 3;
        layout2.verticalSpacing = 3;
        line2.setLayout(layout2);
        createLineContent2(line2);
    }

    private void createLineContent1(Composite parent) {
        fontViewer = new MComboViewer(parent, MComboViewer.FILTERED);
        fontViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        fontViewer.getControl().setToolTipText(
                PropertyMessages.FontFamily_toolTip);
        fontViewer.setContentProvider(new ArrayContentProvider());
        fontViewer.setLabelProvider(new FontLabelProvider());
        fontViewer.getControl().setEnabled(false);
        IFontNameListCallback callback = new IFontNameListCallback() {
            public void setAvailableFontNames(List<String> fontNames) {
                if (fontViewer == null || fontViewer.getControl().isDisposed())
                    return;
                fontViewer.getControl().setEnabled(true);
                fontViewer.setInput(fontNames);
                TextStyleData ts = calcCurrentTextStyle();
                if (ts != null && ts.name != null) {
                    fontViewer.setSelection(new StructuredSelection(ts.name));
                }
                fontViewer
                        .addSelectionChangedListener(new FontSelectionChangedListener());
            }
        };
        FontUtils.fetchAvailableFontNames(parent.getDisplay(), callback);

        ToolBarManager fontBar = new ToolBarManager(SWT.FLAT);
        fontBar.add(new ChooseFontAction());
        ToolBar barControl = fontBar.createControl(parent);
        barControl.setLayoutData(new GridData(GridData.END, GridData.CENTER,
                false, false));
    }

    private void createLineContent2(Composite parent) {
        sizeViewer = new MComboViewer(parent, MComboViewer.FILTERED);
        sizeViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        sizeViewer.getControl().setToolTipText(
                PropertyMessages.FontSize_toolTip);
        sizeViewer.setContentProvider(new ArrayContentProvider());
        sizeViewer.setLabelProvider(new SizeLabelProvider());
        sizeViewer.setPermitsUnprovidedElement(true);
        if (FONT_SIZE_LIST.isEmpty()) {
            FONT_SIZE_LIST.addAll(Arrays.asList(8, 9, 10, 11, 12, 14, 16, 18,
                    20, 22, 24, 36, 48, 56));
        }
        sizeViewer.setInput(FONT_SIZE_LIST);
        sizeViewer
                .addSelectionChangedListener(new SizeSelectionChangedListener());

        ToolBarManager styleBar = new ToolBarManager(SWT.FLAT);
        styleBar.add(boldAction = new BoldAction());
        styleBar.add(italicAction = new ItalicAction());
        styleBar.add(strikeoutAction = new StrikeoutAction());

        textColorPicker = new ColorPicker(
                ColorPicker.AUTO | ColorPicker.CUSTOM, PaletteContents
                        .getDefault());
        textColorPicker.getAction().setToolTipText(
                PropertyMessages.TextColor_toolTip);
        textColorPicker.addOpenListener(new ColorOpenListener());
        styleBar.add(textColorPicker);

        alignGroup = new AlignmentGroup();
        alignGroup.add(alignLeftAction = new AlignLeftAction());
        alignGroup.add(alignCenterAction = new AlignCenterAction());
        alignGroup.add(alignRightAction = new AlignRightAction());
        styleBar.add(alignGroup);

        ToolBar barControl = styleBar.createControl(parent);
        barControl.setLayoutData(new GridData(GridData.END, GridData.CENTER,
                false, false));

    }

    protected void doRefresh() {
        TextStyleData ts = calcCurrentTextStyle();
        currentTextStyle = ts;
        if (fontViewer != null && !fontViewer.getControl().isDisposed()
                && fontViewer.getControl().isEnabled()) {
            fontViewer
                    .setSelection(ts == null || ts.name == null ? StructuredSelection.EMPTY
                            : new StructuredSelection(ts.name));
        }
        if (sizeViewer != null && !sizeViewer.getControl().isDisposed()) {
            sizeViewer
                    .setSelection(ts == null || ts.height < 0 ? StructuredSelection.EMPTY
                            : new StructuredSelection(Integer
                                    .valueOf(ts.height)));
        }
        if (boldAction != null)
            boldAction.setChecked(ts != null && ts.bold);
        if (italicAction != null)
            italicAction.setChecked(ts != null && ts.italic);
        if (strikeoutAction != null)
            strikeoutAction.setChecked(ts != null && ts.strikeout);
        if (textColorPicker != null)
            updateColorPicker(textColorPicker);

        switch (ts == null ? PositionConstants.LEFT : ts.align) {
        case PositionConstants.LEFT:
            if (alignLeftAction != null)
                alignLeftAction.setChecked(ts != null);
            break;
        case PositionConstants.CENTER:
            if (alignCenterAction != null)
                alignCenterAction.setChecked(ts != null);
            break;
        case PositionConstants.RIGHT:
            if (alignRightAction != null)
                alignRightAction.setChecked(ts != null);
            break;
        }
    }

    private void updateColorPicker(ColorPicker picker) {
        if (currentTextStyle == null || currentTextStyle.color == null) {
            picker.setSelection(ColorSelection.EMPTY);
            picker.setAutoColor(null);
        } else {
            IGraphicalPart part = getGraphicalPart(getSelectedElements()[0]);
            IStyleSelector ss = getStyleSelector(part);
            String autoValue = ss.getAutoValue(part, Styles.TextColor);
            RGB autoColor = StyleUtils.convertRGB(Styles.TextColor, autoValue);
            picker.setAutoColor(autoColor);
            String userValue = ss.getUserValue(part, Styles.TextColor);
            int type;
            if (userValue == null) {
                type = IColorSelection.AUTO;
                userValue = autoValue;
            } else {
                type = IColorSelection.CUSTOM;
            }
            if (type != IColorSelection.AUTO && Styles.NONE.equals(userValue)) {
                type = IColorSelection.NONE;
            }
            RGB color = StyleUtils.convertRGB(Styles.TextColor, userValue);
            picker.setSelection(new ColorSelection(type, color));
        }
    }

    protected TextStyleData calcCurrentTextStyle() {
        TextStyleData textStyle = null;
        IViewer viewer = getActiveViewer();
        if (viewer != null) {
            for (Object o : getSelectedElements()) {
                IGraphicalPart part = getGraphicalPart(o, viewer);
                if (part != null) {
                    IStyleSelector ss = getStyleSelector(part);
                    TextStyleData ts = calcTextStyle(part, ss);
                    if (ts == null)
                        return null;
                    textStyle = mergeStyle(textStyle, ts);
                    if (textStyle.name == null && textStyle.height < 0
                            && textStyle.color == null && !textStyle.italic
                            && !textStyle.bold && !textStyle.underline
                            && !textStyle.strikeout
                    /* && textStyle.align == 0 */) {
                        textStyle = null;
                        break;
                    }
                }
            }
        }
        return textStyle;
    }

    private TextStyleData mergeStyle(TextStyleData source, TextStyleData target) {
        if (source == null)
            return new TextStyleData(target);
        if (source.name != null && !source.name.equals(target.name))
            source.name = null;
        if (source.height > 0 && source.height != target.height)
            source.height = -1;
        if (source.italic && !target.italic)
            source.italic = false;
        if (source.bold && !target.bold)
            source.bold = false;
        if (source.color != null && !source.color.equals(target.color))
            source.color = null;
        if (source.strikeout && !target.strikeout)
            source.strikeout = false;
        if (source.underline && !target.underline)
            source.underline = false;

        if (source.align != target.align)
            source.align = SWT.LEFT;
        return source;
    }

    private TextStyleData calcTextStyle(IGraphicalPart part, IStyleSelector ss) {
        return StyleUtils.getTextStyleData(part, ss, null);
    }

    public void setFocus() {
        if (fontViewer != null && !fontViewer.getControl().isDisposed()) {
            fontViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        fontViewer = null;
        sizeViewer = null;
        currentTextStyle = null;
        boldAction = null;
        italicAction = null;
        strikeoutAction = null;
        currentTextStyle = null;
        textColorPicker = null;

        alignLeftAction = null;
        alignCenterAction = null;
        alignRightAction = null;
    }

    private void changeFontName(String name) {
        sendRequest(addStyle(
                createStyleRequest(CommandMessages.Command_ModifyFont),
                Styles.FontFamily, name));
    }

    private void changeFont(String fontName, int fontHeight, boolean bold,
            boolean italic, boolean underline, boolean strikeout, RGB color) {
        Request req = createStyleRequest(CommandMessages.Command_ModifyFont);
        if (fontName != null)
            addStyle(req, Styles.FontFamily, fontName);
        if (fontHeight > 0)
            addStyle(req, Styles.FontSize, StyleUtils.addUnitPoint(fontHeight));
        addStyle(req, Styles.FontWeight, bold ? Styles.FONT_WEIGHT_BOLD
                : Styles.NORMAL);
        addStyle(req, Styles.FontStyle, italic ? Styles.FONT_STYLE_ITALIC
                : Styles.NORMAL);
        addStyle(req, Styles.TextDecoration, StyleUtils.toTextDecoration(
                underline, strikeout));
        addStyle(req, Styles.TextColor, color == null ? null : ColorUtils
                .toString(color));
        sendRequest(req);
    }

    private void changeFontSize(int fontHeight) {
        sendRequest(addStyle(
                createStyleRequest(CommandMessages.Command_ModifyFont),
                Styles.FontSize, StyleUtils.addUnitPoint(fontHeight)));
    }

    private void changeBold(boolean bold) {
        sendRequest(addStyle(
                createStyleRequest(CommandMessages.Command_ModifyFont),
                Styles.FontWeight, bold ? Styles.FONT_WEIGHT_BOLD
                        : Styles.NORMAL));
    }

    private void changeItalic(boolean italic) {
        sendRequest(addStyle(
                createStyleRequest(CommandMessages.Command_ModifyFont),
                Styles.FontStyle, italic ? Styles.FONT_STYLE_ITALIC
                        : Styles.NORMAL));
    }

    private void changeStrikeout(boolean strikeout) {
        String decoration = null;
        if (currentTextStyle != null && currentTextStyle.underline) {
            decoration = Styles.TEXT_DECORATION_UNDERLINE;
        }
        if (decoration == null) {
            decoration = strikeout ? Styles.TEXT_DECORATION_LINE_THROUGH
                    : Styles.NORMAL;
        } else if (strikeout) {
            decoration = decoration + " " + Styles.TEXT_DECORATION_LINE_THROUGH; //$NON-NLS-1$
        }
        sendRequest(addStyle(
                createStyleRequest(CommandMessages.Command_ModifyFont),
                Styles.TextDecoration, decoration));
    }

    private void changeAlignLeft() {
        Request leftReq = createStyleRequest(CommandMessages.Command_TextAlignLeft);
        Request request = addStyle(leftReq, Styles.TextAlign, Styles.ALIGN_LEFT);
        sendRequest(request);
    }

    private void changeAlignCenter() {
        Request centerReq = createStyleRequest(CommandMessages.Command_TextAlignCenter);
        Request request = addStyle(centerReq, Styles.TextAlign,
                Styles.ALIGN_CENTER);
        sendRequest(request);
    }

    private void changeAlignRight() {
        Request rightReq = createStyleRequest(CommandMessages.Command_TextAlignRight);
        Request request = addStyle(rightReq, Styles.TextAlign,
                Styles.ALIGN_RIGHT);
        sendRequest(request);
    }

    private void changeTextColor(IColorSelection selection) {
        changeColor(selection, Styles.TextColor,
                CommandMessages.Command_ModifyTextColor);
    }

    private void showTempFont(IFontChooser source) {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer == null)
            return;

        TextStyle textStyle;
        if (source == null) {
            textStyle = null;
        } else {
            textStyle = new TextStyle(FontUtils.getFont(source.getFontName(),
                    source.getFontHeight(), source.getBold(), source
                            .getItalic()), ColorUtils.getColor(source
                    .getColor()), null);
            textStyle.strikeout = source.getStrikeout();
            textStyle.underline = source.getUnderline();
        }
        for (Object o : getSelectedElements()) {
            IGraphicalPart part = getGraphicalPart(o, viewer);
            if (part != null) {
                IGraphicalPart p = part instanceof ITopicPart ? ((ITopicPart) part)
                        .getOwnerBranch()
                        : part;
//                (ITitleTextPart) part
//                        .getAdapter(ITitleTextPart.class);
                if (p != null) {
                    if (textStyle == null) {
                        MindMapUtils.flushCache(p, MindMapUI.CACHE_TEXT_STYLE);
                    } else {
                        MindMapUtils.setCache(p, MindMapUI.CACHE_TEXT_STYLE,
                                textStyle);
                    }
                    p.refresh();
                }
            }
        }
    }
}