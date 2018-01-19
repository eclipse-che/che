/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
