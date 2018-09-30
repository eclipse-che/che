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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ImagePullSecretProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.IngressTlsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.InstallerServersPortProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PodTerminationGracePeriodProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ProxySettingsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecurityContextProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ServiceAccountProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.limits.ram.RamLimitRequestProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.ServersConverter;

/**
 * Applies the set of configurations to the Kubernetes environment and environment configuration
 * with the desired order, which corresponds to the needs of the Kubernetes infrastructure.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
public interface KubernetesEnvironmentProvisioner<T extends KubernetesEnvironment> {

  void provision(T k8sEnv, RuntimeIdentity identity) throws InfrastructureException;

  @Singleton
  class KubernetesEnvironmentProvisionerImpl
      implements KubernetesEnvironmentProvisioner<KubernetesEnvironment> {

    private boolean pvcEnabled;
    private WorkspaceVolumesStrategy volumesStrategy;
    private UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner;
    private ServersConverter<KubernetesEnvironment> serversConverter;
    private EnvVarsConverter envVarsConverter;
    private RestartPolicyRewriter restartPolicyRewriter;
    private RamLimitRequestProvisioner ramLimitProvisioner;
    private InstallerServersPortProvisioner installerServersPortProvisioner;
    private LogsVolumeMachineProvisioner logsVolumeMachineProvisioner;
    private SecurityContextProvisioner securityContextProvisioner;
    private PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner;
    private IngressTlsProvisioner externalServerIngressTlsProvisioner;
    private ImagePullSecretProvisioner imagePullSecretProvisioner;
    private ProxySettingsProvisioner proxySettingsProvisioner;
    private ServiceAccountProvisioner serviceAccountProvisioner;

    @Inject
    public KubernetesEnvironmentProvisionerImpl(
        @Named("che.infra.kubernetes.pvc.enabled") boolean pvcEnabled,
        UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner,
        ServersConverter<KubernetesEnvironment> serversConverter,
        EnvVarsConverter envVarsConverter,
        RestartPolicyRewriter restartPolicyRewriter,
        WorkspaceVolumesStrategy volumesStrategy,
        RamLimitRequestProvisioner ramLimitProvisioner,
        InstallerServersPortProvisioner installerServersPortProvisioner,
        LogsVolumeMachineProvisioner logsVolumeMachineProvisioner,
        SecurityContextProvisioner securityContextProvisioner,
        PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner,
        IngressTlsProvisioner externalServerIngressTlsProvisioner,
        ImagePullSecretProvisioner imagePullSecretProvisioner,
        ProxySettingsProvisioner proxySettingsProvisioner,
        ServiceAccountProvisioner serviceAccountProvisioner) {
      this.pvcEnabled = pvcEnabled;
      this.volumesStrategy = volumesStrategy;
      this.uniqueNamesProvisioner = uniqueNamesProvisioner;
      this.serversConverter = serversConverter;
      this.envVarsConverter = envVarsConverter;
      this.restartPolicyRewriter = restartPolicyRewriter;
      this.ramLimitProvisioner = ramLimitProvisioner;
      this.installerServersPortProvisioner = installerServersPortProvisioner;
      this.logsVolumeMachineProvisioner = logsVolumeMachineProvisioner;
      this.securityContextProvisioner = securityContextProvisioner;
      this.podTerminationGracePeriodProvisioner = podTerminationGracePeriodProvisioner;
      this.externalServerIngressTlsProvisioner = externalServerIngressTlsProvisioner;
      this.imagePullSecretProvisioner = imagePullSecretProvisioner;
      this.proxySettingsProvisioner = proxySettingsProvisioner;
      this.serviceAccountProvisioner = serviceAccountProvisioner;
    }

    public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
        throws InfrastructureException {
      // 1 stage - update environment according Infrastructure specific
      installerServersPortProvisioner.provision(k8sEnv, identity);
      if (pvcEnabled) {
        logsVolumeMachineProvisioner.provision(k8sEnv, identity);
      }

      // 2 stage - converting Che model env to Kubernetes env
      serversConverter.provision(k8sEnv, identity);
      envVarsConverter.provision(k8sEnv, identity);
      if (pvcEnabled) {
        volumesStrategy.provision(k8sEnv, identity);
      }

      // 3 stage - add Kubernetes env items
      restartPolicyRewriter.provision(k8sEnv, identity);
      uniqueNamesProvisioner.provision(k8sEnv, identity);
      ramLimitProvisioner.provision(k8sEnv, identity);
      externalServerIngressTlsProvisioner.provision(k8sEnv, identity);
      securityContextProvisioner.provision(k8sEnv, identity);
      podTerminationGracePeriodProvisioner.provision(k8sEnv, identity);
      imagePullSecretProvisioner.provision(k8sEnv, identity);
      proxySettingsProvisioner.provision(k8sEnv, identity);
      serviceAccountProvisioner.provision(k8sEnv, identity);
    }
  }
}
