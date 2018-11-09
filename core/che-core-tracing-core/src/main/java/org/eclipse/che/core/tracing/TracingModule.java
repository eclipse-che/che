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
package org.eclipse.che.core.tracing;

import com.google.common.annotations.Beta;
import com.google.inject.AbstractModule;
import io.opentracing.Tracer;

@Beta
public class TracingModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Tracer.class).toProvider(TracerProvider.class);
  }
}
