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
package org.eclipse.che.api.deploy.jsonrpc;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.execution.ExecutorServiceBuilder;
import org.eclipse.che.commons.observability.ExecutorServiceWrapper;

/**
 * {@link RequestProcessorConfigurationProvider.Configuration} implementation used to configure
 * {@link CheMinorWebSocketEndpoint}
 */
public class CheMinorWebSocketEndpointConfiguration
    implements RequestProcessorConfigurationProvider.Configuration {

  private final ExecutorService executor;

  public static final String JSON_RPC_MINOR_CORE_POOL_SIZE_PARAMETER_NAME =
      "che.core.jsonrpc.minor_processor_core_pool_size";
  public static final String JSON_RPC_MINOR_MAX_POOL_SIZE_PARAMETER_NAME =
      "che.core.jsonrpc.minor_processor_max_pool_size";
  public static final String JSON_RPC_MINOR_QUEUE_CAPACITY_PARAMETER_NAME =
      "che.core.jsonrpc.minor_processor_queue_capacity";

  @Inject
  public CheMinorWebSocketEndpointConfiguration(
      @Named(JSON_RPC_MINOR_CORE_POOL_SIZE_PARAMETER_NAME) int corePoolSize,
      @Named(JSON_RPC_MINOR_MAX_POOL_SIZE_PARAMETER_NAME) int maxPoolSize,
      @Named(JSON_RPC_MINOR_QUEUE_CAPACITY_PARAMETER_NAME) int queueCapacity,
      ExecutorServiceWrapper wrapper) {
    this.executor =
        wrapper.wrap(
            new ExecutorServiceBuilder()
                .corePoolSize(corePoolSize)
                .maxPoolSize(maxPoolSize)
                .queueCapacity(queueCapacity)
                .threadFactory(
                    new ThreadFactoryBuilder()
                        .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                        .setNameFormat(CheMinorWebSocketEndpoint.ENDPOINT_ID + "-%d")
                        .setDaemon(true)
                        .build())
                .build(),
            CheMinorWebSocketEndpoint.ENDPOINT_ID);
  }

  @Override
  public String getEndpointId() {
    return CheMinorWebSocketEndpoint.ENDPOINT_ID;
  }

  @Override
  public ExecutorService getExecutorService() {
    return executor;
  }
}
