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
package org.eclipse.che.plugin.testing.ide.handler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.testing.shared.Constants;
import org.eclipse.che.api.testing.shared.messages.TestingMessageNames;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.testing.ide.messages.BuildTreeEnded;
import org.eclipse.che.plugin.testing.ide.messages.ClientTestingMessage;
import org.eclipse.che.plugin.testing.ide.messages.Message;
import org.eclipse.che.plugin.testing.ide.messages.RootPresentationMessage;
import org.eclipse.che.plugin.testing.ide.messages.SuiteTreeEnded;
import org.eclipse.che.plugin.testing.ide.messages.SuiteTreeNode;
import org.eclipse.che.plugin.testing.ide.messages.SuiteTreeStarted;
import org.eclipse.che.plugin.testing.ide.messages.TestCount;
import org.eclipse.che.plugin.testing.ide.messages.TestFailed;
import org.eclipse.che.plugin.testing.ide.messages.TestFinished;
import org.eclipse.che.plugin.testing.ide.messages.TestIgnored;
import org.eclipse.che.plugin.testing.ide.messages.TestReporterAttached;
import org.eclipse.che.plugin.testing.ide.messages.TestStarted;
import org.eclipse.che.plugin.testing.ide.messages.TestStdErr;
import org.eclipse.che.plugin.testing.ide.messages.TestStdOut;
import org.eclipse.che.plugin.testing.ide.messages.TestSuiteFinished;
import org.eclipse.che.plugin.testing.ide.messages.TestSuiteStarted;
import org.eclipse.che.plugin.testing.ide.messages.TestingMessageVisitor;
import org.eclipse.che.plugin.testing.ide.messages.UncapturedOutputMessage;
import org.eclipse.che.plugin.testing.ide.model.TestingEventsProcessor;
import org.eclipse.che.plugin.testing.ide.model.event.TestFailedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestFinishedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestIgnoredEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestOutputEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestStartedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestSuiteFinishedEvent;
import org.eclipse.che.plugin.testing.ide.model.event.TestSuiteStartedEvent;

/**
 * Handler which receives messages from the Testing tools. Pass all messages to {@link
 * TestingEventsProcessor}
 *
 * @author David Festal
 */
@Singleton
public class TestingHandler implements TestingMessageVisitor {

  private TestingEventsProcessor processor;

  @Inject
  public TestingHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(Constants.TESTING_RPC_METHOD_NAME)
        .paramsAsString()
        .noResult()
        .withConsumer(this::handleTestingMessage);
  }

  private void handleTestingMessage(String jsonMessage) {
    ClientTestingMessage message = ClientTestingMessage.parse(jsonMessage);
    if (message != null) {
      message.visit(this);
    }
  }

  public void setProcessor(TestingEventsProcessor processor) {
    this.processor = processor;
  }

  @Override
  public void visitTestingMessage(ClientTestingMessage message) {

    if (TestingMessageNames.TESTING_STARTED.equals(message.getName())) {
      if (processor != null) {
        processor.onStartTesting();
      }
      return;
    }
    if (TestingMessageNames.FINISH_TESTING.equals(message.getName())) {
      if (processor != null) {
        processor.onFinishTesting();
      }
      return;
    }
    Log.error(getClass(), "Unexpected test message: " + message.getName());
  }

  @Override
  public void visitMessageWithStatus(Message message) {}

  @Override
  public void visitTestSuiteStarted(TestSuiteStarted suiteStarted) {
    TestSuiteStartedEvent event = new TestSuiteStartedEvent(suiteStarted);
    if (processor != null) {
      processor.onTestSuiteStarted(event);
    }
  }

  @Override
  public void visitTestSuiteFinished(TestSuiteFinished suiteFinished) {
    TestSuiteFinishedEvent event = new TestSuiteFinishedEvent(suiteFinished);
    if (processor != null) {
      processor.onTestSuiteFinished(event);
    }
  }

  @Override
  public void visitTestStdErr(TestStdErr testStdErr) {
    fireTestOutput(new TestOutputEvent(testStdErr, testStdErr.getErr(), false));
  }

  private void fireTestOutput(TestOutputEvent event) {
    if (processor != null) {
      processor.onTestOutput(event);
    }
  }

  @Override
  public void visitTestStarted(TestStarted testStarted) {
    TestStartedEvent event = new TestStartedEvent(testStarted);
    if (processor != null) {
      processor.onTestStarted(event);
    }
  }

  @Override
  public void visitTestStdOut(TestStdOut testStdOut) {
    fireTestOutput(new TestOutputEvent(testStdOut, testStdOut.getStdOut(), true));
  }

  @Override
  public void visitTestFailed(TestFailed testFailed) {
    TestFailedEvent event = new TestFailedEvent(testFailed);
    if (processor != null) {
      processor.onTestFailed(event);
    }
  }

  @Override
  public void visitTestFinished(TestFinished testFinished) {
    TestFinishedEvent event = new TestFinishedEvent(testFinished);
    if (processor != null) {
      processor.onTestFinished(event);
    }
  }

  @Override
  public void visitTestIgnored(TestIgnored testIgnored) {
    TestIgnoredEvent event = new TestIgnoredEvent(testIgnored);
    if (processor != null) {
      processor.onTestIgnored(event);
    }
  }

  @Override
  public void visitSuiteTreeStarted(SuiteTreeStarted suiteTreeStarted) {
    if (processor != null) {
      processor.onSuiteTreeStarted(suiteTreeStarted.getSuiteName(), suiteTreeStarted.getLocation());
    }
  }

  @Override
  public void visitTestCount(TestCount testCount) {
    if (processor != null) {
      processor.onTestCountInSuite(testCount.getCount());
    }
  }

  @Override
  public void visitTestReporterAttached(TestReporterAttached testReporterAttached) {
    if (processor != null) {
      processor.onTestFrameworkAttached();
    }
  }

  @Override
  public void visitSuiteTreeEnded(SuiteTreeEnded suiteTreeEnded) {
    if (processor != null) {
      processor.onSuiteTreeEnded(suiteTreeEnded.getSuiteName());
    }
  }

  @Override
  public void visitSuiteTreeNode(SuiteTreeNode suiteTreeNode) {
    if (processor != null) {
      processor.onSuiteTreeNodeAdded(suiteTreeNode.getSuiteName(), suiteTreeNode.getLocation());
    }
  }

  @Override
  public void visitBuildTreeEnded(BuildTreeEnded buildTreeEnded) {
    if (processor != null) {
      processor.onBuildTreeEnded();
    }
  }

  @Override
  public void visitRootPresentation(RootPresentationMessage presentationMessage) {
    if (processor != null) {
      processor.onRootPresentationAdded(
          presentationMessage.getRootName(),
          presentationMessage.getComment(),
          presentationMessage.getLocation());
    }
  }

  @Override
  public void visitUncapturedOutput(UncapturedOutputMessage uncapturedOutputMessage) {
    if (processor != null) {
      processor.onUncapturedOutput(
          uncapturedOutputMessage.getOutput(), uncapturedOutputMessage.getOutputType());
    }
  }
}
