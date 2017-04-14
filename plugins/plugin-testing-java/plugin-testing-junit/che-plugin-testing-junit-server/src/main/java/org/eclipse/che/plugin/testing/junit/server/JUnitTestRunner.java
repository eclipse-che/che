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

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.server.listener.AbstractTestListener;
import org.eclipse.che.api.testing.server.listener.OutputTestListener;
import org.eclipse.che.api.testing.shared.TestCase;
import org.eclipse.che.api.testing.shared.TestResult;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.testing.classpath.server.TestClasspathProvider;
import org.eclipse.che.plugin.testing.classpath.server.TestClasspathRegistry;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

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
 * @author David Festal
 */
public class JUnitTestRunner implements TestRunner {

    private static final String   JUNIT4X_RUNNER_CLASS = "org.junit.runner.JUnitCore";
    private static final String   JUNIT3X_RUNNER_CLASS = "junit.textui.TestRunner";
    private ClassLoader           projectClassLoader;
    private ProjectManager        projectManager;
    private TestClasspathRegistry classpathRegistry;

    @Inject
    public JUnitTestRunner(ProjectManager projectManager,
                           TestClasspathRegistry classpathRegistry) {
        this.projectManager = projectManager;
        this.classpathRegistry = classpathRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResult execute(Map<String, String> testParameters) throws Exception {
        String projectAbsolutePath = testParameters.get("absoluteProjectPath");
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

        boolean isJUnit4Compatible = false;
        boolean isJUnit3Compatible = false;

        try {
            Class.forName(JUNIT4X_RUNNER_CLASS, true, projectClassLoader);
            isJUnit4Compatible = true;
        } catch (Exception ignored) {
        }

        try {
            Class.forName(JUNIT3X_RUNNER_CLASS, true, projectClassLoader);
            isJUnit3Compatible = true;
        } catch (Exception ignored) {
        }

        boolean useJUnitV3API = false;
        if (!isJUnit4Compatible) {
            if (!isJUnit3Compatible) {
                throw new ClassNotFoundException("JUnit classes not found in the following project classpath: "
                                                 + Arrays.asList(providedClassLoader.getURLs()));
            } else {
                useJUnitV3API = true;
            }
        }

        String currentWorkingDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", projectAbsolutePath);
            TestResult testResult;
            if (runClass) {
                String fqn = testParameters.get("fqn");
                testResult = useJUnitV3API ? run3x(fqn) : run4x(fqn);
            } else {
                testResult = useJUnitV3API ? runAll3x(projectAbsolutePath) : runAll4x(projectAbsolutePath);
            }
            testResult.setProjectPath(projectPath);
            return testResult;
        } finally {
            System.setProperty("user.dir", currentWorkingDir);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "junit";
    }

    private TestResult run4x(String testClass) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class< ? > clsTest = Class.forName(testClass, true, classLoader);
        return run4xTestClasses(clsTest);
    }

