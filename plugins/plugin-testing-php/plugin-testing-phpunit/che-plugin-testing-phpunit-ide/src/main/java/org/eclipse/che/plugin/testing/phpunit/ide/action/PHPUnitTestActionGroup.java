/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.phpunit.ide.action;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.plugin.testing.ide.action.TestAction;

/**
 * PHPUnit test action implementation.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestActionGroup implements TestAction {

  private final BaseAction runScriptTestAction;

  @Inject
  public PHPUnitTestActionGroup(
      ActionManager actionManager, PHPRunScriptTestAction runScriptTestAction) {
    actionManager.registerAction("PHPRunScriptTestAction", runScriptTestAction);
    this.runScriptTestAction = runScriptTestAction;
  }

  @Override
  public void addMainMenuItems(DefaultActionGroup testMainMenu) {
    testMainMenu.add(runScriptTestAction);
  }

  @Override
  public void addContextMenuItems(DefaultActionGroup testContextMenu) {
    testContextMenu.add(runScriptTestAction);
  }
}
