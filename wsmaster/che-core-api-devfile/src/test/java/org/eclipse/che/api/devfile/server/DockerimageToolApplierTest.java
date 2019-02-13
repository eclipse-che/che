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
package org.eclipse.che.api.devfile.server;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.devfile.model.Devfile;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests {@link DockerimageToolApplier}.
 *
 * @author Sergii Leshchenko
 */
public class DockerimageToolApplierTest {

  private static final String PROJECTS_MOUNT_PATH = "/projects";

  private Devfile devfile;
  private WorkspaceConfigImpl workspaceConfig;

  private DockerimageToolApplier applier;

  @BeforeMethod
  public void setUp() {
    applier = new DockerimageToolApplier(PROJECTS_MOUNT_PATH);
    workspaceConfig = new WorkspaceConfigImpl();
    devfile = new Devfile().withTools(new ArrayList<>());
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp = "The tool must have `dockerimage` type")
  public void shouldThrowExceptionIfNonDockerimageToolIsSpecified() throws Exception {
    applier.apply(new Tool().withType("cheEditor"), devfile, workspaceConfig);
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

    devfile.getTools().add(dockerimageTool);

    // when
    applier.apply(dockerimageTool, devfile, workspaceConfig);

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

    devfile.getTools().add(dockerimageTool);

    // when
    applier.apply(dockerimageTool, devfile, workspaceConfig);

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

    devfile.getTools().add(dockerimageTool);

    // when
    applier.apply(dockerimageTool, devfile, workspaceConfig);

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

    devfile.getTools().add(dockerimageTool);

    // when
    applier.apply(dockerimageTool, devfile, workspaceConfig);

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

    devfile.getTools().add(dockerimageTool);

    // when
    applier.apply(dockerimageTool, devfile, workspaceConfig);

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

    devfile.getTools().add(dockerimageTool);

    // when
    applier.apply(dockerimageTool, devfile, workspaceConfig);

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
    applier.apply(dockerimageTool, devfile, workspaceConfig);

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
    applier.apply(dockerimageTool, devfile, workspaceConfig);

    // then
    MachineConfigImpl machineConfig = getMachineConfig(workspaceConfig, "jdk");
    assertFalse(machineConfig.getVolumes().containsKey(PROJECTS_VOLUME_NAME));
  }

