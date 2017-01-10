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
package org.eclipse.che.ide.ext.java.testing.testng.client;

import com.google.inject.Inject;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.ext.java.testing.core.client.TestAction;
import org.eclipse.che.ide.ext.java.testing.testng.client.action.RunAllTestAction;
import org.eclipse.che.ide.ext.java.testing.testng.client.action.RunClassContextTestAction;
import org.eclipse.che.ide.ext.java.testing.testng.client.action.RunClassTestAction;
import org.eclipse.che.ide.ext.java.testing.testng.client.action.RunTestXMLAction;
import org.eclipse.che.ide.util.browser.UserAgent;

/**
 * TestNG ide implementation.
 *
 * @author Mirage Abeysekara
 */
public class TestNGTestAction implements TestAction {

    private final Action runClassTestAction;
    private final Action runAllTestAction;
    private final Action runClassContextTestAction;
    private final Action runTestXMLAction;

    @Inject
    public TestNGTestAction(ActionManager actionManager,
                            RunClassTestAction runClassTestAction,
                            RunAllTestAction runAllTestAction,
                            RunClassContextTestAction runClassContextTestAction,
                            RunTestXMLAction runTestXMLAction,
                            KeyBindingAgent keyBinding) {

        actionManager.registerAction("TestNGActionRunClass", runClassTestAction);
        actionManager.registerAction("TestNGActionRunAll", runAllTestAction);
        actionManager.registerAction("TestNGActionRunXML", runTestXMLAction);
        actionManager.registerAction("TestNGActionRunClassContext", runClassContextTestAction);

        if (UserAgent.isMac()) {
            keyBinding.getGlobal().addKey(new KeyBuilder().control().alt().charCode('g').build(), "TestNGActionRunAll");
            keyBinding.getGlobal().addKey(new KeyBuilder().control().shift().charCode('g').build(), "TestNGActionRunClass");
        } else {
            keyBinding.getGlobal().addKey(new KeyBuilder().action().alt().charCode('g').build(), "TestNGActionRunAll");
            keyBinding.getGlobal().addKey(new KeyBuilder().action().shift().charCode('g').build(), "TestNGActionRunClass");
        }

        this.runAllTestAction = runAllTestAction;
        this.runClassContextTestAction = runClassContextTestAction;
        this.runClassTestAction = runClassTestAction;
        this.runTestXMLAction = runTestXMLAction;
    }


    @Override
    public void addMainMenuItems(DefaultActionGroup testMainMenu) {
        testMainMenu.add(runClassTestAction);
        testMainMenu.add(runAllTestAction);
        testMainMenu.add(runTestXMLAction);
    }

    @Override
    public void addContextMenuItems(DefaultActionGroup testContextMenu) {
        testContextMenu.add(runClassContextTestAction);
        testContextMenu.add(runAllTestAction);
        testContextMenu.add(runTestXMLAction);
    }
}
