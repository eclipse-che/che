/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
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
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.core.model.machine.ServerConf;
import org.eclipse.che.api.environment.server.TypeSpecificEnvironmentParser;
import org.eclipse.che.plugin.docker.machine.parser.DockerImageEnvironmentParser;
import org.eclipse.che.plugin.docker.machine.parser.DockerfileEnvironmentParser;

import java.util.Set;

/**
 * Module for components that are needed for {@link MachineProviderImpl}
 *
 * @author Alexander Garagatyi
 */
public class DockerMachineModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(org.eclipse.che.plugin.docker.machine.cleaner.DockerAbandonedResourcesCleaner.class);
        bind(org.eclipse.che.plugin.docker.machine.cleaner.RemoveWorkspaceFilesAfterRemoveWorkspaceEventSubscriber.class);

        @SuppressWarnings("unused") Multibinder<String> devMachineEnvVars =
                Multibinder.newSetBinder(binder(),
                                         String.class,
                                         Names.named("machine.docker.dev_machine.machine_env"))
                           .permitDuplicates();
        @SuppressWarnings("unused") Multibinder<String> allMachinesEnvVars =
                Multibinder.newSetBinder(binder(),
                                         String.class,
                                         Names.named("machine.docker.machine_env"))
                           .permitDuplicates();

        @SuppressWarnings("unused") Multibinder<ServerConf> devMachineServers =
                Multibinder.newSetBinder(binder(),
                                         ServerConf.class,
                                         Names.named("machine.docker.dev_machine.machine_servers"));
        @SuppressWarnings("unused") Multibinder<ServerConf> machineServers =
                Multibinder.newSetBinder(binder(),
                                         ServerConf.class,
                                         Names.named("machine.docker.machine_servers"));

        @SuppressWarnings("unused") Multibinder<String> devMachineVolumes =
                Multibinder.newSetBinder(binder(),
                                         String.class,
                                         Names.named("machine.docker.dev_machine.machine_volumes"))
                           .permitDuplicates();

        @SuppressWarnings("unused") Multibinder<String> machineVolumes =
                Multibinder.newSetBinder(binder(),
                                         String.class,
                                         Names.named("machine.docker.machine_volumes"));

        // Provides set of sets of strings instead of set of strings.
        // This allows providers to return empty set as a value if no value should be added by provider.
        // .permitDuplicates() is needed to allow different providers add empty sets.
        @SuppressWarnings("unused") Multibinder<Set<String>> networks =
                Multibinder.newSetBinder(binder(),
                                         new TypeLiteral<Set<String>>() {},
                                         Names.named("machine.docker.networks"))
                           .permitDuplicates();

        bind(org.eclipse.che.api.environment.server.ContainerNameGenerator.class)
                .to(org.eclipse.che.plugin.docker.machine.DockerContainerNameGenerator.class);

        MapBinder<String, TypeSpecificEnvironmentParser> envParserMapBinder = MapBinder.newMapBinder(binder(),
                                                                                                     String.class,
                                                                                                     TypeSpecificEnvironmentParser.class);
        envParserMapBinder.addBinding("dockerfile").to(DockerfileEnvironmentParser.class);
        envParserMapBinder.addBinding("dockerimage").to(DockerImageEnvironmentParser.class);

        allMachinesEnvVars.addBinding().toProvider(org.eclipse.che.plugin.docker.machine.ApiEndpointEnvVariableProvider.class);

        // Provides set of sets of strings instead of set of strings.
        // This allows providers to return empty set as a value if no value should be added by provider.
        // .permitDuplicates() is needed to allow different providers add empty sets.
        Multibinder<Set<String>> extraHosts = Multibinder.newSetBinder(binder(),
                                                                       new TypeLiteral<Set<String>>() {},
                                                                       Names.named("che.docker.extra_hosts"))
                                                         .permitDuplicates();
        extraHosts.addBinding()
                  .toProvider(org.eclipse.che.plugin.docker.machine.DockerExtraHostsFromPropertyProvider.class);
    }
}
