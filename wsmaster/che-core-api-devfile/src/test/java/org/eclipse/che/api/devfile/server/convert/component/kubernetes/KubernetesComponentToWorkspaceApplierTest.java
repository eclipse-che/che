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
package org.eclipse.che.api.devfile.server.convert.component.kubernetes;

import static io.fabric8.kubernetes.client.utils.Serialization.unmarshal;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.COMPONENT_NAME_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_COMPONENT_TYPE;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.devfile.model.Component;
import org.eclipse.che.api.devfile.model.Entrypoint;
import org.eclipse.che.api.devfile.server.FileContentProvider.FetchNotSupportedProvider;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
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

  public static final String LOCAL_FILENAME = "local.yaml";
  public static final String COMPONENT_NAME = "foo";

  private WorkspaceConfigImpl workspaceConfig;

  private KubernetesComponentToWorkspaceApplier applier;
  @Mock private KubernetesRecipeParser k8sRecipeParser;
  @Mock private KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @Captor private ArgumentCaptor<List<HasMetadata>> objectsCaptor;

  @BeforeMethod
  public void setUp() {
    applier = new KubernetesComponentToWorkspaceApplier(k8sRecipeParser, k8sEnvProvisioner);

    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Fetching content of file `local.yaml` specified in `local` field of component `foo` is not "
              + "supported. Please provide its content in `localContent` field. Cause: fetch is not supported")
  public void
      shouldThrowExceptionWhenRecipeComponentIsPresentAndContentProviderDoesNotSupportFetching()
          throws Exception {
    // given
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME);

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
              + LOCAL_FILENAME
              + " for component '"
              + COMPONENT_NAME
              + "': .*")
  public void shouldThrowExceptionWhenRecipeContentIsNotAValidYaml() throws Exception {
    // given
    doThrow(new ValidationException("non valid")).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME);

    // when
    applier.apply(workspaceConfig, component, s -> "some_non_yaml_content");
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error during recipe content retrieval for component 'foo' with type 'kubernetes': fetch failed")
  public void shouldThrowExceptionWhenExceptionHappensOnContentProvider() throws Exception {
    // given
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME);

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
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

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
  public void shouldUseLocalContentAsRecipeIfPresent() throws Exception {
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withLocalContent(yamlRecipeContent)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    applier.apply(workspaceConfig, component, new FetchNotSupportedProvider());

    verify(k8sEnvProvisioner)
        .provision(
            workspaceConfig,
            KubernetesEnvironment.TYPE,
            toK8SList(yamlRecipeContent).getItems(),
            emptyMap());
  }

  @Test
  public void shouldProvisionEnvironmentWithCorrectRecipeTypeAndContentFromOSList()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            workspaceConfig,
            OpenShiftEnvironment.TYPE,
            toK8SList(yamlRecipeContent).getItems(),
            emptyMap());
  }

  @Test
  public void shouldFilterRecipeWithGivenSelectors() throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");

    final Map<String, String> selector = singletonMap("app.kubernetes.io/component", "webapp");
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(selector);
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    verify(k8sEnvProvisioner)
        .provision(
            eq(workspaceConfig),
            eq(OpenShiftEnvironment.TYPE),
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
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    final Map<String, String> selector = singletonMap("app.kubernetes.io/component", "webapp");
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(selector);
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_NAME_COMMAND_ATTRIBUTE, COMPONENT_NAME);
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
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_NAME_COMMAND_ATTRIBUTE, COMPONENT_NAME);
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
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    List<String> command = asList("teh", "command");
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withEntrypoints(
                singletonList(new Entrypoint().withParentName("petclinic").withCommand(command)))
            .withSelector(Collections.emptyMap());

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
