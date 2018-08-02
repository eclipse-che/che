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
package org.testng.che.tests;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.Assert;
import org.testng.CheTestNGListener;
import org.testng.TestResultWrapper;
import org.testng.annotations.Test;

public class ListeneterTest {

  @Test
  public void testListener() throws Exception {
    StringBuffer buffer = new StringBuffer();
    CheTestNGListener listener = createListener(buffer);
    String className = "the.Test";
    listener.onSuiteStart(className, true);

    TestResult setUp = new TestResult(className, "setUp");
    listener.internalOnTestStart(setUp, null, -1, true);
    listener.internalOnConfigurationSuccess(setUp);
    TestResult result = new TestResult(className, "testMethod");
    listener.internalOnTestStart(result);
    listener.onTestFinished(result);
    TestResult tearDown = new TestResult(className, "tearDown");
    listener.internalOnTestStart(tearDown, null, -1, true);
    listener.internalOnConfigurationSuccess(tearDown);

    listener.internalSuiteFinish(className);

    Assert.assertEquals(
        buffer.toString(),
        "@@<{\"name\":\"testReporterAttached\"}>\n"
            + "@@<{\"name\":\"testSuiteStarted\", \"attributes\":{\"name\":\"Test\", \"location\":\"java:suite://the.Test\"}}>\n"
            + "@@<{\"name\":\"testStarted\", \"attributes\":{\"name\":\"Test.setUp\", \"locationHint\":\"java:test://the.Test.setUp\", \"config\":\"true\"}}>\n"
            + "@@<{\"name\":\"testFinished\", \"attributes\":{\"name\":\"Test.setUp\", \"duration\":\"0\"}}>\n"
            + "@@<{\"name\":\"testStarted\", \"attributes\":{\"name\":\"Test.testMethod\", \"locationHint\":\"java:test://the.Test.testMethod[0]\", \"config\":\"false\"}}>\n"
            + "@@<{\"name\":\"testFinished\", \"attributes\":{\"name\":\"Test.testMethod\", \"duration\":\"0\"}}>\n"
            + "@@<{\"name\":\"testStarted\", \"attributes\":{\"name\":\"Test.tearDown\", \"locationHint\":\"java:test://the.Test.tearDown\", \"config\":\"true\"}}>\n"
            + "@@<{\"name\":\"testFinished\", \"attributes\":{\"name\":\"Test.tearDown\", \"duration\":\"0\"}}>\n"
            + "@@<{\"name\":\"testSuiteFinished\", \"attributes\":{\"name\":\"the.Test\"}}>\n");
  }

  private CheTestNGListener createListener(StringBuffer buffer) {
    return new CheTestNGListener(
        new PrintStream(
            new OutputStream() {
              @Override
              public void write(int b) throws IOException {
                buffer.append(new String(new byte[] {(byte) b}));
              }
            }));
  }

  private static class TestResult extends TestResultWrapper {

    private final String className;
    private final String methodName;
    private final Throwable throwable;
    private final Object[] parames;

    public TestResult(String className, String methodName, Throwable throwable, Object[] parames) {
      super(null);
      this.className = className;
      this.methodName = methodName;
      this.throwable = throwable;
      this.parames = parames;
    }

    public TestResult(String className, String methodName) {
      this(className, methodName, null, new Object[0]);
    }

    @Override
    public Throwable getThrowable() {
      return throwable;
    }

    @Override
    public String getFileName() {
      return null;
    }

    @Override
    public String getXmlTestName() {
      return null;
    }

    @Override
    public List<String> getTestHierarchy() {
      return Collections.singletonList(className);
    }

    @Override
    public String getClassName() {
      return className;
    }

    @Override
    public long getDuration() {
      return 0;
    }

    @Override
    public String getDisplayMethodName() {
      return methodName;
    }

    @Override
    public String getMethodName() {
      return methodName;
    }

    @Override
    public Object[] getParameters() {
      return parames;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      if (!super.equals(o)) return false;

      TestResult result = (TestResult) o;

      if (className != null ? !className.equals(result.className) : result.className != null)
        return false;
      if (methodName != null ? !methodName.equals(result.methodName) : result.methodName != null)
        return false;
      if (throwable != null ? !throwable.equals(result.throwable) : result.throwable != null)
        return false;
      // Probably incorrect - comparing Object[] arrays with Arrays.equals
      return Arrays.equals(parames, result.parames);
    }

    @Override
    public int hashCode() {
      int result = className.hashCode();
      result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
      result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
      result = 31 * result + Arrays.hashCode(parames);
      return result;
    }
  }
}
