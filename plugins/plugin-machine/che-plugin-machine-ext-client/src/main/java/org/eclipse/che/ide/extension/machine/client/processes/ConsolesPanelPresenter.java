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
package org.eclipse.che.ide.extension.machine.client.processes;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

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
import org.eclipse.che.ide.api.machine.events.DevMachineStateEvent;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.HasView;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStartingEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.machine.MachineStateEvent;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.DefaultOutputConsole;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenu;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenuFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter.TerminalStateListener;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.COMMAND_NODE;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.ROOT_NODE;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.TERMINAL_NODE;

/**
 * Presenter for managing machines process and terminals.
 *
 * @author Anna Shumilova
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class ConsolesPanelPresenter extends BasePresenter implements ConsolesPanelView.ActionDelegate,
                                                                     HasView,
                                                                     ProcessFinishedEvent.Handler,
                                                                     OutputConsole.ConsoleOutputListener,
                                                                     WorkspaceStartingEvent.Handler,
                                                                     WorkspaceStoppedEvent.Handler,
                                                                     MachineStateEvent.Handler,
                                                                     DevMachineStateEvent.Handler {

    private static final String DEFAULT_TERMINAL_NAME = "Terminal";

    public static final String SSH_PORT = "22";

    private final DtoFactory                   dtoFactory;
    private final DialogFactory                dialogFactory;
    private final EntityFactory                entityFactory;
    private final TerminalFactory              terminalFactory;
    private final CommandConsoleFactory        commandConsoleFactory;
    private final NotificationManager          notificationManager;
    private final MachineLocalizationConstant  localizationConstant;
    private final ConsolesPanelView            view;
    private final MachineResources             resources;
    private final AppContext                   appContext;
    private final MachineServiceClient         machineService;
    private final WorkspaceAgent               workspaceAgent;
    private final CommandTypeRegistry          commandTypeRegistry;
    private final Map<String, ProcessTreeNode> machineNodes;

    final List<ProcessTreeNode>                rootNodes;
    final Map<String, TerminalPresenter>       terminals;
    final Map<String, OutputConsole>           consoles;
    final Map<OutputConsole, String>           consoleCommands;

    private OutputConsole                      workspaceConsole;

    ProcessTreeNode                            rootNode;
    ProcessTreeNode                            selectedTreeNode;
    ProcessTreeNode                            contextTreeNode;

    private ConsoleTreeContextMenuFactory      consoleTreeContextMenuFactory;

    @Inject
    public ConsolesPanelPresenter(ConsolesPanelView view,
                                  EventBus eventBus,
                                  DtoFactory dtoFactory,
                                  DialogFactory dialogFactory,
                                  EntityFactory entityFactory,
                                  TerminalFactory terminalFactory,
                                  CommandConsoleFactory commandConsoleFactory,
                                  CommandTypeRegistry commandTypeRegistry,
                                  WorkspaceAgent workspaceAgent,
                                  NotificationManager notificationManager,
                                  MachineLocalizationConstant localizationConstant,
                                  MachineServiceClient machineService,
                                  MachineResources resources,
                                  AppContext appContext,
                                  ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory) {
        this.view = view;
        this.terminalFactory = terminalFactory;
        this.workspaceAgent = workspaceAgent;
        this.commandConsoleFactory = commandConsoleFactory;
        this.commandTypeRegistry = commandTypeRegistry;
        this.dtoFactory = dtoFactory;
        this.dialogFactory = dialogFactory;
        this.notificationManager = notificationManager;
        this.localizationConstant = localizationConstant;
        this.resources = resources;
        this.entityFactory = entityFactory;
        this.appContext = appContext;
        this.machineService = machineService;
        this.consoleTreeContextMenuFactory = consoleTreeContextMenuFactory;

        this.rootNodes = new ArrayList<>();
        this.terminals = new HashMap<>();
        this.consoles = new HashMap<>();
        this.consoleCommands = new HashMap<>();
        this.machineNodes = new HashMap<>();

        this.view.setDelegate(this);
        this.view.setTitle(localizationConstant.viewConsolesTitle());

        eventBus.addHandler(ProcessFinishedEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStartingEvent.TYPE, this);
        eventBus.addHandler(WorkspaceStoppedEvent.TYPE, this);
        eventBus.addHandler(MachineStateEvent.TYPE, this);
        eventBus.addHandler(DevMachineStateEvent.TYPE, this);

        rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, rootNodes);

        workspaceConsole = commandConsoleFactory.create("");
        updateCommandOutput("", workspaceConsole);

        fetchMachines();
    }

    @Override
    public View getView() {
        return view;
    }

    @NotNull
    @Override
    public String getTitle() {
        return localizationConstant.viewConsolesTitle();
    }

    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return resources.terminal();
    }

    @Override
    public String getTitleToolTip() {
        return localizationConstant.viewProcessesTooltip();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onMachineCreating(MachineStateEvent event) {
        workspaceAgent.setActivePart(this);
    }

    @Override
    public void onMachineRunning(MachineStateEvent event) {
        workspaceAgent.setActivePart(this);

        machineService.getMachine(event.getMachineId()).then(new Operation<MachineDto>() {
            @Override
            public void apply(MachineDto machine) throws OperationException {
                addMachineNode(machine);
            }
        });
    }

    @Override
    public void onMachineDestroyed(MachineStateEvent event) {
        String destroyedMachineId = event.getMachineId();

        ProcessTreeNode destroyedMachineNode = machineNodes.get(destroyedMachineId);
        if (destroyedMachineNode == null) {
            return;
        }

        rootNodes.remove(destroyedMachineNode);
        onCloseTerminal(destroyedMachineNode);
        onStopCommandProcess(destroyedMachineNode);

        view.setProcessesData(rootNode);
    }

    @Override
    public void onDevMachineStarted(DevMachineStateEvent event) {
    }

    @Override
    public void onDevMachineDestroyed(DevMachineStateEvent event) {
    }

    /** Get the list of all available machines. */
    public void fetchMachines() {
        machineService.getMachines(appContext.getWorkspaceId()).then(new Operation<List<MachineDto>>() {
            @Override
            public void apply(List<MachineDto> machines) throws OperationException {
                MachineDto devMachine = getDevMachine(machines);
                ProcessTreeNode devMachineTreeNode = addMachineNode(devMachine);

                machines.remove(devMachine);

                for (MachineDto machine : machines) {
                    addMachineNode(machine);
                }

                view.selectNode(devMachineTreeNode);

                workspaceAgent.setActivePart(ConsolesPanelPresenter.this);
            }
        });
    }

    private MachineDto getDevMachine(List<MachineDto> machines) {
        for (MachineDto machine : machines) {
            if (machine.getConfig().isDev()) {
                return machine;
            }
        }

        throw new IllegalArgumentException("Dev machine can not be null");
    }

    public void printDevMachineOutput(String text) {
        OutputConsole console = consoles.get("");
        if (console != null && console instanceof DefaultOutputConsole) {
            ((DefaultOutputConsole)console).printText(text);
        }
    }

    private ProcessTreeNode addMachineNode(MachineDto machine) {
        List<ProcessTreeNode> processTreeNodes = new ArrayList<ProcessTreeNode>();

        ProcessTreeNode machineNode = new ProcessTreeNode(MACHINE_NODE, rootNode, machine, null, processTreeNodes);
        machineNode.setRunning(true);
        machineNodes.put(machine.getId(), machineNode);

        if (rootNodes.contains(machineNode)) {
            rootNodes.remove(machineNode);
        }

        rootNodes.add(machineNode);

        view.setProcessesData(rootNode);

        restoreState(machine);

        return machineNode;
    }

    private void restoreState(final org.eclipse.che.api.core.model.machine.Machine machine) {
        machineService.getProcesses(machine.getId()).then(new Operation<List<MachineProcessDto>>() {
            @Override
            public void apply(List<MachineProcessDto> arg) throws OperationException {
                for (MachineProcessDto machineProcessDto : arg) {
                    final CommandDto commandDto = dtoFactory.createDto(CommandDto.class)
                                                            .withName(machineProcessDto.getName())
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

    /**
     * Adds command node to process tree and displays command output
     *
     * @param machineId
     *         id of machine in which the command will be executed
     * @param outputConsole
     *         the console for command output
     */
    public void addCommandOutput(@NotNull String machineId, @NotNull OutputConsole outputConsole) {
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
            commandId = processTreeNode.getId();
            view.hideProcessOutput(commandId);
        } else {
            ProcessTreeNode commandNode =
                    new ProcessTreeNode(COMMAND_NODE, machineTreeNode, outputConsoleTitle, outputConsole.getTitleIcon(), null);
            commandId = commandNode.getId();
            view.addProcessNode(commandNode);
            addChildToMachineNode(commandNode, machineTreeNode);
        }

        updateCommandOutput(commandId, outputConsole);

        resfreshStopButtonState(commandId);
        workspaceAgent.setActivePart(this);
    }

    private void updateCommandOutput(@NotNull final String command, @NotNull OutputConsole outputConsole) {
        consoles.put(command, outputConsole);
        consoleCommands.put(outputConsole, command);

        outputConsole.go(new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget widget) {
                view.addProcessWidget(command, widget);
                view.selectNode(view.getNodeById(command));
            }
        });

        outputConsole.addOutputListener(this);
    }

    /**
     * Opens new terminal for the selected machine.
     */
    public void newTerminal() {
        workspaceAgent.setActivePart(this);

        if (selectedTreeNode == null) {
            if (appContext.getDevMachine() != null) {
                onAddTerminal(appContext.getDevMachine().getId());
            }
            return;
        }

        if (selectedTreeNode.getType() == MACHINE_NODE) {
            onAddTerminal(selectedTreeNode.getId());
        } else {
            if (selectedTreeNode.getParent() != null &&
                selectedTreeNode.getParent().getType() == MACHINE_NODE) {
                onAddTerminal(appContext.getDevMachine().getId());
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
    public void onAddTerminal(@NotNull final String machineId) {
        machineService.getMachine(machineId).then(new Operation<MachineDto>() {
            @Override
            public void apply(MachineDto arg) throws OperationException {
                Machine machine = entityFactory.createMachine(arg);
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
                final ProcessTreeNode terminalNode =
                        new ProcessTreeNode(TERMINAL_NODE, machineTreeNode, terminalName, resources.terminalTreeIcon(), null);
                addChildToMachineNode(terminalNode, machineTreeNode);

                final String terminalId = terminalNode.getId();
                terminals.put(terminalId, newTerminal);
                view.addProcessNode(terminalNode);
                view.addProcessWidget(terminalId, terminalWidget);
                resfreshStopButtonState(terminalId);

                newTerminal.setVisible(true);
                newTerminal.connect();
                newTerminal.setListener(new TerminalStateListener() {
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
    public void onPreviewSsh(@NotNull final String machineId) {
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

    @Override
    public void onCloseTerminal(@NotNull ProcessTreeNode node) {
        String terminalId = node.getId();
        if (terminals.containsKey(terminalId)) {
            onStopProcess(node);
            terminals.get(terminalId).stopTerminal();
            terminals.remove(terminalId);
        }
    }

    @Override
    public void onTreeNodeSelected(@NotNull ProcessTreeNode node) {
        selectedTreeNode = node;

        view.showProcessOutput(node.getId());
        resfreshStopButtonState(node.getId());
    }

    /**
     * Returns currently selected process tree node.
     *
     * @return
     *         selected tree node
     */
    public ProcessTreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    @Override
    public void onStopCommandProcess(@NotNull ProcessTreeNode node) {
        String commandId = node.getId();
        if (consoles.containsKey(commandId) && !consoles.get(commandId).isFinished()) {
            consoles.get(commandId).stop();
        }
    }

    @Override
    public void onCloseCommandOutputClick(@NotNull ProcessTreeNode node) {
        String commandId = node.getId();
        OutputConsole console = consoles.get(commandId);

        if (console == null) {
            return;
        }

        if (console.isFinished()) {
            console.close();
            onStopProcess(node);
            consoles.remove(commandId);
            consoleCommands.remove(console);
            return;
        }

        dialogFactory.createConfirmDialog("", localizationConstant.outputsConsoleViewStopProcessConfirmation(console.getTitle()),
                                          getConfirmCloseConsoleCallback(console, node), null)
                     .show();
    }

    /**
     * Returns context selected tree node.
     *
     * @return
     *      tree node
     */
    public ProcessTreeNode getContextTreeNode() {
        return contextTreeNode;
    }

    /**
     * Returns context selected output console.
     *
     * @return
     *          output console
     */
    public OutputConsole getContextOutputConsole() {
        if (contextTreeNode == null) {
            return null;
        }

        return consoles.get(contextTreeNode.getId());
    }

    @Override
    public void onContextMenu(int mouseX, int mouseY, ProcessTreeNode node) {
        contextTreeNode = node;

        ConsoleTreeContextMenu contextMenu = consoleTreeContextMenuFactory.newContextMenu(node);
        contextMenu.show(mouseX, mouseY);
    }

    private ConfirmCallback getConfirmCloseConsoleCallback(final OutputConsole console, final ProcessTreeNode node) {
        return new ConfirmCallback() {
            @Override
            public void accepted() {
                console.stop();
                onStopProcess(node);

                console.close();
                consoles.remove(node.getId());
                consoleCommands.remove(console);
            }
        };
    }

    private void onStopProcess(@NotNull ProcessTreeNode node) {
        String processId = node.getId();
        ProcessTreeNode parentNode = node.getParent();

        int processIndex = view.getNodeIndex(processId);
        if (processIndex < 0) {
            return;
        }

        int countWidgets = terminals.size() + consoles.size();
        if (countWidgets <= 2) {
            view.hideProcessOutput(processId);
            removeChildFromMachineNode(node, parentNode);
            return;
        }

        int neighborIndex = processIndex > 0 ? processIndex - 1 : processIndex + 1;
        ProcessTreeNode neighborNode = view.getNodeByIndex(neighborIndex);
        String neighborNodeId = neighborNode.getId();

        removeChildFromMachineNode(node, parentNode);
        view.selectNode(neighborNode);
        resfreshStopButtonState(neighborNodeId);
        view.showProcessOutput(neighborNodeId);
        view.hideProcessOutput(processId);
    }

    private void resfreshStopButtonState(String selectedNodeId) {
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

    private ProcessTreeNode findProcessTreeNodeById(@NotNull String id) {
        for (ProcessTreeNode processTreeNode : rootNode.getChildren()) {
            if (id.equals(processTreeNode.getId())) {
                return processTreeNode;
            }
        }
        return null;
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

    private ProcessTreeNode getProcessTreeNodeByName(String processName, ProcessTreeNode machineTreeNode) {
        for (ProcessTreeNode processTreeNode : machineTreeNode.getChildren()) {
            if (processTreeNode.getName().equals(processName)) {
                return processTreeNode;
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

    private boolean isCommandStopped(String commandId) {
        return consoles.containsKey(commandId) && consoles.get(commandId).isFinished();
    }

    @Override
    public void onConsoleOutput(OutputConsole console) {
        String command = consoleCommands.get(console);
        if (command != null) {
            view.markProcessHasOutput(command);
        }
    }

    @Override
    public void onWorkspaceStarting(WorkspaceStartingEvent event) {
        workspaceConsole = commandConsoleFactory.create("");
        updateCommandOutput("", workspaceConsole);

        fetchMachines();
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

}
