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
package org.eclipse.che.junit.junit4;

import org.junit.runner.Request;

import java.util.LinkedList;
import java.util.List;

/**
 * Utility class for building test executing request.
 */
public class TestRunnerUtil {
    /**
     * Creates a request which contains all tests to be executed.
     *
     * @param args
     *         array of test classes or test method (if args.length == 1) to be executed
     * @return an abstract description of tests to be run
     */
    public static Request buildRequest(String[] args) {
        if (args.length == 0) {
            return null;
        }
        if (args.length == 1) {
            String suite = args[0];
            int separatorIndex = suite.indexOf('#');
            return separatorIndex == -1 ? getRequestForClass(suite) : getRequestForOneMethod(suite, separatorIndex);
        }

        return getRequestForClasses(args);
    }

    private static Request getRequestForOneMethod(String suite, int separatorIndex) {
        try {
            Class suiteClass = Class.forName(suite.substring(0, separatorIndex));
            String method = suite.substring(separatorIndex + 1);
            return Request.method(suiteClass, method);
        } catch (ClassNotFoundException e) {
            System.err.print("No test found to run. Args is 0");
            return null;
        }
    }

    private static Request getRequestForClass(String suite) {
        try {
            return Request.aClass(Class.forName(suite));
        } catch (ClassNotFoundException e) {
            System.err.print("No test found to run. Args is 0");
            return null;
        }
    }

    private static Request getRequestForClasses(String[] args) {
        List<Class<?>> suites = new LinkedList<>();
        for (String classFqn : args) {
            try {
                Class<?> aClass = Class.forName(classFqn);
                suites.add(aClass);
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (suites.isEmpty()) {
            return null;
        }
        return Request.classes(suites.toArray(new Class[suites.size()]));
    }
}
