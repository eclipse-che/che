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
package org.eclipse.che.ide.dto;

import com.google.gwt.inject.client.AbstractGinModule;

/** GIN module for configuring DTO components. */
public class DtoModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(DtoRegistrar.class).asEagerSingleton();
  }
}
