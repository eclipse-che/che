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
package org.eclipse.che.junit.junit4.listeners;

import java.io.PrintStream;
import org.eclipse.che.junit.TestingMessageHelper;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/** Listener for whole life cycle of the JUnit test run. */
public class CheJUnitTestListener {
  private final PrintStream out;

  private long myCurrentTestStart;

  public CheJUnitTestListener() {
    this.out = System.out;
    TestingMessageHelper.reporterAttached(out);
  }

  /** Called before any tests have been run. */
  public void testRunStarted() {
    TestingMessageHelper.rootPresentation(out);
  }

  /**
   * Called when an atomic test is about to be started.
   *
   * @param description the description of the test that is about to be run (generally a class and
   *     method name)
   */
  public void testStarted(Description description) {
    myCurrentTestStart = System.currentTimeMillis();

    TestingMessageHelper.testStarted(out, description);
  }

  /**
   * Called when an atomic test has finished, whether the test succeeds or fails.
   *
   * @param description the description of the test that just ran
   */
  public void testFinished(Description description) {
    long duration = System.currentTimeMillis() - myCurrentTestStart;

    TestingMessageHelper.testFinished(out, description, duration);
  }

  /**
   * Called when test suite starts.
   *
   * @param description the description of the test suite
   */
  public void testSuiteStarted(Description description) {
    TestingMessageHelper.testSuiteStarted(out, description);
  }

  /**
   * Called when test suite finished.
   *
   * @param currentSuite name of test suite
   */
  public void testSuiteFinished(String currentSuite) {
    TestingMessageHelper.testSuiteFinished(out, currentSuite);
  }

  /**
   * Called when an atomic test fails.
   *
   * @param failure describes the test that failed and the exception that was thrown
   */
  public void testFailure(Failure failure) {
    long duration = System.currentTimeMillis() - myCurrentTestStart;

    TestingMessageHelper.testFailed(out, failure, duration);
  }

  /**
   * Called when all tests have finished
   *
   * @param result the summary of the test run, including all the tests that failed
   */
  public void testRunFinished(Result result) {
    TestingMessageHelper.testRunFinished(out, result);
  }

  /**
   * Called when a test will not be run, generally because a test method is annotated with {@link
   * org.junit.Ignore}.
   *
   * @param description describes the test that will not be run
   */
  public void testIgnored(Description description) {
    TestingMessageHelper.testIgnored(out, description.getMethodName());
  }

  /**
   * Parse test tree and send atomic test nodes.
   *
   * @param description the description of the test tree
   */
  public void suiteSendTree(Description description) {
    if (description.isTest()) {
      TestingMessageHelper.treeNode(out, description);
    } else {
      suiteTreeStarted(description);
      for (Description child : description.getChildren()) {
        suiteSendTree(child);
      }
      suiteTreeEnded(description);
    }
  }

  /**
   * Called when build of test tree started.
   *
   * @param description describes the test suite
   */
  public void suiteTreeStarted(Description description) {
    TestingMessageHelper.suiteTreeNodeStarted(out, description);
  }

  /**
   * Called when build of test tree finished.
   *
   * @param description describes the test suite
   */
  public void suiteTreeEnded(Description description) {
    TestingMessageHelper.suiteTreeNodeEnded(out, description);
  }
}
