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
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * {@link ISuiteListener} implementation, delegate all events to {@link
 * org.testng.CheTestNGListener}
 */
public class CheSuiteListener implements ISuiteListener {

  private final CheTestNGListener delegate;

  public CheSuiteListener(CheTestNGListener delegate) {
    this.delegate = delegate;
  }

  @Override
  public void onStart(ISuite suite) {
    delegate.onSuiteStart(suite);
  }

  @Override
  public void onFinish(ISuite suite) {
    delegate.onSuiteFinish(suite);
  }
}
