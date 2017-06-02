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

import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.plugin.java.testing.AbstractJavaTestRunner;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * JUnit implementation for the test runner service.
 * <p>
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
public class JUnitTestRunner extends AbstractJavaTestRunner {

    private static final Logger LOG = LoggerFactory.getLogger(JUnitTestRunner.class);

    private static final String JUNIT4X_RUNNER_CLASS = "org.junit.runner.JUnitCore";
    private static final String JUNIT3X_RUNNER_CLASS = "junit.textui.TestRunner";

    private static final String JUNIT_TEST_ANNOTATION = "org.junit.Test";

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

    @Override
    protected boolean isTestMethod(IMethod method, ICompilationUnit compilationUnit) {
        try {
            if (isTest(method, compilationUnit)) {
                return true;
            }

            if (method.isConstructor()) {
                return false;
            }
            int flags = method.getFlags();
            if (!Flags.isPublic(flags)) {
                return false;
            }

            if (Flags.isAbstract(flags)) {
                return false;
            }
            if (Flags.isStatic(flags)) {
                return false;
            }

            if (!method.getElementName().startsWith("test")) { //JUnit 3 case
                return false;
            }
            //TODO add check class hierarchy for JUnit3

            return method.getReturnType().equals("V"); // 'V' is void signature

        } catch (JavaModelException ignored) {
            return false;
        }
    }

    private boolean isTest(IMethod method, ICompilationUnit compilationUnit) {
        try {
            IAnnotation[] annotations = method.getAnnotations();
            IAnnotation test = null;
            for (IAnnotation annotation : annotations) {
                if (annotation.getElementName().equals("Test")) {
                    test = annotation;
                    break;
                }
                if (annotation.getElementName().equals(JUNIT_TEST_ANNOTATION)) {
                    return true;
                }
            }

            if (test == null) {
                return false;
            }

            IImportDeclaration[] imports = compilationUnit.getImports();
            for (IImportDeclaration importDeclaration : imports) {
                if (importDeclaration.getElementName().equals(JUNIT_TEST_ANNOTATION)) {
                    return true;
                }
            }

            for (IImportDeclaration importDeclaration : imports) {
                if (importDeclaration.isOnDemand()) {
                    String elementName = importDeclaration.getElementName();
                    elementName = elementName.substring(0, elementName.length() - 3); //remove .*
                    if (JUNIT_TEST_ANNOTATION.startsWith(elementName)) {
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
