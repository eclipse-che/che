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
package org.eclipse.che.plugin.java.testing;

import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Abstract java test runner.
 * Can recognize test methods, find java project and compilation unit by path.
 */
public abstract class AbstractJavaTestRunner implements TestRunner {
    private static final Logger LOG                = LoggerFactory.getLogger(AbstractJavaTestRunner.class);
    private static final String TEST_OUTPUT_FOLDER = "/test-output";

    protected static final String JAVA_EXECUTABLE = "java";

    private int debugPort = -1;
    private String workspacePath;

    public AbstractJavaTestRunner(String workspacePath) {
        this.workspacePath = workspacePath;
    }

    @Override
    public List<TestPosition> detectTests(TestDetectionContext context) {
        IJavaProject javaProject = getJavaProject(context.getProjectPath());
        if (!javaProject.exists()) {
            return Collections.emptyList();
        }

        List<TestPosition> result = new ArrayList<>();
        try {
            ICompilationUnit compilationUnit = findCompilationUnitByPath(javaProject, context.getFilePath());
            if (context.getOffset() == -1) {
                addAllTestsMethod(result, compilationUnit);
            } else {
                IJavaElement element = compilationUnit.getElementAt(context.getOffset());
                if ((element.getElementType() == IJavaElement.METHOD)) {
                    if (isTestMethod((IMethod)element, compilationUnit)) {
                        result.add(createTestPosition((IMethod)element));
                    }
                } else {
                    addAllTestsMethod(result, compilationUnit);
                }

            }
        } catch (JavaModelException e) {
            LOG.debug("Can't read all methods.", e);
        }

        return result;
    }

    private void addAllTestsMethod(List<TestPosition> result, ICompilationUnit compilationUnit) throws JavaModelException {
        for (IType type : compilationUnit.getAllTypes()) {
            for (IMethod method : type.getMethods()) {
                if (isTestMethod(method, compilationUnit)) {
                    result.add(createTestPosition(method));
                }
            }
        }
    }

    private TestPosition createTestPosition(IMethod method) throws JavaModelException {
        ISourceRange nameRange = method.getNameRange();
        ISourceRange sourceRange = method.getSourceRange();

        return DtoFactory.newDto(TestPosition.class).withFrameworkName(getName())
                         .withTestName(method.getElementName())
                         .withTestNameStartOffset(nameRange.getOffset())
                         .withTestNameLength(nameRange.getLength())
                         .withTestBodyLength(sourceRange.getLength());
    }

    protected abstract boolean isTestMethod(IMethod method, ICompilationUnit compilationUnit);

    protected IJavaProject getJavaProject(String projectPath) {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath);
        return JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(project);
    }

    protected String getOutputDirectory(IJavaProject javaProject) {
        String path = workspacePath + javaProject.getPath() + TEST_OUTPUT_FOLDER;
        try {
            IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(true);
            for (IClasspathEntry iClasspathEntry : resolvedClasspath) {
                if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    IPath outputLocation = iClasspathEntry.getOutputLocation();
                    if (outputLocation == null) {
                        continue;
                    }
                    return workspacePath + outputLocation.removeLastSegments(1).append(TEST_OUTPUT_FOLDER);
                }
            }
        } catch (JavaModelException e) {
            return path;
        }
        return path;
    }

    protected ICompilationUnit findCompilationUnitByPath(IJavaProject javaProject, String filePath) {
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
                throw getRuntimeException(filePath);
            }

            String packagePath = packageRootPath.toOSString();
            if (!packagePath.endsWith("/")) {
                packagePath += "/";
            }

            String pathToClass = filePath.substring(packagePath.length());
            IJavaElement element = javaProject.findElement(new Path(pathToClass));
            if (element != null && element.getElementType() == IJavaElement.COMPILATION_UNIT) {
                return (ICompilationUnit)element;
            } else {
                throw getRuntimeException(filePath);
            }
        } catch (JavaModelException e) {
            throw new RuntimeException("Can't find Compilation Unit.", e);
        }
    }

    @Override
    public int getDebugPort() {
        return debugPort;
    }

    protected void generateDebuggerPort() {
        Random random = new Random();
        int port = random.nextInt(65535);
        if (isPortAvailable(port)) {
            debugPort = port;
        } else {
            generateDebuggerPort();
        }
    }

    private static boolean isPortAvailable(int port) {
        try (Socket ignored = new Socket("localhost", port)) {
            return false;
        } catch (IOException ignored) {
            return true;
        }
    }

    private RuntimeException getRuntimeException(String filePath) {
        return new RuntimeException("Can't find IClasspathEntry for path " + filePath);
    }
}
