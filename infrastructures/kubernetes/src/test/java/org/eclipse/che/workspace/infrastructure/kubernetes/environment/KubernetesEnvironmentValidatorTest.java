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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
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
 * Tests {@link KubernetesEnvironmentValidator}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesEnvironmentValidatorTest {
  @Mock private KubernetesEnvironment kubernetesEnvironment;

  private KubernetesEnvironmentValidator environmentValidator;

  @BeforeMethod
  public void setUp() throws Exception {
    environmentValidator = new KubernetesEnvironmentValidator();
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Environment should contain at least 1 pod")
  public void shouldThrowExceptionWhenEnvDoesNotHaveAnyPods() throws Exception {
    // given
    when(kubernetesEnvironment.getPodsData()).thenReturn(emptyMap());

    // when
    environmentValidator.validate(kubernetesEnvironment);
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
    environmentValidator.validate(kubernetesEnvironment);
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
