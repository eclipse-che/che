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
package org.eclipse.che.api.testing.server.listener;

import java.util.HashMap;

/**
 * Listener abstraction for the testing services to report their progress.
 *
 * @author David Festal
 */
@Deprecated
public abstract class AbstractTestListener {
  public static class TestSummary {
    private int errors;
    private int failures;

    public TestSummary() {
      this.errors = 0;
      this.failures = 0;
    }

    public void addError() {
      errors++;
    }

    public void addFailure() {
      failures++;
    }

    public int getErrors() {
      return errors;
    }

    public int getFailures() {
      return failures;
    }

    public boolean succeeded() {
      return failures == 0 && errors == 0;
    }

    @Override
    public String toString() {
      return new StringBuilder()
          .append(failures)
          .append(" failures and ")
          .append(errors)
          .append(" errors")
          .toString();
    }
  }

  HashMap<String, AbstractTestListener.TestSummary> runningTests = new HashMap<>();

  public synchronized void startTest(String testKey, String testName) {
    runningTests.put(testKey, null);
    startedTest(testKey, testName);
  }

  public synchronized void endTest(String testKey, String testName) {
    AbstractTestListener.TestSummary summary = runningTests.remove(testKey);
    endedTest(testKey, testName, summary);
  }

  protected abstract void startedTest(String testKey, String testName);

  protected abstract void endedTest(
      String testKey, String testName, AbstractTestListener.TestSummary summary);

  protected abstract void addedFailure(String testKey, Throwable throwable);

  protected abstract void addedError(String testKey, Throwable throwable);

  private synchronized AbstractTestListener.TestSummary getOrCreateTestSummary(String testKey) {
    AbstractTestListener.TestSummary summary = runningTests.get(testKey);
    if (summary == null) {
      summary = new TestSummary();
      runningTests.put(testKey, summary);
    }
    return summary;
  }

  public void addFailure(String testKey, Throwable throwable) {
    getOrCreateTestSummary(testKey).addFailure();
    addedFailure(testKey, throwable);
  }

  public void addError(String testKey, Throwable throwable) {
    getOrCreateTestSummary(testKey).addError();
    addedError(testKey, throwable);
  }
}
