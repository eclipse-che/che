/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.UniqueWorkspacePVCStrategy.UNIQUE_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.DefaultHostIngressExternalServerExposer.DEFAULT_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostIngressExternalServerExposer.MULTI_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostIngressExternalServerExposer.SINGLE_HOST_STRATEGY;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import java.util.Map;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiExternalEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiInternalEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarProvider;
import org.eclipse.che.api.workspace.server.wsnext.WorkspaceNextApplier;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.bootstrapper.KubernetesBootstrapperFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesRuntimeCacheModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.RemoveNamespaceOnWorkspaceRemove;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.UniqueWorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspacePVCCleaner;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumeStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesCheApiExternalEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesCheApiInternalEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.LogsRootEnvVariableProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.IngressAnnotationsProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.DefaultHostIngressExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostIngressExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostIngressExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.DefaultSecureServersFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsnext.KubernetesWorkspaceNextApplier;

/** @author Sergii Leshchenko */
public class KubernetesInfraModule extends AbstractModule {
  @Override
  protected void configure() {
    MapBinder<String, InternalEnvironmentFactory> factories =
        MapBinder.newMapBinder(binder(), String.class, InternalEnvironmentFactory.class);

    factories.addBinding(KubernetesEnvironment.TYPE).to(KubernetesEnvironmentFactory.class);
    factories.addBinding(DockerImageEnvironment.TYPE).to(DockerImageEnvironmentFactory.class);

    bind(RuntimeInfrastructure.class).to(KubernetesInfrastructure.class);

    install(new FactoryModuleBuilder().build(KubernetesRuntimeContextFactory.class));

    install(new FactoryModuleBuilder().build(KubernetesRuntimeFactory.class));
    install(new FactoryModuleBuilder().build(KubernetesBootstrapperFactory.class));
    install(new FactoryModuleBuilder().build(StartSynchronizerFactory.class));

    bind(WorkspacePVCCleaner.class).asEagerSingleton();
    bind(RemoveNamespaceOnWorkspaceRemove.class).asEagerSingleton();

    bind(CheApiInternalEnvVarProvider.class).to(KubernetesCheApiInternalEnvVarProvider.class);
    bind(CheApiExternalEnvVarProvider.class).to(KubernetesCheApiExternalEnvVarProvider.class);

    MapBinder<String, WorkspaceVolumesStrategy> volumesStrategies =
        MapBinder.newMapBinder(binder(), String.class, WorkspaceVolumesStrategy.class);
    volumesStrategies.addBinding(COMMON_STRATEGY).to(CommonPVCStrategy.class);
    volumesStrategies.addBinding(UNIQUE_STRATEGY).to(UniqueWorkspacePVCStrategy.class);
    bind(WorkspaceVolumesStrategy.class).toProvider(WorkspaceVolumeStrategyProvider.class);

    Multibinder.newSetBinder(binder(), ServiceTermination.class)
        .addBinding()
        .to(KubernetesClientTermination.class);

    MapBinder<String, ExternalServerExposerStrategy<KubernetesEnvironment>> ingressStrategies =
        MapBinder.newMapBinder(
            binder(),
            new TypeLiteral<String>() {},
            new TypeLiteral<ExternalServerExposerStrategy<KubernetesEnvironment>>() {});
    ingressStrategies
        .addBinding(MULTI_HOST_STRATEGY)
        .to(MultiHostIngressExternalServerExposer.class);
    ingressStrategies
        .addBinding(SINGLE_HOST_STRATEGY)
        .to(SingleHostIngressExternalServerExposer.class);
    ingressStrategies
        .addBinding(DEFAULT_HOST_STRATEGY)
        .to(DefaultHostIngressExternalServerExposer.class);
    bind(new TypeLiteral<ExternalServerExposerStrategy<KubernetesEnvironment>>() {})
        .toProvider(
            new TypeLiteral<ExternalServerExposerStrategyProvider<KubernetesEnvironment>>() {});

    bind(ServersConverter.class).to(new TypeLiteral<ServersConverter<KubernetesEnvironment>>() {});

    Multibinder<EnvVarProvider> envVarProviders =
        Multibinder.newSetBinder(binder(), EnvVarProvider.class);
    envVarProviders.addBinding().to(LogsRootEnvVariableProvider.class);

    bind(new TypeLiteral<Map<String, String>>() {})
        .annotatedWith(com.google.inject.name.Names.named("infra.kubernetes.ingress.annotations"))
        .toProvider(IngressAnnotationsProvider.class);

    install(new JpaKubernetesRuntimeCacheModule());

    MapBinder<String, WorkspaceNextApplier> wsNext =
        MapBinder.newMapBinder(binder(), String.class, WorkspaceNextApplier.class);
    wsNext.addBinding(KubernetesEnvironment.TYPE).to(KubernetesWorkspaceNextApplier.class);

    bind(new TypeLiteral<SecureServerExposerFactory<KubernetesEnvironment>>() {})
        .toProvider(
            new TypeLiteral<SecureServerExposerFactoryProvider<KubernetesEnvironment>>() {});

    MapBinder<String, SecureServerExposerFactory<KubernetesEnvironment>>
        secureServerExposerFactories =
            MapBinder.newMapBinder(
                binder(),
                new TypeLiteral<String>() {},
                new TypeLiteral<SecureServerExposerFactory<KubernetesEnvironment>>() {});

    secureServerExposerFactories
        .addBinding("default")
        .to(new TypeLiteral<DefaultSecureServersFactory<KubernetesEnvironment>>() {});
  }
}
