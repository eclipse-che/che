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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.inject.Inject;

import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.server.listener.AbstractTestListener;
import org.eclipse.che.api.testing.server.listener.OutputTestListener;
import org.eclipse.che.api.testing.shared.TestCase;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.testing.classpath.server.TestClasspathProvider;
import org.eclipse.che.plugin.testing.classpath.server.TestClasspathRegistry;
import org.eclipse.core.resources.ResourcesPlugin;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

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
 * </pre>
 * 
 * @author Mirage Abeysekara
 */
public class TestNGRunner implements TestRunner {

    private ClassLoader           projectClassLoader;
    private ProjectManager        projectManager;
    private TestClasspathRegistry classpathRegistry;

    @Inject
    public TestNGRunner(ProjectManager projectManager, TestClasspathRegistry classpathRegistry) {
        this.projectManager = projectManager;
        this.classpathRegistry = classpathRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResult execute(Map<String, String> testParameters) throws Exception {
        String projectAbsolutePath = testParameters.get("absoluteProjectPath");
        String xmlPath = testParameters.get("testngXML");
        boolean updateClasspath = Boolean.valueOf(testParameters.get("updateClasspath"));
        boolean runClass = Boolean.valueOf(testParameters.get("runClass"));
        String projectPath = testParameters.get("projectPath");
        String projectType = "";
        if (projectManager != null) {
            projectType = projectManager.getProject(projectPath).getType();
        }

        ClassLoader currentClassLoader = this.getClass().getClassLoader();
        TestClasspathProvider classpathProvider = classpathRegistry.getTestClasspathProvider(projectType);
        URLClassLoader providedClassLoader = (URLClassLoader)classpathProvider.getClassLoader(projectAbsolutePath, projectPath,
                                                                                              updateClasspath);
        projectClassLoader = new URLClassLoader(providedClassLoader.getURLs(), null) {
            @Override
            protected Class< ? > findClass(String name) throws ClassNotFoundException {
                if (name.startsWith("javassist.")) {
                    return currentClassLoader.loadClass(name);
                }
                return super.findClass(name);
            }
        };

        TestResult testResult;
        if (runClass) {
            String fqn = testParameters.get("fqn");
            testResult = run(projectAbsolutePath, fqn);
        } else {
            if (xmlPath == null) {
                testResult = runAll(projectAbsolutePath);
            } else {
                testResult = runTestXML(projectAbsolutePath, ResourcesPlugin.getPathToWorkspace() + xmlPath);
            }
        }
        testResult.setProjectPath(projectPath);
        return testResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "testng";
    }

    private TestResult run(String projectAbsolutePath, String testClass) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class< ? > clsTest = Class.forName(testClass, true, classLoader);
        return runTestClasses(projectAbsolutePath, clsTest);

    }

    private TestResult runAll(String projectAbsolutePath) throws Exception {
        List<String> testClassNames = new ArrayList<>();
        Files.walk(Paths.get(projectAbsolutePath, "target", "test-classes")).forEach(filePath -> {
            if (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().endsWith(".class")) {
                String path = Paths.get(projectAbsolutePath, "target", "test-classes").relativize(filePath).toString();
                String className = path.replace(File.separatorChar, '.');
                className = className.substring(0, className.length() - 6);
                testClassNames.add(className);
            }
        });
        @SuppressWarnings("rawtypes")
        List<Class> testableClasses = new ArrayList<>();
        for (String className : testClassNames) {
            Class< ? > clazz = Class.forName(className, false, projectClassLoader);
            if (isTestable(clazz)) {
                testableClasses.add(clazz);
            }
        }
        return runTestClasses(projectAbsolutePath, testableClasses.toArray(new Class[testableClasses.size()]));

    }

