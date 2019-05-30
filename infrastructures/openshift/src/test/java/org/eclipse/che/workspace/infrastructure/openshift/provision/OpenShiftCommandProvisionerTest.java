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

import static org.eclipse.che.api.workspace.shared.Constants.ARBITRARY_USER_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.CONTAINER_SOURCE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.RECIPE_CONTAINER_SOURCE;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftCommandProvisioner.ADD_USER_COMMAND;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftCommandProvisioner.COMMAND_FORMAT;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftCommandProvisioner.SHELL_ARGS;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftCommandProvisioner.SHELL_BINARY;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class OpenShiftCommandProvisionerTest {

  private static String POD_NAME = "testPod";
  private static String RECIPE_CONTAINER_NAME = "recipeContainer";
  private static String SIDECAR_CONTAINER_NAME = "nonRecipeContainer";

  private static List<String> CONTAINER_COMMAND = ImmutableList.of("testCommand");
  private static List<String> CONTAINER_ARGS = ImmutableList.of("test", "args");

  private static List<String> UPDATED_COMMAND =
      ImmutableList.of(
          SHELL_ARGS,
          String.format(
              COMMAND_FORMAT,
              ADD_USER_COMMAND,
              String.join(" ", CONTAINER_COMMAND),
              String.join(" ", CONTAINER_ARGS)));

  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  @Mock private InternalMachineConfig recipeMachine;
  @Mock private InternalMachineConfig sidecarMachine;

  Container recipeContainer;
  Container sidecarContainer;
  PodData podData;

  private OpenShiftCommandProvisioner provisioner;

  @BeforeMethod
  public void setup() {
    recipeContainer = buildContainer(RECIPE_CONTAINER_NAME);
    sidecarContainer = buildContainer(SIDECAR_CONTAINER_NAME);

    podData = buildPodData(POD_NAME, recipeContainer, sidecarContainer);
    when(osEnv.getPodsData()).thenReturn(ImmutableMap.of(POD_NAME, podData));

    when(recipeMachine.getAttributes())
        .thenReturn(ImmutableMap.of(CONTAINER_SOURCE_ATTRIBUTE, RECIPE_CONTAINER_SOURCE));
    when(sidecarMachine.getAttributes())
        .thenReturn(ImmutableMap.of(CONTAINER_SOURCE_ATTRIBUTE, "plugin"));

    when(osEnv.getMachines())
        .thenReturn(
            ImmutableMap.of(
                Names.machineName(podData, recipeContainer),
                recipeMachine,
                Names.machineName(podData, sidecarContainer),
                sidecarMachine));

    when(osEnv.getPodsData()).thenReturn(ImmutableMap.of(POD_NAME, podData));

    this.provisioner = new OpenShiftCommandProvisioner();
  }

  @Test
  public void shouldDoNothingWhenAttributeNotPresent() throws Exception {
    when(osEnv.getAttributes()).thenReturn(Collections.emptyMap());

    provisioner.provision(osEnv, runtimeIdentity);

    verify(osEnv).getAttributes();
    verifyNoMoreInteractions(osEnv);
  }

  @Test
  public void shouldDoNothingWhenAttributeIsFalse() throws Exception {
    when(osEnv.getAttributes()).thenReturn(ImmutableMap.of(ARBITRARY_USER_ATTRIBUTE, "false"));

    provisioner.provision(osEnv, runtimeIdentity);

    verify(osEnv).getAttributes();
    verifyNoMoreInteractions(osEnv);
  }

  @Test
  public void shouldUpdateCommandAndArgsForRecipeMachine() throws Exception {
    when(osEnv.getAttributes()).thenReturn(ImmutableMap.of(ARBITRARY_USER_ATTRIBUTE, "true"));

    provisioner.provision(osEnv, runtimeIdentity);

    PodData actualPod = osEnv.getPodsData().get(POD_NAME);
    Container actual =
        actualPod
            .getSpec()
            .getContainers()
            .stream()
            .filter(c -> c.getName().equals(RECIPE_CONTAINER_NAME))
            .findFirst()
            .get();

    assertEquals(actual.getName(), RECIPE_CONTAINER_NAME, "Should not modify container name");
    assertEquals(actual.getCommand(), SHELL_BINARY, "Should update container Command");
    assertEquals(actual.getArgs(), UPDATED_COMMAND, "Should update conatiner Args");
  }

  @Test
  public void shouldDoNothingForSidecarMachine() throws Exception {
    when(osEnv.getAttributes()).thenReturn(ImmutableMap.of(ARBITRARY_USER_ATTRIBUTE, "true"));

    provisioner.provision(osEnv, runtimeIdentity);

    PodData actualPod = osEnv.getPodsData().get(POD_NAME);
    Container actual =
        actualPod
            .getSpec()
            .getContainers()
            .stream()
            .filter(c -> c.getName().equals(SIDECAR_CONTAINER_NAME))
            .findFirst()
            .get();

    assertEquals(actual.getName(), SIDECAR_CONTAINER_NAME);
    assertEquals(
        actual.getCommand(), CONTAINER_COMMAND, "Should not change non-recipe container Command");
    assertEquals(actual.getArgs(), CONTAINER_ARGS, "Should not change non-recipe container Args");
  }

  @Test
  public void shouldNotRewriteWhenOriginalCommandIsNull() throws Exception {
    when(osEnv.getAttributes()).thenReturn(ImmutableMap.of(ARBITRARY_USER_ATTRIBUTE, "true"));
    Container noCommand = buildContainer(RECIPE_CONTAINER_NAME);
    PodData podData = buildPodData(POD_NAME, noCommand, sidecarContainer);
    podData
        .getSpec()
        .getContainers()
        .stream()
        .filter(c -> RECIPE_CONTAINER_NAME.equals(c.getName()))
        .forEach(
            c -> {
              c.setCommand(null);
              c.setArgs(null);
            });
    when(osEnv.getPodsData()).thenReturn(ImmutableMap.of(POD_NAME, podData));

    provisioner.provision(osEnv, runtimeIdentity);

    PodData actualPod = osEnv.getPodsData().get(POD_NAME);
    Container actual =
        actualPod
            .getSpec()
            .getContainers()
            .stream()
            .filter(c -> c.getName().equals(RECIPE_CONTAINER_NAME))
            .findFirst()
            .get();

    assertEquals(actual.getName(), RECIPE_CONTAINER_NAME);
    assertNull(
        actual.getCommand(), "Should not do anything to command when container command is null");
    assertNull(actual.getArgs(), "Should not do anything to args when container command is null");
  }

  @Test
  public void shouldNotRewriteWhenOriginalCommandIsEmpty() throws Exception {
    when(osEnv.getAttributes()).thenReturn(ImmutableMap.of(ARBITRARY_USER_ATTRIBUTE, "true"));
    Container noCommand = buildContainer(RECIPE_CONTAINER_NAME);
    noCommand.setCommand(Collections.emptyList());
    noCommand.setArgs(Collections.emptyList());
    PodData podData = buildPodData(POD_NAME, noCommand, sidecarContainer);
    when(osEnv.getPodsData()).thenReturn(ImmutableMap.of(POD_NAME, podData));

    provisioner.provision(osEnv, runtimeIdentity);

    PodData actualPod = osEnv.getPodsData().get(POD_NAME);
    Container actual =
        actualPod
            .getSpec()
            .getContainers()
            .stream()
            .filter(c -> c.getName().equals(RECIPE_CONTAINER_NAME))
            .findFirst()
            .get();

    assertEquals(actual.getName(), RECIPE_CONTAINER_NAME);
    assertTrue(
        actual.getCommand().isEmpty(),
        "Should not do anything to command when container command is empty");
    assertTrue(
        actual.getArgs().isEmpty(),
        "Should not do anything to args when container command is empty");
  }

  private Container buildContainer(String name) {
    return new ContainerBuilder()
        .withName(name)
        .withCommand(CONTAINER_COMMAND)
        .withArgs(CONTAINER_ARGS)
        .build();
  }

  private PodData buildPodData(String name, Container... containers) {
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(name)
            .endMetadata()
            .withNewSpec()
            .withContainers(containers)
            .endSpec()
            .build();
    return new PodData(pod);
  }
}
