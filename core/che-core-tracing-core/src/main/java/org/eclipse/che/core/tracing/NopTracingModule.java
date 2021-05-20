/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import com.google.inject.AbstractModule;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;

/** Guice module that is used if tracing is not enabled. */
public class NopTracingModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Tracer.class).toProvider(new TracerProvider(NoopTracerFactory.create()));
  }
}
