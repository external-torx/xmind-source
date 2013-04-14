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
package org.xmind.ui.internal.editor;

import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IEditDomainListener;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ISelectionStack;
import org.xmind.gef.SelectionStack;
import org.xmind.gef.draw2d.IRelayeredPane;
import org.xmind.gef.draw2d.ISkylightLayer;
import org.xmind.gef.service.CenterPresercationService;
import org.xmind.gef.service.FeedbackService;
import org.xmind.gef.service.IAnimationService;
import org.xmind.gef.service.IFeedbackService;
import org.xmind.gef.service.IImageRegistryService;
import org.xmind.gef.service.IRevealService;
import org.xmind.gef.service.IShadowService;
import org.xmind.gef.service.ImageRegistryService;
import org.xmind.gef.service.ShadowService;
import org.xmind.gef.tool.ITool;
import org.xmind.gef.ui.actions.ActionRegistry;
import org.xmind.gef.ui.actions.CopyAction;
import org.xmind.gef.ui.actions.IActionRegistry;
import org.xmind.gef.ui.actions.PasteAction;
import org.xmind.gef.ui.actions.RequestAction;
import org.xmind.gef.ui.actions.SelectAllAction;
import org.xmind.gef.ui.editor.GraphicalEditorPage;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.util.Properties;
import org.xmind.ui.actions.MindMapActionFactory;
import org.xmind.ui.animation.AnimationService;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.TopicContextService;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.internal.actions.ActualSizeAction;
import org.xmind.ui.internal.actions.AddMarkerAction;
import org.xmind.ui.internal.actions.AlignmentRequestAction;
import org.xmind.ui.internal.actions.CancelHyperlinkAction;
import org.xmind.ui.internal.actions.CollapseAction;
import org.xmind.ui.internal.actions.CollapseAllAction;
import org.xmind.ui.internal.actions.CreateBoundaryAction;
import org.xmind.ui.internal.actions.CreateRelationshipAction;
import org.xmind.ui.internal.actions.CreateSheetFromTopicAction;
import org.xmind.ui.internal.actions.CreateSummaryAction;
import org.xmind.ui.internal.actions.CutAction;
import org.xmind.ui.internal.actions.DeleteAction;
import org.xmind.ui.internal.actions.DrillDownAction;
import org.xmind.ui.internal.actions.DrillUpAction;
import org.xmind.ui.internal.actions.EditLabelAction;
import org.xmind.ui.internal.actions.EditNotesAction;
import org.xmind.ui.internal.actions.EditTitleAction;
import org.xmind.ui.internal.actions.ExtendAction;
import org.xmind.ui.internal.actions.ExtendAllAction;
import org.xmind.ui.internal.actions.FinishAction;
import org.xmind.ui.internal.actions.FitMapAction;
import org.xmind.ui.internal.actions.FitSelectionAction;
import org.xmind.ui.internal.actions.InsertAttachmentAction;
import org.xmind.ui.internal.actions.InsertFloatingTopicAction;
import org.xmind.ui.internal.actions.InsertImageAction;
import org.xmind.ui.internal.actions.InsertParentTopicAction;
import org.xmind.ui.internal.actions.InsertSubtopicAction;
import org.xmind.ui.internal.actions.InsertTopicAction;
import org.xmind.ui.internal.actions.InsertTopicBeforeAction;
import org.xmind.ui.internal.actions.ModifyHyperlinkAction;
import org.xmind.ui.internal.actions.OpenHyperlinkAction;
import org.xmind.ui.internal.actions.PrintMapAction;
import org.xmind.ui.internal.actions.ResetPositionAction;
import org.xmind.ui.internal.actions.SaveAttachmentAsAction;
import org.xmind.ui.internal.actions.SelectBrothersAction;
import org.xmind.ui.internal.actions.SelectChildrenAction;
import org.xmind.ui.internal.actions.SortRequestAction;
import org.xmind.ui.internal.actions.TileAction;
import org.xmind.ui.internal.actions.TraverseAction;
import org.xmind.ui.internal.actions.ZoomInAction;
import org.xmind.ui.internal.actions.ZoomOutAction;
import org.xmind.ui.internal.layers.SkylightLayer;
import org.xmind.ui.internal.mindmap.DrillDownTraceService;
import org.xmind.ui.internal.mindmap.HighlightService;
import org.xmind.ui.internal.mindmap.MindMapRevealService;
import org.xmind.ui.internal.mindmap.MindMapTopicContextService;
import org.xmind.ui.internal.mindmap.MindMapViewer;
import org.xmind.ui.internal.mindmap.UndoRedoTipsService;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IDrillDownTraceService;
import org.xmind.ui.mindmap.IHighlightService;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.MindMapUtils;

