/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesEnvironmentPodsValidator}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesEnvironmentPodsValidatorTest {

  @Mock private KubernetesEnvironment kubernetesEnvironment;

  private KubernetesEnvironmentPodsValidator podsValidator;

  @BeforeMethod
  public void setUp() {
    podsValidator = new KubernetesEnvironmentPodsValidator();
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Environment should contain at least 1 pod or deployment")
  public void shouldThrowExceptionWhenEnvDoesNotHaveAnyPods() throws Exception {
    // given
    when(kubernetesEnvironment.getPodsData()).thenReturn(emptyMap());

    // when
    podsValidator.validate(kubernetesEnvironment);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Environment contains machines that are missing in recipe: pod1/db")
  public void shouldThrowExceptionWhenMachineIsDeclaredButThereIsNotContainerInKubernetesRecipe()
      throws Exception {
    // given
    String podName = "pod1";
    Pod pod = createPod("pod1", "main");
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    when(kubernetesEnvironment.getPodsData()).thenReturn(ImmutableMap.of(podName, podData));
    when(kubernetesEnvironment.getMachines())
        .thenReturn(ImmutableMap.of(podName + "/db", mock(InternalMachineConfig.class)));

    // when
    podsValidator.validate(kubernetesEnvironment);
  }

  @Test
  public void shouldPassWhenMachineIsDeclaredAndIsInitContainerInKubernetesRecipe()
      throws Exception {
    // given
    String podName = "pod1";
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(podName)
            .endMetadata()
            .withNewSpec()
            .withInitContainers(singletonList(createContainer("init")))
            .endSpec()
            .build();
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    when(kubernetesEnvironment.getPodsData()).thenReturn(ImmutableMap.of(podName, podData));
    when(kubernetesEnvironment.getMachines())
        .thenReturn(ImmutableMap.of(podName + "/init", mock(InternalMachineConfig.class)));

    // when
    podsValidator.validate(kubernetesEnvironment);

    // then
    // no exception means machine matches init container - it's expected
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Environment contains pod with missing metadata")
  public void shouldThrowExceptionWhenPodHasNoMetadata() throws Exception {
    // given
    PodData podData = new PodData(new PodSpec(), null);
    when(kubernetesEnvironment.getPodsData()).thenReturn(ImmutableMap.of("", podData));

    // when
    podsValidator.validate(kubernetesEnvironment);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Pod 'pod1' with missing metadata")
  public void shouldThrowExceptionWhenPodHasNoSpec() throws Exception {
    // given
    PodData podData = new PodData(null, new ObjectMetaBuilder().withName("pod1").build());
    when(kubernetesEnvironment.getPodsData()).thenReturn(ImmutableMap.of("pod1", podData));

    // when
    podsValidator.validate(kubernetesEnvironment);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Pod 'pod1' contains volume 'user-data' with PVC sources that references missing PVC 'non-existing'")
  public void shouldThrowExceptionWhenPodHasVolumeThatReferencesMissingPVC() throws Exception {
    // given
    String podName = "pod1";
    Pod pod = createPod("pod1", "main");
    pod.getSpec()
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("user-data")
                .withNewPersistentVolumeClaim()
                .withClaimName("non-existing")
                .endPersistentVolumeClaim()
                .build());
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    when(kubernetesEnvironment.getPodsData()).thenReturn(ImmutableMap.of(podName, podData));
    when(kubernetesEnvironment.getMachines())
        .thenReturn(ImmutableMap.of(podName + "/main", mock(InternalMachineConfig.class)));

    // when
    podsValidator.validate(kubernetesEnvironment);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Container 'main' in pod 'pod1' contains volume mount that references missing volume 'non-existing'")
  public void shouldThrowExceptionWhenContainerHasVolumeMountThatReferencesMissingPodVolume()
      throws Exception {
    // given
    String podName = "pod1";
    Pod pod = createPod("pod1", "main");
    pod.getSpec()
        .getContainers()
        .get(0)
        .getVolumeMounts()
        .add(new VolumeMountBuilder().withName("non-existing").withMountPath("/tmp/data").build());
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    when(kubernetesEnvironment.getPodsData()).thenReturn(ImmutableMap.of(podName, podData));
    when(kubernetesEnvironment.getMachines())
        .thenReturn(ImmutableMap.of(podName + "/main", mock(InternalMachineConfig.class)));

    // when
    podsValidator.validate(kubernetesEnvironment);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Container 'foo' in pod 'pod1' contains volume mount that references missing volume 'non-existing'")
  public void shouldThrowExceptionWhenInitContainerHasVolumeMountThatReferencesMissingPodVolume()
      throws Exception {
    // given
    String podName = "pod1";
    Pod pod = createPod("pod1", "main");
    pod.getSpec()
        .getInitContainers()
        .add(
            new ContainerBuilder()
                .withName("foo")
                .withVolumeMounts(
                    new VolumeMountBuilder()
                        .withName("non-existing")
                        .withMountPath("/tmp/data")
                        .build())
                .build());
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    when(kubernetesEnvironment.getPodsData()).thenReturn(ImmutableMap.of(podName, podData));
    when(kubernetesEnvironment.getMachines())
        .thenReturn(ImmutableMap.of(podName + "/main", mock(InternalMachineConfig.class)));

    // when
    podsValidator.validate(kubernetesEnvironment);
  }

  private Pod createPod(String name, String... containers) {
    return new PodBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withContainers(
            Arrays.stream(containers).map(this::createContainer).collect(Collectors.toList()))
        .endSpec()
        .build();
  }

  private Container createContainer(String name) {
    return new ContainerBuilder().withName(name).build();
  }
}
