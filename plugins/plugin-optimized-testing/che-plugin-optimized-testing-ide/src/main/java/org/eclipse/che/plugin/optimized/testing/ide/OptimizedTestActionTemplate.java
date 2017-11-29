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
package org.eclipse.che.plugin.optimized.testing.ide;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.plugin.testing.ide.action.RunDebugTestAbstractAction;

public abstract class OptimizedTestActionTemplate implements OptimizedTestAction {

  private final Action runTestAction;
  private final Action debugTestAction;

  public OptimizedTestActionTemplate(
      RunDebugTestAbstractAction runTestAction, RunDebugTestAbstractAction debugTestAction) {
    this.runTestAction = runTestAction;
    this.debugTestAction = debugTestAction;
    runTestAction.modifyTestExecutionContext(
        OptimizedTestExecutionModifier::modifyTestExecutionContext);
    debugTestAction.modifyTestExecutionContext(
        OptimizedTestExecutionModifier::modifyTestExecutionContext);
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
