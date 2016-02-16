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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.machine.gwt.client.MachineServiceClient;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.workspace.gwt.client.WorkspaceServiceClient;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectEvent;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectHandler;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.extension.machine.client.command.edit.EditCommandsPresenter;
import org.eclipse.che.ide.ui.dropdown.DropDownHeaderWidget;
import org.eclipse.che.ide.ui.dropdown.DropDownListFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.eclipse.che.ide.extension.machine.client.MachineExtension.GROUP_COMMANDS_LIST;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action that allows user to select command from list of all commands.
 *
 * @author Artem Zatsarynnyi
 * @author Oleksii Orel
 */
@Singleton
public class SelectCommandComboBoxReady extends AbstractPerspectiveAction implements CustomComponentAction,
                                                                                     EditCommandsPresenter.ConfigurationChangedListener,
                                                                                     CloseCurrentProjectHandler,
                                                                                     WsAgentStateHandler,
                                                                                     DropDownHeaderWidget.ActionDelegate {

    public static final  String                           GROUP_COMMANDS     = "CommandsGroup";
    private static final Comparator<CommandConfiguration> commandsComparator = new CommandsComparator();

    private final MachineLocalizationConstant locale;
    private final DropDownHeaderWidget        dropDownHeaderWidget;
    private final HTML                        devMachineLabelWidget;
    private final DropDownListFactory         dropDownListFactory;
    private final AppContext                  appContext;
    private final String                      workspaceId;
    private final ActionManager               actionManager;
    private final WorkspaceServiceClient      workspaceServiceClient;
    private final MachineServiceClient        machineServiceClient;
    private final CommandTypeRegistry         commandTypeRegistry;
    private final MachineResources            resources;

    private List<CommandConfiguration> commands;
    private DefaultActionGroup         commandActions;
    private String                     lastDevMachineId;

    @Inject
    public SelectCommandComboBoxReady(MachineLocalizationConstant locale,
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

        this.dropDownListFactory = dropDownListFactory;
        this.appContext = appContext;
        this.workspaceId = appContext.getWorkspace().getId();
        this.dropDownHeaderWidget = dropDownListFactory.createList(GROUP_COMMANDS_LIST);
        this.devMachineLabelWidget = new HTML(locale.selectCommandEmptyCurrentDevMachineText());

        commands = new LinkedList<>();

        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
        editCommandsPresenter.addConfigurationsChangedListener(this);

        commandActions = new DefaultActionGroup(GROUP_COMMANDS, false, actionManager);
        actionManager.registerAction(GROUP_COMMANDS, commandActions);

        lastDevMachineId = null;

        dropDownHeaderWidget.setDelegate(this);
    }

    @Override
    public void onSelect() {
        //do nothing
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        final String currentDevMachineId = appContext.getDevMachineId();
        if (currentDevMachineId == null) {
            return;
        }
        if (lastDevMachineId == null || !currentDevMachineId.equals(lastDevMachineId)) {
            //Gets DevMachine name by ID.
            machineServiceClient.getMachine(currentDevMachineId).then(new Operation<MachineDto>() {
                @Override
                public void apply(MachineDto arg) throws OperationException {
                    devMachineLabelWidget.setText(arg.getName());
                }
            });
            lastDevMachineId = currentDevMachineId;
        }
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
        customComponentHeader.add(this.devMachineLabelWidget);
        commandIconPanel.setStyleName(resources.getCss().selectCommandBoxIconPanel());
        commandIconPanel.add(new SVGImage(resources.cmdIcon()));
        customComponentHeader.add(commandIconPanel);
        customComponentHeader.add((Widget)dropDownHeaderWidget);

        return customComponentHeader;
    }

    /** Returns selected command. */
    @Nullable
    public CommandConfiguration getSelectedCommand() {
        if (commands.isEmpty()) {
            return null;
        }

        final String selectedCommandName = dropDownHeaderWidget.getSelectedElementName();

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
        dropDownHeaderWidget.selectElement(command.getName());
    }

    @Override
    public void onCloseCurrentProject(CloseCurrentProjectEvent event) {
        setCommandConfigurations(Collections.<CommandConfiguration>emptyList(), null);
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
        final DefaultActionGroup commandsList = (DefaultActionGroup)actionManager.getAction(GROUP_COMMANDS_LIST);

        commands.clear();

        clearCommandActions(commandsList);
        commandActions.removeAll();

        Collections.sort(commandConfigurations, commandsComparator);
        CommandConfiguration prevCommand = null;
        for (CommandConfiguration configuration : commandConfigurations) {
            if (prevCommand == null || !configuration.getType().getId().equals(prevCommand.getType().getId())) {
                commandActions.addSeparator(configuration.getType().getDisplayName());
            }
            commandActions.add(dropDownListFactory.createElement(configuration.getName(), configuration.getName(), dropDownHeaderWidget));
            prevCommand = configuration;
        }

        commandsList.addAll(commandActions);
        commands.addAll(commandConfigurations);

        if (commandToSelect != null) {
            setSelectedCommand(commandToSelect);
        } else {
            selectLastUsedCommand();
        }
    }

    private void clearCommandActions(@NotNull DefaultActionGroup commandsList) {
        for (Action action : commandActions.getChildActionsOrStubs()) {
            commandsList.remove(action);
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
            dropDownHeaderWidget.selectElement(command.getName());
        }
    }

    /** Clears the selected element in the 'Select Command' menu. */
    private void setEmptyCommand() {
        dropDownHeaderWidget.selectElement(this.locale.selectCommandEmptyCurrentCommandText());
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
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {

    }

    private static class CommandsComparator implements Comparator<CommandConfiguration> {
        @Override
        public int compare(CommandConfiguration o1, CommandConfiguration o2) {
            return o1.getType().getId().compareTo(o2.getType().getId());
        }
    }
}
