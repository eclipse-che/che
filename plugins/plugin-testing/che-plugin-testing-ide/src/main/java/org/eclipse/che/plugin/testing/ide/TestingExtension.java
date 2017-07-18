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
package org.eclipse.che.plugin.testing.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.testing.ide.action.DebugTestAction;
import org.eclipse.che.plugin.testing.ide.action.RunTestAction;

import java.util.Set;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;

/**
 * Java test extension.
 *
 * @author Mirage Abeysekara
 */
@Singleton
@Extension(title = "Testing Extension", version = "1.0.0")
public class TestingExtension {

    @Inject
    public TestingExtension(ActionManager actionManager,
                            TestLocalizationConstant localization,
                            Set<TestAction> testActions,
                            DebugTestAction debugTestAction,
                            RunTestAction runTestAction) {

        DefaultActionGroup runMenu = (DefaultActionGroup) actionManager.getAction(GROUP_RUN);
        DefaultActionGroup testMainMenu = new DefaultActionGroup(localization.actionGroupMenuName(), true,
                actionManager);
        actionManager.registerAction("TestingMainGroup", testMainMenu);

        for (TestAction testAction : testActions) {
            testAction.addMainMenuItems(testMainMenu);
            testMainMenu.addSeparator();
        }
        actionManager.registerAction("RunTest", runTestAction);
        actionManager.registerAction("DebugTest", debugTestAction);
        testMainMenu.add(runTestAction);
        testMainMenu.add(debugTestAction);
        runMenu.addSeparator();
        runMenu.add(testMainMenu);
        DefaultActionGroup explorerMenu = (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
        DefaultActionGroup testContextMenu = new DefaultActionGroup(localization.contextActionGroupMenuName(), true,
                actionManager);
        actionManager.registerAction("TestingContextGroup", testContextMenu);
        for (TestAction testAction : testActions) {
            testAction.addContextMenuItems(testContextMenu);
            testContextMenu.addSeparator();
        }
        testContextMenu.add(runTestAction);
        testContextMenu.add(debugTestAction);
        explorerMenu.addSeparator();
        explorerMenu.add(testContextMenu);
        explorerMenu.addSeparator();
    }
}
