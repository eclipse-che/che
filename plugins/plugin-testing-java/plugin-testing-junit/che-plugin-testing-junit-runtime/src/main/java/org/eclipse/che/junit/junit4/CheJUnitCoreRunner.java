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

import java.util.List;
import org.eclipse.che.junit.TestingMessageHelper;
import org.eclipse.che.junit.junit4.listeners.CheJUnitTestListener;
import org.eclipse.che.junit.junit4.listeners.JUnitExecutionListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

/** Custom JUnit4 runner that reports results visa {@link CheJUnitTestListener}. */
public class CheJUnitCoreRunner extends JUnitCore {
  private CheJUnitTestListener cheJUnitTestListener;

  /**
   * Create a <code>request</code> where all tests are described and run it.
   *
   * @param suites the name of the test classes to be executed. If array has one element - it is an
   *     information about test method to be executed (for example
   *     full.qualified.ClassName#methodName)
   */
  public void run(String[] suites) {
    createListener();

    List<JUnit4TestReference> newSuites = TestRunnerUtil.createTestReferences(suites);

    if (newSuites.isEmpty()) {
      TestingMessageHelper.reporterAttached(System.out);
      return;
    }

    RunNotifier runNotifier = new RunNotifier();
    runNotifier.addListener(new JUnitExecutionListener(cheJUnitTestListener));
    cheJUnitTestListener.testRunStarted();

    for (JUnit4TestReference jUnit4TestReference : newSuites) {
      jUnit4TestReference.sendTree(cheJUnitTestListener);
    }

    Result result = new Result();
    final RunListener listener = result.createListener();
    runNotifier.addListener(listener);

    for (JUnit4TestReference testReference : newSuites) {
      testReference.run(runNotifier);
    }
    runNotifier.fireTestRunFinished(result);
  }

  /** Creates custom listener {@link CheJUnitTestListener} and adds it to */
  private void createListener() {
    cheJUnitTestListener = new CheJUnitTestListener();
    this.addListener(new JUnitExecutionListener(cheJUnitTestListener));
  }
}
