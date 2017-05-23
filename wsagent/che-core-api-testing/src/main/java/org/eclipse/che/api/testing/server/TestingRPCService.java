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
package org.eclipse.che.api.testing.server;

import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.testing.server.framework.TestFrameworkRegistry;
import org.eclipse.che.api.testing.server.framework.TestMessagesOutputTransmitter;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.shared.Constants;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.commons.lang.execution.ProcessHandler;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class TestingRPCService {

    private final RequestTransmitter requestTransmitter;
    private final TestFrameworkRegistry frameworkRegistry;
    private String endpoint;
    private TestMessagesOutputTransmitter outputTransmitter;

    @Inject
    public TestingRPCService(RequestTransmitter requestTransmitter, TestFrameworkRegistry frameworkRegistry) {
        this.requestTransmitter = requestTransmitter;
        this.frameworkRegistry = frameworkRegistry;
    }


    private boolean runTests(String endpoint, TestExecutionContext context) {
        this.endpoint = endpoint;
        TestRunner testRunner = frameworkRegistry.getTestRunner(context.getFrameworkName());

        if (testRunner != null) {
            if (outputTransmitter != null) {
                outputTransmitter.stop();
            }
            ProcessHandler processHandler = testRunner.execute(context);
            outputTransmitter = new TestMessagesOutputTransmitter(processHandler, requestTransmitter, endpoint);
            return true;
        } else {
            //TODO add logging and send info message about failure
            return false;
        }
    }

    @Inject
    private void configureRunTestHandler(RequestHandlerConfigurator configurator) {
        configurator.newConfiguration()
                .methodName(Constants.RUN_TESTS_METHOD)
                .paramsAsDto(TestExecutionContext.class)
                .resultAsBoolean()
                .withFunction(this::runTests);
    }
}
