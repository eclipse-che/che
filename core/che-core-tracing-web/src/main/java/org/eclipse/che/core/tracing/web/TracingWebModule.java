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
import com.google.inject.servlet.ServletModule;
import io.opentracing.contrib.web.servlet.filter.TracingFilter;
import javax.inject.Singleton;

@Beta
public class TracingWebModule extends ServletModule {
  @Override
  protected void configureServlets() {
    // tracing
    bind(TracingFilter.class).toProvider(TracingFilterProvider.class).in(Singleton.class);
    filter("/*").through(TracingFilter.class);
  }
}
