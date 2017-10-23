/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType;
import org.eclipse.che.ide.ext.java.client.refactoring.move.wizard.MovePresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeEnabledState;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangePreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringChange;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

/** @author Valeriy Svydenko */
@RunWith(GwtMockitoTestRunner.class)
public class PreviewPresenterTest {
  public static final String SESSION_ID = "sessionId";

  @Mock private PreviewView view;
  @Mock private Provider<MovePresenter> movePresenterProvider;
  @Mock private Provider<RenamePresenter> renamePresenterProvider;
  @Mock private DtoFactory dtoFactory;
  @Mock private RefactoringServiceClient refactoringService;
  @Mock private EditorAgent editorAgent;
  @Mock private RefactoringUpdater refactoringUpdater;

  @Mock private ChangeEnabledState changeEnableState;
  @Mock private RefactoringChange refactoringChanges;
  @Mock private RefactorInfo refactorInfo;
  @Mock private RefactoringSession refactoringSession;
  @Mock private RefactoringPreview refactoringPreview;
  @Mock private EditorPartPresenter editor;
  @Mock private Promise<RefactoringPreview> refactoringPreviewPromise;
  @Mock private RefactoringResult refactoringStatus;
  @Mock private TextEditor activeEditor;
  @Mock private ChangePreview changePreview;
  @Mock private Promise<RefactoringResult> refactoringStatusPromise;
  @Mock private Promise<ChangePreview> changePreviewPromise;
  @Mock private Promise<Void> updateAfterRefactoringPromise;
  @Mock private Promise<Boolean> fileTrackingSuspendEventPromise;
  @Mock private Promise<Void> handleMovingFilesPromise;
  @Mock private Promise<Void> changeEnableStatePromise;
  @Mock private ClientServerEventService clientServerEventService;

  @Captor private ArgumentCaptor<Operation<RefactoringPreview>> refactoringPreviewOperation;
  @Captor private ArgumentCaptor<Operation<RefactoringResult>> refactoringStatusOperation;
  @Captor private ArgumentCaptor<Operation<ChangePreview>> changePreviewOperation;
  @Captor private ArgumentCaptor<Operation<Void>> changeEnableStateOperation;
  @Captor private ArgumentCaptor<Operation<Boolean>> clientServerSuspendOperation;

  private PreviewPresenter presenter;

  @Before
  public void setUp() throws Exception {
    List<EditorPartPresenter> editors = new ArrayList<>();
    editors.add(editor);
    when(editorAgent.getActiveEditor()).thenReturn(activeEditor);
    when(dtoFactory.createDto(RefactoringSession.class)).thenReturn(refactoringSession);
    when(dtoFactory.createDto(ChangeEnabledState.class)).thenReturn(changeEnableState);
    when(dtoFactory.createDto(RefactoringChange.class)).thenReturn(refactoringChanges);
    when(refactoringService.getRefactoringPreview(refactoringSession))
        .thenReturn(refactoringPreviewPromise);
    when(refactoringService.applyRefactoring(anyObject())).thenReturn(refactoringStatusPromise);
    when(refactoringService.getChangePreview(refactoringChanges)).thenReturn(changePreviewPromise);
    when(refactoringService.changeChangeEnabledState(changeEnableState))
        .thenReturn(changeEnableStatePromise);
    when(editorAgent.getOpenedEditors()).thenReturn(editors);
    when(clientServerEventService.sendFileTrackingSuspendEvent())
        .thenReturn(fileTrackingSuspendEventPromise);
    List<ChangeInfo> changes = new ArrayList<>();
    when(refactoringStatus.getChanges()).thenReturn(changes);
    when(refactoringUpdater.handleMovingFiles(anyList())).thenReturn(handleMovingFilesPromise);
    when(refactoringUpdater.updateAfterRefactoring(anyList()))
        .thenReturn(updateAfterRefactoringPromise);

    presenter =
        new PreviewPresenter(
            view,
            movePresenterProvider,
            renamePresenterProvider,
            dtoFactory,
            editorAgent,
            refactoringUpdater,
            refactoringService,
            clientServerEventService);
  }

  @Test
  public void constructorShouldBePerformed() throws Exception {
    verify(view).setDelegate(presenter);
  }

  @Test
  public void viewShouldBeShowed() throws Exception {
    presenter.show(SESSION_ID, refactorInfo);

    verify(refactoringSession).setSessionId(SESSION_ID);
    verify(refactoringPreviewPromise).then(refactoringPreviewOperation.capture());
    refactoringPreviewOperation.getValue().apply(refactoringPreview);
    verify(view).setTreeOfChanges(refactoringPreview);
    verify(view).show();
  }

