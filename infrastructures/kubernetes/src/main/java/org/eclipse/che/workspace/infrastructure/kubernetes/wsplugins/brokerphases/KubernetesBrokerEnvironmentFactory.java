/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.brokerphases;

import com.google.common.annotations.Beta;
import java.util.Collection;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.provision.env.AgentAuthEnableEnvVarProvider;
import org.eclipse.che.api.workspace.server.spi.provision.env.MachineTokenEnvVarProvider;
import org.eclipse.che.api.workspace.server.wsplugins.model.PluginFQN;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesTrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TrustedCAProvisioner;

/**
 * Extends {@link BrokerEnvironmentFactory} to be used in the kubernetes infrastructure.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksandr Garagatyi
 */
@Beta
public class KubernetesBrokerEnvironmentFactory
    extends BrokerEnvironmentFactory<KubernetesEnvironment> {

  private final TrustedCAProvisioner trustedCAProvisioner;

  @Inject
  public KubernetesBrokerEnvironmentFactory(
      @Named("che.websocket.endpoint") String cheWebsocketEndpoint,
      @Named("che.workspace.plugin_broker.pull_policy") String brokerPullPolicy,
      @Named("che.infra.kubernetes.trusted_ca_bundles_mount_path") String certificateMountPath,
      AgentAuthEnableEnvVarProvider authEnableEnvVarProvider,
      MachineTokenEnvVarProvider machineTokenEnvVarProvider,
      @Named("che.workspace.plugin_broker.artifacts.image") String artifactsBrokerImage,
      @Named("che.workspace.plugin_broker.metadata.image") String metadataBrokerImage,
      @Nullable @Named("che.workspace.plugin_registry_url") String pluginRegistryUrl,
      @Nullable @Named("che.workspace.plugin_registry_internal_url")
              String pluginRegistryInternalUrl,
      KubernetesTrustedCAProvisioner trustedCAProvisioner,
      CertificateProvisioner certProvisioner) {
    super(
        cheWebsocketEndpoint,
        brokerPullPolicy,
        authEnableEnvVarProvider,
        machineTokenEnvVarProvider,
        artifactsBrokerImage,
        metadataBrokerImage,
        pluginRegistryUrl,
        pluginRegistryInternalUrl,
        trustedCAProvisioner,
        certificateMountPath,
        certProvisioner);
    this.trustedCAProvisioner = trustedCAProvisioner;
  }

  @Override
  protected KubernetesEnvironment doCreate(BrokersConfigs brokersConfigs) {
    return KubernetesEnvironment.builder()
        .setConfigMaps(brokersConfigs.configMaps)
        .setMachines(brokersConfigs.machines)
        .setPods(brokersConfigs.pods)
        .build();
  }

  @Override
  public KubernetesEnvironment createForMetadataBroker(
      Collection<PluginFQN> pluginFQNs, RuntimeIdentity runtimeID, boolean mergePlugins)
      throws InfrastructureException {
    KubernetesEnvironment kubernetesEnvironment =
        super.createForMetadataBroker(pluginFQNs, runtimeID, mergePlugins);
    // trustedCAProvisioner.provision(kubernetesEnvironment, runtimeID);
    return kubernetesEnvironment;
  }
}
