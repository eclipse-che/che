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
package org.eclipse.che.ide.extension.machine.client.command.edit;

import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Roman Nikitenko */
@RunWith(MockitoJUnitRunner.class)
public class EditCommandsPresenterTest {

    private static String COMMAND_NAME = "commandName";
    private static String COMMAND_TYPE = "commandType";

    @Mock
    private EditCommandsView            view;
    @Mock
    private CommandManager              commandManager;
    @Mock
    private CommandTypeRegistry         commandTypeRegistry;
    @Mock
    private DialogFactory               dialogFactory;
    @Mock
    private MachineLocalizationConstant machineLocale;
    @Mock
    private CoreLocalizationConstant    coreLocale;

    @Mock
    private CommandImpl command;

    @Mock
    private Promise<List<CommandImpl>>                                    commandsPromise;
    @Mock
    private Promise<CommandImpl>                                          commandPromise;
    @Captor
    private ArgumentCaptor<Function<List<CommandDto>, List<CommandImpl>>> commandsCaptor;
    @Captor
    private ArgumentCaptor<Operation<CommandImpl>>                        commandCaptor;
    @Captor
    private ArgumentCaptor<Operation<WorkspaceDto>>                       workspaceCaptor;

    @InjectMocks
    private EditCommandsPresenter presenter;

    @Before
    public void setUp() {
        presenter.editedCommandNameInitial = COMMAND_NAME;

        when(commandManager.update(anyString(), anyObject())).thenReturn(commandPromise);

        CommandType commandType = mock(CommandType.class);
        when(commandType.getId()).thenReturn(COMMAND_TYPE);
        List<CommandType> commandTypes = new ArrayList<>(1);
        commandTypes.add(commandType);
        when(commandTypeRegistry.getCommandTypes()).thenReturn(commandTypes);

        when(command.getType()).thenReturn(COMMAND_TYPE);
        when(command.getName()).thenReturn(COMMAND_NAME);
        List<CommandImpl> commands = new ArrayList<>(1);
        commands.add(command);
        when(commandManager.getCommands()).thenReturn(commands);
    }

    @Test
    public void onEnterClickedWhenCancelButtonInFocus() throws Exception {
        when(view.isCancelButtonInFocus()).thenReturn(true);

        presenter.onEnterClicked();

        verify(view).setCancelButtonState(false);
        verify(view).setSaveButtonState(false);
        verify(commandManager).getCommands();
        verify(view).setData(anyObject());
        verify(view).setFilterState(anyBoolean());
        verify(view).setCloseButtonInFocus();
        verify(view, never()).close();
        verify(commandManager, never()).update(anyString(), anyObject());
        verify(commandManager, never()).remove(anyString());
    }

    @Test
    public void onEnterClickedWhenCloseButtonInFocus() throws Exception {
        when(view.isCloseButtonInFocus()).thenReturn(true);

        presenter.onEnterClicked();

        verify(view).close();
        verify(commandManager, never()).getCommands();
        verify(commandManager, never()).update(anyString(), anyObject());
        verify(commandManager, never()).remove(anyString());
    }

    @Test
    public void onEnterClickedWhenSaveButtonInFocus() throws Exception {
        when(view.isCancelButtonInFocus()).thenReturn(false);
        when(view.isCloseButtonInFocus()).thenReturn(false);

        when(view.getSelectedCommand()).thenReturn(command);

        when(commandPromise.then((Operation)anyObject())).thenReturn(commandPromise);
        when(commandPromise.catchError((Operation)anyObject())).thenReturn(commandPromise);

        presenter.onEnterClicked();

        verify(commandManager).update(anyString(), eq(command));

        verify(commandPromise, times(2)).then(commandCaptor.capture());
        commandCaptor.getValue().apply(command);

        verify(view).setCancelButtonState(false);
        verify(view).setSaveButtonState(false);
        verify(commandManager).getCommands();
        verify(view).setData(anyObject());
        verify(view).setFilterState(anyBoolean());
        verify(view).setCloseButtonInFocus();
        verify(view, never()).close();
    }
}
