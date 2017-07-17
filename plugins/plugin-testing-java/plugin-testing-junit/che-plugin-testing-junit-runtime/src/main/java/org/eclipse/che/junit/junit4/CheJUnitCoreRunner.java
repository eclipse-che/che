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

import org.eclipse.che.junit.TestingMessageHelper;
import org.eclipse.che.junit.junit4.listeners.CheJUnitTestListener;
import org.eclipse.che.junit.junit4.listeners.JUnitExecutionListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;

/**
 * Custom JUnit4 runner that reports results visa {@link CheJUnitTestListener}.
 */
public class CheJUnitCoreRunner extends JUnitCore {
    private CheJUnitTestListener cheJUnitTestListener;

    /**
     * Create a <code>request</code> where all tests are described and run it.
     *
     * @param suites
     *         the name of the test classes to be executed.
     *         If array has one element - it is an information about test method to be executed
     *         (for example full.qualified.ClassName#methodName)
     */
    public void run(String[] suites) {
        Request request = TestRunnerUtil.buildRequest(suites);
        if (request == null) {
            TestingMessageHelper.reporterAttached(System.out);
            System.err.print("No test found to run.");
        } else {
            Runner runner = request.getRunner();
            Description description = runner.getDescription();

            if (cheJUnitTestListener != null) {
                cheJUnitTestListener.suiteTreeStarted(description);
                cheJUnitTestListener.suiteSendTree(description);
                cheJUnitTestListener.suiteTreeEnded(description);
            }

            super.run(request);
        }
    }

    /**
     * Creates custom listener {@link CheJUnitTestListener} and adds it to
     */
    public void createListener() {
        cheJUnitTestListener = new CheJUnitTestListener();
        this.addListener(new JUnitExecutionListener(cheJUnitTestListener));
    }
}
