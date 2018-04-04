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
