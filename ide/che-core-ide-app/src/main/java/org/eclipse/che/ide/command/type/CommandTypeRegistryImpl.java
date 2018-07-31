/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.type;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Implementation of {@link CommandTypeRegistry}.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandTypeRegistryImpl implements CommandTypeRegistry {

  private final CommandTypeMessages messages;
  private final Map<String, CommandType> commandTypes;

  @Inject
  public CommandTypeRegistryImpl(CommandTypeMessages messages) {
    this.messages = messages;
    this.commandTypes = new HashMap<>();
  }

  @Inject(optional = true)
  private void register(Set<CommandType> commandTypes) {
    for (CommandType type : commandTypes) {
      final String id = type.getId();

      if (this.commandTypes.containsKey(id)) {
        Log.warn(getClass(), messages.typeRegistryMessageAlreadyRegistered(id));
      } else {
        this.commandTypes.put(id, type);
      }
    }
  }

  @Override
  public Optional<CommandType> getCommandTypeById(String id) {
    return Optional.ofNullable(commandTypes.get(id));
  }

  @Override
  public Set<CommandType> getCommandTypes() {
    return new HashSet<>(commandTypes.values());
  }
}
