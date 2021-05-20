/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.infraproxy.server;

import com.google.inject.AbstractModule;

/** Guice module class configuring the infra proxy. */
public class InfraProxyModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(InfrastructureApiService.class);
  }
}
