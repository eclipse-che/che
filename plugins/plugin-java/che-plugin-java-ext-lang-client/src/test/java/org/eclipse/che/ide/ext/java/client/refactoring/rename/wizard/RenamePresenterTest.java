/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard;

import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.COMPILATION_UNIT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.JAVA_ELEMENT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.PACKAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.ide.ext.java.client.refactoring.preview.PreviewPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames.SimilarNamesConfigurationPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatusEntry;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

@RunWith(GwtMockitoTestRunner.class)
public class RenamePresenterTest {
  private static final String SESSION_ID = "sessionId";
  private static final String TEXT = "text.text";

  // variables for constructor
  @Mock private RenameView view;
  @Mock private SimilarNamesConfigurationPresenter similarNamesConfigurationPresenter;
  @Mock private JavaLocalizationConstant locale;
  @Mock private EditorAgent editorAgent;
  @Mock private RefactoringUpdater refactoringUpdater;
  @Mock private NotificationManager notificationManager;
  @Mock private AppContext appContext;
  @Mock private PreviewPresenter previewPresenter;
  @Mock private DtoFactory dtoFactory;
  @Mock private RefactoringServiceClient refactorService;
  @Mock private LoaderFactory loaderFactory;
  @Mock private ClientServerEventService clientServerEventService;

  @Mock private TextEditor activeEditor;
  @Mock private EditorInput editorInput;
  @Mock private File file;
  @Mock private Container container;
  @Mock private Container srcFolder;
  @Mock private Project project;
  @Mock private RefactoringSession refactoringSession;
  @Mock private RenameSettings renameSettings;
  @Mock private RefactoringResult refactoringStatus;
  @Mock private CreateRenameRefactoring createRenameRefactoringDto;
  @Mock private PromiseError promiseError;
  @Mock private ChangeCreationResult changeCreationResult;
  @Mock private RenameRefactoringSession session;
  @Mock private DialogFactory dialogFactory;

  @Mock private Promise<RenameRefactoringSession> renameRefactoringSessionPromise;
  @Mock private Promise<Void> renameSettingsPromise;
  @Mock private Promise<ChangeCreationResult> changeCreationResultPromise;
  @Mock private Promise<RefactoringResult> refactoringStatusPromise;
  @Mock private Promise<Void> updateAfterRefactoringPromise;
  @Mock private Promise<Boolean> fileTrackingSuspendEventPromise;
  @Mock private Promise<Void> handleMovingFilesPromise;

  @Captor
  private ArgumentCaptor<Operation<RenameRefactoringSession>> renameRefactoringSessionCaptor;

  @Captor private ArgumentCaptor<Operation<PromiseError>> promiseErrorCaptor;

  @Captor
  private ArgumentCaptor<Function<Void, Promise<ChangeCreationResult>>> renameSettingsPromiseCaptor;

  @Captor private ArgumentCaptor<Operation<ChangeCreationResult>> changeCreationResultCaptor;
  @Captor private ArgumentCaptor<Operation<RefactoringResult>> refactoringStatusCaptor;
  @Captor private ArgumentCaptor<Operation<Boolean>> clientServerSuspendOperation;
  @Captor private ArgumentCaptor<Operation<Void>> updateAfterRefactoringOperation;

  private RenamePresenter renamePresenter;

  private RefactorInfo refactorInfo;

