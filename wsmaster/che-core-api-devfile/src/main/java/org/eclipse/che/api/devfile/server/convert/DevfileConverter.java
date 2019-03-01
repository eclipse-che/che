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
package org.eclipse.che.api.devfile.server.convert;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.stream.Collectors.toCollection;
import static org.eclipse.che.api.devfile.server.Constants.CURRENT_SPEC_VERSION;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.DevfileFactory;
import org.eclipse.che.api.devfile.server.DevfileRecipeFormatException;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.tool.ToolProvisioner;
import org.eclipse.che.api.devfile.server.convert.tool.ToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;

/**
 * Helps to convert Devfile to workspace config and back.
 *
 * @author Max Shaposhnyk
 * @author Sergii Leshchenko
 */
public class DevfileConverter {

  private final ProjectConverter projectConverter;
  private final CommandConverter commandConverter;
  private final Map<String, ToolToWorkspaceApplier> toolTypeToApplier;
  private final Set<ToolProvisioner> toolProvisioners;

  @Inject
  public DevfileConverter(
      ProjectConverter projectConverter,
      CommandConverter commandConverter,
      Set<ToolProvisioner> toolProvisioners,
      Map<String, ToolToWorkspaceApplier> toolTypeToApplier) {
    this.projectConverter = projectConverter;
    this.commandConverter = commandConverter;
    this.toolProvisioners = toolProvisioners;
    this.toolTypeToApplier = toolTypeToApplier;
  }

  /**
   * Exports workspace config into {@link Devfile}
   *
   * @param wsConfig initial workspace config
   * @return devfile resulted devfile
   * @throws WorkspaceExportException if export of given workspace config is impossible
   */
  public Devfile workspaceToDevFile(WorkspaceConfigImpl wsConfig) throws WorkspaceExportException {
    if (wsConfig.getEnvironments().size() > 1) {
      throw new WorkspaceExportException(
          format(
              "Workspace '%s' cannot be converted to devfile because it contains multiple environments",
              wsConfig.getName()));
    }

    Devfile devfile =
        DevfileFactory.newDevfile()
            .withSpecVersion(CURRENT_SPEC_VERSION)
            .withName(wsConfig.getName());

    // Manage projects
    devfile.setProjects(
        wsConfig
            .getProjects()
            .stream()
            .map(projectConverter::toDevfileProject)
            .collect(toCollection(ArrayList::new)));

    for (CommandImpl command : wsConfig.getCommands()) {
      devfile.getCommands().add(commandConverter.toDevfileCommand(command));
    }

    for (ToolProvisioner toolConverter : toolProvisioners) {
      toolConverter.provision(devfile, wsConfig);
    }

    return devfile;
  }

  /**
   * Converts given {@link Devfile} into {@link WorkspaceConfigImpl workspace config}.
   *
   * @param devfile initial devfile
   * @param contentProvider content provider for recipe-type tool
   * @return constructed workspace config
   * @throws DevfileException when general devfile error occurs
   * @throws DevfileException when devfile requires additional files content but the specified
   *     content provider does not support it
   * @throws DevfileFormatException when devfile format is invalid
   * @throws DevfileRecipeFormatException when content of the file specified in recipe type tool is
   *     empty or its format is invalid
   */
  public WorkspaceConfigImpl devFileToWorkspaceConfig(
      Devfile devfile, FileContentProvider contentProvider) throws DevfileException {
    checkArgument(devfile != null, "Devfile must not be null");
    checkArgument(contentProvider != null, "Content provider must not be null");

    validateCurrentVersion(devfile);
    WorkspaceConfigImpl config = new WorkspaceConfigImpl();

    config.setName(devfile.getName());

    for (Command command : devfile.getCommands()) {
      config.getCommands().add(commandConverter.toWorkspaceCommand(command));
    }

    // note that tool applier modifies commands in workspace config
    // so, commands should be already converted
    for (Tool tool : devfile.getTools()) {
      ToolToWorkspaceApplier applier = toolTypeToApplier.get(tool.getType());
      if (applier == null) {
        throw new DevfileException(
            String.format(
                "Devfile contains tool `%s` with type `%s` that can not be converted to workspace",
                tool.getName(), tool.getType()));
      }
      applier.apply(config, tool, contentProvider);
    }

    devfile
        .getProjects()
        .stream()
        .map(projectConverter::toWorkspaceProject)
        .forEach(project -> config.getProjects().add(project));

    return config;
  }

  private static void validateCurrentVersion(Devfile devFile) throws DevfileFormatException {
    if (Strings.isNullOrEmpty(devFile.getSpecVersion())) {
      throw new DevfileFormatException("Provided Devfile has no spec version specified");
    }
    if (!CURRENT_SPEC_VERSION.equals(devFile.getSpecVersion())) {
      throw new DevfileFormatException(
          format("Provided Devfile has unsupported version '%s'", devFile.getSpecVersion()));
    }
  }
}
