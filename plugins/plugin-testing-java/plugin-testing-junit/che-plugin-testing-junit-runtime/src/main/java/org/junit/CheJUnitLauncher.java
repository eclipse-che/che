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
package org.junit;

import org.junit.listeners.CheJUnitTestListener;
import org.junit.listeners.JUnitExecutionListener;

/**
 * Main JUnit4 test runner.
 */
public class CheJUnitLauncher {
    /**
     * Main method which creates an instance of {@link org.junit.runner.JUnitCore}, adds custom listener and runs all tests.
     *
     * @param args
     *         arrays of tests to be executed
     */
    public static void main(String[] args) {
        CheJUnit jUnitCore = new CheJUnit();
        CheJUnitTestListener cheJUnitTestListener = new CheJUnitTestListener();
        jUnitCore.addListener(new JUnitExecutionListener(cheJUnitTestListener));
        if (args.length == 0) {
            TestingMessageHelper.reporterAttached(System.out);
            System.err.print("No test found to run.");
        } else {
            jUnitCore.run(args);
        }
    }
}
