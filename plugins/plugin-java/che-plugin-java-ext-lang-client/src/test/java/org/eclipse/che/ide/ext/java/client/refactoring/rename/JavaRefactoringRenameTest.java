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
package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.shared.dto.LinkedModeModel;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.LinkedRenameRefactoringApply;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatusEntry;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.link.HasLinkedMode;
import org.eclipse.che.ide.jseditor.client.link.LinkedMode;
import org.eclipse.che.ide.jseditor.client.link.LinkedModel;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditor;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialog;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.JAVA_ELEMENT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.ERROR;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.FATAL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.INFO;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.WARNING;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Andrinko
 */
@RunWith(GwtMockitoTestRunner.class)
public class JavaRefactoringRenameTest {

    private static final String TEXT                 = "some text for test";
    private static final String PATH                 = "to/be/or/not/to/be";
    private static final String JAVA_CLASS__NAME     = "JavaTest";
    private static final String JAVA_CLASS_FULL_NAME = JAVA_CLASS__NAME + ".java";
    private static final String NEW_JAVA_CLASS_NAME  = "NewJavaTest.java";
    private static final String SESSION_ID           = "some session id";
    private static final int    CURSOR_OFFSET        = 10;


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
    private EventBus                 eventBus;
    @Mock
    private DtoUnmarshallerFactory   unmarshallerFactory;
    @Mock
    private NotificationManager      notificationManager;

    @Mock
    private CreateRenameRefactoring           createRenameRefactoringDto;
    @Mock
    private LinkedRenameRefactoringApply      linkedRenameRefactoringApplyDto;
    @Mock(extraInterfaces = {HasLinkedMode.class, EditorWithAutoSave.class})
    private TextEditor                        textEditor;
    @Mock
    private EditorInput                       editorInput;
    @Mock
    private VirtualFile                       virtualFile;
    @Mock
    private ProjectConfigDto                  projectConfig;
    @Mock
    private HasProjectConfig                  hasProjectConfig;
    @Mock
    private Promise<RenameRefactoringSession> createRenamePromise;
    @Mock
    private RenameRefactoringSession          session;
    @Mock
    private LinkedModeModel                   linkedModel;
    @Mock
    private InputDialog                       dialog;
    @Mock
    private Promise<RefactoringResult>        applyModelPromise;
    @Mock
    private RefactoringResult                 result;
    @Mock
    private RefactoringStatusEntry            entry;
    @Mock
    private LoaderFactory                     loaderFactory;

    @Captor
    private ArgumentCaptor<Operation<RenameRefactoringSession>> renameRefCaptor;
    @Captor
    private ArgumentCaptor<LinkedMode.LinkedModeListener>       inputArgumentCaptor;
    @Captor
    private ArgumentCaptor<Operation<RefactoringResult>>        refactoringStatusCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>             refactoringErrorCaptor;

    @Mock
    private LinkedMode linkedMode;

    @Mock
    private LinkedModel editorLinkedModel;

    @Mock
    private Document document;

    @Mock
    private MessageLoader loader;

    private JavaRefactoringRename refactoringRename;