  @Test
  public void shouldCreateToolFromDockerimageEnvironmentWithoutMachineConfiguration()
      throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));

    // when
    Tool dockerTool = applier.from("dockerEnv", dockerEnv);

    // then
    assertEquals(dockerTool.getName(), "dockerEnv");
    assertEquals(dockerTool.getImage(), "eclipse/ubuntu_jdk8:latest");
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Environment with 'dockerimage' recipe must contain only one machine configuration")
  public void
      shouldThrowAnExceptionIfTryToCreateToolFromDockerimageEnvironmentWithMultipleMachinesConfiguration()
          throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    dockerEnv
        .getMachines()
        .put(
            "machine1",
            new MachineConfigImpl(emptyList(), emptyMap(), emptyMap(), emptyMap(), emptyMap()));
    dockerEnv
        .getMachines()
        .put(
            "machine2",
            new MachineConfigImpl(emptyList(), emptyMap(), emptyMap(), emptyMap(), emptyMap()));

    // when
    Tool dockerTool = applier.from("dockerEnv", dockerEnv);

    // then
    assertEquals(dockerTool.getName(), "dockerEnv");
    assertEquals(dockerTool.getId(), "eclipse/ubuntu_jdk8:latest");
  }

  @Test
  public void shouldCreateToolFromDockerimageEnvironmentWithMachineConfigurationWithServer()
      throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    ServerConfigImpl serverConfig =
        new ServerConfigImpl("8080/TCP", "http", "/api", ImmutableMap.of("public", "true"));
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(
                emptyList(),
                ImmutableMap.of("server", serverConfig),
                emptyMap(),
                emptyMap(),
                emptyMap()));

    // when
    Tool dockerTool = applier.from("dockerEnv", dockerEnv);

    // then
    assertEquals(dockerTool.getEndpoints().size(), 1);
    Endpoint endpoint = dockerTool.getEndpoints().get(0);
    assertEquals(endpoint.getName(), "server");
    assertEquals(endpoint.getPort(), new Integer(8080));
    assertEquals(endpoint.getAttributes().size(), 3);
    assertEquals(endpoint.getAttributes().get("protocol"), "http");
    assertEquals(endpoint.getAttributes().get("path"), "/api");
    assertEquals(endpoint.getAttributes().get("public"), "true");
  }

  @Test
  public void
      shouldCreateToolFromDockerimageEnvironmentWithMachineConfigurationWithServerWhenPathAndProtocolIsMissing()
          throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    ServerConfigImpl serverConfig = new ServerConfigImpl("8080/TCP", null, null, emptyMap());
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(
                emptyList(),
                ImmutableMap.of("server", serverConfig),
                emptyMap(),
                emptyMap(),
                emptyMap()));

    // when
    Tool dockerTool = applier.from("dockerEnv", dockerEnv);

    // then
    assertEquals(dockerTool.getEndpoints().size(), 1);
    Endpoint endpoint = dockerTool.getEndpoints().get(0);
    assertEquals(endpoint.getName(), "server");
    assertEquals(endpoint.getPort(), new Integer(8080));
    assertTrue(endpoint.getAttributes().isEmpty());
  }

  @Test
  public void shouldCreateToolFromDockerimageEnvironmentWithMachineConfigurationWithEnvVars()
      throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    Map<String, String> env = ImmutableMap.of("key1", "value1", "key2", "value2");
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(emptyList(), emptyMap(), env, emptyMap(), emptyMap()));

    // when
    Tool dockerTool = applier.from("dockerEnv", dockerEnv);

    // then
    List<Env> toolEnv = dockerTool.getEnv();
    assertEquals(toolEnv.size(), 2);
    Optional<Env> env1Opt = toolEnv.stream().filter(e -> e.getName().equals("key1")).findAny();
    assertTrue(env1Opt.isPresent());
    assertEquals(env1Opt.get().getValue(), "value1");

    Optional<Env> env2Opt = toolEnv.stream().filter(e -> e.getName().equals("key2")).findAny();
    assertTrue(env2Opt.isPresent());
    assertEquals(env2Opt.get().getValue(), "value2");
  }

  @Test
  public void shouldCreateToolFromDockerimageEnvironmentWithMachineConfigurationWithVolumes()
      throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    Map<String, VolumeImpl> volumes =
        ImmutableMap.of("data", new VolumeImpl().withPath("/tmp/data"));
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(emptyList(), emptyMap(), emptyMap(), emptyMap(), volumes));

    // when
    Tool dockerTool = applier.from("dockerEnv", dockerEnv);

    // then
    assertEquals(dockerTool.getVolumes().size(), 1);
    Volume volume = dockerTool.getVolumes().get(0);
    assertEquals(volume.getName(), "data");
    assertEquals(volume.getContainerPath(), "/tmp/data");
  }

  @Test
  public void shouldCreateToolFromDockerimageEnvironmentWithMachineConfigurationWithProjectVolume()
      throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    Map<String, VolumeImpl> volumes =
        ImmutableMap.of(PROJECTS_VOLUME_NAME, new VolumeImpl().withPath("/projects"));
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(emptyList(), emptyMap(), emptyMap(), emptyMap(), volumes));

    // when
    Tool dockerTool = applier.from("dockerEnv", dockerEnv);

    // then
    assertTrue(dockerTool.getVolumes().isEmpty());
    assertTrue(dockerTool.getMountSources());
  }

  @Test
  public void shouldCreateToolFromDockerimageEnvironmentWithMachineConfigurationWithMemoryLimitSet()
      throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    Map<String, String> attributes = new HashMap<>();
    attributes.put(MEMORY_LIMIT_ATTRIBUTE, "1G");
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(emptyList(), emptyMap(), emptyMap(), attributes, emptyMap()));

    // when
    Tool dockerTool = applier.from("dockerEnv", dockerEnv);

    // then
    assertEquals(dockerTool.getMemoryLimit(), "1G");
  }

  private MachineConfigImpl getMachineConfig(
      WorkspaceConfigImpl workspaceConfig, String machineName) {
    String defaultEnvName = workspaceConfig.getDefaultEnv();
    EnvironmentImpl defaultEnv = workspaceConfig.getEnvironments().get(defaultEnvName);

    MachineConfigImpl machineConfig = defaultEnv.getMachines().get(machineName);
    assertNotNull(machineConfig);

    return machineConfig;
  }
}
