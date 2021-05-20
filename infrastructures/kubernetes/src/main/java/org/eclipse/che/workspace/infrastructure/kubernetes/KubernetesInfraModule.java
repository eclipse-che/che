/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static com.google.inject.name.Names.named;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PerWorkspacePVCStrategy.PER_WORKSPACE_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.UniqueWorkspacePVCStrategy.UNIQUE_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.DefaultHostExternalServiceExposureStrategy.DEFAULT_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy.SINGLE_HOST_STRATEGY;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import java.util.Map;
import org.eclipse.che.api.system.server.ServiceTermination;
import org.eclipse.che.api.workspace.server.NoEnvironmentFactory;
import org.eclipse.che.api.workspace.server.WorkspaceAttributeValidator;
import org.eclipse.che.api.workspace.server.devfile.DevfileBindings;
import org.eclipse.che.api.workspace.server.devfile.validator.ComponentIntegrityValidator.NoopComponentIntegrityValidator;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiExternalEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.CheApiInternalEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.EnvVarProvider;
import org.eclipse.che.api.workspace.server.wsplugins.ChePluginsApplier;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.KubernetesNamespaceService;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesRuntimeCacheModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.DockerimageComponentToWorkspaceApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesComponentToWorkspaceApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesComponentValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesDevfileBindings;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.RemoveNamespaceOnWorkspaceRemove;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PerWorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.UniqueWorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspacePVCCleaner;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumeStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GatewayTlsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.IngressTlsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesCheApiExternalEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesCheApiInternalEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesPreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesTrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.LogsRootEnvVariableProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.IngressAnnotationsProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.PreviewUrlExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.DefaultHostExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.KubernetesExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultihostIngressServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.NonTlsDistributedClusterModeNotifier;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesPluginsToolingApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.PluginBrokerManager;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.KubernetesBrokerEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerService;

