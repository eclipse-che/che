/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.UniqueWorkspacePVCStrategy.UNIQUE_STRATEGY;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarProvider;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.KubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesMachineCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesRuntimeStateCache;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.UniqueWorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspacePVCCleaner;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumeStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesCheApiEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.LogsRootEnvVariableProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.ExternalServerExposerStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.RemoveProjectOnWorkspaceRemove;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftExternalServerExposer;

/** @author Sergii Leshchenko */
public class OpenShiftInfraModule extends AbstractModule {
  @Override
  protected void configure() {
    MapBinder<String, InternalEnvironmentFactory> factories =
        MapBinder.newMapBinder(binder(), String.class, InternalEnvironmentFactory.class);

    factories.addBinding(OpenShiftEnvironment.TYPE).to(OpenShiftEnvironmentFactory.class);
    factories.addBinding(DockerImageEnvironment.TYPE).to(DockerImageEnvironmentFactory.class);

    bind(RuntimeInfrastructure.class).to(OpenShiftInfrastructure.class);

    bind(KubernetesNamespaceFactory.class).to(OpenShiftProjectFactory.class);

    install(new FactoryModuleBuilder().build(OpenShiftRuntimeContextFactory.class));
    install(new FactoryModuleBuilder().build(OpenShiftRuntimeFactory.class));

    install(new FactoryModuleBuilder().build(KubernetesBootstrapperFactory.class));
    bind(WorkspacePVCCleaner.class).asEagerSingleton();
    bind(RemoveProjectOnWorkspaceRemove.class).asEagerSingleton();

    bind(CheApiEnvVarProvider.class).to(KubernetesCheApiEnvVarProvider.class);

    MapBinder<String, WorkspaceVolumesStrategy> volumesStrategies =
        MapBinder.newMapBinder(binder(), String.class, WorkspaceVolumesStrategy.class);
    volumesStrategies.addBinding(COMMON_STRATEGY).to(CommonPVCStrategy.class);
    volumesStrategies.addBinding(UNIQUE_STRATEGY).to(UniqueWorkspacePVCStrategy.class);
    bind(WorkspaceVolumesStrategy.class).toProvider(WorkspaceVolumeStrategyProvider.class);

    bind(new TypeLiteral<ExternalServerExposerStrategy<OpenShiftEnvironment>>() {})
        .to(OpenShiftExternalServerExposer.class);
    bind(ServersConverter.class).to(new TypeLiteral<ServersConverter<OpenShiftEnvironment>>() {});

    Multibinder<EnvVarProvider> envVarProviders =
        Multibinder.newSetBinder(binder(), EnvVarProvider.class);
    envVarProviders.addBinding().to(LogsRootEnvVariableProvider.class);

    bind(KubernetesRuntimeStateCache.class).to(JpaKubernetesRuntimeStateCache.class);
    bind(KubernetesMachineCache.class).to(JpaKubernetesMachineCache.class);
  }
}
