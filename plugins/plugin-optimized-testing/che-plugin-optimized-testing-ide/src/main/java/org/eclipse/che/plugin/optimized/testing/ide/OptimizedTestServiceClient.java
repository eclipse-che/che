/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.optimized.testing.ide;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.testing.shared.OptimizedConstants;
import org.eclipse.che.api.testing.shared.dto.FailedTestsToStoreDto;
import org.eclipse.che.api.testing.shared.dto.SmartTestingConfigurationDto;

/** Client for calling optimized test services */
@Singleton
public class OptimizedTestServiceClient {

  private final RequestTransmitter requestTransmitter;

  @Inject
  public OptimizedTestServiceClient(RequestTransmitter requestTransmitter) {
    this.requestTransmitter = requestTransmitter;
  }

  public JsonRpcPromise<SmartTestingConfigurationDto> getConfiguration(String projectPath) {
    return requestTransmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName(OptimizedConstants.OPTIMIZED_TESTING_RPC_LOAD_CONFIGURATION)
        .paramsAsDto(projectPath)
        .sendAndReceiveResultAsDto(SmartTestingConfigurationDto.class);
  }

  public void saveTestFailureResults(FailedTestsToStoreDto testsWithFailure) {
    requestTransmitter
        .newRequest()
        .endpointId("ws-agent")
        .methodName(OptimizedConstants.OPTIMIZED_TESTING_RPC_SAVE_TEST_FAILURE_RESULTS)
        .paramsAsDto(testsWithFailure)
        .sendAndSkipResult();
  }
}
