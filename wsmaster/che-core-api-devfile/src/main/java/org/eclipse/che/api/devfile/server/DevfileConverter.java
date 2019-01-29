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
import static java.util.Collections.singletonMap;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

/** Helps to convert devfile into workspace config and back. */
public class DevfileConverter {

  private DevfileEnvironmentFactory devfileEnvironmentFactory;

  @Inject
  public DevfileConverter(DevfileEnvironmentFactory devfileEnvironmentFactory) {
    this.devfileEnvironmentFactory = devfileEnvironmentFactory;
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
   * Converts given {@link Devfile} into workspace config.
   *
   * @param devfile initial devfile
   * @param recipeFileContentProvider content provider for recipe-type tool
   * @return constructed workspace config
   * @throws DevfileException when general devfile error occurs
   * @throws DevfileFormatException when devfile format is invalid
   * @throws DevfileRecipeFormatException when content of the file specified in recipe type tool is
   *     empty or its format is invalid
   */
  public WorkspaceConfigImpl devFileToWorkspaceConfig(
      Devfile devfile, RecipeFileContentProvider recipeFileContentProvider)
      throws DevfileException {
    validateCurrentVersion(devfile);
    WorkspaceConfigImpl config = new WorkspaceConfigImpl();

    config.setName(devfile.getName());

    // Manage projects
    List<ProjectConfigImpl> projects = new ArrayList<>();
    devfile.getProjects().forEach(project -> projects.add(devProjectToProjectConfig(project)));
    config.setProjects(projects);

    // Manage tools
    Map<String, String> attributes = new HashMap<>();
    StringJoiner pluginsStringJoiner = new StringJoiner(",");
    StringJoiner toolIdToNameMappingStringJoiner = new StringJoiner(",");
    for (Tool tool : devfile.getTools()) {
      switch (tool.getType()) {
        case EDITOR_TOOL_TYPE:
          attributes.put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, tool.getId());
          break;
        case PLUGIN_TOOL_TYPE:
          pluginsStringJoiner.add(tool.getId());
          break;
        case KUBERNETES_TOOL_TYPE:
        case OPENSHIFT_TOOL_TYPE:
          try {
            EnvironmentImpl environment =
                devfileEnvironmentFactory.createEnvironment(tool, recipeFileContentProvider);
            final String environmentName = tool.getName();
            config.setDefaultEnv(environmentName);
            config.setEnvironments(singletonMap(environmentName, environment));
          } catch (IllegalArgumentException e) {
            throw new DevfileFormatException(e.getMessage(), e);
          }
          continue;
        default:
          throw new DevfileFormatException(
              format("Unsupported tool %s type provided: %s", tool.getName(), tool.getType()));
      }
      toolIdToNameMappingStringJoiner.add(tool.getId() + "=" + tool.getName());
    }
    if (pluginsStringJoiner.length() > 0) {
      attributes.put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, pluginsStringJoiner.toString());
    }
    if (toolIdToNameMappingStringJoiner.length() > 0) {
      attributes.put(ALIASES_WORKSPACE_ATTRIBUTE_NAME, toolIdToNameMappingStringJoiner.toString());
    }
    config.setAttributes(attributes);

    // Manage commands
    List<CommandImpl> commands = new ArrayList<>();
    devfile
        .getCommands()
        .forEach(command -> commands.addAll(devCommandToCommandImpls(devfile, command)));
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
        command.getAttributes().put(WORKING_DIRECTORY_ATTRIBUTE, devAction.getWorkdir());
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

  private Command commandImplToDevCommand(CommandImpl command, Map<String, String> toolsIdToName) {
    Command devCommand = new Command().withName(command.getName());
    Action action = new Action().withCommand(command.getCommandLine()).withType(command.getType());
    String workingDir = command.getAttributes().get(WORKING_DIRECTORY_ATTRIBUTE);
    if (!isNullOrEmpty(workingDir)) {
      action.setWorkdir(workingDir);
    }
    action.setTool(toolsIdToName.getOrDefault(command.getAttributes().get("pluginId"), ""));
    devCommand.getActions().add(action);
    devCommand.setAttributes(command.getAttributes());
    // Remove internal attributes
    devCommand.getAttributes().remove(WORKING_DIRECTORY_ATTRIBUTE);
    devCommand.getAttributes().remove("pluginId");
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
        .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
  }

  private static void validateCurrentVersion(Devfile devFile) throws DevfileFormatException {
    if (!CURRENT_SPEC_VERSION.equals(devFile.getSpecVersion())) {
      throw new DevfileFormatException(
          format("Provided Devfile has unsupported version %s", devFile.getSpecVersion()));
    }
  }
}
