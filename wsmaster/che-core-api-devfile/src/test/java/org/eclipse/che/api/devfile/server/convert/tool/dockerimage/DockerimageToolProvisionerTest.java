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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.model.Env;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.model.Volume;
import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class DockerimageToolProvisionerTest {

  private DockerimageToolProvisioner dockerimageToolProvisioner;

  @BeforeMethod
  public void setUp() {
    dockerimageToolProvisioner = new DockerimageToolProvisioner();
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Workspace with multiple `dockerimage` environments can not be converted to devfile")
  public void shouldThrowExceptionIfWorkspaceHasMultipleEnvironmentsWithKubernetesOpenShiftRecipes()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    EnvironmentImpl dockerimageEnv1 = new EnvironmentImpl();
    dockerimageEnv1.setRecipe(new RecipeImpl(DockerImageEnvironment.TYPE, null, null, null));
    workspaceConfig.getEnvironments().put("dockerimage1", dockerimageEnv1);

    EnvironmentImpl dockerimageEnv2 = new EnvironmentImpl();
    dockerimageEnv2.setRecipe(new RecipeImpl(DockerImageEnvironment.TYPE, null, null, null));
    workspaceConfig.getEnvironments().put("dockerimage2", dockerimageEnv2);

    // when
    dockerimageToolProvisioner.provision(new Devfile(), workspaceConfig);
  }

  @Test
  public void shouldNoNothingIfWorkspaceDoesNotHaveEnvironmentsWithKubernetesOpenShiftRecipes()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    EnvironmentImpl anotherEnv = new EnvironmentImpl();
    anotherEnv.setRecipe(new RecipeImpl("nonDockerimage", null, null, null));
    workspaceConfig.getEnvironments().put("anotherEnv", anotherEnv);

    // when
    dockerimageToolProvisioner.provision(new Devfile(), workspaceConfig);
  }

  @Test
  public void shouldProvisionDockerimageToolWhenDockerimageEnvironmentHasNoMachineConfiguration()
      throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    Devfile devfile = new Devfile();

    // when
    dockerimageToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool dockerimageTool = devfile.getTools().get(0);
    assertEquals(dockerimageTool.getName(), "dockerEnv");
    assertEquals(dockerimageTool.getImage(), "eclipse/ubuntu_jdk8:latest");
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Environment with 'dockerimage' recipe must contain only one machine configuration")
  public void
      shouldThrowAnExceptionIfTryToProvisionDockerimageToolWhenDockerimageEnvironmentWithMultipleMachinesConfiguration()
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
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    Devfile devfile = new Devfile();

    // when
    dockerimageToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool dockerimageTool = devfile.getTools().get(0);
    assertEquals(dockerimageTool.getName(), "dockerEnv");
    assertEquals(dockerimageTool.getId(), "eclipse/ubuntu_jdk8:latest");
  }

  @Test
  public void
      shouldProvisionDockerimageToolIfDockerimageEnvironmentWithMachineConfigurationWithServerIsProvided()
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
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    Devfile devfile = new Devfile();

    // when
    dockerimageToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool dockerimageTool = devfile.getTools().get(0);
    assertEquals(dockerimageTool.getEndpoints().size(), 1);
    Endpoint endpoint = dockerimageTool.getEndpoints().get(0);
    assertEquals(endpoint.getName(), "server");
    assertEquals(endpoint.getPort(), new Integer(8080));
    assertEquals(endpoint.getAttributes().size(), 3);
    assertEquals(endpoint.getAttributes().get("protocol"), "http");
    assertEquals(endpoint.getAttributes().get("path"), "/api");
    assertEquals(endpoint.getAttributes().get("public"), "true");
  }

  @Test
  public void
      shouldProvisionDockerimageToolIfDockerimageEnvironmentWithMachineConfigurationWithServerIsSpecifiedButPathAndProtocolAreMissing()
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
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    Devfile devfile = new Devfile();

    // when
    dockerimageToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool dockerimageTool = devfile.getTools().get(0);
    assertEquals(dockerimageTool.getEndpoints().size(), 1);
    Endpoint endpoint = dockerimageTool.getEndpoints().get(0);
    assertEquals(endpoint.getName(), "server");
    assertEquals(endpoint.getPort(), new Integer(8080));
    assertTrue(endpoint.getAttributes().isEmpty());
  }

  @Test
  public void
      shouldProvisionDockerimageToolIfDockerimageEnvironmentWithMachineConfigurationWithEnvVarsIsSpecified()
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
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    Devfile devfile = new Devfile();

    // when
    dockerimageToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool dockerimageTool = devfile.getTools().get(0);
    List<Env> toolEnv = dockerimageTool.getEnv();
    assertEquals(toolEnv.size(), 2);
    Optional<Env> env1Opt = toolEnv.stream().filter(e -> e.getName().equals("key1")).findAny();
    assertTrue(env1Opt.isPresent());
    assertEquals(env1Opt.get().getValue(), "value1");

    Optional<Env> env2Opt = toolEnv.stream().filter(e -> e.getName().equals("key2")).findAny();
    assertTrue(env2Opt.isPresent());
    assertEquals(env2Opt.get().getValue(), "value2");
  }

  @Test
  public void
      shouldProvisionDockerimageToolIfDockerimageEnvironmentWithMachineConfigurationWithVolumesIsSpecified()
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
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    Devfile devfile = new Devfile();

    // when
    dockerimageToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool dockerimageTool = devfile.getTools().get(0);
    assertEquals(dockerimageTool.getVolumes().size(), 1);
    Volume volume = dockerimageTool.getVolumes().get(0);
    assertEquals(volume.getName(), "data");
    assertEquals(volume.getContainerPath(), "/tmp/data");
  }

  @Test
  public void
      shouldProvisionDockerimageToolIfDockerimageEnvironmentWithMachineConfigurationWithProjectVolumeIfSpecified()
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
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    Devfile devfile = new Devfile();

    // when
    dockerimageToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool dockerimageTool = devfile.getTools().get(0);
    assertTrue(dockerimageTool.getVolumes().isEmpty());
    assertTrue(dockerimageTool.getMountSources());
  }

  @Test
  public void
      shouldProvisionDockerimageToolIfDockerimageEnvironmentWithMachineConfigurationWithMemoryLimitSet()
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
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    Devfile devfile = new Devfile();

    // when
    dockerimageToolProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getTools().size(), 1);
    Tool dockerimageTool = devfile.getTools().get(0);
    assertEquals(dockerimageTool.getMemoryLimit(), "1G");
  }
}
