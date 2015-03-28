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
package com.codenvy.ide.ext.myextension;

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
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import com.codenvy.ide.ext.myextension.action.MyAction;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @Singleton is required in case the instance is triggered several times this extension will be initialized several times as well.
 * @Extension lets us know this is an extension and code injected in it will be executed when launched
 */
@Singleton
@Extension(title = "Icon registry API", version = "1.0.0")
public class MyExtension

/**
 * All menu items are actions. Here we register a new action in actionManager - HelloWorldID.
 * Then, we get it in the DefaultActionGroup
 * which is general class for all items on the toolbar, context menus etc.
 */
{
    @Inject
    public MyExtension(ActionManager actionManager, MyAction action, IconRegistry iconRegistry) {
        iconRegistry.registerIcon(new Icon("my.icon", "my-extension/mammoth_happy.png"));
        actionManager.registerAction("helloWorldID", action);

        DefaultActionGroup contextMenu = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_HELP);

        /**
         * Finally, this action is added to a menu
         */
        contextMenu.add(action, Constraints.LAST);
    }
}
