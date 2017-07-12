/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.phpunit.server;

import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * PHPUnit implementation for the test runner service.
 *
 * <pre>
 * Available Parameters for {@link PHPUnitTestRunner#runTests(Map)}
 *
 * <em>projectPath</em> : Relative path to the project directory
 * <em>absoluteProjectPath</em> : Absolute path to the project directory
 * <em>testTarget</em> : Path to test target (container or script).
 * </pre>
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestRunner implements TestRunner {

    public static final Logger      LOG       = LoggerFactory.getLogger(PHPUnitTestRunner.class);
    public static final String      RUNNER_ID = "PHPUnit";

    private final PHPUnitTestEngine testEngine;

    @Inject
    public PHPUnitTestRunner(ProjectManager projectManager) {
        testEngine = new PHPUnitTestEngine(projectManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return RUNNER_ID;
    }

    @Override
    public int getDebugPort() {
        return -1;
    }

    @Override
    public List<TestPosition> detectTests(TestDetectionContext context) {
        return emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResultRootDto runTests(Map<String, String> testParameters) throws Exception {
        return testEngine.executeTests(testParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TestResultDto> getTestResults(List<String> testResultsPath) {
        return testEngine.getTestResults(testResultsPath);
    }

    @Override
    public ProcessHandler execute(TestExecutionContext context) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResult execute(Map<String, String> testParameters) throws Exception {
        return null;
    }

}
