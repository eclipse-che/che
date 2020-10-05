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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static com.google.common.collect.ImmutableMap.of;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class Openshift4TrustedCAProvisionerTest {

  private static final String POD_NAME = "testPod";
  private static final String CONFIGMAP_NAME = "ca-certs";
  private static final String CONFIGMAP_LABELS = "foo=bar";
  private static final String CERTIFICATE_MOUNT_PATH = "/certs";
  private static final String CONFIGMAP_KEY = "testConfigMapKey";
  private static final String CONFIGMAP_VALUE = "testConfigMapValue";

  @Mock OpenShiftClientFactory clientFactory;

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private OpenShiftProject openShiftProject;
  @Mock private KubernetesConfigsMaps kubernetesConfigsMaps;

  @Mock private OpenShiftClient k8sClient;

  Map<String, ConfigMap> envConfigMaps = new HashMap<>();

  private Openshift4TrustedCAProvisioner trustedCAProvisioner;

  @BeforeMethod
  public void setup() throws Exception {
    lenient().when(clientFactory.createOC()).thenReturn(k8sClient);
    lenient().when(openShiftProject.configMaps()).thenReturn(kubernetesConfigsMaps);
    lenient().when(k8sEnv.getConfigMaps()).thenReturn(envConfigMaps);
    this.trustedCAProvisioner =
        new Openshift4TrustedCAProvisioner(
            CONFIGMAP_NAME, CONFIGMAP_NAME, CONFIGMAP_LABELS, CERTIFICATE_MOUNT_PATH);
  }

  @Test
  public void shouldDoNothingIfCAStoreIsNotInitialized() throws Exception {
    Openshift4TrustedCAProvisioner localProvisioner =
        new Openshift4TrustedCAProvisioner(
            null, CONFIGMAP_NAME, CONFIGMAP_LABELS, CERTIFICATE_MOUNT_PATH);

    localProvisioner.provision(k8sEnv, openShiftProject);
    verifyZeroInteractions(k8sEnv, openShiftProject, clientFactory, openShiftProject);
  }

  @Test
  public void shouldProvisionTrustStoreMapAndMountIt() throws Exception {
    Pod pod = newPod();
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(of(POD_NAME, podData)).when(k8sEnv).getPodsData();

    trustedCAProvisioner.provision(k8sEnv, openShiftProject);

    assertEquals(envConfigMaps.size(), 1);
    assertTrue(envConfigMaps.get(CONFIGMAP_NAME).getMetadata().getLabels().containsKey("foo"));
    assertEquals(envConfigMaps.get(CONFIGMAP_NAME).getMetadata().getLabels().get("foo"), "bar");
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
        .withLabels(of("foo", "bar"))
        .endMetadata()
        .withData(of(CONFIGMAP_KEY, CONFIGMAP_VALUE))
        .build();
  }
}
