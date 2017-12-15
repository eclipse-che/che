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
import org.eclipse.che.plugin.optimized.testing.ide.OptimizedTestAction;
import org.eclipse.che.plugin.optimized.testing.ide.OptimizedTestActionTemplate;
import org.eclipse.che.plugin.testing.testng.ide.action.DebugTestNgTestAction;
import org.eclipse.che.plugin.testing.testng.ide.action.RunTestNgTestAction;

public class OptimizedTestNgTestAction extends OptimizedTestActionTemplate
    implements OptimizedTestAction {
  public static final String TEST_ACTION_RUN = "OptimizedTestNgActionRun";
  public static final String TEST_ACTION_DEBUG = "OptimizedTestNgActionDebug";

  @Inject
  public OptimizedTestNgTestAction(
      ActionManager actionManager,
      RunTestNgTestAction runTestNgTestAction,
      DebugTestNgTestAction debugTestNgTestAction) {
    super(runTestNgTestAction, debugTestNgTestAction);
    actionManager.registerAction(TEST_ACTION_RUN, runTestNgTestAction);
    actionManager.registerAction(TEST_ACTION_DEBUG, debugTestNgTestAction);
  }
}
