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
package org.eclipse.che.plugin.docker.machine.ext;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.plugin.docker.machine.ext.provider.WsAgentVolumeProvider;

/**
 * Guice module for extension servers feature in docker machines
 *
 * @author Alexander Garagatyi
 * @author Sergii Leschenko
 * @author Roman Iuvshyn
 */
// Not a DynaModule, install manually
public class DockerExtServerModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<String> volumesMultibinder = Multibinder.newSetBinder(binder(),
                                                                          String.class,
                                                                          Names.named("machine.docker.dev_machine.machine_volumes"));
        volumesMultibinder.addBinding()
                          .toProvider(WsAgentVolumeProvider.class);

        Multibinder<String> devMachineEnvVars = Multibinder.newSetBinder(binder(),
                                                                         String.class,
                                                                         Names.named("machine.docker.dev_machine.machine_env"))
                                                           .permitDuplicates();
        devMachineEnvVars.addBinding().toProvider(org.eclipse.che.plugin.docker.machine.ext.provider.ApiEndpointEnvVariableProvider.class);
        devMachineEnvVars.addBinding().toProvider(org.eclipse.che.plugin.docker.machine.ext.provider.ProjectsRootEnvVariableProvider.class);
        devMachineEnvVars.addBinding().toProvider(org.eclipse.che.plugin.docker.machine.ext.provider.JavaOptsEnvVariableProvider.class);
        devMachineEnvVars.addBinding()
                         .toInstance(CheBootstrap.CHE_LOCAL_CONF_DIR
                                     + '='
                                     + org.eclipse.che.plugin.docker.machine.ext.provider.DockerExtConfBindingProvider
                                             .EXT_CHE_LOCAL_CONF_DIR);
        Multibinder<String> allMachinesEnvVars = Multibinder.newSetBinder(binder(),
                                                                          String.class,
                                                                          Names.named("machine.docker.machine_env"))
                                                            .permitDuplicates();
        allMachinesEnvVars.addBinding()
                          .toProvider(org.eclipse.che.plugin.docker.machine.ext.provider.ApiEndpointEnvVariableProvider.class);
        org.eclipse.che.plugin.docker.machine.ext.provider.DockerExtConfBindingProvider extConfBindingProvider =
                new org.eclipse.che.plugin.docker.machine.ext.provider.DockerExtConfBindingProvider();
        if (extConfBindingProvider.get() != null) {
            volumesMultibinder.addBinding().toProvider(extConfBindingProvider);
        }
    }
}
