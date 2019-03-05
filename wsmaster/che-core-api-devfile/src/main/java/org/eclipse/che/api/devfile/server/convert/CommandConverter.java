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

import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.WORKING_DIRECTORY_ATTRIBUTE;

import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.server.Constants;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;

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
   * @throws WorkspaceExportException if workspace command does not has specified tool name
   *     attribute where it should be run
   */
  public Command toDevfileCommand(CommandImpl command) throws WorkspaceExportException {
    String toolName = command.getAttributes().remove(Constants.TOOL_NAME_COMMAND_ATTRIBUTE);
    if (toolName == null) {
      throw new WorkspaceExportException(
          format("Command `%s` has no specified tool where it should be run", command.getName()));
    }

    Command devCommand = new Command().withName(command.getName());
    Action action = new Action().withCommand(command.getCommandLine()).withType(command.getType());
    action.setWorkdir(command.getAttributes().remove(WORKING_DIRECTORY_ATTRIBUTE));
    action.setTool(toolName);
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
  public CommandImpl toWorkspaceCommand(Command devfileCommand) throws DevfileFormatException {
    if (devfileCommand.getActions().size() != 1) {
      throw new DevfileFormatException(
          format("Command `%s` MUST has one and only one action", devfileCommand.getName()));
    }

    Action commandAction = devfileCommand.getActions().get(0);

    return toWorkspaceCommand(devfileCommand, commandAction);
  }

  private CommandImpl toWorkspaceCommand(Command devCommand, Action commandAction) {
    CommandImpl command = new CommandImpl();
    command.setName(devCommand.getName());
    command.setType(commandAction.getType());
    command.setCommandLine(commandAction.getCommand());

    if (commandAction.getWorkdir() != null) {
      command.getAttributes().put(WORKING_DIRECTORY_ATTRIBUTE, commandAction.getWorkdir());
    }

    command.getAttributes().put(Constants.TOOL_NAME_COMMAND_ATTRIBUTE, commandAction.getTool());

    command.getAttributes().putAll(devCommand.getAttributes());

    return command;
  }
}
