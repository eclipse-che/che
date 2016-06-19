/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
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
import org.eclipse.che.ide.api.constraints.Anchor;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;

import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Set;

import static org.eclipse.che.ide.api.action.IdeActions.*;

@Singleton
@Extension(title = "Test Extension", version = "1.0.0")
public class TestExtension {

    @Inject
    public TestExtension(TestResources resources, ActionManager actionManager,TestLocalizationConstant localization,
                         Set<TestAction> testActions) {

        Log.info(TestExtension.class,"TestRunner ASFDK");
//        DefaultActionGroup mainMenu = (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_MENU);
//        DefaultActionGroup testMenu = new DefaultActionGroup("Test", true, actionManager);
//        mainMenu.add(testMenu,  new Constraints(Anchor.AFTER, GROUP_RUN));
//        actionManager.registerAction("TestMenuID", testMenu);
//        actionManager.registerAction("TestActionID", action);
//        testMenu.add(action);
//
//        actionManager.registerAction("TestActionID2", action2);
//        testMenu.add(action2);

        Log.info(TestExtension.class,"TestAction Injected String" + testActions.toString());


        DefaultActionGroup runMenu = (DefaultActionGroup) actionManager.getAction(GROUP_RUN);

        DefaultActionGroup testMainMenu =
                new DefaultActionGroup(localization.actionGroupMenuName(), true, actionManager);
        actionManager.registerAction("TestMainGroup", testMainMenu);

        for(TestAction testAction : testActions){
            testAction.addMainMenuItems(testMainMenu);
        }

        runMenu.addSeparator();
        runMenu.add(testMainMenu);


//        actionManager.registerAction("TestActionRunClass", runClassTestAction);
//        testMainMenu.add(runClassTestAction);
//        actionManager.registerAction("TestActionRunAll", runAllTestAction);
//        testMainMenu.add(runAllTestAction);


        DefaultActionGroup explorerMenu = (DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
        DefaultActionGroup testContextMenu =
                new DefaultActionGroup(localization.actionGroupMenuName(), true, actionManager);
        actionManager.registerAction("TestContextGroup", testContextMenu);

        for(TestAction testAction : testActions){
            testAction.addContextMenuItems(testContextMenu);
        }

//        actionManager.registerAction("TestActionRunClassContext", runClassContextTestAction);
//        testContextMenu.add(runClassContextTestAction);
//
//        testContextMenu.add(runAllTestAction);

        explorerMenu.addSeparator();
        explorerMenu.add(testContextMenu);
        explorerMenu.addSeparator();

//        DefaultActionGroup testMenu2 = new DefaultActionGroup("Testrgr", true, actionManager);
//        exploermenu.add(action);
//        Log.info(TestExtension.class,"TestRunner test menu context");
//        actionManager.registerAction("TestMenuID2", action);

//        keyBinding.getGlobal().addKey(new KeyBuilder().action().alt().charCode('z').build(),
//                "TestActionRunAll");
//
//        keyBinding.getGlobal().addKey(new KeyBuilder().action().shift().charCode('z').build(),
//                "TestActionRunClass");
    }
}
