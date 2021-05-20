/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile.convert;

import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.COMMAND_ACTION_REFERENCE_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.Command.COMMAND_ACTION_REFERENCE_CONTENT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.Command.WORKING_DIRECTORY_ATTRIBUTE;

import java.io.IOException;
import org.eclipse.che.api.core.model.workspace.devfile.Action;
import org.eclipse.che.api.core.model.workspace.devfile.Command;
import org.eclipse.che.api.workspace.server.devfile.Constants;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;

/**
 * Helps to convert {@link CommandImpl workspace command} to {@link Command devfile command} and
 * vice versa.
 *
 * @author Sergii Leshchenko
 */
public class CommandConverter {

  /**
   * Converts the specified workspace command to devfile command.
   *
   * @param command source workspace command
   * @return created devfile command based on the specified workspace command
   * @throws WorkspaceExportException if workspace command does not has specified component name
   *     attribute where it should be run
   */
  public CommandImpl toDevfileCommand(
      org.eclipse.che.api.workspace.server.model.impl.CommandImpl command)
      throws WorkspaceExportException {
    String componentName =
        command.getAttributes().remove(Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE);
    if (componentName == null) {
      throw new WorkspaceExportException(
          format(
              "Command `%s` has no specified component where it should be run", command.getName()));
    }

    CommandImpl devCommand = new CommandImpl();
    devCommand.setName(command.getName());
    ActionImpl action = new ActionImpl();
    action.setCommand(command.getCommandLine());
    action.setType(command.getType());
    action.setWorkdir(command.getAttributes().remove(WORKING_DIRECTORY_ATTRIBUTE));
    action.setComponent(componentName);
    action.setType(Constants.EXEC_ACTION_TYPE);
    devCommand.getActions().add(action);
    devCommand.setAttributes(command.getAttributes());
    return devCommand;
  }

  /**
   * Converts the specified devfile command to workspace command.
   *
   * @param devfileCommand devfile command that should be converted
   * @return created workspace command based on the specified devfile command
   * @throws DevfileFormatException if devfile command does not have any action
   * @throws DevfileFormatException if devfile command has more than one action
   */
  public org.eclipse.che.api.workspace.server.model.impl.CommandImpl toWorkspaceCommand(
      Command devfileCommand, FileContentProvider fileContentProvider) throws DevfileException {
    if (devfileCommand.getActions().size() != 1) {
      throw new DevfileFormatException(
          format("Command `%s` MUST has one and only one action", devfileCommand.getName()));
    }

    Action commandAction = devfileCommand.getActions().get(0);

    return toWorkspaceCommand(devfileCommand, commandAction, fileContentProvider);
  }

  private org.eclipse.che.api.workspace.server.model.impl.CommandImpl toWorkspaceCommand(
      Command devCommand, Action commandAction, FileContentProvider contentProvider)
      throws DevfileException {
    org.eclipse.che.api.workspace.server.model.impl.CommandImpl command =
        new org.eclipse.che.api.workspace.server.model.impl.CommandImpl();
    command.setName(devCommand.getName());
    command.setType(commandAction.getType());
    command.setCommandLine(commandAction.getCommand());
    command.setPreviewUrl(devCommand.getPreviewUrl());

    if (commandAction.getWorkdir() != null) {
      command.getAttributes().put(WORKING_DIRECTORY_ATTRIBUTE, commandAction.getWorkdir());
    }

    if (commandAction.getComponent() != null) {
      command
          .getAttributes()
          .put(Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE, commandAction.getComponent());
    }

    if (commandAction.getReference() != null) {
      command.getAttributes().put(COMMAND_ACTION_REFERENCE_ATTRIBUTE, commandAction.getReference());
    }

    if (commandAction.getReferenceContent() != null) {
      command
          .getAttributes()
          .put(COMMAND_ACTION_REFERENCE_CONTENT_ATTRIBUTE, commandAction.getReferenceContent());
    } else if (commandAction.getReference() != null) {
      try {
        String referenceContent = contentProvider.fetchContent(commandAction.getReference());
        command.getAttributes().put(COMMAND_ACTION_REFERENCE_CONTENT_ATTRIBUTE, referenceContent);
      } catch (IOException e) {
        throw new DevfileException(
            format(
                "Failed to fetch content of action from reference %s: %s",
                commandAction.getReference(), e.getMessage()),
            e);
      }
    }

    command.getAttributes().putAll(devCommand.getAttributes());

    return command;
  }
}
