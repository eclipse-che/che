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

/** Describes information about test which is not ran. */
public class NotRunInfo extends AbstractTestStateInfo {

  public static final NotRunInfo INSTANCE = new NotRunInfo();

  private NotRunInfo() {}

  @Override
  public boolean isFinal() {
    return false;
  }

  @Override
  public boolean isInProgress() {
    return false;
  }

  @Override
  public boolean isProblem() {
    return false;
  }

  @Override
  public boolean wasLaunched() {
    return false;
  }

  @Override
  public boolean wasTerminated() {
    return false;
  }

  @Override
  public TestStateDescription getDescription() {
    return TestStateDescription.NOT_RUN;
  }
}
