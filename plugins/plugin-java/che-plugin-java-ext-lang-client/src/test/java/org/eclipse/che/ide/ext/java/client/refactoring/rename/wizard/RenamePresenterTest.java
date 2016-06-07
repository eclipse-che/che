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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard;

import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.node.JavaFileNode;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.ide.ext.java.client.refactoring.preview.PreviewPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames.SimilarNamesConfigurationPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatusEntry;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.ConfirmDialog;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.COMPILATION_UNIT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.JAVA_ELEMENT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.PACKAGE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class RenamePresenterTest {
    private final static String PROJECT_PATH = "projectPath";
    private final static String SESSION_ID   = "sessionId";
    private final static String TEXT         = "text.text";

    //variables for constructor
    @Mock
    private RenameView                         view;
    @Mock
    private SimilarNamesConfigurationPresenter similarNamesConfigurationPresenter;
    @Mock
    private JavaLocalizationConstant           locale;
    @Mock
    private EventBus                           eventBus;
    @Mock
    private ProjectExplorerPresenter           projectExplorer;
    @Mock
    private EditorAgent                        editorAgent;
    @Mock
    private RefactoringUpdater                 refactoringUpdater;
    @Mock
    private NotificationManager                notificationManager;
    @Mock
    private AppContext                         appContext;
    @Mock
    private PreviewPresenter                   previewPresenter;
    @Mock
    private DtoFactory                         dtoFactory;
    @Mock
    private RefactoringServiceClient           refactorService;
    @Mock
    private LoaderFactory                      loaderFactory;

    @Mock
    private JavaFileNode             javaFileNode;
    @Mock
    private TextEditor               activeEditor;
    @Mock
    private PackageNode              packageNode;
    @Mock
    private RefactoringSession       refactoringSession;
    @Mock
    private RenameSettings           renameSettings;
    @Mock
    private RefactoringResult        refactoringStatus;
    @Mock
    private CreateRenameRefactoring  createRenameRefactoringDto;
    @Mock
    private CurrentProject           currentProject;
    @Mock
    private ProjectConfigDto         projectConfig;
    @Mock
    private PromiseError             promiseError;
    @Mock
    private ChangeCreationResult     changeCreationResult;
    @Mock
    private RenameRefactoringSession session;
    @Mock
    private DialogFactory            dialogFactory;

    @Mock
    private Promise<RenameRefactoringSession> renameRefactoringSessionPromise;
    @Mock
    private Promise<Void>                     renameSettingsPromise;
    @Mock
    private Promise<ChangeCreationResult>     changeCreationResultPromise;
    @Mock
    private Promise<RefactoringResult>        refactoringStatusPromise;

    @Captor
    private ArgumentCaptor<Operation<RenameRefactoringSession>>           renameRefactoringSessionCaptor;
    @Captor
    private ArgumentCaptor<Operation<PromiseError>>                       promiseErrorCaptor;
    @Captor
    private ArgumentCaptor<Function<Void, Promise<ChangeCreationResult>>> renameSettingsPromiseCaptor;
    @Captor
    private ArgumentCaptor<Operation<ChangeCreationResult>>               changeCreationResultCaptor;
    @Captor
    private ArgumentCaptor<Operation<RefactoringResult>>                  refactoringStatusCaptor;

    private RenamePresenter renamePresenter;

    private RefactorInfo refactorInfo;


    @Before
    public void setUp() throws Exception {
        when(editorAgent.getActiveEditor()).thenReturn(activeEditor);
        when(dtoFactory.createDto(CreateRenameRefactoring.class)).thenReturn(createRenameRefactoringDto);
        when(dtoFactory.createDto(RefactoringSession.class)).thenReturn(refactoringSession);
        when(dtoFactory.createDto(RenameSettings.class)).thenReturn(renameSettings);
        when(refactoringSession.getSessionId()).thenReturn(SESSION_ID);
        when(appContext.getCurrentProject()).thenReturn(currentProject);
        when(currentProject.getProjectConfig()).thenReturn(projectConfig);
        when(javaFileNode.getParent()).thenReturn(javaFileNode);
        when(javaFileNode.getName()).thenReturn(TEXT);
        when(promiseError.getMessage()).thenReturn(TEXT);
        when(session.getOldName()).thenReturn(TEXT);
        when(projectConfig.getPath()).thenReturn(PROJECT_PATH);
        when(session.getSessionId()).thenReturn(SESSION_ID);
        when(refactorService.createRenameRefactoring(createRenameRefactoringDto)).thenReturn(renameRefactoringSessionPromise);
        List<?> selectedElements = Collections.singletonList(javaFileNode);
        refactorInfo = RefactorInfo.of(MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, selectedElements);
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.LOCAL_VARIABLE);
        when(renameRefactoringSessionPromise.then(Matchers.<Operation<RenameRefactoringSession>>anyObject()))
                .thenReturn(renameRefactoringSessionPromise);
        when(view.isUpdateDelegateUpdating()).thenReturn(true);
        when(view.isUpdateQualifiedNames()).thenReturn(true);
        when(view.isUpdateSimilarlyVariables()).thenReturn(true);
        when(similarNamesConfigurationPresenter.getMachStrategy()).thenReturn(RenameSettings.MachStrategy.SUFFIX);
        when(refactorService.setRenameSettings(renameSettings)).thenReturn(renameSettingsPromise);
        when(renameSettingsPromise.thenPromise(Matchers.<Function<Void, Promise<ChangeCreationResult>>>any()))
                .thenReturn(changeCreationResultPromise);
        when(refactorService.createChange(refactoringSession)).thenReturn(changeCreationResultPromise);
        when(changeCreationResultPromise.then(Matchers.<Operation<ChangeCreationResult>>any())).thenReturn(changeCreationResultPromise);
        when(changeCreationResultPromise.catchError(Matchers.<Operation<PromiseError>>anyObject())).thenReturn(changeCreationResultPromise);
        when(refactorService.applyRefactoring(refactoringSession)).thenReturn(refactoringStatusPromise);

        when(changeCreationResult.getStatus()).thenReturn(refactoringStatus);

        renamePresenter = new RenamePresenter(view,
                                              similarNamesConfigurationPresenter,
                                              locale,
                                              editorAgent,
                                              refactoringUpdater,
                                              appContext,
                                              notificationManager,
                                              previewPresenter,
                                              refactorService,
                                              dtoFactory,
                                              dialogFactory);
    }

    @Test
    public void wizardShouldNotBeShowIfRenameRefactoringObjectWasNotCreated() throws Exception {
        List<?> selectedElements = Collections.singletonList(javaFileNode);
        RefactorInfo refactorInfo = RefactorInfo.of(MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, selectedElements);

        renamePresenter.show(refactorInfo);

        verify(createRenameRefactoringDto).setRefactorLightweight(false);
        verify(createRenameRefactoringDto).setPath(anyString());
        verify(createRenameRefactoringDto).setType(COMPILATION_UNIT);
        verify(createRenameRefactoringDto).setProjectPath(PROJECT_PATH);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(promiseError);
        verify(notificationManager).notify(anyString(), anyString(), any(StatusNotification.Status.class), any(DisplayMode.class));
    }

    @Test
    public void renameCompilationUnitWizardShouldBeShowCompilationUnit() throws Exception {
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.COMPILATION_UNIT);
        renamePresenter.show(refactorInfo);

        verifyPreparingRenameRefactoringDto();

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameCompilationUnitTitle();
        verify(view).setTitle(anyString());
        verify(view).setVisiblePatternsPanel(true);
        verify(view).setVisibleFullQualifiedNamePanel(true);
        verify(view).setVisibleSimilarlyVariablesPanel(true);

        verify(view).show();
    }

    @Test
    public void renamePackageWizardShouldBeShow() throws Exception {
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.PACKAGE);
        renamePresenter.show(refactorInfo);

        verifyPreparingRenameRefactoringDto();

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renamePackageTitle();
        verify(view).setTitle(anyString());
        verify(view).setVisiblePatternsPanel(true);
        verify(view).setVisibleFullQualifiedNamePanel(true);
        verify(view).setVisibleRenameSubpackagesPanel(true);

        verify(view).show();
    }

    @Test
    public void renameTypeWizardShouldBeShow() throws Exception {
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.TYPE);
        renamePresenter.show(refactorInfo);

        verifyPreparingRenameRefactoringDto();

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameTypeTitle();
        verify(view).setTitle(anyString());
        verify(view).setVisiblePatternsPanel(true);
        verify(view).setVisibleFullQualifiedNamePanel(true);
        verify(view).setVisibleSimilarlyVariablesPanel(true);

        verify(view).show();
    }

    @Test
    public void renameFieldWizardShouldBeShow() throws Exception {
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.FIELD);
        renamePresenter.show(refactorInfo);

        verifyPreparingRenameRefactoringDto();

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameFieldTitle();
        verify(view).setTitle(anyString());
        verify(view).setVisiblePatternsPanel(true);

        verify(view).show();
    }

    @Test
    public void renameEnumConstantWizardShouldBeShow() throws Exception {
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.ENUM_CONSTANT);
        renamePresenter.show(refactorInfo);

        verifyPreparingRenameRefactoringDto();

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameEnumTitle();
        verify(view).setTitle(anyString());
        verify(view).setVisiblePatternsPanel(true);

        verify(view).show();
    }

    @Test
    public void renameTypeParameterWizardShouldBeShow() throws Exception {
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.TYPE_PARAMETER);
        renamePresenter.show(refactorInfo);

        verifyPreparingRenameRefactoringDto();

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameTypeVariableTitle();
        verify(view).setTitle(anyString());

        verify(view).show();
    }

    @Test
    public void renameMethodWizardShouldBeShow() throws Exception {
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.METHOD);
        renamePresenter.show(refactorInfo);

        verifyPreparingRenameRefactoringDto();

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameMethodTitle();
        verify(view).setTitle(anyString());
        verify(view).setVisibleKeepOriginalPanel(true);

        verify(view).show();
    }

    @Test
    public void renameLocalVariableWizardShouldBeShow() throws Exception {
        renamePresenter.show(refactorInfo);

        verifyPreparingRenameRefactoringDto();

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameLocalVariableTitle();
        verify(view).setTitle(anyString());

        verify(view).show();
    }

    @Test
    public void renameLocalVariableWizardShouldBeShowedIfRefactoringInfoIsNull() throws Exception {
        TextEditor editorPartPresenter = mock(TextEditor.class);
        EditorInput editorInput = mock(EditorInput.class);
        VirtualFile virtualFile = mock(VirtualFile.class);
        when(editorAgent.getActiveEditor()).thenReturn(editorPartPresenter);
        when(editorPartPresenter.getEditorInput()).thenReturn(editorInput);
        when(editorPartPresenter.getCursorOffset()).thenReturn(2);
        when(editorInput.getFile()).thenReturn(virtualFile);
        when(virtualFile.getName()).thenReturn(TEXT);

        renamePresenter.show((RefactorInfo)null);

        verify(createRenameRefactoringDto).setType(JAVA_ELEMENT);
        verify(createRenameRefactoringDto).setPath(anyString());
        verify(createRenameRefactoringDto).setOffset(2);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameLocalVariableTitle();
        verify(view).setTitle(anyString());

        verify(view).show();
    }

    @Test
    public void renameProjectWizardShouldBeShowAsCompilationUnit() throws Exception {
        List<?> selectedElements = Collections.singletonList(packageNode);
        refactorInfo = RefactorInfo.of(MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, selectedElements);
        when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.COMPILATION_UNIT);
        when(packageNode.getStorablePath()).thenReturn(TEXT);

        renamePresenter.show(refactorInfo);

        verify(createRenameRefactoringDto).setType(PACKAGE);
        verify(createRenameRefactoringDto).setPath(TEXT);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        verifyPreparingWizard();

        verify(locale).renameCompilationUnitTitle();
        verify(view).setTitle(anyString());

        verify(view).show();
    }

    private void verifyPreparingWizard() {
        verify(view).clearErrorLabel();
        verify(view).setOldName(TEXT);
        verify(view).setEnableAcceptButton(false);
        verify(view).setEnablePreviewButton(false);
        verify(view).setVisiblePatternsPanel(false);
        verify(view).setVisibleFullQualifiedNamePanel(false);
        verify(view).setVisibleKeepOriginalPanel(false);
        verify(view).setVisibleRenameSubpackagesPanel(false);
        verify(view).setVisibleSimilarlyVariablesPanel(false);
    }

    private void verifyPreparingRenameRefactoringDto() {
        verify(createRenameRefactoringDto).setRefactorLightweight(false);
        verify(createRenameRefactoringDto).setPath(anyString());
        verify(createRenameRefactoringDto).setType(COMPILATION_UNIT);
        verify(createRenameRefactoringDto).setProjectPath(PROJECT_PATH);
    }

    @Test
    public void changesShouldNotBeAppliedAndShowErrorMessage() throws Exception {
        RefactoringStatus refactoringStatus = mock(RefactoringStatus.class);
        when(refactoringStatus.getSeverity()).thenReturn(4);
        when(changeCreationResult.isCanShowPreviewPage()).thenReturn(false);
        when(changeCreationResult.getStatus()).thenReturn(refactoringStatus);

        renamePresenter.show(refactorInfo);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        renamePresenter.onAcceptButtonClicked();

        verify(refactoringSession).setSessionId(SESSION_ID);

        verifyPreparingRenameSettingsDto();
        verifyPreparingRenameChanges();

        verify(changeCreationResultPromise).then(changeCreationResultCaptor.capture());
        changeCreationResultCaptor.getValue().apply(changeCreationResult);
        verify(view).showErrorMessage(any());
    }

    @Test
    public void changesShouldNotBeAppliedAndShowNotificationMessage() throws Exception {
        renamePresenter.show(refactorInfo);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        renamePresenter.onAcceptButtonClicked();

        verify(refactoringSession).setSessionId(SESSION_ID);

        verifyPreparingRenameSettingsDto();
        verifyPreparingRenameChanges();

        verify(changeCreationResultPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getMessage();
        verify(notificationManager).notify(anyString(), anyString(), any(StatusNotification.Status.class), any(DisplayMode.class));
    }

    @Test
    public void changesShouldBeAppliedWithOkStatus() throws Exception {
        List<ChangeInfo> changes = new ArrayList<>();
        when(refactoringStatus.getChanges()).thenReturn(changes);

        when(changeCreationResult.isCanShowPreviewPage()).thenReturn(true);
        when(refactoringStatus.getSeverity()).thenReturn(0);
        EditorPartPresenter openEditor = mock(EditorPartPresenter.class);
        List<EditorPartPresenter> openEditors = new ArrayList<>();
        EditorInput editorInput = mock(EditorInput.class);
        VirtualFile virtualFile = mock(VirtualFile.class);
        openEditors.add(openEditor);
        when(editorAgent.getOpenedEditors()).thenReturn(openEditors);
        when(openEditor.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(virtualFile);
        when(virtualFile.getPath()).thenReturn(TEXT);

        renamePresenter.show(refactorInfo);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        renamePresenter.onAcceptButtonClicked();

        verify(refactoringSession).setSessionId(SESSION_ID);

        verifyPreparingRenameSettingsDto();
        verifyPreparingRenameChanges();

        verify(changeCreationResultPromise).then(changeCreationResultCaptor.capture());
        changeCreationResultCaptor.getValue().apply(changeCreationResult);

        verify(refactorService).applyRefactoring(refactoringSession);
        verify(refactoringStatusPromise).then(refactoringStatusCaptor.capture());
        refactoringStatusCaptor.getValue().apply(refactoringStatus);

        verify(refactoringStatus, times(2)).getSeverity();
        verify(view).hide();
        verify(refactoringUpdater).updateAfterRefactoring(refactorInfo, changes);
    }

    @Test
    public void changesShouldBeAppliedWithNotErrorStatus() throws Exception {
        when(changeCreationResult.isCanShowPreviewPage()).thenReturn(true);
        when(refactoringStatus.getSeverity()).thenReturn(0);
        EditorPartPresenter openEditor = mock(EditorPartPresenter.class);
        List<EditorPartPresenter> openEditors = new ArrayList<>();
        EditorInput editorInput = mock(EditorInput.class);
        VirtualFile virtualFile = mock(VirtualFile.class);
        openEditors.add(openEditor);
        when(editorAgent.getOpenedEditors()).thenReturn(openEditors);
        when(openEditor.getEditorInput()).thenReturn(editorInput);
        when(editorInput.getFile()).thenReturn(virtualFile);
        when(virtualFile.getPath()).thenReturn(TEXT);

        renamePresenter.show(refactorInfo);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        renamePresenter.onAcceptButtonClicked();

        verify(refactoringSession).setSessionId(SESSION_ID);

        verifyPreparingRenameSettingsDto();
        verifyPreparingRenameChanges();

        verify(changeCreationResultPromise).then(changeCreationResultCaptor.capture());
        changeCreationResultCaptor.getValue().apply(changeCreationResult);

        verify(refactorService).applyRefactoring(refactoringSession);
        verify(refactoringStatusPromise).then(refactoringStatusCaptor.capture());
        refactoringStatusCaptor.getValue().apply(refactoringStatus);

        verify(view).hide();
        verify(refactoringUpdater).updateAfterRefactoring(eq(refactorInfo), Matchers.<List<ChangeInfo>>anyObject());
    }

    @Test
    public void previewPageIsNotReadyAndShowErrorMessage() throws Exception {
        when(changeCreationResult.isCanShowPreviewPage()).thenReturn(false);
        when(changeCreationResult.getStatus()).thenReturn(refactoringStatus);
        when(refactoringStatus.getSeverity()).thenReturn(4);

        renamePresenter.show(refactorInfo);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        renamePresenter.onPreviewButtonClicked();

        verify(refactoringSession).setSessionId(SESSION_ID);

        verifyPreparingRenameSettingsDto();
        verifyPreparingRenameChanges();

        verify(changeCreationResultPromise).then(changeCreationResultCaptor.capture());
        changeCreationResultCaptor.getValue().apply(changeCreationResult);
        verify(view).showErrorMessage(refactoringStatus);
    }

    @Test
    public void previewPageIsNotReadyAndShowNotificationMessage() throws Exception {
        renamePresenter.show(refactorInfo);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        renamePresenter.onPreviewButtonClicked();

        verify(refactoringSession).setSessionId(SESSION_ID);

        verifyPreparingRenameSettingsDto();
        verifyPreparingRenameChanges();

        verify(changeCreationResultPromise).catchError(promiseErrorCaptor.capture());
        promiseErrorCaptor.getValue().apply(promiseError);
        verify(promiseError).getMessage();
        verify(notificationManager).notify(anyString(), anyString(), any(StatusNotification.Status.class), any(DisplayMode.class));
    }

    @Test
    public void previewPageShouldBeShow() throws Exception {
        when(changeCreationResult.isCanShowPreviewPage()).thenReturn(true);
        when(refactoringStatus.getSeverity()).thenReturn(0);

        renamePresenter.show(refactorInfo);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        renamePresenter.onPreviewButtonClicked();

        verify(refactoringSession).setSessionId(SESSION_ID);

        verifyPreparingRenameSettingsDto();
        verifyPreparingRenameChanges();

        verify(changeCreationResultPromise).then(changeCreationResultCaptor.capture());
        changeCreationResultCaptor.getValue().apply(changeCreationResult);

        verify(view).hide();
        verify(previewPresenter).show(SESSION_ID, refactorInfo);
        verify(previewPresenter).setTitle(anyString());
    }

    @Test
    public void warningDialogShouldBeDisplayedWhenRefactoringPerformsWithWarning() throws OperationException {
        renamePresenter.show(session);

        ConfirmDialog dialog = mock(ConfirmDialog.class);
        RefactoringStatusEntry statusEntry = mock(RefactoringStatusEntry.class);
        List<RefactoringStatusEntry> entries = Arrays.asList(statusEntry);

        when(refactoringStatus.getEntries()).thenReturn(entries);
        when(refactoringStatus.getSeverity()).thenReturn(2);
        when(dialogFactory.createConfirmDialog(anyString(),
                                               anyString(),
                                               anyString(),
                                               anyString(),
                                               Matchers.<ConfirmCallback>anyObject(),
                                               Matchers.<CancelCallback>anyObject())).thenReturn(dialog);
        renamePresenter.onAcceptButtonClicked();

        verify(changeCreationResultPromise).then(changeCreationResultCaptor.capture());
        changeCreationResultCaptor.getValue().apply(changeCreationResult);

        verify(dialogFactory).createConfirmDialog(anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  anyString(),
                                                  Matchers.<ConfirmCallback>anyObject(),
                                                  Matchers.<CancelCallback>anyObject());
        verify(dialog).show();
    }

    @Test
    public void previewPageShouldNotBeShow() throws Exception {
        when(changeCreationResult.isCanShowPreviewPage()).thenReturn(false);
        when(refactoringStatus.getSeverity()).thenReturn(4);

        renamePresenter.show(refactorInfo);

        verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
        verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
        renameRefactoringSessionCaptor.getValue().apply(session);

        renamePresenter.onPreviewButtonClicked();

        verify(refactoringSession).setSessionId(SESSION_ID);

        verifyPreparingRenameSettingsDto();
        verifyPreparingRenameChanges();

        verify(changeCreationResultPromise).then(changeCreationResultCaptor.capture());
        changeCreationResultCaptor.getValue().apply(changeCreationResult);

        verify(view).showErrorMessage(any());
    }

    @Test
    public void focusShouldBeSetAfterClosingTheEditor() throws Exception {
        renamePresenter.onCancelButtonClicked();

        verify(activeEditor).setFocus();
    }

    private void verifyPreparingRenameSettingsDto() {
        verify(renameSettings).setDelegateUpdating(true);
        verify(view, times(2)).isUpdateDelegateUpdating();
        verify(renameSettings).setUpdateSubpackages(anyBoolean());
        verify(view).isUpdateSubpackages();
        verify(renameSettings).setUpdateReferences(anyBoolean());
        verify(view).isUpdateReferences();
        verify(renameSettings).setUpdateQualifiedNames(true);
        verify(view, times(2)).isUpdateQualifiedNames();
        verify(renameSettings).setFilePatterns(anyString());
        verify(renameSettings).setUpdateTextualMatches(anyBoolean());
        verify(view).isUpdateTextualOccurrences();
        verify(renameSettings).setUpdateSimilarDeclarations(true);
        verify(view, times(2)).isUpdateSimilarlyVariables();
        verify(renameSettings).setMachStrategy(3);
        verify(similarNamesConfigurationPresenter).getMachStrategy();
    }

    private void verifyPreparingRenameChanges() throws FunctionException {
        verify(refactorService).setRenameSettings(renameSettings);
        verify(renameSettingsPromise).thenPromise(renameSettingsPromiseCaptor.capture());
        renameSettingsPromiseCaptor.getValue().apply(any());
        verify(refactorService).createChange(refactoringSession);
    }
}
