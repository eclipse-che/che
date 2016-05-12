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
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsPresenter;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.ide.ui.dropdown.DropDownListFactory;
import org.eclipse.che.ide.ui.dropdown.DropDownWidget;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.extension.machine.client.MachineExtension.GROUP_COMMANDS_LIST;
import static org.eclipse.che.ide.extension.machine.client.MachineExtension.GROUP_MACHINES_LIST;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action that allows user to select command from list of all commands.
 *
 * @author Artem Zatsarynnyi
 * @author Oleksii Orel
 */
@Singleton
public class SelectCommandComboBox extends AbstractPerspectiveAction implements CustomComponentAction,
                                                                                EditCommandsPresenter.ConfigurationChangedListener,
                                                                                WsAgentStateHandler,
                                                                                MachineStateEvent.Handler {

    public static final String GROUP_COMMANDS = "CommandsGroup";
    public static final String GROUP_MACHINES = "MachinesGroup";

    private final MachineLocalizationConstant locale;
    private final MachineResources            resources;
    private final Map<String, Machine>        registeredMachineMap;
    private final ActionManager               actionManager;
    private final WorkspaceServiceClient      workspaceServiceClient;
    private final MachineServiceClient        machineServiceClient;
    private final CommandTypeRegistry         commandTypeRegistry;
    private final DropDownWidget              commandsListWidget;
    private final DropDownWidget              machinesListWidget;
    private final List<CommandConfiguration>  commands;
    private final String                      workspaceId;
    private final DefaultActionGroup          commandActions;
    private final DefaultActionGroup          machinesActions;

    @Inject
    public SelectCommandComboBox(MachineLocalizationConstant locale,
                                 MachineResources resources,
                                 ActionManager actionManager,
                                 EventBus eventBus,
                                 DropDownListFactory dropDownListFactory,
                                 WorkspaceServiceClient workspaceServiceClient,
                                 MachineServiceClient machineServiceClient,
                                 CommandTypeRegistry commandTypeRegistry,
                                 EditCommandsPresenter editCommandsPresenter,
                                 AppContext appContext) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              locale.selectCommandControlTitle(),
              locale.selectCommandControlDescription(),
              null, null);
        this.locale = locale;
        this.resources = resources;
        this.actionManager = actionManager;
        this.workspaceServiceClient = workspaceServiceClient;
        this.machineServiceClient = machineServiceClient;
        this.commandTypeRegistry = commandTypeRegistry;
        this.workspaceId = appContext.getWorkspaceId();

        this.registeredMachineMap = new HashMap<>();
        this.commands = new ArrayList<>();

        this.machinesListWidget = dropDownListFactory.createDropDown(GROUP_MACHINES);
        this.commandsListWidget = dropDownListFactory.createDropDown(GROUP_COMMANDS);

        editCommandsPresenter.addConfigurationsChangedListener(this);

        commandActions = new DefaultActionGroup(GROUP_COMMANDS, false, actionManager);
        actionManager.registerAction(GROUP_COMMANDS, commandActions);

        machinesActions = new DefaultActionGroup(GROUP_MACHINES, false, actionManager);
        actionManager.registerAction(GROUP_MACHINES, machinesActions);

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
        eventBus.addHandler(MachineStateEvent.TYPE, this);
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
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
    public CommandConfiguration getSelectedCommand() {
        if (commands.isEmpty()) {
            return null;
        }

        final String selectedCommandName = commandsListWidget.getSelectedName();

        for (CommandConfiguration configuration : commands) {
            if (configuration.getName().equals(selectedCommandName)) {
                return configuration;
            }
        }
        return null;
    }

    /** Returns command by it's name. */
    @Nullable
    public CommandConfiguration getCommandByName(String name) {
        if (commands.isEmpty()) {
            return null;
        }

        for (CommandConfiguration configuration : commands) {
            if (configuration.getName().equals(name)) {
                return configuration;
            }
        }
        return null;
    }

    public void setSelectedCommand(CommandConfiguration command) {
        commandsListWidget.selectElement(command.getName(), command.getName());
    }

    /**
     * Load all saved commands.
     *
     * @param commandToSelect
     *         command that should be selected after loading all commands
     */
    private void loadCommands(@Nullable final CommandConfiguration commandToSelect) {
        workspaceServiceClient.getCommands(workspaceId).then(new Function<List<CommandDto>, List<CommandConfiguration>>() {
            @Override
            public List<CommandConfiguration> apply(List<CommandDto> arg) throws FunctionException {
                final List<CommandConfiguration> configurationList = new ArrayList<>();

                for (CommandDto command : arg) {
                    final CommandType type = commandTypeRegistry.getCommandTypeById(command.getType());
                    // skip command if it's type isn't registered
                    if (type != null) {
                        try {
                            configurationList.add(type.getConfigurationFactory().createFromDto(command));
                        } catch (IllegalArgumentException e) {
                            Log.warn(EditCommandsPresenter.class, e.getMessage());
                        }
                    }
                }

                return configurationList;
            }
        }).then(new Operation<List<CommandConfiguration>>() {
            @Override
            public void apply(List<CommandConfiguration> commandConfigurations) throws OperationException {
                setCommandConfigurations(commandConfigurations, commandToSelect);
            }
        });
    }

    /**
     * Sets command configurations to the list.
     *
     * @param commandConfigurations
     *         collection of command configurations to set
     * @param commandToSelect
     *         command that should be selected or {@code null} if none
     */
    private void setCommandConfigurations(@NotNull List<CommandConfiguration> commandConfigurations,
                                          @Nullable CommandConfiguration commandToSelect) {
        commands.clear();
        commandActions.removeAll();

        final DefaultActionGroup commandsList = (DefaultActionGroup)actionManager.getAction(GROUP_COMMANDS_LIST);
        if (commandsList != null) {
            commandActions.addAll(commandsList);
        }

        Collections.sort(commandConfigurations, new Comparator<CommandConfiguration>() {
            @Override
            public int compare(CommandConfiguration o1, CommandConfiguration o2) {
                return o1.getType().getId().compareTo(o2.getType().getId());
            }
        });
        CommandConfiguration prevCommand = null;
        for (CommandConfiguration configuration : commandConfigurations) {
            if (prevCommand == null || !configuration.getType().getId().equals(prevCommand.getType().getId())) {
                commandActions.addSeparator(configuration.getType().getDisplayName());
            }
            commandActions.add(commandsListWidget.createAction(configuration.getName(), configuration.getName()));
            prevCommand = configuration;
        }

        commands.addAll(commandConfigurations);

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
            // TODO: consider to saving last used command ID somewhere
            // for now, we always select first command
            final CommandConfiguration command = commands.get(0);
            commandsListWidget.selectElement(command.getName(), command.getName());
        }
    }

    @Nullable
    public Machine getSelectedMachine() {
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
    public void onConfigurationAdded(CommandConfiguration command) {
        loadCommands(null);
    }

    @Override
    public void onConfigurationRemoved(CommandConfiguration command) {
        loadCommands(null);
    }

    @Override
    public void onConfigurationsUpdated(CommandConfiguration command) {
        loadCommands(command);
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        loadCommands(null);
        loadMachines();
    }

    private void loadMachines() {
        machineServiceClient.getMachines(workspaceId).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> machines) throws OperationException {
                addMachineActions(machines);
            }
        });
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

    private void addMachineActions(List<MachineDto> machines) {
        for (MachineDto machine : machines) {
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

        final List<Map.Entry<String, MachineDto>> machineEntryList = new LinkedList(registeredMachineMap.entrySet());
        // defined MachineDto Comparator here
        Collections.sort(machineEntryList, new MachineDtoListEntryComparator());

        String machineCategory = null;
        for (Map.Entry<String, MachineDto> machineEntry : machineEntryList) {
            final MachineDto machine = machineEntry.getValue();
            final MachineConfigDto machineConfig = machine.getConfig();

            if (!this.getMachineCategory(machineConfig).equals(machineCategory)) {
                machineCategory = this.getMachineCategory(machineConfig);
                machinesActions.addSeparator(machineCategory);
            }
            machinesActions.add(machinesListWidget.createAction(machine.getId(), machineConfig.getName()));
        }

        machinesListWidget.updatePopup();

        if (machinesListWidget.getSelectedName() == null && machinesActions.getChildrenCount() > 0) {
            MachineDto firstMachine = machineEntryList.get(0).getValue();
            if (firstMachine == null) {
                return;
            }
            machinesListWidget.selectElement(firstMachine.getId(), firstMachine.getConfig().getName());
        }
    }

    private String getMachineCategory(MachineConfigDto machineConfig) {
        if (machineConfig.isDev()) {
            return locale.devMachineCategory();
        }
        return machineConfig.getType();
    }

    private class MachineDtoListEntryComparator implements Comparator<Map.Entry<String, MachineDto>> {
        @Override
        public int compare(Map.Entry<String, MachineDto> o1, Map.Entry<String, MachineDto> o2) {
            final MachineDto firstMachine = o1.getValue();
            final MachineDto secondMachine = o2.getValue();

            if (firstMachine == null) {
                return -1;
            }
            if (secondMachine == null) {
                return 1;
            }

            final MachineConfigDto firstMachineConfig = firstMachine.getConfig();
            final MachineConfigDto secondMachineConfig = secondMachine.getConfig();

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
