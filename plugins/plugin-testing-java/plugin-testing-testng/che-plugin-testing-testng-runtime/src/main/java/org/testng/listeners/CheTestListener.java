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
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/** Listener for test running. */
public class CheTestListener implements ITestListener {

  private final CheTestNGListener delegate;

  public CheTestListener(CheTestNGListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onTestStart(ITestResult result) {
    delegate.onTestStart(result);
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    delegate.onTestSuccess(result);
  }

  @Override
  public void onTestFailure(ITestResult result) {
    delegate.onTestFailure(result);
  }

  @Override
  public void onTestSkipped(ITestResult result) {
    delegate.onTestSkipped(result);
  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    delegate.onTestFailedButWithinSuccessPercentage(result);
  }

  @Override
  public void onStart(ITestContext context) {
    // ignore
  }

  @Override
  public void onFinish(ITestContext context) {
    // ignore
  }
}
