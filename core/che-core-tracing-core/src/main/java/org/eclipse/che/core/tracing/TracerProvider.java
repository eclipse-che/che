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
package org.eclipse.che.core.tracing;

import com.google.common.annotations.Beta;
import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import io.opentracing.util.GlobalTracer;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Guice @{@link javax.inject.Provider} of @{@link io.opentracing.Tracer} objects. Register Tracer
 * in @{@link io.opentracing.util.GlobalTracer} for future use by classes that has no access to
 * container like datasources, etc.
 */
@Beta
@Singleton
public class TracerProvider implements Provider<Tracer> {
  private static final Logger LOG = LoggerFactory.getLogger(TracerProvider.class);
  private final Tracer tracer;

  public TracerProvider() {
    this(TracerResolver.resolveTracer());
  }

  public TracerProvider(Tracer tracer) {
    this.tracer = tracer;
    GlobalTracer.registerIfAbsent(tracer);
  }

  @Override
  public Tracer get() {
    LOG.debug("{} tracer is used ", tracer);
    return tracer;
  }
}
