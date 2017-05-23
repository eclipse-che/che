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
package org.eclipse.che.plugin.testing.ide.model;

/**
 * Lister for Test running events.
 */
public interface TestStateEventsListener {

    /**
     * Called  when testing started, before suites and tests
     *
     * @param testRootState
     */
    void onTestingStarted(TestRootState testRootState);

    /**
     * Called after all tests finished
     *
     * @param testRootState
     */
    void onTestingFinished(TestRootState testRootState);


    /**
     * Called when test suite started
     *
     * @param testState
     */
    void onSuiteStarted(TestState testState);

    /**
     * Called when test suite finished
     *
     * @param testState
     */
    void onSuiteFinished(TestState testState);

    void onSuiteTreeNodeAdded(TestState testState);

    void onSuiteTreeStarted(TestState testState);

    /**
     * @param count
     */
    void onTestsCountInSuite(int count);

    /**
     * Called when test started
     *
     * @param testState
     */
    void onTestStarted(TestState testState);

    /**
     * Called when test finished
     *
     * @param testState
     */
    void onTestFinished(TestState testState);

    /**
     * Called when test failed
     *
     * @param testState
     */
    void onTestFailed(TestState testState);

    /**
     * Called when test ignored
     *
     * @param testState
     */
    void onTestIgnored(TestState testState);

    /**
     * Called when test root state presentation is added
     *
     * @param testRootState
     */
    void onRootPresentationAdded(TestRootState testRootState);
}