    @Before
    public void setUp() {
        when(loaderFactory.newLoader()).thenReturn(loader);

        refactoringRename = new JavaRefactoringRename(renamePresenter,
                                                      refactoringUpdater,
                                                      locale,
                                                      refactoringServiceClient,
                                                      dtoFactory,
                                                      dialogFactory,
                                                      notificationManager,
                                                      loaderFactory);

        when(dtoFactory.createDto(CreateRenameRefactoring.class)).thenReturn(createRenameRefactoringDto);
        when(dtoFactory.createDto(LinkedRenameRefactoringApply.class)).thenReturn(linkedRenameRefactoringApplyDto);
        when(textEditor.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(virtualFile);
        when(projectConfig.getPath()).thenReturn(TEXT);
        when(virtualFile.getName()).thenReturn(JAVA_CLASS_FULL_NAME);
        when(refactoringServiceClient.createRenameRefactoring(createRenameRefactoringDto)).thenReturn(createRenamePromise);
        when(createRenamePromise.then((Operation<RenameRefactoringSession>)any())).thenReturn(createRenamePromise);
        when(applyModelPromise.then((Operation<RefactoringResult>)any())).thenReturn(applyModelPromise);
        when(session.getLinkedModeModel()).thenReturn(linkedModel);
        when(session.getSessionId()).thenReturn(SESSION_ID);
        when(locale.renameDialogTitle()).thenReturn(TEXT);
        when(locale.renameDialogLabel()).thenReturn(TEXT);
        when(dialogFactory.createInputDialog(eq(TEXT), eq(TEXT), any(InputCallback.class), isNull(CancelCallback.class)))
                .thenReturn(dialog);
        when(refactoringServiceClient.applyLinkedModeRename(linkedRenameRefactoringApplyDto)).thenReturn(applyModelPromise);
        when(virtualFile.getPath()).thenReturn(PATH);
        when(virtualFile.getProject()).thenReturn(hasProjectConfig);
        when(hasProjectConfig.getProjectConfig()).thenReturn(projectConfig);
        when(textEditor.getCursorOffset()).thenReturn(CURSOR_OFFSET);
        when(textEditor.getDocument()).thenReturn(document);
        when(document.getContentRange(anyInt(), anyInt())).thenReturn(NEW_JAVA_CLASS_NAME);
        when(((HasLinkedMode)textEditor).getLinkedMode()).thenReturn(linkedMode);
        when(((HasLinkedMode)textEditor).createLinkedModel()).thenReturn(editorLinkedModel);
        when(textEditor.getDocument()).thenReturn(document);
        when(document.getFile()).thenReturn(virtualFile);

        when(result.getEntries()).thenReturn(Collections.singletonList(entry));
    }

    @Test
    public void renameRefactoringShouldBeAppliedSuccess() throws OperationException {
        when(result.getSeverity()).thenReturn(OK);
        List<ChangeInfo> changes = new ArrayList<>();
        when(result.getChanges()).thenReturn(changes);

        refactoringRename.refactor(textEditor);

        mainCheckRenameRefactoring();

        verify(refactoringUpdater).updateAfterRefactoring(Matchers.<RefactorInfo>any(), Matchers.<List<ChangeInfo>>any());
    }

    @Test
    public void renameRefactoringShouldBeAppliedSuccessAndShowWizard() throws OperationException {
        when(result.getSeverity()).thenReturn(OK);
        when(session.isMastShowWizard()).thenReturn(true);

        refactoringRename.refactor(textEditor);

        verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
        verify(createRenamePromise).then(renameRefCaptor.capture());
        renameRefCaptor.getValue().apply(session);
        verify(session).isMastShowWizard();
        verify(renamePresenter).show(session);
    }

    @Test
    public void renameRefactoringShouldBeShowErrorWindow() throws OperationException {
        PromiseError arg = Mockito.mock(PromiseError.class);
        MessageDialog dialog = Mockito.mock(MessageDialog.class);

        when(result.getSeverity()).thenReturn(OK);
        when(session.isMastShowWizard()).thenReturn(true);
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
    }

    @Test
    public void renameRefactoringShouldBeFailedByError() throws OperationException {
        when(result.getSeverity()).thenReturn(ERROR);

        refactoringRename.refactor(textEditor);

        mainCheckRenameRefactoring();
    }

    @Test
    public void renameRefactoringShouldBeWithWarning() throws OperationException {
        when(result.getSeverity()).thenReturn(WARNING);

        refactoringRename.refactor(textEditor);

        mainCheckRenameRefactoring();
    }

    @Test
    public void renameRefactoringShouldBeWithINFO() throws OperationException {
        when(result.getSeverity()).thenReturn(INFO);

        refactoringRename.refactor(textEditor);

        mainCheckRenameRefactoring();
    }

    private void mainCheckRenameRefactoring() throws OperationException {
        verify(dtoFactory).createDto(CreateRenameRefactoring.class);
        verify(textEditor).getCursorOffset();
        verify(createRenameRefactoringDto).setOffset(CURSOR_OFFSET);
        verify(createRenameRefactoringDto).setRefactorLightweight(true);
        verify(textEditor).getEditorInput();
        verify(editorInput).getFile();
        verify(createRenameRefactoringDto).setPath(JAVA_CLASS__NAME);
        verify(createRenameRefactoringDto).setProjectPath(TEXT);
        verify(projectConfig).getPath();
        verify(createRenameRefactoringDto).setProjectPath(TEXT);
        verify(createRenameRefactoringDto).setType(JAVA_ELEMENT);

        verify(refactoringServiceClient).createRenameRefactoring(createRenameRefactoringDto);
        verify(createRenamePromise).then(renameRefCaptor.capture());
        renameRefCaptor.getValue().apply(session);
        verify(session).isMastShowWizard();
        verify(session, times(2)).getLinkedModeModel();

        verify(linkedMode).addListener(inputArgumentCaptor.capture());
        inputArgumentCaptor.getValue().onLinkedModeExited(true, 0, 1);
        verify(dtoFactory).createDto(LinkedRenameRefactoringApply.class);
        linkedRenameRefactoringApplyDto.setNewName(NEW_JAVA_CLASS_NAME);
        linkedRenameRefactoringApplyDto.setSessionId(SESSION_ID);

        verify(refactoringServiceClient).applyLinkedModeRename(linkedRenameRefactoringApplyDto);

        verify(applyModelPromise).then(refactoringStatusCaptor.capture());
        refactoringStatusCaptor.getValue().apply(result);

        verify(result).getSeverity();
    }
}
