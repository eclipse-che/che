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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.LocalObjectReference;
import io.fabric8.kubernetes.api.model.LocalObjectReferenceBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.PodSecurityContextBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Toleration;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests {@link PodMerger}.
 *
 * @author Sergii Leshchenko
 */
public class PodMergerTest {

  private PodMerger podMerger;

  @BeforeMethod
  public void setUp() {
    podMerger = new PodMerger();
  }

  @Test
  public void shouldMergeMetasOfPodsData() throws Exception {
    // given
    ObjectMeta podMeta1 =
        new ObjectMetaBuilder()
            .withName("ignored-1")
            .withAnnotations(ImmutableMap.of("ann1", "v1"))
            .withLabels(ImmutableMap.of("label1", "v1"))
            .build();
    podMeta1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(new PodSpecBuilder().build(), podMeta1);

    ObjectMeta podMeta2 =
        new ObjectMetaBuilder()
            .withName("ignored-2")
            .withAnnotations(ImmutableMap.of("ann2", "v2"))
            .withLabels(ImmutableMap.of("label2", "v2"))
            .build();
    podMeta2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(new PodSpecBuilder().build(), podMeta2);

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    ObjectMeta podMeta = podTemplate.getMetadata();
    verifyContainsAllFrom(podMeta, podData1.getMetadata());
    verifyContainsAllFrom(podMeta, podData2.getMetadata());
  }

  @Test
  public void shouldMatchMergedPodTemplateLabelsWithDeploymentSelector() throws Exception {
    // given
    ObjectMeta podMeta1 =
        new ObjectMetaBuilder()
            .withName("ignored-1")
            .withAnnotations(ImmutableMap.of("ann1", "v1"))
            .withLabels(ImmutableMap.of("label1", "v1"))
            .build();
    podMeta1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(new PodSpecBuilder().build(), podMeta1);

    // when
    Deployment merged = podMerger.merge(Collections.singletonList(podData1));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    ObjectMeta podMeta = podTemplate.getMetadata();
    Map<String, String> deploymentSelector = merged.getSpec().getSelector().getMatchLabels();
    assertTrue(podMeta.getLabels().entrySet().containsAll(deploymentSelector.entrySet()));
  }

  @Test
  public void shouldMergeSpecsOfPodsData() throws Exception {
    // given
    PodSpec podSpec1 =
        new PodSpecBuilder()
            .withContainers(new ContainerBuilder().withName("c1").build())
            .withInitContainers(new ContainerBuilder().withName("initC1").build())
            .withVolumes(new VolumeBuilder().withName("v1").build())
            .withNodeSelector(Map.of("foo1", "bar1"))
            .withImagePullSecrets(new LocalObjectReferenceBuilder().withName("secret1").build())
            .withTolerations(new Toleration("Effect", "key", "operator", 0L, "value1"))
            .build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 =
        new PodSpecBuilder()
            .withContainers(new ContainerBuilder().withName("c2").build())
            .withInitContainers(new ContainerBuilder().withName("initC2").build())
            .withVolumes(new VolumeBuilder().withName("v2").build())
            .withNodeSelector(Map.of("foo2", "bar2"))
            .withImagePullSecrets(new LocalObjectReferenceBuilder().withName("secret2").build())
            .withTolerations(new Toleration("Effect", "key", "operator", 0L, "value2"))
            .build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    verifyContainsAllFrom(podTemplate.getSpec(), podData1.getSpec());
    verifyContainsAllFrom(podTemplate.getSpec(), podData2.getSpec());
  }

  @Test
  public void shouldGenerateContainerNamesIfCollisionHappened() throws Exception {
    // given
    PodSpec podSpec1 =
        new PodSpecBuilder().withContainers(new ContainerBuilder().withName("c").build()).build();
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 =
        new PodSpecBuilder().withContainers(new ContainerBuilder().withName("c").build()).build();
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    List<Container> containers = podTemplate.getSpec().getContainers();
    assertEquals(containers.size(), 2);
    Container container1 = containers.get(0);
    assertEquals(container1.getName(), "c");
    Container container2 = containers.get(1);
    assertNotEquals(container2.getName(), "c");
    assertTrue(container2.getName().startsWith("c"));
  }

