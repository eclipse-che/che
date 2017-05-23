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

import org.eclipse.che.plugin.testing.ide.model.event.TestFailedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestFinishedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestIgnoredEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestOutputEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestStartedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestSuiteFinishedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestSuiteStartedEvent;

/**
 *
 */
public interface TestingEventsProcessor {

    void addListener(TestStateEventsListener listener);

    void onStartTesting();

    void onTestSuiteStarted(TestSuiteStartedEvent event);

    void onTestSuiteFinished(TestSuiteFinishedEvent event);

    void onTestOutput(TestOutputEvent event);

    void onTestStarted(TestStartedEvent event);

    void onTestFailed(TestFailedEvent event);

    void onTestFinished(TestFinishedEvent event);

    void onTestIgnored(TestIgnoredEvent event);

    void onSuiteTreeStarted(String suiteName, String location);

    void onTestCountInSuite(int count);

    void onTestFrameworkAttached();

    void onSuiteTreeEnded(String suiteName);

    void onSuiteTreeNodeAdded(String suiteName, String location);

    void onBuildTreeEnded();

    void onRootPresentationAdded(String rootName, String comment, String location);

    void onUncapturedOutput(String output, Printer.OutputType outputType);

    void onFinishTesting();
}
