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
package org.eclipse.che.api.testing.server.framework;

import java.util.List;
import java.util.Map;

import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;

/**
 * Interface for defining test frameworks for the test runner. All test
 * framework implementations should implement this interface in order to
 * register for the test runner service.
 *
 * @author Mirage Abeysekara
 */
public interface TestRunner {

    /**
     * Call this method to execute the test cases and return the results. The
     * test runner framework will automatically call this method to execute
     * tests.
     *
     * @param testParameters
     *            Map of parameters for executing the test cases. Most of the
     *            parameters are coming from the REST service request are passed
     *            as a map.
     * @return the test results.
     * @throws Exception
     *             when test runner execution fails.
     */
    @Deprecated
    TestResult execute(Map<String, String> testParameters) throws Exception;

    /**
     * Call this method to execute the test cases and return the results. The
     * test runner framework will automatically call this method to execute
     * tests.
     *
     * @param testParameters
     *            Map of parameters for executing the test cases. Most of the
     *            parameters are coming from the REST service request are passed
     *            as a map.
     * @return the test results.
     * @throws Exception
     *             when test runner execution fails.
     */
    TestResultRootDto runTests(Map<String, String> testParameters) throws Exception;

    /**
     * Call this method to get child elements for test result that is stored
     * under given path (i.e. test cases/test suites that are grouped by related
     * test suite).
     * 
     * @param testResultsPath
     * @return child elements for test result under given path
     */
    List<TestResultDto> getTestResults(List<String> testResultsPath);

    /**
     * The test runner framework will call this method to get the framework name
     * for registration.
     *
     * @return the implementation framework name
     */
    String getName();
}
