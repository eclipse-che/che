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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheInstallationLocation;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class TrustStoreCertificateProvisionerTest {

  private static final String POD_NAME = "testPod";
  private static final String CONFIGMAP_NAME = "ca-certs";
  private static final String CERTIFICATE_MOUNT_PATH = "/certs";
  private static final String CONFIGMAP_KEY = "testConfigMapKey";
  private static final String CONFIGMAP_VALUE = "testConfigMapValue";

  @Mock CheInstallationLocation cheInstallationLocation;
  @Mock KubernetesClientFactory clientFactory;

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private KubernetesNamespace namespace;
  @Mock private KubernetesConfigsMaps kubernetesConfigsMaps;
  private ConfigMap sourceMap = newConfigMap();

  @Mock private KubernetesClient k8sClient;
  @Mock private UserManager userManager;

  @Mock
  private MixedOperation<
          ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>>
      configMapOperation;

  @Mock
  private NonNamespaceOperation<
          ConfigMap, ConfigMapList, DoneableConfigMap, Resource<ConfigMap, DoneableConfigMap>>
      nonNamespaceOperation;

  @Mock private Resource<ConfigMap, DoneableConfigMap> configMapResource;

  private TrustStoreCertificateProvisioner trustStoreCertificateProvisioner;

  @BeforeMethod
  public void setup() throws Exception {
    this.trustStoreCertificateProvisioner =
        new TrustStoreCertificateProvisioner(
            CONFIGMAP_NAME, CERTIFICATE_MOUNT_PATH, cheInstallationLocation, clientFactory);

    lenient().when(clientFactory.create()).thenReturn(k8sClient);
    lenient().when(k8sClient.configMaps()).thenReturn(configMapOperation);
    lenient().when(configMapOperation.inNamespace(any())).thenReturn(nonNamespaceOperation);
    lenient().when(nonNamespaceOperation.withName(any())).thenReturn(configMapResource);
    lenient().when(configMapResource.get()).thenReturn(sourceMap);
    lenient().when(namespace.configMaps()).thenReturn(kubernetesConfigsMaps);
  }

  @Test
  public void shouldProvisionTrustStoreMapAndMountIt() throws Exception {
    ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
    Pod pod = newPod();
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();
    doReturn(null).when(kubernetesConfigsMaps).get(anyString());

    trustStoreCertificateProvisioner.provision(k8sEnv, runtimeIdentity, namespace);

    verify(kubernetesConfigsMaps).create(captor.capture());
    assertEquals(captor.getValue().getData(), sourceMap.getData());
    PodSpec podSpec = pod.getSpec();
    assertEquals(podSpec.getVolumes().size(), 1);
    assertEquals(podSpec.getVolumes().get(0).getConfigMap().getName(), CONFIGMAP_NAME);
    assertTrue(
        podSpec
            .getContainers()
            .stream()
            .allMatch(
                c -> c.getVolumeMounts().get(0).getMountPath().equals(CERTIFICATE_MOUNT_PATH)));
  }

  @Test
  public void shouldRenewMapIfDidntMatch() throws Exception {
    ArgumentCaptor<ConfigMap> captor = ArgumentCaptor.forClass(ConfigMap.class);
    Pod pod = newPod();
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();
    ConfigMap existing = newConfigMap();
    existing.getData().put("foo", "bar");
    doReturn(existing).when(kubernetesConfigsMaps).get(anyString());

    trustStoreCertificateProvisioner.provision(k8sEnv, runtimeIdentity, namespace);

    verify(kubernetesConfigsMaps).create(captor.capture());
    assertEquals(captor.getValue().getData(), sourceMap.getData());
  }

  private static Pod newPod() {
    return new PodBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(POD_NAME).build())
        .withNewSpec()
        .withContainers(new ContainerBuilder().build(), new ContainerBuilder().build())
        .endSpec()
        .build();
  }

  private static ConfigMap newConfigMap() {
    return new ConfigMapBuilder()
        .withNewMetadata()
        .withName(CONFIGMAP_NAME)
        .endMetadata()
        .withData(ImmutableMap.of(CONFIGMAP_KEY, CONFIGMAP_VALUE))
        .build();
  }
}
