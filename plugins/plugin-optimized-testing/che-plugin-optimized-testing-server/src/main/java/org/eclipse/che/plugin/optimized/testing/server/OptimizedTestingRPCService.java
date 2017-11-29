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
package org.eclipse.che.plugin.optimized.testing.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.testing.shared.OptimizedConstants;
import org.eclipse.che.api.testing.shared.dto.FailedTestsToStoreDto;
import org.eclipse.che.api.testing.shared.dto.SmartTestingConfigurationDto;

/** Optimized Test JSON RPC API. */
@Singleton
public class OptimizedTestingRPCService {

  @Inject
  private void loadSmartTestingConfigurationHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(OptimizedConstants.OPTIMIZED_TESTING_RPC_LOAD_CONFIGURATION)
        .paramsAsDto(String.class)
        .resultAsDto(SmartTestingConfigurationDto.class)
        .withFunction(SmartTestingConfigLoader::getMainSmartTestingConfigParams);
  }

  @Inject
  private void saveTestFailureResults(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(OptimizedConstants.OPTIMIZED_TESTING_RPC_SAVE_TEST_FAILURE_RESULTS)
        .paramsAsDto(FailedTestsToStoreDto.class)
        .noResult()
        .withConsumer(TestFailureResultStorage::saveFailedTests);
  }
}
