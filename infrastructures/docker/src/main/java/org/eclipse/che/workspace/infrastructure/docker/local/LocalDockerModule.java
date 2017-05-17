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
package org.eclipse.che.workspace.infrastructure.docker.local;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.workspace.infrastructure.docker.InfrastructureProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.CheDockerExtraHostProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.CheInContainerNetworkProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.DockerApiHostEnvVariableProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.LocalWorkspaceFolderPathProvider;
import org.eclipse.che.workspace.infrastructure.docker.local.providers.WorkspaceFolderPathProvider;
import org.eclipse.che.workspace.infrastructure.docker.strategy.DefaultServerEvaluationStrategy;
import org.eclipse.che.workspace.infrastructure.docker.strategy.ServerEvaluationStrategy;

import java.util.Set;

/**
 * @author Alexander Garagatyi
 */
public class LocalDockerModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder<Set<String>> networks = Multibinder.newSetBinder(binder(),
                                                                     new TypeLiteral<Set<String>>() {},
                                                                     Names.named("machine.docker.networks"));
        networks.addBinding().toProvider(CheInContainerNetworkProvider.class);

        bind(WorkspaceFilesCleaner.class).to(LocalWorkspaceFilesCleaner.class);
        bind(InfrastructureProvisioner.class).to(LocalCheInfrastructureProvisioner.class);
        bind(WorkspaceFolderPathProvider.class).to(LocalWorkspaceFolderPathProvider.class);

        Multibinder<Set<String>> extraHosts = Multibinder.newSetBinder(binder(),
                                                                       new TypeLiteral<Set<String>>() {},
                                                                       Names.named("che.docker.extra_hosts"))
                                                         .permitDuplicates();
        extraHosts.addBinding().toProvider(CheDockerExtraHostProvider.class);

        Multibinder<String> devMachineEnvVars =
                Multibinder.newSetBinder(binder(),
                                         String.class,
                                         Names.named("machine.docker.dev_machine.machine_env"));
        devMachineEnvVars.addBinding().toProvider(DockerApiHostEnvVariableProvider.class);

        MapBinder<String, DockerConnector> dockerConnectors =
                MapBinder.newMapBinder(binder(), String.class, DockerConnector.class);
        dockerConnectors.addBinding("default").to(DockerConnector.class);

        bind(org.eclipse.che.plugin.docker.client.DockerRegistryChecker.class).asEagerSingleton();
        MapBinder<String, ServerEvaluationStrategy> strategies =
                MapBinder.newMapBinder(binder(), String.class, ServerEvaluationStrategy.class);
        strategies.addBinding("docker-local").to(LocalDockerServerEvaluationStrategy.class);
    }
}
