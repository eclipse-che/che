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

import static io.fabric8.kubernetes.client.utils.Serialization.unmarshal;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.Pod;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.devfile.URLFileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
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

  private WorkspaceConfigImpl workspaceConfig;

  private KubernetesComponentToWorkspaceApplier applier;
  @Mock private KubernetesRecipeParser k8sRecipeParser;
  @Mock private KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @Captor private ArgumentCaptor<List<HasMetadata>> objectsCaptor;

  @BeforeMethod
  public void setUp() {
    Set<String> k8sBasedComponents = new HashSet<>();
    k8sBasedComponents.add(KUBERNETES_COMPONENT_TYPE);
    k8sBasedComponents.add("openshift"); // so that we can work with the petclinic.yaml
    applier =
        new KubernetesComponentToWorkspaceApplier(
            k8sRecipeParser, k8sEnvProvisioner, k8sBasedComponents);

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
    verify(k8sEnvProvisioner)
        .provision(
            workspaceConfig,
            KubernetesEnvironment.TYPE,
            toK8SList(yamlRecipeContent).getItems(),
            emptyMap());
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

    verify(k8sEnvProvisioner)
        .provision(
            workspaceConfig,
            KubernetesEnvironment.TYPE,
            toK8SList(yamlRecipeContent).getItems(),
            emptyMap());
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
            eq(workspaceConfig),
            eq(KubernetesEnvironment.TYPE),
            objectsCaptor.capture(),
            eq(emptyMap()));
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

  private KubernetesList toK8SList(String content) {
    return unmarshal(content, KubernetesList.class);
  }

  private String getResource(String resourceName) throws IOException {
    return Files.readFile(getClass().getClassLoader().getResourceAsStream(resourceName));
  }
}
