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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl;
import org.eclipse.che.workspace.infrastructure.docker.environment.dockerimage.DockerImageEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPoint;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.util.EntryPointParser;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class DockerimageComponentProvisionerTest {

  private DockerimageComponentProvisioner dockerimageComponentProvisioner;

  @Mock private EntryPointParser entryPointParser;

  @BeforeMethod
  public void setUp() throws Exception {
    dockerimageComponentProvisioner = new DockerimageComponentProvisioner(entryPointParser);
    when(entryPointParser.parse(any())).thenReturn(new EntryPoint(emptyList(), emptyList()));
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
    dockerimageComponentProvisioner.provision(new DevfileImpl(), workspaceConfig);
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
    dockerimageComponentProvisioner.provision(new DevfileImpl(), workspaceConfig);
  }

  @Test
  public void
      shouldProvisionDockerimageComponentWhenDockerimageEnvironmentHasNoMachineConfiguration()
          throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    DevfileImpl devfile = new DevfileImpl();

    // when
    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl dockerimageComponent = devfile.getComponents().get(0);
    assertEquals(dockerimageComponent.getAlias(), "dockerEnv");
    assertEquals(dockerimageComponent.getImage(), "eclipse/ubuntu_jdk8:latest");
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Environment with 'dockerimage' recipe must contain only one machine configuration")
  public void
      shouldThrowAnExceptionIfTryToProvisionDockerimageComponentWhenDockerimageEnvironmentWithMultipleMachinesConfiguration()
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

    DevfileImpl devfile = new DevfileImpl();

    // when
    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl dockerimageComponent = devfile.getComponents().get(0);
    assertEquals(dockerimageComponent.getAlias(), "dockerEnv");
    assertEquals(dockerimageComponent.getId(), "eclipse/ubuntu_jdk8:latest");
  }

  @Test
  public void
      shouldProvisionDockerimageComponentIfDockerimageEnvironmentWithMachineConfigurationWithServerIsProvided()
          throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    ServerConfigImpl serverConfig =
        new ServerConfigImpl(
            "8080/TCP", "http", "/api", ImmutableMap.of(PUBLIC_ENDPOINT_ATTRIBUTE, "true"));
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

    DevfileImpl devfile = new DevfileImpl();

    // when
    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl dockerimageComponent = devfile.getComponents().get(0);
    assertEquals(dockerimageComponent.getEndpoints().size(), 1);
    EndpointImpl endpoint = dockerimageComponent.getEndpoints().get(0);
    assertEquals(endpoint.getName(), "server");
    assertEquals(endpoint.getPort(), Integer.valueOf(8080));
    assertEquals(endpoint.getAttributes().size(), 3);
    assertEquals(endpoint.getAttributes().get("protocol"), "http");
    assertEquals(endpoint.getAttributes().get("path"), "/api");
    assertEquals(endpoint.getAttributes().get(PUBLIC_ENDPOINT_ATTRIBUTE), "true");
  }

  @Test
  public void
      shouldProvisionDockerimageComponentIfDockerimageEnvironmentWithMachineConfigurationWithServerIsSpecifiedButPathAndProtocolAreMissing()
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

    DevfileImpl devfile = new DevfileImpl();

    // when
    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl dockerimageComponent = devfile.getComponents().get(0);
    assertEquals(dockerimageComponent.getEndpoints().size(), 1);
    EndpointImpl endpoint = dockerimageComponent.getEndpoints().get(0);
    assertEquals(endpoint.getName(), "server");
    assertEquals(endpoint.getPort(), Integer.valueOf(8080));
    assertTrue(endpoint.getAttributes().isEmpty());
  }

  @Test
  public void
      shouldProvisionDockerimageComponentIfDockerimageEnvironmentWithMachineConfigurationWithEnvVarsIsSpecified()
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

    DevfileImpl devfile = new DevfileImpl();

    // when
    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl dockerimageComponent = devfile.getComponents().get(0);
    List<EnvImpl> ComponentEnv = dockerimageComponent.getEnv();
    assertEquals(ComponentEnv.size(), 2);
    Optional<EnvImpl> env1Opt =
        ComponentEnv.stream().filter(e -> e.getName().equals("key1")).findAny();
    assertTrue(env1Opt.isPresent());
    assertEquals(env1Opt.get().getValue(), "value1");

    Optional<EnvImpl> env2Opt =
        ComponentEnv.stream().filter(e -> e.getName().equals("key2")).findAny();
    assertTrue(env2Opt.isPresent());
    assertEquals(env2Opt.get().getValue(), "value2");
  }

  @Test
  public void
      shouldProvisionDockerimageComponentIfDockerimageEnvironmentWithMachineConfigurationWithVolumesIsSpecified()
          throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    Map<String, org.eclipse.che.api.workspace.server.model.impl.VolumeImpl> volumes =
        ImmutableMap.of(
            "data",
            new org.eclipse.che.api.workspace.server.model.impl.VolumeImpl().withPath("/tmp/data"));
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(emptyList(), emptyMap(), emptyMap(), emptyMap(), volumes));
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    DevfileImpl devfile = new DevfileImpl();

    // when
    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl dockerimageComponent = devfile.getComponents().get(0);
    assertEquals(dockerimageComponent.getVolumes().size(), 1);
    VolumeImpl volume = dockerimageComponent.getVolumes().get(0);
    assertEquals(volume.getName(), "data");
    assertEquals(volume.getContainerPath(), "/tmp/data");
  }

  @Test
  public void
      shouldProvisionDockerimageComponentIfDockerimageEnvironmentWithMachineConfigurationWithProjectVolumeIfSpecified()
          throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    Map<String, org.eclipse.che.api.workspace.server.model.impl.VolumeImpl> volumes =
        ImmutableMap.of(
            PROJECTS_VOLUME_NAME,
            new org.eclipse.che.api.workspace.server.model.impl.VolumeImpl().withPath("/projects"));
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(emptyList(), emptyMap(), emptyMap(), emptyMap(), volumes));
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    DevfileImpl devfile = new DevfileImpl();

    // when
    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl dockerimageComponent = devfile.getComponents().get(0);
    assertTrue(dockerimageComponent.getVolumes().isEmpty());
    assertTrue(dockerimageComponent.getMountSources());
  }

  @Test
  public void
      shouldProvisionDockerimageComponentIfDockerimageEnvironmentWithMachineConfigurationWithMemoryLimitSet()
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

    DevfileImpl devfile = new DevfileImpl();

    // when
    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl dockerimageComponent = devfile.getComponents().get(0);
    assertEquals(dockerimageComponent.getMemoryLimit(), "1G");
  }

  @Test
  public void shouldIncludeEntrypointPropertiesWhenSpecifiedInTheEnvironment() throws Exception {
    // given
    EnvironmentImpl dockerEnv = new EnvironmentImpl();
    dockerEnv.setRecipe(new RecipeImpl("dockerimage", null, "eclipse/ubuntu_jdk8:latest", null));
    dockerEnv
        .getMachines()
        .put(
            "myMachine",
            new MachineConfigImpl(emptyList(), emptyMap(), emptyMap(), emptyMap(), emptyMap()));
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    workspaceConfig.getEnvironments().put("dockerEnv", dockerEnv);

    when(entryPointParser.parse(any()))
        .thenReturn(new EntryPoint(asList("/bin/sh", "-c"), asList("echo", "hi")));

    DevfileImpl devfile = new DevfileImpl();

    // when

    dockerimageComponentProvisioner.provision(devfile, workspaceConfig);

    // then
    assertEquals(devfile.getComponents().size(), 1);
    ComponentImpl Component = devfile.getComponents().get(0);
    assertEquals(Component.getCommand(), asList("/bin/sh", "-c"));
    assertEquals(Component.getArgs(), asList("echo", "hi"));
  }
}
