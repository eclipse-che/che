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
package org.eclipse.che.ide.extension.machine.client.command.producer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_CONTEXT_MENU;

/**
 * Manages actions for contextual commands.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CommandProducerActionManager implements MachineStateEvent.Handler, WsAgentStateHandler {

    private final ActionManager                actionManager;
    private final CommandProducerActionFactory commandProducerActionFactory;
    private final AppContext                   appContext;
    private final MachineServiceClient         machineServiceClient;

    private final List<Machine>                            machines;
    private final Set<CommandProducer>                     commandProducers;
    private final Map<Action, DefaultActionGroup>          actions2ActionGroups;
    private final Map<Machine, List<Action>>               actionsByMachines;
    private final Map<CommandProducer, DefaultActionGroup> producers2ActionGroups;

    private DefaultActionGroup commandProducersActionsGroup;

    @Inject
    public CommandProducerActionManager(EventBus eventBus,
                                        ActionManager actionManager,
                                        CommandProducerActionFactory commandProducerActionFactory,
                                        AppContext appContext,
                                        MachineServiceClient machineServiceClient) {
        this.actionManager = actionManager;
        this.commandProducerActionFactory = commandProducerActionFactory;
        this.appContext = appContext;
        this.machineServiceClient = machineServiceClient;

        machines = new ArrayList<>();
        commandProducers = new HashSet<>();
        actions2ActionGroups = new HashMap<>();
        actionsByMachines = new HashMap<>();
        producers2ActionGroups = new HashMap<>();

        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @Inject(optional = true)
    private void start(Set<CommandProducer> commandProducers) {
        this.commandProducers.addAll(commandProducers);

        commandProducersActionsGroup = new DefaultActionGroup(actionManager);
        actionManager.registerAction("commandProducersActionsGroup", commandProducersActionsGroup);

        DefaultActionGroup mainContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_CONTEXT_MENU);
        mainContextMenu.add(commandProducersActionsGroup);

        DefaultActionGroup editorTabContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_EDITOR_TAB_CONTEXT_MENU);
        editorTabContextMenu.add(commandProducersActionsGroup);

        fetchMachines();
    }

    private void fetchMachines() {
        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> arg) throws OperationException {
                machines.addAll(arg);
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

    private void createActionsForProducer(CommandProducer producer) {
        Action action;

        if (producer.getMachineTypes().isEmpty()) {
            action = commandProducerActionFactory.create(producer.getName(), producer, appContext.getDevMachine().getDescriptor());

            actionManager.registerAction(producer.getName(), action);
        } else {
            action = new DefaultActionGroup(producer.getName(), true, actionManager);

            producers2ActionGroups.put(producer, (DefaultActionGroup)action);

            actionManager.registerAction(producer.getName(), action);

            for (Machine machine : machines) {
                createActionsForMachine(machine);
            }
        }

        commandProducersActionsGroup.add(action);
    }

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

                DefaultActionGroup actionGroup = producers2ActionGroups.get(commandProducer);
                if (actionGroup != null) {
                    actionGroup.add(machineAction);

                    actions2ActionGroups.put(machineAction, actionGroup);
                }
            }
        }
    }

    private void removeActionsForMachine(Machine machine) {
        List<Action> actions = actionsByMachines.remove(machine);
        if (actions != null) {
            for (Action action : actions) {
                DefaultActionGroup actionGroup = actions2ActionGroups.remove(action);
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
}
