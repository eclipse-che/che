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
import static org.eclipse.che.api.devfile.Constants.CURRENT_SPEC_VERSION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.DevFile;
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

  static DevFile workspaceToDevFile(WorkspaceConfigImpl wsConfig) {
    DevFile devFile = new DevFile();
    devFile.setSpecVersion(CURRENT_SPEC_VERSION);
    devFile.setName(wsConfig.getName());

    // Manage projects
    List<Project> projects = new ArrayList<>();
    for (ProjectConfigImpl project : wsConfig.getProjects()) {
      Project devProject = new Project();
      devProject.setName(project.getName());
      Source source = new Source();
      source.setType(project.getSource().getType());
      source.setLocation(project.getSource().getLocation());
      for (Map.Entry<String, String> entry : project.getSource().getParameters().entrySet()) {
        source.setAdditionalProperty(entry.getKey(), entry.getValue());
      }
      devProject.setSource(source);
      projects.add(devProject);
    }
    devFile.setProjects(projects);

    // Manage commands
    List<Command> commands = new ArrayList<>();
    for (CommandImpl command : wsConfig.getCommands()) {
      Command devCommand = new Command();
      devCommand.setName(command.getName());
      Action action = new Action();
      action.setCommand(command.getCommandLine());
      if (!isNullOrEmpty(command.getAttributes().get("workingDir"))) {
        action.setWorkdir(command.getAttributes().get("workingDir"));
      }
      commands.add(devCommand);
    }
    devFile.setCommands(commands);

    // Manage tools
    List<Tool> tools = new ArrayList<>();
    if (wsConfig.getAttributes().containsKey("editor")) {
      Tool editorTool = new Tool();
      editorTool.setType("cheEditor");
      String editorId = wsConfig.getAttributes().get("editor");
      editorTool.setId(editorId);
      editorTool.setName(editorId.substring(0, editorId.indexOf(":")));
      tools.add(editorTool);
    }

    if (wsConfig.getAttributes().containsKey("plugins")) {
      for (String pluginId : wsConfig.getAttributes().get("plugins").split(",")) {
        Tool pluginTool = new Tool();
        pluginTool.setName(pluginId.substring(0, pluginId.indexOf(":")));
        pluginTool.setId(pluginId);
        pluginTool.setType("chePlugin");
        tools.add(pluginTool);
      }
    }

    devFile.setTools(tools);
    return devFile;
  }

  static WorkspaceConfigImpl devFileToWorkspaceConfig(DevFile devFile)
      throws DevFileFormatException {
    validateDevFile(devFile);
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
      sourceStorage.setParameters(devProject.getAdditionalProperties());
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
      } else {
        pluginsStringJoiner.add(tool.getId());
      }
    }
    attributes.put("plugins", pluginsStringJoiner.toString());
    config.setAttributes(attributes);

    // Manage commands
    List<CommandImpl> commands = new ArrayList<>();
    for (Command devCommand : devFile.getCommands()) {
      for (Action devAction : devCommand.getActions()) {
        CommandImpl command = new CommandImpl();
        command.setName(devCommand.getName() + ":" + devAction.getTool());
        command.setCommandLine(devAction.getCommand());
        command.getAttributes().put("workingDir", devAction.getWorkdir());
        Optional<Tool> toolOfCommand =
            devFile
                .getTools()
                .stream()
                .filter(tool -> tool.getName().equals(devAction.getTool()))
                .findFirst();
        if (toolOfCommand.isPresent()) {
          command.getAttributes().put("pluginId", toolOfCommand.get().getId());
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

  private static void validateDevFile(DevFile devFile) throws DevFileFormatException {
    if (!CURRENT_SPEC_VERSION.equals(devFile.getSpecVersion())) {
      throw new DevFileFormatException(
          format("Provided devfile has unsupported version %s", devFile.getSpecVersion()));
    }
  }
}
