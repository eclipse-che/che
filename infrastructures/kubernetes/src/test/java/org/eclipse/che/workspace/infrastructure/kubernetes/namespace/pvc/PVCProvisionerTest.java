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
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class PVCProvisionerTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String PVC_NAME_PREFIX = "che-claim";

  private static final String POD_1_NAME = "main";
  private static final String CONTAINER_1_NAME = "app";
  private static final String CONTAINER_2_NAME = "db";
  private static final String MACHINE_1_NAME = POD_1_NAME + '/' + CONTAINER_1_NAME;
  private static final String MACHINE_2_NAME = POD_1_NAME + '/' + CONTAINER_2_NAME;

  private static final String POD_2_NAME = "second";
  private static final String CONTAINER_3_NAME = "app2";
  private static final String MACHINE_3_NAME = POD_2_NAME + '/' + CONTAINER_3_NAME;

  private static final String VOLUME_1_NAME = "vol1";
  private static final String VOLUME_2_NAME = "vol2";

  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";
  private static final String PVC_STORAGE_CLASS_NAME = "default";

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1", "infraNamespace");

  private KubernetesEnvironment k8sEnv;

  private Pod pod;
  private Pod pod2;

  @Mock private PodsVolumes podsVolumes;
  private PVCProvisioner provisioner;

  @BeforeMethod
  public void setUp() {
    provisioner =
        new PVCProvisioner(
            PVC_NAME_PREFIX, PVC_QUANTITY, PVC_ACCESS_MODE, PVC_STORAGE_CLASS_NAME, podsVolumes);

    k8sEnv = KubernetesEnvironment.builder().build();

    k8sEnv
        .getMachines()
        .put(
            MACHINE_1_NAME,
            TestObjects.newMachineConfig()
                .withVolume(VOLUME_1_NAME, "/path")
                .withVolume(VOLUME_2_NAME, "/path2")
                .build());

    k8sEnv
        .getMachines()
        .put(
            MACHINE_2_NAME,
            TestObjects.newMachineConfig().withVolume(VOLUME_2_NAME, "/path2").build());

    k8sEnv
        .getMachines()
        .put(
            MACHINE_3_NAME,
            TestObjects.newMachineConfig().withVolume(VOLUME_1_NAME, "/path").build());

    pod =
        newPod(POD_1_NAME)
            .withContainers(
                newContainer(CONTAINER_1_NAME).build(), newContainer(CONTAINER_2_NAME).build())
            .build();

    pod2 = newPod(POD_2_NAME).withContainers(newContainer(CONTAINER_3_NAME).build()).build();

    k8sEnv.addPod(pod);
    k8sEnv.addPod(pod2);
  }

  @Test
  public void testProvisionPVCsForEachVolumeWithUniqueName() throws Exception {
    // given
    k8sEnv.getPersistentVolumeClaims().clear();

    // when
    provisioner.convertCheVolumes(k8sEnv, WORKSPACE_ID);

    // then
    assertEquals(pod.getSpec().getVolumes().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(0).getVolumeMounts().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(1).getVolumeMounts().size(), 1);

    assertEquals(pod2.getSpec().getVolumes().size(), 1);
    assertEquals(pod2.getSpec().getContainers().get(0).getVolumeMounts().size(), 1);
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 2);

    PersistentVolumeClaim pvcForVolume1 =
        findPvc(VOLUME_1_NAME, k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForVolume1);
    assertTrue(pvcForVolume1.getMetadata().getName().startsWith(PVC_NAME_PREFIX));

    PersistentVolumeClaim pvcForVolume2 =
        findPvc(VOLUME_2_NAME, k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForVolume2);
    assertTrue(pvcForVolume2.getMetadata().getName().startsWith(PVC_NAME_PREFIX));
  }

  @Test
  public void testMatchingUserDefinedPVCWithCheVolume() throws Exception {
    // given
    k8sEnv.getPersistentVolumeClaims().put("userDataPVC", newPVC("userDataPVC"));

    pod.getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("userData")
                .withPersistentVolumeClaim(
                    new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName("userDataPVC")
                        .build())
                .build());

    pod.getSpec()
        .getContainers()
        .get(0)
        .getVolumeMounts()
        .add(new VolumeMountBuilder().withName("userData").withSubPath("/home/user/data").build());

    k8sEnv.getMachines().values().forEach(m -> m.getVolumes().clear());
    k8sEnv
        .getMachines()
        .get(MACHINE_2_NAME)
        .getVolumes()
        .put("userDataPVC", new VolumeImpl().withPath("/"));

    // when
    provisioner.convertCheVolumes(k8sEnv, WORKSPACE_ID);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    PersistentVolumeClaim pvcForUserData =
        findPvc("userDataPVC", k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForUserData);
    assertEquals("userDataPVC", pvcForUserData.getMetadata().getName());

    PodSpec podSpec = k8sEnv.getPodsData().get(POD_1_NAME).getSpec();

    io.fabric8.kubernetes.api.model.Volume userPodVolume = podSpec.getVolumes().get(0);
    assertEquals(
        userPodVolume.getPersistentVolumeClaim().getClaimName(),
        pvcForUserData.getMetadata().getName());
    assertEquals(
        podSpec.getVolumes().get(0).getPersistentVolumeClaim().getClaimName(),
        pvcForUserData.getMetadata().getName());

    // check container bound to user-defined PVC
    Container container1 = podSpec.getContainers().get(0);
    assertEquals(container1.getVolumeMounts().size(), 1);
    VolumeMount volumeMount = container1.getVolumeMounts().get(0);
    assertEquals(volumeMount.getName(), userPodVolume.getName());

    // check container that is bound to Che Volume via Machine configuration
    Container container2 = podSpec.getContainers().get(1);
    VolumeMount cheVolumeMount2 = container2.getVolumeMounts().get(0);
    assertEquals(cheVolumeMount2.getName(), userPodVolume.getName());
  }

  @Test
  public void testDoNotProvisionPVCsWhenItIsAlreadyProvisionedForGivenVolumeAndWorkspace()
      throws Exception {
    final String pvcUniqueName1 = PVC_NAME_PREFIX + "-3121";
    PersistentVolumeClaim pvc1 =
        newPVC(pvcUniqueName1, ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_1_NAME));
    pvc1.getAdditionalProperties().put("CHE_PROVISIONED", true);
    final String pvcUniqueName2 = PVC_NAME_PREFIX + "-71333";
    PersistentVolumeClaim pvc2 =
        newPVC(pvcUniqueName2, ImmutableMap.of(CHE_VOLUME_NAME_LABEL, VOLUME_2_NAME));
    pvc2.getAdditionalProperties().put("CHE_PROVISIONED", true);

    k8sEnv.getPersistentVolumeClaims().put(pvc1.getMetadata().getName(), pvc1);
    k8sEnv.getPersistentVolumeClaims().put(pvc2.getMetadata().getName(), pvc2);

    provisioner.convertCheVolumes(k8sEnv, WORKSPACE_ID);

    assertEquals(pod.getSpec().getVolumes().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(0).getVolumeMounts().size(), 2);
    assertEquals(pod.getSpec().getContainers().get(1).getVolumeMounts().size(), 1);
    assertEquals(pod2.getSpec().getVolumes().size(), 1);
    assertEquals(pod2.getSpec().getContainers().get(0).getVolumeMounts().size(), 1);
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 2);
    assertTrue(k8sEnv.getPersistentVolumeClaims().containsKey(pvcUniqueName1));
    assertTrue(k8sEnv.getPersistentVolumeClaims().containsKey(pvcUniqueName2));
  }

  @Test
  public void testProvisioningPVCsToK8sEnvironment() throws Exception {
    // given
    k8sEnv = KubernetesEnvironment.builder().build();
    Map<String, PersistentVolumeClaim> toProvision = new HashMap<>();
    toProvision.put("appStorage", newPVC("appStorage"));

    Pod pod =
        newPod(POD_1_NAME)
            .withContainers(
                newContainer(CONTAINER_1_NAME)
                    .withVolumeMount("appStorage", "/data", "data")
                    .withVolumeMount("appStorage", "/config", "config")
                    .build())
            .withPVCVolume("appStorage", "appStorage")
            .build();

    k8sEnv.addPod(pod);

    // when
    provisioner.provision(k8sEnv, toProvision);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    PersistentVolumeClaim pvcForUserData =
        findPvc("appStorage", k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForUserData);
    assertTrue(pvcForUserData.getMetadata().getName().startsWith(PVC_NAME_PREFIX));

    verify(podsVolumes)
        .changePVCReferences(
            k8sEnv.getPodsData().values(), "appStorage", pvcForUserData.getMetadata().getName());
  }

  @Test
  public void testMatchEnvPVCsByVolumeNameWhenProvisioningPVCsToK8sEnvironment() throws Exception {
    // given
    k8sEnv = KubernetesEnvironment.builder().build();
    k8sEnv
        .getPersistentVolumeClaims()
        .put(
            "appStorage",
            newPVC("pvc123123", ImmutableMap.of(CHE_VOLUME_NAME_LABEL, "appStorage")));

    Map<String, PersistentVolumeClaim> toProvision = new HashMap<>();
    toProvision.put("appStorage", newPVC("appStorage"));

    Pod pod =
        newPod(POD_1_NAME)
            .withContainers(
                newContainer(CONTAINER_1_NAME)
                    .withVolumeMount("appStorage", "/data", "data")
                    .withVolumeMount("appStorage", "/config", "config")
                    .build())
            .withPVCVolume("appStorage", "appStorage")
            .build();

    k8sEnv.addPod(pod);

    // when
    provisioner.provision(k8sEnv, toProvision);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    PersistentVolumeClaim pvcForUserData =
        findPvc("appStorage", k8sEnv.getPersistentVolumeClaims());
    assertNotNull(pvcForUserData);
    assertEquals("pvc123123", pvcForUserData.getMetadata().getName());

    verify(podsVolumes)
        .changePVCReferences(k8sEnv.getPodsData().values(), "appStorage", "pvc123123");
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

  private PersistentVolumeClaim findPvc(
      String volumeName, Map<String, PersistentVolumeClaim> claims) {
    return claims
        .values()
        .stream()
        .filter(c -> volumeName.equals(c.getMetadata().getLabels().get(CHE_VOLUME_NAME_LABEL)))
        .findAny()
        .orElse(null);
  }
}
