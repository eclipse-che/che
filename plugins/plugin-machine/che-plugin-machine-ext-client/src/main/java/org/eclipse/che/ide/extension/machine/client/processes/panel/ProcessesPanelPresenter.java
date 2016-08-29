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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.workspace.event.EnvironmentOutputEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessFinishedEvent;
import org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenu;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenuFactory;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                                                      OutputConsole.ConsoleOutputListener,
                                                                      WorkspaceStoppedEvent.Handler,
                                                                      MachineStateEvent.Handler,
                                                                      WsAgentStateHandler,
                                                                      EnvironmentOutputEvent.Handler {

    public static final  String SSH_PORT              = "22";
    private static final String DEFAULT_TERMINAL_NAME = "Terminal";

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
    private final DtoFactory                    dtoFactory;
    private final CommandTypeRegistry           commandTypeRegistry;
    private final ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory;
    private final Map<String, ProcessTreeNode>  machineNodes;

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
                                   DtoFactory dtoFactory,
                                   CommandTypeRegistry commandTypeRegistry,
                                   ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory) {
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
        this.dtoFactory = dtoFactory;
        this.commandTypeRegistry = commandTypeRegistry;
        this.consoleTreeContextMenuFactory = consoleTreeContextMenuFactory;

        machineNodes = new HashMap<>();
        rootNodes = new ArrayList<>();
        rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, rootNodes);
        terminals = new HashMap<>();
        consoles = new HashMap<>();
        consoleCommands = new HashMap<>();

        view.setDelegate(this);

        eventBus.addHandler(ProcessFinishedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(EnvironmentOutputEvent.TYPE, this);

        fetchMachines();
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
        addMachineNode(event.getMachine());
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        ProcessTreeNode destroyedMachineNode = machineNodes.get(event.getMachineId());
        if (destroyedMachineNode == null) {
            return;
        }

        rootNodes.remove(destroyedMachineNode);
        onCloseTerminal(destroyedMachineNode);
        onStopCommandProcess(destroyedMachineNode);

        view.setProcessesData(rootNode);
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
        if (selectedTreeNode != null) {
            if (selectedTreeNode.getType() == MACHINE_NODE) {
                onAddTerminal(appContext.getWorkspaceId(), selectedTreeNode.getId());
            } else {
                if (selectedTreeNode.getParent() != null &&
                    selectedTreeNode.getParent().getType() == MACHINE_NODE) {
                    onAddTerminal(appContext.getWorkspaceId(), appContext.getDevMachine().getId());
                }
            }
        }

        // no selected node
        if (appContext.getDevMachine() != null) {
            onAddTerminal(appContext.getWorkspaceId(), appContext.getDevMachine().getId());
        }
    }

    /**
     * Adds new terminal to the processes panel
     *
     * @param machineId
     *         id of machine in which the terminal will be added
     */
    @Override
    public void onAddTerminal(final String workspaceId, final String machineId) {
        machineServiceClient.getMachine(workspaceId, machineId).then(new Operation<MachineDto>() {
            @Override
            public void apply(MachineDto arg) throws OperationException {
                org.eclipse.che.ide.extension.machine.client.machine.Machine machine = entityFactory.createMachine(arg);
                final ProcessTreeNode machineTreeNode = findProcessTreeNodeById(machineId);

                if (machineTreeNode == null) {
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
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(localizationConstant.failedToFindMachine(machineId));
            }
        });
    }

    @Override
    public void onPreviewSsh(String machineId) {
        ProcessTreeNode machineTreeNode = findProcessTreeNodeById(machineId);
        if (machineTreeNode == null) {
            return;
        }

        MachineDto machine = (MachineDto)machineTreeNode.getData();

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
    public void onTreeNodeSelected(ProcessTreeNode node) {
        view.showProcessOutput(node.getId());
        refreshStopButtonState(node.getId());
    }

    /**
     * Returns the ssh service address in format - host:port (example - localhost:32899)
     *
     * @param machine
     *         machine to retrieve address
     * @return ssh service address in format host:port
     */
    private String getSshServerAddress(MachineDto machine) {
        if (machine.getRuntime().getServers().containsKey(SSH_PORT + "/tcp")) {
            return machine.getRuntime().getServers().get(SSH_PORT + "/tcp").getAddress();
        } else {
            return null;
        }
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
        view.addProcessNode(commandNode);
        addChildToMachineNode(commandNode, machineTreeNode);

        addOutputConsole(commandId, outputConsole, false);

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

    private void addOutputConsole(final String id, final OutputConsole outputConsole, final boolean machineConsole) {
        consoles.put(id, outputConsole);
        consoleCommands.put(outputConsole, id);

        outputConsole.go(new AcceptsOneWidget() {
            @Override
            public void setWidget(final IsWidget widget) {
                view.addWidget(id, outputConsole.getTitle(), outputConsole.getTitleIcon(), widget, machineConsole);
                view.selectNode(view.getNodeById(id));
            }
        });

        outputConsole.addOutputListener(this);
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

    private void addChildToMachineNode(ProcessTreeNode childNode, ProcessTreeNode machineTreeNode) {
        machineTreeNode.getChildren().add(childNode);
        view.setProcessesData(rootNode);
        view.selectNode(childNode);
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

    private ProcessTreeNode addMachineNode(Machine machine) {
        if (machineNodes.containsKey(machine.getId())) {
            return machineNodes.get(machine.getId());
        }

        final ProcessTreeNode machineNode = new ProcessTreeNode(MACHINE_NODE, rootNode, machine, null, new ArrayList<ProcessTreeNode>());
        machineNode.setRunning(true);
        machineNodes.put(machine.getId(), machineNode);

        if (rootNodes.contains(machineNode)) {
            rootNodes.remove(machineNode);
        }

        rootNodes.add(machineNode);

        OutputConsole outputConsole = commandConsoleFactory.create(machine.getConfig().getName());
        addOutputConsole(machine.getId(), outputConsole, true);

        view.setProcessesData(rootNode);

        return machineNode;
    }

    /** Get the list of all available machines. */
    public void fetchMachines() {
        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> machines) throws OperationException {
                if (machines.isEmpty()) {
                    return;
                }

                ProcessTreeNode machineToSelect = null;
                MachineDto devMachine = getDevMachine(machines);
                if (devMachine != null) {
                    machineToSelect = addMachineNode(devMachine);
                    machines.remove(devMachine);
                }

                for (MachineDto machine : machines) {
                    addMachineNode(machine);
                }

                if (machineToSelect == null) {
                    machineToSelect = machineNodes.entrySet().iterator().next().getValue();
                }
                view.selectNode(machineToSelect);

                workspaceAgent.setActivePart(ProcessesPanelPresenter.this);
            }
        });
    }

    private MachineDto getDevMachine(List<MachineDto> machines) {
        for (MachineDto machine : machines) {
            if (machine.getConfig().isDev()) {
                return machine;
            }
        }

        throw null;
    }

    @Override
    public void onEnvironmentOutputEvent(EnvironmentOutputEvent event) {
        for (ProcessTreeNode machineNode : machineNodes.values()) {
            if (machineNode.getName().equals(event.getMachineName())) {
                printMachineOutput(machineNode.getId(), event.getContent());
            }
        }
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
        machineServiceClient.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> machines) throws OperationException {
                for (MachineDto machine : machines) {
                    restoreState(machine);
                }
            }
        });
    }

    private void restoreState(final Machine machine) {
        machineServiceClient.getProcesses(machine.getWorkspaceId(), machine.getId()).then(new Operation<List<MachineProcessDto>>() {
            @Override
            public void apply(List<MachineProcessDto> arg) throws OperationException {
                for (MachineProcessDto machineProcessDto : arg) {
                    final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                            .withName(machineProcessDto.getName())
                                                            .withAttributes(machineProcessDto.getAttributes())
                                                            .withCommandLine(machineProcessDto.getCommandLine())
                                                            .withType(machineProcessDto.getType());

                    final CommandType type = commandTypeRegistry.getCommandTypeById(commandDto.getType());
                    if (type != null) {
                        final CommandConfiguration configuration = type.getConfigurationFactory().createFromDto(commandDto);
                        final CommandOutputConsole console = commandConsoleFactory.create(configuration, machine);
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
}
