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
package org.eclipse.che.ide.workspace.events;

import com.google.gwt.inject.client.AbstractGinModule;

public class WorkspaceEventsModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(WorkspaceStatusEventHandler.class).asEagerSingleton();
    bind(MachineStatusEventHandler.class).asEagerSingleton();
    bind(ServerStatusEventHandler.class).asEagerSingleton();

    bind(InstallerLogHandler.class).asEagerSingleton();
    bind(InstallerStatusEventHandler.class).asEagerSingleton();
    bind(MachineLogHandler.class).asEagerSingleton();
  }
}
