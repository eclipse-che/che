/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.logger;

/**
 * Codenvy API imports. In this extension we'll need
 * to talk to Parts and Action API. Gin and Singleton
 * imports are obligatory as well for any extension
 */

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import com.codenvy.ide.ext.logger.action.AnalyticsEventAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * &#0064;Singleton is required in case the instance is triggered several times this extension will be initialized several times as well.
 * <p/>
 * &#0064;Extension lets us know this is an extension and code injected in it will be executed when launched.
 */
@Singleton
@Extension(title = "Analytics Event Logger", version = "1.0.0")
public class AnalyticsEventLoggerExtension {

    /**
     * All menu items are actions. Here we register a new action in actionManager - AnalyticsEventID. Then,
     * we get it in the DefaultActionGroup which is general class for all items on the toolbar, context menus etc.
     */
    @Inject
    public AnalyticsEventLoggerExtension(ActionManager actionManager, AnalyticsEventAction action) {
        actionManager.registerAction("analyticsEventID", action);

        DefaultActionGroup contextMenu =
                (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_CONTEXT_MENU);

        /**
         * Finally, this action is added to a menu
         */
        contextMenu.add(action, Constraints.LAST);
    }
}
