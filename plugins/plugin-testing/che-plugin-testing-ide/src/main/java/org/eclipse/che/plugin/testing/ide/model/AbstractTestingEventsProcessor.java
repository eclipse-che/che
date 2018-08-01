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
package org.eclipse.che.plugin.testing.ide.model;

import java.util.ArrayList;
import java.util.List;

/** Base event processor, converts events form runner to internal form */
public abstract class AbstractTestingEventsProcessor implements TestingEventsProcessor {
  private final String testFrameworkName;

  protected final List<TestStateEventsListener> listeners = new ArrayList<>();

  public AbstractTestingEventsProcessor(String testFrameworkName) {
    this.testFrameworkName = testFrameworkName;
  }

  /**
   * Adds test state event listener.
   *
   * @param listener new listener
   */
  public void addListener(TestStateEventsListener listener) {
    listeners.add(listener);
  }

  /**
   * Calls when test is started.
   *
   * @param testRootState test state
   */
  protected void callTestingStarted(TestRootState testRootState) {
    listeners.forEach(listener -> listener.onTestingStarted(testRootState));
  }

  /**
   * Calls when test framework is attached.
   *
   * @param rootState test state
   */
  protected void callTestFrameworkAttached(TestRootState rootState) {
    rootState.setTestReporterAttached();
  }

  /**
   * Calls when test suite is started.
   *
   * @param suite test suite
   */
  protected void callSuiteStarted(TestState suite) {
    listeners.forEach(listener -> listener.onSuiteStarted(suite));
  }

  /**
   * Calls when test suite is finished.
   *
   * @param suite test suite
   */
  protected void callSuiteFinished(TestState suite) {
    listeners.forEach(listener -> listener.onSuiteFinished(suite));
  }

  /**
   * Calls when test is started.
   *
   * @param testState test state
   */
  protected void callTestStarted(TestState testState) {
    listeners.forEach(listener -> listener.onTestStarted(testState));
  }

  /**
   * Calls when test is failed
   *
   * @param testState test state
   */
  protected void callTestFailed(TestState testState) {
    listeners.forEach(listener -> listener.onTestFailed(testState));
  }

  /**
   * Calls when test is finished.
   *
   * @param testState test state
   */
  protected void callTestFinished(TestState testState) {
    listeners.forEach(listener -> listener.onTestFinished(testState));
  }

  /**
   * Calls when test is ignored.
   *
   * @param testState test state
   */
  protected void callTestIgnored(TestState testState) {
    listeners.forEach(listener -> listener.onTestIgnored(testState));
  }

  /**
   * Calls with test count.
   *
   * @param count test count
   */
  protected void callTestCountInSuite(int count) {
    listeners.forEach(listener -> listener.onTestsCountInSuite(count));
  }

  /**
   * Calls when test root is finished.
   *
   * @param testRootState test root state
   */
  protected void callTestingFinished(TestRootState testRootState) {
    listeners.forEach(listener -> listener.onTestingFinished(testRootState));
  }
}
