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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvFromSource;
import io.fabric8.kubernetes.api.model.EnvFromSourceBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import java.util.HashMap;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link UniqueNamesProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class UniqueNamesProvisionerTest {

  private static final String WORKSPACE_ID = "workspace37";
  private static final String POD_NAME = "testPod";
  private static final String DEPLOYMENT_NAME = "testDeployment";
  private static final String CONFIGMAP_NAME = "testConfigMap";
  private static final String CONFIGMAP_KEY = "testConfigMapKey";
  private static final String CONFIGMAP_VALUE = "testConfigMapValue";
  private static final String INGRESS_NAME = "testIngress";

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  private UniqueNamesProvisioner<KubernetesEnvironment> uniqueNamesProvisioner;

  @BeforeMethod
  public void setup() {
    uniqueNamesProvisioner = new UniqueNamesProvisioner<>();
  }

  @Test
  public void provideUniquePodsNames() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    Pod pod = newPod();
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    ObjectMeta podMetadata = pod.getMetadata();
    assertNotEquals(podMetadata.getName(), POD_NAME);
    assertEquals(podMetadata.getLabels().get(CHE_ORIGINAL_NAME_LABEL), POD_NAME);
  }

  @Test
  public void provideUniqueDeploymentsName() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    Deployment deployment = newDeployment();
    doReturn(ImmutableMap.of(DEPLOYMENT_NAME, deployment)).when(k8sEnv).getDeploymentsCopy();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    ObjectMeta deploymentMetadata = deployment.getMetadata();
    assertNotEquals(deploymentMetadata.getName(), DEPLOYMENT_NAME);
    assertEquals(deploymentMetadata.getLabels().get(CHE_ORIGINAL_NAME_LABEL), DEPLOYMENT_NAME);
  }

  @Test
  public void provideUniqueConfigMapName() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    ConfigMap configMap = newConfigMap();
    doReturn(ImmutableMap.of(CONFIGMAP_NAME, configMap)).when(k8sEnv).getConfigMaps();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    ObjectMeta configMapMetadata = configMap.getMetadata();
    assertNotEquals(configMapMetadata.getName(), CONFIGMAP_NAME);
    assertEquals(configMapMetadata.getLabels().get(CHE_ORIGINAL_NAME_LABEL), CONFIGMAP_NAME);
  }

  @Test
  public void rewritePodConfigMapEnv() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    ConfigMap configMap = newConfigMap();
    doReturn(ImmutableMap.of(CONFIGMAP_NAME, configMap)).when(k8sEnv).getConfigMaps();

    EnvVar envVar =
        new EnvVarBuilder()
            .withNewValueFrom()
            .withNewConfigMapKeyRef()
            .withName(CONFIGMAP_NAME)
            .withKey(CONFIGMAP_KEY)
            .endConfigMapKeyRef()
            .endValueFrom()
            .build();
    Container container = new ContainerBuilder().withEnv(envVar).build();
    Pod pod = newPod();
    pod.getSpec().setContainers(ImmutableList.of(container));
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    String newConfigMapName = configMap.getMetadata().getName();
    EnvVar newEnvVar = container.getEnv().iterator().next();
    assertEquals(newEnvVar.getValueFrom().getConfigMapKeyRef().getName(), newConfigMapName);
  }

  @Test
  public void doesNotRewritePodConfigMapEnvWhenNoConfigMap() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    EnvVar envVar =
        new EnvVarBuilder()
            .withNewValueFrom()
            .withNewConfigMapKeyRef()
            .withName(CONFIGMAP_NAME)
            .withKey(CONFIGMAP_KEY)
            .endConfigMapKeyRef()
            .endValueFrom()
            .build();
    Container container = new ContainerBuilder().withEnv(envVar).build();
    Pod pod = newPod();
    pod.getSpec().setContainers(ImmutableList.of(container));
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    EnvVar newEnvVar = container.getEnv().iterator().next();
    assertEquals(newEnvVar.getValueFrom().getConfigMapKeyRef().getName(), CONFIGMAP_NAME);
  }

  @Test
  public void rewritePodConfigMapEnvFrom() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    ConfigMap configMap = newConfigMap();
    doReturn(ImmutableMap.of(CONFIGMAP_NAME, configMap)).when(k8sEnv).getConfigMaps();

    EnvFromSource envFrom =
        new EnvFromSourceBuilder()
            .withNewConfigMapRef()
            .withName(CONFIGMAP_NAME)
            .endConfigMapRef()
            .build();
    Container container = new ContainerBuilder().withEnvFrom(envFrom).build();
    Pod pod = newPod();
    pod.getSpec().setContainers(ImmutableList.of(container));
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    String newConfigMapName = configMap.getMetadata().getName();
    EnvFromSource newEnvFromSource = container.getEnvFrom().iterator().next();
    assertEquals(newEnvFromSource.getConfigMapRef().getName(), newConfigMapName);
  }

  @Test
  public void doesNotRewritePodConfigMapEnvFromWhenNoConfigMap() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    EnvFromSource envFrom =
        new EnvFromSourceBuilder()
            .withNewConfigMapRef()
            .withName(CONFIGMAP_NAME)
            .endConfigMapRef()
            .build();
    Container container = new ContainerBuilder().withEnvFrom(envFrom).build();
    Pod pod = newPod();
    pod.getSpec().setContainers(ImmutableList.of(container));
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    EnvFromSource newEnvFromSource = container.getEnvFrom().iterator().next();
    assertEquals(newEnvFromSource.getConfigMapRef().getName(), CONFIGMAP_NAME);
  }

  @Test
  public void rewritePodConfigMapVolumes() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    ConfigMap configMap = newConfigMap();
    doReturn(ImmutableMap.of(CONFIGMAP_NAME, configMap)).when(k8sEnv).getConfigMaps();

    Volume volume =
        new VolumeBuilder().withNewConfigMap().withName(CONFIGMAP_NAME).endConfigMap().build();
    Pod pod = newPod();
    pod.getSpec().setVolumes(ImmutableList.of(volume));
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    String newConfigMapName = configMap.getMetadata().getName();
    Volume newVolume = pod.getSpec().getVolumes().iterator().next();
    assertEquals(newVolume.getConfigMap().getName(), newConfigMapName);
  }

  @Test
  public void doesNotRewritePodConfigMapVolumesWhenNoConfigMap() throws Exception {
    when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    Volume volume =
        new VolumeBuilder().withNewConfigMap().withName(CONFIGMAP_NAME).endConfigMap().build();
    Pod pod = newPod();
    pod.getSpec().setVolumes(ImmutableList.of(volume));
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(ImmutableMap.of(POD_NAME, podData)).when(k8sEnv).getPodsData();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    Volume newVolume = pod.getSpec().getVolumes().iterator().next();
    assertEquals(newVolume.getConfigMap().getName(), CONFIGMAP_NAME);
  }

  @Test
  public void provideUniqueIngressesNames() throws Exception {
    final HashMap<String, Ingress> ingresses = new HashMap<>();
    Ingress ingress = newIngress();
    ingresses.put(POD_NAME, ingress);
    doReturn(ingresses).when(k8sEnv).getIngresses();

    uniqueNamesProvisioner.provision(k8sEnv, runtimeIdentity);

    final ObjectMeta ingressMetadata = ingress.getMetadata();
    assertNotEquals(ingressMetadata.getName(), INGRESS_NAME);
    assertEquals(ingressMetadata.getLabels().get(CHE_ORIGINAL_NAME_LABEL), INGRESS_NAME);
  }

  private static Pod newPod() {
    return new PodBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(POD_NAME).build())
        .withNewSpec()
        .endSpec()
        .build();
  }

  private static Deployment newDeployment() {
    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(DEPLOYMENT_NAME)
        .endMetadata()
        .withNewSpec()
        .withNewTemplate()
        .withNewMetadata()
        .withName(POD_NAME)
        .endMetadata()
        .endTemplate()
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

  private static Ingress newIngress() {
    return new IngressBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(INGRESS_NAME).build())
        .build();
  }
}
