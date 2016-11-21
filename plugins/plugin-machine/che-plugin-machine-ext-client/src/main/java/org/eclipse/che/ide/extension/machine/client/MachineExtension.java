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
package org.eclipse.che.ide.extension.machine.client;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.actions.StopWorkspaceAction;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.Perspective;
import org.eclipse.che.ide.api.parts.PerspectiveManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.actions.CreateMachineAction;
import org.eclipse.che.ide.extension.machine.client.actions.CreateSnapshotAction;
import org.eclipse.che.ide.extension.machine.client.actions.DestroyMachineAction;
import org.eclipse.che.ide.extension.machine.client.actions.EditCommandsAction;
import org.eclipse.che.ide.extension.machine.client.actions.ExecuteSelectedCommandAction;
import org.eclipse.che.ide.extension.machine.client.actions.RestartMachineAction;
import org.eclipse.che.ide.extension.machine.client.actions.RunCommandAction;
import org.eclipse.che.ide.extension.machine.client.actions.SelectCommandComboBox;
import org.eclipse.che.ide.extension.machine.client.actions.SwitchPerspectiveAction;
import org.eclipse.che.ide.extension.machine.client.command.macros.ServerPortProvider;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStatusHandler;
import org.eclipse.che.ide.extension.machine.client.processes.NewTerminalAction;
import org.eclipse.che.ide.extension.machine.client.processes.actions.CloseConsoleAction;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ReRunProcessAction;
import org.eclipse.che.ide.extension.machine.client.processes.actions.StopProcessAction;
import org.eclipse.che.ide.extension.machine.client.processes.panel.ProcessesPanelPresenter;
import org.eclipse.che.ide.extension.machine.client.targets.EditTargetsAction;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.statepersistance.AppStateManager;
import org.eclipse.che.ide.util.input.KeyCodeMap;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CENTER_TOOLBAR;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_CONSOLES_TREE_CONTEXT_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_MAIN_MENU;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RIGHT_TOOLBAR;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_RUN;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_WORKSPACE;
import static org.eclipse.che.ide.api.constraints.Anchor.AFTER;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;
import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Machine extension entry point.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 */
@Singleton
@Extension(title = "Machine", version = "1.0.0")
public class MachineExtension {

    public static final String GROUP_MACHINE_TOOLBAR   = "MachineGroupToolbar";
    public static final String GROUP_COMMANDS_DROPDOWN = "CommandsSelector";
    public static final String GROUP_COMMANDS_LIST     = "CommandsListGroup";
    public static final String GROUP_MACHINES_DROPDOWN = "MachinesSelector";
    public static final String GROUP_MACHINES_LIST     = "MachinesListGroup";

    private final PerspectiveManager        perspectiveManager;
    private final Provider<AppStateManager> appStateManagerProvider;

