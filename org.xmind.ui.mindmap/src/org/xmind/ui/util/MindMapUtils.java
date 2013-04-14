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
package org.xmind.ui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.IAdaptable;
import org.xmind.core.IBoundary;
import org.xmind.core.INumbering;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ISheet;
import org.xmind.core.ISheetComponent;
import org.xmind.core.ISummary;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicComponent;
import org.xmind.core.ITopicRange;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.gef.IViewer;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IPart;
import org.xmind.ui.branch.IFreeableBranchStructureExtension;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ICacheManager;
import org.xmind.ui.mindmap.IIconTipPart;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.IViewerModel;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ImageUtils;

public class MindMapUtils {

    private static final List<IPart> NO_PARTS = Collections.emptyList();

    private static final String NUMBER_SEPARATOR = "."; //$NON-NLS-1$

    protected MindMapUtils() {
    }

    public static Object toRealModel(Object model) {
        if (model instanceof IViewerModel)
            return ((IViewerModel) model).getRealModel();
        if (model instanceof IMindMap)
            return ((IMindMap) model).getSheet();
        return model;
    }

    public static Object getRealModel(IPart part) {
        return toRealModel(part.getModel());
    }

    public static List<Object> getRealModels(List<? extends IPart> parts) {
        List<Object> models = new ArrayList<Object>(parts.size());
        for (IPart p : parts) {
            Object m = getRealModel(p);
            if (m != null && !models.contains(m))
                models.add(m);
        }
        return models;
    }

    public static List<ITopic> getTopics(List<? extends IPart> parts) {
        List<ITopic> topics = new ArrayList<ITopic>(parts.size());
        for (IPart p : parts) {
            Object model = getRealModel(p);
            if (model instanceof ITopic && !topics.contains(model))
                topics.add((ITopic) model);
        }
        return topics;
    }

    public static List<IPart> getParts(List<?> models, IViewer viewer) {
        List<IPart> parts = new ArrayList<IPart>(models.size());
        for (Object model : models) {
            IPart p = viewer.findPart(model);
            if (p != null && !parts.contains(p))
                parts.add(p);
        }
        return parts;
    }

    public static boolean isSingleTopic(ISelection selection) {
        return matchesSelection(selection, MindMapUI.CATEGORY_TOPIC, true);
    }

    public static boolean isSingle(ISelection selection, String category) {
        return matchesSelection(selection, category, true);
    }

    public static boolean hasSuchElements(ISelection selection, String category) {
        return matchesSelection(selection, category, false);
    }

