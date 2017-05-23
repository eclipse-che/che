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
package org.eclipse.che.api.testing.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * DTO for representing the executed test result of a Java unit test.
 *
 * @author Mirage Abeysekara
 */
@DTO
@Deprecated
public interface TestResult {

    /**
     * Returns the path of the project containing the test cases.
     * 
     * @return the framework name
     */
    String getProjectPath();

    /**
     * Sets the path of the project containing the test cases.
     * 
     * @param projectPath
     */
    void setProjectPath(String projectPath);

    /**
     * Returns the framework name used for executing the test cases.
     * 
     * @return the framework name
     */
    String getTestFramework();

    /**
     * Sets the framework name used for executing the test cases.
     * 
     * @param framework
     */
    void setTestFramework(String framework);

    /**
     * Indicates whether the test was successful.
     * 
     * @return true if all tests are passed.
     */
    boolean isSuccess();

    /**
     * Sets whether the test was successful.
     * 
     * @param success
     */
    void setSuccess(boolean success);

    /**
     * Returns the details of the test cases.
     * 
     * @return a list of test cases.
     */
    List<TestCase> getTestCases();

    /**
     * Sets the details of the test cases.
     * 
     * @param failures
     */
    void setTestCases(List<TestCase> failures);

    /**
     * Indicates how many tests were run.
     * 
     * @return the count of run test cases.
     */
    int getTestCaseCount();

    /**
     * Sets how many tests were run.
     * 
     * @param count
     */
    void setTestCaseCount(int count);

    /**
     * Indicates how many tests were failed.
     * 
     * @return the count of test failures.
     */
    int getFailureCount();

    /**
     * Sets how many tests were failed.
     * 
     * @param count
     */
    void setFailureCount(int count);
}
