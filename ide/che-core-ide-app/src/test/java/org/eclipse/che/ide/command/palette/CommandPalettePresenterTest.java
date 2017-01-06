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
package org.eclipse.che.ide.command.palette;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.machine.shared.dto.MachineDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceRuntimeDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.machine.chooser.MachineChooser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CommandPalettePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandPalettePresenterTest {

    @Mock
    private CommandPaletteView view;

    @Mock
    private CommandManager commandManager;

    @Mock
    private CommandExecutor commandExecutor;

    @Mock
    private DialogFactory dialogFactory;

    @Mock
    private AppContext appContext;

    @Mock
    private MachineChooser machineChooser;

    @Mock
    private CommandUtils commandUtils;

    @Mock
    private PaletteMessages messages;

    @Mock
    private Promise<Machine> machinePromise;

    @Captor
    private ArgumentCaptor<Operation<Machine>> selectedMachineCaptor;

    @Captor
    private ArgumentCaptor<Map<CommandGoal, List<ContextualCommand>>> filteredCommandsCaptor;

    @InjectMocks
    private CommandPalettePresenter presenter;

    @Test
    public void shouldSetViewDelegate() throws Exception {
        verify(view).setDelegate(eq(presenter));
    }

    @Test
    public void shouldShowViewAndSetCommands() throws Exception {
        presenter.showDialog();

        verify(view).show();
        verify(commandManager).getCommands();
        verify(view).setCommands(Matchers.<Map<CommandGoal, List<ContextualCommand>>>any());
    }

    @Test
    public void shouldFilterCommands() throws Exception {
        // given
        ContextualCommand cmd1 = mock(ContextualCommand.class);
        when(cmd1.getName()).thenReturn("test");

        ContextualCommand cmd2 = mock(ContextualCommand.class);
        when(cmd2.getName()).thenReturn("run");

        List<ContextualCommand> commands = new ArrayList<>();
        commands.add(cmd1);
        commands.add(cmd2);

        when(commandManager.getCommands()).thenReturn(commands);

        Map<CommandGoal, List<ContextualCommand>> filteredCommandsMock = new HashMap<>();
        filteredCommandsMock.put(mock(CommandGoal.class), commands);
        when(commandUtils.groupCommandsByGoal(commands)).thenReturn(filteredCommandsMock);

        // when
        presenter.onFilterChanged("run");

        // then
        verify(commandUtils).groupCommandsByGoal(commands);
        verify(view).setCommands(filteredCommandsCaptor.capture());
        final Map<CommandGoal, List<ContextualCommand>> filteredCommandsValue = filteredCommandsCaptor.getValue();
        assertEquals(filteredCommandsMock, filteredCommandsValue);
    }

    @Test
    public void shouldExecuteCommand() throws Exception {
        // given
        Workspace workspace = mock(Workspace.class);
        when(appContext.getWorkspace()).thenReturn(workspace);

        WorkspaceRuntimeDto workspaceRuntime = mock(WorkspaceRuntimeDto.class);
        when(workspace.getRuntime()).thenReturn(workspaceRuntime);

        List<MachineDto> machines = new ArrayList<>(1);
        MachineDto chosenMachine = mock(MachineDto.class);
        machines.add(chosenMachine);
        when(workspaceRuntime.getMachines()).thenReturn(machines);

        when(machineChooser.show()).thenReturn(machinePromise);

        ContextualCommand commandToExecute = mock(ContextualCommand.class);

        // when
        presenter.onCommandExecute(commandToExecute);

        // then
        verify(view).close();
        verify(machineChooser).show();

        verify(machinePromise).then(selectedMachineCaptor.capture());
        selectedMachineCaptor.getValue().apply(chosenMachine);

        verify(commandExecutor).executeCommand(eq(commandToExecute), eq(chosenMachine));
    }
}
