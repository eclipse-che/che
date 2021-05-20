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
package org.eclipse.che.workspace.infrastructure.openshift;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.WorkspaceVolumesStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStoragePodInterceptor;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.AsyncStorageProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.DeploymentMetadataProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GatewayRouterProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GitConfigProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ImagePullSecretProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.NodeSelectorProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.PodTerminationGracePeriodProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ProxySettingsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.ServiceAccountProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.SshKeysProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TlsProvisionerProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.TolerationsProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.VcsSslCertificateProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.limits.ram.ContainerResourceProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.restartpolicy.RestartPolicyRewriter;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.server.ServersConverter;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftUniqueNamesProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenshiftTrustedCAProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.provision.RouteTlsProvisioner;
import org.eclipse.che.workspace.infrastructure.openshift.server.OpenShiftPreviewUrlExposer;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftEnvironmentProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftEnvironmentProvisionerTest {

  @Mock private WorkspaceVolumesStrategy volumesStrategy;
  @Mock private OpenShiftUniqueNamesProvisioner uniqueNamesProvisioner;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private RouteTlsProvisioner tlsRouteProvisioner;
  @Mock private TlsProvisionerProvider<OpenShiftEnvironment> tlsRouteProvisionerProvider;
  @Mock private EnvVarsConverter envVarsProvisioner;
  @Mock private ServersConverter<OpenShiftEnvironment> serversProvisioner;
  @Mock private RestartPolicyRewriter restartPolicyRewriter;
  @Mock private ContainerResourceProvisioner ramLimitProvisioner;
  @Mock private LogsVolumeMachineProvisioner logsVolumeMachineProvisioner;
  @Mock private PodTerminationGracePeriodProvisioner podTerminationGracePeriodProvisioner;
  @Mock private ImagePullSecretProvisioner imagePullSecretProvisioner;
  @Mock private ProxySettingsProvisioner proxySettingsProvisioner;
  @Mock private ServiceAccountProvisioner serviceAccountProvisioner;
  @Mock private AsyncStorageProvisioner asyncStorageProvisioner;
  @Mock private AsyncStoragePodInterceptor asyncStoragePodObserver;
  @Mock private CertificateProvisioner certificateProvisioner;
  @Mock private SshKeysProvisioner sshKeysProvisioner;
  @Mock private GitConfigProvisioner gitConfigProvisioner;
  @Mock private OpenShiftPreviewUrlExposer previewUrlEndpointsProvisioner;
  @Mock private VcsSslCertificateProvisioner vcsSslCertificateProvisioner;
  @Mock private NodeSelectorProvisioner nodeSelectorProvisioner;
  @Mock private TolerationsProvisioner tolerationsProvisioner;
  @Mock private GatewayRouterProvisioner gatewayRouterProvisioner;
  @Mock private DeploymentMetadataProvisioner deploymentMetadataProvisioner;
  @Mock private OpenshiftTrustedCAProvisioner trustedCAProvisioner;

  private OpenShiftEnvironmentProvisioner osInfraProvisioner;

  private InOrder provisionOrder;

  @BeforeMethod
  public void setUp() {
    when(tlsRouteProvisionerProvider.get()).thenReturn(tlsRouteProvisioner);
    osInfraProvisioner =
        new OpenShiftEnvironmentProvisioner(
            true,
            uniqueNamesProvisioner,
            tlsRouteProvisionerProvider,
            serversProvisioner,
            envVarsProvisioner,
            restartPolicyRewriter,
            volumesStrategy,
            ramLimitProvisioner,
            logsVolumeMachineProvisioner,
            podTerminationGracePeriodProvisioner,
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
            previewUrlEndpointsProvisioner,
            vcsSslCertificateProvisioner,
            gatewayRouterProvisioner,
            deploymentMetadataProvisioner,
            trustedCAProvisioner);
    provisionOrder =
        inOrder(
            logsVolumeMachineProvisioner,
            serversProvisioner,
            envVarsProvisioner,
            volumesStrategy,
            uniqueNamesProvisioner,
            tlsRouteProvisioner,
            restartPolicyRewriter,
            ramLimitProvisioner,
            nodeSelectorProvisioner,
            tolerationsProvisioner,
            podTerminationGracePeriodProvisioner,
            imagePullSecretProvisioner,
            proxySettingsProvisioner,
            serviceAccountProvisioner,
            certificateProvisioner,
            sshKeysProvisioner,
            vcsSslCertificateProvisioner,
            gitConfigProvisioner,
            previewUrlEndpointsProvisioner,
            gatewayRouterProvisioner,
            deploymentMetadataProvisioner,
            trustedCAProvisioner);
  }

  @Test
  public void performsOrderedProvisioning() throws Exception {
    osInfraProvisioner.provision(osEnv, runtimeIdentity);

    provisionOrder.verify(logsVolumeMachineProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(serversProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(envVarsProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(volumesStrategy).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(restartPolicyRewriter).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(tlsRouteProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(ramLimitProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(nodeSelectorProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(tolerationsProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder
        .verify(podTerminationGracePeriodProvisioner)
        .provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(imagePullSecretProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(proxySettingsProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(serviceAccountProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(certificateProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(sshKeysProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(vcsSslCertificateProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(gitConfigProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(gatewayRouterProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(deploymentMetadataProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(trustedCAProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verify(uniqueNamesProvisioner).provision(eq(osEnv), eq(runtimeIdentity));
    provisionOrder.verifyNoMoreInteractions();
  }
}
