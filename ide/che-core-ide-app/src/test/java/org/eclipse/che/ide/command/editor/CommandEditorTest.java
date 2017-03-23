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
package org.eclipse.che.ide.command.editor;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandImpl.ApplicableContext;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.WARNING;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests for {@link CommandEditor}. */
@RunWith(GwtMockitoTestRunner.class)
public class CommandEditorTest {

    private static final String EDITED_COMMAND_NAME = "build";

    @Mock
    private CommandEditorView        view;
    @Mock
    private WorkspaceAgent           workspaceAgent;
    @Mock
    private IconRegistry             iconRegistry;
    @Mock
    private CommandManager           commandManager;
    @Mock
    private NamePage                 namePage;
    @Mock
    private CommandLinePage          commandLinePage;
    @Mock
    private GoalPage                 goalPage;
    @Mock
    private ProjectsPage             projectsPage;
    @Mock
    private PreviewUrlPage           previewUrlPage;
    @Mock
    private NotificationManager      notificationManager;
    @Mock
    private DialogFactory            dialogFactory;
    @Mock
    private EditorAgent              editorAgent;
    @Mock
    private CoreLocalizationConstant localizationConstants;
    @Mock
    private EditorMessages           editorMessages;
    @Mock
    private NodeFactory              nodeFactory;

    @InjectMocks
    private CommandEditor editor;

    @Mock
    private EditorInput                    editorInput;
    @Mock
    private CommandFileNode                editedCommandFile;
    @Mock
    private CommandImpl                    editedCommand;
    @Mock
    private EditorAgent.OpenEditorCallback openEditorCallback;

    @Mock
    private Promise<CommandImpl>                    commandPromise;
    @Captor
    private ArgumentCaptor<Operation<CommandImpl>>  operationCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>> errorOperationCaptor;

    @Before
    public void setUp() throws Exception {
        when(editedCommand.getName()).thenReturn(EDITED_COMMAND_NAME);
        when(editedCommand.getApplicableContext()).thenReturn(mock(ApplicableContext.class));
        when(editedCommandFile.getData()).thenReturn(editedCommand);
        when(editorInput.getFile()).thenReturn(editedCommandFile);

        editor.init(editorInput, openEditorCallback);
    }

    @Test
    public void shouldBeInitialized() throws Exception {
        verify(view).setDelegate(editor);
        verify(commandManager).addCommandChangedListener(editor);

        verify(commandLinePage).setDirtyStateListener(any(DirtyStateListener.class));
        verify(goalPage).setDirtyStateListener(any(DirtyStateListener.class));
        verify(projectsPage).setDirtyStateListener(any(DirtyStateListener.class));
        verify(previewUrlPage).setDirtyStateListener(any(DirtyStateListener.class));

        verify(commandLinePage).edit(editor.editedCommand);
        verify(goalPage).edit(editor.editedCommand);
        verify(projectsPage).edit(editor.editedCommand);
        verify(previewUrlPage).edit(editor.editedCommand);
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
        when(commandManager.updateCommand(anyString(), eq(editor.editedCommand))).thenReturn(commandPromise);
        when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);

        editor.doSave();

        verify(commandManager).updateCommand(anyString(), eq(editor.editedCommand));
    }

    @Test(expected = OperationException.class)
    public void shouldShowNotificationWhenFailedToSaveCommand() throws Exception {
        when(commandManager.updateCommand(anyString(), eq(editor.editedCommand))).thenReturn(commandPromise);
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
        when(commandManager.updateCommand(anyString(), eq(editor.editedCommand))).thenReturn(commandPromise);
        when(commandPromise.then(any(Operation.class))).thenReturn(commandPromise);

        editor.onCommandSave();

        verify(commandManager).updateCommand(anyString(), eq(editor.editedCommand));
    }

    @Test
    public void shouldCloseEditorWhenEditedCommandRemoved() throws Exception {
        CommandImpl removedCommand = mock(CommandImpl.class);
        when(removedCommand.getName()).thenReturn(EDITED_COMMAND_NAME);

        editor.onCommandRemoved(removedCommand);

        verify(editorAgent).closeEditor(editor);
    }
}
