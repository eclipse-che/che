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
package org.eclipse.che.ide.ext.java.testing.core.server.framework;

import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathProvider;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;

import java.util.Map;

/**
 * Interface for defining test frameworks for the test runner. All test framework implementations should
 * implement this interface in order to register for the test runner service.
 *
 * @author Mirage Abeysekara
 */
public interface TestRunner {

    /**
     * Call this method to execute the test cases and return the results.
     * The test runner framework will automatically call this method to execute tests.
     *
     * @param testParameters    Map of parameters for executing the test cases. Most of the parameters are coming from
     *                          the REST service request are passed as a map.
     * @param classpathProvider The classpath provider for executing the test cases.
     * @return the test results.
     * @throws Exception when test runner execution fails.
     */
    TestResult execute(Map<String, String> testParameters, TestClasspathProvider classpathProvider) throws Exception;

    /**
     * The test runner framework will call this method to get the framework name for registration.
     *
     * @return the implementation framework name
     */
    String getName();
}
