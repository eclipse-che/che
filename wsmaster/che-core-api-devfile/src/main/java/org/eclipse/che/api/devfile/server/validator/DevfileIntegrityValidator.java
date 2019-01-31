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
package org.eclipse.che.api.devfile.server.validator;

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PLUGIN_TOOL_TYPE;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.inject.Singleton;
import org.eclipse.che.api.devfile.model.Action;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Project;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.DevfileFormatException;

/** Validates devfile logical integrity. */
@Singleton
public class DevfileIntegrityValidator {

  /**
   * Checks than name may contain only letters, digits, symbols _.- and does not starts with
   * non-word character.
   */
  private static final Pattern PROJECT_NAME_PATTERN = Pattern.compile("^[\\w\\d]+[\\w\\d_.-]*$");

  /**
   * Performs the following checks:
   *
   * <pre>
   * <ul>
   *   <li>All listed items (projects, tools, commands) have unique names</li>
   *   <li>There is only one tool of type cheEditor</li>
   *   <li>All tools exists which are referenced/required by command actions</li>
   *   <li>Project names conforms naming rules</li>
   * </ul>
   * </pre>
   *
   * @param devfile input devfile
   * @throws DevfileFormatException if some of the checks is failed
   */
  public void validateDevfile(Devfile devfile) throws DevfileFormatException {
    validateProjects(devfile);
    Set<String> toolNames = validateTools(devfile);
    validateCommands(devfile, toolNames);
  }

  private Set<String> validateTools(Devfile devfile) throws DevfileFormatException {
    Set<String> existingNames = new HashSet<>();
    Tool editorTool = null;
    Tool recipeTool = null;
    for (Tool tool : devfile.getTools()) {
      if (!existingNames.add(tool.getName())) {
        throw new DevfileFormatException(format("Duplicate tool name found:'%s'", tool.getName()));
      }
      switch (tool.getType()) {
        case EDITOR_TOOL_TYPE:
          if (editorTool != null) {
            throw new DevfileFormatException(
                format(
                    "Multiple editor tools found: '%s', '%s'",
                    editorTool.getName(), tool.getName()));
          }
          checkFieldNotSet(tool, "local", tool.getLocal());
          checkFieldNotSet(tool, "selector", tool.getSelector());
          editorTool = tool;
          break;
        case PLUGIN_TOOL_TYPE:
          checkFieldNotSet(tool, "local", tool.getLocal());
          checkFieldNotSet(tool, "selector", tool.getSelector());
          break;
        case KUBERNETES_TOOL_TYPE:
        case OPENSHIFT_TOOL_TYPE:
          if (recipeTool != null) {
            throw new DevfileFormatException(
                format(
                    "Multiple non plugin or editor type tools found: '%s', '%s'",
                    recipeTool.getName(), tool.getName()));
          }
          checkFieldNotSet(tool, "id", tool.getId());
          recipeTool = tool;
          break;
        default:
          throw new DevfileFormatException(
              format("Unsupported tool '%s' type provided:'%s'", tool.getName(), tool.getType()));
      }
    }
    return existingNames;
  }

  private void checkFieldNotSet(Tool tool, String fieldName, Object fieldValue)
      throws DevfileFormatException {
    if (fieldValue == null) {
      return;
    }
    throw new DevfileFormatException(
        format(
            "Tool of type '%s' cannot contain '%s' field, please check '%s' tool",
            tool.getType(), fieldName, tool.getName()));
  }

  private void validateCommands(Devfile devfile, Set<String> toolNames)
      throws DevfileFormatException {
    Set<String> existingNames = new HashSet<>();
    for (Command command : devfile.getCommands()) {
      if (!existingNames.add(command.getName())) {
        throw new DevfileFormatException(
            format("Duplicate command name found:'%s'", command.getName()));
      }

      if (command.getActions().isEmpty()) {
        throw new DevfileFormatException(
            format("Command '%s' does not have actions.", command.getName()));
      }

      // It is temporary restriction while exec plugin is not able to handle multiple commands
      // in different containers as one Task. Later supporting of multiple actions in one command
      // may be implemented.
      if (command.getActions().size() > 1) {
        throw new DevfileFormatException(
            format("Multiple actions in command '%s' are not supported yet.", command.getName()));
      }
      Action action = command.getActions().get(0);

      if (!toolNames.contains(action.getTool())) {
        throw new DevfileFormatException(
            format(
                "Command '%s' has action that refers to non-existing tools '%s'",
                command.getName(), action.getTool()));
      }

    }
  }

  private void validateProjects(Devfile devfile) throws DevfileFormatException {
    Set<String> existingNames = new HashSet<>();
    for (Project project : devfile.getProjects()) {
      if (!existingNames.add(project.getName())) {
        throw new DevfileFormatException(
            format("Duplicate project name found:'%s'", project.getName()));
      }
      if (!PROJECT_NAME_PATTERN.matcher(project.getName()).matches()) {
        throw new DevfileFormatException(
            format(
                "Invalid project name found:'%s'. Name must contain only Latin letters,"
                    + "digits or these following special characters ._-",
                project.getName()));
      }
    }
  }
}
