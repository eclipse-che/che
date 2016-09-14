/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.machine.ssh;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
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
        Multibinder<org.eclipse.che.api.machine.server.spi.InstanceProvider> machineProviderMultibinder =
                Multibinder.newSetBinder(binder(),
                                         org.eclipse.che.api.machine.server.spi.InstanceProvider.class);
        machineProviderMultibinder.addBinding()
                                  .to(SshMachineInstanceProvider.class);

        install(new FactoryModuleBuilder()
                        .implement(org.eclipse.che.api.machine.server.spi.Instance.class,
                                   org.eclipse.che.plugin.machine.ssh.SshMachineInstance.class)
                        .implement(org.eclipse.che.api.machine.server.spi.InstanceProcess.class,
                                   org.eclipse.che.plugin.machine.ssh.SshMachineProcess.class)
                        .implement(org.eclipse.che.plugin.machine.ssh.SshClient.class,
                                   org.eclipse.che.plugin.machine.ssh.jsch.JschSshClient.class)
                        .build(SshMachineFactory.class));

        bindConstant().annotatedWith(Names.named("machine.ssh.server.terminal.location")).to("~/che");

        Multibinder.newSetBinder(binder(), org.eclipse.che.api.agent.server.launcher.AgentLauncher.class)
                   .addBinding().to(org.eclipse.che.plugin.machine.ssh.SshMachineImplTerminalLauncher.class);

        Multibinder<org.eclipse.che.api.core.model.machine.ServerConf> machineServers =
                Multibinder.newSetBinder(binder(),
                                         org.eclipse.che.api.core.model.machine.ServerConf.class,
                                         Names.named("machine.ssh.machine_servers"));
        machineServers.addBinding().toProvider(TerminalServerConfProvider.class);
    }
}
