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

import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.TestObjects.newContainer;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.TestObjects.newPod;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class PodsVolumesTest {
  private static final String POD_1_NAME = "main";
  private static final String CONTAINER_1_NAME = "app";
  private static final String CONTAINER_2_NAME = "db";

  private PodData podData;

  private PodsVolumes podsVolumes;

  @BeforeMethod
  public void setUp() {
    Pod pod =
        newPod(POD_1_NAME)
            .withContainers(
                newContainer(CONTAINER_1_NAME).build(), newContainer(CONTAINER_2_NAME).build())
            .build();
    podData = new PodData(pod.getSpec(), pod.getMetadata());

    podsVolumes = new PodsVolumes();
  }

  @Test
  public void shouldChangePVCReference() {
    // given
    podData
        .getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("userData")
                .withPersistentVolumeClaim(
                    new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName("userData")
                        .build())
                .build());

    // when
    podsVolumes.changePVCReferences(ImmutableList.of(podData), "userData", "newPVCName");

    // then
    assertEquals(podData.getSpec().getVolumes().size(), 1);
    Volume volume = podData.getSpec().getVolumes().get(0);
    assertEquals(volume.getPersistentVolumeClaim().getClaimName(), "newPVCName");
  }

  @Test
  public void shouldNotChangeNonMatchingVolumesChangePVCReference() {
    // given
    podData
        .getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("userData")
                .withPersistentVolumeClaim(
                    new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName("nonMatching")
                        .build())
                .build());

    // when
    podsVolumes.changePVCReferences(ImmutableList.of(podData), "userData", "newPVCName");

    // then
    assertEquals(podData.getSpec().getVolumes().size(), 1);
    Volume volume = podData.getSpec().getVolumes().get(0);
    assertEquals(volume.getPersistentVolumeClaim().getClaimName(), "nonMatching");
  }

  @Test
  public void shouldReplaceVolumesWithCommon() {
    // given
    podData
        .getSpec()
        .getInitContainers()
        .add(
            new ContainerBuilder()
                .withName("userInitContainer")
                .withVolumeMounts(
                    new VolumeMountBuilder()
                        .withName("initData")
                        .withSubPath("/tmp/init/userData")
                        .build())
                .build());

    podData
        .getSpec()
        .getContainers()
        .get(0)
        .getVolumeMounts()
        .add(new VolumeMountBuilder().withName("userData").withSubPath("/home/user/data").build());

    podData
        .getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("userData")
                .withPersistentVolumeClaim(
                    new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName("userDataPVC")
                        .build())
                .build());
    podData
        .getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("initData")
                .withPersistentVolumeClaim(
                    new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName("initDataPVC")
                        .build())
                .build());

    // when
    podsVolumes.replacePVCVolumesWithCommon(ImmutableMap.of("pod", podData), "commonPVC");

    // then
    assertEquals(podData.getSpec().getVolumes().size(), 1);
    assertEquals(
        podData.getSpec().getVolumes().get(0).getPersistentVolumeClaim().getClaimName(),
        "commonPVC");
    assertEquals(
        podData.getSpec().getInitContainers().get(0).getVolumeMounts().get(0).getName(),
        "commonPVC");
    assertEquals(
        podData.getSpec().getContainers().get(0).getVolumeMounts().get(0).getName(), "commonPVC");
  }

  @Test
  public void shouldNotReplaceNonPVCVolumes() {
    // given
    podData
        .getSpec()
        .getInitContainers()
        .add(
            new ContainerBuilder()
                .withName("userInitContainer")
                .withVolumeMounts(new VolumeMountBuilder().withName("configMap").build())
                .build());

    podData
        .getSpec()
        .getContainers()
        .get(0)
        .getVolumeMounts()
        .add(new VolumeMountBuilder().withName("secret").withSubPath("/home/user/data").build());

    podData
        .getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("configMap")
                .withConfigMap(new ConfigMapVolumeSourceBuilder().withName("configMap").build())
                .build());
    podData
        .getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("secret")
                .withSecret(new SecretVolumeSourceBuilder().withSecretName("secret").build())
                .build());

    // when
    podsVolumes.replacePVCVolumesWithCommon(ImmutableMap.of("pod", podData), "commonPVC");

    // then
    assertEquals(podData.getSpec().getVolumes().size(), 2);
    assertNotNull(podData.getSpec().getVolumes().get(0).getConfigMap());
    assertNull(podData.getSpec().getVolumes().get(0).getPersistentVolumeClaim());

    assertNotNull(podData.getSpec().getVolumes().get(1).getSecret());
    assertNull(podData.getSpec().getVolumes().get(1).getPersistentVolumeClaim());

    assertEquals(
        podData.getSpec().getInitContainers().get(0).getVolumeMounts().get(0).getName(),
        "configMap");
    assertEquals(
        podData.getSpec().getContainers().get(0).getVolumeMounts().get(0).getName(), "secret");
  }
}
