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
package org.eclipse.che.plugin.testing.junit.server;

import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.commons.lang.execution.ProcessHandler;

import javax.inject.Inject;
import java.util.Map;

/**
 * JUnit implementation for the test runner service.
 *
 * <pre>
 *
 * <em>absoluteProjectPath</em> : Absolute path to the project directory
 * <em>updateClasspath</em> : A boolean indicating whether rebuilding of class path is required.
 * <em>runClass</em> : A boolean indicating whether the test runner should execute all the test cases or a test class
 *            indicated by <em>fqn</em> parameter.
 * <em>fqn</em> : Fully qualified class name of the test class if the <em>runClass</em> is true.
 * </pre>
 *
 * @author Mirage Abeysekara
 * @author David Festal
 */
public class JUnitTestRunner implements TestRunner {

    private static final String   JUNIT4X_RUNNER_CLASS = "org.junit.runner.JUnitCore";
    private static final String   JUNIT3X_RUNNER_CLASS = "junit.textui.TestRunner";
    private ClassLoader           projectClassLoader;

    @Inject
    public JUnitTestRunner() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public TestResult execute(Map<String, String> testParameters) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProcessHandler execute(TestExecutionContext context) {
        //TODO Need implement this method
        throw new UnsupportedOperationException("Need implement this method");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "junit";
    }

}
