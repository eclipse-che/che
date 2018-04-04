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
package org.eclipse.che.ide.command.execute;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.command.CommandAddedEvent;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandRemovedEvent;
import org.eclipse.che.ide.api.command.CommandUpdatedEvent;
import org.eclipse.che.ide.api.command.CommandsLoadedEvent;
import org.eclipse.che.ide.api.workspace.event.WsAgentServerStoppedEvent;

/**
 * Manager listens for creating/removing commands and adds/removes related {@link
 * ExecuteCommandAction}s in the context menus.
 */
@Singleton
public class ExecuteCommandActionManager {

  private static final String COMMANDS_ACTION_GROUP_ID_PREFIX = "commandsActionGroup";
  private static final String COMMAND_ACTION_ID_PREFIX = "command_";
  private static final String GOAL_ACTION_GROUP_ID_PREFIX = "goal_";

  private final Provider<CommandManager> commandManagerProvider;
  private final ActionManager actionManager;
  private final CommandsActionGroup commandsActionGroup;
  private final GoalPopUpGroupFactory goalPopUpGroupFactory;
  private final ExecuteCommandActionFactory commandActionFactory;
  private final CommandGoalRegistry goalRegistry;

  /** Map of command's name to an appropriate {@link ExecuteCommandAction}. */
  private final Map<String, Action> commandActions;
  /** Map of command goal's ID to an appropriate action group. */
  private final Map<String, DefaultActionGroup> goalPopUpGroups;

  @Inject
  public ExecuteCommandActionManager(
      Provider<CommandManager> commandManagerProvider,
      ActionManager actionManager,
      CommandsActionGroup commandsActionGroup,
      GoalPopUpGroupFactory goalPopUpGroupFactory,
      ExecuteCommandActionFactory commandActionFactory,
      CommandGoalRegistry goalRegistry,
      EventBus eventBus) {
    this.commandManagerProvider = commandManagerProvider;
    this.actionManager = actionManager;
    this.commandsActionGroup = commandsActionGroup;
    this.goalPopUpGroupFactory = goalPopUpGroupFactory;
    this.commandActionFactory = commandActionFactory;
    this.goalRegistry = goalRegistry;

    commandActions = new HashMap<>();
    goalPopUpGroups = new HashMap<>();

    initialize();

    eventBus.addHandler(CommandAddedEvent.getType(), e -> addAction(e.getCommand()));
    eventBus.addHandler(CommandRemovedEvent.getType(), e -> removeAction(e.getCommand()));
    eventBus.addHandler(
        CommandUpdatedEvent.getType(),
        e -> {
          removeAction(e.getInitialCommand());
          addAction(e.getUpdatedCommand());
        });

    eventBus.addHandler(WsAgentServerStoppedEvent.TYPE, e -> disposeActions());

    eventBus.addHandler(
        CommandsLoadedEvent.getType(),
        e -> {
          disposeActions();
          registerActions();
        });
  }

  private void initialize() {
    actionManager.registerAction(COMMANDS_ACTION_GROUP_ID_PREFIX, commandsActionGroup);

    // inject 'Commands' menu into context menus
    ((DefaultActionGroup) actionManager.getAction(GROUP_MAIN_CONTEXT_MENU))
        .add(commandsActionGroup);
    ((DefaultActionGroup) actionManager.getAction(GROUP_EDITOR_TAB_CONTEXT_MENU))
        .add(commandsActionGroup);
    ((DefaultActionGroup) actionManager.getAction(GROUP_CONSOLES_TREE_CONTEXT_MENU))
        .add(commandsActionGroup);
  }

  /**
   * Fetch registered action from command manager and constructs actions which should be registered
   * in action manager and context menus.
   */
  private void registerActions() {
    commandManagerProvider.get().getCommands().forEach(ExecuteCommandActionManager.this::addAction);
  }

  /**
   * Packet dispose of registered actions and remove all action groups. This action need to be
   * called for example when workspace is stopped.
   */
  private void disposeActions() {
    commandActions.values().forEach(ExecuteCommandActionManager.this::removeAction);
    goalPopUpGroups.values().forEach(ExecuteCommandActionManager.this::removeAction);

    commandActions.clear();
    goalPopUpGroups.clear();
  }

  /**
   * Creates action for executing the given command and adds created action to the appropriate
   * action group.
   */
  private void addAction(CommandImpl command) {
    ExecuteCommandAction action = commandActionFactory.create(command);

    actionManager.registerAction(COMMAND_ACTION_ID_PREFIX + command.getName(), action);
    commandActions.put(command.getName(), action);

    getActionGroupForCommand(command).add(action);
  }

  /**
   * Returns the action group which is appropriate for placing the action for executing the given
   * command. If appropriate action group doesn't exist it will be created and added to the right
   * place.
   */
  private DefaultActionGroup getActionGroupForCommand(CommandImpl command) {
    String goalId = command.getGoal();
    if (isNullOrEmpty(goalId)) {
      goalId = goalRegistry.getDefaultGoal().getId();
    }

    DefaultActionGroup commandGoalPopUpGroup = goalPopUpGroups.get(goalId);

    if (commandGoalPopUpGroup == null) {
      commandGoalPopUpGroup = goalPopUpGroupFactory.create(goalId);
      actionManager.registerAction(GOAL_ACTION_GROUP_ID_PREFIX + goalId, commandGoalPopUpGroup);
      goalPopUpGroups.put(goalId, commandGoalPopUpGroup);

      commandsActionGroup.add(commandGoalPopUpGroup);
    }

    return commandGoalPopUpGroup;
  }

  /** Removes actual action and dispose it from the action manager. */
  private void removeAction(Action commandAction) {
    String commandActionId = actionManager.getId(commandAction);

    if (commandActionId != null) {
      if (actionManager.isGroup(commandActionId)) {
        commandsActionGroup.remove(commandAction);
      }

      actionManager.unregisterAction(commandActionId);
    }
  }

  /**
   * Removes action for executing the given command and removes the appropriate action group in case
   * it's empty.
   */
  private void removeAction(CommandImpl command) {
    Action commandAction = commandActions.remove(command.getName());

    if (commandAction != null) {
      removeAction(commandAction);

      // remove action from it's action group
      String goalId = command.getGoal();
      if (isNullOrEmpty(goalId)) {
        goalId = goalRegistry.getDefaultGoal().getId();
      }

      // remove action group if it's empty
      DefaultActionGroup goalPopUpGroup = goalPopUpGroups.remove(goalId);

      if (goalPopUpGroup != null) {
        goalPopUpGroup.remove(commandAction);

        if (goalPopUpGroup.getChildrenCount() == 0) {
          removeAction(goalPopUpGroup);
        }
      }
    }
  }
}
