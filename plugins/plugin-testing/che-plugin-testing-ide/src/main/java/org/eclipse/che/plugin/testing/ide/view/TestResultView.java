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
package org.eclipse.che.plugin.testing.ide.view;

import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/**
 * View for the result of java tests.
 *
 * @author Mirage Abeysekara
 */
public interface TestResultView extends View<TestResultView.ActionDelegate> {

  /**
   * Sets whether this panel is visible.
   *
   * @param visible visible - true to show the object, false to hide it
   */
  void setVisible(boolean visible);

  /**
   * Activate Test results part.
   *
   * @param result test results which comes from the server
   */
  @Deprecated
  void showResults(TestResult result);

  /**
   * Activate Test results part.
   *
   * @param result test results which comes from the server
   */
  void showResults(TestResultRootDto result);

  /** Clears the result view. */
  void clear();

  interface ActionDelegate extends BaseActionDelegate {}
}
