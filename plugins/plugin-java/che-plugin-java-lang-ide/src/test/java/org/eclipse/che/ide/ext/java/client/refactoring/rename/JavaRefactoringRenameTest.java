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
package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import com.google.common.base.Optional;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.event.FileEvent;
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
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedModel;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.api.dialogs.MessageDialog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.JAVA_ELEMENT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.ERROR;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.FATAL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.INFO;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.WARNING;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Andrinko
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class JavaRefactoringRenameTest {

    private static final String NEW_JAVA_CLASS_NAME = "NewJavaTest.java";
    private static final String SESSION_ID          = "some session id";
    private static final int    CURSOR_OFFSET       = 10;

    //variables for constructor
    @Mock
    private RenamePresenter          renamePresenter;
    @Mock
    private JavaLocalizationConstant locale;
    @Mock
    private DialogFactory            dialogFactory;
    @Mock
    private RefactoringServiceClient refactoringServiceClient;
    @Mock
    private DtoFactory               dtoFactory;
    @Mock
    private RefactoringUpdater       refactoringUpdater;
    @Mock
    private NotificationManager notificationManager;

    @Mock
    private CreateRenameRefactoring           createRenameRefactoringDto;
    @Mock
    private LinkedRenameRefactoringApply      linkedRenameRefactoringApplyDto;
    @Mock(extraInterfaces = {HasLinkedMode.class, EditorWithAutoSave.class})
    private TextEditor                        textEditor;
    @Mock
    private EditorInput                       editorInput;
    @Mock
    private File                              file;
    @Mock
    private Container                         srcFolder;
    @Mock
    private Project                           relatedProject;
    @Mock
    private Promise<RenameRefactoringSession> createRenamePromise;
    @Mock
    private RenameRefactoringSession          session;
    @Mock
    private LinkedModeModel                   linkedModel;
    @Mock
    private Promise<RefactoringResult>        applyModelPromise;
    @Mock
    private RefactoringResult                 result;
    @Mock
    private FileEvent                         fileEvent;
    @Mock
    private RefactoringStatusEntry            entry;
    @Mock
    private LinkedMode                        linkedMode;
    @Mock
    private LinkedModel                       editorLinkedModel;
    @Mock
    private EventBus                          eventBus;
    @Mock
    private Document                          document;

    @Captor
    private ArgumentCaptor<Operation<RenameRefactoringSession>> renameRefCaptor;
    @Captor
    private ArgumentCaptor<LinkedMode.LinkedModeListener>       inputArgumentCaptor;
    @Captor
    private ArgumentCaptor<Operation<RefactoringResult>>        refactoringStatusCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>             refactoringErrorCaptor;

    @InjectMocks
    private JavaRefactoringRename refactoringRename;

    @Before
    public void setUp() {
        when(dtoFactory.createDto(CreateRenameRefactoring.class)).thenReturn(createRenameRefactoringDto);
        when(dtoFactory.createDto(LinkedRenameRefactoringApply.class)).thenReturn(linkedRenameRefactoringApplyDto);
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
        when(refactoringServiceClient.createRenameRefactoring(createRenameRefactoringDto)).thenReturn(createRenamePromise);
        when(createRenamePromise.then((Operation<RenameRefactoringSession>)any())).thenReturn(createRenamePromise);
        when(applyModelPromise.then((Operation<RefactoringResult>)any())).thenReturn(applyModelPromise);
        when(session.getLinkedModeModel()).thenReturn(linkedModel);
        when(refactoringServiceClient.applyLinkedModeRename(linkedRenameRefactoringApplyDto)).thenReturn(applyModelPromise);
        when(textEditor.getCursorOffset()).thenReturn(CURSOR_OFFSET);
        when(document.getContentRange(anyInt(), anyInt())).thenReturn(NEW_JAVA_CLASS_NAME);
        when(((HasLinkedMode)textEditor).getLinkedMode()).thenReturn(linkedMode);
        when(((HasLinkedMode)textEditor).createLinkedModel()).thenReturn(editorLinkedModel);
        when(textEditor.getDocument()).thenReturn(document);
        when(document.getFile()).thenReturn(file);

        when(result.getEntries()).thenReturn(Collections.singletonList(entry));
    }

    @Test
    public void renameRefactoringShouldBeAppliedSuccess() throws OperationException {
        when(result.getSeverity()).thenReturn(OK);
        List<ChangeInfo> changes = new ArrayList<>();
        when(result.getChanges()).thenReturn(changes);

        refactoringRename.refactor(textEditor);

        mainCheckRenameRefactoring();

        verify(refactoringUpdater).updateAfterRefactoring(Matchers.<List<ChangeInfo>>any());
        verify(eventBus).addHandler(FileEvent.TYPE, refactoringRename);
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
        when(result.getSeverity()).thenReturn(OK);

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

        when(result.getSeverity()).thenReturn(OK);
        when(locale.renameRename()).thenReturn("renameTitle");
        when(locale.renameOperationUnavailable()).thenReturn("renameBody");
        when(dialogFactory.createMessageDialog(anyString(), anyString(), anyObject())).thenReturn(dialog);

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
    }

    @Test
    public void renameRefactoringShouldBeFailedByError() throws OperationException {
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);
        when(result.getSeverity()).thenReturn(ERROR);

        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject())).thenReturn(confirmDialog);

        refactoringRename.refactor(textEditor);

        verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
        verify(createRenamePromise).then(renameRefCaptor.capture());
        renameRefCaptor.getValue().apply(session);

        verify(linkedMode).addListener(inputArgumentCaptor.capture());
        inputArgumentCaptor.getValue().onLinkedModeExited(true, 0, 1);

        verify(refactoringServiceClient).applyLinkedModeRename(linkedRenameRefactoringApplyDto);

        verify(applyModelPromise).then(refactoringStatusCaptor.capture());
        refactoringStatusCaptor.getValue().apply(result);

        verify(locale).warningOperationTitle();
        verify(locale).renameWithWarnings();
        verify(locale).showRenameWizard();
        verify(locale).buttonCancel();
        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  Matchers.<ConfirmCallback>anyObject(),
                                                  Matchers.<CancelCallback>anyObject());
        verify(confirmDialog).show();
    }

    @Test
    public void renameRefactoringShouldBeWithWarningOrErrorStatus() throws OperationException {
        ConfirmDialog confirmDialog = mock(ConfirmDialog.class);

        when(result.getSeverity()).thenReturn(WARNING);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject())).thenReturn(confirmDialog);

        refactoringRename.refactor(textEditor);

        verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
        verify(createRenamePromise).then(renameRefCaptor.capture());
        renameRefCaptor.getValue().apply(session);

        verify(linkedMode).addListener(inputArgumentCaptor.capture());
        inputArgumentCaptor.getValue().onLinkedModeExited(true, 0, 1);

        verify(refactoringServiceClient).applyLinkedModeRename(linkedRenameRefactoringApplyDto);

        verify(applyModelPromise).then(refactoringStatusCaptor.capture());
        refactoringStatusCaptor.getValue().apply(result);

        verify(locale).warningOperationTitle();
        verify(locale).renameWithWarnings();
        verify(locale).showRenameWizard();
        verify(locale).buttonCancel();
        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  Matchers.<ConfirmCallback>anyObject(),
                                                  Matchers.<CancelCallback>anyObject());
        verify(confirmDialog).show();
    }

    @Test
    public void renameRefactoringShouldBeWithINFO() throws OperationException {
        when(result.getSeverity()).thenReturn(INFO);

        refactoringRename.refactor(textEditor);

        mainCheckRenameRefactoring();
        verify(result).getSeverity();
    }

    @Test
    public void renameRefactoringShouldBeWithOK() throws OperationException {
        when(result.getSeverity()).thenReturn(OK);

        refactoringRename.refactor(textEditor);

        mainCheckRenameRefactoring();
        verify(result).getSeverity();
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
