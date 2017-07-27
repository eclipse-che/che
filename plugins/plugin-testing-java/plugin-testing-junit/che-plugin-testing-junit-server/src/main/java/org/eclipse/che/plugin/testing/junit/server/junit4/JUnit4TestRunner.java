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
package org.eclipse.che.plugin.testing.junit.server.junit4;

import com.google.inject.name.Named;

import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.commons.lang.execution.CommandLine;
import org.eclipse.che.commons.lang.execution.ExecutionException;
import org.eclipse.che.commons.lang.execution.JavaParameters;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.junit.junit4.CheJUnitCoreRunner;
import org.eclipse.che.plugin.java.testing.AbstractJavaTestRunner;
import org.eclipse.che.plugin.java.testing.ClasspathUtil;
import org.eclipse.che.plugin.java.testing.ProjectClasspathProvider;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * JUnit implementation for the test runner service.
 */
public class JUnit4TestRunner extends AbstractJavaTestRunner {
    private static final Logger LOG = LoggerFactory.getLogger(JUnit4TestRunner.class);

    private static final String JUNIT_TEST_NAME = "junit";
    private static final String MAIN_CLASS_NAME = "org.eclipse.che.junit.junit4.CheJUnitLauncher";

    private String                   workspacePath;
    private JUnit4TestFinder         jUnit4TestFinder;
    private ProjectClasspathProvider classpathProvider;

    @Inject
    public JUnit4TestRunner(@Named("che.user.workspaces.storage") String workspacePath,
                            JUnit4TestFinder jUnit4TestFinder,
                            ProjectClasspathProvider classpathProvider) {
        super(workspacePath);
        this.workspacePath = workspacePath;
        this.jUnit4TestFinder = jUnit4TestFinder;
        this.classpathProvider = classpathProvider;
    }

    @Override
    public ProcessHandler execute(TestExecutionContext context) {
        IJavaProject javaProject = getJavaProject(context.getProjectPath());
        if (javaProject.exists()) {
            return startTestProcess(javaProject, context);
        }

        return null;
    }

    @Override
    public String getName() {
        return JUNIT_TEST_NAME;
    }

    @Override
    public boolean isTestMethod(IMethod method, ICompilationUnit compilationUnit) {
        try {
            int flags = method.getFlags();
            // 'V' is void signature
            return !(method.isConstructor() ||
                     !Flags.isPublic(flags) ||
                     Flags.isAbstract(flags) ||
                     Flags.isStatic(flags) ||
                     !"V".equals(method.getReturnType()))
                   && jUnit4TestFinder.isTest(method, compilationUnit);

        } catch (JavaModelException ignored) {
            return false;
        }
    }

    private ProcessHandler startTestProcess(IJavaProject javaProject, TestExecutionContext context) {
        JavaParameters parameters = new JavaParameters();
        parameters.setJavaExecutable(JAVA_EXECUTABLE);
        parameters.setMainClassName(MAIN_CLASS_NAME);
        parameters.setWorkingDirectory(workspacePath + javaProject.getPath());

        List<String> classPath = new ArrayList<>();
        Set<String> projectClassPath = classpathProvider.getProjectClassPath(javaProject);
        classPath.addAll(projectClassPath);
        classPath.add(ClasspathUtil.getJarPathForClass(CheJUnitCoreRunner.class));
        parameters.getClassPath().addAll(classPath);

        List<String> suite = createTestSuite(context, javaProject);
        for (String element : suite) {
            parameters.getParametersList().add(element);
        }
        if (context.isDebugModeEnable()) {
            generateDebuggerPort();
            parameters.getVmParameters().add("-Xdebug");
            parameters.getVmParameters().add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + getDebugPort());
        }
        CommandLine command = parameters.createCommand();
        try {
            return new ProcessHandler(command.createProcess());
        } catch (ExecutionException e) {
            LOG.error("Can't run JUnit JVM", e);
        }

        return null;
    }

    private List<String> createTestSuite(TestExecutionContext context, IJavaProject javaProject) {
        switch (context.getContextType()) {
            case FILE:
                return jUnit4TestFinder.findTestClassDeclaration(findCompilationUnitByPath(javaProject, context.getFilePath()));
            case FOLDER:
                return jUnit4TestFinder.findClassesInPackage(javaProject, context.getFilePath());
            case PROJECT:
                return jUnit4TestFinder.findClassesInProject(javaProject);
            case CURSOR_POSITION:
                return jUnit4TestFinder.findTestMethodDeclaration(findCompilationUnitByPath(javaProject, context.getFilePath()),
                                                                  context.getCursorOffset());
        }

        return emptyList();
    }

    @Override
    @Deprecated
    public TestResult execute(Map<String, String> testParameters) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestResultRootDto runTests(Map<String, String> testParameters) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TestResultDto> getTestResults(List<String> testResultsPath) {
        throw new UnsupportedOperationException();
    }
}
