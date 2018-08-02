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
package org.eclipse.che.junit.junit4;

import org.eclipse.che.junit.junit4.listeners.CheJUnitTestListener;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/** Describes request of the test runner. */
public class JUnit4TestReference {
  private final Runner runner;
  private final Description description;

  public JUnit4TestReference(Runner runner, Description root) {
    this.runner = runner;
    description = root;
  }

  /** Returns count of the test methods. */
  public int countTestCases() {
    return countTestCases(description);
  }

  /** Returns a {@link Runner} for this Request */
  public Runner getRunner() {
    return runner;
  }

  /** @return a {@link Description} showing the tests to be run by the receiver. */
  public Description getDescription() {
    return description;
  }

  /**
   * Run the tests for this runner.
   *
   * @param runNotifier will be notified of events while tests are being run--tests being started,
   *     finishing, and failing
   */
  public void run(RunNotifier runNotifier) {
    runner.run(runNotifier);
  }

  /** Sends tree structure of the current test. */
  public void sendTree(CheJUnitTestListener listener) {
    if (description.isTest()) listener.suiteTreeStarted(description);

    listener.suiteSendTree(description);
    listener.suiteTreeEnded(description);
  }

  @Override
  public String toString() {
    return description.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof JUnit4TestReference)) return false;

    JUnit4TestReference ref = (JUnit4TestReference) obj;
    return (ref.description.equals(description));
  }

  @Override
  public int hashCode() {
    return description.hashCode();
  }

  private int countTestCases(Description description) {
    if (description.isTest()) {
      return 1;
    } else {
      int result = 0;
      for (Description child : description.getChildren()) {
        result += countTestCases(child);
      }
      return result;
    }
  }
}