    private TestResult runAll4x(String projectAbsolutePath) throws Exception {
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
            if (isTestable4x(clazz)) {
                testableClasses.add(clazz);
            }
        }
        return run4xTestClasses(testableClasses.toArray(new Class[testableClasses.size()]));
    }

    private boolean isTestable4x(Class< ? > clazz) throws ClassNotFoundException {
        for (Method method : clazz.getDeclaredMethods()) {
            for (Annotation annotation : method.getAnnotations()) {
                if (annotation.annotationType().getName().equals("org.junit.Test")) {
                    return true;
                }
            }
        }
        return false;
    }

    private Object create4xTestListener(ClassLoader loader,
                                        Class< ? > listenerClass,
                                        List<Object> allTests,
                                        AbstractTestListener delegate) throws Exception {
        ProxyFactory f = new ProxyFactory();
        f.setSuperclass(listenerClass);
        f.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method m) {
                String methodName = m.getName();
                switch (methodName) {
                    case "testStarted":
                    case "testFinished":
                    case "testFailure":
                    case "testAssumptionFailure":
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
                Object description = null;
                Throwable throwable = null;

                switch (methodName) {
                    case "testStarted":
                    case "testFinished":
                        description = args[0];
                        throwable = null;
                        break;
                    case "testFailure":
                    case "testAssumptionFailure":
                        description = args[0].getClass().getMethod("getDescription", new Class< ? >[0]).invoke(args[0]);
                        throwable = (Throwable)args[0].getClass().getMethod("getException", new Class< ? >[0]).invoke(args[0]);
                        break;
                    default:
                        return null;
                }

                String testKey = (String)description.getClass().getMethod("getDisplayName", new Class< ? >[0]).invoke(description);
                String testName = testKey;
                switch (methodName) {
                    case "testStarted":
                        delegate.startTest(testKey, testName);
                        allTests.add(description);
                        break;

                    case "testFinished":
                        delegate.endTest(testKey, testName);
                        break;

                    case "testFailure":
                        delegate.addFailure(testKey, throwable);
                        break;

                    case "testAssumptionFailure":
                        delegate.addError(testKey, throwable);
                        break;
                }
                return null;
            }
        };
        Object listener = c.getConstructor().newInstance();
        ((javassist.util.proxy.Proxy)listener).setHandler(mi);
        return listener;
    }

    private TestResult run4xTestClasses(Class< ? >... classes) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class< ? > clsJUnitCore = Class.forName("org.junit.runner.JUnitCore", true, classLoader);
        Class< ? > clsRequest = Class.forName("org.junit.runner.Request", true, classLoader);
        Class< ? > clsRunner = Class.forName("org.junit.runner.Runner", true, classLoader);
        Class< ? > clsResult = Class.forName("org.junit.runner.Result", true, classLoader);
        Class< ? > clsFailure = Class.forName("org.junit.runner.notification.Failure", true, classLoader);
        Class< ? > clsDescription = Class.forName("org.junit.runner.Description", true, classLoader);
        Class< ? > clsThrowable = Class.forName("java.lang.Throwable", true, classLoader);
        Class< ? > clsStackTraceElement = Class.forName("java.lang.StackTraceElement", true, classLoader);
        Class< ? > clsTestRunner = Class.forName("org.junit.runner.notification.RunListener", true, classLoader);
        Object jUnitCore = clsJUnitCore.getConstructor().newInstance();

        Object result;

        List<Object> allRunTests = new ArrayList<Object>();
        try (OutputTestListener outputListener = new OutputTestListener(this.getClass().getName() + ".run4xTestClasses")) {
            Object testListener = create4xTestListener(classLoader, clsTestRunner, allRunTests, outputListener);
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(projectClassLoader);
                clsJUnitCore.getMethod("addListener", clsTestRunner).invoke(jUnitCore, testListener);
                Object request = clsRequest.getMethod("classes", Class[].class).invoke(null, new Object[]{classes});
                Object runner = clsRequest.getMethod("getRunner").invoke(request);
                Object suiteDescription = clsRunner.getMethod("getDescription").invoke(runner);
                result = clsJUnitCore.getMethod("run", clsRequest).invoke(jUnitCore, request);
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
                clsJUnitCore.getMethod("removeListener", clsTestRunner).invoke(jUnitCore, testListener);
            }
        }

        TestResult dtoResult = DtoFactory.getInstance().createDto(TestResult.class);
        boolean isSuccess = (Boolean)clsResult.getMethod("wasSuccessful").invoke(result);

        Map<String, TestCase> testCases = new HashMap<>();

        for (Object testDescription : allRunTests) {
            String testKey = (String)clsDescription.getMethod("getDisplayName").invoke(testDescription);
            TestCase dtoTestCase = DtoFactory.getInstance().createDto(TestCase.class);
            String className = (String)clsDescription.getMethod("getClassName").invoke(testDescription);
            String methodName = (String)clsDescription.getMethod("getMethodName").invoke(testDescription);
            dtoTestCase.setClassName(className);
            dtoTestCase.setMethod(methodName);
            dtoTestCase.setFailingLine(-1);
            dtoTestCase.setMessage("");
            dtoTestCase.setTrace("");
            testCases.put(testKey, dtoTestCase);
        }

        List< ? > failures = (List< ? >)clsResult.getMethod("getFailures").invoke(result);
        for (Object failure : failures) {
            TestCase dtoFailure = DtoFactory.getInstance().createDto(TestCase.class);
            Object description = clsFailure.getMethod("getDescription").invoke(failure);
            String testKey = (String)clsDescription.getMethod("getDisplayName").invoke(description);

            String className = (String)clsDescription.getMethod("getClassName").invoke(description);
            String methodName = (String)clsDescription.getMethod("getMethodName").invoke(description);

            String message = (String)clsFailure.getMethod("getMessage").invoke(failure);
            Object exception = clsFailure.getMethod("getException").invoke(failure);
            Object stackTrace = clsThrowable.getMethod("getStackTrace").invoke(exception);
            Integer failLine = null;
            if (stackTrace.getClass().isArray()) {
                int length = Array.getLength(stackTrace);
                for (int i = 0; i < length; i++) {
                    Object stackElement = Array.get(stackTrace, i);
                    String failClass = (String)clsStackTraceElement.getMethod("getClassName").invoke(stackElement);
                    String failMethod = (String)clsStackTraceElement.getMethod("getMethodName").invoke(stackElement);
                    if (failClass.equals(className) && failMethod.equals(methodName)) {
                        failLine = (Integer)clsStackTraceElement.getMethod("getLineNumber").invoke(stackElement);
                        break;
                    }
                }
            }
            String trace = (String)clsFailure.getMethod("getTrace").invoke(failure);
            dtoFailure.setClassName(className);
            dtoFailure.setMethod(methodName);
            dtoFailure.setFailed(true);
            dtoFailure.setFailingLine(failLine == null ? -1 : failLine);
            dtoFailure.setMessage(message);
            dtoFailure.setTrace(trace);
            testCases.put(testKey, dtoFailure);
        }

        dtoResult.setTestFramework("JUnit4x");
        dtoResult.setSuccess(isSuccess);
        dtoResult.setFailureCount(failures.size());
        dtoResult.setTestCaseCount(testCases.size());
        List<TestCase> testList = new ArrayList<>(testCases.size());
        testList.addAll(testCases.values());
        dtoResult.setTestCases(testList);
        return dtoResult;
    }

    private TestResult run3x(String testClass) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class< ? > clsTest = Class.forName(testClass, true, classLoader);
        return run3xTestClasses(clsTest);

    }

    private TestResult runAll3x(String projectAbsolutePath) throws Exception {
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
            if (isTestable3x(clazz)) {
                testableClasses.add(clazz);
            }
        }
        return run3xTestClasses(testableClasses.toArray(new Class[testableClasses.size()]));
    }

    private boolean isTestable3x(Class< ? > clazz) throws ClassNotFoundException {
        Class< ? > superClass = Class.forName("junit.framework.TestCase", true, projectClassLoader);
        return superClass.isAssignableFrom(clazz);
    }

    private Object create3xTestListener(ClassLoader loader,
                                        Class< ? > listenerClass,
                                        List<Object> allRunTests,
                                        AbstractTestListener delegate) throws Exception {
        ProxyFactory f = new ProxyFactory();
        f.setSuperclass(Object.class);
        f.setInterfaces(new Class< ? >[]{listenerClass});
        f.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method m) {
                String methodName = m.getName();
                switch (methodName) {
                    case "startTest":
                    case "endTest":
                    case "addError":
                    case "addFailure":
                        return true;
                }
                return false;
            }
        });
        Class< ? > c = f.createClass();
        MethodHandler mi = new MethodHandler() {
            @Override
            public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
                Object testCaseObject = args[0];
                allRunTests.add(testCaseObject);
                Class< ? > testCaseClass = testCaseObject.getClass();
                String testClassName = testCaseClass.getName();
                String testMethodName = (String)testCaseClass.getMethod("getName").invoke(testCaseObject);

                String testKey = testMethodName + "(" + testClassName + ")";
                String testName = testKey;
                String methodName = method.getName();
                switch (methodName) {
                    case "startTest":
                        delegate.startTest(testKey, testName);
                        break;

                    case "endTest":
                        delegate.endTest(testKey, testName);
                        break;

                    case "addError":
                        delegate.addError(testKey, (Throwable)args[1]);
                        break;

                    case "addFailure":
                        delegate.addFailure(testKey, (Throwable)args[1]);
                        break;
                }
                return null;
            }
        };
        Object listener = c.getConstructor().newInstance();
        ((javassist.util.proxy.Proxy)listener).setHandler(mi);
        return listener;
    }

    private TestResult run3xTestClasses(Class< ? >... classes) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class< ? > clsTestSuite = Class.forName("junit.framework.TestSuite", true, classLoader);
        Class< ? > clsTestCase = Class.forName("junit.framework.TestCase", true, classLoader);
        Class< ? > clsTestResult = Class.forName("junit.framework.TestResult", true, classLoader);
        Class< ? > clsThrowable = Class.forName("java.lang.Throwable", true, classLoader);
        Class< ? > clsStackTraceElement = Class.forName("java.lang.StackTraceElement", true, classLoader);
        Class< ? > clsFailure = Class.forName("junit.framework.TestFailure", true, classLoader);
        Object testSuite = clsTestSuite.getConstructor().newInstance();
        Object testResult = clsTestResult.getConstructor().newInstance();
        Class< ? > clsTestListener = Class.forName("junit.framework.TestListener", true, classLoader);

        List<Object> allRunTests = new ArrayList<>();

        try (OutputTestListener outputListener = new OutputTestListener(this.getClass().getName() + ".run3xTestClasses")) {
            Object testListener = create3xTestListener(classLoader, clsTestListener, allRunTests, outputListener);
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(projectClassLoader);
                clsTestResult.getMethod("addListener", clsTestListener).invoke(
                                                                               testResult, testListener);
                for (Class< ? > testClass : classes) {
                    clsTestSuite.getMethod("addTestSuite", Class.class).invoke(testSuite, testClass);
                }
                clsTestSuite.getMethod("run", clsTestResult).invoke(testSuite, testResult);
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
                clsTestResult.getMethod("removeListener", clsTestListener).invoke(
                                                                                  testResult, testListener);
            }
        }

        boolean isSuccess = (Boolean)clsTestResult.getMethod("wasSuccessful").invoke(testResult);

        Map<String, TestCase> testCases = new HashMap<>();

        TestResult dtoResult = DtoFactory.getInstance().createDto(TestResult.class);
        Enumeration< ? > failures = (Enumeration< ? >)clsTestResult.getMethod("failures").invoke(testResult);

        for (Object testDescription : allRunTests) {
            String className = testDescription.getClass().getName();
            String methodName = (String)clsTestCase.getMethod("getName").invoke(testDescription);
            String testKey = methodName + "(" + className + ")";
            TestCase dtoTestCase = DtoFactory.getInstance().createDto(TestCase.class);
            dtoTestCase.setClassName(className);
            dtoTestCase.setMethod(methodName);
            dtoTestCase.setFailingLine(-1);
            dtoTestCase.setMessage("");
            dtoTestCase.setTrace("");
            testCases.put(testKey, dtoTestCase);
        }

        int failureCount = 0;
        while (failures.hasMoreElements()) {
            failureCount++;
            TestCase dtoTestCase = DtoFactory.getInstance().createDto(TestCase.class);
            Object failure = failures.nextElement();
            Object failClassObject = clsFailure.getMethod("failedTest").invoke(failure);
            String className = failClassObject.getClass().getName();
            String methodName = (String)clsTestCase.getMethod("getName").invoke(failClassObject);
            String testFailureKey = methodName + "(" + className + ")";

            String message = (String)clsFailure.getMethod("exceptionMessage").invoke(failure);
            String trace = (String)clsFailure.getMethod("trace").invoke(failure);
            Object exception = clsFailure.getMethod("thrownException").invoke(failure);
            Object stackTrace = clsThrowable.getMethod("getStackTrace").invoke(exception);
            Integer failLine = null;
            if (stackTrace.getClass().isArray()) {
                int length = Array.getLength(stackTrace);
                for (int i = 0; i < length; i++) {
                    Object arrayElement = Array.get(stackTrace, i);
                    String failClass = (String)clsStackTraceElement.getMethod("getClassName").invoke(arrayElement);
                    String failMethod = (String)clsStackTraceElement.getMethod("getMethodName").invoke(arrayElement);
                    if (failClass.equals(className) && failMethod.equals(methodName)) {
                        failLine = (Integer)clsStackTraceElement.getMethod("getLineNumber").invoke(arrayElement);
                        break;
                    }
                }
            }
            dtoTestCase.setClassName(className);
            dtoTestCase.setMethod(methodName);
            dtoTestCase.setFailed(true);
            dtoTestCase.setFailingLine(failLine == null ? -1 : failLine);
            dtoTestCase.setMessage(message);
            dtoTestCase.setTrace(trace);
            testCases.put(testFailureKey, dtoTestCase);
        }

        dtoResult.setTestFramework("JUnit3x");
        dtoResult.setSuccess(isSuccess);
        dtoResult.setFailureCount(failureCount);
        dtoResult.setTestCaseCount(testCases.size());
        List<TestCase> testList = new ArrayList<>(testCases.size());
        testList.addAll(testCases.values());
        dtoResult.setTestCases(testList);
        return dtoResult;
    }
}
