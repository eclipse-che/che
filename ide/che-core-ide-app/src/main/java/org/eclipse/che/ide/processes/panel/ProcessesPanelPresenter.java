/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.processes.panel;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_SSH_REFERENCE;
import static org.eclipse.che.api.workspace.shared.Constants.SERVER_TERMINAL_REFERENCE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.COMMAND_NODE;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.ROOT_NODE;
import static org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType.TERMINAL_NODE;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.runtime.Machine;
import org.eclipse.che.api.core.model.workspace.runtime.Server;
import org.eclipse.che.api.core.model.workspace.runtime.ServerStatus;
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
import org.eclipse.che.ide.api.command.CommandsLoadedEvent;
import org.eclipse.che.ide.api.command.exec.ExecAgentCommandManager;
import org.eclipse.che.ide.api.command.exec.ProcessFinishedEvent;
import org.eclipse.che.ide.api.command.exec.dto.GetProcessLogsResponseDto;
import org.eclipse.che.ide.api.command.exec.dto.GetProcessesResponseDto;
import org.eclipse.che.ide.api.macro.MacroProcessor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.api.parts.PartStackStateChangedEvent;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.api.workspace.WsAgentServerUtil;
import org.eclipse.che.ide.api.workspace.event.ExecAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.MachineRunningEvent;
import org.eclipse.che.ide.api.workspace.event.MachineStartingEvent;
import org.eclipse.che.ide.api.workspace.event.TerminalAgentServerRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.ServerImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;
import org.eclipse.che.ide.command.toolbar.processes.ActivateProcessOutputEvent;
import org.eclipse.che.ide.command.toolbar.processes.ProcessOutputClosedEvent;
import org.eclipse.che.ide.console.CommandConsoleFactory;
import org.eclipse.che.ide.console.CommandOutputConsole;
import org.eclipse.che.ide.console.CommandOutputConsolePresenter;
import org.eclipse.che.ide.console.CompositeOutputConsole;
import org.eclipse.che.ide.console.DefaultOutputConsole;
import org.eclipse.che.ide.machine.MachineResources;
import org.eclipse.che.ide.processes.DisplayMachineOutputEvent;
import org.eclipse.che.ide.processes.ProcessTreeNode;
import org.eclipse.che.ide.processes.ProcessTreeNode.ProcessNodeType;
import org.eclipse.che.ide.processes.ProcessTreeNodeSelectedEvent;
import org.eclipse.che.ide.processes.actions.AddTabMenuFactory;
import org.eclipse.che.ide.processes.actions.ConsoleTreeContextMenu;
import org.eclipse.che.ide.processes.actions.ConsoleTreeContextMenuFactory;
import org.eclipse.che.ide.processes.loading.WorkspaceLoadingTracker;
import org.eclipse.che.ide.processes.runtime.RuntimeInfo;
import org.eclipse.che.ide.processes.runtime.RuntimeInfoLocalization;
import org.eclipse.che.ide.processes.runtime.RuntimeInfoProvider;
import org.eclipse.che.ide.processes.runtime.RuntimeInfoWidgetFactory;
import org.eclipse.che.ide.terminal.TerminalFactory;
import org.eclipse.che.ide.terminal.TerminalOptionsJso;
import org.eclipse.che.ide.terminal.TerminalPresenter;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.loaders.DownloadWorkspaceOutputEvent;
import org.eclipse.che.ide.ui.multisplitpanel.SubPanel;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGResource;

