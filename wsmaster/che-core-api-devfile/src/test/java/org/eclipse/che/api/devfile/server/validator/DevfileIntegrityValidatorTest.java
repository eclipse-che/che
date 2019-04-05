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
package org.eclipse.che.api.devfile.server.validator;

import static org.eclipse.che.api.devfile.server.Constants.EDITOR_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EntrypointImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ProjectImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

@Listeners(MockitoTestNGListener.class)
public class DevfileIntegrityValidatorTest {

  private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

  private DevfileImpl initialDevfile;

  private DevfileIntegrityValidator integrityValidator;

  @Mock private KubernetesRecipeParser kubernetesRecipeParser;

  @BeforeClass
  public void setUp() throws Exception {
    integrityValidator = new DevfileIntegrityValidator(kubernetesRecipeParser);
    String devFileYamlContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("devfile.yaml"));
    initialDevfile = objectMapper.readValue(devFileYamlContent, DevfileImpl.class);
  }

  @Test
  public void shouldValidateCorrectDevfile() throws Exception {
    // when
    integrityValidator.validateDevfile(initialDevfile);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate component name found:'mvn-stack'")
  public void shouldThrowExceptionOnDuplicateComponentName() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    ComponentImpl component = new ComponentImpl();
    component.setName(initialDevfile.getComponents().get(0).getName());
    broken.getComponents().add(component);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Multiple editor components found: 'theia-ide', 'editor-2'")
  public void shouldThrowExceptionOnMultipleEditors() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    ComponentImpl component = new ComponentImpl();
    component.setName("editor-2");
    component.setType(EDITOR_COMPONENT_TYPE);
    broken.getComponents().add(component);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate command name found:'build'")
  public void shouldThrowExceptionOnDuplicateCommandName() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);

    CommandImpl command = new CommandImpl();
    command.setName(initialDevfile.getCommands().get(0).getName());
    broken.getCommands().add(command);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Command 'build' does not have actions.")
  public void shouldThrowExceptionWhenCommandDoesNotHaveActions() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    broken.getCommands().get(0).getActions().clear();

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Multiple actions in command 'build' are not supported yet.")
  public void shouldThrowExceptionWhenCommandHasMultipleActions() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    broken.getCommands().get(0).getActions().add(new ActionImpl());

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Command 'build' has action that refers to non-existing components 'no_such_component'")
  public void shouldThrowExceptionOnUnexistingCommandActionComponent() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    broken.getCommands().get(0).getActions().clear();
    ActionImpl action = new ActionImpl();
    action.setComponent("no_such_component");
    broken.getCommands().get(0).getActions().add(action);

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate project name found:'petclinic'")
  public void shouldThrowExceptionOnDuplicateProjectName() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    ProjectImpl project = new ProjectImpl();
    project.setName(initialDevfile.getProjects().get(0).getName());
    broken.getProjects().add(project);
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Invalid project name found:'.*'. Name must contain only Latin letters,"
              + "digits or these following special characters ._-")
  public void shouldThrowExceptionOnInvalidProjectName() throws Exception {
    DevfileImpl broken = new DevfileImpl(initialDevfile);
    broken.getProjects().get(0).setName("./" + initialDevfile.getProjects().get(0).getName());
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(expectedExceptions = DevfileFormatException.class)
  public void shouldThrowExceptionOnSelectorFilteringOutEverything() throws Exception {
    // given
    when(kubernetesRecipeParser.parse(any(String.class)))
        .thenReturn(
            Collections.singletonList(
                new PodBuilder()
                    .withNewMetadata()
                    .addToLabels("app", "test")
                    .endMetadata()
                    .build()));

    Map<String, String> selector = new HashMap<>();
    selector.put("app", "a different value");

    DevfileImpl devfile = new DevfileImpl(initialDevfile);
    // this is the openshift component which is the only one sensitive to the selector in our
    // example
    // devfile
    devfile.getComponents().get(3).setReferenceContent("content");
    devfile.getComponents().get(3).setSelector(selector);

    // when
    integrityValidator.validateContentReferences(devfile, __ -> "");

    // then exception is thrown
  }

  @Test(expectedExceptions = DevfileFormatException.class)
  public void shouldThrowExceptionOnEntrypointNotMatchingAnyContainer() throws Exception {
    // given
    when(kubernetesRecipeParser.parse(any(String.class)))
        .thenReturn(
            Collections.singletonList(
                new PodBuilder()
                    .withNewSpec()
                    .addNewContainer()
                    .withName("container")
                    .endContainer()
                    .endSpec()
                    .build()));

    DevfileImpl devfile = new DevfileImpl(initialDevfile);
    devfile.getComponents().get(0).setReferenceContent("content");
    EntrypointImpl entrypoint = new EntrypointImpl();
    entrypoint.setContainerName("not that container");
    devfile.getComponents().get(0).setEntrypoints(Collections.singletonList(entrypoint));

    // when
    integrityValidator.validateContentReferences(devfile, __ -> "");

    // then exception is thrown
  }

  @Test
  public void shouldNotValidateContentReferencesOnNonKuberenetesComponents() throws Exception {
    // given

    // just remove all the content-referencing components and check that all still works
    DevfileImpl devfile = new DevfileImpl(initialDevfile);
    Iterator<ComponentImpl> it = devfile.getComponents().iterator();
    while (it.hasNext()) {
      String componentType = it.next().getType();
      if (componentType.equals(KUBERNETES_COMPONENT_TYPE)
          || componentType.equals(OPENSHIFT_COMPONENT_TYPE)) {
        it.remove();
      }
    }

    // when
    integrityValidator.validateContentReferences(devfile, __ -> "");

    // then
    // no exception is thrown
  }
}
