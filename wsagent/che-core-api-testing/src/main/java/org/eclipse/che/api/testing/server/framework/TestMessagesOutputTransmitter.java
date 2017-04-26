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
package org.eclipse.che.api.testing.server.framework;

import org.eclipse.che.api.core.jsonrpc.RequestTransmitter;
import org.eclipse.che.api.testing.server.messages.ServerTestingMessage;
import org.eclipse.che.api.testing.shared.Constants;
import org.eclipse.che.commons.lang.execution.ProcessEvent;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.commons.lang.execution.ProcessListener;
import org.eclipse.che.commons.lang.execution.ProcessOutputType;

/**
 * Process and send testing messages to the client
 */
public class TestMessagesOutputTransmitter {

    private final RequestTransmitter requestTransmitter;
    private final String endpoint;
    private ProcessHandler processHandler;

    public TestMessagesOutputTransmitter(ProcessHandler processHandler, RequestTransmitter requestTransmitter, String endpoint) {
        this.processHandler = processHandler;
        this.requestTransmitter = requestTransmitter;
        this.endpoint = endpoint;

        processHandler.addProcessListener(new ProcessListener() {
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
                //ignore
            }
        });
    }

    private void processTestingStopped() {
        requestTransmitter.transmitStringToNone(endpoint, Constants.TESTING_RPC_METHOD_NAME, ServerTestingMessage.FINISH_TESTING.asJsonString());
    }

    private void process(String text, ProcessOutputType outputType) {
        //TODO
    }

    private void processStartTesting() {
        requestTransmitter.transmitStringToNone(endpoint, Constants.TESTING_RPC_METHOD_NAME, ServerTestingMessage.TESTING_STARTED.asJsonString());
    }

    public void stop() {
        if (!processHandler.isProcessTerminated()) {
            processHandler.destroyProcess();
        }
    }
}
