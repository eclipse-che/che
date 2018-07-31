/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.processes.runtime;

import com.google.gwt.inject.client.AbstractGinModule;

/**
 * GIN module for configuring Runtime Info List information functionality.
 *
 * @author Vlad Zhukovskyi
 * @since 5.18.0
 */
public class RuntimeInfoGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(RuntimeInfoProvider.class).to(ContextBasedRuntimeInfoProvider.class);
    bind(RuntimeInfoWidgetFactory.class).to(CellTableRuntimeInfoWidgetFactory.class);
  }
}
