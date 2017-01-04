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
package org.eclipse.che.ide.ext.java.testing.junit.client;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.ext.java.testing.core.client.TestAction;
import org.eclipse.che.ide.ext.java.testing.junit.client.action.RunAllTestAction;
import org.eclipse.che.ide.ext.java.testing.junit.client.action.RunClassContextTestAction;
import org.eclipse.che.ide.ext.java.testing.junit.client.action.RunClassTestAction;
import org.eclipse.che.ide.util.browser.UserAgent;

/**
 * JUnit 3.x and 4.x ide implementation.
 *
 * @author Mirage Abeysekara
 */
public class JUnitTestAction implements TestAction {

    private final Action runClassTestAction;
    private final Action runAllTestAction;
    private final Action runClassContextTestAction;

    @Inject
    public JUnitTestAction(ActionManager actionManager, RunClassTestAction runClassTestAction,
                           RunAllTestAction runAllTestAction, RunClassContextTestAction runClassContextTestAction,
                           KeyBindingAgent keyBinding) {

        actionManager.registerAction("TestActionRunClass", runClassTestAction);
        actionManager.registerAction("TestActionRunAll", runAllTestAction);
        actionManager.registerAction("TestActionRunClassContext", runClassContextTestAction);

        if (UserAgent.isMac()) {
            keyBinding.getGlobal().addKey(new KeyBuilder().control().alt().charCode('z').build(), "TestActionRunAll");
            keyBinding.getGlobal().addKey(new KeyBuilder().control().shift().charCode('z').build(), "TestActionRunClass");
        } else {
            keyBinding.getGlobal().addKey(new KeyBuilder().action().alt().charCode('z').build(), "TestActionRunAll");
            keyBinding.getGlobal().addKey(new KeyBuilder().action().shift().charCode('z').build(), "TestActionRunClass");
        }

        this.runAllTestAction = runAllTestAction;
        this.runClassContextTestAction = runClassContextTestAction;
        this.runClassTestAction = runClassTestAction;
    }


    @Override
    public void addMainMenuItems(DefaultActionGroup testMainMenu) {
        testMainMenu.add(runClassTestAction);
        testMainMenu.add(runAllTestAction);
    }

    @Override
    public void addContextMenuItems(DefaultActionGroup testContextMenu) {
        testContextMenu.add(runClassContextTestAction);
        testContextMenu.add(runAllTestAction);
    }
}
