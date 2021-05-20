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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.DockerimageComponentToWorkspaceApplier.CHE_COMPONENT_NAME_LABEL;
import static org.mockito.ArgumentMatchers.any;
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
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class DockerimageComponentToWorkspaceApplierTest {

  private static final String PROJECTS_MOUNT_PATH = "/projects";

  private WorkspaceConfigImpl workspaceConfig;

  private DockerimageComponentToWorkspaceApplier dockerimageComponentApplier;

  @Mock private KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @Captor private ArgumentCaptor<List<HasMetadata>> objectsCaptor;
  @Captor private ArgumentCaptor<Map<String, MachineConfigImpl>> machinesCaptor;

  @BeforeMethod
  public void setUp() throws Exception {
    dockerimageComponentApplier =
        new DockerimageComponentToWorkspaceApplier(
            PROJECTS_MOUNT_PATH, "Always", k8sEnvProvisioner);
    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test
  public void
      shouldProvisionK8sEnvironmentWithMachineConfigAndGeneratedDeploymentForSpecifiedDockerimage()
          throws Exception {
    // given
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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
    ObjectMeta podMeta = podTemplate.getMetadata();
    assertEquals(podMeta.getName(), "jdk");

    Map<String, String> deploymentSelector = deployment.getSpec().getSelector().getMatchLabels();
    assertFalse(deploymentSelector.isEmpty());
    assertTrue(podMeta.getLabels().entrySet().containsAll(deploymentSelector.entrySet()));

    Container container = podTemplate.getSpec().getContainers().get(0);
    assertEquals(container.getName(), "jdk");
    assertEquals(container.getImage(), "eclipse/ubuntu_jdk8:latest");
    assertEquals(container.getImagePullPolicy(), "Always");

    assertEquals(Names.machineName(podMeta, container), "jdk");
  }

  @Test
  public void shouldBeAbleToSetupSpecificImagePullPolicy() throws DevfileException {
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponentApplier =
        new DockerimageComponentToWorkspaceApplier(PROJECTS_MOUNT_PATH, "Never", k8sEnvProvisioner);

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            any(),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("eclipse-ubuntu_jdk8-latest");
    assertNotNull(machineConfig);

    List<HasMetadata> objects = objectsCaptor.getValue();
    assertEquals(objects.size(), 1);
    assertTrue(objects.get(0) instanceof Deployment);
    Deployment deployment = (Deployment) objects.get(0);
    PodTemplateSpec podTemplate = deployment.getSpec().getTemplate();
    ObjectMeta podMeta = podTemplate.getMetadata();
    assertEquals(podMeta.getName(), "eclipse-ubuntu_jdk8-latest");

    Map<String, String> deploymentSelector = deployment.getSpec().getSelector().getMatchLabels();
    assertFalse(deploymentSelector.isEmpty());
    assertTrue(podMeta.getLabels().entrySet().containsAll(deploymentSelector.entrySet()));

    Container container = podTemplate.getSpec().getContainers().get(0);
    assertEquals(container.getName(), "eclipse-ubuntu_jdk8-latest");
    assertEquals(container.getImagePullPolicy(), "Never");
  }

  @Test
  public void shouldProvisionK8sEnvironmentWithMachineConfigFromSpecifiedDockerimageWithoutAlias()
      throws Exception {
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            any(),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("eclipse-ubuntu_jdk8-latest");
    assertNotNull(machineConfig);

    List<HasMetadata> objects = objectsCaptor.getValue();
    assertEquals(objects.size(), 1);
    assertTrue(objects.get(0) instanceof Deployment);
    Deployment deployment = (Deployment) objects.get(0);
    PodTemplateSpec podTemplate = deployment.getSpec().getTemplate();
    ObjectMeta podMeta = podTemplate.getMetadata();
    assertEquals(podMeta.getName(), "eclipse-ubuntu_jdk8-latest");

    Map<String, String> deploymentSelector = deployment.getSpec().getSelector().getMatchLabels();
    assertFalse(deploymentSelector.isEmpty());
    assertTrue(podMeta.getLabels().entrySet().containsAll(deploymentSelector.entrySet()));

    Container container = podTemplate.getSpec().getContainers().get(0);
    assertEquals(container.getName(), "eclipse-ubuntu_jdk8-latest");
    assertEquals(container.getImage(), "eclipse/ubuntu_jdk8:latest");

    assertEquals(
        Names.machineName(podTemplate.getMetadata(), container), "eclipse-ubuntu_jdk8-latest");
  }

  @Test
  public void shouldProvisionSpecifiedEnvVars() throws Exception {
    // given
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setEnv(singletonList(new EnvImpl("envName", "envValue")));

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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
  public void shouldProvisionContainerWithMemoryResourcesSpecified() throws Exception {
    // given
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setMemoryRequest("128M");

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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
    assertEquals(memoryLimit.getAmount(), "1");
    assertEquals(memoryLimit.getFormat(), "G");

    Quantity memoryRequest = container.getResources().getRequests().get("memory");
    assertEquals(memoryRequest.getAmount(), "128");
    assertEquals(memoryRequest.getFormat(), "M");
  }

  @Test
  public void shouldProvisionContainerWithCpuLimitsSpecified() throws Exception {
    // given
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setCpuRequest("1576m");
    dockerimageComponent.setCpuLimit("2.22");

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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
    Quantity cpuLimit = container.getResources().getLimits().get("cpu");
    Quantity cpuRequest = container.getResources().getRequests().get("cpu");
    assertEquals(cpuRequest.getAmount(), "1.576");
    assertEquals(cpuLimit.getAmount(), "2.22");
  }

  @Test
  public void shouldProvisionMachineConfigWithConfiguredServers() throws Exception {
    // given
    EndpointImpl endpoint =
        new EndpointImpl(
            "jdk-ls",
            4923,
            ImmutableMap.of(
                "protocol",
                "http",
                "path",
                "/ls",
                PUBLIC_ENDPOINT_ATTRIBUTE,
                "false",
                "secure",
                "false"));
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setEndpoints(singletonList(endpoint));

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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
    assertEquals(attributes.get(ServerConfig.INTERNAL_SERVER_ATTRIBUTE), "true");
    assertEquals(attributes.get("secure"), "false");
    assertEquals(attributes.get(ServerConfig.REQUIRE_SUBDOMAIN), "true");
  }

  @Test
  public void shouldProvisionServiceForDiscoverableServer() throws Exception {
    // given
    EndpointImpl endpoint =
        new EndpointImpl(
            "jdk-ls",
            4923,
            ImmutableMap.of(
                "protocol",
                "http",
                "path",
                "/ls",
                PUBLIC_ENDPOINT_ATTRIBUTE,
                "false",
                "secure",
                "false",
                "discoverable",
                "true"));
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setEndpoints(singletonList(endpoint));

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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
    assertEquals(
        deployment.getSpec().getTemplate().getMetadata().getLabels().get(CHE_COMPONENT_NAME_LABEL),
        "jdk");
  }

  @Test
  public void shouldProvisionServersWithHttpPortIsTheCorrespondingAttrIsMissing() throws Exception {
    // given
    EndpointImpl endpoint = new EndpointImpl("jdk-ls", 4923, emptyMap());
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setEndpoints(singletonList(endpoint));

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setVolumes(singletonList(new VolumeImpl("data", "/tmp/data/")));

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk");
    assertNotNull(machineConfig);
    org.eclipse.che.api.workspace.server.model.impl.VolumeImpl volume =
        machineConfig.getVolumes().get("data");
    assertNotNull(volume);
    assertEquals(volume.getPath(), "/tmp/data/");
  }

  @Test
  public void shouldProvisionMachineConfigWithMountSources() throws Exception {
    // given
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setMountSources(true);

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk");
    assertNotNull(machineConfig);
    org.eclipse.che.api.workspace.server.model.impl.VolumeImpl projectsVolume =
        machineConfig.getVolumes().get(PROJECTS_VOLUME_NAME);
    assertNotNull(projectsVolume);
    assertEquals(projectsVolume.getPath(), PROJECTS_MOUNT_PATH);
  }

  @Test
  public void shouldProvisionMachineConfigWithAliasAttribute() throws Exception {
    // given
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk-alias");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            machinesCaptor.capture());
    MachineConfigImpl machineConfig = machinesCaptor.getValue().get("jdk-alias");
    assertNotNull(machineConfig);
    assertEquals(machineConfig.getAttributes().get(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE), "jdk-alias");
  }

  @Test
  public void shouldProvisionMachineConfigWithoutSourcesByDefault() throws Exception {
    // given
    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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

    ComponentImpl dockerimageComponent = new ComponentImpl();
    dockerimageComponent.setAlias("jdk");
    dockerimageComponent.setType(DOCKERIMAGE_COMPONENT_TYPE);
    dockerimageComponent.setImage("eclipse/ubuntu_jdk8:latest");
    dockerimageComponent.setMemoryLimit("1G");
    dockerimageComponent.setCommand(command);
    dockerimageComponent.setArgs(args);

    // when
    dockerimageComponentApplier.apply(workspaceConfig, dockerimageComponent, null);

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

  @Test(dataProvider = "imageNames")
  public void testGeneratesValidMachineNameFromImageName(String imageName)
      throws ValidationException, DevfileException {

    // given
    String machineName = DockerimageComponentToWorkspaceApplier.toMachineName(imageName);
    MachineConfigsValidator validator = new MachineConfigsValidator();
    Map<String, InternalMachineConfig> configs = new HashMap<>();
    configs.put(machineName, new InternalMachineConfig());

    // when
    validator.validate(configs);

    // then no exception is thrown
  }

  @DataProvider
  public static Object[][] imageNames() {
    return new Object[][] {
      new Object[] {"maven"}, new Object[] {"maven-3.6"}, new Object[] {"eclipse/che-3.6:latest"}
    };
  }
}
