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
package org.eclipse.che.core.tracing.web;

import com.google.common.annotations.Beta;
import io.opentracing.Tracer;
import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Guice @{@link javax.inject.Provider} of @{@link
 * io.opentracing.contrib.web.servlet.filter.TracingFilter} objects
 */
@Beta
@Singleton
public class TracingFilterProvider implements Provider<TracingFilter> {

  private final TracingFilter filter;

  @Inject
  public TracingFilterProvider(Tracer tracer) {
    filter = new TracingFilter(tracer);
  }

  @Override
  public TracingFilter get() {
    return filter;
  }
}
