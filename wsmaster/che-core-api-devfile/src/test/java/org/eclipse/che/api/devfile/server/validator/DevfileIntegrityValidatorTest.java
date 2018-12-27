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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Source;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.DevfileFormatException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

public class DevfileIntegrityValidatorTest {

  private ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

  private Devfile initialDevfile;

  private DevfileIntegrityValidator integrityValidator;

  @BeforeClass
  public void setUp() throws Exception {
    integrityValidator = new DevfileIntegrityValidator();
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
      expectedExceptionsMessageRegExp = "Multiple editor tools found: 'theia-ide', 'editor-2'")
  public void shouldThrowExceptionOnMultipleEditors() throws Exception {
    Devfile broken = copyOf(initialDevfile);
    broken.getTools().add(new Tool().withName("editor-2").withType("cheEditor"));
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
      expectedExceptionsMessageRegExp =
          "Found actions which refer to non-existing tools in command 'build':'no_such_tool'")
  public void shouldThrowExceptionOnUnexistingCommandActionTool() throws Exception {
    Devfile broken = copyOf(initialDevfile);
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
