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
package org.eclipse.che.ide.processes.panel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.machine.events.MachineRunningEvent;
import org.eclipse.che.ide.api.machine.events.MachineStartingEvent;
import org.eclipse.che.ide.api.machine.events.ProcessFinishedEvent;
import org.eclipse.che.ide.api.machine.events.TerminalAgentServerRunningEvent;
import org.eclipse.che.ide.api.machine.execagent.dto.GetProcessLogsResponseDto;
import org.eclipse.che.ide.api.machine.execagent.dto.GetProcessesResponseDto;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.command.toolbar.processes.ActivateProcessOutputEvent;
import org.eclipse.che.ide.command.toolbar.processes.ProcessOutputClosedEvent;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.console.CommandOutputConsole;
import org.eclipse.che.ide.console.CommandOutputConsolePresenter;
import org.eclipse.che.ide.console.DefaultOutputConsole;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.ProcessTreeNodeSelectedEvent;
import org.eclipse.che.ide.processes.actions.ConsoleTreeContextMenu;
import org.eclipse.che.ide.processes.actions.ConsoleTreeContextMenuFactory;
import org.eclipse.che.ide.terminal.TerminalFactory;
import org.eclipse.che.ide.terminal.TerminalPresenter;
import org.eclipse.che.ide.ui.loaders.DownloadWorkspaceOutputEvent;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_TERMINAL_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_WS_AGENT_HTTP_REFERENCE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.COMMAND_NODE;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.ROOT_NODE;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.TERMINAL_NODE;

