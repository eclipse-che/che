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
package org.testng.listeners;

import org.testng.CheTestNGListener;
import org.testng.IConfigurationListener;
import org.testng.ITestResult;

/** Listener for events related to configuration methods. */
public class CheConfigurationListener implements IConfigurationListener {

  private final CheTestNGListener delegate;

  private boolean ignoreStarted;

  public CheConfigurationListener(CheTestNGListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onConfigurationSuccess(ITestResult itr) {
    delegate.onConfigurationSuccess(itr, !ignoreStarted);
  }

  @Override
  public void onConfigurationFailure(ITestResult itr) {
    delegate.onConfigurationFailure(itr, !ignoreStarted);
  }

  @Override
  public void onConfigurationSkip(ITestResult itr) {
    // ignore
  }

  public void setIgnoreStarted() {
    ignoreStarted = true;
  }
}
