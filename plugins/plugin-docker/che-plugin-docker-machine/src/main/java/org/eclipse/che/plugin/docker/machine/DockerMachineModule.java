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
package org.eclipse.che.plugin.docker.machine;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.core.model.machine.ServerConf;

/**
 * Module for components that are needed for {@link DockerInstanceProvider}
 *
 * @author Alexander Garagatyi
 */
public class DockerMachineModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(org.eclipse.che.plugin.docker.machine.cleaner.DockerContainerCleaner.class);

        Multibinder<String> devMachineEnvVars = Multibinder.newSetBinder(binder(),
                                                                         String.class,
                                                                         Names.named("machine.docker.dev_machine.machine_env"))
                                                           .permitDuplicates();
        Multibinder<String> allMachinesEnvVars = Multibinder.newSetBinder(binder(),
                                                                          String.class,
                                                                          Names.named("machine.docker.machine_env"))
                                                            .permitDuplicates();

        Multibinder<ServerConf> devMachineServers = Multibinder.newSetBinder(binder(),
                                                                             ServerConf.class,
                                                                             Names.named("machine.docker.dev_machine.machine_servers"));
        Multibinder<ServerConf> machineServers = Multibinder.newSetBinder(binder(),
                                                                          ServerConf.class,
                                                                          Names.named("machine.docker.machine_servers"));

        Multibinder<String> devMachineVolumes = Multibinder.newSetBinder(binder(),
                                                                         String.class,
                                                                         Names.named("machine.docker.dev_machine.machine_volumes"))
                                                           .permitDuplicates();

        Multibinder<String> machineVolumes = Multibinder.newSetBinder(binder(),
                                                                      String.class,
                                                                      Names.named("machine.docker.machine_volumes"));
    }
}
