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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.LinkedList;
import java.util.List;
import org.junit.runner.Request;
import org.junit.runner.Runner;

/** Utility class for building test executing request. */
public class TestRunnerUtil {
  /**
   * Build list of {@clink JUnit4TestReference}.
   *
   * @param suites array of test classes or test method (if args.length == 1) to execute
   * @return list of {@link JUnit4TestReference}
   */
  public static List<JUnit4TestReference> createTestReferences(String[] suites) {
    if (suites.length == 0) {
      return emptyList();
    } else if (suites.length == 1) {
      String suite = suites[0];
      int separatorIndex = suite.indexOf('#');
      return separatorIndex == -1
          ? getRequestForClass(suite)
          : getRequestForOneMethod(suite, separatorIndex);
    }

    return getRequestForClasses(suites);
  }

  private static List<JUnit4TestReference> getRequestForOneMethod(
      String suite, int separatorIndex) {
    try {
      Class suiteClass = Class.forName(suite.substring(0, separatorIndex));
      String method = suite.substring(separatorIndex + 1);
      Request request = Request.method(suiteClass, method);
      Runner runner = request.getRunner();
      return singletonList(new JUnit4TestReference(runner, runner.getDescription()));
    } catch (ClassNotFoundException e) {
      System.err.print("No test found to run.");
      return emptyList();
    }
  }

  private static List<JUnit4TestReference> getRequestForClass(String suite) {
    try {
      Request request = Request.aClass(Class.forName(suite));
      Runner runner = request.getRunner();
      return singletonList(new JUnit4TestReference(runner, runner.getDescription()));
    } catch (ClassNotFoundException e) {
      System.err.print("No test found to run.");
      return emptyList();
    }
  }

  private static List<JUnit4TestReference> getRequestForClasses(String[] args) {
    List<JUnit4TestReference> suites = new LinkedList<>();
    for (String classFqn : args) {
      try {
        Class<?> aClass = Class.forName(classFqn);
        Request request = Request.aClass(aClass);
        Runner runner = request.getRunner();
        suites.add(new JUnit4TestReference(runner, runner.getDescription()));
      } catch (ClassNotFoundException ignored) {
      }
    }
    if (suites.isEmpty()) {
      System.err.print("No test found to run.");
      return emptyList();
    }
    return suites;
  }
}
