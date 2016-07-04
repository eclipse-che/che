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
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.MachineStatus;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.events.DevMachineStateEvent;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.machine.shared.dto.MachineProcessDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.extension.machine.client.inject.factories.EntityFactory;
import org.eclipse.che.ide.extension.machine.client.inject.factories.TerminalFactory;
import org.eclipse.che.ide.extension.machine.client.machine.Machine;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandConsoleFactory;
import org.eclipse.che.ide.extension.machine.client.outputspanel.console.CommandOutputConsole;
import org.eclipse.che.ide.extension.machine.client.perspective.terminal.TerminalPresenter;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.extension.machine.client.processes.actions.ConsoleTreeContextMenuFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.COMMAND_NODE;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.MACHINE_NODE;
import static org.eclipse.che.ide.extension.machine.client.processes.ProcessTreeNode.ProcessNodeType.ROOT_NODE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Roman Nikitenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class ConsolesPanelPresenterTest {
    private static final String MACHINE_ID     = "machineID";
    private static final String WORKSPACE_ID   = "workspaceID";
    private static final String PROCESS_ID     = "processID";
    private static final String PROCESS_NAME   = "processName";
    private static final String OUTPUT_CHANNEL = "outputChannel";
    private static final int    PID            = 101;

    @Mock
    private DtoFactory                          dtoFactory;
    @Mock
    private CommandConsoleFactory               commandConsoleFactory;
    @Mock
    private CommandTypeRegistry                 commandTypeRegistry;
    @Mock
    private DialogFactory                       dialogFactory;
    @Mock
    private WorkspaceAgent                      workspaceAgent;
    @Mock
    private NotificationManager                 notificationManager;
    @Mock
    private MachineLocalizationConstant         localizationConstant;
    @Mock
    private TerminalFactory                     terminalFactory;
    @Mock
    private ConsolesPanelView                   view;
    @Mock
    private MachineResources                    resources;
    @Mock
    private AppContext                          appContext;
    @Mock
    private MachineServiceClient                machineService;
    @Mock
    private EntityFactory                       entityFactory;
    @Mock
    private EventBus                            eventBus;
    @Mock
    private WorkspaceDto                        workspace;
    @Mock
    private OutputConsole                       outputConsole;
    @Mock
    private ConsoleTreeContextMenuFactory       consoleTreeContextMenuFactory;

    @Mock
    private Promise<List<MachineDto>> machinesPromise;

    @Mock
    private Promise<List<MachineProcessDto>> processesPromise;

    @Mock
    private Promise<MachineDto> machinePromise;

    @Captor
    private ArgumentCaptor<AcceptsOneWidget>                   acceptsOneWidgetCaptor;
    @Captor
    private ArgumentCaptor<Operation<List<MachineDto>>>        machinesCaptor;
    @Captor
    private ArgumentCaptor<Operation<List<MachineProcessDto>>> processesCaptor;
    @Captor
    private ArgumentCaptor<Operation<MachineDto>>              machineCaptor;
    @Captor
    private ArgumentCaptor<DevMachineStateEvent.Handler>       devMachineStateHandlerCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>            errorOperation;

    private ConsolesPanelPresenter presenter;

    @Before
    public void setUp() {
        when(appContext.getWorkspaceId()).thenReturn(WORKSPACE_ID);

        when(machineService.getMachines(anyString())).thenReturn(machinesPromise);
        when(machineService.getMachine(anyString())).thenReturn(machinePromise);
        when(machinePromise.then(Matchers.<Operation<MachineDto>>anyObject())).thenReturn(machinePromise);

        when(machineService.getProcesses(anyString())).thenReturn(processesPromise);
        when(processesPromise.then(Matchers.<Operation<List<MachineProcessDto>>>anyObject())).thenReturn(processesPromise);
        when(commandConsoleFactory.create(anyString())).thenReturn(mock(OutputConsole.class));

        presenter =
                new ConsolesPanelPresenter(view, eventBus, dtoFactory, dialogFactory, entityFactory, terminalFactory, commandConsoleFactory,
                                           commandTypeRegistry, workspaceAgent, notificationManager, localizationConstant,
                                           machineService, resources, appContext, consoleTreeContextMenuFactory);
    }

    @Test
    public void shouldRestoreState() throws Exception {
        CommandType commandType = mock(CommandType.class);
        CommandConfiguration commandConfiguration = mock(CommandConfiguration.class);
        CommandConfigurationFactory commandConfigurationFactory = mock(CommandConfigurationFactory.class);
        CommandOutputConsole outputConsole = mock(CommandOutputConsole.class);
        when(commandTypeRegistry.getCommandTypeById(anyString())).thenReturn(commandType);
        when(commandType.getConfigurationFactory()).thenReturn(commandConfigurationFactory);
        when(commandConfigurationFactory.createFromDto(anyObject())).thenReturn(commandConfiguration);
        when(commandConsoleFactory.create(anyObject(), any(org.eclipse.che.api.core.model.machine.Machine.class))).thenReturn(outputConsole);

        CommandDto commandDto = mock(CommandDto.class);
        when(dtoFactory.createDto(anyObject())).thenReturn(commandDto);
        when(commandDto.withName(anyString())).thenReturn(commandDto);
        when(commandDto.withCommandLine(anyString())).thenReturn(commandDto);
        when(commandDto.withType(anyString())).thenReturn(commandDto);

        MachineProcessDto machineProcessDto = mock(MachineProcessDto.class);
        when(machineProcessDto.getOutputChannel()).thenReturn(OUTPUT_CHANNEL);
        when(machineProcessDto.getPid()).thenReturn(PID);
        List<MachineProcessDto> processes = new ArrayList<>(1);
        processes.add(machineProcessDto);

        MachineDto machineDto = mock(MachineDto.class);
        MachineConfigDto machineConfigDto = mock(MachineConfigDto.class);
        when(machineDto.getConfig()).thenReturn(machineConfigDto);
        when(machineConfigDto.isDev()).thenReturn(true);
        when(machineDto.getId()).thenReturn(MACHINE_ID);
        when(machineDto.getStatus()).thenReturn(MachineStatus.RUNNING);
        List<MachineDto> machines = new ArrayList<>(2);
        machines.add(machineDto);

        verify(machinesPromise).then(machinesCaptor.capture());
        machinesCaptor.getValue().apply(machines);

        verify(processesPromise).then(processesCaptor.capture());
        processesCaptor.getValue().apply(processes);

        verify(outputConsole).listenToOutput(eq(OUTPUT_CHANNEL));
        verify(outputConsole).attachToProcess(machineProcessDto);
        verify(workspaceAgent, times(2)).setActivePart(eq(presenter));
    }

    @Test
    public void shouldFetchMachines() throws Exception {
        MachineDto machineDto = mock(MachineDto.class);
        MachineConfigDto machineConfigDto = mock(MachineConfigDto.class);
        when(machineDto.getConfig()).thenReturn(machineConfigDto);
        when(machineConfigDto.isDev()).thenReturn(true);
        when(machineDto.getStatus()).thenReturn(MachineStatus.RUNNING);
        List<MachineDto> machines = new ArrayList<>(2);
        machines.add(machineDto);

        when(appContext.getWorkspace()).thenReturn(workspace);
        DevMachineStateEvent devMachineStateEvent = mock(DevMachineStateEvent.class);
        verify(eventBus, times(5)).addHandler(anyObject(), devMachineStateHandlerCaptor.capture());

        DevMachineStateEvent.Handler devMachineStateHandler = devMachineStateHandlerCaptor.getAllValues().get(0);
        devMachineStateHandler.onDevMachineStarted(devMachineStateEvent);

        verify(appContext).getWorkspaceId();
        verify(machineService).getMachines(eq(WORKSPACE_ID));
        verify(machinesPromise).then(machinesCaptor.capture());
        machinesCaptor.getValue().apply(machines);
        verify(view).setProcessesData(anyObject());
    }

    @Test
    public void shouldShowErrorWhenMachineNodeIsNull() throws Exception {
        List<ProcessTreeNode> children = new ArrayList<>();
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);

        OutputConsole outputConsole = mock(OutputConsole.class);

        presenter.addCommandOutput(MACHINE_ID, outputConsole);
        verify(notificationManager).notify(anyString(), anyString(), any(StatusNotification.Status.class), any(DisplayMode.class));
        verify(localizationConstant, times(2)).machineNotFound(eq(MACHINE_ID));
    }

    @Test
    public void shouldAddCommand() throws Exception {
        ProcessTreeNode machineNode = mock(ProcessTreeNode.class);
        when(machineNode.getId()).thenReturn(MACHINE_ID);
        List<ProcessTreeNode> children = new ArrayList<>();
        children.add(machineNode);
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);

        presenter.addCommandOutput(MACHINE_ID, outputConsole);

        verify(view).addProcessNode(anyObject());
        verify(view, never()).hideProcessOutput(anyString());

        verify(outputConsole).go(acceptsOneWidgetCaptor.capture());
        IsWidget widget = mock(IsWidget.class);
        acceptsOneWidgetCaptor.getValue().setWidget(widget);

        verify(view).addProcessWidget(anyString(), eq(widget));
        verify(view, times(2)).selectNode(anyObject());
        verify(view).setProcessesData(anyObject());
        verify(view).getNodeById(anyString());
        verify(view, times(2)).setStopButtonVisibility(anyString(), anyBoolean());
    }

    @Test
    public void shouldDisplayStopProcessButtonAtAddingCommand() throws Exception {
        ProcessTreeNode machineNode = mock(ProcessTreeNode.class);
        when(machineNode.getId()).thenReturn(MACHINE_ID);
        List<ProcessTreeNode> children = new ArrayList<>();
        children.add(machineNode);
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);

        ProcessTreeNode selectedCommandNode = new ProcessTreeNode(COMMAND_NODE, null, PROCESS_NAME, null, children);
        children.add(selectedCommandNode);

        when(outputConsole.isFinished()).thenReturn(false);

        presenter.consoles.clear();

        presenter.addCommandOutput(MACHINE_ID, outputConsole);

        verify(view).addProcessNode(anyObject());
        verify(view, never()).hideProcessOutput(anyString());

        verify(outputConsole).go(acceptsOneWidgetCaptor.capture());
        IsWidget widget = mock(IsWidget.class);
        acceptsOneWidgetCaptor.getValue().setWidget(widget);

        verify(view).addProcessWidget(anyString(), eq(widget));
        verify(view, times(2)).selectNode(anyObject());
        verify(view).setProcessesData(anyObject());
        verify(view).getNodeById(anyString());
        verify(view).setStopButtonVisibility(anyString(), eq(true));
    }

    @Test
    public void shouldHideStopProcessButtonAtAddingCommand() throws Exception {
        ProcessTreeNode machineNode = mock(ProcessTreeNode.class);
        when(machineNode.getId()).thenReturn(MACHINE_ID);
        List<ProcessTreeNode> children = new ArrayList<>();
        children.add(machineNode);
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);

        ProcessTreeNode selectedCommandNode = new ProcessTreeNode(COMMAND_NODE, null, PROCESS_NAME, null, children);
        children.add(selectedCommandNode);

        when(outputConsole.isFinished()).thenReturn(true);

        presenter.consoles.clear();

        presenter.addCommandOutput(MACHINE_ID, outputConsole);

        verify(view).addProcessNode(anyObject());
        verify(view, never()).hideProcessOutput(anyString());

        verify(outputConsole).go(acceptsOneWidgetCaptor.capture());
        IsWidget widget = mock(IsWidget.class);
        acceptsOneWidgetCaptor.getValue().setWidget(widget);

        verify(view).addProcessWidget(anyString(), eq(widget));
        verify(view, times(2)).selectNode(anyObject());
        verify(view).setProcessesData(anyObject());
        verify(view).getNodeById(anyString());
        verify(view).setStopButtonVisibility(anyString(), eq(false));
    }

    @Test
    public void shouldHideStopProcessButtonAtAddingTerminal() throws Exception {
        MachineDto machineDto = mock(MachineDto.class);
        MachineConfigDto machineConfigDto = mock(MachineConfigDto.class);
        when(machineDto.getConfig()).thenReturn(machineConfigDto);
        when(machineConfigDto.isDev()).thenReturn(true);
        when(machineDto.getStatus()).thenReturn(MachineStatus.RUNNING);

        ProcessTreeNode machineNode = mock(ProcessTreeNode.class);
        when(machineNode.getId()).thenReturn(MACHINE_ID);
        List<ProcessTreeNode> children = new ArrayList<>();
        children.add(machineNode);
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);

        Machine machine = mock(Machine.class);
        when(entityFactory.createMachine(anyObject())).thenReturn(machine);
        TerminalPresenter terminal = mock(TerminalPresenter.class);
        when(terminalFactory.create(machine)).thenReturn(terminal);
        IsWidget terminalWidget = mock(IsWidget.class);
        when(terminal.getView()).thenReturn(terminalWidget);

        presenter.addCommandOutput(MACHINE_ID, outputConsole);
        presenter.onAddTerminal(MACHINE_ID);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machineDto);

        verify(entityFactory).createMachine(anyObject());
        verify(terminalFactory).create(eq(machine));
        verify(terminal).getView();
        verify(view, times(2)).setProcessesData(anyObject());
        verify(view, times(2)).selectNode(anyObject());
        verify(view).addProcessWidget(anyString(), eq(terminalWidget));
        verify(view, times(2)).addProcessNode(anyObject());
        verify(terminal).setVisible(eq(true));
        verify(terminal).connect();
        verify(terminal).setListener(anyObject());
        verify(view, times(3)).setStopButtonVisibility(anyString(), eq(false));
    }

    @Test
    public void shouldReplaceCommandOutput() throws Exception {
        MachineDto machineDto = mock(MachineDto.class);
        when(machineDto.getId()).thenReturn(MACHINE_ID);
        MachineConfigDto machineConfigDto = mock(MachineConfigDto.class);
        when(machineDto.getConfig()).thenReturn(machineConfigDto);

        List<ProcessTreeNode> children = new ArrayList<>();
        ProcessTreeNode commandNode = new ProcessTreeNode(COMMAND_NODE, null, PROCESS_NAME, null, children);
        children.add(commandNode);
        ProcessTreeNode machineNode = new ProcessTreeNode(MACHINE_NODE, null, machineDto, null, children);
        children.add(machineNode);
        when(machineNode.getId()).thenReturn(MACHINE_ID);

        String commandId = commandNode.getId();
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);
        presenter.consoles.put(commandId, outputConsole);

        when(outputConsole.isFinished()).thenReturn(true);
        when(outputConsole.getTitle()).thenReturn(PROCESS_NAME);

        presenter.addCommandOutput(MACHINE_ID, outputConsole);

        verify(view, never()).addProcessNode(anyObject());
        verify(view, never()).setProcessesData(anyObject());

        verify(outputConsole).go(acceptsOneWidgetCaptor.capture());
        IsWidget widget = mock(IsWidget.class);
        acceptsOneWidgetCaptor.getValue().setWidget(widget);

        verify(view).hideProcessOutput(eq(commandId));
        verify(view).addProcessWidget(eq(commandId), eq(widget));
        verify(view).selectNode(anyObject());
        verify(view).getNodeById(eq(commandId));
    }

    @Test
    public void shouldAddTerminal() throws Exception {
        MachineDto machineDto = mock(MachineDto.class);
        MachineConfigDto machineConfigDto = mock(MachineConfigDto.class);
        when(machineDto.getConfig()).thenReturn(machineConfigDto);
        when(machineConfigDto.isDev()).thenReturn(true);
        when(machineDto.getStatus()).thenReturn(MachineStatus.RUNNING);

        ProcessTreeNode machineNode = mock(ProcessTreeNode.class);
        when(machineNode.getId()).thenReturn(MACHINE_ID);
        List<ProcessTreeNode> children = new ArrayList<>();
        children.add(machineNode);
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);

        Machine machine = mock(Machine.class);
        when(entityFactory.createMachine(anyObject())).thenReturn(machine);
        TerminalPresenter terminal = mock(TerminalPresenter.class);
        when(terminalFactory.create(machine)).thenReturn(terminal);
        IsWidget terminalWidget = mock(IsWidget.class);
        when(terminal.getView()).thenReturn(terminalWidget);

        presenter.onAddTerminal(MACHINE_ID);

        verify(machinePromise).then(machineCaptor.capture());
        machineCaptor.getValue().apply(machineDto);

        verify(entityFactory).createMachine(anyObject());
        verify(terminalFactory).create(eq(machine));
        verify(terminal).getView();
        verify(view).setProcessesData(anyObject());
        verify(view).selectNode(anyObject());
        verify(view).addProcessWidget(anyString(), eq(terminalWidget));
        verify(view).addProcessNode(anyObject());
        verify(terminal).setVisible(eq(true));
        verify(terminal).connect();
        verify(terminal).setListener(anyObject());
    }

    @Test
    public void shouldShowCommanOutputWhenCommandSelected() throws Exception {
        ProcessTreeNode commandNode = mock(ProcessTreeNode.class);
        when(commandNode.getId()).thenReturn(PROCESS_ID);

        presenter.consoles.put(PROCESS_ID, outputConsole);

        presenter.onTreeNodeSelected(commandNode);

        verify(view).showProcessOutput(eq(PROCESS_ID));
        verify(view).setStopButtonVisibility(anyString(), eq(true));
    }

    @Test
    public void stopButtonShouldBeHiddenWhenConsoleHasFinishedProcess() {
        ProcessTreeNode commandNode = mock(ProcessTreeNode.class);
        when(commandNode.getId()).thenReturn(PROCESS_ID);

        when(outputConsole.isFinished()).thenReturn(true);
        presenter.consoles.put(PROCESS_ID, outputConsole);

        presenter.onTreeNodeSelected(commandNode);

        verify(view).setStopButtonVisibility(PROCESS_ID, false);
    }

    @Test
    public void stopButtonStateShouldBeRefreshedWhenConsoleHasRunningProcess() {
        ProcessTreeNode commandNode = mock(ProcessTreeNode.class);
        when(commandNode.getId()).thenReturn(PROCESS_ID);

        when(outputConsole.isFinished()).thenReturn(false);
        presenter.consoles.put(PROCESS_ID, outputConsole);

        presenter.onTreeNodeSelected(commandNode);

        verify(view).setStopButtonVisibility(PROCESS_ID, true);
    }

    @Test
    public void stopButtonShouldBeHiddenWhenProcessFinished() {
        when(outputConsole.isFinished()).thenReturn(true);
        presenter.consoles.put(PROCESS_ID, outputConsole);

        presenter.onProcessFinished(new ProcessFinishedEvent(null));

        verify(view).setStopButtonVisibility(PROCESS_ID, false);
    }

    @Test
    public void shouldStopProcessWithoutCloseCommanOutput() throws Exception {
        ProcessTreeNode machineNode = mock(ProcessTreeNode.class);
        ProcessTreeNode commandNode = mock(ProcessTreeNode.class);
        when(machineNode.getId()).thenReturn(MACHINE_ID);
        List<ProcessTreeNode> children = new ArrayList<>();
        children.add(machineNode);
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);

        when(outputConsole.isFinished()).thenReturn(false);
        presenter.consoles.put(PROCESS_ID, outputConsole);
        //noinspection ConstantConditions
        machineNode.getChildren().add(commandNode);

        when(commandNode.getId()).thenReturn(PROCESS_ID);
        when(view.getNodeIndex(anyString())).thenReturn(0);
        when(machineNode.getChildren()).thenReturn(children);
        when(commandNode.getParent()).thenReturn(machineNode);

        presenter.onStopCommandProcess(commandNode);

        verify(outputConsole).stop();
        verify(view, never()).hideProcessOutput(eq(PROCESS_ID));
        verify(view, never()).removeProcessNode(eq(commandNode));
    }

    @Test
    public void shouldCloseCommanOutputWhenCommandHasFinished() throws Exception {
        ProcessTreeNode machineNode = mock(ProcessTreeNode.class);
        ProcessTreeNode commandNode = mock(ProcessTreeNode.class);
        when(machineNode.getId()).thenReturn(MACHINE_ID);
        List<ProcessTreeNode> children = new ArrayList<>();
        children.add(machineNode);
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);

        when(outputConsole.isFinished()).thenReturn(true);
        presenter.consoles.put(PROCESS_ID, outputConsole);
        machineNode.getChildren().add(commandNode);

        when(commandNode.getId()).thenReturn(PROCESS_ID);
        when(view.getNodeIndex(anyString())).thenReturn(0);
        when(machineNode.getChildren()).thenReturn(children);
        when(commandNode.getParent()).thenReturn(machineNode);

        presenter.onCloseCommandOutputClick(commandNode);

        verify(commandNode, times(2)).getId();
        verify(commandNode).getParent();
        verify(view).getNodeIndex(eq(PROCESS_ID));
        verify(view).hideProcessOutput(eq(PROCESS_ID));
        verify(view).removeProcessNode(eq(commandNode));
        verify(view).setProcessesData(anyObject());
    }

    @Test
    public void shouldShowConfirmDialogWhenCommandHasNotFinished() throws Exception {
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        ProcessTreeNode commandNode = mock(ProcessTreeNode.class);

        when(outputConsole.isFinished()).thenReturn(false);
        presenter.consoles.put(PROCESS_ID, outputConsole);

        when(commandNode.getId()).thenReturn(PROCESS_ID);
        when(dialogFactory.createConfirmDialog(anyString(), anyString(), anyObject(), anyObject())).thenReturn(confirmDialog);

        presenter.onCloseCommandOutputClick(commandNode);

        verify(commandNode).getId();
        verify(view, never()).hideProcessOutput(anyString());
        verify(view, never()).removeProcessNode(anyObject());
        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), anyObject(), anyObject());
        verify(confirmDialog).show();
    }

    @Test
    public void shouldShowTerminalWhenTerminalNodeSelected() throws Exception {
        TerminalPresenter terminal = mock(TerminalPresenter.class);
        presenter.terminals.put(PROCESS_ID, terminal);

        ProcessTreeNode terminalNode = mock(ProcessTreeNode.class);
        when(terminalNode.getId()).thenReturn(PROCESS_ID);
        presenter.onTreeNodeSelected(terminalNode);

        verify(view).showProcessOutput(eq(PROCESS_ID));
        verify(view, never()).setStopButtonVisibility(PROCESS_ID, true);
    }

    @Test
    public void shouldCloseTerminal() throws Exception {
        ProcessTreeNode machineNode = mock(ProcessTreeNode.class);
        when(machineNode.getId()).thenReturn(MACHINE_ID);

        TerminalPresenter terminal = mock(TerminalPresenter.class);

        ProcessTreeNode terminalNode = mock(ProcessTreeNode.class);
        when(terminalNode.getId()).thenReturn(PROCESS_ID);

        List<ProcessTreeNode> children = new ArrayList<>();
        children.add(machineNode);
        presenter.rootNode = new ProcessTreeNode(ROOT_NODE, null, null, null, children);
        presenter.terminals.put(PROCESS_ID, terminal);

        when(view.getNodeIndex(anyString())).thenReturn(0);
        when(machineNode.getChildren()).thenReturn(children);
        when(terminalNode.getParent()).thenReturn(machineNode);

        presenter.onCloseTerminal(terminalNode);

        verify(terminal).stopTerminal();
        verify(terminalNode, times(2)).getId();
        verify(terminalNode).getParent();
        verify(view).getNodeIndex(eq(PROCESS_ID));
        verify(view).hideProcessOutput(eq(PROCESS_ID));
        verify(view).removeProcessNode(eq(terminalNode));
        verify(view).setProcessesData(anyObject());
    }

    @Test
    public void shouldReturnTitle() throws Exception {
        presenter.getTitle();

        verify(localizationConstant, times(2)).viewConsolesTitle();
    }

    @Test
    public void shouldReturnTitleToolTip() throws Exception {
        presenter.getTitleToolTip();

        verify(localizationConstant).viewProcessesTooltip();
    }

    @Test
    public void shouldSetViewVisible() throws Exception {
        presenter.setVisible(true);

        verify(view).setVisible(eq(true));
    }

    @Test
    public void shouldReturnTitleSVGImage() {
        presenter.getTitleImage();

        verify(resources).terminal();
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);

        presenter.go(container);

        verify(container).setWidget(eq(view));
    }

}
