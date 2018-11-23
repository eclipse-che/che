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
package org.eclipse.che.commons.tracing;

import com.google.inject.Inject;
import io.opentracing.Tracer;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * This is a helper to declare an optional tracer in a {@link Inject}-annotated constructor of some
 * Guice-managed class.
 */
public class OptionalTracer {

  @Inject(optional = true)
  private Tracer tracer;

  @Nullable
  public static Tracer fromNullable(@Nullable OptionalTracer optionalTracer) {
    return optionalTracer == null ? null : optionalTracer.tracer;
  }

  @Nullable
  public Tracer getTracer() {
    return tracer;
  }
}
