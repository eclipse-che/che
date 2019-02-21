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
package org.eclipse.che.api.devfile.server.convert.tool.dockerimage;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.model.Env;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.model.Volume;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class DockerimageToolToWorkspaceApplierTest {

  private static final String PROJECTS_MOUNT_PATH = "/projects";

  private WorkspaceConfigImpl workspaceConfig;

  private DockerimageToolToWorkspaceApplier dockerimageToolApplier;

  @BeforeMethod
  public void setUp() {
    dockerimageToolApplier = new DockerimageToolToWorkspaceApplier(PROJECTS_MOUNT_PATH);
    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test
  public void shouldProvisionEnvironmentWithDockerimageRecipe() throws Exception {
    // given
    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G");

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    String defaultEnvName = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnvName);
    EnvironmentImpl defaultEnv = workspaceConfig.getEnvironments().get(defaultEnvName);
    RecipeImpl recipe = defaultEnv.getRecipe();
    assertEquals(recipe.getType(), "dockerimage");
    assertEquals(recipe.getContent(), "eclipse/ubuntu_jdk8:latest");

    assertEquals(defaultEnv.getMachines().size(), 1);

    MachineConfigImpl machineConfig = defaultEnv.getMachines().get("jdk");
    assertNotNull(machineConfig);
  }

  @Test
  public void shouldProvisionMachineConfigWithEnvVarSpecified() throws Exception {
    // given
    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G")
            .withEnv(singletonList(new Env().withName("envName").withValue("envValue")));

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    MachineConfigImpl machineConfig = getMachineConfig(workspaceConfig, "jdk");
    Map<String, String> envVars = machineConfig.getEnv();
    assertEquals(envVars.size(), 1);
    assertEquals(envVars.get("envName"), "envValue");
  }

  @Test
  public void shouldProvisionMachineConfigWithMemoryLimitAttributeServersSpecified()
      throws Exception {
    // given
    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G");

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    MachineConfigImpl machineConfig = getMachineConfig(workspaceConfig, "jdk");
    assertEquals(machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE), "1000000000");
  }

  @Test
  public void shouldProvisionMachineConfigWithConfiguredServers() throws Exception {
    // given
    Endpoint endpoint =
        new Endpoint()
            .withName("jdk-ls")
            .withPort(4923)
            .withAttributes(
                ImmutableMap.of(
                    "protocol",
                    "http",
                    "path",
                    "/ls",
                    "public",
                    "false",
                    "secure",
                    "false",
                    "discoverable",
                    "true"));
    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G")
            .withEndpoints(singletonList(endpoint));

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    MachineConfigImpl machineConfig = getMachineConfig(workspaceConfig, "jdk");
    assertEquals(machineConfig.getServers().size(), 1);
    ServerConfigImpl serverConfig = machineConfig.getServers().get("jdk-ls");
    assertEquals(serverConfig.getProtocol(), "http");
    assertEquals(serverConfig.getPath(), "/ls");
    assertEquals(serverConfig.getPort(), "4923");
    Map<String, String> attributes = serverConfig.getAttributes();
    assertEquals(attributes.size(), 3);
    assertEquals(attributes.get("public"), "false");
    assertEquals(attributes.get("secure"), "false");
    assertEquals(attributes.get("discoverable"), "true");
  }

  @Test
  public void shouldProvisionServersWithHttpPortIsTheCorrespondingAttrIsMissing() throws Exception {
    // given
    Endpoint endpoint = new Endpoint().withName("jdk-ls").withPort(4923).withAttributes(emptyMap());
    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G")
            .withEndpoints(singletonList(endpoint));

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    MachineConfigImpl machineConfig = getMachineConfig(workspaceConfig, "jdk");
    assertEquals(machineConfig.getServers().size(), 1);
    ServerConfigImpl serverConfig = machineConfig.getServers().get("jdk-ls");
    assertEquals(serverConfig.getProtocol(), "http");
  }

  @Test
  public void shouldProvisionMachineConfigWithConfiguredVolumes() throws Exception {
    // given
    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G")
            .withVolumes(
                singletonList(new Volume().withName("data").withContainerPath("/tmp/data/")));

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    MachineConfigImpl machineConfig = getMachineConfig(workspaceConfig, "jdk");
    VolumeImpl volume = machineConfig.getVolumes().get("data");
    assertNotNull(volume);
    assertEquals(volume.getPath(), "/tmp/data/");
  }

  @Test
  public void shouldProvisionMachineConfigWithMountSources() throws Exception {
    // given
    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G")
            .withMountSources(true);

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    MachineConfigImpl machineConfig = getMachineConfig(workspaceConfig, "jdk");
    VolumeImpl projectsVolume = machineConfig.getVolumes().get(PROJECTS_VOLUME_NAME);
    assertNotNull(projectsVolume);
    assertEquals(projectsVolume.getPath(), PROJECTS_MOUNT_PATH);
  }

  @Test
  public void shouldProvisionMachineConfigWithoutSourcesByDefault() throws Exception {
    // given
    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G");

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    MachineConfigImpl machineConfig = getMachineConfig(workspaceConfig, "jdk");
    Assert.assertFalse(machineConfig.getVolumes().containsKey(PROJECTS_VOLUME_NAME));
  }

  private MachineConfigImpl getMachineConfig(
      WorkspaceConfigImpl workspaceConfig, String machineName) {
    String defaultEnvName = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnvName);

    EnvironmentImpl defaultEnv = workspaceConfig.getEnvironments().get(defaultEnvName);
    assertNotNull(defaultEnv);

    MachineConfigImpl machineConfig = defaultEnv.getMachines().get(machineName);
    assertNotNull(machineConfig);

    return machineConfig;
  }
}
