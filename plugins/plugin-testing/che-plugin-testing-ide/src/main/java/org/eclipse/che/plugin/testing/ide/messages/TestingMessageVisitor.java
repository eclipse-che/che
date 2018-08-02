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
package org.eclipse.che.plugin.testing.ide.messages;

/** Visitor of test messages. */
public interface TestingMessageVisitor {

  /** visits message from test method. */
  void visitTestingMessage(ClientTestingMessage message);

  /** visits test message with status. */
  void visitMessageWithStatus(Message message);

  /** visits test message when suite is started. */
  void visitTestSuiteStarted(TestSuiteStarted suiteStarted);

  /** visits test message when suite is finished. */
  void visitTestSuiteFinished(TestSuiteFinished suiteFinished);

  /** visits StdErr test message. */
  void visitTestStdErr(TestStdErr testStdErr);

  /** visits test message when test is started. */
  void visitTestStarted(TestStarted testStarted);

  /** visits StdOut test message. */
  void visitTestStdOut(TestStdOut testStdOut);

  /** visits test message when test is failed. */
  void visitTestFailed(TestFailed testFailed);

  /** visits test message when test is finished. */
  void visitTestFinished(TestFinished testFinished);

  /** visits test message when test is ignored. */
  void visitTestIgnored(TestIgnored testIgnored);

  /** visits test message when suite tree is started. */
  void visitSuiteTreeStarted(SuiteTreeStarted suiteTreeStarted);

  /** visits test message when test count. */
  void visitTestCount(TestCount testCount);

  /** visits test message when test report attached. */
  void visitTestReporterAttached(TestReporterAttached testReporterAttached);

  /** visits test message when suite tree is ended. */
  void visitSuiteTreeEnded(SuiteTreeEnded suiteTreeEnded);

  /** visits suite tree node message. */
  void visitSuiteTreeNode(SuiteTreeNode suiteTreeNode);

  /** visits suite tree node message is ended. */
  void visitBuildTreeEnded(BuildTreeEnded buildTreeEnded);

  /** visits root presentation message. */
  void visitRootPresentation(RootPresentationMessage presentationMessage);

  /** visits uncapture output message. */
  void visitUncapturedOutput(UncapturedOutputMessage uncapturedOutputMessage);
}
