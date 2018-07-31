/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.explorer;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandAddedEvent;
import org.eclipse.che.ide.api.command.CommandAddedEvent.CommandAddedHandler;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandRemovedEvent;
import org.eclipse.che.ide.api.command.CommandRemovedEvent.CommandRemovedHandler;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.command.CommandUpdatedEvent;
import org.eclipse.che.ide.api.command.CommandUpdatedEvent.CommandUpdatedHandler;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.command.CommandResources;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.command.type.chooser.CommandTypeChooser;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/** Tests for {@link CommandsExplorerPresenter}. */
@RunWith(GwtMockitoTestRunner.class)
public class CommandsExplorerPresenterTest {

  @Mock private CommandsExplorerView view;
  @Mock private CommandResources resources;
  @Mock private WorkspaceAgent workspaceAgent;
  @Mock private CommandManager commandManager;
  @Mock private NotificationManager notificationManager;
  @Mock private CommandTypeChooser commandTypeChooser;
  @Mock private ExplorerMessages messages;
  @Mock private CommandsExplorerPresenter.RefreshViewTask refreshViewTask;
  @Mock private DialogFactory dialogFactory;
  @Mock private NodeFactory nodeFactory;
  @Mock private Provider<EditorAgent> editorAgentProvider;
  @Mock private AppContext appContext;
  @Mock private EventBus eventBus;

  @InjectMocks private CommandsExplorerPresenter presenter;

  @Mock private Promise<Void> voidPromise;
  @Mock private Promise<CommandImpl> commandPromise;
  @Mock private Promise<CommandType> commandTypePromise;
  @Captor private ArgumentCaptor<Operation<PromiseError>> errorOperationCaptor;
  @Captor private ArgumentCaptor<Operation<CommandType>> commandTypeOperationCaptor;

  @Test
  public void shouldSetViewDelegate() throws Exception {
    verify(view).setDelegate(eq(presenter));
  }

  @Test
  public void testStart() throws Exception {
    verify(eventBus).addHandler(eq(CommandAddedEvent.getType()), any(CommandAddedHandler.class));
    verify(eventBus)
        .addHandler(eq(CommandRemovedEvent.getType()), any(CommandRemovedHandler.class));
    verify(eventBus)
        .addHandler(eq(CommandUpdatedEvent.getType()), any(CommandUpdatedHandler.class));
  }

  @Test
  public void testGo() throws Exception {
    AcceptsOneWidget container = mock(AcceptsOneWidget.class);

    presenter.go(container);

    verifyViewRefreshed();
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
    when(appContext.getProjects()).thenReturn(new Project[0]);
    when(commandManager.createCommand(anyString(), anyString(), any(ApplicableContext.class)))
        .thenReturn(commandPromise);
    when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);
    when(commandPromise.catchError(any(Operation.class))).thenReturn(commandPromise);

    // when
    presenter.onCommandAdd(0, 0);

    // then
    verify(commandTypeChooser).show(0, 0);
    verify(commandTypePromise).then(commandTypeOperationCaptor.capture());
    commandTypeOperationCaptor.getValue().apply(selectedCommandType);

    verify(view).getSelectedGoal();

    verify(commandManager)
        .createCommand(eq(commandGoalId), eq(commandTypeId), any(ApplicableContext.class));
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
    when(appContext.getProjects()).thenReturn(new Project[0]);
    when(commandManager.createCommand(anyString(), anyString(), any(ApplicableContext.class)))
        .thenReturn(commandPromise);
    when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);
    when(commandPromise.catchError(any(Operation.class))).thenReturn(commandPromise);

    // when
    presenter.onCommandAdd(0, 0);

    // then
    verify(commandTypeChooser).show(0, 0);
    verify(commandTypePromise).then(commandTypeOperationCaptor.capture());
    commandTypeOperationCaptor.getValue().apply(selectedCommandType);

    verify(view).getSelectedGoal();

    verify(commandManager)
        .createCommand(eq(commandGoalId), eq(commandTypeId), any(ApplicableContext.class));

    verify(commandPromise).catchError(errorOperationCaptor.capture());
    errorOperationCaptor.getValue().apply(mock(PromiseError.class));
    verify(messages).unableCreate();
    verify(notificationManager).notify(anyString(), anyString(), eq(FAIL), eq(EMERGE_MODE));
  }

  @Test
  public void shouldDuplicateCommand() throws Exception {
    CommandImpl command = mock(CommandImpl.class);
    when(commandManager.createCommand(any(CommandImpl.class))).thenReturn(commandPromise);
    when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);

    presenter.onCommandDuplicate(command);

    verify(commandManager).createCommand(command);
  }

  @Test(expected = OperationException.class)
  public void shouldShowNotificationWhenFailedToDuplicateCommand() throws Exception {
    CommandImpl command = mock(CommandImpl.class);
    when(commandManager.createCommand(any(CommandImpl.class))).thenReturn(commandPromise);
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
    when(dialogFactory.createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(ConfirmCallback.class),
            nullable(CancelCallback.class)))
        .thenReturn(confirmDialog);
    ArgumentCaptor<ConfirmCallback> confirmCallbackCaptor =
        ArgumentCaptor.forClass(ConfirmCallback.class);

    CommandImpl command = mock(CommandImpl.class);
    String cmdName = "build";
    when(command.getName()).thenReturn(cmdName);
    when(commandManager.removeCommand(nullable(String.class))).thenReturn(voidPromise);

    // when
    presenter.onCommandRemove(command);

    // then
    verify(dialogFactory)
        .createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            confirmCallbackCaptor.capture(),
            isNull());
    confirmCallbackCaptor.getValue().accepted();
    verify(confirmDialog).show();
    verify(commandManager).removeCommand(cmdName);
  }

  @Test
  public void shouldNotRemoveCommandWhenCancelled() throws Exception {
    ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
    when(dialogFactory.createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(ConfirmCallback.class),
            nullable(CancelCallback.class)))
        .thenReturn(confirmDialog);
    CommandImpl command = mock(CommandImpl.class);

    presenter.onCommandRemove(command);

    verify(dialogFactory)
        .createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(ConfirmCallback.class),
            isNull());
    verify(confirmDialog).show();
    verify(commandManager, never()).removeCommand(anyString());
  }

  @Test(expected = OperationException.class)
  public void shouldShowNotificationWhenFailedToRemoveCommand() throws Exception {
    // given
    ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
    when(dialogFactory.createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(ConfirmCallback.class),
            nullable(CancelCallback.class)))
        .thenReturn(confirmDialog);
    ArgumentCaptor<ConfirmCallback> confirmCallbackCaptor =
        ArgumentCaptor.forClass(ConfirmCallback.class);

    when(commandManager.removeCommand(nullable(String.class))).thenReturn(voidPromise);

    // when
    presenter.onCommandRemove(mock(CommandImpl.class));

    // then
    verify(dialogFactory)
        .createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            confirmCallbackCaptor.capture(),
            isNull());

    confirmCallbackCaptor.getValue().accepted();

    verify(voidPromise).catchError(errorOperationCaptor.capture());
    errorOperationCaptor.getValue().apply(mock(PromiseError.class));
    verify(messages).unableRemove();
    verify(notificationManager).notify(anyString(), anyString(), eq(FAIL), eq(EMERGE_MODE));
  }

  private void verifyViewRefreshed() throws Exception {
    verify(refreshViewTask).delayAndSelectCommand(isNull(CommandImpl.class));
  }
}
