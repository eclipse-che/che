/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift;

import static org.eclipse.che.api.workspace.server.devfile.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.COMMON_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PerWorkspacePVCStrategy.PER_WORKSPACE_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.UniqueWorkspacePVCStrategy.UNIQUE_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy.SINGLE_HOST_STRATEGY;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
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
import org.eclipse.che.workspace.infrastructure.kubernetes.AsyncStorageModeValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.InconsistentRuntimesDetector;
import org.eclipse.che.workspace.infrastructure.kubernetes.K8sInfraNamespaceWsAttributeValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientTermination;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.StartSynchronizerFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.KubernetesNamespaceService;
import org.eclipse.che.workspace.infrastructure.kubernetes.cache.jpa.JpaKubernetesRuntimeCacheModule;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.DockerimageComponentToWorkspaceApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesComponentToWorkspaceApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesComponentValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesDevfileBindings;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.PerWorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.UniqueWorkspacePVCStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspacePVCCleaner;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumeStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStoragePodInterceptor;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStoragePodWatcher;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GatewayTlsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesCheApiExternalEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesCheApiInternalEnvVarProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.LogsRootEnvVariableProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.PreviewUrlExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayServerExposer;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.SingleHostExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.CookiePathStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.NonTlsDistributedClusterModeNotifier;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.KubernetesPluginsToolingApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.PluginBrokerManager;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.SidecarToolingProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases.BrokerEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events.BrokerService;
import org.eclipse.che.workspace.infrastructure.openshift.devfile.OpenshiftComponentToWorkspaceApplier;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.RemoveProjectOnWorkspaceRemove;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftPreviewUrlCommandProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenshiftTrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.RouteTlsProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftCookiePathStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftPreviewUrlExposer;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftServerExposureStrategy;
import org.eclipse.che.workspace.infrastructure.openshift.server.RouteServerExposer;
import org.eclipse.che.workspace.infrastructure.openshift.server.external.OpenShiftExternalServerExposerProvider;
import org.eclipse.che.workspace.infrastructure.openshift.wsplugins.brokerphases.OpenshiftBrokerEnvironmentFactory;

