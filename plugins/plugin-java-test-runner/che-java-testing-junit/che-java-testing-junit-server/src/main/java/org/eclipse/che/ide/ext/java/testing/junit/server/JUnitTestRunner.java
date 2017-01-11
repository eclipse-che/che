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
package org.eclipse.che.ide.ext.java.testing.junit.server;


import org.eclipse.che.dto.server.DtoFactory;

import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathProvider;
import org.eclipse.che.ide.ext.java.testing.core.server.framework.TestRunner;
import org.eclipse.che.ide.ext.java.testing.junit4x.shared.JUnitTestResult;
import org.eclipse.che.ide.ext.java.testing.core.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * JUnit implementation for the test runner service.
 *
 * <pre>
 * Available Parameters for {@link JUnitTestRunner#execute(Map, TestClasspathProvider)}
 *
 * <em>absoluteProjectPath</em> : Absolute path to the project directory
 * <em>updateClasspath</em> : A boolean indicating whether rebuilding of class path is required.
 * <em>runClass</em> : A boolean indicating whether the test runner should execute all the test cases or a test class
 *            indicated by <em>fqn</em> parameter.
 * <em>fqn</em> : Fully qualified class name of the test class if the <em>runClass</em> is true.
 * </pre>
 *
 * @author Mirage Abeysekara
 */
public class JUnitTestRunner implements TestRunner {

    private static final String JUNIT4X_RUNNER_CLASS = "org.junit.runner.JUnitCore";
    private static final String JUNIT3X_RUNNER_CLASS = "junit.textui.TestRunner";
    private String projectPath;
    private ClassLoader projectClassLoader;


    private TestResult run4x(String testClass) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class<?> clsTest = Class.forName(testClass, true, classLoader);
        return run4xTestClasses(clsTest);

    }

    private TestResult runAll4x() throws Exception {

        List<String> testClassNames = new ArrayList<>();
        Files.walk(Paths.get(projectPath, "target", "test-classes")).forEach(filePath -> {
            if (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().endsWith(".class")) {
                String path = Paths.get(projectPath, "target", "test-classes").relativize(filePath).toString();
                String className = path.replace(File.separatorChar, '.');
                className = className.substring(0, className.length() - 6);
                testClassNames.add(className);
            }
        });

        List<Class> testableClasses = new ArrayList<>();
        for (String className : testClassNames) {
            Class<?> clazz = Class.forName(className, false, projectClassLoader);
            if (isTestable4x(clazz)) {
                testableClasses.add(clazz);
            }
        }
        return run4xTestClasses(testableClasses.toArray(new Class[testableClasses.size()]));
    }


    private boolean isTestable4x(Class<?> clazz) throws ClassNotFoundException {
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().getName().equals("org.junit.Test")) {
                    return true;
                }
            }
        }
        return false;
    }


    private TestResult run4xTestClasses(Class<?>... classes) throws Exception {

        ClassLoader classLoader = projectClassLoader;
        Class<?> clsJUnitCore = Class.forName("org.junit.runner.JUnitCore", true, classLoader);
        Class<?> clsResult = Class.forName("org.junit.runner.Result", true, classLoader);
        Class<?> clsFailure = Class.forName("org.junit.runner.notification.Failure", true, classLoader);
        Class<?> clsDescription = Class.forName("org.junit.runner.Description", true, classLoader);
        Class<?> clsThrowable = Class.forName("java.lang.Throwable", true, classLoader);
        Class<?> clsStackTraceElement = Class.forName("java.lang.StackTraceElement", true, classLoader);
        Object result = clsJUnitCore.getMethod("runClasses", Class[].class).invoke(null, new Object[]{classes});
        JUnitTestResult dtoResult = DtoFactory.getInstance().createDto(JUnitTestResult.class);
        boolean isSuccess = (Boolean) clsResult.getMethod("wasSuccessful").invoke(result);
        List failures = (List) clsResult.getMethod("getFailures").invoke(result);
        List<Failure> jUnitFailures = new ArrayList<>();

        for (Object failure : failures) {
            Failure dtoFailure = DtoFactory.getInstance().createDto(Failure.class);

            String message = (String) clsFailure.getMethod("getMessage").invoke(failure);
            Object description = clsFailure.getMethod("getDescription").invoke(failure);
            String failClassName = (String) clsDescription.getMethod("getClassName").invoke(description);
            Object exception = clsFailure.getMethod("getException").invoke(failure);
            Object stackTrace = clsThrowable.getMethod("getStackTrace").invoke(exception);
            String failMethod = "";
            Integer failLine = null;

            if (stackTrace.getClass().isArray()) {
                int length = Array.getLength(stackTrace);
                for (int i = 0; i < length; i++) {
                    Object stackElement = Array.get(stackTrace, i);
                    String failClass = (String) clsStackTraceElement.getMethod("getClassName").invoke(stackElement);
                    if (failClass.equals(failClassName)) {
                        failMethod = (String) clsStackTraceElement.getMethod("getMethodName").invoke(stackElement);
                        failLine = (Integer) clsStackTraceElement.getMethod("getLineNumber").invoke(stackElement);
                        break;
                    }
                }
            }
            String trace = (String) clsFailure.getMethod("getTrace").invoke(failure);
            dtoFailure.setFailingClass(failClassName);
            dtoFailure.setFailingMethod(failMethod);
            dtoFailure.setFailingLine(failLine);
            dtoFailure.setMessage(message);
            dtoFailure.setTrace(trace);
            jUnitFailures.add(dtoFailure);
        }
        dtoResult.setTestFramework("JUnit4x");
        dtoResult.setSuccess(isSuccess);
        dtoResult.setFailureCount(jUnitFailures.size());
        dtoResult.setFailures(jUnitFailures);
        dtoResult.setFrameworkVersion("4.x");
        return dtoResult;
    }





    private TestResult run3x(String testClass) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class<?> clsTest = Class.forName(testClass, true, classLoader);
        return run3xTestClasses(clsTest);

    }

    private TestResult runAll3x() throws Exception {
        List<String> testClassNames = new ArrayList<>();
        Files.walk(Paths.get(projectPath, "target", "test-classes")).forEach(filePath -> {
            if (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().endsWith(".class")) {
                String path = Paths.get(projectPath, "target", "test-classes").relativize(filePath).toString();
                String className = path.replace(File.separatorChar, '.');
                className = className.substring(0, className.length() - 6);
                testClassNames.add(className);
            }
        });

        List<Class> testableClasses = new ArrayList<>();
        for (String className : testClassNames) {
            Class<?> clazz = Class.forName(className, false, projectClassLoader);
            if (isTestable3x(clazz)) {
                testableClasses.add(clazz);
            }
        }

        return run3xTestClasses(testableClasses.toArray(new Class[testableClasses.size()]));

    }


    private boolean isTestable3x(Class<?> clazz) throws ClassNotFoundException {
        Class<?> superClass = Class.forName("junit.framework.TestCase", true, projectClassLoader);
        return superClass.isAssignableFrom(clazz);
    }


    private TestResult run3xTestClasses(Class<?>... classes) throws Exception {


        ClassLoader classLoader = projectClassLoader;
        Class<?> clsTestSuite = Class.forName("junit.framework.TestSuite", true, classLoader);
        Class<?> clsTestResult = Class.forName("junit.framework.TestResult", true, classLoader);
        Class<?> clsThrowable = Class.forName("java.lang.Throwable", true, classLoader);
        Class<?> clsStackTraceElement = Class.forName("java.lang.StackTraceElement", true, classLoader);
        Class<?> clsFailure = Class.forName("junit.framework.TestFailure", true, classLoader);
        Object testSuite = clsTestSuite.newInstance();
        Object testResult = clsTestResult.newInstance();

        for(Class testClass : classes){
            clsTestSuite.getMethod("addTestSuite", Class.class).invoke(testSuite, testClass);
        }

        clsTestSuite.getMethod("run", clsTestResult).invoke(testSuite, testResult);

        JUnitTestResult dtoResult = DtoFactory.getInstance().createDto(JUnitTestResult.class);
        boolean isSuccess = (Boolean) clsTestResult.getMethod("wasSuccessful").invoke(testResult);
        Enumeration failures = (Enumeration) clsTestResult.getMethod("failures").invoke(testResult);
        List<Failure> jUnitFailures = new ArrayList<>();

        while(failures.hasMoreElements()){
            Failure dtoFailure = DtoFactory.getInstance().createDto(Failure.class);
            Object failure =  failures.nextElement();
            String message = (String) clsFailure.getMethod("exceptionMessage").invoke(failure);
            String trace = (String) clsFailure.getMethod("trace").invoke(failure);
            Object failClassObject = clsFailure.getMethod("failedTest").invoke(failure);
            String failClassName = failClassObject.getClass().getName();
            Object exception = clsFailure.getMethod("thrownException").invoke(failure);
            Object stackTrace = clsThrowable.getMethod("getStackTrace").invoke(exception);
            String failMethod = "";
            Integer failLine = null;

            if (stackTrace.getClass().isArray()) {
                int length = Array.getLength(stackTrace);
                for (int i = 0; i < length; i ++) {
                    Object arrayElement = Array.get(stackTrace, i);
                    String failClass = (String) clsStackTraceElement.getMethod("getClassName").invoke(arrayElement);
                    if(failClass.equals(failClassName)) {
                        failMethod = (String) clsStackTraceElement.getMethod("getMethodName").invoke(arrayElement);
                        failLine = (Integer) clsStackTraceElement.getMethod("getLineNumber").invoke(arrayElement);
                        break;
                    }
                }
            }
            dtoFailure.setFailingClass(failClassName);
            dtoFailure.setFailingMethod(failMethod);
            dtoFailure.setFailingLine(failLine);
            dtoFailure.setMessage(message);
            dtoFailure.setTrace(trace);
            jUnitFailures.add(dtoFailure);
        }

        dtoResult.setTestFramework("JUnit3x");
        dtoResult.setSuccess(isSuccess);
        dtoResult.setFailureCount(jUnitFailures.size());
        dtoResult.setFailures(jUnitFailures);
        dtoResult.setFrameworkVersion("3.x");
        return dtoResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResult execute(Map<String, String> testParameters,
                              TestClasspathProvider classpathProvider) throws Exception {

        projectPath = testParameters.get("absoluteProjectPath");
        boolean updateClasspath = Boolean.valueOf(testParameters.get("updateClasspath"));
        boolean runClass = Boolean.valueOf(testParameters.get("runClass"));
        projectClassLoader = classpathProvider.getClassLoader(projectPath, updateClasspath);
        TestResult testResult;

        try {
            Class.forName(JUNIT4X_RUNNER_CLASS, true, projectClassLoader);
            if (runClass) {
                String fqn = testParameters.get("fqn");
                testResult = run4x(fqn);
            } else {
                testResult = runAll4x();
            }
            return testResult;
        } catch (Exception ignored) {
        }

        try {
            Class.forName(JUNIT3X_RUNNER_CLASS, true, projectClassLoader);
            if (runClass) {
                String fqn = testParameters.get("fqn");
                testResult = run3x(fqn);
            } else {
                testResult = runAll3x();
            }
            return testResult;
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "junit";
    }
}