  @Before
  public void setUp() throws Exception {
    when(editorAgent.getActiveEditor()).thenReturn(activeEditor);
    when(activeEditor.getEditorInput()).thenReturn(editorInput);
    when(editorInput.getFile()).thenReturn(file);
    when(file.getRelatedProject()).thenReturn(Optional.of(project));
    when(file.getParentWithMarker(eq(SourceFolderMarker.ID))).thenReturn(Optional.of(srcFolder));
    when(file.getName()).thenReturn("A.java");
    when(file.getExtension()).thenReturn("java");
    when(file.getLocation()).thenReturn(Path.valueOf("/project/src/a/b/c/A.java"));
    when(file.getResourceType()).thenReturn(Resource.FILE);
    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/project/src"));
    when(project.getLocation()).thenReturn(Path.valueOf("/project"));
    when(dtoFactory.createDto(CreateRenameRefactoring.class))
        .thenReturn(createRenameRefactoringDto);
    when(container.getLocation()).thenReturn(Path.valueOf("/project/src/a/b/c"));
    when(container.getRelatedProject()).thenReturn(Optional.of(project));
    when(dtoFactory.createDto(RefactoringSession.class)).thenReturn(refactoringSession);
    when(dtoFactory.createDto(RenameSettings.class)).thenReturn(renameSettings);
    when(refactoringSession.getSessionId()).thenReturn(SESSION_ID);
    when(session.getOldName()).thenReturn(TEXT);
    when(session.getSessionId()).thenReturn(SESSION_ID);
    when(refactorService.createRenameRefactoring(createRenameRefactoringDto))
        .thenReturn(renameRefactoringSessionPromise);
    when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.LOCAL_VARIABLE);
    when(renameRefactoringSessionPromise.then(
            org.mockito.ArgumentMatchers.<Operation<RenameRefactoringSession>>anyObject()))
        .thenReturn(renameRefactoringSessionPromise);
    when(view.isUpdateDelegateUpdating()).thenReturn(true);
    when(view.isUpdateQualifiedNames()).thenReturn(true);
    when(view.isUpdateSimilarlyVariables()).thenReturn(true);
    when(similarNamesConfigurationPresenter.getMachStrategy())
        .thenReturn(RenameSettings.MachStrategy.SUFFIX);
    when(refactorService.setRenameSettings(renameSettings)).thenReturn(renameSettingsPromise);
    when(renameSettingsPromise.thenPromise(
            org.mockito.ArgumentMatchers.<Function<Void, Promise<ChangeCreationResult>>>any()))
        .thenReturn(changeCreationResultPromise);
    when(changeCreationResultPromise.then(
            org.mockito.ArgumentMatchers.<Operation<ChangeCreationResult>>any()))
        .thenReturn(changeCreationResultPromise);
    when(changeCreationResultPromise.catchError(
            org.mockito.ArgumentMatchers.<Operation<PromiseError>>anyObject()))
        .thenReturn(changeCreationResultPromise);
    when(refactorService.applyRefactoring(refactoringSession)).thenReturn(refactoringStatusPromise);

    when(changeCreationResult.getStatus()).thenReturn(refactoringStatus);

    when(clientServerEventService.sendFileTrackingSuspendEvent())
        .thenReturn(fileTrackingSuspendEventPromise);
    when(refactoringUpdater.handleMovingFiles(anyList())).thenReturn(handleMovingFilesPromise);
    when(refactoringUpdater.updateAfterRefactoring(anyList()))
        .thenReturn(updateAfterRefactoringPromise);
    when(updateAfterRefactoringPromise.then(
            org.mockito.ArgumentMatchers.<Operation<Void>>anyObject()))
        .thenReturn(updateAfterRefactoringPromise);

