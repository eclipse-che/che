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
package org.eclipse.che.junit.junit4.listeners;

import org.eclipse.che.junit.TestingMessageHelper;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.io.PrintStream;

/**
 * Listener for whole life cycle of the JUnit test run.
 */
public class CheJUnitTestListener {
    private final PrintStream out;

    public CheJUnitTestListener() {
        this.out = System.out;
        TestingMessageHelper.reporterAttached(out);
    }

    /**
     * Called before any tests have been run.
     *
     * @param description
     *         describes the tests to be run
     */
    public void testRunStarted(Description description) {
        TestingMessageHelper.rootPresentation(out, description.getDisplayName(), description.getClassName());
    }

    /**
     * Called when an atomic test is about to be started.
     *
     * @param description
     *         the description of the test that is about to be run
     *         (generally a class and method name)
     */
    public void testStarted(Description description) {
        TestingMessageHelper.testStarted(out, description.getMethodName());
    }

    /**
     * Called when an atomic test has finished, whether the test succeeds or fails.
     *
     * @param description
     *         the description of the test that just ran
     */
    public void testFinished(Description description) {
        TestingMessageHelper.testFinished(out, description.getMethodName());
    }

    /**
     * Called when an atomic test fails.
     *
     * @param failure
     *         describes the test that failed and the exception that was thrown
     */
    public void testFailure(Failure failure) {
        TestingMessageHelper.testFailed(out, failure);
    }

    /**
     * Called when all tests have finished
     *
     * @param result
     *         the summary of the test run, including all the tests that failed
     */
    public void testRunFinished(Result result) {
        TestingMessageHelper.testRunFinished(out, result);
    }

    /**
     * Called when a test will not be run, generally because a test method is annotated
     * with {@link org.junit.Ignore}.
     *
     * @param description
     *         describes the test that will not be run
     */
    public void testIgnored(Description description) {
        TestingMessageHelper.testIgnored(out, description.getMethodName());
    }

}
