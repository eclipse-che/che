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
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.devfile.model.Endpoint;
import org.eclipse.che.api.devfile.model.Env;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.model.Volume;
import org.eclipse.che.api.devfile.server.convert.tool.kubernetes.KubernetesEnvironmentProvisioner;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class DockerimageToolToWorkspaceApplierTest {

  private static final String PROJECTS_MOUNT_PATH = "/projects";

  private WorkspaceConfigImpl workspaceConfig;

  private DockerimageToolToWorkspaceApplier dockerimageToolApplier;

  @Mock private KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @Captor private ArgumentCaptor<List<HasMetadata>> objectsCaptor;
  @Captor private ArgumentCaptor<Map<String, MachineConfigImpl>> machinesCaptor;

  @BeforeMethod
  public void setUp() throws Exception {
    dockerimageToolApplier =
        new DockerimageToolToWorkspaceApplier(PROJECTS_MOUNT_PATH, k8sEnvProvisioner);
    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test
  public void
      shouldProvisionK8sEnvironmentWithMachineConfigAndGeneratedDeploymentForSpecifiedDockerimage()
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
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk");
    assertNotNull(machineConfig);

    List<HasMetadata> objects = objectsCaptor.getValue();
    assertEquals(objects.size(), 1);
    assertTrue(objects.get(0) instanceof Deployment);
    Deployment deployment = (Deployment) objects.get(0);
    PodTemplateSpec podTemplate = deployment.getSpec().getTemplate();
    Map<String, String> annotations = podTemplate.getMetadata().getAnnotations();
    assertEquals(annotations.get(String.format(MACHINE_NAME_ANNOTATION_FMT, "jdk")), "jdk");
    Container container = podTemplate.getSpec().getContainers().get(0);
    assertEquals(container.getName(), "jdk");
    assertEquals(container.getImage(), "eclipse/ubuntu_jdk8:latest");
  }

  @Test
  public void shouldProvisionSpecifiedEnvVars() throws Exception {
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
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    List<HasMetadata> objects = objectsCaptor.getValue();
    assertEquals(objects.size(), 1);
    assertTrue(objects.get(0) instanceof Deployment);
    Deployment deployment = (Deployment) objects.get(0);
    PodTemplateSpec podTemplate = deployment.getSpec().getTemplate();
    assertEquals(podTemplate.getSpec().getContainers().size(), 1);
    Container container = podTemplate.getSpec().getContainers().get(0);
    int env = container.getEnv().size();
    assertEquals(env, 1);
    EnvVar containerEnvVar = container.getEnv().get(0);
    assertEquals(containerEnvVar.getName(), "envName");
    assertEquals(containerEnvVar.getValue(), "envValue");
  }

  @Test
  public void shouldProvisionContainerWithMemoryLimitSpecified() throws Exception {
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
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    List<HasMetadata> objects = objectsCaptor.getValue();
    assertEquals(objects.size(), 1);
    assertTrue(objects.get(0) instanceof Deployment);
    Deployment deployment = (Deployment) objects.get(0);
    PodTemplateSpec podTemplate = deployment.getSpec().getTemplate();
    assertEquals(podTemplate.getSpec().getContainers().size(), 1);
    Container container = podTemplate.getSpec().getContainers().get(0);
    Quantity memoryLimit = container.getResources().getLimits().get("memory");
    assertEquals(memoryLimit.getAmount(), "1G");
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
                    PUBLIC_ENDPOINT_ATTRIBUTE,
                    "false",
                    "secure",
                    "false"));
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
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk");
    assertNotNull(machineConfig);
    assertEquals(machineConfig.getServers().size(), 1);
    ServerConfigImpl serverConfig = machineConfig.getServers().get("jdk-ls");
    assertEquals(serverConfig.getProtocol(), "http");
    assertEquals(serverConfig.getPath(), "/ls");
    assertEquals(serverConfig.getPort(), "4923");
    Map<String, String> attributes = serverConfig.getAttributes();
    assertEquals(attributes.size(), 2);
    assertEquals(attributes.get(ServerConfig.INTERNAL_SERVER_ATTRIBUTE), "true");
    assertEquals(attributes.get("secure"), "false");
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
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk");
    assertNotNull(machineConfig);
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
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk");
    assertNotNull(machineConfig);
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
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk");
    assertNotNull(machineConfig);
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
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk");
    assertNotNull(machineConfig);
    assertFalse(machineConfig.getVolumes().containsKey(PROJECTS_VOLUME_NAME));
  }

  @Test
  public void shouldProvisionCommandAndArgs() throws Exception {
    // given
    List<String> command = singletonList("/usr/bin/rf");
    List<String> args = Arrays.asList("-r", "f");

    Tool dockerimageTool =
        new Tool()
            .withName("jdk")
            .withType(DOCKERIMAGE_TOOL_TYPE)
            .withImage("eclipse/ubuntu_jdk8:latest")
            .withMemoryLimit("1G")
            .withCommand(command)
            .withArgs(args);

    // when
    dockerimageToolApplier.apply(workspaceConfig, dockerimageTool, null);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    List<HasMetadata> objects = objectsCaptor.getValue();
    assertEquals(objects.size(), 1);
    assertTrue(objects.get(0) instanceof Deployment);
    Deployment deployment = (Deployment) objects.get(0);
    PodTemplateSpec podTemplate = deployment.getSpec().getTemplate();
    assertEquals(podTemplate.getSpec().getContainers().size(), 1);
    Container container = podTemplate.getSpec().getContainers().get(0);
    assertEquals(container.getCommand(), command);
    assertEquals(container.getArgs(), args);
  }
}