    private boolean isTestable(Class< ? > clazz) throws ClassNotFoundException {
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().getName().equals("org.testng.annotations.Test")) {
                    return true;
                }
            }
        }
        return false;
    }

    private Object createTestListener(ClassLoader loader, Class< ? > listenerClass, AbstractTestListener delegate) throws Exception {
        ProxyFactory f = new ProxyFactory();
        f.setSuperclass(listenerClass);
        f.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method m) {
                String methodName = m.getName();
                switch (methodName) {
                    case "onTestStart":
                    case "onTestSuccess":
                    case "testFailure":
                    case "onTestFailure":
                        return true;
                }
                return false;
            }
        });
        Class< ? > c = f.createClass();
        MethodHandler mi = new MethodHandler() {
            @Override
            public Object invoke(Object self, Method m, Method method, Object[] args) throws Throwable {
                String methodName = m.getName();
                Object testResult = null;
                Throwable throwable = null;

                switch (methodName) {
                    case "onTestStart":
                    case "onTestSuccess":
                        testResult = args[0];
                        throwable = null;
                        break;
                    case "onTestFailure":
                        testResult = args[0];
                        throwable = (Throwable)testResult.getClass().getMethod("getThrowable", new Class< ? >[0]).invoke(args[0]);
                        break;
                }

                Object testClass = testResult.getClass().getMethod("getTestClass").invoke(testResult);
                Object testMethod = testResult.getClass().getMethod("getMethod").invoke(testResult);
                String testClassName = (String)testClass.getClass().getMethod("getName").invoke(testClass);
                String testMethodName = (String)testMethod.getClass().getMethod("getMethodName").invoke(testMethod);

                String testKey = new StringBuilder().append(testMethodName)
                                                    .append('(').append(testClassName).append(')').toString();
                String testName = testKey;
                switch (methodName) {
                    case "onTestStart":
                        delegate.startTest(testKey, testName);
                        break;

                    case "onTestSuccess":
                        delegate.endTest(testKey, testName);
                        break;

                    case "onTestFailure":
                        delegate.addFailure(testKey, throwable);
                        delegate.endTest(testKey, testName);
                        break;

                    default:

                }
                return method.invoke(self, args);
            }
        };
        Object listener = c.getConstructor().newInstance();
        ((javassist.util.proxy.Proxy)listener).setHandler(mi);
        return listener;
    }


    private TestResult runTest(String projectAbsolutePath, BiConsumer<Class< ? >, Object> configure) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class< ? > clsTestNG = Class.forName("org.testng.TestNG", true, classLoader);
        Class< ? > clsTestListner = Class.forName("org.testng.TestListenerAdapter", true, classLoader);
        Class< ? > clsITestListner = Class.forName("org.testng.ITestListener", true, classLoader);
        Class< ? > clsResult = Class.forName("org.testng.ITestResult", true, classLoader);
        Class< ? > clsIClass = Class.forName("org.testng.IClass", true, classLoader);
        Class< ? > clsITestNGMethod = Class.forName("org.testng.ITestNGMethod", true, classLoader);
        Class< ? > clsThrowable = Class.forName("java.lang.Throwable", true, classLoader);
        Class< ? > clsStackTraceElement = Class.forName("java.lang.StackTraceElement", true, classLoader);
        Object testNG = clsTestNG.newInstance();

        Object testListner;
        try (OutputTestListener outputListener = new OutputTestListener(this.getClass().getName() + ".runTest")) {
            testListner = createTestListener(classLoader, clsTestListner, outputListener);
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(projectClassLoader);

                clsTestNG.getMethod("addListener", clsITestListner).invoke(testNG, testListner);

                configure.accept(clsTestNG, testNG);

                clsTestNG.getMethod("setOutputDirectory", String.class).invoke(testNG,
                                                                               Paths.get(projectAbsolutePath, "target", "testng-out")
                                                                                    .toString());
                clsTestNG.getMethod("run").invoke(testNG);
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
            }
        }

        List<Object> allTests = new ArrayList<>();
        for (Object failure : (List< ? >)clsTestListner.getMethod("getFailedTests").invoke(testListner)) {
            allTests.add(failure);
        }
        int failureCount = allTests.size();

        for (Object success : (List< ? >)clsTestListner.getMethod("getPassedTests").invoke(testListner)) {
            allTests.add(success);
        }

        TestResult dtoResult = DtoFactory.getInstance().createDto(TestResult.class);
        boolean isSuccess = (failureCount == 0);
        List<TestCase> testCases = new ArrayList<>();
        for (Object test : allTests) {
            TestCase dtoFailure = DtoFactory.getInstance().createDto(TestCase.class);
            Object testClass = clsResult.getMethod("getTestClass").invoke(test);
            Object testMethod = clsResult.getMethod("getMethod").invoke(test);
            String testClassName = (String)clsIClass.getMethod("getName").invoke(testClass);
            String testMethodName = (String)clsITestNGMethod.getMethod("getMethodName").invoke(testMethod);

            dtoFailure.setClassName(testClassName);
            dtoFailure.setMethod(testMethodName);

            Object throwable = clsResult.getMethod("getThrowable").invoke(test);
            if (throwable != null) {
                String message = (String)clsThrowable.getMethod("getMessage").invoke(throwable);
                Object stackTrace = clsThrowable.getMethod("getStackTrace").invoke(throwable);
                Integer failLine = null;
                if (stackTrace.getClass().isArray()) {
                    int length = Array.getLength(stackTrace);
                    for (int i = 0; i < length; i++) {
                        Object arrayElement = Array.get(stackTrace, i);
                        String failClass = (String)clsStackTraceElement.getMethod("getClassName").invoke(arrayElement);
                        String failMethod = (String)clsStackTraceElement.getMethod("getMethodName").invoke(arrayElement);
                        if (failClass.equals(testClassName) && failMethod.equals(testMethodName)) {
                            failLine = (Integer)clsStackTraceElement.getMethod("getLineNumber").invoke(arrayElement);
                            break;
                        }
                    }
                }
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                clsThrowable.getMethod("printStackTrace", PrintWriter.class).invoke(throwable, pw);
                String trace = sw.toString();
                dtoFailure.setFailingLine(failLine == null ? -1 : failLine);
                dtoFailure.setMessage(message);
                dtoFailure.setTrace(trace);
                dtoFailure.setFailed(true);
            } else {
                dtoFailure.setFailingLine(-1);
                dtoFailure.setFailed(false);
            }
            testCases.add(dtoFailure);
        }

        dtoResult.setTestFramework("TestNG");
        dtoResult.setSuccess(isSuccess);
        dtoResult.setFailureCount(failureCount);
        dtoResult.setTestCaseCount(testCases.size());
        dtoResult.setTestCases(testCases);
        return dtoResult;
    }

    private TestResult runTestXML(String projectAbsolutePath, String xmlPath) throws Exception {
        return runTest(projectAbsolutePath, (clsTestNG, testNG) -> {
            try {
                List<String> testSuites = new ArrayList<>();
                testSuites.add(xmlPath);
                clsTestNG.getMethod("setTestSuites", List.class).invoke(testNG, testSuites);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private TestResult runTestClasses(String projectAbsolutePath, Class< ? >... classes) throws Exception {
        return runTest(projectAbsolutePath, (clsTestNG, testNG) -> {
            try {
                clsTestNG.getMethod("setTestClasses", Class[].class).invoke(testNG, new Object[]{classes});
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public TestResultRootDto runTests(Map<String, String> testParameters) throws Exception {
        // New API - Not supported yet
        return null;
    }

    @Override
    public List<TestResultDto> getTestResults(List<String> testResultsPath) {
        // New API - Not supported yet
        return null;
    }
}