/** @author Sergii Leshchenko */
public class OpenShiftInfraModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<WorkspaceAttributeValidator> workspaceAttributeValidators =
        Multibinder.newSetBinder(binder(), WorkspaceAttributeValidator.class);
    workspaceAttributeValidators.addBinding().to(K8sInfraNamespaceWsAttributeValidator.class);
    workspaceAttributeValidators.addBinding().to(AsyncStorageModeValidator.class);

    bind(KubernetesNamespaceService.class);

    MapBinder<String, InternalEnvironmentFactory> factories =
        MapBinder.newMapBinder(binder(), String.class, InternalEnvironmentFactory.class);

    factories.addBinding(OpenShiftEnvironment.TYPE).to(OpenShiftEnvironmentFactory.class);
    factories.addBinding(KubernetesEnvironment.TYPE).to(KubernetesEnvironmentFactory.class);
    factories.addBinding(Constants.NO_ENVIRONMENT_RECIPE_TYPE).to(NoEnvironmentFactory.class);

    bind(InconsistentRuntimesDetector.class).asEagerSingleton();
    bind(RuntimeInfrastructure.class).to(OpenShiftInfrastructure.class);

    bind(KubernetesNamespaceFactory.class).to(OpenShiftProjectFactory.class);
    bind(KubernetesClientFactory.class).to(OpenShiftClientFactory.class);

    install(new FactoryModuleBuilder().build(OpenShiftRuntimeContextFactory.class));
    install(new FactoryModuleBuilder().build(OpenShiftRuntimeFactory.class));
    install(new FactoryModuleBuilder().build(StartSynchronizerFactory.class));

    bind(WorkspacePVCCleaner.class).asEagerSingleton();
    bind(RemoveProjectOnWorkspaceRemove.class).asEagerSingleton();

    bind(TrustedCAProvisioner.class).to(OpenshiftTrustedCAProvisioner.class);

    bind(CheApiInternalEnvVarProvider.class).to(KubernetesCheApiInternalEnvVarProvider.class);
    bind(CheApiExternalEnvVarProvider.class).to(KubernetesCheApiExternalEnvVarProvider.class);

    MapBinder<String, WorkspaceVolumesStrategy> volumesStrategies =
        MapBinder.newMapBinder(binder(), String.class, WorkspaceVolumesStrategy.class);
    volumesStrategies.addBinding(COMMON_STRATEGY).to(CommonPVCStrategy.class);
    volumesStrategies.addBinding(PER_WORKSPACE_STRATEGY).to(PerWorkspacePVCStrategy.class);
    volumesStrategies.addBinding(UNIQUE_STRATEGY).to(UniqueWorkspacePVCStrategy.class);
    bind(WorkspaceVolumesStrategy.class).toProvider(WorkspaceVolumeStrategyProvider.class);

    MapBinder<WorkspaceExposureType, ExternalServerExposer<OpenShiftEnvironment>>
        exposureStrategies =
            MapBinder.newMapBinder(binder(), new TypeLiteral<>() {}, new TypeLiteral<>() {});
    exposureStrategies.addBinding(WorkspaceExposureType.NATIVE).to(RouteServerExposer.class);
    exposureStrategies
        .addBinding(WorkspaceExposureType.GATEWAY)
        .to(new TypeLiteral<GatewayServerExposer<OpenShiftEnvironment>>() {});

    bind(new TypeLiteral<ExternalServerExposer<OpenShiftEnvironment>>() {})
        .annotatedWith(com.google.inject.name.Names.named("multihost-exposer"))
        .to(RouteServerExposer.class);

    bind(new TypeLiteral<ExternalServerExposerProvider<OpenShiftEnvironment>>() {})
        .to(OpenShiftExternalServerExposerProvider.class);

    bind(ServersConverter.class).to(new TypeLiteral<ServersConverter<OpenShiftEnvironment>>() {});
    bind(PreviewUrlExposer.class).to(new TypeLiteral<OpenShiftPreviewUrlExposer>() {});
    bind(PreviewUrlCommandProvisioner.class)
        .to(new TypeLiteral<OpenShiftPreviewUrlCommandProvisioner>() {});

    Multibinder<EnvVarProvider> envVarProviders =
        Multibinder.newSetBinder(binder(), EnvVarProvider.class);
    envVarProviders.addBinding().to(LogsRootEnvVariableProvider.class);

    install(new JpaKubernetesRuntimeCacheModule());

    Multibinder.newSetBinder(binder(), ServiceTermination.class)
        .addBinding()
        .to(KubernetesClientTermination.class);

    MapBinder<String, ChePluginsApplier> pluginsAppliers =
        MapBinder.newMapBinder(binder(), String.class, ChePluginsApplier.class);
    pluginsAppliers.addBinding(OpenShiftEnvironment.TYPE).to(KubernetesPluginsToolingApplier.class);

    bind(SecureServerExposerFactoryProvider.class)
        .to(new TypeLiteral<SecureServerExposerFactoryProvider<OpenShiftEnvironment>>() {});

    bind(BrokerService.class);

    bind(new TypeLiteral<BrokerEnvironmentFactory<OpenShiftEnvironment>>() {})
        .to(OpenshiftBrokerEnvironmentFactory.class);

    bind(PluginBrokerManager.class)
        .to(new TypeLiteral<PluginBrokerManager<OpenShiftEnvironment>>() {});

    bind(SidecarToolingProvisioner.class)
        .to(new TypeLiteral<SidecarToolingProvisioner<OpenShiftEnvironment>>() {});

    MapBinder<WorkspaceExposureType, TlsProvisioner<OpenShiftEnvironment>> tlsProvisioners =
        MapBinder.newMapBinder(
            binder(),
            new TypeLiteral<WorkspaceExposureType>() {},
            new TypeLiteral<TlsProvisioner<OpenShiftEnvironment>>() {});
    tlsProvisioners
        .addBinding(WorkspaceExposureType.GATEWAY)
        .to(new TypeLiteral<GatewayTlsProvisioner<OpenShiftEnvironment>>() {});
    tlsProvisioners.addBinding(WorkspaceExposureType.NATIVE).to(RouteTlsProvisioner.class);

    bind(new TypeLiteral<KubernetesEnvironmentProvisioner<OpenShiftEnvironment>>() {})
        .to(OpenShiftEnvironmentProvisioner.class);

    DevfileBindings.onComponentIntegrityValidatorBinder(
        binder(),
        binder -> {
          binder.addBinding(KUBERNETES_COMPONENT_TYPE).to(KubernetesComponentValidator.class);
          binder.addBinding(OPENSHIFT_COMPONENT_TYPE).to(KubernetesComponentValidator.class);
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
          binder
              .addBinding(OPENSHIFT_COMPONENT_TYPE)
              .to(OpenshiftComponentToWorkspaceApplier.class);
        });

    KubernetesDevfileBindings.addKubernetesBasedEnvironmentTypeBindings(
        binder(), KubernetesEnvironment.TYPE, OpenShiftEnvironment.TYPE);

    KubernetesDevfileBindings.addKubernetesBasedComponentTypeBindings(
        binder(), KUBERNETES_COMPONENT_TYPE, OPENSHIFT_COMPONENT_TYPE);

    KubernetesDevfileBindings.addAllowedEnvironmentTypeUpgradeBindings(
        binder(), OpenShiftEnvironment.TYPE, KubernetesEnvironment.TYPE);

    MapBinder<String, ExternalServiceExposureStrategy> ingressStrategies =
        MapBinder.newMapBinder(binder(), String.class, ExternalServiceExposureStrategy.class);
    ingressStrategies.addBinding(MULTI_HOST_STRATEGY).to(OpenShiftServerExposureStrategy.class);
    ingressStrategies
        .addBinding(SINGLE_HOST_STRATEGY)
        .to(SingleHostExternalServiceExposureStrategy.class);
    bind(ExternalServiceExposureStrategy.class).toProvider(ServiceExposureStrategyProvider.class);
    bind(CookiePathStrategy.class).to(OpenShiftCookiePathStrategy.class);
    bind(NonTlsDistributedClusterModeNotifier.class);
    bind(AsyncStorageProvisioner.class);
    bind(AsyncStoragePodInterceptor.class);
    bind(AsyncStoragePodWatcher.class);
  }
}
