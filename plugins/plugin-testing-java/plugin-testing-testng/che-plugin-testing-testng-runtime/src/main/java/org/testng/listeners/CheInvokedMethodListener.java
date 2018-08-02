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
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * Listener for invoking before and after method actions by TestNG. This listener will only be
 * invoked for configuration and test methods.
 */
public class CheInvokedMethodListener implements IInvokedMethodListener {

  private final CheTestNGListener delegate;

  public CheInvokedMethodListener(CheTestNGListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    synchronized (delegate) {
      if (!testResult.getMethod().isTest()) {
        delegate.onConfigurationStart(testResult);
      }
    }
  }

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    // ignore
  }
}
