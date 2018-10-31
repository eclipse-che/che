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

import io.opentracing.Tracer;
import io.opentracing.contrib.tracerresolver.TracerResolver;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class TracerProvider implements Provider<Tracer> {
  private final Tracer tracer;

  public TracerProvider() {
    this.tracer = TracerResolver.resolveTracer();
  }

  @Override
  public Tracer get() {
    return tracer;
  }
}
