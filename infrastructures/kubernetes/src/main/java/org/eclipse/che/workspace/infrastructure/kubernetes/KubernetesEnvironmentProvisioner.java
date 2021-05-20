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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStoragePodInterceptor;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GatewayRouterProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GitConfigProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ImagePullSecretProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.KubernetesTrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.NodeSelectorProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PodTerminationGracePeriodProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ProxySettingsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.SecurityContextProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ServiceAccountProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.SshKeysProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisionerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TolerationsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.UniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.VcsSslCertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.limits.ram.ContainerResourceProvisioner;
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
    private final ContainerResourceProvisioner resourceLimitRequestProvisioner;
    private final LogsVolumeMachineProvisioner logsVolumeMachineProvisioner;
    private final SecurityContextProvisioner securityContextProvisioner;
    private final PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner;
    private final TlsProvisioner<KubernetesEnvironment> externalServerTlsProvisioner;
    private final ImagePullSecretProvisioner imagePullSecretProvisioner;
    private final ProxySettingsProvisioner proxySettingsProvisioner;
    private final NodeSelectorProvisioner nodeSelectorProvisioner;
    private final TolerationsProvisioner tolerationsProvisioner;
    private final AsyncStorageProvisioner asyncStorageProvisioner;
    private final AsyncStoragePodInterceptor asyncStoragePodInterceptor;
    private final ServiceAccountProvisioner serviceAccountProvisioner;
    private final CertificateProvisioner certificateProvisioner;
    private final SshKeysProvisioner sshKeysProvisioner;
    private final GitConfigProvisioner gitConfigProvisioner;
    private final PreviewUrlExposer<KubernetesEnvironment> previewUrlExposer;
    private final VcsSslCertificateProvisioner vcsSslCertificateProvisioner;
    private final GatewayRouterProvisioner gatewayRouterProvisioner;
    private final KubernetesTrustedCAProvisioner trustedCAProvisioner;

    @Inject
    public KubernetesEnvironmentProvisionerImpl(
        @Named("che.infra.kubernetes.pvc.enabled") boolean pvcEnabled,
        UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner,
        ServersConverter<KubernetesEnvironment> serversConverter,
        EnvVarsConverter envVarsConverter,
        RestartPolicyRewriter restartPolicyRewriter,
        WorkspaceVolumesStrategy volumesStrategy,
        ContainerResourceProvisioner resourceLimitRequestProvisioner,
        LogsVolumeMachineProvisioner logsVolumeMachineProvisioner,
        SecurityContextProvisioner securityContextProvisioner,
        PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner,
        TlsProvisionerProvider<KubernetesEnvironment> externalServerTlsProvisionerProvider,
        ImagePullSecretProvisioner imagePullSecretProvisioner,
        ProxySettingsProvisioner proxySettingsProvisioner,
        NodeSelectorProvisioner nodeSelectorProvisioner,
        TolerationsProvisioner tolerationsProvisioner,
        AsyncStorageProvisioner asyncStorageProvisioner,
        AsyncStoragePodInterceptor asyncStoragePodInterceptor,
        ServiceAccountProvisioner serviceAccountProvisioner,
        CertificateProvisioner certificateProvisioner,
        SshKeysProvisioner sshKeysProvisioner,
        GitConfigProvisioner gitConfigProvisioner,
        PreviewUrlExposer<KubernetesEnvironment> previewUrlExposer,
        VcsSslCertificateProvisioner vcsSslCertificateProvisioner,
        GatewayRouterProvisioner gatewayRouterProvisioner,
        KubernetesTrustedCAProvisioner trustedCAProvisioner) {
      this.pvcEnabled = pvcEnabled;
      this.volumesStrategy = volumesStrategy;
      this.uniqueNamesProvisioner = uniqueNamesProvisioner;
      this.serversConverter = serversConverter;
      this.envVarsConverter = envVarsConverter;
      this.restartPolicyRewriter = restartPolicyRewriter;
      this.resourceLimitRequestProvisioner = resourceLimitRequestProvisioner;
      this.logsVolumeMachineProvisioner = logsVolumeMachineProvisioner;
      this.securityContextProvisioner = securityContextProvisioner;
      this.podTerminationGracePeriodProvisioner = podTerminationGracePeriodProvisioner;
      this.externalServerTlsProvisioner = externalServerTlsProvisionerProvider.get();
      this.imagePullSecretProvisioner = imagePullSecretProvisioner;
      this.proxySettingsProvisioner = proxySettingsProvisioner;
      this.nodeSelectorProvisioner = nodeSelectorProvisioner;
      this.tolerationsProvisioner = tolerationsProvisioner;
      this.asyncStorageProvisioner = asyncStorageProvisioner;
      this.asyncStoragePodInterceptor = asyncStoragePodInterceptor;
      this.serviceAccountProvisioner = serviceAccountProvisioner;
      this.certificateProvisioner = certificateProvisioner;
      this.sshKeysProvisioner = sshKeysProvisioner;
      this.vcsSslCertificateProvisioner = vcsSslCertificateProvisioner;
      this.gitConfigProvisioner = gitConfigProvisioner;
      this.previewUrlExposer = previewUrlExposer;
      this.gatewayRouterProvisioner = gatewayRouterProvisioner;
      this.trustedCAProvisioner = trustedCAProvisioner;
    }

    @Traced
    public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
        throws InfrastructureException {
      final String workspaceId = identity.getWorkspaceId();
      TracingTags.WORKSPACE_ID.set(workspaceId);

      LOG.debug("Start provisioning Kubernetes environment for workspace '{}'", workspaceId);
      // 1 stage - update environment according Infrastructure specific
      if (pvcEnabled) {
        asyncStoragePodInterceptor.intercept(k8sEnv, identity);
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
      resourceLimitRequestProvisioner.provision(k8sEnv, identity);
      nodeSelectorProvisioner.provision(k8sEnv, identity);
      tolerationsProvisioner.provision(k8sEnv, identity);
      externalServerTlsProvisioner.provision(k8sEnv, identity);
      securityContextProvisioner.provision(k8sEnv, identity);
      podTerminationGracePeriodProvisioner.provision(k8sEnv, identity);
      imagePullSecretProvisioner.provision(k8sEnv, identity);
      proxySettingsProvisioner.provision(k8sEnv, identity);
      serviceAccountProvisioner.provision(k8sEnv, identity);
      asyncStorageProvisioner.provision(k8sEnv, identity);
      certificateProvisioner.provision(k8sEnv, identity);
      sshKeysProvisioner.provision(k8sEnv, identity);
      vcsSslCertificateProvisioner.provision(k8sEnv, identity);
      gitConfigProvisioner.provision(k8sEnv, identity);
      gatewayRouterProvisioner.provision(k8sEnv, identity);
      trustedCAProvisioner.provision(k8sEnv, identity);
      uniqueNamesProvisioner.provision(k8sEnv, identity);
      LOG.debug("Provisioning Kubernetes environment done for workspace '{}'", workspaceId);
    }
  }
}
