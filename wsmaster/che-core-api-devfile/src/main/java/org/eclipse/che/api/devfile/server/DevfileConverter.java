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
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.core.model.workspace.config.Command.PLUGIN_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.Command.WORKING_DIRECTORY_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.ALIASES_WORKSPACE_ATTRIBUTE_NAME;
import static org.eclipse.che.api.devfile.server.Constants.CURRENT_SPEC_VERSION;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_TOOL_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import javax.inject.Inject;
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

/**
 * Helps to convert Devfile into workspace config and back.
 *
 * @author Max Shaposhnyk
 * @author Sergii Leshchenko
 */
public class DevfileConverter {

  private KubernetesToolApplier kubernetesToolApplier;

  @Inject
  public DevfileConverter(KubernetesToolApplier kubernetesToolApplier) {
    this.kubernetesToolApplier = kubernetesToolApplier;
  }

  /**
   * Exports workspace config into {@link Devfile}
   *
   * @param wsConfig initial workspace config
   * @return devfile resulted devfile
   * @throws WorkspaceExportException if export of given workspace config is impossible
   */
  public Devfile workspaceToDevFile(WorkspaceConfigImpl wsConfig) throws WorkspaceExportException {

    if (!isNullOrEmpty(wsConfig.getDefaultEnv()) || !wsConfig.getEnvironments().isEmpty()) {
      throw new WorkspaceExportException(
          format(
              "Workspace %s cannot be converted to devfile since it contains environments which have no equivalent in devfile model",
              wsConfig.getName()));
    }

    Devfile devfile =
        new Devfile().withSpecVersion(CURRENT_SPEC_VERSION).withName(wsConfig.getName());

    // Manage projects
    List<Project> projects = new ArrayList<>();
    wsConfig
        .getProjects()
        .forEach(projectConfig -> projects.add(projectConfigToDevProject(projectConfig)));
    devfile.setProjects(projects);

    // Manage commands
    Map<String, String> toolsIdToName = parseTools(wsConfig);
    List<Command> commands = new ArrayList<>();
    wsConfig
        .getCommands()
        .forEach(command -> commands.add(commandImplToDevCommand(command, toolsIdToName)));
    devfile.setCommands(commands);

    // Manage tools
    List<Tool> tools = new ArrayList<>();
    for (Map.Entry<String, String> entry : wsConfig.getAttributes().entrySet()) {
      if (entry.getKey().equals(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE)) {
        String editorId = entry.getValue();
        Tool editorTool =
            new Tool()
                .withType(EDITOR_TOOL_TYPE)
                .withId(editorId)
                .withName(toolsIdToName.getOrDefault(editorId, editorId));
        tools.add(editorTool);
      } else if (entry.getKey().equals(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE)) {
        for (String pluginId : entry.getValue().split(",")) {
          Tool pluginTool =
              new Tool()
                  .withId(pluginId)
                  .withType(PLUGIN_TOOL_TYPE)
                  .withName(toolsIdToName.getOrDefault(pluginId, pluginId));
          tools.add(pluginTool);
        }
      }
    }
    devfile.setTools(tools);
    return devfile;
  }

  /**
   * Converts given {@link Devfile} into {@link WorkspaceConfigImpl workspace config}.
   *
   * @param devfile initial devfile
   * @param contentProvider content provider for recipe-type tool
   * @return constructed workspace config
   * @throws DevfileException when general devfile error occurs
   * @throws DevfileFormatException when devfile format is invalid
   * @throws DevfileRecipeFormatException when content of the file specified in recipe type tool is
   *     empty or its format is invalid
   */
  public WorkspaceConfigImpl devFileToWorkspaceConfig(
      Devfile devfile, RecipeFileContentProvider contentProvider) throws DevfileException {
    validateCurrentVersion(devfile);
    WorkspaceConfigImpl config = new WorkspaceConfigImpl();

    config.setName(devfile.getName());

    // fill in WorkspaceConfig with configured projects
    List<ProjectConfigImpl> projects = new ArrayList<>();
    devfile.getProjects().forEach(project -> projects.add(devProjectToProjectConfig(project)));
    config.setProjects(projects);

    // fill in Workspace Config with configured tools
    fillInTools(config, devfile, contentProvider);

    // fill in Workspace Config with configured commands
    fillInCommands(config, devfile);

    return config;
  }

