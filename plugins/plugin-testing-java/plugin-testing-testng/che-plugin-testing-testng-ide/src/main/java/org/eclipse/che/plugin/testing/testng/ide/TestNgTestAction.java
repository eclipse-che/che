/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.testng.ide;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.plugin.testing.ide.action.TestAction;
import org.eclipse.che.plugin.testing.testng.ide.action.DebugTestNgTestAction;
import org.eclipse.che.plugin.testing.testng.ide.action.RunTestNgTestAction;

/** Registrar for run/debug actions. */
public class TestNgTestAction implements TestAction {
  public static final String TEST_ACTION_RUN = "TestNgActionRun";
  public static final String TEST_ACTION_DEBUG = "TestNgActionDebug";

  private final BaseAction runTestAction;
  private final BaseAction debugTestAction;

  @Inject
  public TestNgTestAction(
      ActionManager actionManager,
      RunTestNgTestAction runTestNgTestAction,
      DebugTestNgTestAction debugTestNgTestAction) {
    actionManager.registerAction(TEST_ACTION_RUN, runTestNgTestAction);
    actionManager.registerAction(TEST_ACTION_DEBUG, debugTestNgTestAction);
    this.runTestAction = runTestNgTestAction;
    this.debugTestAction = debugTestNgTestAction;
  }

  @Override
  public void addMainMenuItems(DefaultActionGroup testMainMenu) {
    testMainMenu.add(runTestAction);
    testMainMenu.add(debugTestAction);
  }

  @Override
  public void addContextMenuItems(DefaultActionGroup testContextMenu) {
    testContextMenu.add(runTestAction);
    testContextMenu.add(debugTestAction);
  }
}
