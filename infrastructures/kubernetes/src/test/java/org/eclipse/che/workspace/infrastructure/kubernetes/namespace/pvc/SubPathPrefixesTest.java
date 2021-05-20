/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_VOLUME_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.TestObjects.newContainer;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.TestObjects.newPod;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class SubPathPrefixesTest {

  private static final String USER_DATA_PVC_NAME = "userDataPVC";
  private static final String WORKSPACE_ID = "workspace123";

  private static final String POD_1_NAME = "main";
  private static final String CONTAINER_1_NAME = "app";
  private static final String CONTAINER_2_NAME = "db";

  private Pod pod;

  private PersistentVolumeClaim pvc;

  private KubernetesEnvironment k8sEnv;

  private SubPathPrefixes subpathPrefixes;

  @BeforeMethod
  public void setup() throws Exception {
    subpathPrefixes = new SubPathPrefixes();

    k8sEnv = KubernetesEnvironment.builder().build();

    pod =
        newPod(POD_1_NAME)
            .withContainers(
                newContainer(CONTAINER_1_NAME).build(), newContainer(CONTAINER_2_NAME).build())
            .build();

    k8sEnv.addPod(pod);

    pvc = newPVC(USER_DATA_PVC_NAME);
    k8sEnv.getPersistentVolumeClaims().put(USER_DATA_PVC_NAME, pvc);

    pod.getSpec()
        .getInitContainers()
        .add(
            new ContainerBuilder()
                .withName("userInitContainer")
                .withVolumeMounts(
                    new VolumeMountBuilder()
                        .withName("userData")
                        .withSubPath("/tmp/init/userData")
                        .build())
                .build());

    pod.getSpec()
        .getContainers()
        .get(0)
        .getVolumeMounts()
        .add(new VolumeMountBuilder().withName("userData").withSubPath("/home/user/data").build());

    pod.getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("userData")
                .withPersistentVolumeClaim(
                    new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName(USER_DATA_PVC_NAME)
                        .build())
                .build());
  }

  @Test
  public void shouldPrefixVolumeMountsSubpathsAndUsePvcNameAsVolumeName() {
    // when
    subpathPrefixes.prefixVolumeMountsSubpaths(k8sEnv, WORKSPACE_ID);

    // then
    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(userPodVolume.getPersistentVolumeClaim().getClaimName(), USER_DATA_PVC_NAME);
    assertEquals(
        podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(), USER_DATA_PVC_NAME);

    Container initContainer = podSpec.getInitContainers().get(0);
    VolumeMount initVolumeMount = initContainer.getVolumeMounts().get(0);
    assertEquals(
        initVolumeMount.getSubPath(),
        WORKSPACE_ID + "/" + USER_DATA_PVC_NAME + "/tmp/init/userData");
    assertEquals(initVolumeMount.getName(), userPodVolume.getName());

    Container container = podSpec.getContainers().get(0);
    VolumeMount volumeMount = container.getVolumeMounts().get(0);
    assertEquals(
        volumeMount.getSubPath(), WORKSPACE_ID + "/" + USER_DATA_PVC_NAME + "/home/user/data");
    assertEquals(volumeMount.getName(), userPodVolume.getName());
  }

  @Test
  public void shouldNotPrefixNotPVCSourcesVolumes() {
    // given
    Volume podVolume = pod.getSpec().getVolumes().get(0);
    podVolume.setPersistentVolumeClaim(null);
    podVolume.setConfigMap(new ConfigMapVolumeSourceBuilder().withName("configMap").build());

    // when
    subpathPrefixes.prefixVolumeMountsSubpaths(k8sEnv, WORKSPACE_ID);

    // then
    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume podDataVolume = podSpec.getVolumes().get(0);

    Container initContainer = podSpec.getInitContainers().get(0);
    VolumeMount initVolumeMount = initContainer.getVolumeMounts().get(0);
    assertEquals(initVolumeMount.getSubPath(), "/tmp/init/userData");
    assertEquals(initVolumeMount.getName(), podDataVolume.getName());

    Container container = podSpec.getContainers().get(0);
    VolumeMount volumeMount = container.getVolumeMounts().get(0);
    assertEquals(volumeMount.getSubPath(), "/home/user/data");
    assertEquals(volumeMount.getName(), podDataVolume.getName());
  }

  @Test
  public void shouldPrefixVolumeMountsSubpathsAndUseVolumeNameStoredInLabels() {
    // given
    String volumeName = "userDataVolume";
    pvc.getMetadata().getLabels().put(CHE_VOLUME_NAME_LABEL, volumeName);

    // when
    subpathPrefixes.prefixVolumeMountsSubpaths(k8sEnv, WORKSPACE_ID);

    // then
    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(userPodVolume.getPersistentVolumeClaim().getClaimName(), USER_DATA_PVC_NAME);
    assertEquals(
        podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(), USER_DATA_PVC_NAME);

    Container initContainer = podSpec.getInitContainers().get(0);
    VolumeMount initVolumeMount = initContainer.getVolumeMounts().get(0);
    assertEquals(
        initVolumeMount.getSubPath(), WORKSPACE_ID + "/" + volumeName + "/tmp/init/userData");
    assertEquals(initVolumeMount.getName(), userPodVolume.getName());

    Container container = podSpec.getContainers().get(0);
    VolumeMount volumeMount = container.getVolumeMounts().get(0);
    assertEquals(volumeMount.getSubPath(), WORKSPACE_ID + "/" + volumeName + "/home/user/data");
    assertEquals(volumeMount.getName(), userPodVolume.getName());
  }

  @Test
  public void shouldReturnWorkspaceIdAsSubpathForWorkspace() {
    // when
    String workspaceSubPath = subpathPrefixes.getWorkspaceSubPath(WORKSPACE_ID);

    // then
    assertEquals(workspaceSubPath, WORKSPACE_ID);
  }

  private static PersistentVolumeClaim newPVC(String name) {
    return newPVC(name, new HashMap<>());
  }

  private static PersistentVolumeClaim newPVC(String name, Map<String, String> labels) {
    return new PersistentVolumeClaimBuilder()
        .withNewMetadata()
        .withName(name)
        .withLabels(labels)
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .build();
  }
}
