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
package org.eclipse.che.ide.ext.java.testing.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;

import java.util.Set;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;

/**
 * Java test extension.
 *
 * @author Mirage Abeysekara
 */
@Singleton
@Extension(title = "Test Extension", version = "1.0.0")
public class TestExtension {

    @Inject
    public TestExtension(ActionManager actionManager, TestLocalizationConstant localization,
                         Set<TestAction> testActions) {

        DefaultActionGroup runMenu = (DefaultActionGroup) actionManager.getAction(GROUP_RUN);

        DefaultActionGroup testMainMenu =
                new DefaultActionGroup(localization.actionGroupMenuName(), true, actionManager);
        actionManager.registerAction("TestMainGroup", testMainMenu);

        for (TestAction testAction : testActions) {
            testAction.addMainMenuItems(testMainMenu);
            testMainMenu.addSeparator();
        }

        runMenu.addSeparator();
        runMenu.add(testMainMenu);


        DefaultActionGroup explorerMenu = (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
        DefaultActionGroup testContextMenu =
                new DefaultActionGroup(localization.actionGroupMenuName(), true, actionManager);
        actionManager.registerAction("TestContextGroup", testContextMenu);

        for (TestAction testAction : testActions) {
            testAction.addContextMenuItems(testContextMenu);
            testContextMenu.addSeparator();
        }

        explorerMenu.addSeparator();
        explorerMenu.add(testContextMenu);
        explorerMenu.addSeparator();
    }

}