  @Test
  public void acceptButtonActionShouldBePerformed() throws Exception {
    VirtualFile virtualFile = Mockito.mock(VirtualFile.class);
    EditorInput editorInput = Mockito.mock(EditorInput.class);
    when(refactoringStatus.getSeverity()).thenReturn(OK);

    when(editor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(virtualFile);

    presenter.onAcceptButtonClicked();

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);
    verify(refactoringStatusPromise).then(refactoringStatusOperation.capture());
    refactoringStatusOperation.getValue().apply(refactoringStatus);
    verify(clientServerEventService).sendFileTrackingResumeEvent();
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(refactoringUpdater).updateAfterRefactoring(anyList());
    verify(view).hide();
  }

  @Test
  public void acceptButtonActionShouldBeNotPerformedIfStatusIsNotOK() throws Exception {
    VirtualFile virtualFile = Mockito.mock(VirtualFile.class);
    EditorInput editorInput = Mockito.mock(EditorInput.class);
    when(refactoringStatus.getSeverity()).thenReturn(2);
    when(editor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(virtualFile);

    presenter.onAcceptButtonClicked();

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);
    verify(refactoringStatusPromise).then(refactoringStatusOperation.capture());
    refactoringStatusOperation.getValue().apply(refactoringStatus);
    verify(clientServerEventService).sendFileTrackingResumeEvent();
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(view, never()).hide();
    verify(editor, never()).getEditorInput();
    verify(editorInput, never()).getFile();
    verify(virtualFile, never()).getLocation();
    verify(view).showErrorMessage(refactoringStatus);
  }

  @Test
  public void showMoveWizardIfOnBackButtonClicked() throws Exception {
    MovePresenter movePresenter = Mockito.mock(MovePresenter.class);
    when(refactorInfo.getMoveType()).thenReturn(MoveType.REFACTOR_MENU);
    when(movePresenterProvider.get()).thenReturn(movePresenter);

    presenter.show(SESSION_ID, refactorInfo);
    presenter.onBackButtonClicked();

    verify(view).hide();
    verify(movePresenter).show(refactorInfo);
  }

  @Test
  public void showRenameWizardIfOnBackButtonClicked() throws Exception {
    RenamePresenter renamePresenter = Mockito.mock(RenamePresenter.class);
    when(refactorInfo.getMoveType()).thenReturn(null);
    when(renamePresenterProvider.get()).thenReturn(renamePresenter);

    presenter.show(SESSION_ID, refactorInfo);
    presenter.onBackButtonClicked();

    verify(view).hide();
    verify(renamePresenter).show(refactorInfo);
  }

  @Test
  public void sendRequestToServerIfEnablingStateOfChangeIsChanging() throws Exception {
    when(refactoringPreview.getId()).thenReturn("id");
    when(refactoringPreview.isEnabled()).thenReturn(true);
    when(refactoringSession.getSessionId()).thenReturn(SESSION_ID);

    presenter.show(SESSION_ID, refactorInfo);
    presenter.onEnabledStateChanged(refactoringPreview);

    verify(changeEnableState).setChangeId("id");
    changeEnableState.setSessionId(SESSION_ID);
    changeEnableState.setEnabled(true);

    verify(changeEnableStatePromise).then(changeEnableStateOperation.capture());
    changeEnableStateOperation.getValue().apply(any());
    verify(refactoringChanges).setChangeId("id");
    verify(refactoringChanges).setSessionId(SESSION_ID);
    verify(changePreviewPromise).then(changePreviewOperation.capture());
    changePreviewOperation.getValue().apply(changePreview);
    verify(view).showDiff(changePreview);
  }

  @Test
  public void performIfSelectionChanged() throws Exception {
    when(refactoringPreview.getId()).thenReturn("id");
    when(refactoringPreview.isEnabled()).thenReturn(true);
    when(refactoringSession.getSessionId()).thenReturn(SESSION_ID);

    presenter.show(SESSION_ID, refactorInfo);
    presenter.onSelectionChanged(refactoringPreview);

    verify(changePreviewPromise).then(changePreviewOperation.capture());
    changePreviewOperation.getValue().apply(changePreview);
    verify(view).showDiff(changePreview);
  }

  @Test
  public void focusShouldBeSetAfterClosingTheEditor() throws Exception {
    presenter.onCancelButtonClicked();

    verify(activeEditor).setFocus();
  }
}
