/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.phpunit.ide;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.plugin.testing.ide.TestActionGroup;
import org.eclipse.che.plugin.testing.phpunit.ide.action.PHPRunContainerTestAction;
import org.eclipse.che.plugin.testing.phpunit.ide.action.PHPRunScriptTestAction;

import com.google.inject.Inject;

/**
 * PHPUnit test action implementation.
 * 
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestActionGroup implements TestActionGroup {

    private final Action runScriptTestAction;
    private PHPRunContainerTestAction runContainerTestAction;

    @Inject
    public PHPUnitTestActionGroup(ActionManager actionManager, PHPRunScriptTestAction runScriptTestAction,
            PHPRunContainerTestAction runAllTestAction, KeyBindingAgent keyBinding) {
        actionManager.registerAction("PHPRunScriptTestAction", runScriptTestAction);
        actionManager.registerAction("PHPRunAllTestAction", runAllTestAction);
        this.runScriptTestAction = runScriptTestAction;
        this.runContainerTestAction = runAllTestAction;
    }

    @Override
    public void addMainMenuItems(DefaultActionGroup testMainMenu) {
        testMainMenu.add(runScriptTestAction);
        testMainMenu.add(runContainerTestAction);
    }

    @Override
    public void addContextMenuItems(DefaultActionGroup testContextMenu) {
        testContextMenu.add(runScriptTestAction);
        testContextMenu.add(runContainerTestAction);
    }
}
