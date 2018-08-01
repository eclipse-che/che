/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import static org.eclipse.che.ide.api.editor.events.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.JAVA_ELEMENT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.ERROR;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.FATAL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.INFO;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.WARNING;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedModel;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatusEntry;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Alexander Andrinko
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class JavaRefactoringRenameTest {

  private static final String NEW_JAVA_CLASS_NAME = "NewJavaTest.java";
  private static final String SESSION_ID = "some session id";
  private static final int CURSOR_OFFSET = 10;

  // variables for constructor
  @Mock private RenamePresenter renamePresenter;
  @Mock private JavaLocalizationConstant locale;
  @Mock private DialogFactory dialogFactory;
  @Mock private RefactoringServiceClient refactoringServiceClient;
  @Mock private DtoFactory dtoFactory;
  @Mock private RefactoringUpdater refactoringUpdater;
  @Mock private NotificationManager notificationManager;
  @Mock private ClientServerEventService clientServerEventService;

  @Mock private CreateRenameRefactoring createRenameRefactoringDto;
  @Mock private LinkedRenameRefactoringApply linkedRenameRefactoringApplyDto;

  @Mock(extraInterfaces = {HasLinkedMode.class, EditorWithAutoSave.class, UndoableEditor.class})
  private TextEditor textEditor;

  @Mock private EditorInput editorInput;
  @Mock private File file;
  @Mock private Container srcFolder;
  @Mock private Project relatedProject;
  @Mock private Promise<RenameRefactoringSession> createRenamePromise;
  @Mock private RenameRefactoringSession session;
  @Mock private LinkedModeModel linkedModel;
  @Mock private Promise<RefactoringResult> applyModelPromise;
  @Mock private RefactoringResult result;
  @Mock private FileEvent fileEvent;
  @Mock private RefactoringStatusEntry entry;
  @Mock private LinkedMode linkedMode;
  @Mock private LinkedModel editorLinkedModel;
  @Mock private EventBus eventBus;
  @Mock private Document document;
  @Mock private HandlesUndoRedo undoRedo;
  @Mock private Promise<Void> updateAfterRefactoringPromise;
  @Mock private Promise<Boolean> fileTrackingSuspendEventPromise;
  @Mock private Promise<Boolean> fileTrackingResumeEventPromise;
  @Mock private Promise<Void> handleMovingFilesPromise;

  @Captor private ArgumentCaptor<Operation<RenameRefactoringSession>> renameRefCaptor;
  @Captor private ArgumentCaptor<LinkedMode.LinkedModeListener> inputArgumentCaptor;
  @Captor private ArgumentCaptor<Operation<RefactoringResult>> refactoringStatusCaptor;
  @Captor private ArgumentCaptor<Operation<PromiseError>> refactoringErrorCaptor;
  @Captor private ArgumentCaptor<Operation<Boolean>> clientServerSuspendOperation;
  @Captor private ArgumentCaptor<Operation<Boolean>> clientServerResumeOperation;
  @Captor private ArgumentCaptor<Operation<Void>> updateAfterRefactoringOperation;

  @InjectMocks private JavaRefactoringRename refactoringRename;

  @Before
  public void setUp() {
    when(dtoFactory.createDto(CreateRenameRefactoring.class))
        .thenReturn(createRenameRefactoringDto);
    when(dtoFactory.createDto(LinkedRenameRefactoringApply.class))
        .thenReturn(linkedRenameRefactoringApplyDto);
    when(textEditor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(file);
    when(file.getName()).thenReturn("A.java");
    when(file.getExtension()).thenReturn("java");
    when(file.getParentWithMarker(eq(SourceFolderMarker.ID))).thenReturn(Optional.of(srcFolder));
    when(file.getLocation()).thenReturn(Path.valueOf("/project/src/a/b/c/A.java"));
    when(file.getResourceType()).thenReturn(Resource.FILE);
    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/project/src"));
    when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/project"));
    when(refactoringServiceClient.createRenameRefactoring(createRenameRefactoringDto))
        .thenReturn(createRenamePromise);
    when(createRenamePromise.then((Operation<RenameRefactoringSession>) any()))
        .thenReturn(createRenamePromise);
    when(applyModelPromise.then((Operation<RefactoringResult>) any()))
        .thenReturn(applyModelPromise);
    when(session.getLinkedModeModel()).thenReturn(linkedModel);
    when(refactoringServiceClient.applyLinkedModeRename(linkedRenameRefactoringApplyDto))
        .thenReturn(applyModelPromise);
    when(textEditor.getCursorOffset()).thenReturn(CURSOR_OFFSET);
    when(document.getContentRange(anyInt(), anyInt())).thenReturn(NEW_JAVA_CLASS_NAME);
    when(((HasLinkedMode) textEditor).getLinkedMode()).thenReturn(linkedMode);
    when(((HasLinkedMode) textEditor).createLinkedModel()).thenReturn(editorLinkedModel);
    when(textEditor.getDocument()).thenReturn(document);
    when(document.getFile()).thenReturn(file);

    when(result.getEntries()).thenReturn(Collections.singletonList(entry));

    when(clientServerEventService.sendFileTrackingSuspendEvent())
        .thenReturn(fileTrackingSuspendEventPromise);
    when(clientServerEventService.sendFileTrackingResumeEvent())
        .thenReturn(fileTrackingResumeEventPromise);
    when(refactoringUpdater.handleMovingFiles(anyList())).thenReturn(handleMovingFilesPromise);
    when(refactoringUpdater.updateAfterRefactoring(anyList()))
        .thenReturn(updateAfterRefactoringPromise);
    when(((UndoableEditor) textEditor).getUndoRedo()).thenReturn(undoRedo);
  }

  @Test
  public void renameRefactoringShouldBeAppliedSuccess() throws OperationException {
    when(result.getSeverity()).thenReturn(OK);
    List<ChangeInfo> changes = new ArrayList<>();
    when(result.getChanges()).thenReturn(changes);

    refactoringRename.refactor(textEditor);

    mainCheckRenameRefactoring();

    verify(refactoringUpdater)
        .updateAfterRefactoring(org.mockito.ArgumentMatchers.<List<ChangeInfo>>any());
    verify(eventBus).addHandler(FileEvent.TYPE, refactoringRename);

    verify(updateAfterRefactoringPromise).then(updateAfterRefactoringOperation.capture());
    updateAfterRefactoringOperation.getValue().apply(null);
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  @Test
  public void turnOffLinkedEditorModeWhenEditorIsClosed() throws Exception {
    when(fileEvent.getOperationType()).thenReturn(CLOSE);
    when(fileEvent.getFile()).thenReturn(file);

    refactoringRename.refactor(textEditor);
    refactoringRename.onFileOperation(fileEvent);

    assertFalse(refactoringRename.isActiveLinkedEditor());
  }

  @Test
  public void renameRefactoringShouldBeAppliedSuccessAndShowWizard() throws OperationException {
    refactoringRename.refactor(textEditor);
    refactoringRename.refactor(textEditor);

    verify(refactoringServiceClient, times(2)).createRenameRefactoring(createRenameRefactoringDto);
    verify(createRenamePromise, times(2)).then(renameRefCaptor.capture());
    renameRefCaptor.getValue().apply(session);
    verify(renamePresenter).show(session);
  }

  @Test
  public void renameRefactoringShouldBeShowErrorWindow() throws OperationException {
    PromiseError arg = Mockito.mock(PromiseError.class);
    MessageDialog dialog = Mockito.mock(MessageDialog.class);

    when(locale.renameRename()).thenReturn("renameTitle");
    when(locale.renameOperationUnavailable()).thenReturn("renameBody");
    when(dialogFactory.createMessageDialog(anyString(), anyString(), anyObject()))
        .thenReturn(dialog);

    refactoringRename.refactor(textEditor);

    verify(createRenamePromise).then(renameRefCaptor.capture());
    renameRefCaptor.getValue().apply(session);
    verify(createRenamePromise).catchError(refactoringErrorCaptor.capture());
    refactoringErrorCaptor.getValue().apply(arg);

    verify(dialogFactory).createMessageDialog("renameTitle", "renameBody", null);
    verify(dialog).show();
  }

  @Test
  public void renameRefactoringShouldBeFailedByFatalError() throws OperationException {
    when(result.getSeverity()).thenReturn(FATAL);

    refactoringRename.refactor(textEditor);

    mainCheckRenameRefactoring();
    verify(result, times(1)).getSeverity();
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  @Test
  public void renameRefactoringShouldBeFailedByError() throws OperationException {
    ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
    when(result.getSeverity()).thenReturn(ERROR);

    when(dialogFactory.createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(ConfirmCallback.class),
            nullable(CancelCallback.class)))
        .thenReturn(confirmDialog);

    refactoringRename.refactor(textEditor);

    verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
    verify(createRenamePromise).then(renameRefCaptor.capture());
    renameRefCaptor.getValue().apply(session);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    verify(linkedMode).addListener(inputArgumentCaptor.capture());
    inputArgumentCaptor.getValue().onLinkedModeExited(true, 0, 1);

    verify(refactoringServiceClient).applyLinkedModeRename(linkedRenameRefactoringApplyDto);

    verify(applyModelPromise).then(refactoringStatusCaptor.capture());
    refactoringStatusCaptor.getValue().apply(result);

    verify(locale).warningOperationTitle();
    verify(locale).renameWithWarnings();
    verify(locale).showRenameWizard();
    verify(locale).buttonCancel();
    verify(dialogFactory)
        .createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            org.mockito.ArgumentMatchers.<ConfirmCallback>anyObject(),
            org.mockito.ArgumentMatchers.<CancelCallback>anyObject());
    verify(confirmDialog).show();
  }

  @Test
  public void renameRefactoringShouldBeWithWarningOrErrorStatus() throws OperationException {
    ConfirmDialog confirmDialog = mock(ConfirmDialog.class);

    when(result.getSeverity()).thenReturn(WARNING);
    when(dialogFactory.createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(ConfirmCallback.class),
            nullable(CancelCallback.class)))
        .thenReturn(confirmDialog);

    refactoringRename.refactor(textEditor);

    verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
    verify(createRenamePromise).then(renameRefCaptor.capture());
    renameRefCaptor.getValue().apply(session);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    verify(linkedMode).addListener(inputArgumentCaptor.capture());
    inputArgumentCaptor.getValue().onLinkedModeExited(true, 0, 1);

    verify(refactoringServiceClient).applyLinkedModeRename(linkedRenameRefactoringApplyDto);

    verify(applyModelPromise).then(refactoringStatusCaptor.capture());
    refactoringStatusCaptor.getValue().apply(result);

    verify(locale).warningOperationTitle();
    verify(locale).renameWithWarnings();
    verify(locale).showRenameWizard();
    verify(locale).buttonCancel();
    verify(dialogFactory)
        .createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            org.mockito.ArgumentMatchers.<ConfirmCallback>anyObject(),
            org.mockito.ArgumentMatchers.<CancelCallback>anyObject());
    verify(confirmDialog).show();
  }

  @Test
  public void renameRefactoringShouldBeWithINFO() throws OperationException {
    when(result.getSeverity()).thenReturn(INFO);

    refactoringRename.refactor(textEditor);

    mainCheckRenameRefactoring();
    verify(result).getSeverity();

    verify(updateAfterRefactoringPromise).then(updateAfterRefactoringOperation.capture());
    updateAfterRefactoringOperation.getValue().apply(null);
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  @Test
  public void renameRefactoringShouldBeWithOK() throws OperationException {
    when(result.getSeverity()).thenReturn(OK);

    refactoringRename.refactor(textEditor);

    mainCheckRenameRefactoring();
    verify(result).getSeverity();

    verify(updateAfterRefactoringPromise).then(updateAfterRefactoringOperation.capture());
    updateAfterRefactoringOperation.getValue().apply(null);
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  @Test
  public void shouldUndoRenameRefactoringWhenUserEnterNewNameButEscapeRename()
      throws OperationException {
    // use case: user hit Refactoring -> Rename action, type a new name and press escape button

    refactoringRename.refactor(textEditor);

    verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
    verify(createRenamePromise).then(renameRefCaptor.capture());
    renameRefCaptor.getValue().apply(session);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    verify(linkedMode).addListener(inputArgumentCaptor.capture());
    inputArgumentCaptor.getValue().onLinkedModeExited(false, 0, 5);

    verify(fileTrackingResumeEventPromise).then(clientServerResumeOperation.capture());
    clientServerResumeOperation.getValue().apply(null);

    verify(refactoringServiceClient, never()).applyLinkedModeRename(anyObject());
    verify((UndoableEditor) textEditor).getUndoRedo();
    verify(undoRedo).undo();
    verify((EditorWithAutoSave) textEditor).enableAutoSave();
  }

  @Test
  public void shouldDoNotApplyUndoOperationWhenUserNotEnterNewNameAndEscapeRename()
      throws OperationException {
    // use case: user hit Refactoring -> Rename action, DO NOT type a new name and press escape
    // button

    refactoringRename.refactor(textEditor);

    verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
    verify(createRenamePromise).then(renameRefCaptor.capture());
    renameRefCaptor.getValue().apply(session);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    verify(linkedMode).addListener(inputArgumentCaptor.capture());
    inputArgumentCaptor.getValue().onLinkedModeExited(false, -1, -1);

    verify(fileTrackingResumeEventPromise).then(clientServerResumeOperation.capture());
    clientServerResumeOperation.getValue().apply(null);

    verify(refactoringServiceClient, never()).applyLinkedModeRename(anyObject());
    verify((UndoableEditor) textEditor, never()).getUndoRedo();
    verify(undoRedo, never()).undo();
    verify((EditorWithAutoSave) textEditor).enableAutoSave();
  }

  private void mainCheckRenameRefactoring() throws OperationException {
    verify(dtoFactory).createDto(CreateRenameRefactoring.class);
    verify(textEditor).getCursorOffset();
    verify(createRenameRefactoringDto).setOffset(CURSOR_OFFSET);
    verify(createRenameRefactoringDto).setRefactorLightweight(true);
    verify(textEditor).getEditorInput();
    verify(editorInput).getFile();
    verify(createRenameRefactoringDto).setPath(eq("a.b.c.A"));
    verify(createRenameRefactoringDto).setProjectPath(eq("/project"));
    verify(createRenameRefactoringDto).setProjectPath(eq("/project"));
    verify(createRenameRefactoringDto).setType(JAVA_ELEMENT);

    verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
    verify(createRenamePromise).then(renameRefCaptor.capture());
    renameRefCaptor.getValue().apply(session);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    verify(session).getLinkedModeModel();

    verify(linkedMode).addListener(inputArgumentCaptor.capture());
    inputArgumentCaptor.getValue().onLinkedModeExited(true, 0, 1);
    verify(dtoFactory).createDto(LinkedRenameRefactoringApply.class);
    linkedRenameRefactoringApplyDto.setNewName(NEW_JAVA_CLASS_NAME);
    linkedRenameRefactoringApplyDto.setSessionId(SESSION_ID);

    verify(refactoringServiceClient).applyLinkedModeRename(linkedRenameRefactoringApplyDto);

    verify(applyModelPromise).then(refactoringStatusCaptor.capture());
    refactoringStatusCaptor.getValue().apply(result);
  }
}