    renamePresenter =
        new RenamePresenter(
            view,
            similarNamesConfigurationPresenter,
            locale,
            editorAgent,
            refactoringUpdater,
            appContext,
            notificationManager,
            previewPresenter,
            refactorService,
            clientServerEventService,
            dtoFactory,
            dialogFactory);
  }

  @Test
  public void wizardShouldNotBeShowIfRenameRefactoringObjectWasNotCreated() throws Exception {
    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verify(createRenameRefactoringDto).setRefactorLightweight(false);
    verify(createRenameRefactoringDto).setPath(nullable(String.class));
    verify(createRenameRefactoringDto).setType(COMPILATION_UNIT);
    verify(createRenameRefactoringDto).setProjectPath(eq("/project"));

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(promiseError);
    verify(notificationManager)
        .notify(
            nullable(String.class),
            nullable(String.class),
            any(StatusNotification.Status.class),
            any(DisplayMode.class));
  }

  @Test
  public void renameCompilationUnitWizardShouldBeShowCompilationUnit() throws Exception {
    when(session.getWizardType())
        .thenReturn(RenameRefactoringSession.RenameWizard.COMPILATION_UNIT);

    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verifyPreparingRenameRefactoringDto();

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameCompilationUnitTitle();
    verify(view).setTitle(nullable(String.class));
    verify(view).setVisiblePatternsPanel(true);
    verify(view).setVisibleFullQualifiedNamePanel(true);
    verify(view).setVisibleSimilarlyVariablesPanel(true);

    verify(view).show();
  }

  @Test
  public void renamePackageWizardShouldBeShow() throws Exception {
    when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.PACKAGE);

    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verifyPreparingRenameRefactoringDto();

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renamePackageTitle();
    verify(view).setTitle(nullable(String.class));
    verify(view).setVisiblePatternsPanel(true);
    verify(view).setVisibleFullQualifiedNamePanel(true);
    verify(view).setVisibleRenameSubpackagesPanel(true);

    verify(view).show();
  }

  @Test
  public void renameTypeWizardShouldBeShow() throws Exception {
    when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.TYPE);

    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verifyPreparingRenameRefactoringDto();

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameTypeTitle();
    verify(view).setTitle(nullable(String.class));
    verify(view).setVisiblePatternsPanel(true);
    verify(view).setVisibleFullQualifiedNamePanel(true);
    verify(view).setVisibleSimilarlyVariablesPanel(true);

    verify(view).show();
  }

  @Test
  public void renameFieldWizardShouldBeShow() throws Exception {
    when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.FIELD);

    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verifyPreparingRenameRefactoringDto();

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameFieldTitle();
    verify(view).setTitle(nullable(String.class));
    verify(view).setVisiblePatternsPanel(true);

    verify(view).show();
  }

  @Test
  public void renameEnumConstantWizardShouldBeShow() throws Exception {
    when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.ENUM_CONSTANT);

    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verifyPreparingRenameRefactoringDto();

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameEnumTitle();
    verify(view).setTitle(nullable(String.class));
    verify(view).setVisiblePatternsPanel(true);

    verify(view).show();
  }

  @Test
  public void renameTypeParameterWizardShouldBeShow() throws Exception {
    when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.TYPE_PARAMETER);

    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verifyPreparingRenameRefactoringDto();

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameTypeVariableTitle();
    verify(view).setTitle(nullable(String.class));

    verify(view).show();
  }

  @Test
  public void renameMethodWizardShouldBeShow() throws Exception {
    when(session.getWizardType()).thenReturn(RenameRefactoringSession.RenameWizard.METHOD);

    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verifyPreparingRenameRefactoringDto();

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameMethodTitle();
    verify(view).setTitle(nullable(String.class));
    verify(view).setVisibleKeepOriginalPanel(true);

    verify(view).show();
  }

  @Test
  public void renameLocalVariableWizardShouldBeShow() throws Exception {

    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    renamePresenter.show(refactorInfo);

    verifyPreparingRenameRefactoringDto();

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameLocalVariableTitle();
    verify(view).setTitle(nullable(String.class));

    verify(view).show();
  }

  @Test
  public void renameLocalVariableWizardShouldBeShowedIfRefactoringInfoIsNull() throws Exception {
    when(activeEditor.getCursorOffset()).thenReturn(2);

    renamePresenter.show((RefactorInfo) null);

    verify(createRenameRefactoringDto).setType(JAVA_ELEMENT);
    verify(createRenameRefactoringDto).setPath(nullable(String.class));
    verify(createRenameRefactoringDto).setOffset(2);

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameLocalVariableTitle();
    verify(view).setTitle(nullable(String.class));

    verify(view).show();
  }

  @Test
  public void renameProjectWizardShouldBeShowAsCompilationUnit() throws Exception {
    refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU,
            RefactoredItemType.COMPILATION_UNIT,
            new Resource[] {container});
    when(session.getWizardType())
        .thenReturn(RenameRefactoringSession.RenameWizard.COMPILATION_UNIT);

    renamePresenter.show(refactorInfo);

    verify(createRenameRefactoringDto).setType(PACKAGE);
    verify(createRenameRefactoringDto).setPath("/project/src/a/b/c");

    verify(refactorService).createRenameRefactoring(createRenameRefactoringDto);
    verify(renameRefactoringSessionPromise).then(renameRefactoringSessionCaptor.capture());
    renameRefactoringSessionCaptor.getValue().apply(session);

    verifyPreparingWizard();

    verify(locale).renameCompilationUnitTitle();
    verify(view).setTitle(nullable(String.class));

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
    verify(createRenameRefactoringDto).setPath(nullable(String.class));
    verify(createRenameRefactoringDto).setType(COMPILATION_UNIT);
    verify(createRenameRefactoringDto).setProjectPath(eq("/project"));
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
    verify(notificationManager)
        .notify(
            nullable(String.class),
            nullable(String.class),
            any(StatusNotification.Status.class),
            any(DisplayMode.class));
  }

  @Test
  public void changesShouldBeAppliedWithOkStatus() throws Exception {
    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    List<ChangeInfo> changes = new ArrayList<>();
    when(refactoringStatus.getChanges()).thenReturn(changes);

    when(changeCreationResult.isCanShowPreviewPage()).thenReturn(true);
    when(refactoringStatus.getSeverity()).thenReturn(0);

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

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    verify(refactorService).applyRefactoring(refactoringSession);
    verify(refactoringStatusPromise).then(refactoringStatusCaptor.capture());
    refactoringStatusCaptor.getValue().apply(refactoringStatus);

    verify(refactoringStatus, times(2)).getSeverity();
    verify(view).hide();

    verify(updateAfterRefactoringPromise).then(updateAfterRefactoringOperation.capture());
    updateAfterRefactoringOperation.getValue().apply(null);
    verify(refactoringUpdater)
        .updateAfterRefactoring(org.mockito.ArgumentMatchers.<List<ChangeInfo>>anyObject());
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  @Test
  public void changesShouldBeAppliedWithNotErrorStatus() throws Exception {
    RefactorInfo refactorInfo =
        RefactorInfo.of(
            MoveType.REFACTOR_MENU, RefactoredItemType.COMPILATION_UNIT, new Resource[] {file});

    when(changeCreationResult.isCanShowPreviewPage()).thenReturn(true);
    when(refactoringStatus.getSeverity()).thenReturn(0);

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

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(null);

    verify(refactorService).applyRefactoring(refactoringSession);
    verify(refactoringStatusPromise).then(refactoringStatusCaptor.capture());
    refactoringStatusCaptor.getValue().apply(refactoringStatus);

    verify(view).hide();
    verify(updateAfterRefactoringPromise).then(updateAfterRefactoringOperation.capture());
    updateAfterRefactoringOperation.getValue().apply(null);
    verify(refactoringUpdater)
        .updateAfterRefactoring(org.mockito.ArgumentMatchers.<List<ChangeInfo>>anyObject());
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(clientServerEventService).sendFileTrackingResumeEvent();
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
    verify(notificationManager)
        .notify(
            nullable(String.class),
            nullable(String.class),
            any(StatusNotification.Status.class),
            any(DisplayMode.class));
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
    verify(previewPresenter).setTitle(nullable(String.class));
  }

  @Test
  public void warningDialogShouldBeDisplayedWhenRefactoringPerformsWithWarning()
      throws OperationException {
    renamePresenter.show(session);

    ConfirmDialog dialog = mock(ConfirmDialog.class);
    RefactoringStatusEntry statusEntry = mock(RefactoringStatusEntry.class);
    List<RefactoringStatusEntry> entries = Collections.singletonList(statusEntry);

    when(refactoringStatus.getEntries()).thenReturn(entries);
    when(refactoringStatus.getSeverity()).thenReturn(2);
    when(dialogFactory.createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            org.mockito.ArgumentMatchers.<ConfirmCallback>anyObject(),
            org.mockito.ArgumentMatchers.<CancelCallback>anyObject()))
        .thenReturn(dialog);
    renamePresenter.onAcceptButtonClicked();

    verify(changeCreationResultPromise).then(changeCreationResultCaptor.capture());
    changeCreationResultCaptor.getValue().apply(changeCreationResult);

    verify(dialogFactory)
        .createConfirmDialog(
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            nullable(String.class),
            org.mockito.ArgumentMatchers.<ConfirmCallback>anyObject(),
            org.mockito.ArgumentMatchers.<CancelCallback>anyObject());
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
    verify(renameSettings).setFilePatterns(nullable(String.class));
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
