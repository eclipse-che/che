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
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GitUserProfileProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ImagePullSecretProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.IngressTlsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PodTerminationGracePeriodProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ProxySettingsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecurityContextProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ServiceAccountProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.VcsSshKeysProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.limits.ram.RamLimitRequestProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.PreviewUrlExposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG =
        LoggerFactory.getLogger(KubernetesEnvironmentProvisionerImpl.class);

    private final boolean pvcEnabled;
    private final WorkspaceVolumesStrategy volumesStrategy;
    private final UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner;
    private final ServersConverter<KubernetesEnvironment> serversConverter;
    private final EnvVarsConverter envVarsConverter;
    private final RestartPolicyRewriter restartPolicyRewriter;
    private final RamLimitRequestProvisioner ramLimitProvisioner;
    private final LogsVolumeMachineProvisioner logsVolumeMachineProvisioner;
    private final SecurityContextProvisioner securityContextProvisioner;
    private final PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner;
    private final IngressTlsProvisioner externalServerIngressTlsProvisioner;
    private final ImagePullSecretProvisioner imagePullSecretProvisioner;
    private final ProxySettingsProvisioner proxySettingsProvisioner;
    private final ServiceAccountProvisioner serviceAccountProvisioner;
    private final CertificateProvisioner certificateProvisioner;
    private final VcsSshKeysProvisioner vcsSshKeysProvisioner;
    private final GitUserProfileProvisioner gitUserProfileProvisioner;
    private final PreviewUrlExposer<KubernetesEnvironment> previewUrlExposer;

    @Inject
    public KubernetesEnvironmentProvisionerImpl(
        @Named("che.infra.kubernetes.pvc.enabled") boolean pvcEnabled,
        UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner,
        ServersConverter<KubernetesEnvironment> serversConverter,
        EnvVarsConverter envVarsConverter,
        RestartPolicyRewriter restartPolicyRewriter,
        WorkspaceVolumesStrategy volumesStrategy,
        RamLimitRequestProvisioner ramLimitProvisioner,
        LogsVolumeMachineProvisioner logsVolumeMachineProvisioner,
        SecurityContextProvisioner securityContextProvisioner,
        PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner,
        IngressTlsProvisioner externalServerIngressTlsProvisioner,
        ImagePullSecretProvisioner imagePullSecretProvisioner,
        ProxySettingsProvisioner proxySettingsProvisioner,
        ServiceAccountProvisioner serviceAccountProvisioner,
        CertificateProvisioner certificateProvisioner,
        VcsSshKeysProvisioner vcsSshKeysProvisioner,
        GitUserProfileProvisioner gitUserProfileProvisioner,
        PreviewUrlExposer<KubernetesEnvironment> previewUrlExposer) {
      this.pvcEnabled = pvcEnabled;
      this.volumesStrategy = volumesStrategy;
      this.uniqueNamesProvisioner = uniqueNamesProvisioner;
      this.serversConverter = serversConverter;
      this.envVarsConverter = envVarsConverter;
      this.restartPolicyRewriter = restartPolicyRewriter;
      this.ramLimitProvisioner = ramLimitProvisioner;
      this.logsVolumeMachineProvisioner = logsVolumeMachineProvisioner;
      this.securityContextProvisioner = securityContextProvisioner;
      this.podTerminationGracePeriodProvisioner = podTerminationGracePeriodProvisioner;
      this.externalServerIngressTlsProvisioner = externalServerIngressTlsProvisioner;
      this.imagePullSecretProvisioner = imagePullSecretProvisioner;
      this.proxySettingsProvisioner = proxySettingsProvisioner;
      this.serviceAccountProvisioner = serviceAccountProvisioner;
      this.certificateProvisioner = certificateProvisioner;
      this.vcsSshKeysProvisioner = vcsSshKeysProvisioner;
      this.gitUserProfileProvisioner = gitUserProfileProvisioner;
      this.previewUrlExposer = previewUrlExposer;
    }

    @Traced
    public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
        throws InfrastructureException {
      final String workspaceId = identity.getWorkspaceId();

      TracingTags.WORKSPACE_ID.set(workspaceId);

      LOG.debug("Start provisioning Kubernetes environment for workspace '{}'", workspaceId);
      // 1 stage - update environment according Infrastructure specific
      if (pvcEnabled) {
        LOG.debug("Provisioning logs volume for workspace '{}'", workspaceId);
        logsVolumeMachineProvisioner.provision(k8sEnv, identity);
      }

      // 2 stage - converting Che model env to Kubernetes env
      LOG.debug("Provisioning servers & env vars converters for workspace '{}'", workspaceId);
      serversConverter.provision(k8sEnv, identity);
      previewUrlExposer.expose(k8sEnv);
      envVarsConverter.provision(k8sEnv, identity);
      if (pvcEnabled) {
        volumesStrategy.provision(k8sEnv, identity);
      }

      // 3 stage - add Kubernetes env items
      LOG.debug("Provisioning environment items for workspace '{}'", workspaceId);
      restartPolicyRewriter.provision(k8sEnv, identity);
      uniqueNamesProvisioner.provision(k8sEnv, identity);
      ramLimitProvisioner.provision(k8sEnv, identity);
      externalServerIngressTlsProvisioner.provision(k8sEnv, identity);
      securityContextProvisioner.provision(k8sEnv, identity);
      podTerminationGracePeriodProvisioner.provision(k8sEnv, identity);
      imagePullSecretProvisioner.provision(k8sEnv, identity);
      proxySettingsProvisioner.provision(k8sEnv, identity);
      serviceAccountProvisioner.provision(k8sEnv, identity);
      certificateProvisioner.provision(k8sEnv, identity);
      vcsSshKeysProvisioner.provision(k8sEnv, identity);
      gitUserProfileProvisioner.provision(k8sEnv, identity);
      LOG.debug("Provisioning Kubernetes environment done for workspace '{}'", workspaceId);
    }
  }
}
