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
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecurityContextProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.limits.ram.RamLimitProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.ServersConverter;

/**
 * Applies the set of configurations to the Kubernetes environment and environment configuration
 * with the desired order, which corresponds to the needs of the Kubernetes infrastructure.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
@Singleton
public class KubernetesEnvironmentProvisioner {

  private final boolean pvcEnabled;
  private final WorkspaceVolumesStrategy volumesStrategy;
  private final UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner;
  private final ServersConverter<KubernetesEnvironment> serversConverter;
  private final EnvVarsConverter envVarsConverter;
  private final RestartPolicyRewriter restartPolicyRewriter;
  private final RamLimitProvisioner ramLimitProvisioner;
  private final InstallerServersPortProvisioner installerServersPortProvisioner;
  private final LogsVolumeMachineProvisioner logsVolumeMachineProvisioner;
  private final SecurityContextProvisioner securityContextProvisioner;
  private final PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner;
  private final IngressTlsProvisioner externalServerIngressTlsProvisioner;
  private final ImagePullSecretProvisioner imagePullSecretProvisioner;

  @Inject
  public KubernetesEnvironmentProvisioner(
      @Named("che.infra.kubernetes.pvc.enabled") boolean pvcEnabled,
      UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner,
      ServersConverter<KubernetesEnvironment> serversConverter,
      EnvVarsConverter envVarsConverter,
      RestartPolicyRewriter restartPolicyRewriter,
      WorkspaceVolumesStrategy volumesStrategy,
      RamLimitProvisioner ramLimitProvisioner,
      InstallerServersPortProvisioner installerServersPortProvisioner,
      LogsVolumeMachineProvisioner logsVolumeMachineProvisioner,
      SecurityContextProvisioner securityContextProvisioner,
      PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner,
      IngressTlsProvisioner externalServerIngressTlsProvisioner,
      ImagePullSecretProvisioner imagePullSecretProvisioner) {
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
  }
}
