/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.datasource.ide;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.datasource.ide.action.HelloWorldAction;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_HELP;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;

@Extension(title = "Datasource Extension", version = "0.0.1")
public class DatasourceExtensions {

    /**
     * Constructor.
     *
     * @param actionManager
     *         the {@link ActionManager} that is used to register our actions
     * @param helloWorldAction
     *         hello world action
     */
    @Inject
    public DatasourceExtensions(
            ActionManager actionManager,
            HelloWorldAction helloWorldAction) {

        actionManager.registerAction("helloWorldAction", helloWorldAction);
        DefaultActionGroup sampleGroup = new DefaultActionGroup("Sample actions", true, actionManager);

        sampleGroup.add(helloWorldAction);

        // contribute sample group to main menu
        DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);
        mainMenu.add(sampleGroup, new Constraints(AFTER, GROUP_HELP));

        // add the HelloWorldAction to the beginning of the toolbar
        DefaultActionGroup toolbar = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_CENTER_TOOLBAR);
        toolbar.add(helloWorldAction, Constraints.FIRST);

        // add HelloWorldAction to context menu as last entry
        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction("resourceOperation");
        mainContextMenuGroup.add(helloWorldAction, Constraints.LAST);

        // add HelloWorldAction after help menu entry
        mainContextMenuGroup.add(helloWorldAction, Constraints.LAST);
    }
}
