/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.testing.ide.model;

import org.eclipse.che.plugin.testing.ide.model.event.TestFailedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestFinishedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestIgnoredEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestOutputEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestStartedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestSuiteFinishedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestSuiteStartedEvent;

/** Describes test events processor. */
public interface TestingEventsProcessor {

  /**
   * Adds test state event listener.
   *
   * @param listener new listener
   */
  void addListener(TestStateEventsListener listener);

  /** Notifies the listener that test is starting. */
  void onStartTesting();

  /**
   * Notifies the listener that test suite has started.
   *
   * @param event instance of {@link TestSuiteStartedEvent}
   */
  void onTestSuiteStarted(TestSuiteStartedEvent event);

  /**
   * Notifies the listener that test suite has finished.
   *
   * @param event instance of {@link TestSuiteFinishedEvent}
   */
  void onTestSuiteFinished(TestSuiteFinishedEvent event);

  /**
   * Notifies the listener that test outputs some message.
   *
   * @param event instance of {@link TestOutputEvent}
   */
  void onTestOutput(TestOutputEvent event);

  /**
   * Notifies the listener that test has started.
   *
   * @param event instance of {@link TestStartedEvent}
   */
  void onTestStarted(TestStartedEvent event);

  /**
   * Notifies the listener that test is failed.
   *
   * @param event instance of {@link TestFailedEvent}
   */
  void onTestFailed(TestFailedEvent event);

  /**
   * Notifies the listener that test is finished.
   *
   * @param event instance of {@link TestFinishedEvent}
   */
  void onTestFinished(TestFinishedEvent event);

  /**
   * Notifies the listener that test is ignored.
   *
   * @param event instance of {@link TestIgnoredEvent}
   */
  void onTestIgnored(TestIgnoredEvent event);

  /**
   * Notifies the listener that suite tree is started.
   *
   * @param suiteName name of suite
   * @param location location of suite
   */
  void onSuiteTreeStarted(String suiteName, String location);

  /**
   * Notifies the listener count of tests.
   *
   * @param count count
   */
  void onTestCountInSuite(int count);

  /** Notifies the listener that framework was attached. */
  void onTestFrameworkAttached();

  /**
   * Notifies the listener that suite tree is ended.
   *
   * @param suiteName name of suite
   */
  void onSuiteTreeEnded(String suiteName);

  /**
   * Notifies the listener that suite tree is added.
   *
   * @param suiteName name of suite
   * @param location location of suite
   */
  void onSuiteTreeNodeAdded(String suiteName, String location);

  /** Notifies the listener that build of test tree is ended. */
  void onBuildTreeEnded();

  /**
   * Notifies the listener that test root presentation was added.
   *
   * @param rootName name of root
   * @param comment comment message
   * @param location location of suite
   */
  void onRootPresentationAdded(String rootName, String comment, String location);

  /**
   * Notifies the listener about uncaptured output.
   *
   * @param output message
   * @param outputType type of message
   */
  void onUncapturedOutput(String output, Printer.OutputType outputType);

  /** Notifies the listener that test was finished. */
  void onFinishTesting();
}
