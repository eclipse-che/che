/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.manager;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toSet;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;

/** Generator for command names. */
@Singleton
class CommandNameGenerator {

  private final CommandTypeRegistry typeRegistry;
  private final Provider<CommandManager> commandManagerProvider;

  @Inject
  CommandNameGenerator(
      CommandTypeRegistry commandTypeRegistry, Provider<CommandManager> commandManagerProvider) {
    this.typeRegistry = commandTypeRegistry;
    this.commandManagerProvider = commandManagerProvider;
  }

  /**
   * Generates unique command names.
   *
   * @param commandTypeId ID of the command type which display name should be used as part of the
   *     generated name
   * @param customName desired name. If some command with {@code customName} already exists then
   *     generated name will be begin with {@code customName}
   * @return command name
   */
  String generate(String commandTypeId, @Nullable String customName) {
    final Set<String> existingNames =
        commandManagerProvider
            .get()
            .getCommands()
            .stream()
            .map(CommandImpl::getName)
            .collect(toSet());

    String newName;

    if (isNullOrEmpty(customName)) {
      Optional<CommandType> commandType = typeRegistry.getCommandTypeById(commandTypeId);
      newName = "new";

      if (commandType.isPresent()) {
        newName = newName + commandType.get().getDisplayName();
      }
    } else {
      if (!existingNames.contains(customName)) {
        return customName;
      }

      newName = customName + " copy";
    }

    if (!existingNames.contains(newName)) {
      return newName;
    }

    for (int count = 1; count < 1000; count++) {
      if (!existingNames.contains(newName + "-" + count)) {
        return newName + "-" + count;
      }
    }

    return newName;
  }
}
