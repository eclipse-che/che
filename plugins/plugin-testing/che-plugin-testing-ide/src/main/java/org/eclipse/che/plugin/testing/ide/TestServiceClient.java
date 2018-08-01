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
package org.eclipse.che.plugin.testing.ide;

import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.testing.shared.Constants;
import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestDetectionResult;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestLaunchResult;

/**
 * Client for calling test services
 *
 * @author Mirage Abeysekara
 * @author David Festal
 */
@Singleton
public class TestServiceClient {
  private final RequestTransmitter requestTransmitter;

  @Inject
  public TestServiceClient(RequestTransmitter requestTransmitter) {
    this.requestTransmitter = requestTransmitter;
  }

  public JsonRpcPromise<TestLaunchResult> runTests(TestExecutionContext context) {
    return requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(Constants.RUN_TESTS_METHOD)
        .paramsAsDto(context)
        .sendAndReceiveResultAsDto(TestLaunchResult.class);
  }

  public JsonRpcPromise<TestDetectionResult> detectTests(TestDetectionContext context) {
    return requestTransmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(Constants.TESTING_RPC_TEST_DETECTION_NAME)
        .paramsAsDto(context)
        .sendAndReceiveResultAsDto(TestDetectionResult.class);
  }
}
