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
package org.eclipse.che.plugin.machine.ssh;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

/**
 * Provides bindings needed for ssh machine implementation usage.
 *
 * @author Alexander Garagatyi
 */
public class SshMachineModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SshMachineInstanceProvider.class);

    bind(SshMachineFactory.class);

    bindConstant().annotatedWith(Names.named("machine.ssh.server.terminal.location")).to("~/che");

    Multibinder<org.eclipse.che.api.core.model.machine.ServerConf> machineServers =
        Multibinder.newSetBinder(
            binder(),
            org.eclipse.che.api.core.model.machine.ServerConf.class,
            Names.named("machine.ssh.machine_servers"));
  }
}
