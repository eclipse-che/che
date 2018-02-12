/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.palette;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandExecutor;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.workspace.model.MachineImpl;
import org.eclipse.che.ide.api.workspace.model.RuntimeImpl;
import org.eclipse.che.ide.api.workspace.model.WorkspaceImpl;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.machine.chooser.MachineChooser;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link CommandsPalettePresenter}. */
@RunWith(MockitoJUnitRunner.class)
public class CommandsPalettePresenterTest {

  @Mock private CommandsPaletteView view;

  @Mock private CommandManager commandManager;

  @Mock private CommandExecutor commandExecutor;

  @Mock private DialogFactory dialogFactory;

  @Mock private AppContext appContext;

  @Mock private MachineChooser machineChooser;

  @Mock private CommandUtils commandUtils;

  @Mock private PaletteMessages messages;

  @Mock private Promise<MachineImpl> machinePromise;

  @Captor private ArgumentCaptor<Operation<MachineImpl>> selectedMachineCaptor;

  @Captor private ArgumentCaptor<Map<CommandGoal, List<CommandImpl>>> filteredCommandsCaptor;

  @InjectMocks private CommandsPalettePresenter presenter;

  @Test
  public void shouldSetViewDelegate() throws Exception {
    verify(view).setDelegate(eq(presenter));
  }

  @Test
  public void shouldShowViewAndSetCommands() throws Exception {
    presenter.showDialog();

    verify(view).showDialog();
    verify(commandManager).getCommands();
    verify(view).setCommands(org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void shouldFilterCommands() throws Exception {
    // given
    CommandImpl cmd1 = mock(CommandImpl.class);
    when(cmd1.getName()).thenReturn("test");

    CommandImpl cmd2 = mock(CommandImpl.class);
    when(cmd2.getName()).thenReturn("run");

    List<CommandImpl> commands = new ArrayList<>();
    commands.add(cmd1);
    commands.add(cmd2);

    when(commandManager.getCommands()).thenReturn(commands);

    Map<CommandGoal, List<CommandImpl>> filteredCommandsMock = new HashMap<>();
    filteredCommandsMock.put(mock(CommandGoal.class), commands);
    when(commandUtils.groupCommandsByGoal(commands)).thenReturn(filteredCommandsMock);

    // when
    presenter.onFilterChanged("run");

    // then
    verify(commandUtils).groupCommandsByGoal(commands);
    verify(view).setCommands(filteredCommandsCaptor.capture());
    final Map<CommandGoal, List<CommandImpl>> filteredCommandsValue =
        filteredCommandsCaptor.getValue();
    assertEquals(filteredCommandsMock, filteredCommandsValue);
  }

  @Test
  public void shouldExecuteCommand() throws Exception {
    // given
    WorkspaceImpl workspace = mock(WorkspaceImpl.class);
    when(appContext.getWorkspace()).thenReturn(workspace);

    RuntimeImpl workspaceRuntime = mock(RuntimeImpl.class);
    when(workspace.getRuntime()).thenReturn(workspaceRuntime);

    Map<String, MachineImpl> machines = new HashMap<>();
    MachineImpl chosenMachine = mock(MachineImpl.class);
    when(chosenMachine.getName()).thenReturn("machine_id");
    machines.put("machine_id", chosenMachine);
    when(workspaceRuntime.getMachines()).thenReturn(machines);

    when(machineChooser.show()).thenReturn(machinePromise);

    CommandImpl commandToExecute = mock(CommandImpl.class);

    // when
    presenter.onCommandExecute(commandToExecute);

    // then
    verify(view).close();
    verify(machineChooser).show();

    verify(machinePromise).then(selectedMachineCaptor.capture());
    selectedMachineCaptor.getValue().apply(chosenMachine);

    verify(commandExecutor).executeCommand(eq(commandToExecute), eq("machine_id"));
  }
}
