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
package org.eclipse.che.plugin.testing.junit.ide;

import com.google.inject.Inject;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.plugin.optimized.testing.ide.OptimizedTestAction;
import org.eclipse.che.plugin.optimized.testing.ide.OptimizedTestActionTemplate;
import org.eclipse.che.plugin.testing.junit.ide.action.DebugJUnitTestAction;
import org.eclipse.che.plugin.testing.junit.ide.action.RunJUnitTestAction;

/** Registrar of JUnit run/debug actions. */
public class OptimizedJUnitTestAction extends OptimizedTestActionTemplate
    implements OptimizedTestAction {
  public static final String OPTIMIZED_TEST_ACTION_RUN = "OptimizedTestJUnitActionRun";
  public static final String OPTIMIZED_TEST_ACTION_DEBUG = "OptimizedTestJUnitActionDebug";

  @Inject
  public OptimizedJUnitTestAction(
      ActionManager actionManager,
      RunJUnitTestAction runJUnitTestAction,
      DebugJUnitTestAction debugJUnitTestAction) {
    super(runJUnitTestAction, debugJUnitTestAction);
    actionManager.registerAction(OPTIMIZED_TEST_ACTION_RUN, runJUnitTestAction);
    actionManager.registerAction(OPTIMIZED_TEST_ACTION_DEBUG, debugJUnitTestAction);
  }
}
