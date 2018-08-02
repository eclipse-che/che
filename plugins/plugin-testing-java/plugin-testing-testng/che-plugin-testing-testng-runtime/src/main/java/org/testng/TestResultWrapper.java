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
package org.testng;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.xml.XmlTest;

/** Wrapper class for {@link ITestResult}, simplify access to internal data */
public class TestResultWrapper {
  private final ITestResult testResult;

  public TestResultWrapper(ITestResult testResult) {
    this.testResult = testResult;
  }

  /**
   * Returns the throwable that was thrown while running the method, or null if no exception was
   * thrown.
   */
  public Throwable getThrowable() {
    return testResult.getThrowable();
  }

  /** @return the fileName or null if the test tag this class was found in. */
  public String getFileName() {
    XmlTest xmlTest = testResult.getTestClass().getXmlTest();
    if (xmlTest != null) {
      return xmlTest.getSuite().getFileName();
    }
    return null;
  }

  /** @return Returns the name. */
  public String getXmlTestName() {
    XmlTest xmlTest = testResult.getTestClass().getXmlTest();
    if (xmlTest != null) {
      return xmlTest.getName();
    }
    return null;
  }

  /** @return Returns the test hierarchy. */
  public List<String> getTestHierarchy() {
    List<String> result;
    XmlTest xmlTest = testResult.getTestClass().getXmlTest();
    if (xmlTest != null) {
      result = Arrays.asList(getClassName(), xmlTest.getName());
    } else {
      result = Collections.singletonList(getClassName());
    }
    return result;
  }

  /** @return Returns the name of test class. */
  public String getClassName() {
    return testResult.getMethod().getTestClass().getName();
  }

  /** @return Returns duration of the test running. */
  public long getDuration() {
    return testResult.getEndMillis() - testResult.getStartMillis();
  }

  /** @return Returns the display name of test method. */
  public String getDisplayMethodName() {
    String testName = testResult.getTestName();

    if (testName != null && !testName.isEmpty()) {
      return testName;
    } else {
      return testResult.getMethod().getMethodName();
    }
  }

  /**
   * Returns the method name. This is needed for serialization because methods are not Serializable.
   *
   * @return the method name.
   */
  public String getMethodName() {
    return testResult.getMethod().getMethodName();
  }

  /** @return The parameters this method was invoked with. */
  public Object[] getParameters() {
    return testResult.getParameters();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TestResultWrapper wrapper = (TestResultWrapper) o;

    return testResult.equals(wrapper.testResult);
  }

  @Override
  public int hashCode() {
    return testResult.hashCode();
  }
}
