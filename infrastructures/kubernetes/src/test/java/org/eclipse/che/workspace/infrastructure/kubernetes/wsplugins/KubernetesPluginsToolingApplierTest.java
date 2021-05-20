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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.Command.WORKING_DIRECTORY_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.CONTAINER_SOURCE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PLUGIN_MACHINE_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.TOOL_CONTAINER_SOURCE;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.SecureServerExposerFactoryProvider.SECURE_EXPOSER_IMPL_PROPERTY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.provision.env.ProjectsRootEnvVariableProvider;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainerPort;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.Command;
import org.eclipse.che.api.workspace.server.wsplugins.model.EnvVar;
import org.eclipse.che.api.workspace.server.wsplugins.model.Volume;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.Warnings;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.EnvVars;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class KubernetesPluginsToolingApplierTest {

  private static final String TEST_IMAGE = "testImage/test/test";
  private static final String TEST_IMAGE_POLICY = "IfNotPresent";
  private static final String ENV_VAR = "PLUGINS_ENV_VAR";
  private static final String ENV_VAR_VALUE = "PLUGINS_ENV_VAR_VALUE";
  private static final String POD_NAME = "pod12";
  private static final String VOLUME_NAME = "test_volume_name";
  private static final String VOLUME_MOUNT_PATH = "/path/test";
  private static final String USER_MACHINE_NAME = POD_NAME + "/userContainer";
  private static final int MEMORY_LIMIT_MB = 200;
  private static final int MEMORY_REQUEST_MB = 100;
  private static final String CPU_LIMIT = "200m";
  private static final String CPU_REQUEST = "100m";
  private static final String CHE_PLUGIN_ENDPOINT_NAME = "test-endpoint-1";

  @Mock private Pod pod;
  @Mock private PodSpec podSpec;
  @Mock private ObjectMeta meta;
  @Mock private Container userContainer;
  @Mock private InternalMachineConfig userMachineConfig;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private ProjectsRootEnvVariableProvider projectsRootEnvVariableProvider;
  @Mock private ChePluginsVolumeApplier chePluginsVolumeApplier;
  @Mock private EnvVars envVars;

  private KubernetesEnvironment internalEnvironment;
  private KubernetesPluginsToolingApplier applier;

  @BeforeMethod
  public void setUp() {
    internalEnvironment = spy(KubernetesEnvironment.builder().build());
    internalEnvironment.setDevfile(new DevfileImpl());
    applier =
        new KubernetesPluginsToolingApplier(
            TEST_IMAGE_POLICY,
            MEMORY_LIMIT_MB,
            MEMORY_REQUEST_MB,
            CPU_LIMIT,
            CPU_REQUEST,
            false,
            projectsRootEnvVariableProvider,
            chePluginsVolumeApplier,
            envVars);

    Map<String, InternalMachineConfig> machines = new HashMap<>();
    List<Container> containers = new ArrayList<>();
    containers.add(userContainer);
    machines.put(USER_MACHINE_NAME, userMachineConfig);

    when(pod.getSpec()).thenReturn(podSpec);
    lenient().when(podSpec.getContainers()).thenReturn(containers);
    lenient().when(pod.getMetadata()).thenReturn(meta);
    lenient().when(meta.getName()).thenReturn(POD_NAME);
    internalEnvironment.addPod(pod);
    internalEnvironment.getMachines().putAll(machines);

    lenient()
        .when(projectsRootEnvVariableProvider.get(any()))
        .thenReturn(new Pair<>("projects_root", "/somewhere/over/the/rainbow"));
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
        runtimeIdentity,
        internalEnvironment,
        singletonList(createChePlugin(createContainer("container", pluginCommand))));

    // then
    List<CommandImpl> envCommands = internalEnvironment.getCommands();
    assertEquals(envCommands.size(), 1);
    CommandImpl envCommand = envCommands.get(0);
    assertEquals(envCommand.getName(), pluginCommand.getName());
    assertEquals(envCommand.getCommandLine(), String.join(" ", pluginCommand.getCommand()));
    assertEquals(envCommand.getType(), "custom");
    assertEquals(
        envCommand.getAttributes().get(WORKING_DIRECTORY_ATTRIBUTE), pluginCommand.getWorkingDir());
    validateContainerNameName(envCommand.getAttributes().get(MACHINE_NAME_ATTRIBUTE), "container");
  }

  @Test
  public void shouldProvisionApplyEnvironmentVariableToContainersAndInitContainersOfPlugin()
      throws Exception {
    // given
    CheContainer container = new CheContainer();
    container.setName("container");
    CheContainer initContainer = new CheContainer();
    initContainer.setName("initContainer");

    ChePlugin chePlugin =
        createChePlugin(
            "publisher/id/1.0.0", singletonList(container), singletonList(initContainer));
    ComponentImpl component = internalEnvironment.getDevfile().getComponents().get(0);
    component.getEnv().add(new EnvImpl("TEST", "VALUE"));
    ArgumentCaptor<Container> containerArgumentCaptor = ArgumentCaptor.forClass(Container.class);

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    verify(envVars, times(2)).apply(containerArgumentCaptor.capture(), eq(component.getEnv()));
    List<Container> containers = containerArgumentCaptor.getAllValues();
    // containers names are suffixed to provide uniqueness and converted to be k8s API compatible
    assertTrue(containers.get(0).getName().startsWith("initcontainer"));
    assertTrue(containers.get(1).getName().startsWith("container"));
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "The downloaded plugin 'publisher/id/version' configuration does "
              + "not have the corresponding component in devfile. Devfile contains the following "
              + "cheEditor/chePlugins: \\[publisher/editor/1, publisher/plugin/1\\]")
  public void shouldThrowExceptionWhenDownloadedPluginIsNotMatchedWitComponent() throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin("publisher/id/version");

    internalEnvironment.getDevfile().getComponents().clear();
    internalEnvironment
        .getDevfile()
        .getComponents()
        .add(new ComponentImpl("cheEditor", "publisher/editor/1"));
    internalEnvironment
        .getDevfile()
        .getComponents()
        .add(new ComponentImpl("chePlugin", "publisher/plugin/1"));
    internalEnvironment.getDevfile().getComponents().add(new ComponentImpl("k8s", null));

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));
  }

  @Test
  public void shouldResolveMachineNameForCommandsInEnvironment() throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin(createContainer("container"));
    String pluginRef = chePlugin.getId();

    CommandImpl pluginCommand = new CommandImpl("test-command", "echo Hello World!", "custom");
    pluginCommand.getAttributes().put("plugin", pluginRef);
    internalEnvironment.getCommands().add(pluginCommand);

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    List<CommandImpl> envCommands = internalEnvironment.getCommands();
    assertEquals(envCommands.size(), 1);
    CommandImpl envCommand = envCommands.get(0);
    assertEquals(envCommand.getName(), pluginCommand.getName());
    assertEquals(envCommand.getType(), pluginCommand.getType());
    assertEquals(envCommand.getCommandLine(), pluginCommand.getCommandLine());
    assertEquals(envCommand.getAttributes().get("plugin"), pluginRef);
    validateContainerNameName(envCommand.getAttributes().get(MACHINE_NAME_ATTRIBUTE), "container");
  }

  @Test
  public void shouldFillInWarningIfChePluginDoesNotHaveAnyContainersButThereAreRelatedCommands()
      throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin("somePublisher/custom-name/0.0.3");
    String pluginRef = chePlugin.getId();

    CommandImpl pluginCommand = new CommandImpl("test-command", "echo Hello World!", "custom");
    pluginCommand.getAttributes().put("plugin", pluginRef);
    internalEnvironment.getCommands().add(pluginCommand);

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    List<Warning> envWarnings = internalEnvironment.getWarnings();
    assertEquals(envWarnings.size(), 1);
    Warning warning = envWarnings.get(0);
    assertEquals(
        warning.getCode(),
        Warnings.COMMAND_IS_CONFIGURED_IN_PLUGIN_WITHOUT_CONTAINERS_WARNING_CODE);
    assertEquals(
        warning.getMessage(),
        "There are configured commands for plugin 'somePublisher/custom-name/0.0.3' that doesn't have any containers");
  }

  @Test
  public void
      shouldNotFillInWarningIfChePluginDoesNotHaveAnyContainersAndThereAreNotRelatedCommands()
          throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin();

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    assertTrue(internalEnvironment.getWarnings().isEmpty());
  }

  @Test
  public void shouldFillInWarningIfChePluginHasMultiplyContainersButThereAreRelatedCommands()
      throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin(createContainer(), createContainer());
    String pluginRef = chePlugin.getId();

    CommandImpl pluginCommand = new CommandImpl("test-command", "echo Hello World!", "custom");
    pluginCommand.getAttributes().put("plugin", pluginRef);
    internalEnvironment.getCommands().add(pluginCommand);

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    List<Warning> envWarnings = internalEnvironment.getWarnings();
    assertEquals(envWarnings.size(), 1);
    Warning warning = envWarnings.get(0);
    assertEquals(
        warning.getCode(),
        Warnings.COMMAND_IS_CONFIGURED_IN_PLUGIN_WITH_MULTIPLY_CONTAINERS_WARNING_CODE);
    assertEquals(
        warning.getMessage(),
        "There are configured commands for plugin '"
            + pluginRef
            + "' that has multiply containers. Commands will be configured to be run in first container");
  }

  @Test
  public void shouldNotFillInWarningIfChePluginHasMultiplyContainersAndThereAreNotRelatedCommands()
      throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin(createContainer(), createContainer());

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    assertTrue(internalEnvironment.getWarnings().isEmpty());
  }

  @Test
  public void shouldUseContainerNameForMachinesName() throws Exception {
    // given
    internalEnvironment.getMachines().clear();

    ChePlugin chePlugin = createChePlugin("publisher/plugin1/0.2.1", createContainer("container1"));

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    Map<String, InternalMachineConfig> machines = internalEnvironment.getMachines();
    assertEquals(machines.size(), 1);
    validateContainerNameName(machines.keySet().iterator().next(), "container1");
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Che plugins tooling configuration can be applied to a workspace with one pod only")
  public void throwsExceptionWhenTheNumberOfPodsIsNot1() throws Exception {
    PodData podData = new PodData(podSpec, meta);
    when(internalEnvironment.getPodsData()).thenReturn(of("pod1", podData, "pod2", podData));

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(createChePlugin()));
  }

  @Test
  public void addToolingContainerToAPod() throws Exception {
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(createChePlugin()));

    verifyPodAndContainersNumber(2);
    Container toolingContainer = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyContainer(toolingContainer);
  }

  @Test
  public void addToolingInitContainerToAPod() throws Exception {
    lenient().when(podSpec.getInitContainers()).thenReturn(new ArrayList<>());
    ChePlugin chePlugin = createChePlugin();
    chePlugin.setInitContainers(singletonList(createContainer()));

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndInitContainersNumber(1);
    Container toolingInitContainer = getOnlyOneInitContainerFromPod(internalEnvironment);
    verifyContainer(toolingInitContainer);
  }

  @Test
  public void addToolingInitContainerWithCommand() throws InfrastructureException {
    List<String> command = Arrays.asList("cp", "-rf", "test-file", "/some-volume/test");
    lenient().when(podSpec.getInitContainers()).thenReturn(new ArrayList<>());
    ChePlugin chePlugin = createChePlugin();
    List<CheContainer> initContainers = singletonList(createContainer(command, null));
    chePlugin.setInitContainers(initContainers);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndInitContainersNumber(1);
    Container toolingInitContainer = getOnlyOneInitContainerFromPod(internalEnvironment);
    verifyContainer(toolingInitContainer);
    assertEquals(toolingInitContainer.getCommand(), command);
  }

  @Test
  public void addToolingInitContainerWithArgs() throws InfrastructureException {
    List<String> args = Arrays.asList("cp", "-rf", "test-file", "/some-volume/test");
    lenient().when(podSpec.getInitContainers()).thenReturn(new ArrayList<>());
    ChePlugin chePlugin = createChePlugin();
    List<CheContainer> initContainers = singletonList(createContainer(null, args));
    chePlugin.setInitContainers(initContainers);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndInitContainersNumber(1);
    Container toolingInitContainer = getOnlyOneInitContainerFromPod(internalEnvironment);
    verifyContainer(toolingInitContainer);
    assertEquals(toolingInitContainer.getArgs(), args);
  }

  @Test
  public void addToolingInitContainerWithCommandAndArgs() throws InfrastructureException {
    List<String> command = singletonList("cp");
    List<String> args = Arrays.asList("-rf", "test-file", "/some-volume/test");
    lenient().when(podSpec.getInitContainers()).thenReturn(new ArrayList<>());
    ChePlugin chePlugin = createChePlugin();
    List<CheContainer> initContainers = singletonList(createContainer(command, args));
    chePlugin.setInitContainers(initContainers);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndInitContainersNumber(1);
    Container toolingInitContainer = getOnlyOneInitContainerFromPod(internalEnvironment);
    verifyContainer(toolingInitContainer);
    assertEquals(toolingInitContainer.getCommand(), command);
    assertEquals(toolingInitContainer.getArgs(), args);
  }

  @Test
  public void addToolingContainerWithCommand() throws InfrastructureException {
    List<String> command = Arrays.asList("tail", "-f", "/dev/null");
    lenient().when(podSpec.getContainers()).thenReturn(new ArrayList<>());
    ChePlugin chePlugin = createChePlugin();
    List<CheContainer> containers = singletonList(createContainer(command, null));
    chePlugin.setContainers(containers);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndContainersNumber(1);
    Container toolingContainer = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyContainer(toolingContainer);
    assertEquals(toolingContainer.getCommand(), command);
  }

  @Test
  public void addToolingContainerWithArgs() throws InfrastructureException {
    List<String> args = Arrays.asList("tail", "-f", "/dev/null");
    lenient().when(podSpec.getContainers()).thenReturn(new ArrayList<>());
    ChePlugin chePlugin = createChePlugin();
    List<CheContainer> containers = singletonList(createContainer(null, args));
    chePlugin.setContainers(containers);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndContainersNumber(1);
    Container toolingContainer = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyContainer(toolingContainer);
    assertEquals(toolingContainer.getArgs(), args);
  }

  @Test
  public void addToolingContainerWithCommandAndArgs() throws InfrastructureException {
    List<String> command = singletonList("tail");
    List<String> args = Arrays.asList("-f", "/dev/null");
    lenient().when(podSpec.getContainers()).thenReturn(new ArrayList<>());
    ChePlugin chePlugin = createChePlugin();
    List<CheContainer> containers = singletonList(createContainer(command, args));
    chePlugin.setContainers(containers);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndContainersNumber(1);
    Container toolingContainer = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyContainer(toolingContainer);
    assertEquals(toolingContainer.getCommand(), command);
    assertEquals(toolingContainer.getArgs(), args);
  }

  @Test
  public void createsPodAndAddToolingIfNoPodIsPresent() throws Exception {
    internalEnvironment = spy(KubernetesEnvironment.builder().build());
    internalEnvironment.setDevfile(new DevfileImpl());
    Map<String, InternalMachineConfig> machines = new HashMap<>();
    machines.put(USER_MACHINE_NAME, userMachineConfig);
    internalEnvironment.getMachines().putAll(machines);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(createChePlugin()));

    verifyPodAndContainersNumber(1);
    Container toolingContainer = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyContainer(toolingContainer);
  }

  @Test
  public void canAddMultipleToolingContainersToAPodFromOnePlugin() throws Exception {
    applier.apply(
        runtimeIdentity,
        internalEnvironment,
        singletonList(createChePlugin(createContainer(), createContainer())));

    verifyPodAndContainersNumber(3);
    List<Container> nonUserContainers = getNonUserContainers(internalEnvironment);
    verifyContainers(nonUserContainers);
  }

  @Test
  public void canAddMultipleToolingContainersToAPodFromSeveralPlugins() throws Exception {
    applier.apply(
        runtimeIdentity,
        internalEnvironment,
        ImmutableList.of(createChePlugin(), createChePlugin()));

    verifyPodAndContainersNumber(3);
    List<Container> nonUserContainers = getNonUserContainers(internalEnvironment);
    verifyContainers(nonUserContainers);
  }

  @Test
  public void applyPluginContainerWithOneVolume() throws InfrastructureException {
    lenient().when(podSpec.getContainers()).thenReturn(new ArrayList<>());

    ChePlugin chePlugin = createChePlugin();
    CheContainer cheContainer = chePlugin.getContainers().get(0);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndContainersNumber(1);
    Container container = getOneAndOnlyNonUserContainer(internalEnvironment);
    verifyContainer(container);

    verify(chePluginsVolumeApplier)
        .applyVolumes(
            any(PodData.class),
            eq(container),
            eq(cheContainer.getVolumes()),
            eq(internalEnvironment));
  }

  @Test
  public void applyPluginInitContainerWithOneVolume() throws InfrastructureException {
    lenient().when(podSpec.getInitContainers()).thenReturn(new ArrayList<>());

    ChePlugin chePlugin = createChePlugin();
    CheContainer initContainer = createContainer();
    chePlugin.setInitContainers(singletonList(initContainer));

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    verifyPodAndInitContainersNumber(1);
    Container toolingInitContainer = getOnlyOneInitContainerFromPod(internalEnvironment);
    verifyContainer(toolingInitContainer);

    verify(chePluginsVolumeApplier)
        .applyVolumes(
            any(PodData.class),
            eq(toolingInitContainer),
            eq(initContainer.getVolumes()),
            eq(internalEnvironment));
  }

  @Test
  public void addsMachinesWithVolumesToAllToolingContainer() throws Exception {
    // given
    ChePlugin chePluginWithNonDefaultVolume = createChePlugin();
    String anotherVolumeName = VOLUME_NAME + "1";
    String anotherVolumeMountPath = VOLUME_MOUNT_PATH + "/something";
    List<Volume> volumes =
        singletonList(new Volume().name(anotherVolumeName).mountPath(anotherVolumeMountPath));
    CheContainer toolingContainer = chePluginWithNonDefaultVolume.getContainers().get(0);
    toolingContainer.setVolumes(volumes);

    ChePlugin chePlugin = createChePlugin();

    // when
    applier.apply(
        runtimeIdentity, internalEnvironment, asList(chePlugin, chePluginWithNonDefaultVolume));

    // then
    Collection<InternalMachineConfig> machineConfigs = getNonUserMachines(internalEnvironment);
    assertEquals(machineConfigs.size(), 2);

    verify(chePluginsVolumeApplier)
        .applyVolumes(
            any(PodData.class),
            any(Container.class),
            eq(chePlugin.getContainers().get(0).getVolumes()),
            eq(internalEnvironment));
  }

  @Test
  public void addsMachineWithVolumeFromChePlugin() throws Exception {
    // given
    ChePlugin chePluginWithNoVolume = createChePlugin();
    chePluginWithNoVolume.getContainers().get(0).setVolumes(emptyList());

    // when
    applier.apply(
        runtimeIdentity, internalEnvironment, asList(createChePlugin(), chePluginWithNoVolume));

    // then
    Collection<InternalMachineConfig> machineConfigs = getNonUserMachines(internalEnvironment);
    assertEquals(machineConfigs.size(), 2);
    verifyNumberOfMachinesWithSpecificNumberOfVolumes(machineConfigs, 2, 0);
  }

  @Test
  public void addsMachineWithServersForContainer() throws Exception {
    // given
    ChePlugin chePlugin = createChePlugin();
    addPortToSingleContainerPlugin(chePlugin, 80, "test-port", emptyMap(), true);

    // when
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

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
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

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
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

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
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    InternalMachineConfig machineConfig = getOneAndOnlyNonUserMachine(internalEnvironment);
    assertEquals(
        machineConfig.getServers(),
        expectedSingleServer(
            443, "test-port", singletonMap("attr1", "value1"), true, "https", "/path/1"));
  }

  @Test
  public void setsSourceAndPluginAttributeForMachineAssociatedWithSidecar() throws Exception {
    ChePlugin chePlugin = createChePlugin();

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    InternalMachineConfig machineConfig = getOneAndOnlyNonUserMachine(internalEnvironment);
    Map<String, String> attributes = machineConfig.getAttributes();
    assertEquals(attributes.get(CONTAINER_SOURCE_ATTRIBUTE), TOOL_CONTAINER_SOURCE);
    assertEquals(attributes.get(PLUGIN_MACHINE_ATTRIBUTE), chePlugin.getId());
  }

  @Test
  public void setsDefaultMemoryLimitForMachineAssociatedWithContainer() throws Exception {
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(createChePlugin()));

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
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

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
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

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
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));

    // then
    verifyK8sServices(internalEnvironment, endpoint1, endpoint2);
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
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(chePlugin));
  }

  @Test
  public void shouldNotSetJWTServerExposerAttributeIfAuthEnabledButAttributeIsPresent()
      throws Exception {
    applier =
        new KubernetesPluginsToolingApplier(
            TEST_IMAGE_POLICY,
            MEMORY_LIMIT_MB,
            MEMORY_REQUEST_MB,
            CPU_LIMIT,
            CPU_REQUEST,
            true,
            projectsRootEnvVariableProvider,
            chePluginsVolumeApplier,
            envVars);
    internalEnvironment.getAttributes().put(SECURE_EXPOSER_IMPL_PROPERTY, "somethingElse");

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(createChePlugin()));

    assertEquals(
        internalEnvironment.getAttributes().get(SECURE_EXPOSER_IMPL_PROPERTY), "somethingElse");
  }

  @Test
  public void shouldSetSpecifiedImagePullPolicy() throws Exception {
    applier =
        new KubernetesPluginsToolingApplier(
            TEST_IMAGE_POLICY,
            MEMORY_LIMIT_MB,
            MEMORY_REQUEST_MB,
            CPU_LIMIT,
            CPU_REQUEST,
            true,
            projectsRootEnvVariableProvider,
            chePluginsVolumeApplier,
            envVars);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(createChePlugin()));

    assertEquals(
        internalEnvironment
            .getPodsCopy()
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
    applier =
        new KubernetesPluginsToolingApplier(
            "None",
            MEMORY_LIMIT_MB,
            MEMORY_REQUEST_MB,
            CPU_LIMIT,
            CPU_REQUEST,
            true,
            projectsRootEnvVariableProvider,
            chePluginsVolumeApplier,
            envVars);

    applier.apply(runtimeIdentity, internalEnvironment, singletonList(createChePlugin()));

    assertNull(
        internalEnvironment
            .getPodsCopy()
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
    applier.apply(runtimeIdentity, internalEnvironment, singletonList(createChePlugin()));

    assertNull(internalEnvironment.getAttributes().get(SECURE_EXPOSER_IMPL_PROPERTY));
  }

  private ChePlugin createChePlugin() {
    return createChePlugin(createContainer());
  }

  private ChePlugin createChePlugin(CheContainer... containers) {
    return createChePlugin(
        "publisher/" + NameGenerator.generate("name", 3) + "/0.0.1",
        Arrays.asList(containers),
        emptyList());
  }

  private ChePlugin createChePlugin(String id, CheContainer... containers) {
    return createChePlugin(id, Arrays.asList(containers), emptyList());
  }

  private ChePlugin createChePlugin(
      String id, List<CheContainer> containers, List<CheContainer> initContainers) {
    String[] splittedId = id.split("/");
    ChePlugin plugin = new ChePlugin();
    plugin.setPublisher(splittedId[0]);
    plugin.setName(splittedId[1]);
    plugin.setVersion(splittedId[2]);
    plugin.setId(id);
    plugin.setContainers(containers);
    plugin.setInitContainers(initContainers);

    internalEnvironment.getDevfile().getComponents().add(new ComponentImpl("chePlugin", id));
    return plugin;
  }

  private CheContainer createContainer(List<String> command, List<String> args) {
    CheContainer container = createContainer();
    container.setCommand(command);
    container.setArgs(args);

    return container;
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
    assertEquals(internalEnvironment.getPodsCopy().size(), 1);
    Pod pod = internalEnvironment.getPodsCopy().values().iterator().next();
    assertEquals(pod.getSpec().getContainers().size(), containersNumber);
  }

  private void verifyPodAndInitContainersNumber(int containersNumber) {
    assertEquals(internalEnvironment.getPodsCopy().size(), 1);
    Pod pod = internalEnvironment.getPodsCopy().values().iterator().next();
    assertEquals(pod.getSpec().getInitContainers().size(), containersNumber);
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
  private void verifyNumberOfMachinesWithSpecificNumberOfVolumes(
      Collection<InternalMachineConfig> machineConfigs, int numberOfMachines, int numberOfVolumes) {

    long numberOfMatchingMachines =
        machineConfigs
            .stream()
            .filter(machineConfig -> machineConfig.getVolumes().size() == numberOfVolumes)
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
    Pod pod = kubernetesEnvironment.getPodsCopy().values().iterator().next();
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

  private Container getOnlyOneInitContainerFromPod(KubernetesEnvironment kubernetesEnvironment) {
    Pod pod = kubernetesEnvironment.getPodsCopy().values().iterator().next();

    List<Container> initContainer = pod.getSpec().getInitContainers();
    assertEquals(initContainer.size(), 1);
    return initContainer.get(0);
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
    servers.put(portName, new ServerConfigImpl(port + "/tcp", protocol, path, serverAttributes));
  }

  private void validateContainerNameName(String actual, String prefixExpected) {
    Pattern pattern = Pattern.compile(prefixExpected + "\\w{3}");
    assertTrue(pattern.matcher(actual).matches());
  }
}
