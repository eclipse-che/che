/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide.model.info;

import org.eclipse.che.plugin.testing.ide.model.TestState;

/** Describes information about suit which is in progress. */
public class SuiteInProgressInfo extends TestInProgressInfo {

  private final TestState testState;

  public SuiteInProgressInfo(TestState testState) {
    this.testState = testState;
  }

  @Override
  public boolean isProblem() {
    for (TestState state : testState.getChildren()) {
      if (state.isProblem()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public TestStateDescription getDescription() {
    return TestStateDescription.RUNNING;
  }
}