  private void fillInTools(
      WorkspaceConfigImpl config, Devfile devfile, RecipeFileContentProvider contentProvider)
      throws DevfileException {
    // Manage tools
    StringJoiner pluginsStringJoiner = new StringJoiner(",");
    for (Tool tool : devfile.getTools()) {
      switch (tool.getType()) {
        case EDITOR_TOOL_TYPE:
          config.getAttributes().put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, tool.getId());
          break;
        case PLUGIN_TOOL_TYPE:
          pluginsStringJoiner.add(tool.getId());
          break;
        case KUBERNETES_TOOL_TYPE:
        case OPENSHIFT_TOOL_TYPE:
          kubernetesToolApplier.apply(tool, devfile, config, contentProvider);
          break;
        default:
          throw new DevfileFormatException(
              format("Unsupported tool %s type provided: %s", tool.getName(), tool.getType()));
      }
    }

    if (pluginsStringJoiner.length() > 0) {
      config
          .getAttributes()
          .put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, pluginsStringJoiner.toString());
    }

    config
        .getAttributes()
        .put(
            ALIASES_WORKSPACE_ATTRIBUTE_NAME,
            devfile
                .getTools()
                .stream()
                .filter(tool -> tool.getId() != null)
                .map(tool -> tool.getId() + "=" + tool.getName())
                .collect(Collectors.joining(",")));
  }

  private void fillInCommands(WorkspaceConfigImpl config, Devfile devfile)
      throws DevfileFormatException {
    Map<String, Tool> tools = devfile.getTools().stream().collect(toMap(Tool::getName, identity()));
    List<CommandImpl> commands = new ArrayList<>();

    for (Command devCommand : devfile.getCommands()) {
      if (devCommand.getActions().size() != 1) {
        throw new DevfileFormatException(
            format("Command `%s` MUST has one and only one action", devCommand.getName()));
      }
      Action commandAction = devCommand.getActions().get(0);

      Tool toolOfCommand = tools.get(commandAction.getTool());

      if (toolOfCommand == null) {
        throw new DevfileFormatException(
            String.format(
                "Action of the command '%s' references missing tool '%s'",
                devCommand.getName(), commandAction.getTool()));
      }

      commands.add(devCommandToCommandImpl(devCommand, commandAction, toolOfCommand));
    }

    config.setCommands(commands);
  }

  private CommandImpl devCommandToCommandImpl(
      Command devCommand, Action commandAction, Tool toolOfCommand) {
    CommandImpl command = new CommandImpl();
    command.setName(devCommand.getName());
    command.setType(commandAction.getType());
    command.setCommandLine(commandAction.getCommand());

    if (commandAction.getWorkdir() != null) {
      command.getAttributes().put(WORKING_DIRECTORY_ATTRIBUTE, commandAction.getWorkdir());
    }

    if (PLUGIN_TOOL_TYPE.equals(toolOfCommand.getType())
        || EDITOR_TOOL_TYPE.equals(toolOfCommand.getType())) {
      command.getAttributes().put(PLUGIN_ATTRIBUTE, toolOfCommand.getId());
    }

    if (devCommand.getAttributes() != null) {
      command.getAttributes().putAll(devCommand.getAttributes());
    }

    return command;
  }

  private Command commandImplToDevCommand(CommandImpl command, Map<String, String> toolsIdToName) {
    Command devCommand = new Command().withName(command.getName());
    Action action = new Action().withCommand(command.getCommandLine()).withType(command.getType());
    String workingDir = command.getAttributes().get(WORKING_DIRECTORY_ATTRIBUTE);
    if (!isNullOrEmpty(workingDir)) {
      action.setWorkdir(workingDir);
    }
    action.setTool(toolsIdToName.getOrDefault(command.getAttributes().get(PLUGIN_ATTRIBUTE), ""));
    devCommand.getActions().add(action);
    devCommand.setAttributes(command.getAttributes());
    // Remove internal attributes
    devCommand.getAttributes().remove(WORKING_DIRECTORY_ATTRIBUTE);
    devCommand.getAttributes().remove(PLUGIN_ATTRIBUTE);
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

  private Map<String, String> parseTools(WorkspaceConfigImpl wsConfig) {
    String aliasesString =
        firstNonNull(wsConfig.getAttributes().get(ALIASES_WORKSPACE_ATTRIBUTE_NAME), "");
    return Arrays.stream(aliasesString.split(","))
        .map(s -> s.split("=", 2))
        .collect(toMap(arr -> arr[0], arr -> arr[1]));
  }

  private static void validateCurrentVersion(Devfile devFile) throws DevfileFormatException {
    if (!CURRENT_SPEC_VERSION.equals(devFile.getSpecVersion())) {
      throw new DevfileFormatException(
          format("Provided Devfile has unsupported version %s", devFile.getSpecVersion()));
    }
  }
}
