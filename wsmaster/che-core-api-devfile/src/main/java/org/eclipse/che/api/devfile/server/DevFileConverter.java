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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.devfile.server.Constants.CURRENT_SPEC_VERSION;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

/** Helps to convert Devfile into workspace config and back. */
public class DevFileConverter {

  public Devfile workspaceToDevFile(WorkspaceConfigImpl wsConfig) {
    Devfile devFile =
        new Devfile().withSpecVersion(CURRENT_SPEC_VERSION).withName(wsConfig.getName());

    // Manage projects
    List<Project> projects = new ArrayList<>();
    wsConfig
        .getProjects()
        .forEach(projectConfig -> projects.add(projectConfigToDevProject(projectConfig)));
    devFile.setProjects(projects);

    // Manage commands
    List<Command> commands = new ArrayList<>();
    wsConfig.getCommands().forEach(command -> commands.add(commandImplToDevCommand(command)));
    devFile.setCommands(commands);

    // Manage tools
    List<Tool> tools = new ArrayList<>();
    for (Map.Entry<String, String> entry : wsConfig.getAttributes().entrySet()) {
      if (entry.getKey().equals("editor")) {
        String editorId = entry.getValue();
        Tool editorTool =
            new Tool()
                .withType("cheEditor")
                .withId(editorId)
                .withName(findToolName(wsConfig, editorId));
        tools.add(editorTool);
      } else if (entry.getKey().equals("plugins")) {
        for (String pluginId : entry.getValue().split(",")) {
          Tool pluginTool =
              new Tool()
                  .withId(pluginId)
                  .withType("chePlugin")
                  .withName(findToolName(wsConfig, pluginId));
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
    devFile.getProjects().forEach(project -> projects.add(devProjectToProjectConfig(project)));
    config.setProjects(projects);

    // Manage tools
    Map<String, String> attributes = new HashMap<>();
    StringJoiner pluginsStringJoiner = new StringJoiner(",");
    StringJoiner toolIdToNameMappingStringJoiner = new StringJoiner(",");
    for (Tool tool : devFile.getTools()) {
      if (tool.getType().equals("cheEditor")) {
        attributes.put("editor", tool.getId());
      } else if (tool.getType().equals("chePlugin")) {
        pluginsStringJoiner.add(tool.getId());
      }
      toolIdToNameMappingStringJoiner.add(tool.getId() + "=" + tool.getName());
    }
    attributes.put("plugins", pluginsStringJoiner.toString());
    attributes.put("toolsAliases", toolIdToNameMappingStringJoiner.toString());
    config.setAttributes(attributes);

    // Manage commands
    List<CommandImpl> commands = new ArrayList<>();
    devFile
        .getCommands()
        .forEach(command -> commands.addAll(devCommandToCommandImpls(devFile, command)));
    config.setCommands(commands);
    return config;
  }

  private List<CommandImpl> devCommandToCommandImpls(Devfile devFile, Command devCommand) {
    List<CommandImpl> commands = new ArrayList<>();
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
    return commands;
  }

  private Command commandImplToDevCommand(CommandImpl command) {
    Command devCommand = new Command().withName(command.getName());
    Action action = new Action().withCommand(command.getCommandLine()).withType(command.getType());
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
    return devCommand;
  }

  private Project projectConfigToDevProject(ProjectConfigImpl projectConfig) {
    Source source =
        new Source()
            .withType(projectConfig.getSource().getType())
            .withLocation(projectConfig.getSource().getLocation());
    return new Project().withName(projectConfig.getName()).withSource(source);
  }

  private ProjectConfigImpl devProjectToProjectConfig(Project devProject) {
    ProjectConfigImpl projectConfig = new ProjectConfigImpl();
    projectConfig.setName(devProject.getName());
    projectConfig.setPath("/" + projectConfig.getName());
    SourceStorageImpl sourceStorage = new SourceStorageImpl();
    sourceStorage.setType(devProject.getSource().getType());
    sourceStorage.setLocation(devProject.getSource().getLocation());
    projectConfig.setSource(sourceStorage);
    return projectConfig;
  }

  private String findToolName(WorkspaceConfigImpl wsConfig, String toolId) {
    String aliasesString = firstNonNull(wsConfig.getAttributes().get("toolsAliases"), "");
    Optional<String> valueOpt =
        Arrays.stream(aliasesString.split(","))
            .filter(s -> s.split("=")[0].equals(toolId))
            .map(s -> s.split("=")[1])
            .findAny();
    return valueOpt.isPresent() ? valueOpt.get() : toolId.substring(0, toolId.indexOf(":"));
  }

  private static void validateCurrentVersion(Devfile devFile) throws DevFileFormatException {
    if (!CURRENT_SPEC_VERSION.equals(devFile.getSpecVersion())) {
      throw new DevFileFormatException(
          format("Provided Devfile has unsupported version %s", devFile.getSpecVersion()));
    }
  }
}
