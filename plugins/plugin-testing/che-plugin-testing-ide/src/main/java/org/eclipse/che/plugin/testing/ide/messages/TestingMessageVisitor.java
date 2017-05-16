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
package org.eclipse.che.plugin.testing.ide.messages;

/**
 *
 */
public interface TestingMessageVisitor {


    void visitTestingMessage(ClientTestingMessage message);

    void visitMessageWithStatus(Message message);

    void visitTestSuiteStarted(TestSuiteStarted suiteStarted);

    void visitTestSuiteFinished(TestSuiteFinished suiteFinished);

    void visitTestStdErr(TestStdErr testStdErr);

    void visitTestStarted(TestStarted testStarted);

    void visitTestStdOut(TestStdOut testStdOut);

    void visitTestFailed(TestFailed testFailed);

    void visitTestFinished(TestFinished testFinished);

    void visitTestIgnored(TestIgnored testIgnored);

    void visitSuiteTreeStarted(SuiteTreeStarted suiteTreeStarted);

    void visitTestCount(TestCount testCount);

    void visitTestReporterAttached(TestReporterAttached testReporterAttached);

    void visitSuiteTreeEnded(SuiteTreeEnded suiteTreeEnded);

    void visitSuiteTreeNode(SuiteTreeNode suiteTreeNode);

    void visitBuildTreeEnded(BuildTreeEnded buildTreeEnded);

    void visitRootPresentation(RootPresentationMessage presentationMessage);

    void visitUncapturedOutput(UncapturedOutputMessage uncapturedOutputMessage);
}
