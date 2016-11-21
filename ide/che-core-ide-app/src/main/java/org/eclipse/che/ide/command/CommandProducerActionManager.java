/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.command;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandProducer;
import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_TOOLBAR;

/**
 * Manages actions for the contextual commands.
 * <p>Manager gets all registered {@link CommandProducer}s and creates related actions in context menus.
 * <p>Manager listens all machines's state (running/destroyed) in order to
 * create/remove actions for the related {@link CommandProducer}s in case
 * they are applicable only for the certain machine types.
 *
 * @author Artem Zatsarynnyi
 * @see CommandProducer
 */
@Singleton
public class CommandProducerActionManager implements MachineStateEvent.Handler, WsAgentStateHandler, Component {

    private final ActionManager                actionManager;
    private final CommandProducerActionFactory commandProducerActionFactory;
    private final AppContext                   appContext;
    private final MachineServiceClient         machineServiceClient;
    private final Resources                    resources;

    private final List<Machine>                            machines;
    private final Set<CommandProducer>                     commandProducers;
    private final Map<Action, DefaultActionGroup>          actionsToActionGroups;
    private final Map<Machine, List<Action>>               actionsByMachines;
    private final Map<CommandProducer, DefaultActionGroup> producersToActionGroups;

    private DefaultActionGroup commandActionsPopUpGroup;

    @Inject
    public CommandProducerActionManager(EventBus eventBus,
                                        ActionManager actionManager,
                                        CommandProducerActionFactory commandProducerActionFactory,
                                        AppContext appContext,
                                        MachineServiceClient machineServiceClient,
                                        Resources resources) {
        this.actionManager = actionManager;
        this.commandProducerActionFactory = commandProducerActionFactory;
        this.appContext = appContext;
        this.machineServiceClient = machineServiceClient;
        this.resources = resources;

        machines = new ArrayList<>();
        commandProducers = new HashSet<>();
        actionsToActionGroups = new HashMap<>();
        actionsByMachines = new HashMap<>();
        producersToActionGroups = new HashMap<>();

        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @Inject(optional = true)
    private void start(Set<CommandProducer> commandProducers) {
        this.commandProducers.addAll(commandProducers);

        commandActionsPopUpGroup = new DefaultActionGroup("Commands", true, actionManager);
        actionManager.registerAction("commandActionsPopUpGroup", commandActionsPopUpGroup);
        commandActionsPopUpGroup.getTemplatePresentation().setSVGResource(resources.execute());
        commandActionsPopUpGroup.getTemplatePresentation().setDescription("Execute command");

        DefaultActionGroup mainContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
        mainContextMenu.add(commandActionsPopUpGroup);

        DefaultActionGroup editorTabContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_EDITOR_TAB_CONTEXT_MENU);
        editorTabContextMenu.add(commandActionsPopUpGroup);

        // add 'Commands' pop-up group to the main toolbar
        DefaultActionGroup commandActionsToolbarGroup = new CommandActionsToolbarGroup(actionManager);
        commandActionsToolbarGroup.add(commandActionsPopUpGroup);
        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_TOOLBAR);
        mainToolbarGroup.add(commandActionsToolbarGroup);
    }

    @Override
    public void start(final Callback<Component, Exception> callback) {
        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> arg) throws OperationException {
                machines.addAll(arg);

                callback.onSuccess(CommandProducerActionManager.this);
            }
        });
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        machines.add(event.getMachine());

        createActionsForMachine(event.getMachine());
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        machines.remove(event.getMachine());

        removeActionsForMachine(event.getMachine());
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        for (CommandProducer commandProducer : commandProducers) {
            createActionsForProducer(commandProducer);
        }
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    /** Creates actions for the given {@link CommandProducer}. */
    private void createActionsForProducer(CommandProducer producer) {
        Action action;

        if (producer.getMachineTypes().isEmpty()) {
            action = commandProducerActionFactory.create(producer.getName(), producer, appContext.getDevMachine().getDescriptor());

            actionManager.registerAction(producer.getName(), action);
        } else {
            action = new DefaultActionGroup(producer.getName(), true, actionManager);

            producersToActionGroups.put(producer, (DefaultActionGroup)action);

            actionManager.registerAction(producer.getName(), action);

            for (Machine machine : machines) {
                createActionsForMachine(machine);
            }
        }

        commandActionsPopUpGroup.add(action);
    }

    /**
     * Creates actions for that {@link CommandProducer}s
     * which are applicable for the given machine's type.
     */
    private void createActionsForMachine(Machine machine) {
        for (CommandProducer commandProducer : commandProducers) {
            if (commandProducer.getMachineTypes().contains(machine.getConfig().getType())) {
                CommandProducerAction machineAction = commandProducerActionFactory.create(machine.getConfig().getName(),
                                                                                          commandProducer,
                                                                                          machine);
                List<Action> actionList = actionsByMachines.get(machine);
                if (actionList == null) {
                    actionList = new ArrayList<>();
                    actionsByMachines.put(machine, actionList);
                }
                actionList.add(machineAction);

                actionManager.registerAction(machine.getConfig().getName(), machineAction);

                DefaultActionGroup actionGroup = producersToActionGroups.get(commandProducer);
                if (actionGroup != null) {
                    actionGroup.add(machineAction);

                    actionsToActionGroups.put(machineAction, actionGroup);
                }
            }
        }
    }

    private void removeActionsForMachine(Machine machine) {
        List<Action> actions = actionsByMachines.remove(machine);
        if (actions != null) {
            for (Action action : actions) {
                DefaultActionGroup actionGroup = actionsToActionGroups.remove(action);
                if (actionGroup != null) {
                    actionGroup.remove(action);

                    String id = actionManager.getId(action);
                    if (id != null) {
                        actionManager.unregisterAction(id);
                    }
                }
            }
        }
    }

    /**
     * Action group for placing {@link CommandProducerAction}s on the toolbar.
     * It's visible when at least one {@link CommandProducerAction} exists.
     */
    private class CommandActionsToolbarGroup extends DefaultActionGroup {

        CommandActionsToolbarGroup(ActionManager actionManager) {
            super(actionManager);
        }

        @Override
        public void update(ActionEvent e) {
            e.getPresentation().setEnabledAndVisible(commandActionsPopUpGroup.getChildrenCount() != 0);
        }
    }
}