/** @author Sergii Leshchenko */
public class KubernetesInfraModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<WorkspaceAttributeValidator> workspaceAttributeValidators =
        Multibinder.newSetBinder(binder(), WorkspaceAttributeValidator.class);
    workspaceAttributeValidators.addBinding().to(K8sInfraNamespaceWsAttributeValidator.class);
    workspaceAttributeValidators.addBinding().to(AsyncStorageModeValidator.class);

    bind(KubernetesNamespaceService.class);

    MapBinder<String, InternalEnvironmentFactory> factories =
        MapBinder.newMapBinder(binder(), String.class, InternalEnvironmentFactory.class);

    factories.addBinding(KubernetesEnvironment.TYPE).to(KubernetesEnvironmentFactory.class);
    factories.addBinding(Constants.NO_ENVIRONMENT_RECIPE_TYPE).to(NoEnvironmentFactory.class);

    bind(RuntimeInfrastructure.class).to(KubernetesInfrastructure.class);
    bind(InconsistentRuntimesDetector.class).asEagerSingleton();

    bind(TrustedCAProvisioner.class).to(KubernetesTrustedCAProvisioner.class);

    MapBinder<WorkspaceExposureType, TlsProvisioner<KubernetesEnvironment>> tlsProvisioners =
        MapBinder.newMapBinder(
            binder(),
            new TypeLiteral<WorkspaceExposureType>() {},
            new TypeLiteral<TlsProvisioner<KubernetesEnvironment>>() {});
    tlsProvisioners
        .addBinding(WorkspaceExposureType.GATEWAY)
        .to(new TypeLiteral<GatewayTlsProvisioner<KubernetesEnvironment>>() {});
    tlsProvisioners.addBinding(WorkspaceExposureType.NATIVE).to(IngressTlsProvisioner.class);

    bind(new TypeLiteral<KubernetesEnvironmentProvisioner<KubernetesEnvironment>>() {})
        .to(KubernetesEnvironmentProvisioner.KubernetesEnvironmentProvisionerImpl.class);

    install(new FactoryModuleBuilder().build(KubernetesRuntimeContextFactory.class));

    install(
        new FactoryModuleBuilder()
            .build(new TypeLiteral<KubernetesRuntimeFactory<KubernetesEnvironment>>() {}));
    install(new FactoryModuleBuilder().build(StartSynchronizerFactory.class));

    bind(WorkspacePVCCleaner.class).asEagerSingleton();
    bind(RemoveNamespaceOnWorkspaceRemove.class).asEagerSingleton();

    bind(CheApiInternalEnvVarProvider.class).to(KubernetesCheApiInternalEnvVarProvider.class);
    bind(CheApiExternalEnvVarProvider.class).to(KubernetesCheApiExternalEnvVarProvider.class);

    MapBinder<String, WorkspaceVolumesStrategy> volumesStrategies =
        MapBinder.newMapBinder(binder(), String.class, WorkspaceVolumesStrategy.class);
    volumesStrategies.addBinding(COMMON_STRATEGY).to(CommonPVCStrategy.class);
    volumesStrategies.addBinding(PER_WORKSPACE_STRATEGY).to(PerWorkspacePVCStrategy.class);
    volumesStrategies.addBinding(UNIQUE_STRATEGY).to(UniqueWorkspacePVCStrategy.class);
    bind(WorkspaceVolumesStrategy.class).toProvider(WorkspaceVolumeStrategyProvider.class);

    Multibinder.newSetBinder(binder(), ServiceTermination.class)
        .addBinding()
        .to(KubernetesClientTermination.class);

    MapBinder<String, ExternalServiceExposureStrategy> ingressStrategies =
        MapBinder.newMapBinder(binder(), String.class, ExternalServiceExposureStrategy.class);
    ingressStrategies
        .addBinding(MULTI_HOST_STRATEGY)
        .to(MultiHostExternalServiceExposureStrategy.class);
    ingressStrategies
        .addBinding(SINGLE_HOST_STRATEGY)
        .to(SingleHostExternalServiceExposureStrategy.class);
    ingressStrategies
        .addBinding(DEFAULT_HOST_STRATEGY)
        .to(DefaultHostExternalServiceExposureStrategy.class);
    bind(ExternalServiceExposureStrategy.class).toProvider(ServiceExposureStrategyProvider.class);

    MapBinder<WorkspaceExposureType, ExternalServerExposer<KubernetesEnvironment>>
        exposureStrategies =
            MapBinder.newMapBinder(binder(), new TypeLiteral<>() {}, new TypeLiteral<>() {});
    exposureStrategies
        .addBinding(WorkspaceExposureType.NATIVE)
        .to(new TypeLiteral<IngressServerExposer<KubernetesEnvironment>>() {});
    exposureStrategies
        .addBinding(WorkspaceExposureType.GATEWAY)
        .to(new TypeLiteral<GatewayServerExposer<KubernetesEnvironment>>() {});

    bind(new TypeLiteral<ExternalServerExposer<KubernetesEnvironment>>() {})
        .annotatedWith(com.google.inject.name.Names.named("multihost-exposer"))
        .to(new TypeLiteral<MultihostIngressServerExposer<KubernetesEnvironment>>() {});

    bind(new TypeLiteral<ExternalServerExposerProvider<KubernetesEnvironment>>() {})
        .to(new TypeLiteral<KubernetesExternalServerExposerProvider<KubernetesEnvironment>>() {});

    bind(ServersConverter.class).to(new TypeLiteral<ServersConverter<KubernetesEnvironment>>() {});
    bind(PreviewUrlExposer.class)
        .to(new TypeLiteral<PreviewUrlExposer<KubernetesEnvironment>>() {});
    bind(PreviewUrlCommandProvisioner.class)
        .to(new TypeLiteral<KubernetesPreviewUrlCommandProvisioner>() {});

    Multibinder<EnvVarProvider> envVarProviders =
        Multibinder.newSetBinder(binder(), EnvVarProvider.class);
    envVarProviders.addBinding().to(LogsRootEnvVariableProvider.class);

    bind(new TypeLiteral<Map<String, String>>() {})
        .annotatedWith(named("infra.kubernetes.ingress.annotations"))
        .toProvider(IngressAnnotationsProvider.class);

    install(new JpaKubernetesRuntimeCacheModule());

    bind(SecureServerExposerFactoryProvider.class)
        .to(new TypeLiteral<SecureServerExposerFactoryProvider<KubernetesEnvironment>>() {});

    MapBinder<String, ChePluginsApplier> chePluginsAppliers =
        MapBinder.newMapBinder(binder(), String.class, ChePluginsApplier.class);
    chePluginsAppliers
        .addBinding(KubernetesEnvironment.TYPE)
        .to(KubernetesPluginsToolingApplier.class);

    bind(BrokerService.class);

    bind(new TypeLiteral<BrokerEnvironmentFactory<KubernetesEnvironment>>() {})
        .to(KubernetesBrokerEnvironmentFactory.class);

    bind(PluginBrokerManager.class)
        .to(new TypeLiteral<PluginBrokerManager<KubernetesEnvironment>>() {});

    bind(SidecarToolingProvisioner.class)
        .to(new TypeLiteral<SidecarToolingProvisioner<KubernetesEnvironment>>() {});

    DevfileBindings.onComponentIntegrityValidatorBinder(
        binder(),
        binder -> {
          binder.addBinding(KUBERNETES_COMPONENT_TYPE).to(KubernetesComponentValidator.class);
          binder.addBinding(DOCKERIMAGE_COMPONENT_TYPE).to(NoopComponentIntegrityValidator.class);
        });

    DevfileBindings.onWorkspaceApplierBinder(
        binder(),
        binder -> {
          binder
              .addBinding(KUBERNETES_COMPONENT_TYPE)
              .to(KubernetesComponentToWorkspaceApplier.class);
          binder
              .addBinding(DOCKERIMAGE_COMPONENT_TYPE)
              .to(DockerimageComponentToWorkspaceApplier.class);
        });

    KubernetesDevfileBindings.addKubernetesBasedEnvironmentTypeBindings(
        binder(), KubernetesEnvironment.TYPE);
    KubernetesDevfileBindings.addKubernetesBasedComponentTypeBindings(
        binder(), KUBERNETES_COMPONENT_TYPE);

    // We need to initialize the bindings somehow. Because no other environment type is upgradable
    // to kubernetes, we just call this in a way that initializes the binding with an empty map.
    KubernetesDevfileBindings.addAllowedEnvironmentTypeUpgradeBindings(
        binder(), KubernetesEnvironment.TYPE);

    bind(NonTlsDistributedClusterModeNotifier.class);
    bind(AsyncStorageProvisioner.class);
  }
}
