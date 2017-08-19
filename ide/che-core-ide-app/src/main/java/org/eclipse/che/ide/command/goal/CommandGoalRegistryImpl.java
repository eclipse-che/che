/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.goal;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.command.BaseCommandGoal;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.util.loging.Log;

/** Implementation of {@link CommandGoalRegistry}. */
@Singleton
public class CommandGoalRegistryImpl implements CommandGoalRegistry {

  private final CommandGoal defaultGoal;
  private final Provider<CommandManager> commandManagerProvider;
  private final GoalMessages messages;
  private final Map<String, CommandGoal> predefinedGoals;

  @Inject
  public CommandGoalRegistryImpl(
      @Named("default") CommandGoal defaultCommandGoal,
      Provider<CommandManager> commandManagerProvider,
      GoalMessages messages) {
    defaultGoal = defaultCommandGoal;
    this.commandManagerProvider = commandManagerProvider;
    this.messages = messages;

    predefinedGoals = new HashMap<>();
  }

  @Inject(optional = true)
  private void register(Set<CommandGoal> goals) {
    for (CommandGoal goal : goals) {
      final String id = goal.getId();

      if (!predefinedGoals.containsKey(id)) {
        predefinedGoals.put(id, goal);
      } else {
        Log.warn(getClass(), messages.messageGoalAlreadyRegistered(id));
      }
    }
  }

  @Override
  public Set<CommandGoal> getAllGoals() {
    Set<CommandGoal> goals = getAllPredefinedGoals();
    goals.addAll(
        commandManagerProvider
            .get()
            .getCommands()
            .stream()
            .map(command -> getGoalForId(command.getGoal()))
            .collect(toSet()));
    return goals;
  }

  @Override
  public Set<CommandGoal> getAllPredefinedGoals() {
    return new HashSet<>(predefinedGoals.values());
  }

  @Override
  public CommandGoal getDefaultGoal() {
    return defaultGoal;
  }

  @Override
  public Optional<CommandGoal> getPredefinedGoalById(String id) {
    return ofNullable(predefinedGoals.get(id));
  }

  @Override
  public CommandGoal getGoalForId(@Nullable String id) {
    if (isNullOrEmpty(id)) {
      return getDefaultGoal();
    }

    return getPredefinedGoalById(id).orElse(new BaseCommandGoal(id));
  }
}
