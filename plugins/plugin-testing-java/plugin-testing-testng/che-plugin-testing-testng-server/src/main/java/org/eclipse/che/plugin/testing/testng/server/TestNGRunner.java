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
package org.eclipse.che.plugin.testing.testng.server;

import com.beust.jcommander.JCommander;
import com.google.inject.name.Named;

import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.execution.CommandLine;
import org.eclipse.che.commons.lang.execution.ExecutionException;
import org.eclipse.che.commons.lang.execution.JavaParameters;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.plugin.java.testing.AbstractJavaTestRunner;
import org.eclipse.che.plugin.java.testing.ClasspathUtil;
import org.eclipse.che.plugin.java.testing.JavaTestAnnotations;
import org.eclipse.che.plugin.java.testing.JavaTestFinder;
import org.eclipse.che.plugin.java.testing.ProjectClasspathProvider;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

/**
 * TestNG implementation for the test runner service.
 */
public class TestNGRunner extends AbstractJavaTestRunner {
    private static final String TESTNG_NAME    = "testng";
    private static final Logger LOG            = LoggerFactory.getLogger(TestNGRunner.class);
    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private       String                   workspacePath;
    private       JavaTestFinder           javaTestFinder;
    private final ProjectClasspathProvider classpathProvider;
    private final TestNGSuiteUtil          suiteUtil;

    @Inject
    public TestNGRunner(@Named("che.user.workspaces.storage") String workspacePath,
                        JavaTestFinder javaTestFinder,
                        ProjectClasspathProvider classpathProvider,
                        TestNGSuiteUtil suiteUtil) {
        super(workspacePath, javaTestFinder);
        this.workspacePath = workspacePath;
        this.javaTestFinder = javaTestFinder;
        this.classpathProvider = classpathProvider;
        this.suiteUtil = suiteUtil;
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
    @Deprecated
    public TestResultRootDto runTests(Map<String, String> testParameters) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public List<TestResultDto> getTestResults(List<String> testResultsPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nullable
    public ProcessHandler execute(TestExecutionContext context) {
        IJavaProject javaProject = getJavaProject(context.getProjectPath());
        if (javaProject.exists()) {
            return startTestProcess(javaProject, context);
        }

        return null;
    }

    private ProcessHandler startTestProcess(IJavaProject javaProject, TestExecutionContext context) {
        File suiteFile = createSuite(context, javaProject);
        if (suiteFile == null) {
            throw new RuntimeException("Can't create TestNG suite xml file.");
        }

        JavaParameters parameters = new JavaParameters();
        parameters.setJavaExecutable(JAVA_EXECUTABLE);
        parameters.setMainClassName("org.testng.CheTestNGLauncher");
        String outputDirectory = getOutputDirectory(javaProject);
        parameters.getParametersList().add("-d", outputDirectory);
        parameters.setWorkingDirectory(workspacePath + javaProject.getPath());
        List<String> classPath = new ArrayList<>();
        Set<String> projectClassPath = classpathProvider.getProjectClassPath(javaProject);
        classPath.addAll(projectClassPath);
        classPath.add(ClasspathUtil.getJarPathForClass(org.testng.CheTestNG.class));
        classPath.add(ClasspathUtil.getJarPathForClass(JCommander.class));
        parameters.getClassPath().addAll(classPath);

        parameters.getParametersList().add("-suiteFile", suiteFile.getAbsolutePath());
        if (context.isDebugModeEnable()) {
            generateDebuggerPort();
            parameters.getVmParameters().add("-Xdebug");
            parameters.getVmParameters().add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + getDebugPort());
        }
        CommandLine command = parameters.createCommand();
        try {
            return new ProcessHandler(command.createProcess());
        } catch (ExecutionException e) {
            LOG.error("Can't run TestNG JVM", e);
        }

        return null;
    }

    private File createSuite(TestExecutionContext context, IJavaProject javaProject) {
        List<String> testSuite = createTestSuite(context, javaProject, JavaTestAnnotations.TESTNG_TEST.getName(), "");

        Map<String, List<String>> classes = buildTestNgSuite(testSuite, context);

        return suiteUtil.writeSuite(System.getProperty(JAVA_IO_TMPDIR), javaProject.getElementName(), classes);
    }

    private Map<String, List<String>> buildTestNgSuite(List<String> tests, TestExecutionContext context) {
        switch (context.getContextType()) {
            case FILE:
            case FOLDER:
            case PROJECT:
                return createContainerSuite(tests);
            case CURSOR_POSITION:
                return createMethodSuite(tests);
            default:
                return emptyMap();
        }
    }

    private Map<String, List<String>> createMethodSuite(List<String> tests) {
        if (tests.isEmpty()) {
            return Collections.emptyMap();
        }

        String testMethodDeclaration = tests.get(0);
        int separatorIndex = testMethodDeclaration.indexOf('#');

        return Collections.singletonMap(testMethodDeclaration.substring(0, separatorIndex),
                                        singletonList(testMethodDeclaration.substring(separatorIndex + 1)));
    }

    private Map<String, List<String>> createContainerSuite(List<String> tests) {
        if (tests.isEmpty()) {
            return emptyMap();
        }
        Map<String, List<String>> classes = new HashMap<>(tests.size());
        for (String testClass : tests) {
            classes.put(testClass, null);
        }
        return classes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return TESTNG_NAME;
    }

    @Override
    protected boolean isTestMethod(IMethod method, ICompilationUnit compilationUnit) {
        return javaTestFinder.isTest(method, compilationUnit, JavaTestAnnotations.TESTNG_TEST.getName());
    }
}
