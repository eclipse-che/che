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
package org.eclipse.che.ide.command.editor;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.WARNING;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.command.CommandRemovedEvent;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.command.editor.page.CommandEditorPage.DirtyStateListener;
import org.eclipse.che.ide.command.editor.page.commandline.CommandLinePage;
import org.eclipse.che.ide.command.editor.page.goal.GoalPage;
import org.eclipse.che.ide.command.editor.page.name.NamePage;
import org.eclipse.che.ide.command.editor.page.previewurl.PreviewUrlPage;
import org.eclipse.che.ide.command.editor.page.project.ProjectsPage;
import org.eclipse.che.ide.command.node.CommandFileNode;
import org.eclipse.che.ide.command.node.NodeFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link CommandEditor}. */
@RunWith(MockitoJUnitRunner.class)
public class CommandEditorTest {

  private static final String EDITED_COMMAND_NAME = "build";

  @Mock private CommandEditorView view;
  @Mock private WorkspaceAgent workspaceAgent;
  @Mock private IconRegistry iconRegistry;
  @Mock private CommandManager commandManager;
  @Mock private NamePage namePage;
  @Mock private CommandLinePage commandLinePage;
  @Mock private GoalPage goalPage;
  @Mock private ProjectsPage projectsPage;
  @Mock private PreviewUrlPage previewUrlPage;
  @Mock private NotificationManager notificationManager;
  @Mock private DialogFactory dialogFactory;
  @Mock private EditorAgent editorAgent;
  @Mock private CoreLocalizationConstant localizationConstants;
  @Mock private EditorMessages editorMessages;
  @Mock private NodeFactory nodeFactory;
  @Mock private EventBus eventBus;

  @InjectMocks private CommandEditor editor;

  @Mock private EditorInput editorInput;
  @Mock private CommandFileNode editedCommandFile;
  @Mock private CommandImpl editedCommand;
  @Mock private EditorAgent.OpenEditorCallback openEditorCallback;

  @Mock private HandlerRegistration handlerRegistration;
  @Mock private Promise<CommandImpl> commandPromise;
  @Captor private ArgumentCaptor<Operation<CommandImpl>> operationCaptor;
  @Captor private ArgumentCaptor<Operation<PromiseError>> errorOperationCaptor;

  @Before
  public void setUp() throws Exception {
    when(eventBus.addHandler(eq(CommandRemovedEvent.getType()), eq(editor)))
        .thenReturn(handlerRegistration);
    when(editedCommand.getName()).thenReturn(EDITED_COMMAND_NAME);
    when(editedCommand.getApplicableContext()).thenReturn(mock(ApplicableContext.class));
    when(editedCommandFile.getData()).thenReturn(editedCommand);
    when(editorInput.getFile()).thenReturn(editedCommandFile);

    editor.init(editorInput, openEditorCallback);
  }

  @Test
  public void shouldBeInitialized() throws Exception {
    verify(view).setDelegate(editor);
    verify(eventBus).addHandler(CommandRemovedEvent.getType(), editor);

    verifyPagesInitialized();
  }

  @Test
  public void shouldExposeViewOnGo() throws Exception {
    AcceptsOneWidget container = mock(AcceptsOneWidget.class);
    editor.go(container);

    verify(container).setWidget(view);
  }

  @Test
  public void shouldReturnTitle() throws Exception {
    editor.getTitle();

    verify(editorInput).getName();
  }

  @Test
  public void shouldReturnTitleTooltip() throws Exception {
    editor.getTitleToolTip();

    verify(editorInput).getName();
  }

  @Test
  public void shouldReturnView() throws Exception {
    assertEquals(view, editor.getView());
  }

  @Test
  public void shouldSaveCommand() throws Exception {
    when(commandManager.updateCommand(anyString(), eq(editor.editedCommand)))
        .thenReturn(commandPromise);
    when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);

    editor.doSave();

    verify(commandManager).updateCommand(anyString(), eq(editor.editedCommand));
    verifyPagesInitialized();
  }

  @Test(expected = OperationException.class)
  public void shouldShowNotificationWhenFailedToSaveCommand() throws Exception {
    when(commandManager.updateCommand(anyString(), eq(editor.editedCommand)))
        .thenReturn(commandPromise);
    when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);

    editor.doSave();

    verify(commandPromise).catchError(errorOperationCaptor.capture());
    errorOperationCaptor.getValue().apply(mock(PromiseError.class));
    verify(editorMessages).editorMessageUnableToSave();
    verify(notificationManager).notify(anyString(), anyString(), WARNING, EMERGE_MODE);
  }

  @Test
  public void shouldCloseEditor() throws Exception {
    editor.close(true);

    verify(workspaceAgent).removePart(editor);
  }

  @Test
  public void shouldCloseEditorWhenCancellingRequested() throws Exception {
    editor.onCommandCancel();

    verify(workspaceAgent).removePart(editor);
  }

  @Test
  public void shouldSaveCommandWhenSavingRequested() throws Exception {
    when(commandManager.updateCommand(anyString(), eq(editor.editedCommand)))
        .thenReturn(commandPromise);
    when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);

    editor.onCommandSave();

    verify(commandManager).updateCommand(anyString(), eq(editor.editedCommand));
  }

  @Test
  public void shouldCloseEditorWhenEditedCommandRemoved() throws Exception {
    CommandImpl removedCommand = mock(CommandImpl.class);
    when(removedCommand.getName()).thenReturn(EDITED_COMMAND_NAME);
    CommandRemovedEvent event = mock(CommandRemovedEvent.class);
    when(event.getCommand()).thenReturn(removedCommand);

    editor.onCommandRemoved(event);

    verify(editorAgent).closeEditor(editor);
    verify(handlerRegistration).removeHandler();
  }

  private void verifyPagesInitialized() throws Exception {
    verify(commandLinePage).setDirtyStateListener(any(DirtyStateListener.class));
    verify(goalPage).setDirtyStateListener(any(DirtyStateListener.class));
    verify(projectsPage).setDirtyStateListener(any(DirtyStateListener.class));
    verify(previewUrlPage).setDirtyStateListener(any(DirtyStateListener.class));

    verify(commandLinePage).edit(editor.editedCommand);
    verify(goalPage).edit(editor.editedCommand);
    verify(projectsPage).edit(editor.editedCommand);
    verify(previewUrlPage).edit(editor.editedCommand);
  }
}
