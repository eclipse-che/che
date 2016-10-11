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
package org.eclipse.che.ide.extension.machine.client.processes.panel;

import com.google.common.base.Strings;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.machine.MachineProcess;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceRuntime;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartedEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsolePresenter;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessFinishedEvent;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNodeSelectedEvent;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenu;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenuFactory;
import org.eclipse.che.ide.ui.loaders.DownloadWorkspaceOutputEvent;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.COMMAND_NODE;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.ROOT_NODE;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.TERMINAL_NODE;

/**
 * Presenter for the panel for managing processes.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ProcessesPanelPresenter extends BasePresenter implements ProcessesPanelView.ActionDelegate,
                                                                      ProcessFinishedEvent.Handler,
                                                                      OutputConsole.ActionDelegate,
                                                                      WorkspaceStartedEvent.Handler,
                                                                      WorkspaceStoppedEvent.Handler,
                                                                      MachineStateEvent.Handler,
                                                                      WsAgentStateHandler,
                                                                      EnvironmentOutputEvent.Handler,
                                                                      DownloadWorkspaceOutputEvent.Handler {

    public static final  String SSH_PORT                    = "22";
    private static final String DEFAULT_TERMINAL_NAME       = "Terminal";

    public static final String TERMINAL_AGENT               = "org.eclipse.che.terminal";
    public static final String SSH_AGENT                    = "org.eclipse.che.ssh";

    final Map<String, OutputConsole>     consoles;
    final Map<OutputConsole, String>     consoleCommands;
    final Map<String, TerminalPresenter> terminals;

    private final ProcessesPanelView            view;
    private final MachineLocalizationConstant   localizationConstant;
    private final MachineResources              resources;
    private final MachineServiceClient          machineServiceClient;
    private final WorkspaceAgent                workspaceAgent;
    private final AppContext                    appContext;
    private final NotificationManager           notificationManager;
    private final EntityFactory                 entityFactory;
    private final TerminalFactory               terminalFactory;
    private final CommandConsoleFactory         commandConsoleFactory;
    private final DialogFactory                 dialogFactory;
    private final ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory;
    private final CommandTypeRegistry           commandTypeRegistry;
    private final Map<String, ProcessTreeNode>  machineNodes;
    private final EventBus                      eventBus;

    ProcessTreeNode rootNode;

    private List<ProcessTreeNode> rootNodes;
    private ProcessTreeNode       contextTreeNode;

    @Inject
    public ProcessesPanelPresenter(ProcessesPanelView view,
                                   MachineLocalizationConstant localizationConstant,
                                   MachineResources resources,
                                   EventBus eventBus,
                                   MachineServiceClient machineServiceClient,
                                   WorkspaceAgent workspaceAgent,
                                   AppContext appContext,
                                   NotificationManager notificationManager,
                                   EntityFactory entityFactory,
                                   TerminalFactory terminalFactory,
                                   CommandConsoleFactory commandConsoleFactory,
                                   DialogFactory dialogFactory,
                                   ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory,
                                   CommandTypeRegistry commandTypeRegistry) {
        this.view = view;
        this.localizationConstant = localizationConstant;
        this.resources = resources;
        this.machineServiceClient = machineServiceClient;
        this.workspaceAgent = workspaceAgent;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.entityFactory = entityFactory;
        this.terminalFactory = terminalFactory;
        this.commandConsoleFactory = commandConsoleFactory;
        this.dialogFactory = dialogFactory;
        this.consoleTreeContextMenuFactory = consoleTreeContextMenuFactory;
        this.eventBus = eventBus;
        this.commandTypeRegistry = commandTypeRegistry;

        machineNodes = new HashMap<>();
        rootNodes = new ArrayList<>();
        rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, rootNodes);
        terminals = new HashMap<>();
        consoles = new HashMap<>();
        consoleCommands = new HashMap<>();

        view.setDelegate(this);

        eventBus.addHandler(ProcessFinishedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStartedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(EnvironmentOutputEvent.TYPE, this);
        eventBus.addHandler(DownloadWorkspaceOutputEvent.TYPE, this);
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public String getTitle() {
        return localizationConstant.viewProcessesTitle();
    }

    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Override
    public IsWidget getView() {
        return view;
    }

    @Override
    public String getTitleToolTip() {
        return localizationConstant.viewProcessesTooltip();
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return resources.terminal();
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
        workspaceAgent.setActivePart(this);
        provideMachineNode(event.getMachine(), false);
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        final MachineEntity machine = event.getMachine();
        if (!machine.isDev()) {
            provideMachineNode(machine, true);
        }
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        ProcessTreeNode destroyedMachineNode = machineNodes.get(event.getMachineId());
        if (destroyedMachineNode == null) {
            return;
        }

        rootNodes.remove(destroyedMachineNode);
        view.setProcessesData(rootNode);

        final Collection<ProcessTreeNode> children = new ArrayList<>();
        children.addAll(destroyedMachineNode.getChildren());
        for (ProcessTreeNode child : children) {
            if (TERMINAL_NODE.equals(child.getType()) && terminals.containsKey(child.getId())) {
                onCloseTerminal(child);
            } else if (COMMAND_NODE.equals(child.getType()) && consoles.containsKey(child.getId())) {
                onStopCommandProcess(child);
                view.hideProcessOutput(child.getId());
            }
        }

        view.hideProcessOutput(destroyedMachineNode.getId());
    }

    @Override
    public void onCloseTerminal(ProcessTreeNode node) {
        closeTerminal(node);
        view.hideProcessOutput(node.getId());
    }

    @Override
    public void onTerminalTabClosing(ProcessTreeNode node) {
        closeTerminal(node);
    }

    private void closeTerminal(ProcessTreeNode node) {
        String terminalId = node.getId();
        if (terminals.containsKey(terminalId)) {
            onStopProcess(node);
            terminals.get(terminalId).stopTerminal();
            terminals.remove(terminalId);
        }
    }

    /** Opens new terminal for the selected machine. */
    public void newTerminal() {
        workspaceAgent.setActivePart(this);

        final ProcessTreeNode selectedTreeNode = view.getSelectedTreeNode();
        final MachineEntity devMachine = appContext.getDevMachine();
        if (selectedTreeNode == null && devMachine != null) {
            onAddTerminal(devMachine.getId());
            return;
        }

        if (selectedTreeNode == null) {
            String notificationTitle = localizationConstant.failedToConnectTheTerminal();
            String notificationContent = localizationConstant.machineNotFound("");
            notificationManager.notify(notificationTitle, notificationContent, FAIL, FLOAT_MODE);
            return;
        }

        if (selectedTreeNode.getType() == MACHINE_NODE) {
            MachineEntity machine = (MachineEntity)selectedTreeNode.getData();
            onAddTerminal(machine.getId());
            return;
        }

        ProcessTreeNode parent = selectedTreeNode.getParent();
        if (parent != null && parent.getType() == MACHINE_NODE) {
            MachineEntity machine = (MachineEntity)parent.getData();
            onAddTerminal(machine.getId());
        }
    }

    /**
     * Selects dev machine.
     */
    public void selectDevMachine() {
        for (final ProcessTreeNode processTreeNode : machineNodes.values()) {
            if (processTreeNode.getData() instanceof MachineEntity) {
                if (((MachineEntity)processTreeNode.getData()).isDev()) {
                    view.selectNode(processTreeNode);

                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            eventBus.fireEvent(new ProcessTreeNodeSelectedEvent(processTreeNode));
                        }
                    });
                }
            }
        }
    }

    /**
     * Adds new terminal to the processes panel
     *
     * @param machineId
     *         id of machine in which the terminal will be added
     */
    @Override
    public void onAddTerminal(final String machineId) {
        final MachineEntity machine = getMachine(machineId);
        final ProcessTreeNode machineTreeNode = findProcessTreeNodeById(machineId);
        if (machineTreeNode == null || machine == null) {
            notificationManager.notify(localizationConstant.failedToConnectTheTerminal(),
                                       localizationConstant.machineNotFound(machineId), FAIL, FLOAT_MODE);
            Log.error(getClass(), localizationConstant.machineNotFound(machineId));
            return;
        }

        final TerminalPresenter newTerminal = terminalFactory.create(machine);
        final IsWidget terminalWidget = newTerminal.getView();
        final String terminalName = getUniqueTerminalName(machineTreeNode);
        final ProcessTreeNode terminalNode = new ProcessTreeNode(TERMINAL_NODE,
                                                                 machineTreeNode,
                                                                 terminalName,
                                                                 resources.terminalTreeIcon(),
                                                                 null);
        addChildToMachineNode(terminalNode, machineTreeNode);

        final String terminalId = terminalNode.getId();
        terminals.put(terminalId, newTerminal);
        view.addProcessNode(terminalNode);
        view.addWidget(terminalId, terminalName, terminalNode.getTitleIcon(), terminalWidget, false);
        refreshStopButtonState(terminalId);

        newTerminal.setVisible(true);
        newTerminal.connect();
        newTerminal.setListener(new TerminalPresenter.TerminalStateListener() {
            @Override
            public void onExit() {
                onStopProcess(terminalNode);
                terminals.remove(terminalId);
            }
        });
    }

    @Override
    public void onPreviewSsh(String machineId) {
        ProcessTreeNode machineTreeNode = findProcessTreeNodeById(machineId);
        if (machineTreeNode == null) {
            return;
        }

        Machine machine = (Machine)machineTreeNode.getData();

        OutputConsole defaultConsole = commandConsoleFactory.create("SSH");
        addCommandOutput(machineId, defaultConsole);

        String machineName = machine.getConfig().getName();
        String sshServiceAddress = getSshServerAddress(machine);
        String machineHost = "";
        String sshPort = SSH_PORT;
        if (sshServiceAddress != null) {
            String[] parts = sshServiceAddress.split(":");
            machineHost = parts[0];
            sshPort = (parts.length == 2) ? parts[1] : sshPort;
        }

        if (defaultConsole instanceof DefaultOutputConsole) {
            ((DefaultOutputConsole)defaultConsole).printText(localizationConstant.sshConnectInfo(machineName, machineHost, sshPort));
        }
    }

    @Override
    public void onTreeNodeSelected(final ProcessTreeNode node) {
        view.showProcessOutput(node.getId());
        refreshStopButtonState(node.getId());

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                eventBus.fireEvent(new ProcessTreeNodeSelectedEvent(node));
            }
        });
    }

    /**
     * Returns the ssh service address in format - host:port (example - localhost:32899)
     *
     * @param machine
     *         machine to retrieve address
     * @return ssh service address in format host:port
     */
    private String getSshServerAddress(Machine machine) {
        Map<String, ? extends Server> servers = machine.getRuntime().getServers();
        final Server sshServer = servers.get(SSH_PORT + "/tcp");
        return sshServer != null ? sshServer.getAddress() : null;
    }

    /**
     * Adds command node to process tree and displays command output
     *
     * @param machineId
     *         id of machine in which the command will be executed
     * @param outputConsole
     *         the console for command output
     */
    public void addCommandOutput(String machineId, OutputConsole outputConsole) {
        ProcessTreeNode machineTreeNode = findProcessTreeNodeById(machineId);
        if (machineTreeNode == null) {
            notificationManager.notify(localizationConstant.failedToExecuteCommand(), localizationConstant.machineNotFound(machineId),
                                       FAIL, FLOAT_MODE);
            Log.error(getClass(), localizationConstant.machineNotFound(machineId));
            return;
        }

        String commandId;
        String outputConsoleTitle = outputConsole.getTitle();
        ProcessTreeNode processTreeNode = getProcessTreeNodeByName(outputConsoleTitle, machineTreeNode);
        if (processTreeNode != null && isCommandStopped(processTreeNode.getId())) {
            // 'reuse' already existing console
            // actually - remove 'already used' console
            commandId = processTreeNode.getId();
            view.hideProcessOutput(commandId);
        }

        ProcessTreeNode commandNode = new ProcessTreeNode(COMMAND_NODE,
                                                          machineTreeNode,
                                                          outputConsoleTitle,
                                                          outputConsole.getTitleIcon(),
                                                          null);
        commandId = commandNode.getId();
        addChildToMachineNode(commandNode, machineTreeNode);

        addOutputConsole(commandId, commandNode, outputConsole, false);

        refreshStopButtonState(commandId);
        workspaceAgent.setActivePart(this);
    }

    @Nullable
    private ProcessTreeNode getProcessTreeNodeByName(String processName, ProcessTreeNode machineTreeNode) {
        for (ProcessTreeNode processTreeNode : machineTreeNode.getChildren()) {
            if (processTreeNode.getName().equals(processName)) {
                return processTreeNode;
            }
        }
        return null;
    }

    private boolean isCommandStopped(String commandId) {
        return consoles.containsKey(commandId) && consoles.get(commandId).isFinished();
    }

    private void addOutputConsole(final String id,
                                  final ProcessTreeNode processNode,
                                  final OutputConsole outputConsole,
                                  final boolean machineConsole) {
        consoles.put(id, outputConsole);
        consoleCommands.put(outputConsole, id);

        outputConsole.go(new AcceptsOneWidget() {
            @Override
            public void setWidget(final IsWidget widget) {
                view.addProcessNode(processNode);
                view.addWidget(id, outputConsole.getTitle(), outputConsole.getTitleIcon(), widget, machineConsole);
                if (!MACHINE_NODE.equals(processNode.getType())) {
                    view.selectNode(view.getNodeById(id));
                }
            }
        });

        outputConsole.addActionDelegate(this);
    }

    private void refreshStopButtonState(String selectedNodeId) {
        if (selectedNodeId == null) {
            return;
        }

        for (Map.Entry<String, OutputConsole> entry : consoles.entrySet()) {
            String nodeId = entry.getKey();
            if (selectedNodeId.equals(nodeId) && !entry.getValue().isFinished()) {
                view.setStopButtonVisibility(selectedNodeId, true);
            } else {
                view.setStopButtonVisibility(nodeId, false);
            }
        }
    }

    @Override
    public void onStopCommandProcess(ProcessTreeNode node) {
        String commandId = node.getId();
        if (consoles.containsKey(commandId) && !consoles.get(commandId).isFinished()) {
            consoles.get(commandId).stop();
        }
    }

    @Override
    public void onCloseCommandOutputClick(final ProcessTreeNode node) {
        closeCommandOutput(node, new SubPanel.RemoveCallback() {
            @Override
            public void remove() {
                view.hideProcessOutput(node.getId());
            }
        });
    }

    @Override
    public void onCommandTabClosing(ProcessTreeNode node, SubPanel.RemoveCallback removeCallback) {
        closeCommandOutput(node, removeCallback);
    }

    private void closeCommandOutput(ProcessTreeNode node, SubPanel.RemoveCallback removeCallback) {
        String commandId = node.getId();
        OutputConsole console = consoles.get(commandId);

        if (console == null) {
            removeCallback.remove();
            return;
        }

        if (console.isFinished()) {
            console.close();
            onStopProcess(node);
            consoles.remove(commandId);
            consoleCommands.remove(console);

            removeCallback.remove();

            return;
        }

        dialogFactory.createConfirmDialog("",
                                          localizationConstant.outputsConsoleViewStopProcessConfirmation(console.getTitle()),
                                          getConfirmCloseConsoleCallback(console, node, removeCallback),
                                          null).show();
    }

    private ConfirmCallback getConfirmCloseConsoleCallback(final OutputConsole console,
                                                           final ProcessTreeNode node,
                                                           final SubPanel.RemoveCallback removeCallback) {
        return new ConfirmCallback() {
            @Override
            public void accepted() {
                console.stop();
                onStopProcess(node);

                console.close();
                consoles.remove(node.getId());
                consoleCommands.remove(console);

                removeCallback.remove();
            }
        };
    }

    private void onStopProcess(ProcessTreeNode node) {
        String processId = node.getId();
        ProcessTreeNode parentNode = node.getParent();

        int processIndex = view.getNodeIndex(processId);
        if (processIndex < 0) {
            return;
        }

        int neighborIndex = processIndex > 0 ? processIndex - 1 : processIndex + 1;
        ProcessTreeNode neighborNode = view.getNodeByIndex(neighborIndex);
        if (neighborNode == null) {
            neighborNode = parentNode;
        }

        removeChildFromMachineNode(node, parentNode);
        view.selectNode(neighborNode);
    }

    private String getUniqueTerminalName(ProcessTreeNode machineNode) {
        String terminalName = DEFAULT_TERMINAL_NAME;
        if (!isTerminalNameExist(machineNode, terminalName)) {
            return DEFAULT_TERMINAL_NAME;
        }

        int counter = 2;
        do {
            terminalName = localizationConstant.viewProcessesTerminalNodeTitle(String.valueOf(counter));
            counter++;
        } while (isTerminalNameExist(machineNode, terminalName));
        return terminalName;
    }

    private boolean isTerminalNameExist(ProcessTreeNode machineNode, String terminalName) {
        for (ProcessTreeNode node : machineNode.getChildren()) {
            if (TERMINAL_NODE == node.getType() && node.getName().equals(terminalName)) {
                return true;
            }
        }
        return false;
    }

    private void addChildToMachineNode(final ProcessTreeNode childNode, final ProcessTreeNode machineTreeNode) {
        machineTreeNode.getChildren().add(childNode);
        view.setProcessesData(rootNode);
        view.selectNode(childNode);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                eventBus.fireEvent(new ProcessTreeNodeSelectedEvent(childNode));
            }
        });
    }

    private void removeChildFromMachineNode(ProcessTreeNode childNode, ProcessTreeNode machineTreeNode) {
        view.removeProcessNode(childNode);
        machineTreeNode.getChildren().remove(childNode);
        view.setProcessesData(rootNode);
    }

    private ProcessTreeNode findProcessTreeNodeById(String id) {
        for (ProcessTreeNode processTreeNode : rootNode.getChildren()) {
            if (id.equals(processTreeNode.getId())) {
                return processTreeNode;
            }
        }
        return null;
    }

    /**
     * Determines the agent is injected in the specified machine.
     *
     * @param machineName
     *          machine name
     * @param agent
     *          agent
     * @return
     *          <b>true</b> is the agent is injected, otherwise return <b>false</b>
     */
    private boolean hasAgent(String machineName, String agent) {
        Workspace workspace = appContext.getWorkspace();
        if (workspace == null) {
            return false;
        }

        WorkspaceConfig workspaceConfig = workspace.getConfig();
        if (workspaceConfig == null) {
            return false;
        }

        Map<String, ? extends Environment> environments = workspaceConfig.getEnvironments();
        if (environments == null) {
            return false;
        }

        for (Environment environment : environments.values()) {
            ExtendedMachine extendedMachine = environment.getMachines().get(machineName);
            if (extendedMachine.getAgents() != null && extendedMachine.getAgents().contains(agent)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Provides machine node:
     * <li>creates new machine node when this one not exist or {@code replace} is {@code true}</li>
     * <li>returns old machine node when this one exist and {@code replace} is {@code false}</li>
     *
     * @param machine
     *         machine to creating node
     * @param replace
     *         existed node will be replaced when {@code replace} is {@code true}
     * @return machine node
     */
    private ProcessTreeNode provideMachineNode(@NotNull MachineEntity machine, boolean replace) {
        final String machineId = machine.getId();
        if (!replace && machineNodes.containsKey(machineId)) {
            return machineNodes.get(machineId);
        }

        final ProcessTreeNode existedMachineNode = machineNodes.remove(machineId);
        final ProcessTreeNode newMachineNode = new ProcessTreeNode(MACHINE_NODE, rootNode, machine, null, new ArrayList<ProcessTreeNode>());
        newMachineNode.setRunning(true);
        newMachineNode.setHasTerminalAgent(hasAgent(machine.getDisplayName(), TERMINAL_AGENT));
        newMachineNode.setHasSSHAgent(hasAgent(machine.getDisplayName(), SSH_AGENT));
        machineNodes.put(machineId, newMachineNode);

        if (rootNodes.contains(existedMachineNode)) {
            rootNodes.remove(existedMachineNode);
        }

        rootNodes.add(newMachineNode);

        view.setProcessesData(rootNode);

        if (existedMachineNode == null) {
            final OutputConsole outputConsole = commandConsoleFactory.create(machine.getConfig().getName());
            addOutputConsole(machineId, newMachineNode, outputConsole, true);
        }

        return newMachineNode;
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

    @Nullable
    private MachineEntity getMachine(@NotNull String machineId) {
        List<MachineEntity> machines = getMachines(appContext.getWorkspace());
        for (MachineEntity machine : machines) {
            if (machineId.equals(machine.getId())) {
                return machine;
            }
        }
        return null;
    }

    //TODO: need to improve this method. Avoid duplicate for(;;).
    //Then we get output form machine it must be added to process tree already.
    @Override
    public void onEnvironmentOutputEvent(EnvironmentOutputEvent event) {
        final String content = event.getContent();
        final String machineName = event.getMachineName();
        for (ProcessTreeNode machineNode : machineNodes.values()) {
            if (machineName.equals(machineNode.getName())) {
                printMachineOutput(machineNode.getId(), content);
                return;
            }
        }

        final List<MachineEntity> machines = getMachines(appContext.getWorkspace());
        for (MachineEntity machineEntity : machines) {
            if (machineName.equals(machineEntity.getDisplayName())) {
                ProcessTreeNode machineNode = provideMachineNode(machineEntity, false);
                printMachineOutput(machineNode.getId(), content);
            }
        }
    }

    @Override
    public void onWorkspaceStarted(WorkspaceStartedEvent event) {
        List<MachineEntity> machines = getMachines(event.getWorkspace());
        if (machines.isEmpty()) {
            return;
        }

        MachineEntity devMachine = null;
        for (MachineEntity machineEntity : machines) {
            if (machineEntity.isDev()) {
                devMachine = machineEntity;
                break;
            }
        }

        ProcessTreeNode machineToSelect = null;
        if (devMachine != null) {
            machineToSelect = provideMachineNode(devMachine, true);
            machines.remove(devMachine);
        }

        for (MachineEntity machine : machines) {
            provideMachineNode(machine, true);
        }

        if (machineToSelect != null) {
            view.selectNode(machineToSelect);
        } else if (!machineNodes.isEmpty()) {
            machineToSelect = machineNodes.entrySet().iterator().next().getValue();
            view.selectNode(machineToSelect);
        }

        workspaceAgent.setActivePart(ProcessesPanelPresenter.this);
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        for (ProcessTreeNode processTreeNode : rootNode.getChildren()) {
            if (processTreeNode.getType() == MACHINE_NODE) {
                onCloseTerminal(processTreeNode);
                processTreeNode.setRunning(false);
                if (processTreeNode.getChildren() != null) {
                    processTreeNode.getChildren().clear();
                }
            }
        }

        rootNode.getChildren().clear();
        rootNodes.clear();
        machineNodes.clear();

        view.clear();
        view.selectNode(null);
        view.setProcessesData(rootNode);
    }

    @Override
    public void onConsoleOutput(OutputConsole console) {
        String command = consoleCommands.get(console);
        if (command != null) {
            view.markProcessHasOutput(command);
        }
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        List<MachineEntity> machines = getMachines(appContext.getWorkspace());
        if (machines.isEmpty()) {
            return;
        }

        for (MachineEntity machine : machines) {
            restoreState(machine);
        }
    }

    private void restoreState(final MachineEntity machine) {
        machineServiceClient.getProcesses(machine.getWorkspaceId(), machine.getId()).then(new Operation<List<MachineProcessDto>>() {
            @Override
            public void apply(List<MachineProcessDto> arg) throws OperationException {
                for (MachineProcessDto machineProcessDto : arg) {
                    /**
                     * Do not show the process if the command line has prefix #hidden
                     */
                    if (!Strings.isNullOrEmpty(machineProcessDto.getCommandLine()) &&
                        machineProcessDto.getCommandLine().startsWith("#hidden")) {
                        continue;
                    }

                    // hide the processes which are launched by command of unknown type
                    if (isProcessLaunchedByCommandOfKnownType(machineProcessDto)) {
                        final CommandOutputConsole console = commandConsoleFactory.create(new CommandImpl(machineProcessDto), machine);
                        console.listenToOutput(machineProcessDto.getOutputChannel());
                        console.attachToProcess(machineProcessDto);

                        addCommandOutput(machine.getId(), console);
                    }
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(localizationConstant.failedToGetProcesses(machine.getId()));
            }
        });
    }

    private boolean isProcessLaunchedByCommandOfKnownType(MachineProcess machineProcess) {
        return commandTypeRegistry.getCommandTypeById(machineProcess.getType()) != null;
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    @Override
    public void onProcessFinished(ProcessFinishedEvent event) {
        for (Map.Entry<String, OutputConsole> entry : consoles.entrySet()) {
            if (entry.getValue().isFinished()) {
                view.setStopButtonVisibility(entry.getKey(), false);
            }
        }
    }

    public void printMachineOutput(String machineId, String text) {
        OutputConsole console = consoles.get(machineId);
        if (console != null && console instanceof DefaultOutputConsole) {
            ((DefaultOutputConsole)console).printText(text);
        }
    }

    /**
     * Prints text to the machine console.
     *
     * @param machineId
     *         machine Id
     * @param text
     *         text to be printed
     * @param color
     *         color of the text or NULL
     */
    public void printMachineOutput(String machineId, String text, String color) {
        OutputConsole console = consoles.get(machineId);
        if (console != null && console instanceof DefaultOutputConsole) {
            ((DefaultOutputConsole)console).printText(text, color);
        }
    }

    /**
     * Returns the console text for the specified machine.
     *
     * @param machineId
     *          machine ID
     * @return
     *          console text or NULL if there is no machine with specified ID
     */
    public String getText(String machineId) {
        OutputConsole console = consoles.get(machineId);
        if (console == null) {
            return null;
        }

        if (console instanceof DefaultOutputConsole) {
            return ((DefaultOutputConsole)console).getText();
        } else if (console instanceof CommandOutputConsolePresenter) {
            return ((CommandOutputConsolePresenter)console).getText();
        }

        return null;
    }

    /**
     * Returns context selected tree node.
     *
     * @return tree node
     */
    public ProcessTreeNode getContextTreeNode() {
        return contextTreeNode;
    }

    /**
     * Returns context selected output console.
     *
     * @return output console
     */
    public OutputConsole getContextOutputConsole() {
        if (contextTreeNode == null) {
            return null;
        }

        return consoles.get(contextTreeNode.getId());
    }

    @Override
    public void onContextMenu(final int mouseX, final int mouseY, final ProcessTreeNode node) {
        view.selectNode(node);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                contextTreeNode = node;
                ConsoleTreeContextMenu contextMenu = consoleTreeContextMenuFactory.newContextMenu(node);
                contextMenu.show(mouseX, mouseY);
            }
        });
    }

    @Override
    public void onDownloadWorkspaceOutput(DownloadWorkspaceOutputEvent event) {
        Machine devMachine = null;

        for (ProcessTreeNode machineNode : machineNodes.values()) {
            if (!(machineNode.getData() instanceof Machine)) {
                continue;
            }

            Machine machine = (Machine)machineNode.getData();
            if (!machine.getConfig().isDev()) {
                continue;
            }

            devMachine = machine;
            break;
        }

        if (devMachine == null) {
            return;
        }

        String fileName = appContext.getWorkspace().getNamespace() + "-" + appContext.getWorkspace().getConfig().getName() +
                " " + DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +
                ".log";

        download(fileName, getText(devMachine.getId()));
    }

    @Override
    public void onDownloadOutput(OutputConsole console) {
        String id = consoleCommands.get(console);

        String fileName = appContext.getWorkspace().getNamespace() + "-" + appContext.getWorkspace().getConfig().getName() +
                " " + DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +
                ".log";
        download(fileName, getText(id));
    }

    /**
     * Invokes the browser to download a file.
     *
     * @param fileName
     *          file name
     * @param text
     *          file content
     */
    private native void download(String fileName, String text) /*-{
        var element = $doc.createElement('a');
        element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
        element.setAttribute('download', fileName);

        element.style.display = 'none';
        $doc.body.appendChild(element);

        element.click();

        $doc.body.removeChild(element);
    }-*/;

}
