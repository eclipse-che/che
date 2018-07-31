/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.inject;

import static java.lang.String.format;

import java.util.concurrent.atomic.AtomicInteger;

/** @author Dmytro Nochevnov */
public class SeleniumTestStatistics {
  private static final String statisticsTemplate = "Passed: %d, failed: %d, skipped: %d.";

  private final AtomicInteger runTests = new AtomicInteger();
  private final AtomicInteger failedTests = new AtomicInteger();
  private final AtomicInteger passedTests = new AtomicInteger();
  private final AtomicInteger skippedTests = new AtomicInteger();

  public int hitStart() {
    return runTests.incrementAndGet();
  }

  public int hitPass() {
    return passedTests.incrementAndGet();
  }

  public int hitFail() {
    return failedTests.incrementAndGet();
  }

  public int hitSkip() {
    return skippedTests.incrementAndGet();
  }

  @Override
  public String toString() {
    synchronized (this) {
      return format(statisticsTemplate, passedTests.get(), failedTests.get(), skippedTests.get());
    }
  }
}
