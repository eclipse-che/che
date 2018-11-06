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

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.devfile.Constants.SPEC_VERSION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.ChePlugin;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Definition;
import org.eclipse.che.api.devfile.model.DevFile;
import org.eclipse.che.api.devfile.model.Exec;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Source;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.model.ToolsCommand;
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
    devFile.setSpecVersion(SPEC_VERSION);
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
      ToolsCommand toolsCommand = new ToolsCommand();
      toolsCommand.setTool("???");
      Action action = new Action();
      Exec exec = new Exec();
      exec.setCommand(command.getCommandLine());
      exec.setWorkdir("???");
      action.setExec(exec);
      toolsCommand.setAction(action);
      devCommand.setToolsCommands(Collections.singletonList(toolsCommand));
      commands.add(devCommand);
    }
    devFile.setCommands(commands);

    // Manage tools
    List<Tool> tools = new ArrayList<>();
    if (wsConfig.getAttributes().containsKey("editor")) {
      Tool editorTool = new Tool();
      editorTool.setName("editor");
      Definition definition = new Definition();
      ChePlugin chePlugin = new ChePlugin();
      chePlugin.setName(wsConfig.getAttributes().get("editor"));
      definition.setChePlugin(chePlugin);
      editorTool.setDefinition(definition);
      tools.add(editorTool);
    }

    if (wsConfig.getAttributes().containsKey("plugins")) {
      for (String plugin : wsConfig.getAttributes().get("plugins").split(",")) {
        Tool pluginTool = new Tool();
        pluginTool.setName(plugin.substring(0, plugin.indexOf(":")));
        Definition definition = new Definition();
        ChePlugin chePlugin = new ChePlugin();
        chePlugin.setName(plugin);
        definition.setChePlugin(chePlugin);
        pluginTool.setDefinition(definition);
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

    // Manage commands
    List<CommandImpl> commands = new ArrayList<>();
    for (Command devCommand : devFile.getCommands()) {
      CommandImpl command = new CommandImpl();
      command.setName(devCommand.getName());
      // TODO: convert rest
      commands.add(command);
    }

    config.setCommands(commands);

    // Manage tools
    Map<String, String> attributes = new HashMap<>();

    for (Tool tool : devFile.getTools()) {
      attributes.put(tool.getName(), tool.getDefinition().getChePlugin().getName());
    }
    config.setAttributes(attributes);

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
    if (!SPEC_VERSION.equals(devFile.getSpecVersion())) {
      throw new DevFileFormatException(
          format("Provided devfile has unsupported version %s", devFile.getSpecVersion()));
    }
  }
}
