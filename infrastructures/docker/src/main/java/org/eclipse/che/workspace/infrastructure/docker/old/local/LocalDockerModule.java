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
package org.eclipse.che.workspace.infrastructure.docker.old.local;

import com.google.inject.AbstractModule;

/**
 * The Module for Local Docker components
 * Note that LocalDockerNodeFactory requires machine.docker.local.project parameter pointed to
 * directory containing workspace projects tree
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 */
public class LocalDockerModule extends AbstractModule {

    @Override
    protected void configure() {
/*
        install(new FactoryModuleBuilder()
                        .implement(Instance.class, DockerInstance.class)
                        .implement(InstanceProcess.class, DockerProcess.class)
                        .implement(DockerNode.class, LocalDockerNode.class)
                        .implement(DockerInstanceRuntimeInfo.class, DockerInstanceRuntimeInfo.class)
                        .build(org.eclipse.che.plugin.docker.machine.DockerMachineFactory.class));

        MapBinder<String, ServerEvaluationStrategy> strategies = MapBinder.newMapBinder(binder(),
                                                                                        String.class,
                                                                                        ServerEvaluationStrategy.class);
        strategies.addBinding("default")
                  .to(org.eclipse.che.plugin.docker.machine.DefaultServerEvaluationStrategy.class);
        strategies.addBinding("docker-local")
                  .to(org.eclipse.che.plugin.docker.machine.LocalDockerServerEvaluationStrategy.class);

        bind(org.eclipse.che.workspace.infrastructure.docker.old.local.node.WorkspaceFolderPathProvider.class)
                .to(org.eclipse.che.workspace.infrastructure.docker.old.provider.LocalWorkspaceFolderPathProvider.class);

        bind(org.eclipse.che.plugin.docker.client.DockerRegistryDynamicAuthResolver.class)
                .to(org.eclipse.che.plugin.docker.client.NoOpDockerRegistryDynamicAuthResolverImpl.class);
        bind(org.eclipse.che.plugin.docker.client.DockerRegistryChecker.class).asEagerSingleton();

        MapBinder<String, DockerConnector> dockerConnectors = MapBinder.newMapBinder(binder(), String.class, DockerConnector.class);
        dockerConnectors.addBinding("default").to(DockerConnector.class);
        dockerConnectors.addBinding("openshift").to(OpenShiftConnector.class);

        Multibinder<String> devMachineEnvVars = Multibinder.newSetBinder(binder(),
                                                                         String.class,
                                                                         Names.named("machine.docker.dev_machine.machine_env"))
                                                           .permitDuplicates();
        devMachineEnvVars.addBinding()
                         .toProvider(org.eclipse.che.workspace.infrastructure.docker.old.local.provider.DockerApiHostEnvVariableProvider.class);

        install(new org.eclipse.che.plugin.docker.machine.DockerMachineModule());

        Multibinder<String> devMachineVolumes = Multibinder.newSetBinder(binder(),
                                                                         String.class,
                                                                         Names.named("machine.docker.dev_machine.machine_volumes"));
        devMachineVolumes.addBinding().toProvider(org.eclipse.che.workspace.infrastructure.docker.old.config.provider.ExtraVolumeProvider.class);

        Multibinder<Set<String>> networks = Multibinder.newSetBinder(binder(),
                                                                     new TypeLiteral<Set<String>>() {},
                                                                     Names.named("machine.docker.networks"));
        networks.addBinding().toProvider(org.eclipse.che.plugin.docker.machine.CheInContainerNetworkProvider.class);

        Multibinder<Set<String>> extraHosts = Multibinder.newSetBinder(binder(),
                                                                       new TypeLiteral<Set<String>>() {},
                                                                       Names.named("che.docker.extra_hosts"))
                                                         .permitDuplicates();
        extraHosts.addBinding()
                  .toProvider(org.eclipse.che.workspace.infrastructure.docker.old.local.provider.CheDockerExtraHostProvider.class);*/
    }
}
