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
package org.eclipse.che.api.deploy;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.deploy.jsonrpc.CheMajorWebSocketEndpointConfiguration;
import org.eclipse.che.api.deploy.jsonrpc.CheMajorWebSocketEndpointExecutorServiceProvider;
import org.eclipse.che.api.deploy.jsonrpc.CheMinorWebSocketEndpointConfiguration;
import org.eclipse.che.api.deploy.jsonrpc.CheMinorWebSocketEndpointExecutorServiceProvider;
import org.eclipse.che.core.metrics.ExecutorServiceMetrics;

/**
 * {@link Module} that provides metered implementation for different classes. Metrics will be
 * published to {@link PrometheusMeterRegistry}.
 */
public class MetricsOverrideBinding implements Module {
  @Override
  public void configure(Binder binder) {
    binder
        .bind(CheMajorWebSocketEndpointExecutorServiceProvider.class)
        .to(MeteredCheMajorWebSocketEndpointExecutorServiceProvider.class);

    binder
        .bind(CheMinorWebSocketEndpointExecutorServiceProvider.class)
        .to(MeteredCheMinorWebSocketEndpointExecutorServiceProvider.class);
  }

  @Singleton
  public static class MeteredCheMajorWebSocketEndpointExecutorServiceProvider
      extends CheMajorWebSocketEndpointExecutorServiceProvider {

    private final PrometheusMeterRegistry meterRegistry;

    private ExecutorService executorService;

    @Inject
    public MeteredCheMajorWebSocketEndpointExecutorServiceProvider(
        @Named(JSON_RPC_MAJOR_CORE_POOL_SIZE_PARAMETER_NAME) int corePoolSize,
        @Named(JSON_RPC_MAJOR_MAX_POOL_SIZE_PARAMETER_NAME) int maxPoolSize,
        @Named(JSON_RPC_MAJOR_QUEUE_CAPACITY_PARAMETER_NAME) int queueCapacity,
        PrometheusMeterRegistry meterRegistry) {
      super(corePoolSize, maxPoolSize, queueCapacity);
      this.meterRegistry = meterRegistry;
    }

    @Override
    public synchronized ExecutorService get() {
      if (executorService == null) {
        executorService =
            ExecutorServiceMetrics.monitor(
                meterRegistry,
                super.get(),
                CheMajorWebSocketEndpointConfiguration.EXECUTOR_NAME,
                Tags.empty());
      }
      return executorService;
    }
  }

  @Singleton
  public static class MeteredCheMinorWebSocketEndpointExecutorServiceProvider
      extends CheMinorWebSocketEndpointExecutorServiceProvider {
    private final PrometheusMeterRegistry meterRegistry;

    private ExecutorService executorService;

    @Inject
    public MeteredCheMinorWebSocketEndpointExecutorServiceProvider(
        @Named(JSON_RPC_MINOR_CORE_POOL_SIZE_PARAMETER_NAME) int corePoolSize,
        @Named(JSON_RPC_MINOR_MAX_POOL_SIZE_PARAMETER_NAME) int maxPoolSize,
        @Named(JSON_RPC_MINOR_QUEUE_CAPACITY_PARAMETER_NAME) int queueCapacity,
        PrometheusMeterRegistry meterRegistry) {
      super(corePoolSize, maxPoolSize, queueCapacity);
      this.meterRegistry = meterRegistry;
    }

    @Override
    public synchronized ExecutorService get() {
      if (executorService == null) {

        executorService =
            ExecutorServiceMetrics.monitor(
                meterRegistry,
                super.get(),
                CheMinorWebSocketEndpointConfiguration.EXECUTOR_NAME,
                Tags.empty());
      }
      return executorService;
    }
  }
}
