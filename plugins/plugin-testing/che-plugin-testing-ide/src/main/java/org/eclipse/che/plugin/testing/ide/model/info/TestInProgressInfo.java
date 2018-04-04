/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide.model.info;

/** Describes information about test which is in progress. */
public class TestInProgressInfo extends AbstractTestStateInfo {

  public static final TestInProgressInfo INSTANCE = new TestInProgressInfo();

  protected TestInProgressInfo() {}

  @Override
  public boolean isFinal() {
    return false;
  }

  @Override
  public boolean isInProgress() {
    return true;
  }

  @Override
  public boolean isProblem() {
    return false;
  }

  @Override
  public boolean wasLaunched() {
    return true;
  }

  @Override
  public boolean wasTerminated() {
    return false;
  }

  @Override
  public TestStateDescription getDescription() {
    return TestStateDescription.RUNNING;
  }
}
