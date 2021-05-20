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
package org.eclipse.che;

import com.google.inject.servlet.ServletModule;
import org.eclipse.che.inject.DynaModule;

@DynaModule
public class DashboardModule extends ServletModule {
  @Override
  protected void configureServlets() {
    filter("/api", "/api/*").through(ApiAccessRejectionFilter.class);
    filter("/*").through(DashboardRedirectionFilter.class);
  }
}
