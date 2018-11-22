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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider.SECURE_EXPOSER_IMPL_PROPERTY;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Command;
import org.eclipse.che.api.workspace.server.wsplugins.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class KubernetesPluginsToolingApplierTest {
  private static final String TEST_IMAGE = "testImage/test:test";
  private static final String TEST_IMAGE_POLICY = "IfNotPresent";
  private static final String ENV_VAR = "PLUGINS_ENV_VAR";
  private static final String ENV_VAR_VALUE = "PLUGINS_ENV_VAR_VALUE";
  private static final String POD_NAME = "pod12";
  private static final String VOLUME_NAME = "test_volume_name";
  private static final String VOLUME_MOUNT_PATH = "/path/test";
  private static final String USER_MACHINE_NAME = POD_NAME + "/userContainer";
  private static final int MEMORY_LIMIT_MB = 200;
  private static final String CHE_PLUGIN_ENDPOINT_NAME = "test-endpoint-1";

  @Mock private Pod pod;
  @Mock private PodSpec podSpec;
  @Mock private ObjectMeta meta;
  @Mock private Container userContainer;
  @Mock private InternalMachineConfig userMachineConfig;

  private KubernetesEnvironment internalEnvironment;
  private List<Container> containers;
  private KubernetesPluginsToolingApplier applier;

  @BeforeMethod
  public void setUp() {
    internalEnvironment = spy(KubernetesEnvironment.builder().build());
    applier = new KubernetesPluginsToolingApplier(TEST_IMAGE_POLICY, MEMORY_LIMIT_MB, false);

    Map<String, InternalMachineConfig> machines = new HashMap<>();
    containers = new ArrayList<>();
    containers.add(userContainer);
    machines.put(USER_MACHINE_NAME, userMachineConfig);

    internalEnvironment.getPods().put(POD_NAME, pod);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(containers);
    lenient().when(pod.getMetadata()).thenReturn(meta);
    lenient().when(meta.getName()).thenReturn(POD_NAME);
    internalEnvironment.getMachines().putAll(machines);
  }

  @Test
  public void shouldProvisionPluginsCommandsToEnvironment() throws Exception {
    // given
    Command pluginCommand =
        new Command()
            .name("test-command")
            .workingDir("~")
            .command(Arrays.asList("./build.sh", "--no-pull"));

    // when
    applier.apply(
        internalEnvironment,
        singletonList(createChePlugin("plugin", createContainer("container", pluginCommand))));

    // then
    List<CommandImpl> envCommands = internalEnvironment.getCommands();
    assertEquals(envCommands.size(), 1);
    CommandImpl envCommand = envCommands.get(0);
    assertEquals(envCommand.getName(), pluginCommand.getName());
    assertEquals(
        envCommand.getCommandLine(),
        pluginCommand.getCommand().stream().collect(Collectors.joining(" ")));
    assertEquals(envCommand.getType(), "custom");
    assertEquals(envCommand.getAttributes().get("workDir"), pluginCommand.getWorkingDir());
    assertEquals(envCommand.getAttributes().get("machineName"), POD_NAME + "/plugin-container");
  }

  @Test
  public void shouldResolveMachineNameForCommandsInEnvironment() throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin("plugin", createContainer("container"));
    String pluginRef = chePlugin.getId() + ":" + chePlugin.getVersion();

    CommandImpl pluginCommand = new CommandImpl("test-command", "echo Hello World!", "custom");
    pluginCommand.getAttributes().put("plugin", pluginRef);
    internalEnvironment.getCommands().add(pluginCommand);

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    List<CommandImpl> envCommands = internalEnvironment.getCommands();
    assertEquals(envCommands.size(), 1);
    CommandImpl envCommand = envCommands.get(0);
    assertEquals(envCommand.getName(), pluginCommand.getName());
    assertEquals(envCommand.getType(), pluginCommand.getType());
    assertEquals(envCommand.getCommandLine(), pluginCommand.getCommandLine());
    assertEquals(envCommand.getAttributes().get("plugin"), pluginRef);
    assertEquals(envCommand.getAttributes().get("machineName"), POD_NAME + "/plugin-container");
  }

  @Test
  public void shouldFillInWarningIfChePluginDoesNotHaveAnyContainersButThereAreRelatedCommands()
      throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin("plugin");
    String pluginRef = chePlugin.getId() + ":" + chePlugin.getVersion();

    CommandImpl pluginCommand = new CommandImpl("test-command", "echo Hello World!", "custom");
    pluginCommand.getAttributes().put("plugin", pluginRef);
    internalEnvironment.getCommands().add(pluginCommand);

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    List<Warning> envWarnings = internalEnvironment.getWarnings();
    assertEquals(envWarnings.size(), 1);
    Warning warning = envWarnings.get(0);
    assertEquals(warning.getCode(), 44012);
    assertEquals(
        warning.getMessage(),
        "There are configured commands for plugin 'some-id:0.0.3' that doesn't have any containers");
  }

  @Test
  public void
      shouldNotFillInWarningIfChePluginDoesNotHaveAnyContainersAndThereAreNotRelatedCommands()
          throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin("plugin");

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    assertTrue(internalEnvironment.getWarnings().isEmpty());
  }

  @Test
  public void shouldFillInWarningIfChePluginHasMultiplyContainersButThereAreRelatedCommands()
      throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin("plugin", createContainer(), createContainer());
    String pluginRef = chePlugin.getId() + ":" + chePlugin.getVersion();

    CommandImpl pluginCommand = new CommandImpl("test-command", "echo Hello World!", "custom");
    pluginCommand.getAttributes().put("plugin", pluginRef);
    internalEnvironment.getCommands().add(pluginCommand);

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    List<Warning> envWarnings = internalEnvironment.getWarnings();
    assertEquals(envWarnings.size(), 1);
    Warning warning = envWarnings.get(0);
    assertEquals(warning.getCode(), 44013);
    assertEquals(
        warning.getMessage(),
        "There are configured commands for plugin 'some-id:0.0.3' that has multiply containers. Commands will be configured to be run in first container");
  }

  @Test
  public void shouldNotFillInWarningIfChePluginHasMultiplyContainersAndThereAreNotRelatedCommands()
      throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin("plugin", createContainer(), createContainer());

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    assertTrue(internalEnvironment.getWarnings().isEmpty());
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Che plugins tooling configuration can be applied to a workspace with one pod only")
  public void throwsExceptionWhenTheNumberOfPodsIsNot1() throws Exception {
    when(internalEnvironment.getPods()).thenReturn(of("pod1", pod, "pod2", pod));

    applier.apply(internalEnvironment, singletonList(createChePlugin()));
  }

  @Test
  public void addToolingContainerToAPod() throws Exception {
    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    verifyPodAndContainersNumber(2);
    Container toolingContainer = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyContainer(toolingContainer);
  }

  @Test
  public void createsPodAndAddToolingIfNoPodIsPresent() throws Exception {
    internalEnvironment.getPods().clear();

    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    verifyPodAndContainersNumber(1);
    Container toolingContainer = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyContainer(toolingContainer);
  }

  @Test
  public void canAddMultipleToolingContainersToAPodFromOnePlugin() throws Exception {
    applier.apply(
        internalEnvironment, singletonList(createChePlugin(createContainer(), createContainer())));

    verifyPodAndContainersNumber(3);
    List<Container> nonUserContainers = getNonUserContainers(internalEnvironment);
    verifyContainers(nonUserContainers);
  }

  @Test
  public void canAddMultipleToolingContainersToAPodFromSeveralPlugins() throws Exception {
    applier.apply(internalEnvironment, ImmutableList.of(createChePlugin(), createChePlugin()));

    verifyPodAndContainersNumber(3);
    List<Container> nonUserContainers = getNonUserContainers(internalEnvironment);
    verifyContainers(nonUserContainers);
  }

  @Test
  public void addsMachineWithVolumeToAToolingContainer() throws Exception {
    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    InternalMachineConfig machineConfig = getOneAndOnlyNonUserMachine(internalEnvironment);
    verifyOneAndOnlyVolume(machineConfig, VOLUME_NAME, VOLUME_MOUNT_PATH);
  }

  @Test
  public void addsMachinesWithVolumesToAllToolingContainer() throws Exception {
    // given
    ChePlugin chePluginWithNonDefaultVolume = createChePlugin();
    String anotherVolumeName = VOLUME_NAME + "1";
    String anotherVolumeMountPath = VOLUME_MOUNT_PATH + "/something";
    chePluginWithNonDefaultVolume
        .getContainers()
        .get(0)
        .setVolumes(
            singletonList(new Volume().name(anotherVolumeName).mountPath(anotherVolumeMountPath)));

    // when
    applier.apply(internalEnvironment, asList(createChePlugin(), chePluginWithNonDefaultVolume));

    // then
    Collection<InternalMachineConfig> machineConfigs = getNonUserMachines(internalEnvironment);
    assertEquals(machineConfigs.size(), 2);
    verifyNumberOfMachinesWithSpecificVolume(machineConfigs, 1, VOLUME_NAME, VOLUME_MOUNT_PATH);
    verifyNumberOfMachinesWithSpecificVolume(
        machineConfigs, 1, anotherVolumeName, anotherVolumeMountPath);
  }

  @Test
  public void addsMachineWithVolumeFromChePlugin() throws Exception {
    // given
    ChePlugin chePluginWithNoVolume = createChePlugin();
    chePluginWithNoVolume.getContainers().get(0).setVolumes(emptyList());

    // when
    applier.apply(internalEnvironment, asList(createChePlugin(), chePluginWithNoVolume));

    // then
    Collection<InternalMachineConfig> machineConfigs = getNonUserMachines(internalEnvironment);
    assertEquals(machineConfigs.size(), 2);
    verifyNumberOfMachinesWithSpecificNumberOfVolumes(machineConfigs, 1, 0);
    verifyNumberOfMachinesWithSpecificNumberOfVolumes(machineConfigs, 1, 1);
  }

  @Test
  public void addsMachineWithServersForContainer() throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port", emptyMap(), true);

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    InternalMachineConfig machineConfig = getOneAndOnlyNonUserMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(), expectedSingleServer(80, "test-port", emptyMap(), true));
  }

  @Test
  public void addsTwoServersForContainers() throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port", emptyMap(), true);
    addPortToSingleContainerPlugin(chePlugin, 8090, "another-test-port", emptyMap(), false);

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    InternalMachineConfig machineConfig = getOneAndOnlyNonUserMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(),
        expectedTwoServers(
            80, "test-port", emptyMap(), true, 8090, "another-test-port", emptyMap(), false));
  }

  @Test
  public void addsMachineWithServersThatUseSamePortButDifferentNames() throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port/http", emptyMap(), true);
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port/ws", emptyMap(), true);

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    InternalMachineConfig machineConfig = getOneAndOnlyNonUserMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(),
        expectedTwoServers(
            80, "test-port/http", emptyMap(), true, 80, "test-port/ws", emptyMap(), true));
  }

  @Test
  public void addsMachineWithServersThatSetProtocolAndPath() throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(
        chePlugin,
        443,
        "test-port",
        ImmutableMap.of("path", "/path/1", "protocol", "https", "attr1", "value1"),
        true);

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    InternalMachineConfig machineConfig = getOneAndOnlyNonUserMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(),
        expectedSingleServer(
            443, "test-port", singletonMap("attr1", "value1"), true, "https", "/path/1"));
  }

  @Test
  public void setsDefaultMemoryLimitForMachineAssociatedWithContainer() throws Exception {
    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    InternalMachineConfig machineConfig = getOneAndOnlyNonUserMachine(internalEnvironment);
    String memoryLimitAttribute = machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE);
    assertEquals(memoryLimitAttribute, Integer.toString(MEMORY_LIMIT_MB * 1024 * 1024));
  }

  @Test
  public void shouldExposeChePluginEndpointsPortsInToolingContainer() throws Exception {
    // given
    ChePluginEndpoint endpoint1 =
        new ChePluginEndpoint().name(CHE_PLUGIN_ENDPOINT_NAME).targetPort(101010).setPublic(true);
    ChePluginEndpoint endpoint2 =
        new ChePluginEndpoint().name("test-endpoint-2").targetPort(2020).setPublic(false);
    CheContainerPort cheContainerPort1 = new CheContainerPort().exposedPort(101010);
    CheContainerPort cheContainerPort2 = new CheContainerPort().exposedPort(2020);
    ChePlugin chePlugin = createChePlugin();
    chePlugin.setEndpoints(asList(endpoint1, endpoint2));
    chePlugin.getContainers().get(0).setPorts(asList(cheContainerPort1, cheContainerPort2));

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    Container container = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyPortsExposed(container, 101010, 2020);
  }

  @Test
  public void shouldNotExposeChePluginPortIfThereIsNoEndpoint() throws Exception {
    // given
    ChePluginEndpoint endpoint1 =
        new ChePluginEndpoint().name(CHE_PLUGIN_ENDPOINT_NAME).targetPort(101010).setPublic(true);
    CheContainerPort cheContainerPort1 = new CheContainerPort().exposedPort(101010);
    CheContainerPort cheContainerPort2 = new CheContainerPort().exposedPort(2020);
    ChePlugin chePlugin = createChePlugin();
    chePlugin.setEndpoints(singletonList(endpoint1));
    chePlugin.getContainers().get(0).setPorts(asList(cheContainerPort1, cheContainerPort2));

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    Container container = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyPortsExposed(container, 101010);
  }

  @Test
  public void shouldAddK8sServicesForChePluginEndpoints() throws Exception {
    // given
    ChePluginEndpoint endpoint1 =
        new ChePluginEndpoint().name(CHE_PLUGIN_ENDPOINT_NAME).targetPort(101010).setPublic(true);
    ChePluginEndpoint endpoint2 =
        new ChePluginEndpoint().name("test-endpoint-2").targetPort(2020).setPublic(false);
    CheContainerPort cheContainerPort1 = new CheContainerPort().exposedPort(101010);
    CheContainerPort cheContainerPort2 = new CheContainerPort().exposedPort(2020);
    ChePlugin chePlugin = createChePlugin();
    chePlugin.setEndpoints(asList(endpoint1, endpoint2));
    chePlugin.getContainers().get(0).setPorts(asList(cheContainerPort1, cheContainerPort2));

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));

    // then
    verifyK8sServices(internalEnvironment, endpoint1, endpoint2);
  }

  @Test
  public void shouldPopulateWorkspaceWideEnvVarsToAllTheContainers() throws Exception {
    // when
    Container container = mock(Container.class);
    containers.add(container);
    List<io.fabric8.kubernetes.api.model.EnvVar> workspaceWideEnvVars = new ArrayList<>();
    //    workspaceWideEnvVars.add();
    //    workspaceWideEnvVars.add();

    // when
    applier.apply(
        internalEnvironment,
        ImmutableList.of(createChePlugin(), createChePlugin(createContainer(), createContainer())));

    // then
    assertEquals(internalEnvironment.getPods().size(), 1);
    Pod pod = internalEnvironment.getPods().values().iterator().next();
    List<Container> actualContainers = pod.getSpec().getContainers();
    assertEquals(actualContainers.size(), 5);
    for (Container actualContainer : actualContainers) {
      assertTrue(actualContainer.getEnv().containsAll(workspaceWideEnvVars));
    }
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Applying of sidecar tooling failed. Kubernetes service with name '"
              + CHE_PLUGIN_ENDPOINT_NAME
              + "' already exists in the workspace environment.")
  public void throwsExceptionOnAddingChePluginEndpointServiceIfServiceExists() throws Exception {
    // given
    ChePluginEndpoint endpoint1 =
        new ChePluginEndpoint().name(CHE_PLUGIN_ENDPOINT_NAME).targetPort(101010).setPublic(true);
    CheContainerPort cheContainerPort1 = new CheContainerPort().exposedPort(101010);
    ChePlugin chePlugin = createChePlugin();
    chePlugin.setEndpoints(singletonList(endpoint1));
    chePlugin.getContainers().get(0).setPorts(singletonList(cheContainerPort1));

    // make collision of service names
    internalEnvironment.getServices().put(CHE_PLUGIN_ENDPOINT_NAME, new Service());

    // when
    applier.apply(internalEnvironment, singletonList(chePlugin));
  }

  @Test
  public void shouldSetJWTServerExposerAttributeIfAuthEnabled() throws Exception {
    applier = new KubernetesPluginsToolingApplier(TEST_IMAGE_POLICY, MEMORY_LIMIT_MB, true);

    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    assertEquals(internalEnvironment.getAttributes().get(SECURE_EXPOSER_IMPL_PROPERTY), "jwtproxy");
  }

  @Test
  public void shouldNotSetJWTServerExposerAttributeIfAuthEnabledButAttributeIsPresent()
      throws Exception {
    applier = new KubernetesPluginsToolingApplier(TEST_IMAGE_POLICY, MEMORY_LIMIT_MB, true);
    internalEnvironment.getAttributes().put(SECURE_EXPOSER_IMPL_PROPERTY, "somethingElse");

    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    assertEquals(
        internalEnvironment.getAttributes().get(SECURE_EXPOSER_IMPL_PROPERTY), "somethingElse");
  }

  @Test
  public void shouldSetSpecifiedImagePullPolicy() throws Exception {
    applier = new KubernetesPluginsToolingApplier(TEST_IMAGE_POLICY, MEMORY_LIMIT_MB, true);

    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    assertEquals(
        internalEnvironment
            .getPods()
            .values()
            .iterator()
            .next()
            .getSpec()
            .getContainers()
            .get(1)
            .getImagePullPolicy(),
        TEST_IMAGE_POLICY);
  }

  @Test
  public void shouldSetNullImagePullPolicyIfValueIsNotStandard() throws Exception {
    applier = new KubernetesPluginsToolingApplier("None", MEMORY_LIMIT_MB, true);

    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    assertNull(
        internalEnvironment
            .getPods()
            .values()
            .iterator()
            .next()
            .getSpec()
            .getContainers()
            .get(1)
            .getImagePullPolicy());
  }

  @Test
  public void shouldNotSetJWTServerExposerAttributeIfAuthDisabled() throws Exception {
    applier.apply(internalEnvironment, singletonList(createChePlugin()));

    assertNull(internalEnvironment.getAttributes().get(SECURE_EXPOSER_IMPL_PROPERTY));
  }

  private ChePlugin createChePlugin() {
    return createChePlugin(createContainer());
  }

  private ChePlugin createChePlugin(CheContainer... containers) {
    return createChePlugin("some-name", containers);
  }

  private ChePlugin createChePlugin(String name, CheContainer... containers) {
    ChePlugin plugin = new ChePlugin();
    plugin.setName(name);
    plugin.setId("some-id");
    plugin.setVersion("0.0.3");
    plugin.setContainers(Arrays.asList(containers));
    return plugin;
  }

  private CheContainer createContainer(Command... commands) {
    return createContainer(generate("container", 5), commands);
  }

  private CheContainer createContainer(String name, Command... commands) {
    CheContainer cheContainer = new CheContainer();
    cheContainer.setImage(TEST_IMAGE);
    cheContainer.setName(name);
    cheContainer.setEnv(singletonList(new EnvVar().name(ENV_VAR).value(ENV_VAR_VALUE)));
    cheContainer.setVolumes(
        singletonList(new Volume().name(VOLUME_NAME).mountPath(VOLUME_MOUNT_PATH)));
    cheContainer.setCommands(Arrays.asList(commands));
    return cheContainer;
  }

  private ServicePort createServicePort(int port) {
    return new ServicePortBuilder()
        .withPort(port)
        .withProtocol("TCP")
        .withNewTargetPort(port)
        .build();
  }

  private void verifyPodAndContainersNumber(int containersNumber) {
    assertEquals(internalEnvironment.getPods().size(), 1);
    Pod pod = internalEnvironment.getPods().values().iterator().next();
    assertEquals(pod.getSpec().getContainers().size(), containersNumber);
  }

  private void verifyContainer(Container toolingContainer) {
    assertEquals(toolingContainer.getImage(), TEST_IMAGE);
    assertEquals(
        toolingContainer.getEnv(),
        singletonList(new io.fabric8.kubernetes.api.model.EnvVar(ENV_VAR, ENV_VAR_VALUE, null)));
  }

  private void verifyContainers(List<Container> containers) {
    for (Container container : containers) {
      verifyContainer(container);
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void verifyOneAndOnlyVolume(
      InternalMachineConfig machineConfig, String volumeName, String volumeMountPath) {
    Map<String, org.eclipse.che.api.core.model.workspace.config.Volume> volumes =
        machineConfig.getVolumes();
    assertEquals(volumes.size(), 1);
    assertEquals(ImmutableMap.of(volumeName, new VolumeImpl().withPath(volumeMountPath)), volumes);
  }

  @SuppressWarnings("SameParameterValue")
  private void verifyNumberOfMachinesWithSpecificNumberOfVolumes(
      Collection<InternalMachineConfig> machineConfigs, int numberOfMachines, int numberOfVolumes) {

    long numberOfMatchingMachines =
        machineConfigs
            .stream()
            .filter(machineConfig -> machineConfig.getVolumes().size() == numberOfVolumes)
            .count();
    assertEquals(numberOfMatchingMachines, numberOfMachines);
  }

  @SuppressWarnings("SameParameterValue")
  private void verifyNumberOfMachinesWithSpecificVolume(
      Collection<InternalMachineConfig> machineConfigs,
      int numberOfMachines,
      String volumeName,
      String volumeMountPath) {

    long numberOfMatchingMachines =
        machineConfigs
            .stream()
            .filter(machineConfig -> machineConfig.getVolumes().size() == 1)
            .filter(machineConfig -> machineConfig.getVolumes().get(volumeName) != null)
            .filter(
                machineConfig ->
                    volumeMountPath.equals(machineConfig.getVolumes().get(volumeName).getPath()))
            .count();
    assertEquals(numberOfMatchingMachines, numberOfMachines);
  }

  private void verifyPortsExposed(Container container, int... ports) {
    List<ContainerPort> actualPorts = container.getPorts();
    List<ContainerPort> expectedPorts = new ArrayList<>();
    for (int port : ports) {
      expectedPorts.add(
          new ContainerPortBuilder().withContainerPort(port).withProtocol("TCP").build());
    }
    assertEquals(actualPorts, expectedPorts);
  }

  private void verifyK8sServices(
      KubernetesEnvironment internalEnvironment, ChePluginEndpoint... endpoints) {
    Map<String, Service> services = internalEnvironment.getServices();
    for (ChePluginEndpoint endpoint : endpoints) {
      assertTrue(services.containsKey(endpoint.getName()));
      Service service = services.get(endpoint.getName());
      assertEquals(service.getMetadata().getName(), endpoint.getName());
      assertEquals(
          service.getSpec().getSelector(), singletonMap(CHE_ORIGINAL_NAME_LABEL, POD_NAME));

      assertEquals(
          service.getSpec().getPorts(), singletonList(createServicePort(endpoint.getTargetPort())));
    }
  }

  private Collection<InternalMachineConfig> getNonUserMachines(
      InternalEnvironment internalEnvironment) {
    Map<String, InternalMachineConfig> machines = internalEnvironment.getMachines();
    Map<String, InternalMachineConfig> nonUserMachines =
        machines
            .entrySet()
            .stream()
            .filter(entry -> !USER_MACHINE_NAME.equals(entry.getKey()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    return nonUserMachines.values();
  }

  private InternalMachineConfig getOneAndOnlyNonUserMachine(
      InternalEnvironment internalEnvironment) {
    Collection<InternalMachineConfig> nonUserMachines = getNonUserMachines(internalEnvironment);
    assertEquals(nonUserMachines.size(), 1);
    return nonUserMachines.iterator().next();
  }

  private List<Container> getNonUserContainers(KubernetesEnvironment kubernetesEnvironment) {
    Pod pod = kubernetesEnvironment.getPods().values().iterator().next();
    return pod.getSpec()
        .getContainers()
        .stream()
        .filter(container -> userContainer != container)
        .collect(Collectors.toList());
  }

  private Container getOneAndOnlyNonUserContainer(KubernetesEnvironment kubernetesEnvironment) {
    List<Container> nonUserContainers = getNonUserContainers(kubernetesEnvironment);
    assertEquals(nonUserContainers.size(), 1);
    return nonUserContainers.get(0);
  }

  private void addPortToSingleContainerPlugin(
      ChePlugin plugin,
      int port,
      String portName,
      Map<String, String> attributes,
      boolean isPublic) {

    assertEquals(plugin.getContainers().size(), 1);

    ChePluginEndpoint endpoint =
        new ChePluginEndpoint()
            .attributes(attributes)
            .name(portName)
            .setPublic(isPublic)
            .targetPort(port);
    plugin.getEndpoints().add(endpoint);
    List<CheContainerPort> ports = plugin.getContainers().get(0).getPorts();
    if (ports
        .stream()
        .map(CheContainerPort::getExposedPort)
        .noneMatch(integer -> integer == port)) {
      ports.add(new CheContainerPort().exposedPort(port));
    }
  }

  @SuppressWarnings("SameParameterValue")
  private Map<String, ServerConfig> expectedSingleServer(
      int port, String portName, Map<String, String> attributes, boolean isExternal) {
    Map<String, ServerConfig> servers = new HashMap<>();
    addExpectedServer(servers, port, portName, attributes, isExternal, null, null);
    return servers;
  }

  @SuppressWarnings("SameParameterValue")
  private Map<String, ServerConfig> expectedSingleServer(
      int port,
      String portName,
      Map<String, String> attributes,
      boolean isExternal,
      String protocol,
      String path) {
    Map<String, ServerConfig> servers = new HashMap<>();
    addExpectedServer(servers, port, portName, attributes, isExternal, protocol, path);
    return servers;
  }

  @SuppressWarnings("SameParameterValue")
  private Map<String, ServerConfig> expectedTwoServers(
      int port,
      String portName,
      Map<String, String> attributes,
      boolean isExternal,
      int port2,
      String portName2,
      Map<String, String> attributes2,
      boolean isExternal2) {
    Map<String, ServerConfig> servers = new HashMap<>();
    addExpectedServer(servers, port, portName, attributes, isExternal, null, null);
    addExpectedServer(servers, port2, portName2, attributes2, isExternal2, null, null);
    return servers;
  }

  private void addExpectedServer(
      Map<String, ServerConfig> servers,
      int port,
      String portName,
      Map<String, String> attributes,
      boolean isExternal,
      String protocol,
      String path) {
    Map<String, String> serverAttributes = new HashMap<>(attributes);
    serverAttributes.put("internal", Boolean.toString(!isExternal));
    servers.put(
        portName,
        new ServerConfigImpl(Integer.toString(port) + "/tcp", protocol, path, serverAttributes));
  }
}
