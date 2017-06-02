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
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.execution.CommandLine;
import org.eclipse.che.commons.lang.execution.ExecutionException;
import org.eclipse.che.commons.lang.execution.JavaParameters;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.plugin.java.testing.AbstractJavaTestRunner;
import org.eclipse.che.plugin.java.testing.ClasspathUtil;
import org.eclipse.che.plugin.java.testing.ProjectClasspathProvider;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

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
public class TestNGRunner extends AbstractJavaTestRunner {

    private static final String TEST_ANNOTATION_FQN = Test.class.getName();
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
        ICompilationUnit compilationUnit = findCompilationUnitByPath(javaProject, filePath);
        IType primaryType = compilationUnit.findPrimaryType();
        String qualifiedName = primaryType.getFullyQualifiedName();
        Map<String, List<String>> classes = Collections.singletonMap(qualifiedName, null);
        return suiteUtil.writeSuite(System.getProperty("java.io.tmpdir"), javaProject.getElementName(), classes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "testng";
    }

    @Override
    protected boolean isTestMethod(IMethod method, ICompilationUnit compilationUnit) {
        try {
            IAnnotation[] annotations = method.getAnnotations();
            IAnnotation test = null;
            for (IAnnotation annotation : annotations) {
                if (annotation.getElementName().equals("Test")) {
                    test = annotation;
                    break;
                }
                if (annotation.getElementName().equals(TEST_ANNOTATION_FQN)) {
                    return true;
                }
            }

            if (test == null) {
                return false;
            }

            IImportDeclaration[] imports = compilationUnit.getImports();
            for (IImportDeclaration importDeclaration : imports) {
                if (importDeclaration.getElementName().equals(TEST_ANNOTATION_FQN)) {
                    return true;
                }
            }

            for (IImportDeclaration importDeclaration : imports) {
                if (importDeclaration.isOnDemand()) {
                    String elementName = importDeclaration.getElementName();
                    elementName = elementName.substring(0, elementName.length() - 3); //remove .*
                    if (TEST_ANNOTATION_FQN.startsWith(elementName)) {
                        return true;
                    }

                }
            }

            return false;
        } catch (JavaModelException e) {
            LOG.info("Can't read method annotations.", e);
            return false;
        }
    }
}
