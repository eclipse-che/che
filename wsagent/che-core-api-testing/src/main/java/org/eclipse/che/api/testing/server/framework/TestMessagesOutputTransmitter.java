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
package org.eclipse.che.api.testing.server.framework;

import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.testing.server.messages.ServerTestingMessage;
import org.eclipse.che.api.testing.server.messages.UncapturedOutputMessage;
import org.eclipse.che.api.testing.shared.Constants;
import org.eclipse.che.commons.lang.execution.ProcessEvent;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.commons.lang.execution.ProcessListener;
import org.eclipse.che.commons.lang.execution.ProcessOutputType;

/** Process and send testing messages to the client */
public class TestMessagesOutputTransmitter {

  private final RequestTransmitter requestTransmitter;
  private final String endpoint;
  private final LineSplitter lineSplitter;
  private ProcessHandler processHandler;

  public TestMessagesOutputTransmitter(
      ProcessHandler processHandler, RequestTransmitter requestTransmitter, String endpoint) {
    this.processHandler = processHandler;
    this.requestTransmitter = requestTransmitter;
    this.endpoint = endpoint;

    lineSplitter = new LineSplitter(this::processLine);

    processHandler.addProcessListener(
        new ProcessListener() {
          @Override
          public void onStart(ProcessEvent event) {
            processStartTesting();
          }

          @Override
          public void onText(ProcessEvent event, ProcessOutputType outputType) {
            process(event.getText(), outputType);
          }

          @Override
          public void onProcessTerminated(ProcessEvent event) {
            processTestingStopped();
          }

          @Override
          public void onProcessWillTerminate(ProcessEvent event) {
            // ignore
          }
        });

    processHandler.startNotify();
  }

  private void processLine(String line, ProcessOutputType outputType) {
    if (!processTestingMessage(line)) {
      sendOutput(line, outputType);
    }
  }

  private void sendOutput(String text, ProcessOutputType outputType) {
    UncapturedOutputMessage message = new UncapturedOutputMessage(text, outputType);
    requestTransmitter
        .newRequest()
        .endpointId(endpoint)
        .methodName(Constants.TESTING_RPC_METHOD_NAME)
        .paramsAsString(message.asJsonString())
        .sendAndSkipResult();
  }

  private boolean processTestingMessage(String line) {
    ServerTestingMessage message = ServerTestingMessage.parse(line.trim());
    if (message != null) {
      requestTransmitter
          .newRequest()
          .endpointId(endpoint)
          .methodName(Constants.TESTING_RPC_METHOD_NAME)
          .paramsAsString(message.asJsonString())
          .sendAndSkipResult();
      return true;
    }
    return false;
  }

  private void processTestingStopped() {
    lineSplitter.flush();
    requestTransmitter
        .newRequest()
        .endpointId(endpoint)
        .methodName(Constants.TESTING_RPC_METHOD_NAME)
        .paramsAsString(ServerTestingMessage.FINISH_TESTING.asJsonString())
        .sendAndSkipResult();
  }

  private void process(String text, ProcessOutputType outputType) {
    lineSplitter.process(text, outputType);
  }

  private void processStartTesting() {
    requestTransmitter
        .newRequest()
        .endpointId(endpoint)
        .methodName(Constants.TESTING_RPC_METHOD_NAME)
        .paramsAsString(ServerTestingMessage.TESTING_STARTED.asJsonString())
        .sendAndSkipResult();
  }

  public void stop() {
    if (!processHandler.isProcessTerminated()) {
      processHandler.destroyProcess();
    }
  }
}
