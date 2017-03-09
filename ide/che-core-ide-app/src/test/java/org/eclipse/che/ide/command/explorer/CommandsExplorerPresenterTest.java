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
package org.eclipse.che.ide.command.explorer;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.type.CommandTypeChooser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link CommandsExplorerPresenter}.
 *
 * @author Artem Zatsarynnyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class CommandsExplorerPresenterTest {

    @Mock
    private CommandsExplorerView                      view;
    @Mock
    private CommandResources                          resources;
    @Mock
    private WorkspaceAgent                            workspaceAgent;
    @Mock
    private CommandManager                            commandManager;
    @Mock
    private NotificationManager                       notificationManager;
    @Mock
    private CommandTypeChooser                        commandTypeChooser;
    @Mock
    private ExplorerMessages                          messages;
    @Mock
    private CommandsExplorerPresenter.RefreshViewTask refreshViewTask;
    @Mock
    private DialogFactory                             dialogFactory;

    @InjectMocks
    private CommandsExplorerPresenter presenter;

    @Mock
    private Promise<Void>                                voidPromise;
    @Mock
    private Promise<ContextualCommand>                   commandPromise;
    @Mock
    private Promise<CommandType>                         commandTypePromise;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>      errorOperationCaptor;
    @Captor
    private ArgumentCaptor<Operation<ContextualCommand>> commandOperationCaptor;
    @Captor
    private ArgumentCaptor<Operation<CommandType>>       commandTypeOperationCaptor;

    @Test
    public void shouldSetViewDelegate() throws Exception {
        verify(view).setDelegate(eq(presenter));
    }

    @Test
    public void testStart() throws Exception {
        Callback callback = mock(Callback.class);

        presenter.start(callback);

        verify(workspaceAgent).openPart(presenter, PartStackType.NAVIGATION, Constraints.LAST);
        verify(workspaceAgent).setActivePart(presenter);
        verify(commandManager).addCommandChangedListener(presenter);
        verify(commandManager).addCommandLoadedListener(presenter);
        verify(callback).onSuccess(presenter);
    }

    @Test
    public void testGo() throws Exception {
        AcceptsOneWidget container = mock(AcceptsOneWidget.class);

        presenter.go(container);

        verify(refreshViewTask).delayAndSelectCommand(isNull(ContextualCommand.class));
        verify(container).setWidget(view);
    }

    @Test
    public void shouldReturnTitle() throws Exception {
        presenter.getTitle();

        verify(messages).partTitle();
    }

    @Test
    public void shouldReturnView() throws Exception {
        IsWidget view = presenter.getView();

        assertEquals(this.view, view);
    }

    @Test
    public void shouldReturnTitleTooltip() throws Exception {
        presenter.getTitleToolTip();

        verify(messages).partTooltip();
    }

    @Test
    public void shouldReturnTitleImage() throws Exception {
        presenter.getTitleImage();

        verify(resources).explorerPart();
    }

    @Test
    public void shouldCreateCommand() throws Exception {
        // given
        CommandType selectedCommandType = mock(CommandType.class);
        String commandTypeId = "mvn";
        when(selectedCommandType.getId()).thenReturn(commandTypeId);

        CommandGoal selectedCommandGoal = mock(CommandGoal.class);
        String commandGoalId = "test";
        when(selectedCommandGoal.getId()).thenReturn(commandGoalId);

        when(view.getSelectedGoal()).thenReturn(selectedCommandGoal);

        when(commandTypeChooser.show(anyInt(), anyInt())).thenReturn(commandTypePromise);

        when(commandManager.createCommand(anyString(),
                                          anyString(),
                                          any(ContextualCommand.ApplicableContext.class))).thenReturn(commandPromise);
        when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);
        when(commandPromise.catchError(any(Operation.class))).thenReturn(commandPromise);

        // when
        presenter.onCommandAdd(0, 0);

        // then
        verify(commandTypeChooser).show(0, 0);
        verify(commandTypePromise).then(commandTypeOperationCaptor.capture());
        commandTypeOperationCaptor.getValue().apply(selectedCommandType);

        verify(view).getSelectedGoal();

        verify(commandManager).createCommand(eq(commandGoalId),
                                             eq(commandTypeId),
                                             any(ContextualCommand.ApplicableContext.class));
    }

    @Test(expected = OperationException.class)
    public void shouldShowNotificationWhenFailedToCreateCommand() throws Exception {
        // given
        CommandType selectedCommandType = mock(CommandType.class);
        String commandTypeId = "mvn";
        when(selectedCommandType.getId()).thenReturn(commandTypeId);

        CommandGoal selectedCommandGoal = mock(CommandGoal.class);
        String commandGoalId = "test";
        when(selectedCommandGoal.getId()).thenReturn(commandGoalId);

        when(view.getSelectedGoal()).thenReturn(selectedCommandGoal);

        when(commandTypeChooser.show(anyInt(), anyInt())).thenReturn(commandTypePromise);

        when(commandManager.createCommand(anyString(),
                                          anyString(),
                                          any(ContextualCommand.ApplicableContext.class))).thenReturn(commandPromise);
        when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);
        when(commandPromise.catchError(any(Operation.class))).thenReturn(commandPromise);

        // when
        presenter.onCommandAdd(0, 0);

        // then
        verify(commandTypeChooser).show(0, 0);
        verify(commandTypePromise).then(commandTypeOperationCaptor.capture());
        commandTypeOperationCaptor.getValue().apply(selectedCommandType);

        verify(view).getSelectedGoal();

        verify(commandManager).createCommand(eq(commandGoalId),
                                             eq(commandTypeId),
                                             any(ContextualCommand.ApplicableContext.class));

        verify(commandPromise).catchError(errorOperationCaptor.capture());
        errorOperationCaptor.getValue().apply(mock(PromiseError.class));
        verify(messages).unableCreate();
        verify(notificationManager).notify(anyString(), anyString(), eq(FAIL), eq(EMERGE_MODE));
    }

    @Test
    public void shouldDuplicateCommand() throws Exception {
        ContextualCommand command = mock(ContextualCommand.class);
        when(commandManager.createCommand(any(ContextualCommand.class))).thenReturn(commandPromise);
        when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);

        presenter.onCommandDuplicate(command);

        verify(commandManager).createCommand(command);
    }

    @Test(expected = OperationException.class)
    public void shouldShowNotificationWhenFailedToDuplicateCommand() throws Exception {
        ContextualCommand command = mock(ContextualCommand.class);
        when(commandManager.createCommand(any(ContextualCommand.class))).thenReturn(commandPromise);
        when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);

        presenter.onCommandDuplicate(command);

        verify(commandPromise).catchError(errorOperationCaptor.capture());
        errorOperationCaptor.getValue().apply(mock(PromiseError.class));
        verify(messages).unableDuplicate();
        verify(notificationManager).notify(anyString(), anyString(), eq(FAIL), eq(EMERGE_MODE));
    }

    @Test
    public void shouldRemoveCommand() throws Exception {
        // given
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               any(ConfirmCallback.class),
                                               any(CancelCallback.class))).thenReturn(confirmDialog);
        ArgumentCaptor<ConfirmCallback> confirmCallbackCaptor = ArgumentCaptor.forClass(ConfirmCallback.class);

        ContextualCommand command = mock(ContextualCommand.class);
        String cmdName = "build";
        when(command.getName()).thenReturn(cmdName);
        when(commandManager.removeCommand(anyString())).thenReturn(voidPromise);

        // when
        presenter.onCommandRemove(command);

        // then
        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), confirmCallbackCaptor.capture(), isNull(CancelCallback.class));
        confirmCallbackCaptor.getValue().accepted();
        verify(confirmDialog).show();
        verify(commandManager).removeCommand(cmdName);
    }

    @Test
    public void shouldNotRemoveCommandWhenCancelled() throws Exception {
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               any(ConfirmCallback.class),
                                               any(CancelCallback.class))).thenReturn(confirmDialog);
        ContextualCommand command = mock(ContextualCommand.class);

        presenter.onCommandRemove(command);

        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), any(ConfirmCallback.class), isNull(CancelCallback.class));
        verify(confirmDialog).show();
        verify(commandManager, never()).removeCommand(anyString());
    }

    @Test(expected = OperationException.class)
    public void shouldShowNotificationWhenFailedToRemoveCommand() throws Exception {
        // given
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               any(ConfirmCallback.class),
                                               any(CancelCallback.class))).thenReturn(confirmDialog);
        ArgumentCaptor<ConfirmCallback> confirmCallbackCaptor = ArgumentCaptor.forClass(ConfirmCallback.class);

        when(commandManager.removeCommand(anyString())).thenReturn(voidPromise);

        // when
        presenter.onCommandRemove(mock(ContextualCommand.class));

        // then
        verify(dialogFactory).createConfirmDialog(anyString(), anyString(), confirmCallbackCaptor.capture(), isNull(CancelCallback.class));
        confirmCallbackCaptor.getValue().accepted();

        verify(voidPromise).catchError(errorOperationCaptor.capture());
        errorOperationCaptor.getValue().apply(mock(PromiseError.class));
        verify(messages).unableRemove();
        verify(notificationManager).notify(anyString(), anyString(), eq(FAIL), eq(EMERGE_MODE));
    }

    @Test
    public void shouldRefreshViewWhenCommandsAreLoaded() throws Exception {
        presenter.onCommandsLoaded();

        verify(refreshViewTask).delayAndSelectCommand(isNull(ContextualCommand.class));
    }

    @Test
    public void shouldRefreshViewWhenCommandAdded() throws Exception {
        ContextualCommand command = mock(ContextualCommand.class);

        presenter.onCommandAdded(command);

        verify(refreshViewTask).delayAndSelectCommand(isNull(ContextualCommand.class));
    }

    @Test
    public void shouldRefreshViewWhenCommandUpdated() throws Exception {
        presenter.onCommandUpdated(mock(ContextualCommand.class), mock(ContextualCommand.class));

        verify(refreshViewTask).delayAndSelectCommand(isNull(ContextualCommand.class));
    }

    @Test
    public void shouldRefreshViewWhenCommandRemoved() throws Exception {
        presenter.onCommandRemoved(mock(ContextualCommand.class));

        verify(refreshViewTask).delayAndSelectCommand(isNull(ContextualCommand.class));
    }

}
