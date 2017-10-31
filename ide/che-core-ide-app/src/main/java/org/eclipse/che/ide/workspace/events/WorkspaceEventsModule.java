/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    bind(MachineLogHandler.class).asEagerSingleton();
  }
}
