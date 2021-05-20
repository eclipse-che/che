/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.jsonrpc.commons.RequestProcessorConfigurationProvider;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.commons.lang.execution.ExecutorServiceBuilder;
import org.eclipse.che.commons.observability.ExecutorServiceWrapper;
import org.slf4j.Logger;

/**
 * {@link RequestProcessorConfigurationProvider.Configuration} implementation used to configure
 * {@link CheMajorWebSocketEndpoint}
 */
public class CheMajorWebSocketEndpointConfiguration
    implements RequestProcessorConfigurationProvider.Configuration {

  private static final Logger LOG = getLogger(CheMajorWebSocketEndpointConfiguration.class);

  private final ExecutorService executor;

  public static final String JSON_RPC_MAJOR_CORE_POOL_SIZE_PARAMETER_NAME =
      "che.core.jsonrpc.processor_core_pool_size";
  public static final String JSON_RPC_MAJOR_MAX_POOL_SIZE_PARAMETER_NAME =
      "che.core.jsonrpc.processor_max_pool_size";
  public static final String JSON_RPC_MAJOR_QUEUE_CAPACITY_PARAMETER_NAME =
      "che.core.jsonrpc.processor_queue_capacity";

  @Inject
  public CheMajorWebSocketEndpointConfiguration(
      @Named(JSON_RPC_MAJOR_CORE_POOL_SIZE_PARAMETER_NAME) int corePoolSize,
      @Named(JSON_RPC_MAJOR_MAX_POOL_SIZE_PARAMETER_NAME) int maxPoolSize,
      @Named(JSON_RPC_MAJOR_QUEUE_CAPACITY_PARAMETER_NAME) int queueCapacity,
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
                        .setNameFormat(CheMajorWebSocketEndpoint.ENDPOINT_ID + "-%d")
                        .setDaemon(true)
                        .build())
                .rejectedExecutionHandler(
                    (r, executor) ->
                        LOG.error(
                            "Executor on major websocket endpoint rejected to handle the payload {}. Some important messages may be lost. Consider increasing `{}`. Now it's configured to {}",
                            r,
                            JSON_RPC_MAJOR_QUEUE_CAPACITY_PARAMETER_NAME,
                            queueCapacity))
                .build(),
            CheMajorWebSocketEndpoint.ENDPOINT_ID);
  }

  @Override
  public String getEndpointId() {
    return CheMajorWebSocketEndpoint.ENDPOINT_ID;
  }

  @Override
  public ExecutorService getExecutorService() {
    return executor;
  }
}
