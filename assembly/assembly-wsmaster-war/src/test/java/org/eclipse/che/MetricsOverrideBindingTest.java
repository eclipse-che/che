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
package org.eclipse.che;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.micrometer.core.instrument.internal.TimedExecutorService;
import java.util.concurrent.ExecutorService;
import org.eclipse.che.api.deploy.jsonrpc.CheJsonRpcWebSocketConfigurationModule;
import org.eclipse.che.api.deploy.jsonrpc.CheMajorWebSocketEndpointConfiguration;
import org.eclipse.che.core.metrics.MetricsModule;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MetricsOverrideBindingTest {

  @Test
  public void shouldInjectMettered() {
    Injector injector =
        Guice.createInjector(
            new CheJsonRpcWebSocketConfigurationModule(),
            new org.eclipse.che.api.deploy.MetricsOverrideBinding(),
            new MetricsModule(),
            new Module() {
              @Override
              public void configure(Binder binder) {
                binder
                    .bindConstant()
                    .annotatedWith(Names.named("che.core.jsonrpc.processor_max_pool_size"))
                    .to(100);
                binder
                    .bindConstant()
                    .annotatedWith(Names.named("che.core.jsonrpc.processor_core_pool_size"))
                    .to(4);
                binder
                    .bindConstant()
                    .annotatedWith(Names.named("che.core.jsonrpc.minor_processor_max_pool_size"))
                    .to(100);
                binder
                    .bindConstant()
                    .annotatedWith(Names.named("che.core.jsonrpc.minor_processor_core_pool_size"))
                    .to(3);
                binder
                    .bindConstant()
                    .annotatedWith(Names.named("che.core.jsonrpc.processor_queue_capacity"))
                    .to(100);
                binder
                    .bindConstant()
                    .annotatedWith(Names.named("che.core.jsonrpc.minor_processor_queue_capacity"))
                    .to(100);

                binder.bindConstant().annotatedWith(Names.named("che.metrics.port")).to(100);
              }
            });
    CheMajorWebSocketEndpointConfiguration configuration =
        injector.getInstance(CheMajorWebSocketEndpointConfiguration.class);
    ExecutorService exc = configuration.getExecutorService();
    Assert.assertTrue(exc instanceof TimedExecutorService);
  }
}