/** Presenter for the panel for managing processes. */
@Singleton
public class ProcessesPanelPresenter extends BasePresenter
    implements ProcessesPanelView.ActionDelegate,
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
        BasicIDEInitializedEvent.Handler,
        CommandsLoadedEvent.CommandsLoadedHandler {

  public static final String SSH_PORT = "22";
  private static final String DEFAULT_TERMINAL_NAME = "Terminal";
  final Map<String, OutputConsole> consoles;
  final Map<OutputConsole, String> consoleCommands;
  final Map<String, TerminalPresenter> terminals;

  private final ProcessesPanelView view;
  private final CoreLocalizationConstant localizationConstant;
  private final MachineResources resources;
  private final Provider<WorkspaceAgent> workspaceAgentProvider;
  private final CommandManager commandManager;
  private final SshServiceClient sshServiceClient;
  private final AppContext appContext;
  private final NotificationManager notificationManager;
  private final TerminalFactory terminalFactory;
  private final CommandConsoleFactory commandConsoleFactory;
  private final DialogFactory dialogFactory;
  private final ConsoleTreeContextMenuFactory consoleTreeContextMenuFactory;
  private final AddTabMenuFactory addTabMenuFactory;
  private final CommandTypeRegistry commandTypeRegistry;
  private final ExecAgentCommandManager execAgentCommandManager;
  private final Provider<MacroProcessor> macroProcessorProvider;
  private final RuntimeInfoWidgetFactory runtimeInfoWidgetFactory;
  private final RuntimeInfoProvider runtimeInfoProvider;
  private final RuntimeInfoLocalization runtimeInfoLocalization;
  private final WsAgentServerUtil wsAgentServerUtil;
  private final EventBus eventBus;
  private final Map<String, ProcessTreeNode> machineNodes;
  ProcessTreeNode rootNode;
  private ProcessTreeNode contextTreeNode;
  private Map<String, MachineImpl> machines;

  @Inject
  public ProcessesPanelPresenter(
      ProcessesPanelView view,
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
      AddTabMenuFactory addTabMenuFactory,
      CommandManager commandManager,
      CommandTypeRegistry commandTypeRegistry,
      SshServiceClient sshServiceClient,
      ExecAgentCommandManager execAgentCommandManager,
      Provider<MacroProcessor> macroProcessorProvider,
      RuntimeInfoWidgetFactory runtimeInfoWidgetFactory,
      RuntimeInfoProvider runtimeInfoProvider,
      RuntimeInfoLocalization runtimeInfoLocalization,
      Provider<WorkspaceLoadingTracker> workspaceLoadingTrackerProvider,
      WsAgentServerUtil wsAgentServerUtil) {
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
    this.addTabMenuFactory = addTabMenuFactory;
    this.eventBus = eventBus;
    this.commandTypeRegistry = commandTypeRegistry;
    this.execAgentCommandManager = execAgentCommandManager;
    this.macroProcessorProvider = macroProcessorProvider;
    this.runtimeInfoWidgetFactory = runtimeInfoWidgetFactory;
    this.runtimeInfoProvider = runtimeInfoProvider;
    this.runtimeInfoLocalization = runtimeInfoLocalization;
    this.wsAgentServerUtil = wsAgentServerUtil;

    machineNodes = new HashMap<>();
    machines = new HashMap<>();
    rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, new ArrayList<ProcessTreeNode>());
    terminals = new LinkedHashMap<>(3, 0.75f, true);
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
    eventBus.addHandler(CommandsLoadedEvent.getType(), this);
    eventBus.addHandler(DownloadWorkspaceOutputEvent.TYPE, this);
    eventBus.addHandler(PartStackStateChangedEvent.TYPE, this);
    eventBus.addHandler(
        ActivateProcessOutputEvent.TYPE, event -> setActiveProcessOutput(event.getPid()));
    eventBus.addHandler(BasicIDEInitializedEvent.TYPE, this);
    eventBus.addHandler(DisplayMachineOutputEvent.TYPE, this::displayMachineOutput);

    Scheduler.get().scheduleDeferred(() -> workspaceLoadingTrackerProvider.get().startTracking());

    Scheduler.get().scheduleDeferred(this::updateMachineList);
  }

  protected void displayMachineOutput(DisplayMachineOutputEvent event) {
    String machineName = event.getMachineName();
    OutputConsole outputConsole = consoles.get(machineName);

    if (outputConsole == null) {
      return;
    }

    outputConsole.go(
        widget -> {
          String title = outputConsole.getTitle();
          SVGResource icon = outputConsole.getTitleIcon();
          view.addWidget(machineName, title, icon, widget, true);
          ProcessTreeNode node = view.getNodeById(machineName);
          view.selectNode(node);
          notifyTreeNodeSelected(node);
        });

    outputConsole.addActionDelegate(this);
  }

  /** Updates list of the machines from application context. */
  public void updateMachineList() {
    if (appContext.getWorkspace() == null) {
      return;
    }

    List<MachineImpl> machines = getMachines();
    if (machines.isEmpty()) {
      return;
    }

    Optional<MachineImpl> wsAgentServerMachine = wsAgentServerUtil.getWsAgentServerMachine();

    wsAgentServerMachine.ifPresent(
        machine -> {
          provideMachineNode(machine.getName(), true, false);
          machines.remove(machine);
        });

    for (MachineImpl machine : machines) {
      provideMachineNode(machine.getName(), true, false);
    }

    if (WorkspaceStatus.RUNNING == appContext.getWorkspace().getStatus()) {
      ProcessTreeNode machineToSelect = machineNodes.entrySet().iterator().next().getValue();
      view.selectNode(machineToSelect);
      notifyTreeNodeSelected(machineToSelect);
    }
  }

  /** determines whether process tree is visible. */
  public boolean isProcessesTreeVisible() {
    return view.isProcessesTreeVisible();
  }

  /** Sets visibility for processes tree */
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
    provideMachineNode(event.getMachine().getName(), false, false);
  }

  @Override
  public void onMachineRunning(MachineRunningEvent event) {
    machines.put(event.getMachine().getName(), event.getMachine());
    provideMachineNode(event.getMachine().getName(), true, false);
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

  /**
   * Provides terminal depends on the current state of IDE:
   *
   * <ul>
   *   <li>One or more terminals is present -> activates last active terminal
   *   <li>Terminal is absent -> opens new terminal for the selected machine and activates terminal
   *       tab
   * </ul>
   */
  public void provideTerminal() {
    if (terminals.isEmpty()) {
      newTerminal(TerminalOptionsJso.createDefault(), true);
      return;
    }

    String lastActiveTerminalId = "";
    Iterator<String> iterator = terminals.keySet().iterator();
    while (iterator.hasNext()) {
      lastActiveTerminalId = iterator.next();
    }

    ProcessTreeNode terminalNode = view.getNodeById(lastActiveTerminalId);
    if (terminalNode == null) {
      newTerminal(TerminalOptionsJso.createDefault(), true);
      return;
    }

    workspaceAgentProvider.get().setActivePart(this);
    view.selectNode(terminalNode);
  }

  /** Opens new terminal for the selected machine. */
  public void newTerminal(TerminalOptionsJso options) {
    newTerminal(options, true);
  }

  /** Opens new terminal for the selected machine and activates terminal tab. */
  public void newTerminal(TerminalOptionsJso options, boolean activate) {
    final ProcessTreeNode selectedTreeNode = view.getSelectedTreeNode();

    final Optional<MachineImpl> devMachine = wsAgentServerUtil.getWsAgentServerMachine();

    if (selectedTreeNode == null && devMachine.isPresent()) {
      onAddTerminal(devMachine.get().getName(), options, activate);
      return;
    }

    if (selectedTreeNode == null) {
      String notificationTitle = localizationConstant.failedToConnectTheTerminal();
      String notificationContent = localizationConstant.machineNotFound("");
      notificationManager.notify(notificationTitle, notificationContent, FAIL, FLOAT_MODE);
      return;
    }

    if (selectedTreeNode.getType() == MACHINE_NODE) {
      String machineName = (String) selectedTreeNode.getData();
      onAddTerminal(machineName, options, activate);
      return;
    }

    ProcessTreeNode parent = selectedTreeNode.getParent();
    if (parent != null && parent.getType() == MACHINE_NODE) {
      String machineName = (String) parent.getData();
      onAddTerminal(machineName, options, activate);
    }
  }

  /** Selects dev machine. */
  private void selectDevMachine() {
    Optional<MachineImpl> devMachine = wsAgentServerUtil.getWsAgentServerMachine();
    if (!devMachine.isPresent()) {
      return;
    }

    for (ProcessTreeNode processTreeNode : machineNodes.values()) {
      if (processTreeNode.getType() == MACHINE_NODE) {
        String machineName = (String) processTreeNode.getData();

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
        final int consolePid = ((CommandOutputConsole) console).getPid();
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
   * @param machineId id of machine in which the terminal will be added
   * @param options terminal options
   */
  @Override
  public void onAddTerminal(final String machineId, TerminalOptionsJso options) {
    onAddTerminal(machineId, options, true);
  }

  /**
   * Adds new terminal to the processes panel
   *
   * @param machineId id of machine in which the terminal will be added
   * @param options terminal options
   * @param activate activate terminal tab
   */
  public void onAddTerminal(final String machineId, TerminalOptionsJso options, boolean activate) {
    final MachineImpl machine = getMachine(machineId);
    if (machine == null) {
      notificationManager.notify(
          localizationConstant.failedToConnectTheTerminal(),
          localizationConstant.machineNotFound(machineId),
          FAIL,
          FLOAT_MODE);
      Log.error(getClass(), localizationConstant.machineNotFound(machineId));
      return;
    }

    final ProcessTreeNode machineTreeNode = provideMachineNode(machine.getName(), false, false);
    if (machineTreeNode == null) {
      return;
    }

    final TerminalPresenter newTerminal = terminalFactory.create(machine, options);
    final IsWidget terminalWidget = newTerminal.getView();
    final String terminalName = getUniqueTerminalName(machineTreeNode);
    final ProcessTreeNode terminalNode =
        new ProcessTreeNode(
            TERMINAL_NODE, machineTreeNode, terminalName, resources.terminalTreeIcon(), null);
    addChildToMachineNode(terminalNode, machineTreeNode, activate);

    final String terminalId = terminalNode.getId();
    terminals.put(terminalId, newTerminal);
    view.addProcessNode(terminalNode);
    terminalWidget.asWidget().ensureDebugId(terminalName);
    view.addWidget(terminalId, terminalName, terminalNode.getTitleIcon(), terminalWidget, true);
    refreshStopButtonState(terminalId);

    workspaceAgentProvider.get().setActivePart(this);

    newTerminal.setVisible(true);
    newTerminal.connect();
    newTerminal.setListener(() -> onCloseTerminal(terminalNode));
  }

  @Override
  public void onPreviewSsh(String machineId) {
    ProcessTreeNode machineTreeNode = findTreeNodeById(machineId);
    if (machineTreeNode == null || machineTreeNode.getType() != MACHINE_NODE) {
      return;
    }

    String machineName = (String) machineTreeNode.getData();
    RuntimeImpl runtime = appContext.getWorkspace().getRuntime();
    if (runtime == null) {
      return;
    }

    Optional<MachineImpl> machine = runtime.getMachineByName(machineName);
    if (!machine.isPresent()) {
      return;
    }

    final OutputConsole defaultConsole = commandConsoleFactory.create("SSH");
    addCommandOutput(machineId, defaultConsole, true);

    String sshServiceAddress = getSshServerAddress(machine.get());
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
    String user = machine.get().getAttributes().get("config.user");
    if (isNullOrEmpty(user)) {
      userName = "root";
    } else {
      userName = user;
    }

    // ssh key
    final String workspaceName = appContext.getWorkspace().getConfig().getName();
    Promise<SshPairDto> sshPairDtoPromise =
        sshServiceClient.getPair("workspace", appContext.getWorkspaceId());

    sshPairDtoPromise
        .then(
            new Operation<SshPairDto>() {
              @Override
              public void apply(SshPairDto sshPairDto) throws OperationException {
                if (defaultConsole instanceof DefaultOutputConsole) {
                  ((DefaultOutputConsole) defaultConsole).enableAutoScroll(false);
                  ((DefaultOutputConsole) defaultConsole)
                      .printText(
                          localizationConstant.sshConnectInfo(
                              machineName,
                              machineHost,
                              sshPort,
                              workspaceName,
                              userName,
                              localizationConstant.sshConnectInfoPrivateKey(
                                  sshPairDto.getPrivateKey())));
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                if (defaultConsole instanceof DefaultOutputConsole) {
                  ((DefaultOutputConsole) defaultConsole).enableAutoScroll(false);
                  ((DefaultOutputConsole) defaultConsole)
                      .printText(
                          localizationConstant.sshConnectInfo(
                              machineName,
                              machineHost,
                              sshPort,
                              workspaceName,
                              userName,
                              localizationConstant.sshConnectInfoNoPrivateKey()));
                }
              }
            });
  }

  @Override
  public void onPreviewServers(String machineId) {
    ProcessTreeNode machineTreeNode = findTreeNodeById(machineId);
    if (machineTreeNode == null || machineTreeNode.getType() != MACHINE_NODE) {
      return;
    }

    String machineName = (String) machineTreeNode.getData();
    RuntimeImpl runtime = appContext.getWorkspace().getRuntime();
    if (runtime == null) {
      return;
    }

    Optional<MachineImpl> machine = runtime.getMachineByName(machineName);
    if (!machine.isPresent()) {
      return;
    }

    List<RuntimeInfo> serverBindings = runtimeInfoProvider.get(machineName);
    Widget widget = runtimeInfoWidgetFactory.create(machineName, serverBindings);

    CompositeOutputConsole servers =
        commandConsoleFactory.create(
            widget, runtimeInfoLocalization.infoTabTitle(), resources.remote());
    addCommandOutput(machineId, servers, true);
  }

  @Override
  public void onTreeNodeSelected(final ProcessTreeNode node) {
    setSelection(new Selection.NoSelectionProvided());

    if (node != null) {
      String nodeId = node.getId();
      ProcessNodeType nodeType = node.getType();
      switch (nodeType) {
        case MACHINE_NODE:
          final MachineImpl machine = getMachine(nodeId);
          if (machine != null) {
            setSelection(new Selection<>(machine));
          }

          view.showProcessOutput(node.getName());
          break;
        case TERMINAL_NODE:
          terminals.get(nodeId);
          // fall through
        default:
          view.showProcessOutput(nodeId);
          refreshStopButtonState(nodeId);
      }
    }

    notifyTreeNodeSelected(node);
  }

  /**
   * Returns the ssh service address in format - host:port (example - localhost:32899)
   *
   * @param machine machine to retrieve address
   * @return ssh service address in format host:port
   */
  private String getSshServerAddress(MachineImpl machine) {
    Optional<ServerImpl> server = machine.getServerByName(SERVER_SSH_REFERENCE);
    if (server.isPresent()) {
      return server.get().getUrl();
    }

    return null;
  }

  /**
   * Adds command node to process tree and displays command output
   *
   * @param outputConsole the console for command output
   */
  public void addCommandOutput(OutputConsole outputConsole) {
    Optional<MachineImpl> devMachine = wsAgentServerUtil.getWsAgentServerMachine();
    devMachine.ifPresent(machine -> addCommandOutput(machine.getName(), outputConsole, true));
  }

  /**
   * Adds command node to process tree and displays command output
   *
   * @param machineId id of machine in which the command will be executed
   * @param outputConsole the console for command output
   * @param activate activate terminal tab
   */
  public void addCommandOutput(String machineId, OutputConsole outputConsole, boolean activate) {
    ProcessTreeNode machineTreeNode = findTreeNodeById(machineId);
    if (machineTreeNode == null) {
      notificationManager.notify(
          localizationConstant.failedToExecuteCommand(),
          localizationConstant.machineNotFound(machineId),
          FAIL,
          FLOAT_MODE);
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

    ProcessTreeNode commandNode =
        new ProcessTreeNode(
            COMMAND_NODE, machineTreeNode, outputConsoleTitle, outputConsole.getTitleIcon(), null);
    commandId = commandNode.getId();
    addChildToMachineNode(commandNode, machineTreeNode, activate);

    addOutputConsole(commandId, commandNode, outputConsole, true, activate);

    refreshStopButtonState(commandId);
    workspaceAgentProvider.get().setActivePart(this);
  }

  @Nullable
  private ProcessTreeNode getProcessTreeNodeByName(
      String processName, ProcessTreeNode machineTreeNode) {
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

  private void addOutputConsole(
      final String id,
      final ProcessTreeNode processNode,
      final OutputConsole outputConsole,
      final boolean removable,
      final boolean activate) {
    consoles.put(id, outputConsole);
    consoleCommands.put(outputConsole, id);

    outputConsole.go(
        new AcceptsOneWidget() {
          @Override
          public void setWidget(final IsWidget widget) {
            view.addProcessNode(processNode);
            view.addWidget(
                id, outputConsole.getTitle(), outputConsole.getTitleIcon(), widget, removable);
            if (!MACHINE_NODE.equals(processNode.getType())) {
              ProcessTreeNode node = view.getNodeById(id);
              if (activate) {
                view.selectNode(node);
                notifyTreeNodeSelected(node);
              }
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
    closeCommandOutput(node, () -> {});
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
      dialogFactory
          .createConfirmDialog(
              "",
              localizationConstant.outputsConsoleViewStopProcessConfirmation(console.getTitle()),
              getConfirmCloseConsoleCallback(console, node, removeCallback),
              null)
          .show();
    }
  }

  private ConfirmCallback getConfirmCloseConsoleCallback(
      final OutputConsole console,
      final ProcessTreeNode node,
      final SubPanel.RemoveCallback removeCallback) {
    return () -> {
      console.stop();
      removeConsole(console, node, removeCallback);
    };
  }

  private void removeConsole(
      final OutputConsole console,
      final ProcessTreeNode node,
      final SubPanel.RemoveCallback removeCallback) {
    console.close();
    onStopProcess(node);

    consoles.remove(node.getId());
    consoleCommands.remove(console);
    view.removeWidget(node.getId());

    removeCallback.remove();

    if (console instanceof CommandOutputConsole) {
      eventBus.fireEvent(new ProcessOutputClosedEvent(((CommandOutputConsole) console).getPid()));
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

  private void addChildToMachineNode(
      final ProcessTreeNode childNode,
      final ProcessTreeNode machineTreeNode,
      final boolean activate) {
    machineTreeNode.getChildren().add(childNode);
    view.setProcessesData(rootNode);

    if (activate) {
      view.selectNode(childNode);
    }

    notifyTreeNodeSelected(childNode);
  }

  private void removeChildFromMachineNode(
      ProcessTreeNode childNode, ProcessTreeNode machineTreeNode) {
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

  /** Checks whether the server is running in the machine. */
  private boolean isServerRunning(String machineName, String serverName) {
    Workspace workspace = appContext.getWorkspace();
    Runtime runtime = workspace.getRuntime();
    if (runtime == null) {
      return false;
    }

    Machine machine = runtime.getMachines().get(machineName);
    if (machine == null) {
      return false;
    }

    Server terminalServer = machine.getServers().get(serverName);
    if (terminalServer == null) {
      return false;
    }

    return terminalServer.getStatus() == ServerStatus.RUNNING;
  }

  /**
   * Provides machine node:
   * <li>creates new machine node when this one not exist or {@code replace} is {@code true}
   * <li>returns old machine node when this one exist and {@code replace} is {@code false}
   *
   * @param machineName name of the machine to creating node
   * @param replace existed node will be replaced when {@code replace} is {@code true}
   * @param activate activate machine node
   * @return machine node
   */
  @Nullable
  private ProcessTreeNode provideMachineNode(
      String machineName, boolean replace, boolean activate) {
    final ProcessTreeNode existedMachineNode = findTreeNodeById(machineName);
    if (!replace && existedMachineNode != null) {
      return existedMachineNode;
    }

    // we need to keep old machine node children
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

    ProcessTreeNode machineNode =
        new ProcessTreeNode(MACHINE_NODE, rootNode, machineName, null, children);

    machineNode.setTerminalServerRunning(isServerRunning(machineName, SERVER_TERMINAL_REFERENCE));

    // rely on "wsagent" server's status since "ssh" server's status is always UNKNOWN
    String wsAgentServerRef = wsAgentServerUtil.getWsAgentHttpServerReference();
    machineNode.setSshServerRunning(isServerRunning(machineName, wsAgentServerRef));

    for (ProcessTreeNode child : children) {
      child.setParent(machineNode);
    }

    machineNodes.put(machineName, machineNode);

    // add to children
    rootNode.getChildren().add(machineNode);

    // update the view
    view.setProcessesData(rootNode);

    // add output for the machine if it is not exist
    if (!consoles.containsKey(machineName)) {
      OutputConsole outputConsole = commandConsoleFactory.create(machineName);
      addOutputConsole(machineName, machineNode, outputConsole, true, activate);
    }

    return machineNode;
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
  public void onEnvironmentOutput(EnvironmentOutputEvent event) {
    printMachineOutput(event.getMachineName(), event.getContent());
  }

  @Override
  public void onWorkspaceRunning(WorkspaceRunningEvent event) {}

  @Override
  public void onWorkspaceStopped(WorkspaceStoppedEvent event) {
    try {
      for (ProcessTreeNode node : rootNode.getChildren()) {
        if (MACHINE_NODE == node.getType()) {
          ArrayList<ProcessTreeNode> children = new ArrayList<>();
          children.addAll(node.getChildren());

          for (ProcessTreeNode child : children) {
            if (COMMAND_NODE == child.getType()) {
              OutputConsole console = consoles.get(child.getId());
              removeConsole(console, child, () -> {});
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
      selectDevMachine();
      TerminalOptionsJso options = TerminalOptionsJso.createDefault().withFocusOnOpen(false);
      newTerminal(options);
    }
  }

  @Override
  public void onTerminalAgentServerRunning(TerminalAgentServerRunningEvent event) {
    // open terminal automatically for dev-machine only
    Optional<MachineImpl> devMachine = wsAgentServerUtil.getWsAgentServerMachine();

    if (devMachine.isPresent() && event.getMachineName().equals(devMachine.get().getName())) {
      provideMachineNode(event.getMachineName(), true, false);

      TerminalOptionsJso options = TerminalOptionsJso.createDefault().withFocusOnOpen(false);
      newTerminal(options, false);
    }
  }

  @Override
  public void onExecAgentServerRunning(ExecAgentServerRunningEvent event) {
    restoreProcessesState(event.getMachineName());
  }

  @Override
  public void onCommandsLoaded(CommandsLoadedEvent event) {
    if (appContext.getWorkspace().getStatus() == RUNNING) {
      getMachines().stream().map(MachineImpl::getName).forEach(this::restoreProcessesState);
    }
  }

  private void restoreProcessesState(String machineName) {
    execAgentCommandManager
        .getProcesses(machineName, false)
        .onSuccess(
            new BiConsumer<String, List<GetProcessesResponseDto>>() {
              @Override
              public void accept(String endpointId, List<GetProcessesResponseDto> processes) {
                for (GetProcessesResponseDto process : processes) {
                  final int pid = process.getPid();
                  final String type = process.getType();

                  /*
                   * Do not show the process if the command line has prefix #hidden
                   */
                  if (!isNullOrEmpty(process.getCommandLine())
                      && process.getCommandLine().startsWith("#hidden")) {
                    continue;
                  }

                  /*
                   * Hide the processes which are launched by command of unknown type
                   */
                  if (commandTypeRegistry.getCommandTypeById(type).isPresent()) {
                    final String processName = process.getName();
                    final Optional<CommandImpl> commandOptional =
                        commandManager.getCommand(processName);

                    if (!commandOptional.isPresent()) {
                      final String commandLine = process.getCommandLine();
                      final CommandImpl command = new CommandImpl(processName, commandLine, type);
                      final CommandOutputConsole console =
                          commandConsoleFactory.create(command, machineName);

                      getAndPrintProcessLogs(console, pid);
                      subscribeToProcess(console, pid);

                      addCommandOutput(machineName, console, true);
                    } else {
                      final CommandImpl commandByName = commandOptional.get();
                      macroProcessorProvider
                          .get()
                          .expandMacros(commandByName.getCommandLine())
                          .then(
                              new Operation<String>() {
                                @Override
                                public void apply(String expandedCommandLine)
                                    throws OperationException {
                                  final CommandImpl command =
                                      new CommandImpl(
                                          commandByName.getName(),
                                          expandedCommandLine,
                                          commandByName.getType(),
                                          commandByName.getAttributes());

                                  final CommandOutputConsole console =
                                      commandConsoleFactory.create(command, machineName);

                                  getAndPrintProcessLogs(console, pid);
                                  subscribeToProcess(console, pid);

                                  addCommandOutput(machineName, console, true);
                                }
                              });
                    }
                  }
                }
              }

              private void getAndPrintProcessLogs(
                  final CommandOutputConsole console, final int pid) {
                String from = null;
                String till = null;
                int limit = 50;
                int skip = 0;
                execAgentCommandManager
                    .getProcessLogs(machineName, pid, from, till, limit, skip)
                    .onSuccess(
                        logs -> {
                          for (GetProcessLogsResponseDto log : logs) {
                            String text = log.getText();
                            console.printOutput(text);
                          }
                        })
                    .onFailure(
                        (s, error) ->
                            Log.error(
                                getClass(),
                                "Error trying to get process log with pid: "
                                    + pid
                                    + ". "
                                    + error.getMessage()));
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
            })
        .onFailure(
            (endpointId, error) ->
                notificationManager.notify(localizationConstant.failedToGetProcesses(machineName)));
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
    if (partStack.equals(event.getPartStack())
        && partStack.getPartStackState() == PartStack.State.NORMAL) {
      view.setProcessesTreeVisible(true);
    }
  }

  /**
   * Prints text to the machine console.
   *
   * @param machineName machine name
   * @param text text to be printed
   */
  public void printMachineOutput(final String machineName, final String text) {
    // Create a temporary machine node to display outputs.

    if (!consoles.containsKey(machineName)) {
      provideMachineNode(machineName, true, false);
    }

    OutputConsole console = consoles.get(machineName);
    if (console != null && console instanceof DefaultOutputConsole) {
      ((DefaultOutputConsole) console).printText(text);
    }
  }

  /**
   * Prints text to the machine console.
   *
   * @param machineName machine name
   * @param text text to be printed
   * @param color color of the text or NULL
   */
  public void printMachineOutput(String machineName, String text, String color) {
    OutputConsole console = consoles.get(machineName);
    if (console != null && console instanceof DefaultOutputConsole) {
      ((DefaultOutputConsole) console).printText(text, color);
    }
  }

  /**
   * Returns the console text for the specified machine.
   *
   * @param machineId machine ID
   * @return console text or NULL if there is no machine with specified ID
   */
  public String getText(String machineId) {
    OutputConsole console = consoles.get(machineId);
    if (console == null) {
      return null;
    }

    if (console instanceof DefaultOutputConsole) {
      return ((DefaultOutputConsole) console).getText();
    } else if (console instanceof CommandOutputConsolePresenter) {
      return ((CommandOutputConsolePresenter) console).getText();
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
   * @param node selected node
   */
  private void notifyTreeNodeSelected(final ProcessTreeNode node) {
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
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

    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                contextTreeNode = node;
                ConsoleTreeContextMenu contextMenu =
                    consoleTreeContextMenuFactory.newContextMenu(node);
                contextMenu.show(mouseX, mouseY);
              }
            });
  }

  @Override
  public void onAddTabButtonClicked(final int mouseX, final int mouseY) {
    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                addTabMenuFactory.newAddTabMenu().show(mouseX, mouseY);
              }
            });
  }

  @Override
  public void onDownloadWorkspaceOutput(DownloadWorkspaceOutputEvent event) {
    Optional<MachineImpl> devMachine = wsAgentServerUtil.getWsAgentServerMachine();

    if (!devMachine.isPresent()) {
      return;
    }

    WorkspaceImpl workspace = appContext.getWorkspace();
    String fileName =
        workspace.getNamespace()
            + "-"
            + workspace.getConfig().getName()
            + " "
            + DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
            + ".log";

    download(fileName, getText(devMachine.get().getName()));
  }

  @Override
  public void onDownloadOutput(OutputConsole console) {
    String id = consoleCommands.get(console);

    String fileName =
        appContext.getWorkspace().getNamespace()
            + "-"
            + appContext.getWorkspace().getConfig().getName()
            + " "
            + DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
            + ".log";
    download(fileName, getText(id));
  }

  /**
   * Invokes the browser to download a file.
   *
   * @param fileName file name
   * @param text file content
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
