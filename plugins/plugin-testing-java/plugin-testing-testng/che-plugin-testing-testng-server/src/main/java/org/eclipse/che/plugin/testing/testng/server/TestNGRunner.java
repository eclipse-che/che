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
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.execution.CommandLine;
import org.eclipse.che.commons.lang.execution.ExecutionException;
import org.eclipse.che.commons.lang.execution.JavaParameters;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.plugin.java.testing.ClasspathUtil;
import org.eclipse.che.plugin.java.testing.ProjectClasspathProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TestNG implementation for the test runner service.
 *
 * @author Mirage Abeysekara
 */
public class TestNGRunner implements TestRunner {

    private static final Logger LOG = LoggerFactory.getLogger(TestNGRunner.class);

    private final ProjectClasspathProvider classpathProvider;
    private final TestNGSuiteUtil suiteUtil;

    @Inject
    public TestNGRunner(ProjectClasspathProvider classpathProvider, TestNGSuiteUtil suiteUtil) {
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
    @Nullable
    public ProcessHandler execute(TestExecutionContext context) {
        String projectPath = context.getProjectPath();
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath);
        IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(project);
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
        parameters.setJavaExecutable("java");
        parameters.setMainClassName("org.testng.CheTestNGLauncher");
        parameters.setWorkingDirectory("/tmp");
        List<String> classPath = new ArrayList<>();
        Set<String> projectClassPath = classpathProvider.getProjectClassPath(javaProject);
        classPath.addAll(projectClassPath);
        classPath.add(ClasspathUtil.getJarPathForClass(org.testng.CheTestNG.class));
        classPath.add(ClasspathUtil.getJarPathForClass(JCommander.class));
        parameters.getClassPath().addAll(classPath);

        parameters.getParametersList().add("-suiteFile", suiteFile.getAbsolutePath());
        CommandLine command = parameters.createCommand();
        try {
            return new ProcessHandler(command.createProcess());
        } catch (ExecutionException e) {
            LOG.error("Can't run TestNG JVM", e);
        }

        return null;
    }

    private File createSuite(TestExecutionContext context, IJavaProject javaProject) {

        switch (context.getTestType()) {
            case FILE:
                return createClassSuite(javaProject, context.getFilePath());
            case FOLDER:
                return createPackageSuite(javaProject, context.getFilePath());

            case PROJECT:
                return createProjectSuite(javaProject);
            case CURSOR_POSITION:
                return createMethodSuite(javaProject, context.getFilePath(), context.getCursorOffset());
        }

        return null;
    }

    private File createMethodSuite(IJavaProject javaProject, String filePath, int cursorOffset) {
        //TODO
        return null;
    }

    private File createProjectSuite(IJavaProject javaProject) {
        //TODO
        return null;
    }

    private File createPackageSuite(IJavaProject javaProject, String packagePath) {
        //TODO
        return null;
    }

    private File createClassSuite(IJavaProject javaProject, String filePath) {

        try {
            IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(false);
            IPath packageRootPath = null;
            for (IClasspathEntry classpathEntry : resolvedClasspath) {
                if (filePath.startsWith(classpathEntry.getPath().toOSString())) {
                    packageRootPath = classpathEntry.getPath();
                    break;
                }
            }

            if (packageRootPath == null) {
                throw new RuntimeException("Can't find IClasspathEntry for path " + filePath);
            }

            String packagePath = packageRootPath.toOSString();
            if (!packagePath.endsWith("/")) {
                packagePath += "/";
            }

            String pathToClass = filePath.substring(packagePath.length());
            IJavaElement element = javaProject.findElement(new Path(pathToClass));
            if (element != null && element instanceof ICompilationUnit) {
                ICompilationUnit compilationUnit = (ICompilationUnit) element;
                IType primaryType = compilationUnit.findPrimaryType();
                String qualifiedName = primaryType.getFullyQualifiedName();
                Map<String, List<String>> classes = Collections.singletonMap(qualifiedName, null);
                return suiteUtil.writeSuite(System.getProperty("java.io.tmpdir"), javaProject.getElementName(), classes);
            } else {
                throw new RuntimeException("Can't find class: " + pathToClass);
            }

        } catch (JavaModelException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "testng";
    }

}
