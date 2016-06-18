package org.eclipse.che.ide.ext.java.testing.server.junit4x;

//import org.junit.runner.JUnitCore;
//import org.junit.runner.Result;

import com.google.inject.Inject;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.testing.server.FrameworkFactory;
import org.eclipse.che.ide.ext.java.testing.server.TestRunner;
import org.eclipse.che.ide.ext.java.testing.server.TestRunner2;
import org.eclipse.che.ide.ext.java.testing.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class JUnit4TestRunner implements TestRunner2 {


    String projectPath;
    ClassLoader projectClassLoader;


    private TestResult run(String testClass) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class<?> clsTest = Class.forName(testClass, true, classLoader);
//        Result r = JUnitCore.runClasses(clsTest);
        return runTestClasses(clsTest);

    }

    private TestResult runAll() throws Exception {
        List<String> testClassNames = new ArrayList<>();
        Files.walk(Paths.get(projectPath, "target", "test-classes")).forEach(filePath -> {
            if (Files.isRegularFile(filePath) && filePath.toString().toLowerCase().endsWith(".class")) {
                String path = Paths.get(projectPath, "target", "test-classes").relativize(filePath).toString();
                String className = path.replace('/', '.');
                className = className.replace('\\', '.');
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
                if (annotation.annotationType().getName().equals("org.junit.Test")) {
                    return true;
                }
            }
        }
        return false;
    }


    private TestResult runTestClasses(Class<?>... classes) throws Exception {
        ClassLoader classLoader = projectClassLoader;
        Class<?> clsJUnitCore = Class.forName("org.junit.runner.JUnitCore", true, classLoader);
        Class<?> clsResult = Class.forName("org.junit.runner.Result", true, classLoader);
        Class<?> clsFailure = Class.forName("org.junit.runner.notification.Failure", true, classLoader);
        Class<?> clsDescription = Class.forName("org.junit.runner.Description", true, classLoader);
        Class<?> clsThrowable = Class.forName("java.lang.Throwable", true, classLoader);
        Class<?> clsStackTraceElement = Class.forName("java.lang.StackTraceElement", true, classLoader);

        Object result = clsJUnitCore.getMethod("runClasses", Class[].class).invoke(null, new Object[]{classes});

        TestResult dtoResult = DtoFactory.getInstance().createDto(TestResult.class);


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

        return dtoResult;
    }

    @Override
    public TestResult execute(String path, ClassLoader classLoader) {
        projectPath = path;
        projectClassLoader = classLoader;
        TestResult a = null;
        try {
            a = runAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return a;
    }

    @Override
    public String getName() {
        return "junit";
    }
}
