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

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for representing the executed test result of a Java unit test.
 *
 * @author Mirage Abeysekara
 */
@DTO
public interface TestResult {

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
     * Returns the details of the failing test cases.
     * 
     * @return a list of test failures.
     */
    List<Failure> getFailures();

    /**
     * Sets the details of the failing test cases.
     * 
     * @param failures
     */
    void setFailures(List<Failure> failures);

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
