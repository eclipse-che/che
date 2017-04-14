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
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.plugin.docker.client.DockerRegistryDynamicAuthResolver;
import org.eclipse.che.plugin.docker.client.NoOpDockerRegistryDynamicAuthResolverImpl;
import org.eclipse.che.workspace.infrastructure.docker.config.DockerExtraHostsFromPropertyProvider;
import org.eclipse.che.workspace.infrastructure.docker.config.dns.DnsResolversModule;
import org.eclipse.che.workspace.infrastructure.docker.config.env.ApiEndpointEnvVariableProvider;
import org.eclipse.che.workspace.infrastructure.docker.config.env.JavaOptsEnvVariableProvider;
import org.eclipse.che.workspace.infrastructure.docker.config.env.ProjectsRootEnvVariableProvider;
import org.eclipse.che.workspace.infrastructure.docker.config.proxy.DockerProxyModule;
import org.eclipse.che.workspace.infrastructure.docker.config.volume.ExtraVolumeModule;
import org.eclipse.che.workspace.infrastructure.docker.environment.DockerEnvironmentTypeModule;
import org.eclipse.che.workspace.infrastructure.docker.strategy.ServerEvaluationStrategyModule;

import java.util.Set;

/**
 * @author Alexander Garagatyi
 */
public class DockerInfraModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<String> devMachineEnvVars =
                Multibinder.newSetBinder(binder(),
                                         String.class,
                                         Names.named("machine.docker.dev_machine.machine_env"))
                           .permitDuplicates();
        Multibinder<String> allMachinesEnvVars =
                Multibinder.newSetBinder(binder(),
                                         String.class,
                                         Names.named("machine.docker.machine_env"))
                           .permitDuplicates();

        /*@SuppressWarnings("unused") Multibinder<ServerConf> devMachineServers =
                Multibinder.newSetBinder(binder(),
                                         ServerConf.class,
                                         Names.named("machine.docker.dev_machine.machine_servers"));
        @SuppressWarnings("unused") Multibinder<ServerConf> machineServers =
                Multibinder.newSetBinder(binder(),
                                         ServerConf.class,
                                         Names.named("machine.docker.machine_servers"));*/

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

        // Extra hosts configuration
        Multibinder<Set<String>> extraHosts = Multibinder.newSetBinder(binder(),
                                                                       new TypeLiteral<Set<String>>() {},
                                                                       Names.named("che.docker.extra_hosts"))
                                                         .permitDuplicates();
        extraHosts.addBinding()
                  .toProvider(DockerExtraHostsFromPropertyProvider.class);

        // Environment variables configuration
        devMachineEnvVars.addBinding().toProvider(ProjectsRootEnvVariableProvider.class);
        devMachineEnvVars.addBinding().toProvider(JavaOptsEnvVariableProvider.class);
        allMachinesEnvVars.addBinding().toProvider(ApiEndpointEnvVariableProvider.class);

        install(new DnsResolversModule());
        install(new DockerProxyModule());
        install(new ExtraVolumeModule());
        install(new DockerEnvironmentTypeModule());
        install(new ServerEvaluationStrategyModule());

        Multibinder<RuntimeInfrastructure> mb = Multibinder.newSetBinder(binder(), RuntimeInfrastructure.class);
        mb.addBinding().to(DockerRuntimeInfrastructure.class);

        bind(DockerRegistryDynamicAuthResolver.class).to(NoOpDockerRegistryDynamicAuthResolverImpl.class);

        install(new FactoryModuleBuilder()
                        .implement(DockerRuntimeContext.class, DockerRuntimeContext.class)
                        .build(RuntimeFactory.class));
    }
}
