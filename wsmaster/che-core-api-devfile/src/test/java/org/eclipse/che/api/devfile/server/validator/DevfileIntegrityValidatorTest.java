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

import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.PodBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Entrypoint;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Source;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
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

  private Devfile initialDevfile;

  private DevfileIntegrityValidator integrityValidator;

  @Mock private KubernetesRecipeParser kubernetesRecipeParser;

  @BeforeClass
  public void setUp() throws Exception {
    integrityValidator = new DevfileIntegrityValidator(kubernetesRecipeParser);
    String devFileYamlContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("devfile.yaml"));
    initialDevfile = objectMapper.readValue(devFileYamlContent, Devfile.class);
  }

  @Test
  public void shouldValidateCorrectDevfile() throws Exception {
    // when
    integrityValidator.validateDevfile(initialDevfile);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate tool name found:'mvn-stack'")
  public void shouldThrowExceptionOnDuplicateToolName() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getTools().add(new Tool().withName(initialDevfile.getTools().get(0).getName()));
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Multiple non plugin or editor type tools found: 'k8s', 'os'")
  public void shouldThrowExceptionOnMultipleNonPluginTools() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getTools().clear();
    broken
        .getTools()
        .add(new Tool().withName("k8s").withType(KUBERNETES_TOOL_TYPE).withLocal("foo.yaml"));
    broken
        .getTools()
        .add(new Tool().withName("os").withType(OPENSHIFT_TOOL_TYPE).withLocal("bar.yaml"));
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Multiple non plugin or editor type tools found: 'k8s', 'dockerimage'")
  public void shouldThrowExceptionOnKubernetesAndDockerimagesTools() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getCommands().clear();
    broken.getTools().clear();
    broken
        .getTools()
        .add(new Tool().withName("k8s").withType(KUBERNETES_TOOL_TYPE).withLocal("foo.yaml"));
    broken.getTools().add(new Tool().withName("dockerimage").withType(DOCKERIMAGE_TOOL_TYPE));
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Multiple editor tools found: 'theia-ide', 'editor-2'")
  public void shouldThrowExceptionOnMultipleEditors() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getTools().add(new Tool().withName("editor-2").withType(EDITOR_TOOL_TYPE));
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate command name found:'build'")
  public void shouldThrowExceptionOnDuplicateCommandName() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getCommands().add(new Command().withName(initialDevfile.getCommands().get(0).getName()));
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Command 'build' does not have actions.")
  public void shouldThrowExceptionWhenCommandDoesNotHaveActions() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getCommands().get(0).getActions().clear();

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Multiple actions in command 'build' are not supported yet.")
  public void shouldThrowExceptionWhenCommandHasMultipleActions() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getCommands().get(0).getActions().add(new Action());
    ;

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Command 'build' has action that refers to non-existing tools 'no_such_tool'")
  public void shouldThrowExceptionOnUnexistingCommandActionTool() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getCommands().get(0).getActions().clear();
    broken.getCommands().get(0).getActions().add(new Action().withTool("no_such_tool"));

    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Duplicate project name found:'petclinic'")
  public void shouldThrowExceptionOnDuplicateProjectName() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getProjects().add(new Project().withName(initialDevfile.getProjects().get(0).getName()));
    // when
    integrityValidator.validateDevfile(broken);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Invalid project name found:'.*'. Name must contain only Latin letters,"
              + "digits or these following special characters ._-")
  public void shouldThrowExceptionOnInvalidProjectName() throws Exception {
    Devfile broken = copyOf(initialDevfile);
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

    Devfile devfile = copyOf(initialDevfile);
    devfile.getTools().get(0).setLocalContent("content");
    devfile.getTools().get(0).setSelector(selector);

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

    Devfile devfile = copyOf(initialDevfile);
    devfile.getTools().get(0).setLocalContent("content");
    devfile
        .getTools()
        .get(0)
        .setEntrypoints(
            Collections.singletonList(new Entrypoint().withContainer("not that container")));

    // when
    integrityValidator.validateContentReferences(devfile, __ -> "");

    // then exception is thrown
  }

  private Devfile copyOf(Devfile source) {
    Devfile result = new Devfile();
    result.setName(source.getName());
    result.setSpecVersion(source.getSpecVersion());
    List<Project> projects = new ArrayList<>();
    for (Project project : source.getProjects()) {
      projects.add(
          new Project()
              .withName(project.getName())
              .withSource(
                  new Source()
                      .withType(project.getSource().getType())
                      .withLocation(project.getSource().getType())));
    }
    result.setProjects(projects);
    List<Tool> tools = new ArrayList<>();
    for (Tool tool : source.getTools()) {
      tools.add(new Tool().withId(tool.getId()).withName(tool.getName()).withType(tool.getType()));
    }
    result.setTools(tools);
    List<Command> commands = new ArrayList<>();
    for (Command command : source.getCommands()) {
      List<Action> actions = new ArrayList<>();
      for (Action action : command.getActions()) {
        actions.add(
            new Action()
                .withCommand(action.getCommand())
                .withTool(action.getTool())
                .withType(action.getType())
                .withWorkdir(action.getWorkdir()));
      }
      commands.add(
          new Command()
              .withName(command.getName())
              .withAttributes(command.getAttributes())
              .withActions(actions));
    }
    result.setCommands(commands);
    return result;
  }
}
