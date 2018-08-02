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

/** Lister for Test running events. */
public interface TestStateEventsListener {

  /**
   * Called when testing started, before suites and tests
   *
   * @param testRootState
   */
  void onTestingStarted(TestRootState testRootState);

  /**
   * Called after all tests finished
   *
   * @param testRootState
   */
  void onTestingFinished(TestRootState testRootState);

  /**
   * Called when test suite started
   *
   * @param testState
   */
  void onSuiteStarted(TestState testState);

  /**
   * Called when test suite finished
   *
   * @param testState
   */
  void onSuiteFinished(TestState testState);

  /**
   * Called when test node added
   *
   * @param testState describes test node
   */
  void onSuiteTreeNodeAdded(TestState testState);

  /**
   * Called when building of test tree started
   *
   * @param testState describes test suite
   */
  void onSuiteTreeStarted(TestState testState);

  /**
   * Called when building of test tree finished
   *
   * @param suite describes test suite
   */
  void onSuiteTreeNodeFinished(TestState suite);

  /** @param count */
  void onTestsCountInSuite(int count);

  /**
   * Called when test started
   *
   * @param testState
   */
  void onTestStarted(TestState testState);

  /**
   * Called when test finished
   *
   * @param testState
   */
  void onTestFinished(TestState testState);

  /**
   * Called when test failed
   *
   * @param testState
   */
  void onTestFailed(TestState testState);

  /**
   * Called when test ignored
   *
   * @param testState
   */
  void onTestIgnored(TestState testState);

  /**
   * Called when test root state presentation is added
   *
   * @param testRootState
   */
  void onRootPresentationAdded(TestRootState testRootState);
}
