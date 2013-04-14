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
package org.xmind.ui.internal.outline;

import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;

public class TopicTreePart extends MindMapTreePartBase {

    public TopicTreePart(ITopic model) {
        super(model);
    }

    public ITopic getTopic() {
        return (ITopic) super.getModel();
    }

    protected Object[] getModelChildren(Object model) {
        return getTopic().getAllChildren().toArray();
    }

    protected void registerCoreEvents(ICoreEventRegister register) {
        super.registerCoreEvents(register);
        register.register(Core.TitleText);
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);
        register.register(Core.TopicHyperlink);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TitleText.equals(type) || Core.TopicHyperlink.equals(type)) {
            update();
        } else if (Core.TopicAdd.equals(type) || Core.TopicRemove.equals(type)) {
            refresh();
        } else {
            super.handleCoreEvent(event);
        }
    }

}