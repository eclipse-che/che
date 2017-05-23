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

import java.util.ArrayList;
import java.util.List;

/**
 * Base event processor, converts events form runner to internal form
 */
public abstract class AbstractTestingEventsProcessor implements TestingEventsProcessor {


    private final String testFrameworkName;

    protected final List<TestStateEventsListener> listeners = new ArrayList<>();

    public AbstractTestingEventsProcessor(String testFrameworkName) {
        this.testFrameworkName = testFrameworkName;
    }


    public void addListener(TestStateEventsListener listener) {
        listeners.add(listener);
    }

    protected void callTestingStarted(TestRootState testRootState) {
        listeners.forEach(listener -> listener.onTestingStarted(testRootState));
    }

    protected void callTestFrameworkAttached(TestRootState rootState) {
        rootState.setTestReporterAttached();
    }

    protected void callSuiteStarted(TestState suite) {
        listeners.forEach(listener -> listener.onSuiteStarted(suite));
    }

    protected void callSuiteFinished(TestState suite) {
        listeners.forEach(listener -> listener.onSuiteFinished(suite));
    }

    protected void callTestStarted(TestState testState) {
        listeners.forEach(listener -> listener.onTestStarted(testState));
    }

    protected void callTestFailed(TestState testState) {
        listeners.forEach(listener -> listener.onTestFailed(testState));
    }

    protected void callTestFinished(TestState testState) {
        listeners.forEach(listener -> listener.onTestFinished(testState));
    }

    protected void callTestIgnored(TestState testState) {
        listeners.forEach(listener -> listener.onTestIgnored(testState));
    }

    protected void callTestCountInSuite(int count) {
        listeners.forEach(listener -> listener.onTestsCountInSuite(count));
    }

    protected void callTestingFinished(TestRootState testRootState) {
        listeners.forEach(listener -> listener.onTestingFinished(testRootState));
    }
}
