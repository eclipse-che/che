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

import static org.eclipse.che.api.core.model.workspace.config.Command.WORKING_DIRECTORY_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.EXEC_ACTION_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.TOOL_NAME_COMMAND_ATTRIBUTE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class CommandConverterTest {

  private CommandConverter commandConverter;

  @BeforeMethod
  public void setUp() {
    commandConverter = new CommandConverter();
  }

  @Test
  public void shouldConvertWorkspaceCommandToDevfileCommand() throws Exception {
    // given
    CommandImpl workspaceCommand = new CommandImpl("build", "mvn clean install", "custom");
    workspaceCommand.getAttributes().put(TOOL_NAME_COMMAND_ATTRIBUTE, "dockerimageTool");
    workspaceCommand.getAttributes().put(WORKING_DIRECTORY_ATTRIBUTE, "/tmp");
    workspaceCommand.getAttributes().put("anotherAttribute", "value");

    // when
    Command devfileCommand = commandConverter.toDevfileCommand(workspaceCommand);

    // then
    assertEquals(devfileCommand.getName(), "build");
    assertEquals(devfileCommand.getActions().size(), 1);
    assertEquals(devfileCommand.getAttributes().size(), 1);
    assertEquals(devfileCommand.getAttributes().get("anotherAttribute"), "value");
    Action action = devfileCommand.getActions().get(0);
    assertEquals(action.getTool(), "dockerimageTool");
    assertEquals(action.getWorkdir(), "/tmp");
    assertEquals(action.getCommand(), "mvn clean install");
    assertEquals(action.getType(), EXEC_ACTION_TYPE);
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Command `build` has no specified tool where it should be run")
  public void shouldThrowAnExceptionIfWorkspaceCommandDoesNotHaveToolNameAttribute()
      throws Exception {
    // given
    CommandImpl workspaceCommand = new CommandImpl("build", "mvn clean install", "custom");

    // when
    commandConverter.toDevfileCommand(workspaceCommand);
  }

  @Test
  public void shouldConvertDevfileCommandToWorkspaceCommands() throws Exception {
    // given
    Command devfileCommand = new Command();
    devfileCommand.setName("build");
    devfileCommand.setAttributes(ImmutableMap.of("attr", "value"));
    Action action = new Action();
    action.setTool("dockerimageTool");
    action.setType(EXEC_ACTION_TYPE);
    action.setWorkdir("/tmp");
    action.setCommand("mvn clean install");
    devfileCommand.getActions().add(action);

    // when
    CommandImpl workspaceCommand = commandConverter.toWorkspaceCommand(devfileCommand);

    // then
    assertEquals(workspaceCommand.getName(), "build");
    assertEquals(workspaceCommand.getType(), EXEC_ACTION_TYPE);
    assertEquals(workspaceCommand.getAttributes().size(), 3);
    assertEquals(workspaceCommand.getAttributes().get("attr"), "value");
    assertEquals(workspaceCommand.getAttributes().get(WORKING_DIRECTORY_ATTRIBUTE), "/tmp");
    assertEquals(
        workspaceCommand.getAttributes().get(TOOL_NAME_COMMAND_ATTRIBUTE), "dockerimageTool");
    assertEquals(workspaceCommand.getCommandLine(), "mvn clean install");
  }

  @Test
  public void
      shouldNotSetWorkingDirAttributeIfItIsMissingInDevfileCommandDuringConvertingDevfileCommandToWorkspaceCommands()
          throws Exception {
    // given
    Command devfileCommand = new Command();
    Action action = new Action();
    action.setWorkdir(null);
    devfileCommand.getActions().add(action);
    devfileCommand.setAttributes(new HashMap<>());

    // when
    CommandImpl workspaceCommand = commandConverter.toWorkspaceCommand(devfileCommand);

    // then
    assertFalse(workspaceCommand.getAttributes().containsKey(WORKING_DIRECTORY_ATTRIBUTE));
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Command `build` MUST has one and only one action")
  public void shouldThrowAnExceptionIfDevfileCommandHasMultipleActions() throws Exception {
    // given
    Command devfileCommand = new Command();
    devfileCommand.setName("build");
    devfileCommand.setAttributes(ImmutableMap.of("attr", "value"));
    devfileCommand.getActions().add(new Action());
    devfileCommand.getActions().add(new Action());

    // when
    commandConverter.toWorkspaceCommand(devfileCommand);
  }
}