    @Inject
    public MachineExtension(final MachineResources machineResources,
                            final EventBus eventBus,
                            final WorkspaceAgent workspaceAgent,
                            final AppContext appContext,
                            final ProcessesPanelPresenter processesPanelPresenter,
                            final Provider<ServerPortProvider> machinePortProvider,
                            final PerspectiveManager perspectiveManager,
                            final Provider<MachineStatusHandler> machineStatusHandlerProvider,
                            final ProjectExplorerPresenter projectExplorerPresenter,
                            final Provider<AppStateManager> appStateManagerProvider) {
        this.perspectiveManager = perspectiveManager;
        this.appStateManagerProvider = appStateManagerProvider;

        machineResources.getCss().ensureInjected();
        machineStatusHandlerProvider.get();

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                restoreTerminal();

                machinePortProvider.get();
                /* Do not show terminal on factories by default */
                if (appContext.getFactory() == null) {
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            processesPanelPresenter.selectDevMachine();
                            processesPanelPresenter.newTerminal();
                        }
                    });
                    workspaceAgent.openPart(processesPanelPresenter, PartStackType.INFORMATION);
                }
                if (!appStateManagerProvider.get().hasStateForWorkspace(appContext.getWorkspaceId())) {
                    workspaceAgent.setActivePart(projectExplorerPresenter);
                }
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {
            }
        });

        eventBus.addHandler(WorkspaceStartingEvent.TYPE, new WorkspaceStartingEvent.Handler() {
            @Override
            public void onWorkspaceStarting(WorkspaceStartingEvent event) {
                maximizeTerminal();
            }
        });

        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, new WorkspaceStoppedEvent.Handler() {
            @Override
            public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        workspaceAgent.setActivePart(projectExplorerPresenter);
                        processesPanelPresenter.selectDevMachine();
                        maximizeTerminal();
                    }
                });
            }
        });

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                // Add Processes part to Project perspective
                perspectiveManager.setPerspectiveId(PROJECT_PERSPECTIVE_ID);
                workspaceAgent.openPart(processesPanelPresenter, PartStackType.INFORMATION);
                if (appContext.getFactory() == null) {
                    workspaceAgent.setActivePart(processesPanelPresenter);
                }
            }
        });
    }

    /**
     * Maximizes terminal.
     */
    private void maximizeTerminal() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                Perspective perspective = perspectiveManager.getActivePerspective();
                if (perspective != null) {
                    perspective.maximizeBottomPart();
                }
            }
        });
    }

    /**
     * Restores terminal to its default size.
     */
    private void restoreTerminal() {
        Perspective perspective = perspectiveManager.getActivePerspective();
        if (perspective != null) {
            perspective.restoreParts();
        }
    }

    @Inject
    private void prepareActions(MachineLocalizationConstant localizationConstant,
                                ActionManager actionManager,
                                KeyBindingAgent keyBinding,
                                ExecuteSelectedCommandAction executeSelectedCommandAction,
                                SelectCommandComboBox selectCommandAction,
                                EditCommandsAction editCommandsAction,
                                CreateMachineAction createMachine,
                                RestartMachineAction restartMachine,
                                DestroyMachineAction destroyMachineAction,
                                StopWorkspaceAction stopWorkspaceAction,
                                SwitchPerspectiveAction switchPerspectiveAction,
                                CreateSnapshotAction createSnapshotAction,
                                RunCommandAction runCommandAction,
                                NewTerminalAction newTerminalAction,
                                EditTargetsAction editTargetsAction,
                                IconRegistry iconRegistry,
                                MachineResources machineResources,
                                ReRunProcessAction reRunProcessAction,
                                StopProcessAction stopProcessAction,
                                CloseConsoleAction closeConsoleAction) {
        final DefaultActionGroup mainMenu = (DefaultActionGroup)actionManager.getAction(GROUP_MAIN_MENU);

        final DefaultActionGroup workspaceMenu = (DefaultActionGroup)actionManager.getAction(GROUP_WORKSPACE);
        final DefaultActionGroup runMenu = (DefaultActionGroup)actionManager.getAction(GROUP_RUN);

        // register actions
        actionManager.registerAction("editCommands", editCommandsAction);
        actionManager.registerAction("selectCommandAction", selectCommandAction);
        actionManager.registerAction("executeSelectedCommand", executeSelectedCommandAction);

        actionManager.registerAction("editTargets", editTargetsAction);

        //add actions in machine menu
        final DefaultActionGroup machineMenu = new DefaultActionGroup(localizationConstant.mainMenuMachine(), true, actionManager);

        actionManager.registerAction("machine", machineMenu);
        actionManager.registerAction("createMachine", createMachine);
        actionManager.registerAction("destroyMachine", destroyMachineAction);
        actionManager.registerAction("restartMachine", restartMachine);
        actionManager.registerAction("stopWorkspace", stopWorkspaceAction);
        actionManager.registerAction("createSnapshot", createSnapshotAction);
        actionManager.registerAction("runCommand", runCommandAction);
        actionManager.registerAction("newTerminal", newTerminalAction);

        // add actions in main menu
        runMenu.add(newTerminalAction, FIRST);
        runMenu.addSeparator();
        runMenu.add(editCommandsAction);
        runMenu.add(editTargetsAction);

        workspaceMenu.add(stopWorkspaceAction);

        mainMenu.add(machineMenu, new Constraints(AFTER, IdeActions.GROUP_PROJECT));
        machineMenu.add(createMachine);
        machineMenu.add(restartMachine);
        machineMenu.add(destroyMachineAction);
        machineMenu.add(createSnapshotAction);

        // add actions on center part of toolbar
        final DefaultActionGroup centerToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_CENTER_TOOLBAR);
        final DefaultActionGroup machineToolbarGroup = new DefaultActionGroup(GROUP_MACHINE_TOOLBAR, false, actionManager);
        actionManager.registerAction(GROUP_MACHINE_TOOLBAR, machineToolbarGroup);
        centerToolbarGroup.add(machineToolbarGroup, FIRST);
        machineToolbarGroup.add(selectCommandAction);
        final DefaultActionGroup executeToolbarGroup = new DefaultActionGroup(actionManager);
        executeToolbarGroup.add(executeSelectedCommandAction);
        machineToolbarGroup.add(executeToolbarGroup);

        // add actions on right part of toolbar
        final DefaultActionGroup rightToolbarGroup = (DefaultActionGroup)actionManager.getAction(GROUP_RIGHT_TOOLBAR);
        rightToolbarGroup.add(switchPerspectiveAction);

        // add group for list of machines
        final DefaultActionGroup machinesList = new DefaultActionGroup(GROUP_MACHINES_DROPDOWN, true, actionManager);
        actionManager.registerAction(GROUP_MACHINES_LIST, machinesList);
        machinesList.add(editTargetsAction, FIRST);

        // add group for list of commands
        final DefaultActionGroup commandList = new DefaultActionGroup(GROUP_COMMANDS_DROPDOWN, true, actionManager);
        actionManager.registerAction(GROUP_COMMANDS_LIST, commandList);
        commandList.add(editCommandsAction, FIRST);


        // Consoles tree context menu group
        DefaultActionGroup consolesTreeContextMenu = (DefaultActionGroup)actionManager.getAction(GROUP_CONSOLES_TREE_CONTEXT_MENU);

        consolesTreeContextMenu.add(reRunProcessAction);
        consolesTreeContextMenu.add(stopProcessAction);
        consolesTreeContextMenu.add(closeConsoleAction);


        // Define hot-keys
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.F12).build(), "newTerminal");

        iconRegistry.registerIcon(new Icon("che.machine.icon", machineResources.devMachine()));
    }
}
