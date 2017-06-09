/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command.execute;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.command.CommandAddedEvent;
import org.eclipse.che.ide.api.command.CommandGoalRegistry;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandRemovedEvent;
import org.eclipse.che.ide.api.command.CommandUpdatedEvent;
import org.eclipse.che.ide.api.component.WsAgentComponent;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;

/**
 * Manager listens for creating/removing commands and adds/removes
 * related {@link ExecuteCommandAction}s in the context menus.
 */
@Singleton
public class ExecuteCommandActionManager implements WsAgentComponent {

    private static final String COMMANDS_ACTION_GROUP_ID_PREFIX = "commandsActionGroup";
    private static final String COMMAND_ACTION_ID_PREFIX        = "command_";
    private static final String GOAL_ACTION_GROUP_ID_PREFIX     = "goal_";

    private final CommandManager              commandManager;
    private final ActionManager               actionManager;
    private final CommandsActionGroup         commandsActionGroup;
    private final GoalPopUpGroupFactory       goalPopUpGroupFactory;
    private final ExecuteCommandActionFactory commandActionFactory;
    private final CommandGoalRegistry         goalRegistry;
    private final EventBus                    eventBus;

    /** Map of command's name to an appropriate {@link ExecuteCommandAction}. */
    private final Map<String, Action>             commandActions;
    /** Map of command goal's ID to an appropriate action group. */
    private final Map<String, DefaultActionGroup> goalPopUpGroups;

    @Inject
    public ExecuteCommandActionManager(CommandManager commandManager,
                                       ActionManager actionManager,
                                       CommandsActionGroup commandsActionGroup,
                                       GoalPopUpGroupFactory goalPopUpGroupFactory,
                                       ExecuteCommandActionFactory commandActionFactory,
                                       CommandGoalRegistry goalRegistry,
                                       EventBus eventBus) {
        this.commandManager = commandManager;
        this.actionManager = actionManager;
        this.commandsActionGroup = commandsActionGroup;
        this.goalPopUpGroupFactory = goalPopUpGroupFactory;
        this.commandActionFactory = commandActionFactory;
        this.goalRegistry = goalRegistry;
        this.eventBus = eventBus;

        commandActions = new HashMap<>();
        goalPopUpGroups = new HashMap<>();
    }

    @Override
    public void start(Callback<WsAgentComponent, Exception> callback) {
        callback.onSuccess(this);

        eventBus.addHandler(CommandAddedEvent.getType(), e -> addAction(e.getCommand()));
        eventBus.addHandler(CommandRemovedEvent.getType(), e -> removeAction(e.getCommand()));
        eventBus.addHandler(CommandUpdatedEvent.getType(), e -> {
            removeAction(e.getInitialCommand());
            addAction(e.getUpdatedCommand());
        });

        actionManager.registerAction(COMMANDS_ACTION_GROUP_ID_PREFIX, commandsActionGroup);

        // inject 'Commands' menu into context menus
        ((DefaultActionGroup)actionManager.getAction(GROUP_MAIN_CONTEXT_MENU)).add(commandsActionGroup);
        ((DefaultActionGroup)actionManager.getAction(GROUP_EDITOR_TAB_CONTEXT_MENU)).add(commandsActionGroup);
        ((DefaultActionGroup)actionManager.getAction(GROUP_CONSOLES_TREE_CONTEXT_MENU)).add(commandsActionGroup);

        commandManager.getCommands().forEach(this::addAction);
    }

    /**
     * Creates action for executing the given command and
     * adds created action to the appropriate action group.
     */
    private void addAction(CommandImpl command) {
        final ExecuteCommandAction action = commandActionFactory.create(command);

        actionManager.registerAction(COMMAND_ACTION_ID_PREFIX + command.getName(), action);
        commandActions.put(command.getName(), action);

        getActionGroupForCommand(command).add(action);
    }

    /**
     * Returns the action group which is appropriate for placing the action for executing the given command.
     * If appropriate action group doesn't exist it will be created and added to the right place.
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

    /**
     * Removes action for executing the given command and
     * removes the appropriate action group in case it's empty.
     */
    private void removeAction(CommandImpl command) {
        final Action commandAction = commandActions.remove(command.getName());

        if (commandAction != null) {
            final String commandActionId = actionManager.getId(commandAction);
            if (commandActionId != null) {
                actionManager.unregisterAction(commandActionId);
            }

            // remove action from it's action group
            String goalId = command.getGoal();
            if (isNullOrEmpty(goalId)) {
                goalId = goalRegistry.getDefaultGoal().getId();
            }

            // remove action group if it's empty
            final DefaultActionGroup goalPopUpGroup = goalPopUpGroups.remove(goalId);

            if (goalPopUpGroup != null) {
                goalPopUpGroup.remove(commandAction);

                if (goalPopUpGroup.getChildrenCount() == 0) {
                    final String goalActionId = actionManager.getId(goalPopUpGroup);
                    if (goalActionId != null) {
                        actionManager.unregisterAction(goalActionId);
                    }
                    commandsActionGroup.remove(goalPopUpGroup);
                }
            }
        }
    }
}
