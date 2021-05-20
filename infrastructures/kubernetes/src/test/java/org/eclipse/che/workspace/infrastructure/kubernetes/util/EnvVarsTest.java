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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.util.EnvVars.extractReferencedVariables;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarSource;
import io.fabric8.kubernetes.api.model.EnvVarSourceBuilder;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.SecretKeySelectorBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EnvVarsTest {

  private EnvVars envVars = new EnvVars();

  @Test(dataProvider = "detectReferencesTestValues")
  public void shouldDetectReferences(String value, Set<EnvVar> expected, String caseName) {
    assertEquals(
        extractReferencedVariables(var("name", value)), expected, caseName + ": just value");
    assertEquals(
        extractReferencedVariables(var("name", "v" + value)),
        expected,
        caseName + ": value with prefix");
    assertEquals(
        extractReferencedVariables(var("name", "v" + value + "v")),
        expected,
        caseName + ": value with prefix and postfix");
    assertEquals(
        extractReferencedVariables(var("name", value + "v")),
        expected,
        caseName + ": value with postfix");
  }

  @DataProvider
  public static Object[][] detectReferencesTestValues() {
    return new Object[][] {
      new Object[] {"value", emptySet(), "no refs"},
      new Object[] {"$(NO_REF", emptySet(), "unclosed ref"},
      new Object[] {"$$(NO_REF)", emptySet(), "escaped ref"},
      new Object[] {"$NO_REF)", emptySet(), "invalid start ref"},
      new Object[] {"$(NO REF)", emptySet(), "invalid name ref"},
      new Object[] {"$(REF)", singleton("REF"), "valid ref"}
    };
  }

  @Test
  public void shouldDetectMultipleReferences() throws Exception {
    // given
    EnvVar envVar = var("a", "$(b) $(c) $$(d)");

    // when
    Set<String> refs = extractReferencedVariables(envVar);

    // then
    assertEquals(refs, new HashSet<>(Arrays.asList("b", "c")));
  }

  @Test
  public void shouldReturnEmptySetOnEnvContainingValueFrom() throws Exception {
    // given
    EnvVar envVar = var("a", null);
    envVar.setValueFrom(
        new EnvVarSourceBuilder()
            .withSecretKeyRef(
                new SecretKeySelectorBuilder()
                    .withName("secret_name")
                    .withKey("secret_key")
                    .build())
            .build());

    // when
    Set<String> refs = extractReferencedVariables(envVar);

    // then
    assertTrue(refs.isEmpty());
  }

  @Test
  public void shouldProvisionEnvIfContainersDoeNotHaveEnvAtAll() throws Exception {
    // given
    PodData pod =
        new PodData(
            new PodBuilder()
                .withNewMetadata()
                .withName("pod")
                .endMetadata()
                .withNewSpec()
                .withInitContainers(new ContainerBuilder().withName("initContainer").build())
                .withContainers(new ContainerBuilder().withName("container").build())
                .endSpec()
                .build());

    // when
    envVars.apply(pod, singletonList(new EnvImpl("TEST_ENV", "anyValue")));

    // then
    List<EnvVar> initCEnv = pod.getSpec().getInitContainers().get(0).getEnv();
    assertEquals(initCEnv.size(), 1);
    assertEquals(initCEnv.get(0), new EnvVar("TEST_ENV", "anyValue", null));

    List<EnvVar> containerEnv = pod.getSpec().getContainers().get(0).getEnv();
    assertEquals(containerEnv.size(), 1);
    assertEquals(containerEnv.get(0), new EnvVar("TEST_ENV", "anyValue", null));
  }

  @Test
  public void shouldOverrideEnvOnApplyingEnvVarIfContainersAlreadyHaveSomeEnvVars()
      throws Exception {
    // given
    EnvVar existingInitCEnvVar = new EnvVar("TEST_ENV", "value", null);
    EnvVar existingCEnvVar = new EnvVar("TEST_ENV", null, new EnvVarSource());
    PodData pod =
        new PodData(
            new PodBuilder()
                .withNewMetadata()
                .withName("pod")
                .endMetadata()
                .withNewSpec()
                .withInitContainers(
                    new ContainerBuilder()
                        .withName("initContainer")
                        .withEnv(copy(existingInitCEnvVar))
                        .build())
                .withContainers(
                    new ContainerBuilder()
                        .withName("container")
                        .withEnv(copy(existingCEnvVar))
                        .build())
                .endSpec()
                .build());

    // when
    envVars.apply(pod, singletonList(new EnvImpl("TEST_ENV", "anyValue")));

    // then
    List<EnvVar> initCEnv = pod.getSpec().getInitContainers().get(0).getEnv();
    assertEquals(initCEnv.size(), 1);
    assertEquals(initCEnv.get(0), new EnvVar("TEST_ENV", "anyValue", null));

    List<EnvVar> containerEnv = pod.getSpec().getContainers().get(0).getEnv();
    assertEquals(containerEnv.size(), 1);
    assertEquals(containerEnv.get(0), new EnvVar("TEST_ENV", "anyValue", null));
  }

  @Test
  public void shouldProvisionEnvIntoK8SListIfContainerAlreadyHasSomeEnvVars() throws Exception {

    // given
    EnvVar existingInitCEnvVar = new EnvVar("ENV", "value", null);
    EnvVar existingCEnvVar = new EnvVar("ENV", null, new EnvVarSource());
    PodData pod =
        new PodData(
            new PodBuilder()
                .withNewMetadata()
                .withName("pod")
                .endMetadata()
                .withNewSpec()
                .withInitContainers(
                    new ContainerBuilder()
                        .withName("initContainer")
                        .withEnv(copy(existingInitCEnvVar))
                        .build())
                .withContainers(
                    new ContainerBuilder()
                        .withName("container")
                        .withEnv(copy(existingCEnvVar))
                        .build())
                .endSpec()
                .build());

    // when
    envVars.apply(pod, singletonList(new EnvImpl("TEST_ENV", "anyValue")));

    // then
    List<EnvVar> initCEnv = pod.getSpec().getInitContainers().get(0).getEnv();
    assertEquals(initCEnv.size(), 2);
    assertEquals(initCEnv.get(0), existingInitCEnvVar);
    assertEquals(initCEnv.get(1), new EnvVar("TEST_ENV", "anyValue", null));

    List<EnvVar> containerEnv = pod.getSpec().getContainers().get(0).getEnv();
    assertEquals(containerEnv.size(), 2);
    assertEquals(containerEnv.get(0), existingCEnvVar);
    assertEquals(containerEnv.get(1), new EnvVar("TEST_ENV", "anyValue", null));
  }

  private static EnvVar var(String name, String value) {
    return new EnvVar(name, value, null);
  }

  private EnvVar copy(EnvVar envVar) {
    return new EnvVar(envVar.getName(), envVar.getValue(), envVar.getValueFrom());
  }
}