/** Presenter for the panel for managing processes. */
@Singleton
public class ProcessesPanelPresenter extends BasePresenter implements ProcessesPanelView.ActionDelegate,
                                                                      ProcessFinishedEvent.Handler,
                                                                      OutputConsole.ActionDelegate,
                                                                      WorkspaceRunningEvent.Handler,
                                                                      WorkspaceStoppedEvent.Handler,
                                                                      MachineStartingEvent.Handler,
                                                                      MachineRunningEvent.Handler,
                                                                      TerminalAgentServerRunningEvent.Handler,
                                                                      ExecAgentServerRunningEvent.Handler,
                                                                      EnvironmentOutputEvent.Handler,
                                                                      DownloadWorkspaceOutputEvent.Handler,
                                                                      PartStackStateChangedEvent.Handler,
                                                                      BasicIDEInitializedEvent.Handler {

    public static final  String SSH_PORT              = "22";
    public static final  String TERMINAL_AGENT        = "org.eclipse.che.terminal";
    public static final  String SSH_AGENT             = "org.eclipse.che.ssh";
    private static final String DEFAULT_TERMINAL_NAME = "Terminal";
    final Map<String, OutputConsole>     consoles;
    final Map<OutputConsole, String>     consoleCommands;
    final Map<String, TerminalPresenter> terminals;

    private final ProcessesPanelView            view;
    private final CoreLocalizationConstant      localizationConstant;
    private final MachineResources              resources;
    private final Provider<WorkspaceAgent>      workspaceAgentProvider;
    private final CommandManager                commandManager;
    private final SshServiceClient              sshServiceClient;
    private final AppContext                    appContext;
    private final NotificationManager           notificationManager;
    private final TerminalFactory               terminalFactory;
    private final CommandConsoleFactory         commandConsoleFactory;
    private final DialogFactory                 dialogFactory;
    private final ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory;
    private final CommandTypeRegistry           commandTypeRegistry;
    private final ExecAgentCommandManager       execAgentCommandManager;
    private final Provider<MacroProcessor>      macroProcessorProvider;
    private final EventBus                      eventBus;
    private final Map<String, ProcessTreeNode>  machineNodes;
    ProcessTreeNode rootNode;
    private ProcessTreeNode          contextTreeNode;
    private Map<String, MachineImpl> machines;

    @Inject
    public ProcessesPanelPresenter(ProcessesPanelView view,
                                   CoreLocalizationConstant localizationConstant,
                                   MachineResources resources,
                                   EventBus eventBus,
                                   Provider<WorkspaceAgent> workspaceAgentProvider,
                                   AppContext appContext,
                                   NotificationManager notificationManager,
                                   TerminalFactory terminalFactory,
                                   CommandConsoleFactory commandConsoleFactory,
                                   DialogFactory dialogFactory,
                                   ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory,
                                   CommandManager commandManager,
                                   CommandTypeRegistry commandTypeRegistry,
                                   SshServiceClient sshServiceClient,
                                   ExecAgentCommandManager execAgentCommandManager,
                                   Provider<MacroProcessor> macroProcessorProvider) {
        this.view = view;
        this.localizationConstant = localizationConstant;
        this.resources = resources;
        this.commandManager = commandManager;
        this.workspaceAgentProvider = workspaceAgentProvider;
        this.sshServiceClient = sshServiceClient;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.terminalFactory = terminalFactory;
        this.commandConsoleFactory = commandConsoleFactory;
        this.dialogFactory = dialogFactory;
        this.consoleTreeContextMenuFactory = consoleTreeContextMenuFactory;
        this.eventBus = eventBus;
        this.commandTypeRegistry = commandTypeRegistry;
        this.execAgentCommandManager = execAgentCommandManager;
        this.macroProcessorProvider = macroProcessorProvider;

        machineNodes = new HashMap<>();
        machines = new HashMap<>();
        rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, new ArrayList<ProcessTreeNode>());
        terminals = new HashMap<>();
        consoles = new HashMap<>();
        consoleCommands = new HashMap<>();

        view.setDelegate(this);

        eventBus.addHandler(ProcessFinishedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceRunningEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
        eventBus.addHandler(MachineStartingEvent.TYPE, this);
        eventBus.addHandler(MachineRunningEvent.TYPE, this);
        eventBus.addHandler(TerminalAgentServerRunningEvent.TYPE, this);
        eventBus.addHandler(ExecAgentServerRunningEvent.TYPE, this);
        eventBus.addHandler(EnvironmentOutputEvent.TYPE, this);
        eventBus.addHandler(DownloadWorkspaceOutputEvent.TYPE, this);
        eventBus.addHandler(PartStackStateChangedEvent.TYPE, this);
        eventBus.addHandler(ActivateProcessOutputEvent.TYPE, event -> setActiveProcessOutput(event.getPid()));
        eventBus.addHandler(BasicIDEInitializedEvent.TYPE, this);

        Scheduler.get().scheduleDeferred(this::updateMachineList);
    }

    /**
     * Updates list of the machines from application context.
     */
    public void updateMachineList() {
        if (appContext.getWorkspace() == null) {
            return;
        }

        List<MachineImpl> machines = getMachines();
        if (machines.isEmpty()) {
            return;
        }

        for (MachineImpl machine : machines) {
            if (machine.getServerByName(SERVER_WS_AGENT_HTTP_REFERENCE).isPresent()) {
                provideMachineNode(machine.getName(), true);
                machines.remove(machine);
                break;
            }
        }

        for (MachineImpl machine : machines) {
            provideMachineNode(machine.getName(), true);
        }

        ProcessTreeNode machineToSelect = machineNodes.entrySet().iterator().next().getValue();

        view.selectNode(machineToSelect);
        notifyTreeNodeSelected(machineToSelect);
    }

    /**
     * determines whether process tree is visible.
     *
     * @return
     */
    public boolean isProcessesTreeVisible() {
        return view.isProcessesTreeVisible();
    }

    /**
     * Sets visibility for processes tree
     *
     * @param visible
     */
    public void setProcessesTreeVisible(boolean visible) {
        view.setProcessesTreeVisible(visible);
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
    public void onMachineStarting(MachineStartingEvent event) {
        provideMachineNode(event.getMachine().getName(), false);
    }

    @Override
    public void onMachineRunning(MachineRunningEvent event) {
        machines.put(event.getMachine().getName(), event.getMachine());
        provideMachineNode(event.getMachine().getName(), true);
    }

    @Override
    public void onCloseTerminal(ProcessTreeNode node) {
        closeTerminal(node);
        view.removeWidget(node.getId());
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
    public void newTerminal(Object source) {
        final ProcessTreeNode selectedTreeNode = view.getSelectedTreeNode();

        final WorkspaceImpl workspace = appContext.getWorkspace();
        final Optional<MachineImpl> devMachine = workspace.getDevMachine();

        if (selectedTreeNode == null && devMachine.isPresent()) {
            onAddTerminal(devMachine.get().getName(), source);
            return;
        }

        if (selectedTreeNode == null) {
            String notificationTitle = localizationConstant.failedToConnectTheTerminal();
            String notificationContent = localizationConstant.machineNotFound("");
            notificationManager.notify(notificationTitle, notificationContent, FAIL, FLOAT_MODE);
            return;
        }

        if (selectedTreeNode.getType() == MACHINE_NODE) {
            String machineName = (String)selectedTreeNode.getData();
            onAddTerminal(machineName, source);
            return;
        }

        ProcessTreeNode parent = selectedTreeNode.getParent();
        if (parent != null && parent.getType() == MACHINE_NODE) {
            String machineName = (String)parent.getData();
            onAddTerminal(machineName, source);
        }
    }

    /**
     * Selects dev machine.
     */
    private void selectDevMachine() {
        Optional<MachineImpl> devMachine = appContext.getWorkspace().getDevMachine();
        if (!devMachine.isPresent()) {
            return;
        }

        for (ProcessTreeNode processTreeNode : machineNodes.values()) {
            if (processTreeNode.getType() == MACHINE_NODE) {
                String machineName = (String)processTreeNode.getData();

                if (machineName.equals(devMachine.get().getName())) {
                    view.selectNode(processTreeNode);
                    notifyTreeNodeSelected(processTreeNode);

                    return;
                }
            }
        }
    }

    /** Set active output of the process with the given PID. */
    private void setActiveProcessOutput(int pid) {
        ProcessTreeNode processNode = null;

        for (Map.Entry<String, OutputConsole> entry : consoles.entrySet()) {
            final OutputConsole console = entry.getValue();
            if (console instanceof CommandOutputConsole) {
                final int consolePid = ((CommandOutputConsole)console).getPid();
                if (pid == consolePid) {
                    final String commandId = consoleCommands.get(console);
                    processNode = findTreeNodeById(commandId);
                }
            }
        }

        if (processNode != null) {
            view.selectNode(processNode);
        }
    }

    /**
     * Adds new terminal to the processes panel
     *
     * @param machineId
     *         id of machine in which the terminal will be added
     */
    @Override
    public void onAddTerminal(final String machineId, Object source) {
        final MachineImpl machine = getMachine(machineId);
        if (machine == null) {
            notificationManager.notify(localizationConstant.failedToConnectTheTerminal(),
                                       localizationConstant.machineNotFound(machineId), FAIL, FLOAT_MODE);
            Log.error(getClass(), localizationConstant.machineNotFound(machineId));
            return;
        }

        final ProcessTreeNode machineTreeNode = provideMachineNode(machine.getName(), false);
        if (machineTreeNode == null) {
            return;
        }

        final TerminalPresenter newTerminal = terminalFactory.create(machine, source);
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
        terminalWidget.asWidget().ensureDebugId(terminalName);
        view.addWidget(terminalId, terminalName, terminalNode.getTitleIcon(), terminalWidget, false);
        refreshStopButtonState(terminalId);

        workspaceAgentProvider.get().setActivePart(this);

        newTerminal.setVisible(true);
        newTerminal.connect();
        newTerminal.setListener(() -> onCloseTerminal(terminalNode));
    }

    @Override
    public void onPreviewSsh(String machineId) {
        ProcessTreeNode machineTreeNode = findTreeNodeById(machineId);
        if (machineTreeNode == null) {
            return;
        }

        MachineImpl machine = (MachineImpl)machineTreeNode.getData();

        final OutputConsole defaultConsole = commandConsoleFactory.create("SSH");
        addCommandOutput(machineId, defaultConsole);

        final String machineName = machine.getName();
        String sshServiceAddress = getSshServerAddress(machine);
        final String machineHost;
        final String sshPort;
        if (sshServiceAddress != null) {
            String[] parts = sshServiceAddress.split(":");
            machineHost = parts[0];
            sshPort = (parts.length == 2) ? parts[1] : SSH_PORT;
        } else {
            sshPort = SSH_PORT;
            machineHost = "";
        }

        // user
        final String userName;
        String user = machine.getProperties().get("config.user");
        if (isNullOrEmpty(user)) {
            userName = "root";
        } else {
            userName = user;
        }

        // ssh key
        final String workspaceName = appContext.getWorkspace().getConfig().getName();
        Promise<SshPairDto> sshPairDtoPromise = sshServiceClient.getPair("workspace", appContext.getWorkspaceId());

        sshPairDtoPromise.then(new Operation<SshPairDto>() {
                                   @Override
                                   public void apply(SshPairDto sshPairDto) throws OperationException {
                                       if (defaultConsole instanceof DefaultOutputConsole) {
                                           ((DefaultOutputConsole)defaultConsole).enableAutoScroll(false);
                                           ((DefaultOutputConsole)defaultConsole).printText(localizationConstant.sshConnectInfo
                                                   (machineName, machineHost, sshPort,
                                                    workspaceName, userName,
                                                    localizationConstant
                                                            .sshConnectInfoPrivateKey(
                                                                    sshPairDto
                                                                            .getPrivateKey())));
                                       }

                                   }
                               }
                              ).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                if (defaultConsole instanceof DefaultOutputConsole) {
                    ((DefaultOutputConsole)defaultConsole).enableAutoScroll(false);
                    ((DefaultOutputConsole)defaultConsole).printText(localizationConstant.sshConnectInfo(machineName, machineHost, sshPort,
                                                                                                         workspaceName, userName,
                                                                                                         localizationConstant
                                                                                                                 .sshConnectInfoNoPrivateKey()));
                }
            }
        });

    }

    @Override
    public void onTreeNodeSelected(final ProcessTreeNode node) {
        setSelection(new Selection.NoSelectionProvided());

        if (node != null) {
            if (ProcessTreeNode.ProcessNodeType.MACHINE_NODE == node.getType()) {
                final MachineImpl machine = getMachine(node.getId());
                if (machine != null) {
                    setSelection(new Selection<>(machine));
                }

                view.showProcessOutput(node.getName());
            } else {
                view.showProcessOutput(node.getId());
                refreshStopButtonState(node.getId());
            }
        }

        notifyTreeNodeSelected(node);
    }

    /**
     * Returns the ssh service address in format - host:port (example - localhost:32899)
     *
     * @param machine
     *         machine to retrieve address
     * @return ssh service address in format host:port
     */
    private String getSshServerAddress(Machine machine) {
        Map<String, ? extends Server> servers = machine.getServers();
        final Server sshServer = servers.get(SSH_PORT + "/tcp");
        return sshServer != null ? sshServer.getUrl() : null;
    }

    public void addCommandOutput(OutputConsole outputConsole) {
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final Optional<MachineImpl> devMachine = workspace.getDevMachine();

        devMachine.ifPresent(machine -> addCommandOutput(machine.getName(), outputConsole));
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
        ProcessTreeNode machineTreeNode = findTreeNodeById(machineId);
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
        workspaceAgentProvider.get().setActivePart(this);
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
                    ProcessTreeNode node = view.getNodeById(id);
                    view.selectNode(node);
                    notifyTreeNodeSelected(node);
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
        closeCommandOutput(node, () -> {
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
            removeConsole(console, node, removeCallback);
        } else {
            dialogFactory.createConfirmDialog("",
                                              localizationConstant.outputsConsoleViewStopProcessConfirmation(console.getTitle()),
                                              getConfirmCloseConsoleCallback(console, node, removeCallback),
                                              null).show();
        }
    }

    private ConfirmCallback getConfirmCloseConsoleCallback(final OutputConsole console,
                                                           final ProcessTreeNode node,
                                                           final SubPanel.RemoveCallback removeCallback) {
        return () -> {
            console.stop();
            removeConsole(console, node, removeCallback);
        };
    }

    private void removeConsole(final OutputConsole console,
                               final ProcessTreeNode node,
                               final SubPanel.RemoveCallback removeCallback) {
        console.close();
        onStopProcess(node);

        consoles.remove(node.getId());
        consoleCommands.remove(console);
        view.removeWidget(node.getId());

        removeCallback.remove();

        if (console instanceof CommandOutputConsole) {
            eventBus.fireEvent(new ProcessOutputClosedEvent(((CommandOutputConsole)console).getPid()));
        }
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
        notifyTreeNodeSelected(neighborNode);
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
        notifyTreeNodeSelected(childNode);
    }

    private void removeChildFromMachineNode(ProcessTreeNode childNode, ProcessTreeNode machineTreeNode) {
        view.removeProcessNode(childNode);
        machineTreeNode.getChildren().remove(childNode);
        view.setProcessesData(rootNode);
    }

    @Nullable
    private ProcessTreeNode findTreeNodeById(String nodeId) {
        for (ProcessTreeNode machineTreeNode : rootNode.getChildren()) {
            if (nodeId.equals(machineTreeNode.getId())) {
                return machineTreeNode;
            } else {
                final Collection<ProcessTreeNode> machineProcesses = machineTreeNode.getChildren();
                if (machineProcesses != null) {
                    for (ProcessTreeNode processTreeNode : machineProcesses) {
                        if (nodeId.equals(processTreeNode.getId())) {
                            return processTreeNode;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Determines the agent is injected in the specified machine.
     *
     * @param machineName
     *         machine name
     * @param agent
     *         agent
     * @return <b>true</b> is the agent is injected, otherwise return <b>false</b>
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
            MachineConfig extendedMachine = environment.getMachines().get(machineName);
            if (extendedMachine != null) {
                if (extendedMachine.getInstallers() != null && extendedMachine.getInstallers().contains(agent)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks supporting of the terminal for machine which is running out the workspace runtime.
     *
     * @param machineId
     *         machine id
     * @return <b>true</b> is the terminal url, otherwise return <b>false</b>
     */
    private boolean hasTerminal(String machineId) {
        List<MachineImpl> wsMachines = getMachines();
        for (MachineImpl machineEntity : wsMachines) {
            if (machineId.equals(machineEntity.getName())) {
                return false;
            }
        }

        final MachineImpl machineEntity = machines.get(machineId);
        return machineEntity != null && machineEntity.getServerByName(SERVER_TERMINAL_REFERENCE).isPresent();
    }

    /**
     * Provides machine node:
     * <li>creates new machine node when this one not exist or {@code replace} is {@code true}</li>
     * <li>returns old machine node when this one exist and {@code replace} is {@code false}</li>
     *
     * @param machineName
     *         name of the machine to creating node
     * @param replace
     *         existed node will be replaced when {@code replace} is {@code true}
     * @return machine node
     */
    @Nullable
    private ProcessTreeNode provideMachineNode(String machineName, boolean replace) {
        final ProcessTreeNode existedMachineNode = findTreeNodeById(machineName);
        if (!replace && existedMachineNode != null) {
            return existedMachineNode;
        }

        //we need to keep old machine node children
        ArrayList<ProcessTreeNode> children = new ArrayList<>();

        // remove existed node
        for (ProcessTreeNode node : rootNode.getChildren()) {
            if (machineName.equals(node.getName())) {
                children.addAll(node.getChildren());
                rootNode.getChildren().remove(node);
                break;
            }
        }

        // create new node
        RuntimeImpl runtime = appContext.getWorkspace().getRuntime();
        if (runtime == null) {
            return null;
        }

        final ProcessTreeNode newMachineNode = new ProcessTreeNode(MACHINE_NODE, rootNode, machineName, null, children);
        // TODO (spi ide)
        newMachineNode.setRunning(/*RUNNING == machine.getStatus()*/true);
        newMachineNode.setHasTerminalAgent(hasAgent(machineName, TERMINAL_AGENT) || hasTerminal(machineName));
        newMachineNode.setHasSSHAgent(hasAgent(machineName, SSH_AGENT));
        for (ProcessTreeNode child : children) {
            child.setParent(newMachineNode);
        }

        machineNodes.put(machineName, newMachineNode);

        // add to children
        rootNode.getChildren().add(newMachineNode);

        // update the view
        view.setProcessesData(rootNode);

        // add output for the machine if it is not exist
        if (!consoles.containsKey(machineName)) {
            OutputConsole outputConsole = commandConsoleFactory.create(machineName);
            addOutputConsole(machineName, newMachineNode, outputConsole, true);
        }

        return newMachineNode;
    }

    private List<MachineImpl> getMachines() {
        final WorkspaceImpl workspace = appContext.getWorkspace();
        final RuntimeImpl runtime = workspace.getRuntime();
        if (runtime == null) {
            return emptyList();
        }

        return new ArrayList<>(runtime.getMachines().values());
    }

    @Nullable
    private MachineImpl getMachine(String machineId) {
        List<MachineImpl> wsMachines = getMachines();
        for (MachineImpl machine : wsMachines) {
            if (machineId.equals(machine.getName())) {
                return machine;
            }
        }
        return machines.get(machineId);
    }

    @Override
    public void onEnvironmentOutputEvent(EnvironmentOutputEvent event) {
        printMachineOutput(event.getMachineName(), event.getContent());
    }

    @Override
    public void onWorkspaceRunning(WorkspaceRunningEvent event) {
        List<MachineImpl> wsMachines = getMachines();
        if (wsMachines.isEmpty()) {
            return;
        }

        MachineImpl devMachine = null;
        for (MachineImpl machine : wsMachines) {
            if (machine.getServerByName(SERVER_WS_AGENT_HTTP_REFERENCE).isPresent()) {
                devMachine = machine;
                break;
            }
        }

        ProcessTreeNode machineToSelect = null;
        if (devMachine != null) {
            machineToSelect = provideMachineNode(devMachine.getName(), true);
            wsMachines.remove(devMachine);
        }

        for (MachineImpl machine : wsMachines) {
            provideMachineNode(machine.getName(), true);
        }

        if (machineToSelect != null) {
            view.selectNode(machineToSelect);
            notifyTreeNodeSelected(machineToSelect);
        } else if (!machineNodes.isEmpty()) {
            machineToSelect = machineNodes.entrySet().iterator().next().getValue();
            view.selectNode(machineToSelect);
            notifyTreeNodeSelected(machineToSelect);
        }

        for (MachineImpl machine : machines.values()) {
            if (/*RUNNING.equals(machine.getStatus()) && */!wsMachines.contains(machine)) {
                provideMachineNode(machine.getName(), true);
            }
        }
    }

    @Override
    public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
        try {
            for (ProcessTreeNode node : rootNode.getChildren()) {
                if (MACHINE_NODE == node.getType()) {
                    node.setRunning(false);

                    ArrayList<ProcessTreeNode> children = new ArrayList<>();
                    children.addAll(node.getChildren());

                    for (ProcessTreeNode child : children) {
                        if (COMMAND_NODE == child.getType()) {
                            OutputConsole console = consoles.get(child.getId());
                            removeConsole(console, child, () -> {
                            });
                        } else if (TERMINAL_NODE == child.getType()) {
                            onCloseTerminal(child);
                        }

                        view.hideProcessOutput(child.getId());
                        view.removeProcessNode(child);

                    }
                }
            }

        } catch (Exception e) {
            Log.error(getClass(), e);
        }

        view.setProcessesData(rootNode);
        selectDevMachine();
    }

    @Override
    public void onConsoleOutput(OutputConsole console) {
        String command = consoleCommands.get(console);
        if (command != null) {
            view.markProcessHasOutput(command);
        }
    }

    @Override
    public void onIDEInitialized(BasicIDEInitializedEvent event) {
        if (appContext.getFactory() == null && partStack != null) {
            partStack.setActivePart(this);
        }

        if (appContext.getWorkspace().getStatus() == RUNNING) {
            for (MachineImpl machine : getMachines()) {
                restoreProcessesState(machine.getName());
            }

            selectDevMachine();
            newTerminal(this);
        }
    }

    @Override
    public void onTerminalAgentServerRunning(TerminalAgentServerRunningEvent event) {
        // open terminal automatically for dev-machine only
        Optional<MachineImpl> devMachine = appContext.getWorkspace().getDevMachine();

        if (devMachine.isPresent() && event.getMachineName().equals(devMachine.get().getName())) {
            provideMachineNode(event.getMachineName(), true);
            newTerminal(this);
        }
    }

    @Override
    public void onExecAgentServerRunning(ExecAgentServerRunningEvent event) {
        restoreProcessesState(event.getMachineName());
    }

    private void restoreProcessesState(String machineName) {
        execAgentCommandManager.getProcesses(machineName, false)
                               .onSuccess(new BiConsumer<String, List<GetProcessesResponseDto>>() {
                                   @Override
                                   public void accept(String endpointId, List<GetProcessesResponseDto> processes) {
                                       for (GetProcessesResponseDto process : processes) {
                                           final int pid = process.getPid();
                                           final String type = process.getType();

                    /*
                     * Do not show the process if the command line has prefix #hidden
                     */
                                           if (!isNullOrEmpty(process.getCommandLine()) &&
                                               process.getCommandLine().startsWith("#hidden")) {
                                               continue;
                                           }

                    /*
                     * Hide the processes which are launched by command of unknown type
                     */
                    if (commandTypeRegistry.getCommandTypeById(type).isPresent()) {
                        final String processName = process.getName();
                        final Optional<CommandImpl> commandOptional = commandManager.getCommand(processName);

                        if (!commandOptional.isPresent()) {
                            final String commandLine = process.getCommandLine();
                            final CommandImpl command = new CommandImpl(processName, commandLine, type);
                            final CommandOutputConsole console = commandConsoleFactory.create(command, machineName);

                                                   getAndPrintProcessLogs(console, pid);
                                                   subscribeToProcess(console, pid);

                            addCommandOutput(machineName, console);
                        } else {
                            final CommandImpl commandByName = commandOptional.get();
                            macroProcessorProvider.get().expandMacros(commandByName.getCommandLine()).then(new Operation<String>() {
                                @Override
                                public void apply(String expandedCommandLine) throws OperationException {
                                    final CommandImpl command = new CommandImpl(commandByName.getName(),
                                                                                expandedCommandLine,
                                                                                commandByName.getType(),
                                                                                commandByName.getAttributes());

                                                                                 final CommandOutputConsole console =
                                                                                         commandConsoleFactory.create(command, machineName);

                                                                                 getAndPrintProcessLogs(console, pid);
                                                                                 subscribeToProcess(console, pid);

                                                                                 addCommandOutput(machineName, console);
                                                                             }
                                                                         });
                                               }
                                           }
                                       }
                                   }

                                   private void getAndPrintProcessLogs(final CommandOutputConsole console, final int pid) {
                                       String from = null;
                                       String till = null;
                                       int limit = 50;
                                       int skip = 0;
                                       execAgentCommandManager.getProcessLogs(machineName, pid, from, till, limit, skip)
                                                              .onSuccess(logs -> {
                                                                  for (GetProcessLogsResponseDto log : logs) {
                                                                      String text = log.getText();
                                                                      console.printOutput(text);
                                                                  }
                                                              })
                                                              .onFailure((s, error) -> Log.error(getClass(),
                                                                                                 "Error trying to get process log with pid: " +
                                                                                                 pid + ". " +
                                                                                                 error.getMessage()));
                                   }

                                   private void subscribeToProcess(CommandOutputConsole console, int pid) {
                                       String stderr = "stderr";
                                       String stdout = "stdout";
                                       String processStatus = "process_status";
                                       String after = null;
                                       execAgentCommandManager
                                               .subscribe(machineName, pid, asList(stderr, stdout, processStatus), after)
                                               .thenIfProcessStartedEvent(console.getProcessStartedConsumer())
                                               .thenIfProcessDiedEvent(console.getProcessDiedConsumer())
                                               .thenIfProcessStdOutEvent(console.getStdOutConsumer())
                                               .thenIfProcessStdErrEvent(console.getStdErrConsumer())
                                               .then(console.getProcessSubscribeConsumer());
                                   }
                               }).onFailure(
                (endpointId, error) -> notificationManager.notify(localizationConstant.failedToGetProcesses(machineName)));
    }

    private CommandImpl getWorkspaceCommandByName(String name) {
        for (Command command : appContext.getWorkspace().getConfig().getCommands()) {
            if (command.getName().equals(name)) {
                return new CommandImpl(command); // wrap model interface into implementation, workaround
            }
        }

        return null;
    }

    @Override
    public void onProcessFinished(ProcessFinishedEvent event) {
        for (Map.Entry<String, OutputConsole> entry : consoles.entrySet()) {
            if (entry.getValue().isFinished()) {
                view.setStopButtonVisibility(entry.getKey(), false);
            }
        }
    }

    @Override
    public void onToggleMaximizeConsole() {
        super.onToggleMaximize();

        if (partStack != null) {
            if (partStack.getPartStackState() == PartStack.State.MAXIMIZED) {
                view.setProcessesTreeVisible(false);
            } else {
                view.setProcessesTreeVisible(true);
            }
        }
    }

    @Override
    public void onPartStackStateChanged(PartStackStateChangedEvent event) {
        if (partStack.equals(event.getPartStack()) &&
            partStack.getPartStackState() == PartStack.State.NORMAL) {
            view.setProcessesTreeVisible(true);
        }
    }

    /**
     * Prints text to the machine console.
     *
     * @param machineName
     *         machine name
     * @param text
     *         text to be printed
     */
    public void printMachineOutput(final String machineName, final String text) {
        // Create a temporary machine node to display outputs.

        if (!consoles.containsKey(machineName)) {
            provideMachineNode(machineName, true);
        }

        OutputConsole console = consoles.get(machineName);
        if (console != null && console instanceof DefaultOutputConsole) {
            ((DefaultOutputConsole)console).printText(text);
        }
    }

    /**
     * Prints text to the machine console.
     *
     * @param machineName
     *         machine name
     * @param text
     *         text to be printed
     * @param color
     *         color of the text or NULL
     */
    public void printMachineOutput(String machineName, String text, String color) {
        OutputConsole console = consoles.get(machineName);
        if (console != null && console instanceof DefaultOutputConsole) {
            ((DefaultOutputConsole)console).printText(text, color);
        }
    }

    /**
     * Returns the console text for the specified machine.
     *
     * @param machineId
     *         machine ID
     * @return console text or NULL if there is no machine with specified ID
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

    /**
     * Sends an event informing that process tree node has been selected.
     *
     * @param node
     *         selected node
     */
    private void notifyTreeNodeSelected(final ProcessTreeNode node) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                eventBus.fireEvent(new ProcessTreeNodeSelectedEvent(node));
            }
        });
    }

    @Override
    public void onContextMenu(final int mouseX, final int mouseY, final ProcessTreeNode node) {
        view.selectNode(node);
        notifyTreeNodeSelected(node);

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
        WorkspaceImpl workspace = appContext.getWorkspace();
        Optional<MachineImpl> devMachine = workspace.getDevMachine();

        if (!devMachine.isPresent()) {
            return;
        }

        String fileName = workspace.getNamespace() + "-" + workspace.getConfig().getName() +
                          " " + DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) +
                          ".log";

        download(fileName, getText(devMachine.get().getName()));
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
     *         file name
     * @param text
     *         file content
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
