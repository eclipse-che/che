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
package org.eclipse.che.plugin.docker.machine.local;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import org.eclipse.che.api.machine.server.MachineService;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.server.spi.InstanceProcess;
import org.eclipse.che.plugin.docker.machine.DockerInstance;
import org.eclipse.che.plugin.docker.machine.DockerInstanceProvider;
import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;
import org.eclipse.che.plugin.docker.machine.local.provider.CheHostVfsRootDirProvider;
import org.eclipse.che.plugin.docker.machine.local.provider.ExtraVolumeProvider;
import org.eclipse.che.plugin.docker.machine.node.DockerNode;
import org.eclipse.che.plugin.docker.machine.DockerProcess;

import static org.eclipse.che.inject.Matchers.names;

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
        bind(MachineService.class);

        install(new FactoryModuleBuilder()
                        .implement(Instance.class, DockerInstance.class)
                        .implement(InstanceProcess.class, DockerProcess.class)
                        .implement(DockerNode.class, LocalDockerNode.class)
                        .implement(DockerInstanceRuntimeInfo.class,
                                   org.eclipse.che.plugin.docker.machine.local.LocalDockerInstanceRuntimeInfo.class)
                        .build(org.eclipse.che.plugin.docker.machine.DockerMachineFactory.class));


        bind(org.eclipse.che.plugin.docker.machine.node.WorkspaceFolderPathProvider.class)
                .to(org.eclipse.che.plugin.docker.machine.local.node.provider.LocalWorkspaceFolderPathProvider.class);

        bind(String.class).annotatedWith(Names.named("host.workspaces.root"))
                          .toProvider(CheHostVfsRootDirProvider.class);

        bind(org.eclipse.che.plugin.docker.client.DockerRegistryChecker.class).asEagerSingleton();

        Multibinder<String> devMachineEnvVars = Multibinder.newSetBinder(binder(),
                                                                         String.class,
                                                                         Names.named("machine.docker.dev_machine.machine_env"))
                                                           .permitDuplicates();
        devMachineEnvVars.addBinding()
                         .toProvider(org.eclipse.che.plugin.docker.machine.local.provider.DockerApiHostEnvVariableProvider.class);

        install(new org.eclipse.che.plugin.docker.machine.DockerMachineModule());

        org.eclipse.che.plugin.docker.machine.local.interceptor.EnableOfflineDockerMachineBuildInterceptor offlineMachineBuildInterceptor =
                new org.eclipse.che.plugin.docker.machine.local.interceptor.EnableOfflineDockerMachineBuildInterceptor();
        requestInjection(offlineMachineBuildInterceptor);
        bindInterceptor(Matchers.subclassesOf(DockerInstanceProvider.class), names("buildImage"), offlineMachineBuildInterceptor);

        Multibinder<String> devMachineVolumes = Multibinder.newSetBinder(binder(),
                                                                         String.class,
                                                                         Names.named("machine.docker.dev_machine.machine_volumes"));
        devMachineVolumes.addBinding().toProvider(org.eclipse.che.plugin.docker.machine.local.provider.ExtraVolumeProvider.class);
    }
}
