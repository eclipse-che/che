/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.phpunit.server;

import java.util.ArrayList;
import java.util.Map;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestCase;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestException;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestRoot;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestSuite;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestWarning;

/**
 * PHPUnit message parser.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitMessageParser {

  public static final String CALL_DYNAMIC = "->";
  public static final String CALL_STATIC = "::";
  private static final String ELEMENT_EVENT = "event";
  private static final String ELEMENT_EXCEPTION = "exception";
  private static final String ELEMENT_TARGET_TESTSUITE = "testsuite";
  private static final String ELEMENT_TARGET_TESTCASE = "testcase";
  private static final String ELEMENT_TEST = "test";
  private static final String ELEMENT_WARNINGS = "warnings";
  public static final String PROPERTY_CLASS = "class";
  public static final String PROPERTY_CODE = "code";
  public static final String PROPERTY_COUNT = "tests";
  public static final String PROPERTY_FILE = "file";
  public static final String PROPERTY_FILTERED = "filtered";
  public static final String PROPERTY_LINE = "line";
  public static final String PROPERTY_MESSAGE = "message";
  public static final String PROPERTY_DIFF = "diff";
  public static final String PROPERTY_NAME = "name";
  public static final String PROPERTY_TIME = "time";
  public static final String PROPERTY_TARGET = "target";
  public static final String PROPERTY_TRACE = "trace";
  public static final String STATUS_ERROR = "error";
  public static final String STATUS_WARNING = "warning";
  public static final String STATUS_FAIL = "fail";
  public static final String STATUS_INCOMPLETE = "incomplete";
  public static final String STATUS_PASS = "pass";
  public static final String STATUS_SKIP = "skip";
  public static final String TAG_END = "end";
  public static final String TAG_START = "start";

  private PHPUnitTestSuite currentGroup;
  private PHPUnitTestCase currentTestCase;

  public PHPUnitMessageParser(PHPUnitTestRoot testRoot) {
    this.currentGroup = testRoot;
  }

  /**
   * Parses provided message from PHPUnit printer.
   *
   * @param message
   */
  public void parse(final Map<?, ?> message) {
    if (message == null) {
      return;
    }
    final String target = (String) message.get(PROPERTY_TARGET);
    final String event = (String) message.get(ELEMENT_EVENT);
    final Map<?, ?> mTest = (Map<?, ?>) message.get(ELEMENT_TEST);
    if (target.equals(ELEMENT_TARGET_TESTSUITE)) {
      parseGroupStart(event, mTest);
      if (event.equals(TAG_END)) {
        parseGroupEnd();
      }
    } else if (target.equals(ELEMENT_TARGET_TESTCASE)) {
      if (event.equals(TAG_START)) {
        parseTestStart(event, mTest);
      } else {
        parseTestEnd(message, event, mTest);
      }
    }
  }

  private void parseGroupStart(final String event, final Map<?, ?> mTest) {
    if (event.equals(TAG_START)) {
      final PHPUnitTestSuite group = new PHPUnitTestSuite(mTest, currentGroup);
      currentGroup.addChild(group, false);
      group.setParent(currentGroup);
      currentGroup = group;
    }
  }

  private void parseGroupEnd() {
    final PHPUnitTestSuite group = currentGroup;
    currentGroup = (PHPUnitTestSuite) currentGroup.getParent();
    currentGroup.addChild(group, true);
  }

  private void parseTestStart(final String event, final Map<?, ?> mTest) {
    final PHPUnitTestCase testCase = new PHPUnitTestCase(mTest, currentGroup, event);
    currentTestCase = testCase;
    currentGroup.addChild(testCase, false);
  }

  private void parseTestEnd(final Map<?, ?> message, final String event, final Map<?, ?> mTest) {
    final PHPUnitTestCase testCase = currentTestCase;
    testCase.updateStatus(event);
    final Map<?, ?> exception = (Map<?, ?>) message.get(ELEMENT_EXCEPTION);
    if (exception != null) parseException(testCase, exception);
    final Map<?, ?> warnings = (Map<?, ?>) message.get(ELEMENT_WARNINGS);
    if (warnings != null) parseWarnings(testCase, warnings);
    final String time = (String) message.get(PROPERTY_TIME);
    testCase.setTime(Double.valueOf(time));
    currentGroup.addChild(testCase, true);
  }

  /**
   * @param testCase
   * @param exception
   */
  private void parseException(final PHPUnitTestCase testCase, final Map<?, ?> exception) {
    testCase.setException(new PHPUnitTestException(exception, testCase));
  }

  private void parseWarnings(final PHPUnitTestCase testCase, final Map<?, ?> warnings) {
    Map<?, ?> mWarning;
    // keep initial order
    for (int i = 0; (mWarning = (Map<?, ?>) warnings.get(String.valueOf(i))) != null; ++i) {
      if (testCase.getWarnings() == null)
        testCase.setWarnings(new ArrayList<PHPUnitTestWarning>(warnings.size()));
      final PHPUnitTestWarning warning = new PHPUnitTestWarning(mWarning, testCase);
      testCase.getWarnings().add(i, warning);
    }
  }
}
