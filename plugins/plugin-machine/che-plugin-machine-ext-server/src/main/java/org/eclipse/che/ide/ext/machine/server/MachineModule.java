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
package org.eclipse.che.ide.ext.machine.server;

import com.google.inject.AbstractModule;
import org.eclipse.che.ide.ext.machine.server.ssh.WorkspaceSshKeys;
import org.eclipse.che.ide.ext.machine.server.ssh.WorkspaceStatusSubscriber;
import org.eclipse.che.inject.DynaModule;

@DynaModule
public class MachineModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(WorkspaceStatusSubscriber.class).asEagerSingleton();
    bind(WorkspaceSshKeys.class).asEagerSingleton();
  }
}
