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
package org.eclipse.che.ide.ext.java.testing.testng.server;


import org.eclipse.che.dto.server.DtoFactory;

import org.eclipse.che.ide.ext.java.testing.core.server.classpath.TestClasspathProvider;
import org.eclipse.che.ide.ext.java.testing.core.server.framework.TestRunner;
import org.eclipse.che.ide.ext.java.testing.core.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.core.shared.TestResult;
import org.eclipse.core.resources.ResourcesPlugin;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TestNG implementation for the test runner service.
 *
 * <pre>
 * Available Parameters for {@link TestNGRunner#execute(Map, TestClasspathProvider)}
 *
 * <em>absoluteProjectPath</em> : Absolute path to the project directory
 * <em>updateClasspath</em> : A boolean indicating whether rebuilding of class path is required.
 * <em>runClass</em> : A boolean indicating whether the test runner should execute all, TestNG XML suite test cases or
 *            a test class indicated by <em>fqn</em> parameter.
 * <em>fqn</em> : Fully qualified class name of the test class if the <em>runClass</em> is true.
 * <em>testngXML</em> : Relative path to the testng.xml file. If this parameter is set, the TestNG test runner will
 *             execute given testng.xml test suite, otherwise all the test classes are get executed.
 *             (Note: If the <em>runClass</em> parameter is true then <em>testngXML</em> parameter gets ignored.)
 *
 * </pre>
 * @author Mirage Abeysekara
 */
public class TestNGRunner implements TestRunner {

    String projectPath;
    ClassLoader projectClassLoader;


    private TestResult run(String testClass) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class<?> clsTest = Class.forName(testClass, true, classLoader);
        return runTestClasses(clsTest);

    }

    private TestResult runAll() throws Exception {
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
            if (isTestable(clazz)) {
                testableClasses.add(clazz);
            }
        }

        return runTestClasses(testableClasses.toArray(new Class[testableClasses.size()]));

    }


    private boolean isTestable(Class<?> clazz) throws ClassNotFoundException {
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().getName().equals("org.testng.annotations.Test")) {
                    return true;
                }
            }
        }
        return false;
    }


    private TestResult runTestClasses(Class<?>... classes) throws Exception {

        ClassLoader classLoader = projectClassLoader;
        Class<?> clsTestNG = Class.forName("org.testng.TestNG", true, classLoader);
        Class<?> clsTestListner = Class.forName("org.testng.TestListenerAdapter", true, classLoader);
        Class<?> clsITestListner = Class.forName("org.testng.ITestListener", true, classLoader);
        Class<?> clsResult = Class.forName("org.testng.ITestResult", true, classLoader);
        Class<?> clsIClass = Class.forName("org.testng.IClass", true, classLoader);
        Class<?> clsThrowable = Class.forName("java.lang.Throwable", true, classLoader);
        Class<?> clsStackTraceElement = Class.forName("java.lang.StackTraceElement", true, classLoader);
        Object testNG = clsTestNG.newInstance();
        Object testListner = clsTestListner.newInstance();
        clsTestNG.getMethod("addListener", clsITestListner).invoke(testNG, testListner);
        clsTestNG.getMethod("setTestClasses", Class[].class).invoke(testNG, new Object[]{classes});
        clsTestNG.getMethod("setOutputDirectory", String.class).invoke(testNG, Paths.get(projectPath,"target","testng-out").toString());
        clsTestNG.getMethod("run").invoke(testNG);

        List failures = (List) clsTestListner.getMethod("getFailedTests").invoke(testListner);
        TestResult dtoResult = DtoFactory.getInstance().createDto(TestResult.class);
        boolean isSuccess = (failures.size() == 0);
        List<Failure> testNGFailures = new ArrayList<>();

        for (Object failure : failures) {
            Failure dtoFailure = DtoFactory.getInstance().createDto(Failure.class);
            Object throwable = clsResult.getMethod("getThrowable").invoke(failure);
            String message = (String) clsThrowable.getMethod("getMessage").invoke(throwable);
            Object failingClass = clsResult.getMethod("getTestClass").invoke(failure);
            String failClassName = (String) clsIClass.getMethod("getName").invoke(failingClass);
            Object stackTrace = clsThrowable.getMethod("getStackTrace").invoke(throwable);
            String failMethod = "";
            Integer failLine = null;

            if (stackTrace.getClass().isArray()) {
                int length = Array.getLength(stackTrace);
                for (int i = 0; i < length; i++) {
                    Object arrayElement = Array.get(stackTrace, i);
                    String failClass = (String) clsStackTraceElement.getMethod("getClassName").invoke(arrayElement);
                    if (failClass.equals(failClassName)) {
                        failMethod = (String) clsStackTraceElement.getMethod("getMethodName").invoke(arrayElement);
                        failLine = (Integer) clsStackTraceElement.getMethod("getLineNumber").invoke(arrayElement);
                        break;
                    }
                }
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            clsThrowable.getMethod("printStackTrace", PrintWriter.class).invoke(throwable, pw);
            String trace = sw.toString();
            dtoFailure.setFailingClass(failClassName);
            dtoFailure.setFailingMethod(failMethod);
            dtoFailure.setFailingLine(failLine);
            dtoFailure.setMessage(message);
            dtoFailure.setTrace(trace);
            testNGFailures.add(dtoFailure);
        }

        dtoResult.setTestFramework("TestNG");
        dtoResult.setSuccess(isSuccess);
        dtoResult.setFailureCount(testNGFailures.size());
        dtoResult.setFailures(testNGFailures);
        return dtoResult;
    }


    private TestResult runTestXML(String xmlPath) throws Exception {


        ClassLoader classLoader = projectClassLoader;
        Class<?> clsTestNG = Class.forName("org.testng.TestNG", true, classLoader);
        Class<?> clsTestListner = Class.forName("org.testng.TestListenerAdapter", true, classLoader);
        Class<?> clsITestListner = Class.forName("org.testng.ITestListener", true, classLoader);
        Class<?> clsResult = Class.forName("org.testng.ITestResult", true, classLoader);
        Class<?> clsIClass = Class.forName("org.testng.IClass", true, classLoader);
        Class<?> clsThrowable = Class.forName("java.lang.Throwable", true, classLoader);
        Class<?> clsStackTraceElement = Class.forName("java.lang.StackTraceElement", true, classLoader);
        Object testNG = clsTestNG.newInstance();
        Object testListner = clsTestListner.newInstance();
        clsTestNG.getMethod("addListener", clsITestListner).invoke(testNG, testListner);
        List<String> testSuites = new ArrayList<>();
        testSuites.add(xmlPath);
        clsTestNG.getMethod("setTestSuites", List.class).invoke(testNG, testSuites);
        clsTestNG.getMethod("setOutputDirectory", String.class).invoke(testNG, Paths.get(projectPath,"target","testng-out").toString());
        clsTestNG.getMethod("run").invoke(testNG);
        List failures = (List) clsTestListner.getMethod("getFailedTests").invoke(testListner);
        TestResult dtoResult = DtoFactory.getInstance().createDto(TestResult.class);
        boolean isSuccess = (failures.size() == 0);
        List<Failure> testNGFailures = new ArrayList<>();

        for (Object failure : failures) {

            Failure dtoFailure = DtoFactory.getInstance().createDto(Failure.class);
            Object throwable = clsResult.getMethod("getThrowable").invoke(failure);
            String message = (String) clsThrowable.getMethod("getMessage").invoke(throwable);
            Object failingClass = clsResult.getMethod("getTestClass").invoke(failure);
            String failClassName = (String) clsIClass.getMethod("getName").invoke(failingClass);
            Object stackTrace = clsThrowable.getMethod("getStackTrace").invoke(throwable);
            String failMethod = "";
            Integer failLine = null;

            if (stackTrace.getClass().isArray()) {
                int length = Array.getLength(stackTrace);
                for (int i = 0; i < length; i++) {
                    Object arrayElement = Array.get(stackTrace, i);
                    String failClass = (String) clsStackTraceElement.getMethod("getClassName").invoke(arrayElement);
                    if (failClass.equals(failClassName)) {
                        failMethod = (String) clsStackTraceElement.getMethod("getMethodName").invoke(arrayElement);
                        failLine = (Integer) clsStackTraceElement.getMethod("getLineNumber").invoke(arrayElement);
                        break;
                    }
                }
            }
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            clsThrowable.getMethod("printStackTrace", PrintWriter.class).invoke(throwable, pw);
            String trace = sw.toString();
            dtoFailure.setFailingClass(failClassName);
            dtoFailure.setFailingMethod(failMethod);
            dtoFailure.setFailingLine(failLine);
            dtoFailure.setMessage(message);
            dtoFailure.setTrace(trace);
            testNGFailures.add(dtoFailure);
        }

        dtoResult.setTestFramework("TestNG");
        dtoResult.setSuccess(isSuccess);
        dtoResult.setFailureCount(testNGFailures.size());
        dtoResult.setFailures(testNGFailures);
        return dtoResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResult execute(Map<String, String> testParameters,
                              TestClasspathProvider classpathProvider) throws Exception {

        projectPath = testParameters.get("absoluteProjectPath");
        String xmlPath = testParameters.get("testngXML");
        boolean updateClasspath = Boolean.valueOf(testParameters.get("updateClasspath"));
        boolean runClass = Boolean.valueOf(testParameters.get("runClass"));
        projectClassLoader = classpathProvider.getClassLoader(projectPath, updateClasspath);
        TestResult testResult;

        if (runClass) {
            String fqn = testParameters.get("fqn");
            testResult = run(fqn);
        } else {
            if (xmlPath == null) {
                testResult = runAll();
            } else {
                testResult = runTestXML(ResourcesPlugin.getPathToWorkspace() + xmlPath);
            }
        }
        return testResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "testng";
    }
}
