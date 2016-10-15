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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.ui.dropdown.DropDownListFactory;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.eclipse.che.ide.extension.machine.client.MachineExtension.GROUP_COMMANDS_LIST;
import static org.eclipse.che.ide.extension.machine.client.MachineExtension.GROUP_MACHINES_LIST;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action allows user to select target machine and command to execute.
 *
 * @author Artem Zatsarynnyi
 * @author Oleksii Orel
 */
@Singleton
public class SelectCommandComboBox extends AbstractPerspectiveAction implements CustomComponentAction,
                                                                                CommandManager.CommandChangedListener,
                                                                                WsAgentStateHandler,
                                                                                MachineStateEvent.Handler,
                                                                                WorkspaceStartedEvent.Handler,
                                                                                WorkspaceStoppedEvent.Handler {

    public static final String GROUP_COMMANDS = "CommandsGroup";
    public static final String GROUP_MACHINES = "MachinesGroup";

    private final MachineLocalizationConstant locale;
    private final MachineResources            resources;
    private final Map<String, Machine>        registeredMachineMap;
    private final ActionManager               actionManager;
    private final EntityFactory               entityFactory;
    private final CommandManager              commandManager;
    private final CommandTypeRegistry         commandTypeRegistry;
    private final AppContext                  appContext;
    private final DropDownWidget              commandsListWidget;
    private final DropDownWidget              machinesListWidget;
    private final List<CommandImpl>           commands;
    private final DefaultActionGroup          commandActions;
    private final DefaultActionGroup          machinesActions;

    private boolean workspaceRunning = false;

    @Inject
    public SelectCommandComboBox(MachineLocalizationConstant locale,
                                 MachineResources resources,
                                 ActionManager actionManager,
                                 EventBus eventBus,
                                 EntityFactory entityFactory,
                                 DropDownListFactory dropDownListFactory,
                                 CommandManager commandManager,
                                 CommandTypeRegistry commandTypeRegistry,
                                 AppContext appContext) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.selectCommandControlTitle(),
              locale.selectCommandControlDescription(),
              null, null);

        this.locale = locale;
        this.resources = resources;
        this.actionManager = actionManager;
        this.commandManager = commandManager;
        this.entityFactory = entityFactory;
        this.commandTypeRegistry = commandTypeRegistry;
        this.appContext = appContext;

        this.registeredMachineMap = new HashMap<>();
        this.commands = new ArrayList<>();

        this.machinesListWidget = dropDownListFactory.createDropDown(GROUP_MACHINES);
        this.commandsListWidget = dropDownListFactory.createDropDown(GROUP_COMMANDS);

        commandManager.addCommandChangedListener(this);

        commandActions = new DefaultActionGroup(GROUP_COMMANDS, false, actionManager);
        actionManager.registerAction(GROUP_COMMANDS, commandActions);

        machinesActions = new DefaultActionGroup(GROUP_MACHINES, false, actionManager);
        actionManager.registerAction(GROUP_MACHINES, machinesActions);

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(workspaceRunning);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        // Create widgets for custom component 'select command'.
        FlowPanel customComponentHeader = new FlowPanel();
        FlowPanel devMachineIconPanel = new FlowPanel();
        FlowPanel commandIconPanel = new FlowPanel();

        customComponentHeader.setStyleName(resources.getCss().selectCommandBox());
        devMachineIconPanel.setStyleName(resources.getCss().selectCommandBoxIconPanel());
        devMachineIconPanel.add(new SVGImage(resources.devMachine()));
        customComponentHeader.add(devMachineIconPanel);
        customComponentHeader.add((Widget)machinesListWidget);
        commandIconPanel.setStyleName(resources.getCss().selectCommandBoxIconPanel());
        commandIconPanel.add(new SVGImage(resources.cmdIcon()));
        customComponentHeader.add(commandIconPanel);
        customComponentHeader.add((Widget)commandsListWidget);

        return customComponentHeader;
    }

    /** Returns selected command. */
    @Nullable
    public CommandImpl getSelectedCommand() {
        if (commands.isEmpty()) {
            return null;
        }

        final String selectedCommandName = commandsListWidget.getSelectedName();

        for (CommandImpl command : commands) {
            if (command.getName().equals(selectedCommandName)) {
                return command;
            }
        }
        return null;
    }

    public void setSelectedCommand(CommandImpl command) {
        commandsListWidget.selectElement(command.getName(), command.getName());
    }

    /** Returns command by it's name. */
    @Nullable
    public CommandImpl getCommandByName(String name) {
        if (commands.isEmpty()) {
            return null;
        }

        for (CommandImpl command : commands) {
            if (command.getName().equals(name)) {
                return command;
            }
        }
        return null;
    }

    /**
     * Load all commands to the widget.
     *
     * @param commandToSelect
     *         command that should be selected after loading all commands
     */
    private void loadCommands(@Nullable final CommandImpl commandToSelect) {
        setCommands(commandManager.getCommands(), commandToSelect);
    }

    /**
     * Sets commands to the widget.
     *
     * @param commands
     *         commands to set
     * @param commandToSelect
     *         command that should be selected or {@code null} if none
     */
    private void setCommands(List<CommandImpl> commands, @Nullable CommandImpl commandToSelect) {
        this.commands.clear();

        commandActions.removeAll();

        final DefaultActionGroup commandsList = (DefaultActionGroup)actionManager.getAction(GROUP_COMMANDS_LIST);
        if (commandsList != null) {
            commandActions.addAll(commandsList);
        }

        Collections.sort(commands, new Comparator<CommandImpl>() {
            @Override
            public int compare(CommandImpl o1, CommandImpl o2) {
                return o1.getType().compareTo(o2.getType());
            }
        });

        CommandImpl prevCommand = null;
        for (CommandImpl command : commands) {
            if (prevCommand == null || !command.getType().equals(prevCommand.getType())) {
                CommandType commandType = commandTypeRegistry.getCommandTypeById(command.getType());
                commandActions.addSeparator(commandType.getDisplayName());
            }

            commandActions.add(commandsListWidget.createAction(command.getName(), command.getName()));
            prevCommand = command;
        }

        this.commands.addAll(commands);

        if (commandToSelect != null) {
            setSelectedCommand(commandToSelect);
        } else {
            selectLastUsedCommand();
        }
    }

    /** Selects last used command. */
    private void selectLastUsedCommand() {
        if (commands.isEmpty()) {
            setEmptyCommand();
        } else {
            // for now, we always select first command
            final CommandImpl command = commands.get(0);
            commandsListWidget.selectElement(command.getName(), command.getName());
        }
    }

    @Nullable
    Machine getSelectedMachine() {
        if (machinesListWidget.getSelectedId() == null) {
            return null;
        }

        return registeredMachineMap.get(machinesListWidget.getSelectedId());
    }

    /** Clears the selected element in the 'Select Command' menu. */
    private void setEmptyCommand() {
        commandsListWidget.selectElement(null, null);
    }

    @Override
    public void onCommandAdded(CommandImpl command) {
        loadCommands(null);
    }

    @Override
    public void onCommandUpdated(CommandImpl command) {
        loadCommands(command);
    }

    @Override
    public void onCommandRemoved(CommandImpl command) {
        loadCommands(null);
    }

    /** Load all machines to the widget. */
    private void loadMachines() {
        List<MachineEntity> machines = getMachines(appContext.getWorkspace());
        if (!machines.isEmpty()) {
            addMachineActions(machines);
        }
    }

    private List<MachineEntity> getMachines(Workspace workspace) {
        WorkspaceRuntime workspaceRuntime = workspace.getRuntime();
        if (workspaceRuntime == null) {
            return emptyList();
        }

        List<? extends Machine> runtimeMachines = workspaceRuntime.getMachines();
        List<MachineEntity> machines = new ArrayList<>(runtimeMachines.size());
        for (Machine machine : runtimeMachines) {
            if (machine instanceof MachineDto) {
                MachineEntity machineEntity = entityFactory.createMachine((MachineDto)machine);
                machines.add(machineEntity);
            }

        }
        return machines;
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        workspaceRunning = true;
        loadCommands(null);
        loadMachines();
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        Machine machine = event.getMachine();

        addMachineAction(machine);
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        Machine machine = event.getMachine();

        final String machineId = machine.getId();

        if (registeredMachineMap.remove(machineId) == null) {
            return;
        }

        if (machine.getConfig().getName().equals(machinesListWidget.getSelectedName())) {
            machinesListWidget.selectElement(null, null);
        }

        updateMachineActions();
    }

    private void addMachineActions(List<MachineEntity> machines) {
        for (MachineEntity machine : machines) {
            registeredMachineMap.put(machine.getId(), machine);
        }

        updateMachineActions();
    }

    private void addMachineAction(Machine machine) {
        registeredMachineMap.put(machine.getId(), machine);
        updateMachineActions();

        machinesListWidget.selectElement(machine.getId(), machine.getConfig().getName());
    }

    private void updateMachineActions() {
        machinesActions.removeAll();
        final DefaultActionGroup actionList = (DefaultActionGroup)actionManager.getAction(GROUP_MACHINES_LIST);
        if (actionList != null) {
            machinesActions.addAll(actionList);
        }

        if (registeredMachineMap.isEmpty()) {
            return;
        }

        final List<Map.Entry<String, Machine>> machineEntryList = new LinkedList(registeredMachineMap.entrySet());
        // defined MachineDto Comparator here
        Collections.sort(machineEntryList, new MachineListEntryComparator());

        String machineCategory = null;
        for (Map.Entry<String, Machine> machineEntry : machineEntryList) {
            final Machine machine = machineEntry.getValue();
            final MachineConfig machineConfig = machine.getConfig();

            if (!this.getMachineCategory(machineConfig).equals(machineCategory)) {
                machineCategory = this.getMachineCategory(machineConfig);
                machinesActions.addSeparator(machineCategory);
            }
            machinesActions.add(machinesListWidget.createAction(machine.getId(), machineConfig.getName()));
        }

        machinesListWidget.updatePopup();

        if (machinesListWidget.getSelectedName() == null && machinesActions.getChildrenCount() > 0) {
            Machine firstMachine = machineEntryList.get(0).getValue();
            if (firstMachine == null) {
                return;
            }
            machinesListWidget.selectElement(firstMachine.getId(), firstMachine.getConfig().getName());
        }
    }

    private String getMachineCategory(MachineConfig machineConfig) {
        if (machineConfig.isDev()) {
            return locale.devMachineCategory();
        }
        return machineConfig.getType();
    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        workspaceRunning = true;
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        machinesListWidget.selectElement(null, null);
        registeredMachineMap.clear();
        workspaceRunning = false;
    }

    private class MachineListEntryComparator implements Comparator<Map.Entry<String, Machine>> {
        @Override
        public int compare(Map.Entry<String, Machine> o1, Map.Entry<String, Machine> o2) {
            final Machine firstMachine = o1.getValue();
            final Machine secondMachine = o2.getValue();

            if (firstMachine == null) {
                return -1;
            }
            if (secondMachine == null) {
                return 1;
            }

            final MachineConfig firstMachineConfig = firstMachine.getConfig();
            final MachineConfig secondMachineConfig = secondMachine.getConfig();

            if (firstMachineConfig.isDev()) {
                return -1;
            }
            if (secondMachineConfig.isDev()) {
                return 1;
            }

            if (firstMachineConfig.getType().equalsIgnoreCase(secondMachineConfig.getType())) {
                return (firstMachineConfig.getName()).compareToIgnoreCase(secondMachineConfig.getName());
            }

            return (getMachineCategory(firstMachineConfig)).compareToIgnoreCase(getMachineCategory(secondMachineConfig));
        }
    }
}
