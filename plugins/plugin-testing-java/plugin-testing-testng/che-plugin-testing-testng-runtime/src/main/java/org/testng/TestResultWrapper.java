/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.testng;

import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Wrapper class for {@link ITestResult}, simplify access to internal data
 */
public class TestResultWrapper {
    private final ITestResult testResult;

    public TestResultWrapper(ITestResult testResult) {
        this.testResult = testResult;
    }

    public Throwable getThrowable() {
        return testResult.getThrowable();
    }

    public String getFileName() {
        XmlTest xmlTest = testResult.getTestClass().getXmlTest();
        if (xmlTest != null) {
            return xmlTest.getSuite().getFileName();
        }
        return null;
    }

    public String getXmlTestName() {
        XmlTest xmlTest = testResult.getTestClass().getXmlTest();
        if (xmlTest != null) {
            return xmlTest.getName();
        }
        return null;
    }

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

    public String getClassName() {
        return testResult.getMethod().getTestClass().getName();
    }

    public long getDuration() {
        return testResult.getEndMillis() - testResult.getStartMillis();
    }

    public String getDisplayMethodName() {
        String testName = testResult.getTestName();

        if (testName != null && !testName.isEmpty()) {
            return testName;
        } else {
            return testResult.getMethod().getMethodName();
        }
    }

    public String getMethodName() {
        return testResult.getMethod().getMethodName();
    }

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