    public static boolean matchesSelection(ISelection selection,
            String category, boolean singleOrMultiple) {
        if (selection == null || category == null || selection.isEmpty())
            return false;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            if (singleOrMultiple) {
                if (ss.size() == 1) {
                    Object o = ss.getFirstElement();
                    if (isThisCategory(o, category))
                        return true;
                }
            } else {
                for (Object o : ss.toArray()) {
                    if (isThisCategory(o, category))
                        return true;
                }
            }
        }
        return false;
    }

    private static boolean isThisCategory(Object o, String category) {
        return MindMapUI.getCategoryManager().belongsToCategory(o, category);
    }

    public static boolean hasCentralTopic(ISelection selection, IViewer viewer) {
        if (selection == null || selection.isEmpty() || viewer == null)
            return false;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            for (Object o : ss.toArray()) {
                if (o.equals(viewer.getAdapter(ITopic.class)))
                    return true;
            }
        }
        return false;
    }

    public static boolean isPropertyModifiable(ISelection selection,
            String propName, IViewer viewer) {
        if (selection == null || propName == null || viewer == null
                || selection.isEmpty())
            return false;
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            boolean hasTopic = false;
            for (Object o : ss.toArray()) {
                if (o instanceof ITopic) {
                    hasTopic = true;
                    IBranchPart branch = findBranch(viewer.findPart(o));
                    if (branch == null
                            || !branch.isPropertyModifiable(propName))
                        return false;
                }
            }
            if (hasTopic)
                return true;
        }
        return false;
    }

    public static IBranchPart findBranch(IPart source) {
        if (source == null)
            return null;
        if (source instanceof IBranchPart)
            return (IBranchPart) source;
        IPart parent = source.getParent();
        if (parent == null)
            return null;
        return findBranch(parent);
    }

    public static ITopicPart findTopicPart(IPart part) {
        if (part == null)
            return null;
        if (part instanceof ITopicPart)
            return (ITopicPart) part;
        return (ITopicPart) part.getAdapter(ITopicPart.class);
    }

    public static List<IPart> getSequenceTopics(IPart start, IPart end) {
        IBranchPart startBranch = findBranch(start);
        IBranchPart endBranch = findBranch(end);

        if (startBranch == null || endBranch == null)
            return NO_PARTS;

        IPart parent = startBranch.getParent();
        if (parent == null || parent != endBranch.getParent())
            return NO_PARTS;

        List<IBranchPart> branches;
        if (parent instanceof IBranchPart) {
            branches = ((IBranchPart) parent).getSubBranches();
        } else if (parent instanceof ISheetPart) {
            branches = ((ISheetPart) parent).getFloatingBranches();
        } else {
            return NO_PARTS;
        }

        int startIndex = branches.indexOf(startBranch);
        int endIndex = branches.indexOf(endBranch);
        if (startIndex < 0 || endIndex < 0)
            return NO_PARTS;

        if (endIndex < startIndex) {
            int temp = endIndex;
            endIndex = startIndex;
            startIndex = temp;
        }
        List<IPart> parts = new ArrayList<IPart>(endIndex - startIndex + 1);
        for (int i = startIndex; i <= endIndex; i++) {
            IPart p = branches.get(i);
            if (p instanceof IBranchPart) {
                parts.add(((IBranchPart) p).getTopicPart());
            }
        }
        return parts;
    }

    public static boolean isTopicTextChar(char c) {
        if (Character.isISOControl(c))
            return false;
        if (c == '+' || c == '-' || c == '*' || c == '/')
            return false;
        return true;
    }

    public static Point toGraphicalPosition(
            org.xmind.core.util.Point modelPosition) {
        if (modelPosition == null)
            return null;
        return new Point(modelPosition.x, modelPosition.y);
    }

    public static org.xmind.core.util.Point toModelPosition(
            Point graphicalPosition) {
        if (graphicalPosition == null)
            return null;
        return new org.xmind.core.util.Point(graphicalPosition.x,
                graphicalPosition.y);
    }

    public static String getText(Object element) {
        return getText(element, ""); //$NON-NLS-1$
    }

    public static String getText(Object element, String defaultText) {
        if (element instanceof ITitled) {
            return ((ITitled) element).getTitleText();
        } else if (element instanceof IWorkbook) {
            return MindMapMessages.TitleText_Workbook;
        }
        return defaultText;
    }

    public static ImageDescriptor getImageDescriptor(Object element) {
        return MindMapUI.getImages().getElementIcon(element, true);
    }

    public static Image getImage(Object element) {
        return ImageUtils.getImage(getImageDescriptor(element));
    }

    public static IPart findToFocus(List<IPart> sources, IViewer viewer) {
        IPart toFocus = null;
        if (viewer != null) {
            IPart focusedPart = viewer.getFocusedPart();
            if (focusedPart != null) {
                toFocus = findToFocus(focusedPart, sources, viewer);
            }
        }
        return toFocus;
    }

    private static IPart findToFocus(IPart currentFocus, List<IPart> sources,
            IViewer viewer) {
        Object model = MindMapUtils.getRealModel(currentFocus);
        List<Object> models = MindMapUtils.getRealModels(sources);
        if (models.contains(model)) {
            if (model instanceof ITopic) {
                return findTopicToFocus((ITopic) model, models, viewer);
            } else if (model instanceof IRelationship) {
                IRelationship r = (IRelationship) model;
                IRelationshipEnd end = r.getEnd2();
                if (end == null)
                    end = r.getEnd1();
                if (end != null) {
                    IPart p = viewer.findPart(end);
                    if (p != null)
                        return p;
                }
            } else if (model instanceof ITopicComponent) {
                ITopic topic = ((ITopicComponent) model).getParent();
                if (topic != null) {
                    IPart p = viewer.findPart(topic);
                    if (p != null)
                        return p;
                }
            }
        }
        return null;
    }

    private static IPart findTopicToFocus(ITopic t, List<Object> models,
            IViewer viewer) {
        ITopic p = t.getParent();
        if (p != null) {
            ITopic topicToFocus = null;
            while (p != null && topicToFocus == null) {
                int index = t.getIndex();
                List<ITopic> children = p.getChildren(t.getType());
                for (int i = index; i >= 0; i--) {
                    t = children.get(i);
                    if (!models.contains(t)) {
                        topicToFocus = t;
                        break;
                    }
                }
                if (topicToFocus == null) {
                    for (int i = index; i < children.size(); i++) {
                        t = children.get(i);
                        if (!models.contains(t)) {
                            topicToFocus = t;
                            break;
                        }
                    }
                }
                if (topicToFocus == null) {
                    t = p;
                    p = t.getParent();
                }
            }
            if (topicToFocus == null && t != null && !models.contains(t)) {
                topicToFocus = t;
            }
            if (topicToFocus != null) {
                IPart tp = viewer.findPart(topicToFocus);
                if (tp != null) {
                    return tp;
                }
            }
        }
        return null;
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == o2)
            return true;
        if (o1 == null || o2 == null)
            return false;
        return o1.equals(o2);
    }

    public static boolean isAncestorInList(ITopic topic,
            Collection<? extends Object> topics) {
        ITopic parent = topic.getParent();
        if (parent == null)
            return false;
        if (topics.contains(parent))
            return true;
        return isAncestorInList(parent, topics);
    }

    public static boolean isDescendentOf(ITopic topic, ITopic ancestor) {
        ITopic parent = topic.getParent();
        if (parent == null)
            return false;
        if (ancestor.equals(parent))
            return true;
        return isDescendentOf(parent, ancestor);
    }

    public static List<ITopic> filterOutDescendents(List<ITopic> topics,
            ITopic root) {
        Iterator<ITopic> it = topics.iterator();
        while (it.hasNext()) {
            ITopic t = it.next();
            if (isAncestorInList(t, topics)
                    || (root != null && !isDescendentOf(t, root))) {
                it.remove();
            }
        }
        return topics;
    }

    public static Set<ITopicRange> findContainedRanges(
            Collection<ITopic> topics, boolean checkSummary,
            boolean checkBoundary) {
        Set<ITopicRange> ranges = new HashSet<ITopicRange>();
        if (!checkSummary && !checkBoundary)
            return ranges;
        Set<ITopic> parents = new HashSet<ITopic>();
        for (ITopic t : topics) {
            ITopic parent = t.getParent();
            if (parent != null && !parents.contains(parent)) {
                parents.add(parent);
                if (checkSummary) {
                    for (ISummary s : parent.getSummaries()) {
                        if (topics.containsAll(s.getEnclosingTopics())) {
                            ranges.add(s);
                        }
                    }
                }
                if (checkBoundary) {
                    for (IBoundary b : parent.getBoundaries()) {
                        if (topics.containsAll(b.getEnclosingTopics())) {
                            ranges.add(b);
                        }
                    }
                }
            }
        }
        return ranges;
    }

    public static ISummaryPart findAttachedSummary(IBranchPart branch,
            IBranchPart child) {
        ITopicPart childNode = child.getTopicPart();
        for (ISummaryPart summary : branch.getSummaries()) {
            if (summary.getNode() == childNode)
                return summary;
        }
        return null;
    }

    public static String[] sortLabels(Collection<String> labels) {
        String[] array = labels.toArray(new String[labels.size()]);
        Arrays.sort(array);
        return array;
    }

    public static String getLabelText(Collection<String> labels) {
        String s = Arrays.toString(sortLabels(labels));
        return s.substring(1, s.length() - 1);
    }

    public static Collection<String> getLabels(String text) {
        String[] sp = text.split(MindMapUI.LABEL_SEPARATOR);
        List<String> list = new ArrayList<String>(sp.length);
        for (String s : sp) {
            s = s.trim();
            if (!"".equals(s) && !list.contains(s)) { //$NON-NLS-1$
                list.add(s);
            }
        }
        return list;
    }

    public static String trimFileName(String name) {
        return name.replaceAll(
                "\\r\\n|\\n|\\r|\\\\|/|\\||:|\"|>|<|\\*|\\?", " "); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String trimSingleLine(String name) {
        return name.replaceAll("\\r\\n|\\r|\\n", " "); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static ICacheManager getCacheManager(IPart p) {
        return (ICacheManager) p.getAdapter(ICacheManager.class);
    }

    public static Object getCache(IPart p, String key) {
        ICacheManager cacheManager = getCacheManager(p);
        if (cacheManager != null)
            return cacheManager.getCache(key);
        return null;
    }

    public static Object flushCache(IPart p, String key) {
        ICacheManager cacheManager = getCacheManager(p);
        if (cacheManager == null)
            return null;
        Object cache = cacheManager.getCache(key);
        cacheManager.flush(key);
        return cache;
    }

    public static Object setCache(IPart p, String key, Object value) {
        ICacheManager cacheManager = getCacheManager(p);
        if (cacheManager == null)
            return null;
        Object oldCache = cacheManager.getCache(key);
        cacheManager.setCache(key, value);
        return oldCache;
    }

    public static String getNumberText(ITopic topic, String defaultFormat) {
        if (!ITopic.ATTACHED.equals(topic.getType()))
            return null;
        ITopic parent = topic.getParent();
        if (parent == null)
            return null;

        int index = topic.getIndex();
        String format = parent.getNumbering().getComputedFormat();
        if (MindMapUI.DEFAULT_NUMBER_FORMAT.equals(format)
                && defaultFormat != null
                && !MindMapUI.DEFAULT_NUMBER_FORMAT.equals(defaultFormat)) {
            format = defaultFormat;
        }
        if (format == null) {
            if (defaultFormat == null)
                return null;
            format = defaultFormat;
        }
        return MindMapUI.getNumberFormatManager().getNumberText(format,
                index + 1);
    }

    public static String getNumberingText(ITopic topic, String defaultFormat) {
        String number = getNumberText(topic, defaultFormat);
        if (number == null || "".equals(number)) { //$NON-NLS-1$
            return null;
        }
        ITopic parent = topic.getParent();
        if (parent != null && parent.getNumbering().prependsParentNumbers()) {
            if (defaultFormat != null
                    && parent.getNumbering().getNumberFormat() != null) {
                defaultFormat = null;
            }
            String parentNumber = getNumberingText(parent, defaultFormat);
            if (parentNumber != null) {
                if (parentNumber.endsWith(NUMBER_SEPARATOR))
                    return parentNumber + number;
                return parentNumber + NUMBER_SEPARATOR + number;
            }
        }
        return number + NUMBER_SEPARATOR;
    }

    public static String getFullNumberingText(ITopic topic, String defaultFormat) {
        String text = getNumberingText(topic, defaultFormat);
        if (text != null) {
            ITopic parent = topic.getParent();
            if (parent != null) {
                INumbering parentNumbering = parent.getNumbering();
                String prefix = parentNumbering.getPrefix();
                if (prefix != null) {
                    text = prefix + " " + text; //$NON-NLS-1$
                }
                String suffix = parentNumbering.getSuffix();
                if (suffix != null) {
                    text = text + " " + suffix; //$NON-NLS-1$
                }
            }
        }
        return text;
    }

    public static boolean isSubBranchesFreeable(IBranchPart branch) {
        if (branch != null) {
            IStructure structure = branch.getBranchPolicy()
                    .getStructure(branch);
            if (structure instanceof IFreeableBranchStructureExtension) {
                return ((IFreeableBranchStructureExtension) structure)
                        .isChildrenFreeable(branch);
            }
        }
        return false;
    }

    public static boolean isBranchFreeable(IBranchPart branch) {
        return branch != null && branch.getTopic().getPosition() != null;
    }

    public static boolean isBranchFree(IBranchPart branch) {
        if (branch == null)
            return false;

        IBranchPart parent = branch.getParentBranch();
        if (parent == null)
            return false;

        return isBranchFreeable(branch) && isSubBranchesFreeable(parent);
    }

    public static ISummary findSummaryBySummaryTopic(ITopic topic) {
        if (topic == null)
            return null;
        return findSummaryBySummaryTopic(topic, topic.getParent());
    }

    public static ISummary findSummaryBySummaryTopic(ITopic topic, ITopic parent) {
        if (topic == null || parent == null)
            return null;
        return findSummaryBySummaryTopic(topic, parent, parent.getSummaries());
    }

    public static ISummary findSummaryBySummaryTopic(ITopic topic,
            ITopic parent, Collection<? extends ITopicRange> ranges) {
        if (topic == null || parent == null || ranges.isEmpty())
            return null;
        String id = topic.getId();
        for (ITopicRange range : ranges) {
            if (range instanceof ISummary) {
                ISummary s = (ISummary) range;
                if (id.equals(s.getTopicId()))
                    return s;
            }
        }
        return null;
    }

    public static List<IBoundary> getSortedBoundaries(ITopic topic) {
        ArrayList<IBoundary> list = new ArrayList<IBoundary>(
                topic.getBoundaries());
        Collections.sort(list, new Comparator<IBoundary>() {

            Map<IBoundary, List<ITopic>> cache = new HashMap<IBoundary, List<ITopic>>();

            public int compare(IBoundary o1, IBoundary o2) {
                if (o1.isMasterBoundary())
                    return 1;
                if (o2.isMasterBoundary())
                    return -1;
                List<ITopic> ts1 = cache.get(o1);
                if (ts1 == null) {
                    ts1 = new ArrayList<ITopic>(o1.getEnclosingTopics());
                    cache.put(o1, ts1);
                }
                List<ITopic> ts2 = cache.get(o2);
                if (ts2 == null) {
                    ts2 = new ArrayList<ITopic>(o2.getEnclosingTopics());
                    cache.put(o2, ts2);
                }
                int ret;
                if (ts1.containsAll(ts2))
                    ret = ts1.size() - ts2.size() + 1;
                else if (ts2.containsAll(ts1))
                    ret = ts1.size() - ts2.size() - 1;
                else if (ts1.isEmpty())
                    ret = -ts2.size() - 1;
                else if (ts2.isEmpty())
                    ret = ts1.size() + 1;
                else
                    ret = ts1.get(0).getIndex() - ts2.get(0).getIndex();
                return -ret;
            }
        });
        return list;
    }

    public static int getLevel(ITopic t, ITopic rootTopic) {
        if (t.equals(rootTopic) || (rootTopic == null && t.isRoot()))
            return 0;

        ITopic parent = t.getParent();
        if (parent == null) {
            return -1;
        }
        int parentLevel = getLevel(parent, rootTopic);
        if (parentLevel < 0)
            return parentLevel;
        return parentLevel + 1;
    }

    public static IWorkbook findWorkbook(Object source) {
        if (source instanceof IWorkbook)
            return (IWorkbook) source;
        if (source instanceof IWorkbookComponent)
            return ((IWorkbookComponent) source).getOwnedWorkbook();
        if (source instanceof IAdaptable) {
            Object adapter = ((IAdaptable) source).getAdapter(IWorkbook.class);
            if (adapter instanceof IWorkbook)
                return (IWorkbook) adapter;
            adapter = ((IAdaptable) source)
                    .getAdapter(IWorkbookComponent.class);
            if (adapter instanceof IWorkbookComponent)
                return ((IWorkbookComponent) adapter).getOwnedWorkbook();
        }
        if (source instanceof org.eclipse.core.runtime.IAdaptable) {
            Object adapter = ((org.eclipse.core.runtime.IAdaptable) source)
                    .getAdapter(IWorkbook.class);
            if (adapter instanceof IWorkbook)
                return (IWorkbook) adapter;
            adapter = ((org.eclipse.core.runtime.IAdaptable) source)
                    .getAdapter(IWorkbookComponent.class);
            if (adapter instanceof IWorkbookComponent)
                return ((IWorkbookComponent) adapter).getOwnedWorkbook();
        }
        return null;
    }

    public static ISheet findSheet(Object source) {
        if (source instanceof ISheet)
            return (ISheet) source;
        if (source instanceof IWorkbookComponent)
            return ((ISheetComponent) source).getOwnedSheet();
        if (source instanceof IAdaptable) {
            Object adapter = ((IAdaptable) source).getAdapter(ISheet.class);
            if (adapter instanceof ISheet)
                return (ISheet) adapter;
            adapter = ((IAdaptable) source).getAdapter(ISheetComponent.class);
            if (adapter instanceof ISheetComponent)
                return ((ISheetComponent) adapter).getOwnedSheet();
        }
        if (source instanceof org.eclipse.core.runtime.IAdaptable) {
            Object adapter = ((org.eclipse.core.runtime.IAdaptable) source)
                    .getAdapter(ISheet.class);
            if (adapter instanceof ISheet)
                return (ISheet) adapter;
            adapter = ((org.eclipse.core.runtime.IAdaptable) source)
                    .getAdapter(ISheetComponent.class);
            if (adapter instanceof ISheetComponent)
                return ((ISheetComponent) adapter).getOwnedSheet();
        }
        return null;
    }

    public static List<ITopicPart> getTopicParts(List<IPart> parts) {
        ArrayList<ITopicPart> topics = new ArrayList<ITopicPart>(parts.size());
        for (IPart p : parts) {
            if (p instanceof ITopicPart)
                topics.add((ITopicPart) p);
            else if (p instanceof IIconTipPart) {
                topics.add(((IIconTipPart) p).getTopicPart());
            }
        }
        topics.trimToSize();
        return topics;
    }

}