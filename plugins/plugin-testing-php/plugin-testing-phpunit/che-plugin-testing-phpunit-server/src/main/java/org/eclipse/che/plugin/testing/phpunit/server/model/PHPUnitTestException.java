/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.testing.phpunit.server.model;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.plugin.testing.phpunit.server.PHPUnitMessageParser;

/**
 * Test exception model element implementation.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestException extends AbstractPHPUnitTestEvent {

  private static final String TOP_CLASS = "Exception"; // $NON-NLS-1$
  private String exceptionClass = TOP_CLASS;

  public PHPUnitTestException(Map<?, ?> exception, PHPUnitTestCase parent) {
    super(exception, parent);
    exceptionClass = (String) exception.get(PHPUnitMessageParser.PROPERTY_CLASS);
  }

  /**
   * Returns exception class.
   *
   * @return exception class
   */
  public String getExceptionClass() {
    return exceptionClass;
  }

  /**
   * Sets abnormal exception if occurred.
   *
   * @param testCase
   */
  public static void addAbnormalException(PHPUnitTestCase testCase) {
    // if(ABNORMAL_EXCEPTION == null) {
    Map<String, String> exception = new HashMap<String, String>();
    exception.put(PHPUnitMessageParser.PROPERTY_CLASS, "An unexpected termination has occurred");
    exception.put(
        PHPUnitMessageParser.PROPERTY_MESSAGE, "The test case was unexpectedly terminated");
    PHPUnitTestException abnormalException = new PHPUnitTestException(exception, null);
    abnormalException.setParent(testCase);
    testCase.setException(abnormalException);
    testCase.setStatus(AbstractPHPUnitTestResult.STATUS_ERROR);
    PHPUnitTestSuite parent = (PHPUnitTestSuite) testCase.getParent();
    if (parent != null) {
      parent.setStatus(testCase.getStatus());
    }
    testCase.setException(new PHPUnitTestException(exception, testCase));
  }
}
