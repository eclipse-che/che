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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesEnvironmentProvisioner.KubernetesEnvironmentProvisionerImpl;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesEnvironmentProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesEnvironmentProvisionerTest {

  @Mock private WorkspaceVolumesStrategy volumesStrategy;
  @Mock private UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner;
  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private EnvVarsConverter envVarsProvisioner;
  @Mock private ServersConverter<KubernetesEnvironment> serversProvisioner;
  @Mock private RestartPolicyRewriter restartPolicyRewriter;
  @Mock private ContainerResourceProvisioner ramLimitProvisioner;
  @Mock private LogsVolumeMachineProvisioner logsVolumeMachineProvisioner;
  @Mock private SecurityContextProvisioner securityContextProvisioner;
  @Mock private PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner;

  @Mock
  private TlsProvisionerProvider<KubernetesEnvironment> externalServerIngressTlsProvisionerProvider;

  @Mock private TlsProvisioner<KubernetesEnvironment> externalServerIngressTlsProvisioner;
  @Mock private ImagePullSecretProvisioner imagePullSecretProvisioner;
  @Mock private ProxySettingsProvisioner proxySettingsProvisioner;
  @Mock private ServiceAccountProvisioner serviceAccountProvisioner;
  @Mock private AsyncStorageProvisioner asyncStorageProvisioner;
  @Mock private AsyncStoragePodInterceptor asyncStoragePodObserver;
  @Mock private CertificateProvisioner certificateProvisioner;
  @Mock private SshKeysProvisioner sshKeysProvisioner;
  @Mock private GitConfigProvisioner gitConfigProvisioner;
  @Mock private PreviewUrlExposer previewUrlExposer;
  @Mock private VcsSslCertificateProvisioner vcsSslCertificateProvisioner;
  @Mock private NodeSelectorProvisioner nodeSelectorProvisioner;
  @Mock private TolerationsProvisioner tolerationsProvisioner;
  @Mock private KubernetesTrustedCAProvisioner trustedCAProvisioner;
  @Mock private GatewayRouterProvisioner gatewayRouterProvisioner;

  private KubernetesEnvironmentProvisioner<KubernetesEnvironment> k8sInfraProvisioner;

  private InOrder provisionOrder;

  @BeforeMethod
  public void setUp() {
    when(externalServerIngressTlsProvisionerProvider.get())
        .thenReturn(externalServerIngressTlsProvisioner);
    k8sInfraProvisioner =
        new KubernetesEnvironmentProvisionerImpl(
            true,
            uniqueNamesProvisioner,
            serversProvisioner,
            envVarsProvisioner,
            restartPolicyRewriter,
            volumesStrategy,
            ramLimitProvisioner,
            logsVolumeMachineProvisioner,
            securityContextProvisioner,
            podTerminationGracePeriodProvisioner,
            externalServerIngressTlsProvisionerProvider,
            imagePullSecretProvisioner,
            proxySettingsProvisioner,
            nodeSelectorProvisioner,
            tolerationsProvisioner,
            asyncStorageProvisioner,
            asyncStoragePodObserver,
            serviceAccountProvisioner,
            certificateProvisioner,
            sshKeysProvisioner,
            gitConfigProvisioner,
            previewUrlExposer,
            vcsSslCertificateProvisioner,
            gatewayRouterProvisioner,
            trustedCAProvisioner);
    provisionOrder =
        inOrder(
            logsVolumeMachineProvisioner,
            volumesStrategy,
            uniqueNamesProvisioner,
            serversProvisioner,
            envVarsProvisioner,
            restartPolicyRewriter,
            ramLimitProvisioner,
            nodeSelectorProvisioner,
            tolerationsProvisioner,
            securityContextProvisioner,
            podTerminationGracePeriodProvisioner,
            externalServerIngressTlsProvisioner,
            imagePullSecretProvisioner,
            proxySettingsProvisioner,
            serviceAccountProvisioner,
            certificateProvisioner,
            gitConfigProvisioner,
            previewUrlExposer,
            gatewayRouterProvisioner,
            trustedCAProvisioner);
  }

  @Test
  public void performsOrderedProvisioning() throws Exception {
    k8sInfraProvisioner.provision(k8sEnv, runtimeIdentity);

    provisionOrder.verify(logsVolumeMachineProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(serversProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(envVarsProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(volumesStrategy).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(restartPolicyRewriter).provision(eq(k8sEnv), eq(runtimeIdentity));

    provisionOrder.verify(ramLimitProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(nodeSelectorProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(tolerationsProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder
        .verify(externalServerIngressTlsProvisioner)
        .provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(securityContextProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder
        .verify(podTerminationGracePeriodProvisioner)
        .provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(imagePullSecretProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(proxySettingsProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(serviceAccountProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(certificateProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(gitConfigProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(gatewayRouterProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(trustedCAProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verify(uniqueNamesProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
    provisionOrder.verifyNoMoreInteractions();
  }
}