public class MindMapEditorPage extends GraphicalEditorPage implements
        ICoreEventListener, IColorProvider, IEditDomainListener,
        IPropertyChangeListener, FocusListener {

    private ISelectionStack selectionStack = null;

    private ICoreEventRegister eventRegister = null;

    private IContextActivation contextActivation = null;

    private IContextService contextService = null;

    private IPreferenceStore prefStore = null;

    private IShadowService shadowService = null;

    private UndoRedoTipsService undoService = null;

    public void init(IGraphicalEditor parent, Object input) {
        super.init(parent, input);
        setPanelContributor(new MindMapEditorPagePanelContributor());
    }

    protected IGraphicalViewer createViewer() {
        return new MindMapViewer();
    }

    protected void createViewerControl(IGraphicalViewer viewer, Composite parent) {
        Control control = ((MindMapViewer) viewer).createControl(parent);
        control.addFocusListener(this);
    }

    public void updatePageTitle() {
        ISheet sheet = getCastedInput();
        String name = sheet.hasTitle() ? sheet.getTitleText() : sheet
                .getRootTopic().getTitleText();
        setPageTitle(MindMapUtils.trimSingleLine(name));
    }

    protected void installModelListeners(Object input) {
        super.installModelListeners(input);
        if (input instanceof ICoreEventSource) {
            eventRegister = new CoreEventRegister((ICoreEventSource) input,
                    this);
            eventRegister.register(Core.TitleText);
        }
        prefStore = MindMapUIPlugin.getDefault().getPreferenceStore();
        if (prefStore != null) {
            prefStore.addPropertyChangeListener(this);
        }
    }

    protected void uninstallModelListeners(Object input) {
        if (prefStore != null) {
            prefStore.removePropertyChangeListener(this);
            prefStore = null;
        }
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            eventRegister = null;
        }
        super.uninstallModelListeners(input);
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (PrefConstants.OVERLAPS_ALLOWED.equals(event.getProperty())) {
            ISheetPart sheet = ((IMindMapViewer) getViewer()).getSheetPart();
            if (sheet != null) {
                sheet.getFigure().revalidate();
            }
        } else if (PrefConstants.SHADOW_ENABLED.equals(event.getProperty())) {
            if (shadowService != null) {
                Object value = event.getNewValue();
                if (value instanceof String)
                    value = Boolean.parseBoolean((String) value);
                if (value instanceof Boolean)
                    shadowService.setActive((Boolean) value);
                else
                    shadowService.setActive(false);
            }
        } else if (PrefConstants.GRADIENT_COLOR.equals(event.getProperty())) {
            IBranchPart part = ((IMindMapViewer) getViewer())
                    .getCentralBranchPart();
            if (part != null) {
                part.treeUpdate(false);
            }
        } else if (PrefConstants.UNDO_REDO_TIPS_ENABLED.equals(event
                .getProperty())) {
            if (undoService != null) {
                Object value = event.getNewValue();
                if (value instanceof String)
                    value = Boolean.parseBoolean((String) value);
                if (value instanceof Boolean)
                    undoService.setActive((Boolean) value);
                else
                    undoService.setActive(false);
            }
        } else if (PrefConstants.UNDO_REDO_TIPS_FADE_DELAY.equals(event
                .getProperty())) {
            if (undoService != null) {
                Object value = event.getNewValue();
                if (value instanceof String) {
                    value = Integer.parseInt((String) value);
                }
                if (value instanceof Integer) {
                    undoService.setDuration(((Integer) value).intValue());
                } else {
                    undoService
                            .setDuration(UndoRedoTipsService.DEFAULT_DURATION);
                }
            }
        }
    }

    public ISheet getCastedInput() {
        return (ISheet) super.getInput();
    }

    protected Object createViewerInput() {
        return new MindMap(getCastedInput());
    }

    protected void initViewer(IGraphicalViewer viewer) {
        super.initViewer(viewer);
        viewer.getZoomManager().setConstraints(MindMapUI.ZOOM_MIN / 100.0d,
                MindMapUI.ZOOM_MAX / 100.0d);

        Properties properties = viewer.getProperties();
        properties.set(IMindMapViewer.VIEWER_CENTERED, Boolean.TRUE);
        properties.set(IMindMapViewer.VIEWER_CORNERED, Boolean.TRUE);
        properties.set(IMindMapViewer.VIEWER_ACTIONS, new ActionRegistry(
                getActionRegistry()));
        properties.set(IMindMapViewer.VIEWER_MARGIN,
                Integer.valueOf(MindMapUI.SHEET_MARGIN));

        initViewerServices((MindMapViewer) viewer);
    }

    protected void configureViewer(IGraphicalViewer viewer) {
        super.configureViewer(viewer);
        if (selectionStack == null)
            selectionStack = createSelectionStack();
        selectionStack.setSelectionProvider(getViewer());
        selectionStack.setCommandStack(getEditDomain().getCommandStack());
    }

    protected ISelectionStack createSelectionStack() {
        return new SelectionStack();
    }

    protected void initViewerServices(MindMapViewer viewer) {
        CenterPresercationService centerPresercationService = new CenterPresercationService(
                viewer);
        viewer.installService(CenterPresercationService.class,
                centerPresercationService);
        centerPresercationService.setActive(true);

        IImageRegistryService imageCacheService = new ImageRegistryService(
                viewer);
        viewer.installService(IImageRegistryService.class, imageCacheService);
        imageCacheService.setActive(true);

        IRevealService revealService = new MindMapRevealService(viewer);
        viewer.installService(IRevealService.class, revealService);
        revealService.setActive(true);

        IAnimationService animationService = new AnimationService(viewer);
        animationService.setPlaybackProvider(MindMapUI.getPlaybackProvider());
        viewer.installService(IAnimationService.class, animationService);
        animationService.setActive(true);

        Layer coverLayer = viewer.getLayer(MindMapUI.LAYER_COVER);
        Layer skylightLayer = viewer.getLayer(MindMapUI.LAYER_SKYLIGHT);
        if (coverLayer instanceof IRelayeredPane
                || skylightLayer instanceof ISkylightLayer) {
            IHighlightService highlightService = new HighlightService(viewer,
                    false);
            if (coverLayer instanceof IRelayeredPane)
                highlightService.setRelayeredPane((IRelayeredPane) coverLayer);
            if (skylightLayer instanceof ISkylightLayer)
                highlightService
                        .setHighlightLayer((SkylightLayer) skylightLayer);
            viewer.installService(IHighlightService.class, highlightService);
            highlightService.setActive(true);
        }

        Layer feedbackLayer = viewer.getLayer(GEF.LAYER_FEEDBACK);
        if (feedbackLayer != null) {
            FeedbackService feedbackService = new FeedbackService(viewer);
            viewer.installService(IFeedbackService.class, feedbackService);
            feedbackService.setLayer(feedbackLayer);
            feedbackService.setSelectionColorProvider(this);
            feedbackService
                    .setSelectionLineWidth(MindMapUI.SELECTION_LINE_WIDTH);
            feedbackService
                    .setSelectionCorner(MindMapUI.SELECTION_ROUNDED_CORNER);
            feedbackService.setActive(true);
        }

        IDrillDownTraceService traceService = new DrillDownTraceService(viewer);
        viewer.installService(IDrillDownTraceService.class, traceService);
        traceService.setActive(true);
        IAction action = getActionRegistry().getAction(
                MindMapActionFactory.DRILL_UP.getId());
        if (action instanceof DrillUpAction) {
            ((DrillUpAction) action).setTraceService(traceService);
        }

        Layer shadowLayer = viewer.getLayer(GEF.LAYER_SHADOW);
        if (shadowLayer != null) {
            shadowService = new ShadowService(viewer);
            viewer.installService(IShadowService.class, shadowService);
            shadowService.setLayer(shadowLayer);
            shadowService.setActive(prefStore != null
                    && prefStore.getBoolean(PrefConstants.SHADOW_ENABLED));
        }

        Layer undoLayer = viewer.getLayer(MindMapUI.LAYER_UNDO);
        if (undoLayer != null) {
            undoService = new UndoRedoTipsService(viewer);
            viewer.installService(UndoRedoTipsService.class, undoService);
            undoService.setLayer(undoLayer);
            undoService.setActive(true);
        }

        MindMapTopicContextService contextService = new MindMapTopicContextService(
                this, viewer);
        viewer.installService(TopicContextService.class, contextService);
        contextService.setActive(true);
    }

    public void setEditDomain(EditDomain domain) {
        if (getEditDomain() != null) {
            getEditDomain().removeEditDomainListener(this);
            deactivateContext();
        }
        super.setEditDomain(domain);
        if (getEditDomain() != null) {
            getEditDomain().addEditDomainListener(this);
            changeContext(getEditDomain().getActiveTool());
        }
    }

    public void activeToolChanged(ITool oldTool, ITool newTool) {
        changeContext(newTool);
    }

    public void setActive(boolean active) {
        boolean oldActive = isActive();
        super.setActive(active);
        boolean newActive = isActive();
        if (oldActive && !newActive) {
            if (getEditDomain() != null) {
                getEditDomain().handleRequest(GEF.REQ_CANCEL, getViewer());
            }
        }
    }

    private void changeContext(ITool newTool) {
        if (!isActive())
            return;

        deactivateContext();
        activateContext(newTool == null ? null : newTool.getContextId());
    }

    private void activateContext(String contextId) {
        if (contextId == null)
            return;
        contextService = (IContextService) getParentEditor().getSite()
                .getService(IContextService.class);
        if (contextService != null) {
            contextActivation = contextService.activateContext(contextId);
        }
    }

    private void deactivateContext() {
        if (contextService != null && contextActivation != null) {
            contextService.deactivateContext(contextActivation);
        }
        contextService = null;
        contextActivation = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events
     * .FocusEvent)
     */
    public void focusGained(FocusEvent e) {
        e.display.asyncExec(new Runnable() {
            public void run() {
                if (isActive())
                    changeContext(getEditDomain().getActiveTool());
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events
     * .FocusEvent)
     */
    public void focusLost(FocusEvent e) {
        changeContext(null);
    }

    protected void initPageActions(IActionRegistry actionRegistry) {
        super.initPageActions(actionRegistry);

        InsertTopicAction insertTopicAction = new InsertTopicAction(this);
        actionRegistry.addAction(insertTopicAction);
        addSelectionAction(insertTopicAction);

        InsertSubtopicAction insertSubtopicAction = new InsertSubtopicAction(
                this);
        actionRegistry.addAction(insertSubtopicAction);
        addSelectionAction(insertSubtopicAction);

        InsertTopicBeforeAction insertTopicBeforeAction = new InsertTopicBeforeAction(
                this);
        actionRegistry.addAction(insertTopicBeforeAction);
        addSelectionAction(insertTopicBeforeAction);

        InsertParentTopicAction insertParentTopicAction = new InsertParentTopicAction(
                this);
        actionRegistry.addAction(insertParentTopicAction);
        addSelectionAction(insertParentTopicAction);

        CreateSheetFromTopicAction insertSheetAction = new CreateSheetFromTopicAction(
                this);
        actionRegistry.addAction(insertSheetAction);
        addSelectionAction(insertSheetAction);

        ExtendAction extendAction = new ExtendAction(this);
        actionRegistry.addAction(extendAction);
        addSelectionAction(extendAction);

        CollapseAction collapseAction = new CollapseAction(this);
        actionRegistry.addAction(collapseAction);
        addSelectionAction(collapseAction);

        ExtendAllAction extendAllAction = new ExtendAllAction(this);
        actionRegistry.addAction(extendAllAction);
        addSelectionAction(extendAllAction);

        CollapseAllAction collapseAllAction = new CollapseAllAction(this);
        actionRegistry.addAction(collapseAllAction);
        addSelectionAction(collapseAllAction);

        ModifyHyperlinkAction modifyHyperlinkAction = new ModifyHyperlinkAction(
                this);
        actionRegistry.addAction(modifyHyperlinkAction);
        addSelectionAction(modifyHyperlinkAction);

        OpenHyperlinkAction openHyperlinkAction = new OpenHyperlinkAction(this);
        actionRegistry.addAction(openHyperlinkAction);
        addSelectionAction(openHyperlinkAction);

        InsertAttachmentAction insertAttachmentAction = new InsertAttachmentAction(
                this);
        actionRegistry.addAction(insertAttachmentAction);
        addSelectionAction(insertAttachmentAction);

        InsertImageAction insertImageAction = new InsertImageAction(this);
        actionRegistry.addAction(insertImageAction);
        addSelectionAction(insertImageAction);

        DeleteAction deleteAction = new DeleteAction(this);
        actionRegistry.addAction(deleteAction);
        addSelectionAction(deleteAction);

        CreateRelationshipAction createRelationshipAction = new CreateRelationshipAction(
                this);
        actionRegistry.addAction(createRelationshipAction);

        CopyAction copyAction = new CopyAction(this);
        actionRegistry.addAction(copyAction);

        CutAction cutAction = new CutAction(this);
        actionRegistry.addAction(cutAction);
        addSelectionAction(cutAction);

        PasteAction pasteAction = new PasteAction(this);
        actionRegistry.addAction(pasteAction);
        addSelectionAction(pasteAction);

        ZoomInAction zoomInAction = new ZoomInAction(this);
        actionRegistry.addAction(zoomInAction);

        ZoomOutAction zoomOutAction = new ZoomOutAction(this);
        actionRegistry.addAction(zoomOutAction);

        ActualSizeAction actualSizeAction = new ActualSizeAction(this);
        actionRegistry.addAction(actualSizeAction);

        FitMapAction fitMapAction = new FitMapAction(this);
        actionRegistry.addAction(fitMapAction);

        FitSelectionAction fitSelectionAction = new FitSelectionAction(this);
        actionRegistry.addAction(fitSelectionAction);

        SelectAllAction selectAllAction = new SelectAllAction(this);
        actionRegistry.addAction(selectAllAction);

        SelectBrothersAction selectBrothersAction = new SelectBrothersAction(
                this);
        actionRegistry.addAction(selectBrothersAction);
        addSelectionAction(selectBrothersAction);

        SelectChildrenAction selectChildrenAction = new SelectChildrenAction(
                this);
        actionRegistry.addAction(selectChildrenAction);
        addSelectionAction(selectChildrenAction);

        DrillDownAction drillDownAction = new DrillDownAction(this);
        actionRegistry.addAction(drillDownAction);
        addSelectionAction(drillDownAction);

        DrillUpAction drillUpAction = new DrillUpAction(this);
        actionRegistry.addAction(drillUpAction);

        CreateBoundaryAction createBoundaryAction = new CreateBoundaryAction(
                this);
        actionRegistry.addAction(createBoundaryAction);

        CreateSummaryAction createSummaryAction = new CreateSummaryAction(this);
        actionRegistry.addAction(createSummaryAction);

//        SortAsAlphaAction sortAsAlphaAction = new SortAsAlphaAction(this);
//        actionRegistry.addAction(sortAsAlphaAction);
//        addSelectionAction(sortAsAlphaAction);
//
//        SortAsPriorityAction sortAsPriorityAction = new SortAsPriorityAction(
//                this);
//        actionRegistry.addAction(sortAsPriorityAction);
//        addSelectionAction(sortAsPriorityAction);
//
//        SortAsModifyDateAction sortAsModifyDateAction = new SortAsModifyDateAction(
//                this);
//        actionRegistry.addAction(sortAsModifyDateAction);
//        addSelectionAction(sortAsModifyDateAction);

        EditTitleAction editTitleAction = new EditTitleAction(this);
        actionRegistry.addAction(editTitleAction);
        addSelectionAction(editTitleAction);

        EditLabelAction editLabelAction = new EditLabelAction(this);
        actionRegistry.addAction(editLabelAction);
        addSelectionAction(editLabelAction);

        EditNotesAction editNotesAction = new EditNotesAction(this);
        actionRegistry.addAction(editNotesAction);
        addSelectionAction(editNotesAction);

        AddMarkerAction addMarkerAction = new AddMarkerAction(
                ActionConstants.ADD_MARKER_ACTION_ID, this);
        actionRegistry.addAction(addMarkerAction);
        addSelectionAction(addMarkerAction);

        TraverseAction raverseAction = new TraverseAction(this);
        actionRegistry.addAction(raverseAction);
        addSelectionAction(raverseAction);

        FinishAction finishAction = new FinishAction(
                MindMapActionFactory.FINISH.getId(), this);
        actionRegistry.addAction(finishAction);

        TileAction tileAction = new TileAction(this);
        actionRegistry.addAction(tileAction);

        ResetPositionAction resetPositionAction = new ResetPositionAction(this);
        actionRegistry.addAction(resetPositionAction);
        addSelectionAction(resetPositionAction);

        actionRegistry.addAction(new RequestAction(MindMapActionFactory.MOVE_UP
                .getId(), this, GEF.REQ_MOVE_UP));
        actionRegistry
                .addAction(new RequestAction(MindMapActionFactory.MOVE_DOWN
                        .getId(), this, GEF.REQ_MOVE_DOWN));
        actionRegistry
                .addAction(new RequestAction(MindMapActionFactory.MOVE_LEFT
                        .getId(), this, GEF.REQ_MOVE_LEFT));
        actionRegistry.addAction(new RequestAction(
                MindMapActionFactory.MOVE_RIGHT.getId(), this,
                GEF.REQ_MOVE_RIGHT));
        actionRegistry.addAction(new RequestAction(MindMapActionFactory.GO_HOME
                .getId(), this, MindMapUI.REQ_SELECT_CENTRAL));
        actionRegistry.addAction(new InsertFloatingTopicAction(
                MindMapActionFactory.INSERT_FLOATING_TOPIC.getId(), this));
        actionRegistry.addAction(new InsertFloatingTopicAction(
                MindMapActionFactory.INSERT_FLOATING_CENTRAL_TOPIC.getId(),
                this));

        CancelHyperlinkAction cancelHyperlinkAction = new CancelHyperlinkAction(
                this);
        actionRegistry.addAction(cancelHyperlinkAction);
        addSelectionAction(cancelHyperlinkAction);

        SaveAttachmentAsAction saveAttachmentAsAction = new SaveAttachmentAsAction(
                this);
        actionRegistry.addAction(saveAttachmentAsAction);
        addSelectionAction(saveAttachmentAsAction);

        addAlignmentAction(PositionConstants.LEFT, actionRegistry);
        addAlignmentAction(PositionConstants.CENTER, actionRegistry);
        addAlignmentAction(PositionConstants.RIGHT, actionRegistry);
        addAlignmentAction(PositionConstants.TOP, actionRegistry);
        addAlignmentAction(PositionConstants.MIDDLE, actionRegistry);
        addAlignmentAction(PositionConstants.BOTTOM, actionRegistry);
        addSortAction(ActionConstants.SORT_TITLE_ID, actionRegistry);
        addSortAction(ActionConstants.SORT_PRIORITY_ID, actionRegistry);
        addSortAction(ActionConstants.SORT_MODIFIED_ID, actionRegistry);

        PrintMapAction printMapAction = new PrintMapAction(this);
        actionRegistry.addAction(printMapAction);
    }

    private void addAlignmentAction(int alignment,
            IActionRegistry actionRegistry) {
        AlignmentRequestAction action = new AlignmentRequestAction(this,
                alignment);
        actionRegistry.addAction(action);
        addSelectionAction(action);
    }

    private void addSortAction(String sortId, IActionRegistry actionRegistry) {
        SortRequestAction action = new SortRequestAction(this, sortId);
        actionRegistry.addAction(action);
        addSelectionAction(action);
    }

    public void dispose() {
        if (selectionStack != null) {
            selectionStack.setCommandStack(null);
            selectionStack.setSelectionProvider(null);
            selectionStack = null;
        }
        deactivateContext();
        super.dispose();
    }

    public Object getAdapter(Class adapter) {
        if (adapter == ISheet.class)
            return getCastedInput();
        if (adapter == IMindMap.class)
            return getViewer().getInput();
        return super.getAdapter(adapter);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TitleText.equals(type)) {
            updatePageTitle();
        }
    }

    public Color getBackground(Object element) {
        if (IFeedbackService.PreselectionColor.equals(element)
                || IFeedbackService.DisabledPreselectionColor.equals(element))
            return ColorUtils.getColor(MindMapUI.FILL_COLOR_PRESELECTION);
        return null;
    }

    public Color getForeground(Object element) {
        if (IFeedbackService.FocusColor.equals(element))
            return ColorUtils.getColor(MindMapUI.LINE_COLOR_FOCUS);
        if (IFeedbackService.SelectionColor.equals(element))
            return ColorUtils.getColor(MindMapUI.LINE_COLOR_SELECTION);
        if (IFeedbackService.PreselectionColor.equals(element)
                || IFeedbackService.DisabledPreselectionColor.equals(element))
            return ColorUtils.getColor(MindMapUI.LINE_COLOR_PRESELECTION);
        if (IFeedbackService.DisabledFocusColor.equals(element))
            return ColorUtils.getColor(MindMapUI.LINE_COLOR_FOCUS_DISABLED);
        if (IFeedbackService.DisabledSelectionColor.equals(element))
            return ColorUtils.getColor(MindMapUI.LINE_COLOR_SELECTION_DISABLED);
        return null;
    }

}