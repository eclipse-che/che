/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.testing.server.junit;


import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.testing.server.TestRunner;
import org.eclipse.che.ide.ext.java.testing.shared.Failure;
import org.eclipse.che.ide.ext.java.testing.shared.TestResult;


import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class JUnitTestRunner extends TestRunner {

    public JUnitTestRunner(String projectPath) throws Exception {
        super(projectPath,null);
    }

    @Override
    public TestResult run(String testClass) throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
//        URLClassLoader classLoader = new URLClassLoader(classUrls.toArray(new URL[classUrls.size()]), null);
//        Class<?> clsTest = Class.forName(testClass, true, classLoader);
//        Class<?> clsJUnitCore = Class.forName("org.junit.runner.JUnitCore", true, classLoader);
//        Class<?> clsResult = Class.forName("org.junit.runner.Result", true, classLoader);
//        Class<?> clsFailure = Class.forName("org.junit.runner.notification.Failure", true, classLoader);

        TestResult dtoResult = DtoFactory.getInstance().createDto(TestResult.class);

//        Object result = clsJUnitCore.getMethod("runClasses", Class[].class)
//                .invoke(null, new Object[]{new Class[]{clsTest}});
//
//        boolean isSuccess = (Boolean) clsResult.getMethod("wasSuccessful", null).invoke(result, null);
//        List failures = (List) clsResult.getMethod("getFailures", null).invoke(result, null);
//
//        List<Failure> jUnitFailures = new ArrayList<>();
//        for (Object failure : failures) {
//            Failure dtoFailure = DtoFactory.getInstance().createDto(Failure.class);
//            String message = (String) clsFailure.getMethod("getMessage", null).invoke(failure, null);
//            String trace = (String) clsFailure.getMethod("getTrace", null).invoke(failure, null);
//            dtoFailure.setMessage(message);
//            dtoFailure.setTrace(trace);
//            jUnitFailures.add(dtoFailure);
//        }
//        dtoResult.setTestFramework("JUnit");
//        dtoResult.setSuccess(isSuccess);
//        dtoResult.setFailureCount(jUnitFailures.size());
//        dtoResult.setFailures(jUnitFailures);
        return dtoResult;
    }

    @Override
    public TestResult runAll() throws Exception {
        return null;
    }
}
