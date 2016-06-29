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
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.CommandTypeRegistry;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Roman Nikitenko */
@RunWith(MockitoJUnitRunner.class)
public class EditCommandsPresenterTest {

    private static String WORKSPACE_ID = "workspaceId";
    private static String COMMAND_NAME = "commandName";

    @Mock
    private EditCommandsView                                        view;
    @Mock
    private WorkspaceServiceClient                                  workspaceServiceClient;
    @Mock
    private CommandManager                                          commandManager;
    @Mock
    private DtoFactory                  dtoFactory;
    @Mock
    private CommandTypeRegistry         commandTypeRegistry;
    @Mock
    private AppContext                  appContext;
    @Mock
    private DialogFactory               dialogFactory;
    @Mock
    private MachineLocalizationConstant machineLocale;
    @Mock
    private CoreLocalizationConstant    coreLocale;
    @Mock
    private WorkspaceDto                workspace;

    @Mock
    private Promise<List<CommandDto>>                                              commandsPromise;
    @Mock
    private Promise<WorkspaceDto>                                             workspacePromise;
    @Mock
    private Promise<List<CommandConfiguration>>                                    commandConfigurationPromise;
    @Captor
    private ArgumentCaptor<Function<List<CommandDto>, List<CommandConfiguration>>> commandsCaptor;
    @Captor
    private ArgumentCaptor<Operation<List<CommandConfiguration>>>                  commandConfigurationCaptor;
    @Captor
    private ArgumentCaptor<Operation<WorkspaceDto>>                           workspaceCaptor;


    @InjectMocks
    private EditCommandsPresenter presenter;

    @Before
    public void setUp() {
        presenter.editedCommandOriginName = COMMAND_NAME;
        presenter.workspaceId = WORKSPACE_ID;
        when(appContext.getWorkspace()).thenReturn(workspace);
        when(workspace.getId()).thenReturn(WORKSPACE_ID);
        when(workspaceServiceClient.getCommands(anyString())).thenReturn(commandsPromise);
        when(commandsPromise.then((Function<List<CommandDto>, List<CommandConfiguration>>)anyObject()))
                .thenReturn(commandConfigurationPromise);
        when(commandConfigurationPromise.then((Operation<List<CommandConfiguration>>)anyObject())).thenReturn(commandConfigurationPromise);
        when(workspaceServiceClient.updateCommand(anyString(), anyString(), anyObject())).thenReturn(workspacePromise);
    }

    @Test
    public void onEnterClickedWhenCancelButtonInFocus() throws Exception {
        when(view.isCancelButtonInFocus()).thenReturn(true);
        CommandDto command = mock(CommandDto.class);
        CommandConfiguration commandConfiguration = mock(CommandConfiguration.class);
        List<CommandDto> commands = new ArrayList<>(1);
        List<CommandConfiguration> confiqurations = new ArrayList<>(1);
        commands.add(command);
        confiqurations.add(commandConfiguration);

        presenter.onEnterClicked();

        verify(view).setCancelButtonState(false);
        verify(view).setSaveButtonState(false);
        verify(workspaceServiceClient).getCommands(anyString());

        verify(commandsPromise).then(commandsCaptor.capture());
        commandsCaptor.getValue().apply(commands);

        verify(commandConfigurationPromise).then(commandConfigurationCaptor.capture());
        commandConfigurationCaptor.getValue().apply(confiqurations);

        verify(view).setData(anyObject());
        verify(view).setFilterState(anyBoolean());
        verify(view).setCloseButtonInFocus();

        verify(view, never()).close();
        verify(workspaceServiceClient, never()).updateCommand(anyString(), anyString(), anyObject());
        verify(workspaceServiceClient, never()).deleteCommand(anyString(), anyString());
    }

    @Test
    public void onEnterClickedWhenCloseButtonInFocus() throws Exception {
        when(view.isCloseButtonInFocus()).thenReturn(true);

        presenter.onEnterClicked();

        verify(view).close();
        verify(workspaceServiceClient, never()).getCommands(anyString());
        verify(workspaceServiceClient, never()).updateCommand(anyString(), anyString(), anyObject());
        verify(workspaceServiceClient, never()).deleteCommand(anyString(), anyString());
    }

    @Test
    public void onEnterClickedWhenSaveButtonInFocus() throws Exception {
        when(view.isCancelButtonInFocus()).thenReturn(false);
        when(view.isCloseButtonInFocus()).thenReturn(false);
        CommandDto command = mock(CommandDto.class);
        CommandConfiguration commandConfiguration = mock(CommandConfiguration.class);
        List<CommandDto> commands = new ArrayList<>(1);
        List<CommandConfiguration> confiqurations = new ArrayList<>(1);
        commands.add(command);
        confiqurations.add(commandConfiguration);
        when(dtoFactory.createDto(CommandDto.class)).thenReturn(command);
        when(command.withName(anyString())).thenReturn(command);
        when(command.withCommandLine(anyString())).thenReturn(command);
        when(command.withType(anyString())).thenReturn(command);
        when(command.withAttributes(anyMap())).thenReturn(command);
        when(view.getSelectedConfiguration()).thenReturn(commandConfiguration);
        when(commandConfiguration.getType()).thenReturn(mock(CommandType.class));
        when(commandConfiguration.getName()).thenReturn(COMMAND_NAME);

        when(workspacePromise.thenPromise(any(Function.class))).thenReturn(workspacePromise);
        when(workspacePromise.then((Operation)anyObject())).thenReturn(workspacePromise);

        presenter.onEnterClicked();

        verify(dtoFactory).createDto(CommandDto.class);
        verify(workspaceServiceClient).updateCommand(eq(WORKSPACE_ID), anyString(), eq(command));
        verify(workspacePromise).then(workspaceCaptor.capture());
        workspaceCaptor.getValue().apply(workspace);

        verify(view).setCancelButtonState(false);
        verify(view).setSaveButtonState(false);
        verify(workspaceServiceClient).getCommands(anyString());

        verify(commandsPromise).then(commandsCaptor.capture());
        commandsCaptor.getValue().apply(commands);

        verify(commandConfigurationPromise).then(commandConfigurationCaptor.capture());
        commandConfigurationCaptor.getValue().apply(confiqurations);

        verify(view).setData(anyObject());
        verify(view).setFilterState(anyBoolean());
        verify(view).setCloseButtonInFocus();
        verify(view, never()).close();
    }
}