  @Test
  public void shouldGenerateInitContainerNamesIfCollisionHappened() throws Exception {
    // given
    PodSpec podSpec1 =
        new PodSpecBuilder()
            .withInitContainers(new ContainerBuilder().withName("initC").build())
            .build();
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 =
        new PodSpecBuilder()
            .withInitContainers(new ContainerBuilder().withName("initC").build())
            .build();
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());
    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    List<Container> initContainers = podTemplate.getSpec().getInitContainers();
    assertEquals(initContainers.size(), 2);
    Container container1 = initContainers.get(0);
    assertEquals(container1.getName(), "initC");
    Container container2 = initContainers.get(1);
    assertNotEquals(container2.getName(), "initC");
    assertTrue(container2.getName().startsWith("initC"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Pods have to have volumes with unique names but there are multiple `volume` volumes")
  public void shouldThrownAnExceptionIfVolumeNameCollisionHappened() throws Exception {
    // given
    PodSpec podSpec1 =
        new PodSpecBuilder().withVolumes(new VolumeBuilder().withName("volume").build()).build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 =
        new PodSpecBuilder().withVolumes(new VolumeBuilder().withName("volume").build()).build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    podMerger.merge(Arrays.asList(podData1, podData2));
  }

  @Test
  public void shouldMergeProjectVolumesWithoutException() throws Exception {
    // given
    PodSpec podSpec1 =
        new PodSpecBuilder()
            .withVolumes(new VolumeBuilder().withName(PROJECTS_VOLUME_NAME).build())
            .build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 =
        new PodSpecBuilder()
            .withVolumes(new VolumeBuilder().withName(PROJECTS_VOLUME_NAME).build())
            .build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    assertEquals(podTemplate.getSpec().getVolumes().size(), 1);
  }

  @Test
  public void shouldNotAddImagePullPolicyTwice() throws Exception {
    // given
    PodSpec podSpec1 =
        new PodSpecBuilder()
            .withImagePullSecrets(new LocalObjectReferenceBuilder().withName("secret").build())
            .build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 =
        new PodSpecBuilder()
            .withImagePullSecrets(new LocalObjectReferenceBuilder().withName("secret").build())
            .build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    List<LocalObjectReference> imagePullSecrets = podTemplate.getSpec().getImagePullSecrets();
    assertEquals(imagePullSecrets.size(), 1);
    assertEquals(imagePullSecrets.get(0).getName(), "secret");
  }

  @Test(dataProvider = "terminationGracePeriodProvider")
  public void shouldBeAbleToMergeTerminationGracePeriodS(
      List<Long> terminationGracePeriods, Long expectedResultLong) throws ValidationException {
    List<PodData> podData =
        terminationGracePeriods
            .stream()
            .map(
                p ->
                    new PodData(
                        new PodSpecBuilder().withTerminationGracePeriodSeconds(p).build(),
                        new ObjectMetaBuilder().build()))
            .collect(Collectors.toList());

    // when
    Deployment merged = podMerger.merge(podData);
    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    assertEquals(podTemplate.getSpec().getTerminationGracePeriodSeconds(), expectedResultLong);
  }

  @DataProvider(name = "terminationGracePeriodProvider")
  public Object[][] terminationGracePeriodProvider() {
    return new Object[][] {
      {Arrays.asList(32L, 30L, 27L), 32L},
      {Arrays.asList(null, null, null), null},
      {Arrays.asList(null, 30L, 27L), 30L},
      {Arrays.asList(32L, null, 27L), 32L},
      {Arrays.asList(null, 27L), 27L},
      {Arrays.asList(27L, 27L), 27L},
      {Arrays.asList(27L, null), 27L}
    };
  }

  @Test
  public void shouldAssignSecurityContextSharedByPods() throws Exception {
    // given
    PodSpec podSpec1 =
        new PodSpecBuilder()
            .withSecurityContext(new PodSecurityContextBuilder().withRunAsUser(42L).build())
            .build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 =
        new PodSpecBuilder()
            .withSecurityContext(new PodSecurityContextBuilder().withRunAsUser(42L).build())
            .build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    PodSecurityContext sc = podTemplate.getSpec().getSecurityContext();
    assertEquals(sc.getRunAsUser(), (Long) 42L);
  }

  @Test(expectedExceptions = ValidationException.class)
  public void shouldFailIfSecurityContextDiffersInPods() throws Exception {
    // given
    PodSpec podSpec1 =
        new PodSpecBuilder()
            .withSecurityContext(new PodSecurityContextBuilder().withRunAsUser(42L).build())
            .build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 =
        new PodSpecBuilder()
            .withSecurityContext(new PodSecurityContextBuilder().withRunAsUser(43L).build())
            .build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    // exception is thrown
  }

  @Test
  public void shouldAssignServiceAccountSharedByPods() throws Exception {
    // given
    PodSpec podSpec1 = new PodSpecBuilder().withServiceAccount("sa").build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 = new PodSpecBuilder().withServiceAccount("sa").build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    String sa = podTemplate.getSpec().getServiceAccount();
    assertEquals(sa, "sa");
  }

  @Test(expectedExceptions = ValidationException.class)
  public void shouldFailServiceAccountDiffersInPods() throws Exception {
    // given
    PodSpec podSpec1 = new PodSpecBuilder().withServiceAccount("sa").build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 = new PodSpecBuilder().withServiceAccount("sb").build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    // exception is thrown
  }

  @Test
  public void shouldAssignServiceAccountNameSharedByPods() throws Exception {
    // given
    PodSpec podSpec1 = new PodSpecBuilder().withServiceAccountName("sa").build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 = new PodSpecBuilder().withServiceAccountName("sa").build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    PodTemplateSpec podTemplate = merged.getSpec().getTemplate();
    String sa = podTemplate.getSpec().getServiceAccountName();
    assertEquals(sa, "sa");
  }

  @Test(expectedExceptions = ValidationException.class)
  public void shouldFailServiceAccountNameDiffersInPods() throws Exception {
    // given
    PodSpec podSpec1 = new PodSpecBuilder().withServiceAccountName("sa").build();
    podSpec1.setAdditionalProperty("add1", 1L);
    PodData podData1 = new PodData(podSpec1, new ObjectMetaBuilder().build());

    PodSpec podSpec2 = new PodSpecBuilder().withServiceAccountName("sb").build();
    podSpec2.setAdditionalProperty("add2", 2L);
    PodData podData2 = new PodData(podSpec2, new ObjectMetaBuilder().build());

    // when
    Deployment merged = podMerger.merge(Arrays.asList(podData1, podData2));

    // then
    // exception is thrown
  }

  private void verifyContainsAllFrom(ObjectMeta source, ObjectMeta toCheck) {
    assertTrue(source.getLabels().entrySet().containsAll(toCheck.getLabels().entrySet()));
    assertTrue(source.getAnnotations().entrySet().containsAll(toCheck.getAnnotations().entrySet()));
    assertTrue(
        source
            .getAdditionalProperties()
            .entrySet()
            .containsAll(toCheck.getAdditionalProperties().entrySet()));
  }

  private void verifyContainsAllFrom(PodSpec source, PodSpec toCheck) {
    assertTrue(source.getContainers().containsAll(toCheck.getContainers()));
    assertTrue(source.getInitContainers().containsAll(toCheck.getInitContainers()));
    assertTrue(source.getVolumes().containsAll(toCheck.getVolumes()));
    assertTrue(source.getImagePullSecrets().containsAll(toCheck.getImagePullSecrets()));
    assertTrue(
        source.getNodeSelector().entrySet().containsAll(toCheck.getNodeSelector().entrySet()));
    assertTrue(
        source
            .getAdditionalProperties()
            .entrySet()
            .containsAll(toCheck.getAdditionalProperties().entrySet()));
    assertTrue(source.getTolerations().containsAll(toCheck.getTolerations()));
  }
}
