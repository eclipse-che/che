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
package org.eclipse.che.plugin.testing.junit.ide;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.plugin.testing.ide.TestAction;
import org.eclipse.che.plugin.testing.junit.ide.action.RunAllTestAction;
import org.eclipse.che.plugin.testing.junit.ide.action.RunClassTestAction;

import com.google.inject.Inject;

/**
 * JUnit 3.x and 4.x ide implementation.
 *
 * @author Mirage Abeysekara
 */
public class JUnitTestAction implements TestAction {
    public static final String TEST_ACTION_RUN_CLASS = "TestJUnitActionRunClassContext";
    public static final String TEST_ACTION_RUN_ALL   = "TestJUnitActionRunAllContext";
    private final Action runClassContextTestAction;
    private final Action runAllContextTestAction;

    @Inject
    public JUnitTestAction(ActionManager actionManager, 
                           RunClassTestAction runClassContextTestAction,
                           RunAllTestAction runAllContextTestAction,
                           KeyBindingAgent keyBinding) {
        actionManager.registerAction(TEST_ACTION_RUN_CLASS, runClassContextTestAction);
        actionManager.registerAction(TEST_ACTION_RUN_ALL, runAllContextTestAction);

        if (UserAgent.isMac()) {
            keyBinding.getGlobal().addKey(new KeyBuilder().control().alt().charCode('z').build(), TEST_ACTION_RUN_ALL);
            keyBinding.getGlobal().addKey(new KeyBuilder().control().shift().charCode('z').build(), TEST_ACTION_RUN_CLASS);
        } else {
            keyBinding.getGlobal().addKey(new KeyBuilder().action().alt().charCode('z').build(), TEST_ACTION_RUN_ALL);
            keyBinding.getGlobal().addKey(new KeyBuilder().action().shift().charCode('z').build(), TEST_ACTION_RUN_CLASS);
        }

        this.runClassContextTestAction = runClassContextTestAction;
        this.runAllContextTestAction = runAllContextTestAction;
    }


    @Override
    public void addMainMenuItems(DefaultActionGroup testMainMenu) {
        testMainMenu.add(runClassContextTestAction);
        testMainMenu.add(runAllContextTestAction);
    }

    @Override
    public void addContextMenuItems(DefaultActionGroup testContextMenu) {
        testContextMenu.add(runClassContextTestAction);
        testContextMenu.add(runAllContextTestAction);
    }
}
