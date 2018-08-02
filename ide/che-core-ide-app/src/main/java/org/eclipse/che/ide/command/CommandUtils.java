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
package org.eclipse.che.ide.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * A smattering of useful methods to work with commands.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandUtils {

  private final CommandGoalRegistry goalRegistry;
  private final CommandTypeRegistry commandTypeRegistry;
  private final IconRegistry iconRegistry;

  @Inject
  public CommandUtils(
      CommandGoalRegistry commandGoalRegistry,
      CommandTypeRegistry commandTypeRegistry,
      IconRegistry iconRegistry) {
    this.goalRegistry = commandGoalRegistry;
    this.commandTypeRegistry = commandTypeRegistry;
    this.iconRegistry = iconRegistry;
  }

  /**
   * Groups the given {@code commands} by its goal.
   *
   * @return map that contains the given {@code commands} grouped by its goal
   */
  public Map<CommandGoal, List<CommandImpl>> groupCommandsByGoal(List<CommandImpl> commands) {
    final Map<CommandGoal, List<CommandImpl>> commandsByGoal = new HashMap<>();

    for (CommandImpl command : commands) {
      final String goalId = command.getGoal();
      final CommandGoal commandGoal = goalRegistry.getGoalForId(goalId);

      commandsByGoal.computeIfAbsent(commandGoal, key -> new ArrayList<>()).add(command);
    }

    return commandsByGoal;
  }

  /** Returns the icon for the given command type ID or {@code null} if none. */
  @Nullable
  public SVGResource getCommandTypeIcon(String typeId) {
    final Optional<CommandType> commandType = commandTypeRegistry.getCommandTypeById(typeId);

    if (commandType.isPresent()) {
      final Icon icon = iconRegistry.getIconIfExist("command.type." + commandType.get().getId());

      if (icon != null) {
        final SVGImage svgImage = icon.getSVGImage();

        if (svgImage != null) {
          return icon.getSVGResource();
        }
      }
    }

    return null;
  }

  /** Returns the icon for the given command goal ID or {@code null} if none. */
  @Nullable
  public SVGResource getCommandGoalIcon(String goalId) {
    final Optional<CommandGoal> goalOptional = goalRegistry.getPredefinedGoalById(goalId);

    if (goalOptional.isPresent()) {
      final Icon icon = iconRegistry.getIconIfExist("command.goal." + goalOptional.get().getId());

      if (icon != null) {
        final SVGImage svgImage = icon.getSVGImage();

        if (svgImage != null) {
          return icon.getSVGResource();
        }
      }
    }

    return null;
  }
}
