/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import static org.eclipse.che.api.core.model.workspace.config.Command.WORKING_DIRECTORY_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.EXEC_ACTION_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
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
    org.eclipse.che.api.workspace.server.model.impl.CommandImpl workspaceCommand =
        new org.eclipse.che.api.workspace.server.model.impl.CommandImpl(
            "build", "mvn clean install", "custom");
    workspaceCommand.getAttributes().put(COMPONENT_ALIAS_COMMAND_ATTRIBUTE, "dockerimageComponent");
    workspaceCommand.getAttributes().put(WORKING_DIRECTORY_ATTRIBUTE, "/tmp");
    workspaceCommand.getAttributes().put("anotherAttribute", "value");

    // when
    CommandImpl devfileCommand = commandConverter.toDevfileCommand(workspaceCommand);

    // then
    assertEquals(devfileCommand.getName(), "build");
    assertEquals(devfileCommand.getActions().size(), 1);
    assertEquals(devfileCommand.getAttributes().size(), 1);
    assertEquals(devfileCommand.getAttributes().get("anotherAttribute"), "value");
    ActionImpl action = devfileCommand.getActions().get(0);
    assertEquals(action.getComponent(), "dockerimageComponent");
    assertEquals(action.getWorkdir(), "/tmp");
    assertEquals(action.getCommand(), "mvn clean install");
    assertEquals(action.getType(), EXEC_ACTION_TYPE);
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Command `build` has no specified component where it should be run")
  public void shouldThrowAnExceptionIfWorkspaceCommandDoesNotHaveComponentNameAttribute()
      throws Exception {
    // given
    org.eclipse.che.api.workspace.server.model.impl.CommandImpl workspaceCommand =
        new org.eclipse.che.api.workspace.server.model.impl.CommandImpl(
            "build", "mvn clean install", "custom");

    // when
    commandConverter.toDevfileCommand(workspaceCommand);
  }

  @Test
  public void shouldConvertDevfileCommandToWorkspaceCommands() throws Exception {
    // given
    CommandImpl devfileCommand = new CommandImpl();
    devfileCommand.setName("build");
    devfileCommand.setAttributes(ImmutableMap.of("attr", "value"));
    ActionImpl action = new ActionImpl();
    action.setComponent("dockerimageComponent");
    action.setType(EXEC_ACTION_TYPE);
    action.setWorkdir("/tmp");
    action.setCommand("mvn clean install");
    devfileCommand.getActions().add(action);

    // when
    org.eclipse.che.api.workspace.server.model.impl.CommandImpl workspaceCommand =
        commandConverter.toWorkspaceCommand(devfileCommand, null);

    // then
    assertEquals(workspaceCommand.getName(), "build");
    assertEquals(workspaceCommand.getType(), EXEC_ACTION_TYPE);
    assertEquals(workspaceCommand.getAttributes().size(), 3);
    assertEquals(workspaceCommand.getAttributes().get("attr"), "value");
    assertEquals(workspaceCommand.getAttributes().get(WORKING_DIRECTORY_ATTRIBUTE), "/tmp");
    assertEquals(
        workspaceCommand.getAttributes().get(COMPONENT_ALIAS_COMMAND_ATTRIBUTE),
        "dockerimageComponent");
    assertEquals(workspaceCommand.getCommandLine(), "mvn clean install");
  }

  @Test
  public void
      shouldNotSetWorkingDirAttributeIfItIsMissingInDevfileCommandDuringConvertingDevfileCommandToWorkspaceCommands()
          throws Exception {
    // given
    CommandImpl devfileCommand = new CommandImpl();
    ActionImpl action = new ActionImpl();
    action.setWorkdir(null);
    devfileCommand.getActions().add(action);
    devfileCommand.setAttributes(new HashMap<>());

    // when
    org.eclipse.che.api.workspace.server.model.impl.CommandImpl workspaceCommand =
        commandConverter.toWorkspaceCommand(devfileCommand, null);

    // then
    assertFalse(workspaceCommand.getAttributes().containsKey(WORKING_DIRECTORY_ATTRIBUTE));
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Command `build` MUST has one and only one action")
  public void shouldThrowAnExceptionIfDevfileCommandHasMultipleActions() throws Exception {
    // given
    CommandImpl devfileCommand = new CommandImpl();
    devfileCommand.setName("build");
    devfileCommand.setAttributes(ImmutableMap.of("attr", "value"));
    devfileCommand.getActions().add(new ActionImpl());
    devfileCommand.getActions().add(new ActionImpl());

    // when
    commandConverter.toWorkspaceCommand(devfileCommand, null);
  }

  @Test
  public void shouldAcceptActionWithCommand() throws Exception {
    // given
    CommandImpl devfileCommand = new CommandImpl();
    devfileCommand.setName("build");

    ActionImpl action = new ActionImpl();
    action.setType("exec");
    action.setCommand("blah");

    devfileCommand.getActions().add(action);

    // when
    Command command = commandConverter.toWorkspaceCommand(devfileCommand, null);

    // then
    assertEquals(command.getCommandLine(), "blah");
  }

  @Test
  public void shouldAcceptActionWithReference() throws Exception {
    // given
    CommandImpl devfileCommand = new CommandImpl();
    devfileCommand.setName("build");

    ActionImpl action = new ActionImpl();
    action.setType("exec");
    action.setReference("blah");

    devfileCommand.getActions().add(action);

    // when
    Command command = commandConverter.toWorkspaceCommand(devfileCommand, fileURL -> "content");

    // then
    assertNull(command.getCommandLine());
    assertEquals("blah", command.getAttributes().get(Command.COMMAND_ACTION_REFERENCE_ATTRIBUTE));
    assertEquals(
        "content", command.getAttributes().get(Command.COMMAND_ACTION_REFERENCE_CONTENT_ATTRIBUTE));
  }

  @Test
  public void shouldAcceptActionWithReferenceContent() throws Exception {
    // given
    CommandImpl devfileCommand = new CommandImpl();
    devfileCommand.setName("build");

    ActionImpl action = new ActionImpl();
    action.setType("exec");
    action.setReference("blah");
    action.setReferenceContent("content");

    devfileCommand.getActions().add(action);

    // when
    Command command = commandConverter.toWorkspaceCommand(devfileCommand, null);

    // then
    assertNull(command.getCommandLine());
    assertEquals("blah", command.getAttributes().get(Command.COMMAND_ACTION_REFERENCE_ATTRIBUTE));
    assertEquals(
        "content", command.getAttributes().get(Command.COMMAND_ACTION_REFERENCE_CONTENT_ATTRIBUTE));
  }
}
