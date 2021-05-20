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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static io.fabric8.kubernetes.client.utils.Serialization.unmarshal;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.ServerConfig.REQUIRE_SUBDOMAIN;
import static org.eclipse.che.api.workspace.server.devfile.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.workspace.server.devfile.URLFileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EnvImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.VolumeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.EnvVars;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class KubernetesComponentToWorkspaceApplierTest {

  public static final String REFERENCE_FILENAME = "reference.yaml";
  public static final String COMPONENT_NAME = "foo";
  public static final String PROJECT_MOUNT_PATH = "/projects";

  private WorkspaceConfigImpl workspaceConfig;

  private KubernetesComponentToWorkspaceApplier applier;
  @Mock private KubernetesRecipeParser k8sRecipeParser;
  @Mock private KubernetesEnvironmentProvisioner k8sEnvProvisioner;
  @Mock private EnvVars envVars;

  @Captor private ArgumentCaptor<List<HasMetadata>> objectsCaptor;

  private Set<String> k8sBasedComponents;

  @BeforeMethod
  public void setUp() {
    k8sBasedComponents = new HashSet<>();
    k8sBasedComponents.add(KUBERNETES_COMPONENT_TYPE);
    k8sBasedComponents.add("openshift"); // so that we can work with the petclinic.yaml
    applier =
        new KubernetesComponentToWorkspaceApplier(
            k8sRecipeParser,
            k8sEnvProvisioner,
            envVars,
            PROJECT_MOUNT_PATH,
            "1Gi",
            "ReadWriteOnce",
            "",
            "Always",
            k8sBasedComponents);

    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Fetching content of file `reference.yaml` specified in `reference` field of component `foo` is not "
              + "supported. Please provide its content in `referenceContent` field. Cause: fetch is not supported")
  public void
      shouldThrowExceptionWhenRecipeComponentIsPresentAndContentProviderDoesNotSupportFetching()
          throws Exception {
    // given
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);

    // when
    applier.apply(
        workspaceConfig,
        component,
        e -> {
          throw new DevfileException("fetch is not supported");
        });
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred during parsing list from file "
              + REFERENCE_FILENAME
              + " for component '"
              + COMPONENT_NAME
              + "': .*")
  public void shouldThrowExceptionWhenRecipeContentIsNotAValidYaml() throws Exception {
    // given
    doThrow(new ValidationException("non valid")).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);

    // when
    applier.apply(workspaceConfig, component, s -> "some_non_yaml_content");
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error during recipe content retrieval for component 'foo' with type 'kubernetes': fetch failed")
  public void shouldThrowExceptionWhenExceptionHappensOnContentProvider() throws Exception {
    // given
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);

    // when
    applier.apply(
        workspaceConfig,
        component,
        e -> {
          throw new IOException("fetch failed");
        });
  }

  @Test
  public void shouldProvisionEnvironmentWithCorrectRecipeTypeAndContentFromK8SList()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    KubernetesList list = toK8SList(yamlRecipeContent);
    replaceImagePullPolicy(list, "Always");
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig), eq(KubernetesEnvironment.TYPE), eq(list.getItems()), anyMap());
  }

  @Test
  public void shouldUseReferenceContentAsRecipeIfPresent() throws Exception {
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setReferenceContent(yamlRecipeContent);
    component.setAlias(COMPONENT_NAME);

    applier.apply(workspaceConfig, component, new URLFileContentProvider(null, null));

    KubernetesList list = toK8SList(yamlRecipeContent);
    replaceImagePullPolicy(list, "Always");

    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig), eq(KubernetesEnvironment.TYPE), eq(list.getItems()), anyMap());
  }

  @Test
  public void shouldProvisionProjectVolumesIfSpecifiedIntoK8SList() throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    List<HasMetadata> k8sList = toK8SList(yamlRecipeContent).getItems();
    doReturn(k8sList).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setMountSources(true);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    verify(k8sEnvProvisioner).provision(any(), any(), objectsCaptor.capture(), any());
    List<HasMetadata> list = objectsCaptor.getValue();

    // Make sure PVC is created
    assertTrue(
        list.stream()
            .filter(hasMeta -> hasMeta instanceof PersistentVolumeClaim)
            .map(o -> (PersistentVolumeClaim) o)
            .anyMatch(claim -> claim.getMetadata().getName().equals(PROJECTS_VOLUME_NAME)));

    for (HasMetadata o : list) {
      if (o instanceof Pod) {
        Pod p = (Pod) o;

        // ignore pods that don't have containers
        if (p.getSpec() == null) {
          continue;
        }
        // Make sure volume is created
        assertTrue(
            p.getSpec()
                .getVolumes()
                .stream()
                .anyMatch(
                    v ->
                        v.getName().equals(PROJECTS_VOLUME_NAME)
                            && v.getPersistentVolumeClaim()
                                .getClaimName()
                                .equals(PROJECTS_VOLUME_NAME)));
        for (Container c : p.getSpec().getContainers()) {
          assertEquals(c.getImagePullPolicy(), "Always");
          assertTrue(
              c.getVolumeMounts()
                  .stream()
                  .anyMatch(
                      vm ->
                          vm.getName().equals(PROJECTS_VOLUME_NAME)
                              && vm.getMountPath().equals(PROJECT_MOUNT_PATH)));
        }
      }
    }
  }

  @Test
  public void shouldProvisionDevfileVolumesIfSpecifiedIntoMachineConfig() throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    List<HasMetadata> k8sList = toK8SList(yamlRecipeContent).getItems();
    doReturn(k8sList).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setVolumes(asList(new VolumeImpl("foo", "/foo1"), new VolumeImpl("bar", "/bar1")));
    ArgumentCaptor<Map<String, MachineConfigImpl>> mapCaptor = ArgumentCaptor.forClass(Map.class);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    verify(k8sEnvProvisioner).provision(any(), any(), any(), mapCaptor.capture());
    Map<String, MachineConfigImpl> configMaps = mapCaptor.getValue();

    for (MachineConfig config : configMaps.values()) {
      assertEquals(config.getVolumes().size(), 2);
      assertTrue(
          config
              .getVolumes()
              .entrySet()
              .stream()
              .anyMatch(
                  entry ->
                      entry.getKey().equals("foo") && entry.getValue().getPath().equals("/foo1")));
      assertTrue(
          config
              .getVolumes()
              .entrySet()
              .stream()
              .anyMatch(
                  entry ->
                      entry.getKey().equals("bar") && entry.getValue().getPath().equals("/bar1")));
    }
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Conflicting volume with same name \\('foo_volume'\\) but different path \\('/foo1'\\) found for component 'foo' and its container 'server'.")
  public void shouldThrowExceptionWhenDevfileVolumeNameExists() throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    List<HasMetadata> k8sList = toK8SList(yamlRecipeContent).getItems();
    doReturn(k8sList).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setVolumes(Collections.singletonList(new VolumeImpl("foo_volume", "/foo1")));

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Conflicting volume with same path \\('/foo/bar'\\) but different name \\('foo'\\) found for component 'foo' and its container 'server'.")
  public void shouldThrowExceptionWhenDevfileVolumePathExists() throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    List<HasMetadata> k8sList = toK8SList(yamlRecipeContent).getItems();
    doReturn(k8sList).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setVolumes(Collections.singletonList(new VolumeImpl("foo", "/foo/bar")));

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);
  }

  @Test
  public void shouldProvisionEnvIntoK8SList() throws Exception {
    // given
    List<HasMetadata> k8sList = new ArrayList<>();
    Pod pod1 =
        new PodBuilder()
            .withNewMetadata()
            .withName("pod1")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    Pod pod2 =
        new PodBuilder()
            .withNewMetadata()
            .withName("pod2")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    k8sList.add(pod1);
    k8sList.add(pod2);
    doReturn(k8sList).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    List<EnvImpl> envToApply = singletonList(new EnvImpl("TEST_ENV", "anyValue"));
    component.setEnv(envToApply);

    // when
    applier.apply(workspaceConfig, component, s -> "content");

    // then
    envVars.apply(new PodData(pod1), envToApply);
    envVars.apply(new PodData(pod2), envToApply);
  }

  @Test
  public void shouldProvisionMachinesMapWithComponentAttributePreSet() throws Exception {
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, MachineConfigImpl>> mapCaptor = ArgumentCaptor.forClass(Map.class);
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    List<HasMetadata> k8sList = toK8SList(yamlRecipeContent).getItems();
    doReturn(k8sList).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setMountSources(true);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    verify(k8sEnvProvisioner).provision(any(), any(), any(), mapCaptor.capture());
    Map<String, MachineConfigImpl> machines = mapCaptor.getValue();
    assertTrue(
        machines
            .values()
            .stream()
            .allMatch(
                config ->
                    config
                        .getAttributes()
                        .get(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE)
                        .equals(component.getAlias())));
  }

  @Test
  public void shouldFilterRecipeWithGivenSelectors() throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");

    final Map<String, String> selector = singletonMap("app.kubernetes.io/component", "webapp");
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setSelector(selector);
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig), eq(KubernetesEnvironment.TYPE), objectsCaptor.capture(), anyMap());
    List<HasMetadata> resultItemsList = objectsCaptor.getValue();
    assertEquals(resultItemsList.size(), 3);
    assertEquals(1, resultItemsList.stream().filter(it -> "Pod".equals(it.getKind())).count());
    assertEquals(1, resultItemsList.stream().filter(it -> "Service".equals(it.getKind())).count());
    assertEquals(1, resultItemsList.stream().filter(it -> "Route".equals(it.getKind())).count());
  }

  @Test(dependsOnMethods = "shouldFilterRecipeWithGivenSelectors", enabled = false)
  public void shouldSetMachineNameAttributeToCommandConfiguredInOpenShiftComponentWithOneContainer()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    final Map<String, String> selector = singletonMap("app.kubernetes.io/component", "webapp");
    ComponentImpl component = new ComponentImpl();
    component.setType(OPENSHIFT_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setSelector(selector);
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_ALIAS_COMMAND_ATTRIBUTE, COMPONENT_NAME);
    workspaceConfig.getCommands().add(command);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    CommandImpl actualCommand = workspaceConfig.getCommands().get(0);
    assertEquals(actualCommand.getAttributes().get(MACHINE_NAME_ATTRIBUTE), "petclinic/server");
  }

  @Test
  public void
      shouldNotSetMachineNameAttributeToCommandConfiguredInOpenShiftComponentWithMultipleContainers()
          throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    ComponentImpl component = new ComponentImpl();
    component.setType(OPENSHIFT_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);

    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_ALIAS_COMMAND_ATTRIBUTE, COMPONENT_NAME);
    workspaceConfig.getCommands().add(command);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    CommandImpl actualCommand = workspaceConfig.getCommands().get(0);
    assertNull(actualCommand.getAttributes().get(MACHINE_NAME_ATTRIBUTE));
  }

  @Test
  public void shouldChangeEntrypointsOnMatchingContainers() throws Exception {
    // given
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    List<String> command = asList("teh", "command");
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);

    EntrypointImpl entrypoint = new EntrypointImpl();
    entrypoint.setParentName("petclinic");
    entrypoint.setCommand(command);
    component.setEntrypoints(singletonList(entrypoint));

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    verify(k8sEnvProvisioner).provision(any(), any(), objectsCaptor.capture(), any());
    List<HasMetadata> list = objectsCaptor.getValue();
    for (HasMetadata o : list) {
      if (o instanceof Pod) {
        Pod p = (Pod) o;

        // ignore pods that don't have containers
        if (p.getSpec() == null) {
          continue;
        }

        Container c = p.getSpec().getContainers().get(0);
        if (o.getMetadata().getName().equals("petclinic")) {
          assertEquals(c.getCommand(), command);
        } else {
          assertTrue(c.getCommand() == null || c.getCommand().isEmpty());
        }
      }
    }
  }

  @Test
  public void shouldBeAbleToOverrideImagePullPolicy() throws Exception {
    // given
    applier =
        new KubernetesComponentToWorkspaceApplier(
            k8sRecipeParser,
            k8sEnvProvisioner,
            envVars,
            PROJECT_MOUNT_PATH,
            "1Gi",
            "ReadWriteOnce",
            "",
            "Never",
            k8sBasedComponents);
    String yamlRecipeContent = getResource("devfile/petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    verify(k8sEnvProvisioner).provision(any(), any(), objectsCaptor.capture(), any());
    List<HasMetadata> list = objectsCaptor.getValue();
    for (HasMetadata o : list) {
      if (o instanceof Pod) {
        Pod p = (Pod) o;

        // ignore pods that don't have containers
        if (p.getSpec() == null) {
          continue;
        }
        List<Container> containers = new ArrayList<>();
        containers.addAll(p.getSpec().getContainers());
        containers.addAll(p.getSpec().getInitContainers());
        for (Container con : containers) {
          assertEquals(con.getImagePullPolicy(), "Never");
        }
      }
    }
  }

  @Test
  public void shouldProvisionEndpointsToAllMachines()
      throws IOException, ValidationException, InfrastructureException, DevfileException {
    // given
    String endpointName = "petclinic-endpoint";
    Integer endpointPort = 8081;

    String yamlRecipeContent = getResource("devfile/petclinicPods.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setEndpoints(singletonList(new EndpointImpl(endpointName, endpointPort, emptyMap())));

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, MachineConfigImpl>> objectsCaptor =
        ArgumentCaptor.forClass(Map.class);
    verify(k8sEnvProvisioner).provision(any(), any(), any(), objectsCaptor.capture());
    Map<String, MachineConfigImpl> configs = objectsCaptor.getValue();
    assertEquals(configs.size(), 3);
    configs
        .values()
        .forEach(
            machineConfig -> {
              Map<String, ServerConfigImpl> serverConfigs = machineConfig.getServers();
              assertEquals(serverConfigs.size(), 1);
              assertTrue(serverConfigs.containsKey(endpointName));
              assertEquals(serverConfigs.get(endpointName).getPort(), endpointPort.toString());
              assertNull(serverConfigs.get(endpointName).getPath());
            });
  }

  @Test
  public void shouldProvisionEndpointWithAttributes()
      throws IOException, ValidationException, InfrastructureException, DevfileException {
    // given
    String endpointName = "petclinic-endpoint";
    Integer endpointPort = 8081;
    String endpointProtocol = "tcp";
    String endpointPath = "path";
    String endpointPublic = "false";
    String endpointSecure = "false";

    String yamlRecipeContent = getResource("devfile/petclinicPods.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);
    component.setEndpoints(
        singletonList(
            new EndpointImpl(
                endpointName,
                endpointPort,
                ImmutableMap.of(
                    "protocol",
                    endpointProtocol,
                    "path",
                    endpointPath,
                    "public",
                    endpointPublic,
                    "secure",
                    endpointSecure))));

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, MachineConfigImpl>> objectsCaptor =
        ArgumentCaptor.forClass(Map.class);
    verify(k8sEnvProvisioner).provision(any(), any(), any(), objectsCaptor.capture());
    Map<String, MachineConfigImpl> configs = objectsCaptor.getValue();
    assertEquals(configs.size(), 3);
    configs
        .values()
        .forEach(
            machineConfig -> {
              Map<String, ServerConfigImpl> serverConfigs = machineConfig.getServers();
              assertEquals(serverConfigs.size(), 1);
              assertTrue(serverConfigs.containsKey(endpointName));
              assertEquals(serverConfigs.get(endpointName).getPort(), endpointPort.toString());
              assertEquals(serverConfigs.get(endpointName).getPath(), endpointPath);
              assertEquals(serverConfigs.get(endpointName).getProtocol(), endpointProtocol);
              assertEquals(
                  serverConfigs.get(endpointName).getAttributes().get(REQUIRE_SUBDOMAIN), "true");
              assertEquals(
                  serverConfigs.get(endpointName).isSecure(), Boolean.parseBoolean(endpointSecure));
              assertEquals(
                  serverConfigs.get(endpointName).isInternal(),
                  !Boolean.parseBoolean(endpointPublic));
            });
  }

  private KubernetesList toK8SList(String content) {
    return unmarshal(content, KubernetesList.class);
  }

  private void replaceImagePullPolicy(KubernetesList list, String imagePullPolicy) {
    list.getItems()
        .stream()
        .filter(item -> item instanceof Pod)
        .map(item -> (Pod) item)
        .filter(pod -> pod.getSpec() != null)
        .flatMap(
            pod ->
                Stream.concat(
                    pod.getSpec().getContainers().stream(),
                    pod.getSpec().getInitContainers().stream()))
        .forEach(c -> c.setImagePullPolicy(imagePullPolicy));
  }

  private String getResource(String resourceName) throws IOException {
    return Files.readFile(getClass().getClassLoader().getResourceAsStream(resourceName));
  }
}
