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
package org.eclipse.che.plugin.testing.testng.ide;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.plugin.testing.ide.TestAction;
import org.eclipse.che.plugin.testing.testng.ide.action.RunAllTestAction;
import org.eclipse.che.plugin.testing.testng.ide.action.RunClassTestAction;
import org.eclipse.che.plugin.testing.testng.ide.action.RunTestXMLAction;

import com.google.inject.Inject;

/**
 * TestNG ide implementation.
 *
 * @author Mirage Abeysekara
 */
public class TestNGTestAction implements TestAction {
    
    public static final String TEST_ACTION_RUN_CLASS = "TestNGActionRunClassContext";
    public static final String TEST_ACTION_RUN_ALL   = "TestNGActionRunAllContext";
    public static final String TEST_ACTION_RUN_XML   = "TestNGActionRunXMLContext";

    private final Action runClassContextTestAction;
    private final Action runAllContextTestAction;
    private final Action runTestXMLContextAction;

    @Inject
    public TestNGTestAction(ActionManager actionManager,
                            RunClassTestAction runClassContextTestAction,
                            RunAllTestAction runAllContextTestAction,
                            RunTestXMLAction runTestXMLContextAction,
                            KeyBindingAgent keyBinding) {
        
        actionManager.registerAction(TEST_ACTION_RUN_CLASS, runClassContextTestAction);
        actionManager.registerAction(TEST_ACTION_RUN_ALL, runAllContextTestAction);
        actionManager.registerAction(TEST_ACTION_RUN_XML, runTestXMLContextAction);
        if (UserAgent.isMac()) {
            keyBinding.getGlobal().addKey(new KeyBuilder().control().alt().charCode('g').build(), TEST_ACTION_RUN_ALL);
            keyBinding.getGlobal().addKey(new KeyBuilder().control().shift().charCode('g').build(), TEST_ACTION_RUN_CLASS);
        } else {
            keyBinding.getGlobal().addKey(new KeyBuilder().action().alt().charCode('g').build(), TEST_ACTION_RUN_ALL);
            keyBinding.getGlobal().addKey(new KeyBuilder().action().shift().charCode('g').build(), TEST_ACTION_RUN_CLASS);
        }
        this.runClassContextTestAction = runClassContextTestAction;
        this.runAllContextTestAction = runAllContextTestAction;
        this.runTestXMLContextAction = runTestXMLContextAction;
    }

    @Override
    public void addMainMenuItems(DefaultActionGroup testMainMenu) {
        testMainMenu.add(runClassContextTestAction);
        testMainMenu.add(runAllContextTestAction);
        testMainMenu.add(runTestXMLContextAction);
    }

    @Override
    public void addContextMenuItems(DefaultActionGroup testContextMenu) {
        testContextMenu.add(runClassContextTestAction);
        testContextMenu.add(runAllContextTestAction);
        testContextMenu.add(runTestXMLContextAction);
    }
}
