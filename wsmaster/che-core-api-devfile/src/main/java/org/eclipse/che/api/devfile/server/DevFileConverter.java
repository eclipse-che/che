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
package org.eclipse.che.api.devfile.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.devfile.server.Constants.CURRENT_SPEC_VERSION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Source;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

public class DevFileConverter {

  public Devfile workspaceToDevFile(WorkspaceConfigImpl wsConfig) {

    Devfile devFile = new Devfile().withVersion(CURRENT_SPEC_VERSION).withName(wsConfig.getName());

    // Manage projects
    List<Project> projects = new ArrayList<>();
    for (ProjectConfigImpl project : wsConfig.getProjects()) {
      Source source =
          new Source()
              .withType(project.getSource().getType())
              .withLocation(project.getSource().getLocation());
      Project devProject = new Project().withName(project.getName()).withSource(source);
      projects.add(devProject);
    }
    devFile.setProjects(projects);

    // Manage commands
    List<Command> commands = new ArrayList<>();
    for (CommandImpl command : wsConfig.getCommands()) {
      Command devCommand = new Command().withName(command.getName());
      Action action =
          new Action().withCommand(command.getCommandLine()).withType(command.getType());
      if (!isNullOrEmpty(command.getAttributes().get("workingDir"))) {
        action.setWorkdir(command.getAttributes().get("workingDir"));
      }
      // Remove internal attributes
      command.getAttributes().remove("workingDir");
      command.getAttributes().remove("pluginId");
      // Put others
      if (devCommand.getAttributes() == null) {
        devCommand.withAttributes(command.getAttributes());
      } else {
        devCommand.getAttributes().putAll(command.getAttributes());
      }

      commands.add(devCommand);
    }
    devFile.setCommands(commands);

    // Manage tools
    List<Tool> tools = new ArrayList<>();
    for (Map.Entry entry : wsConfig.getAttributes().entrySet()) {
      if (entry.getKey().equals("editor")) {
        String editorId = wsConfig.getAttributes().get("editor");
        Tool editorTool =
            new Tool()
                .withType("cheEditor")
                .withId(editorId)
                .withName(wsConfig.getAttributes().get(editorId));
        tools.add(editorTool);
      } else if (entry.getKey().equals("plugins")) {
        for (String pluginId : wsConfig.getAttributes().get("plugins").split(",")) {
          Tool pluginTool =
              new Tool()
                  .withId(pluginId)
                  .withType("chePlugin")
                  .withName(wsConfig.getAttributes().get(pluginId));
          tools.add(pluginTool);
        }
      }
    }

    devFile.setTools(tools);
    return devFile;
  }

  public WorkspaceConfigImpl devFileToWorkspaceConfig(Devfile devFile)
      throws DevFileFormatException {
    validateCurrentVersion(devFile);
    WorkspaceConfigImpl config = new WorkspaceConfigImpl();

    config.setName(devFile.getName());

    // Manage projects
    List<ProjectConfigImpl> projects = new ArrayList<>();
    for (Project devProject : devFile.getProjects()) {
      ProjectConfigImpl projectConfig = new ProjectConfigImpl();
      projectConfig.setName(devProject.getName());
      projectConfig.setPath("/" + projectConfig.getName());
      SourceStorageImpl sourceStorage = new SourceStorageImpl();
      sourceStorage.setType(devProject.getSource().getType());
      sourceStorage.setLocation(devProject.getSource().getLocation());
      projectConfig.setSource(sourceStorage);
      projects.add(projectConfig);
    }
    config.setProjects(projects);

    // Manage tools
    Map<String, String> attributes = new HashMap<>();
    StringJoiner pluginsStringJoiner = new StringJoiner(",");
    for (Tool tool : devFile.getTools()) {
      if (tool.getType().equals("cheEditor")) {
        attributes.put("editor", tool.getId());
      } else if (tool.getType().equals("chePlugin")) {
        pluginsStringJoiner.add(tool.getId());
      }
      attributes.put(tool.getId(), tool.getName());
    }
    attributes.put("plugins", pluginsStringJoiner.toString());
    config.setAttributes(attributes);

    // Manage commands
    List<CommandImpl> commands = new ArrayList<>();
    for (Command devCommand : devFile.getCommands()) {
      for (Action devAction : devCommand.getActions()) {
        CommandImpl command = new CommandImpl();
        command.setName(devCommand.getName() + ":" + devAction.getTool());
        command.setType(devAction.getType());
        command.setCommandLine(devAction.getCommand());
        if (devAction.getWorkdir() != null) {
          command.getAttributes().put("workingDir", devAction.getWorkdir());
        }
        Optional<Tool> toolOfCommand =
            devFile
                .getTools()
                .stream()
                .filter(tool -> tool.getName().equals(devAction.getTool()))
                .findFirst();
        if (toolOfCommand.isPresent() && !isNullOrEmpty(toolOfCommand.get().getId())) {
          command.getAttributes().put("pluginId", toolOfCommand.get().getId());
        }
        if (devCommand.getAttributes() != null) {
          command.getAttributes().putAll(devCommand.getAttributes());
        }
        commands.add(command);
      }
    }

    config.setCommands(commands);

    // TODO: Add default environment. Remove when it will be possible
    config.setDefaultEnv("default");
    EnvironmentImpl environment = new EnvironmentImpl();
    RecipeImpl recipe = new RecipeImpl();
    recipe.setType("dockerimage");
    recipe.setContent("eclipse/ubuntu_jdk8");
    environment.setRecipe(recipe);
    MachineConfigImpl machine = new MachineConfigImpl();
    machine.setAttributes(singletonMap("memoryLimitBytes", "2147483648"));
    environment.setMachines(singletonMap("dev-machine", machine));
    config.setEnvironments(singletonMap("default", environment));
    return config;
  }

  private static void validateCurrentVersion(Devfile devFile) throws DevFileFormatException {
    if (!CURRENT_SPEC_VERSION.equals(devFile.getVersion())) {
      throw new DevFileFormatException(
          format("Provided devfile has unsupported version %s", devFile.getVersion()));
    }
  }
}
