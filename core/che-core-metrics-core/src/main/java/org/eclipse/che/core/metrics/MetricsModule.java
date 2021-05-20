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
package org.eclipse.che.core.metrics;

import com.google.common.annotations.Beta;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import okhttp3.EventListener;

@Beta
public class MetricsModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(MetricsServer.class).asEagerSingleton();
    bind(MetricsBinder.class).asEagerSingleton();
    bind(CollectorRegistry.class).toInstance(CollectorRegistry.defaultRegistry);
    bind(PrometheusMeterRegistry.class)
        .toProvider(PrometheusMeterRegistryProvider.class)
        .asEagerSingleton();
    bind(MeterRegistry.class).to(PrometheusMeterRegistry.class);

    Multibinder<MeterBinder> meterMultibinder =
        Multibinder.newSetBinder(binder(), MeterBinder.class);
    meterMultibinder.addBinding().to(ClassLoaderMetrics.class);
    meterMultibinder.addBinding().to(JvmMemoryMetrics.class);
    meterMultibinder.addBinding().to(JvmGcMetrics.class);
    meterMultibinder.addBinding().to(JvmThreadMetrics.class);
    meterMultibinder.addBinding().to(LogbackMetrics.class);
    meterMultibinder.addBinding().to(FileDescriptorMetrics.class);
    meterMultibinder.addBinding().to(ProcessorMetrics.class);
    meterMultibinder.addBinding().to(UptimeMetrics.class);
    meterMultibinder.addBinding().to(FileStoresMeterBinder.class);
    meterMultibinder.addBinding().to(ApiResponseCounter.class);
    meterMultibinder.addBinding().to(ProcessMemoryMetrics.class);
    meterMultibinder.addBinding().to(ProcessThreadMetrics.class);

    bind(EventListener.class).toProvider(OkHttpMetricsEventListenerProvider.class);
  }
}
