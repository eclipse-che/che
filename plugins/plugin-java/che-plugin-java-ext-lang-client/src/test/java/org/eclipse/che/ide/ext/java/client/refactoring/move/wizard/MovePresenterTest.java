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
package org.eclipse.che.ide.ext.java.client.refactoring.move.wizard;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.ERROR;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.FATAL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.INFO;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.WARNING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Optional;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.preview.PreviewPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateMoveRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ElementToMove;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.MoveSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ReorgDestination;
import org.eclipse.che.ide.resource.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@RunWith(GwtMockitoTestRunner.class)
public class MovePresenterTest {
  public static final String DESTINATION = "destaination";
  public static final String PROJECT_PATH = "projectPAth";

  // constructor params
  @Mock private MoveView moveView;
  @Mock private RefactoringUpdater refactoringUpdater;
  @Mock private PreviewPresenter previewPresenter;
  @Mock private RefactoringServiceClient refactorService;
  @Mock private RefactoringSession session;
  @Mock private JavaNavigationService navigationService;
  @Mock private DtoFactory dtoFactory;
  @Mock private JavaLocalizationConstant locale;
  @Mock private NotificationManager notificationManager;
  @Mock private EditorAgent editorAgent;

  // local params
  @Mock private RefactorInfo refactorInfo;
  @Mock private File file;
  @Mock private Container srcFolder;
  @Mock private Project relatedProject;

  @Mock private RefactoringStatus refactoringStatus;
  @Mock private RefactoringResult refactoringResult;
  @Mock private ElementToMove javaElement;
  @Mock private TextEditor activeEditor;
  @Mock private CreateMoveRefactoring moveRefactoring;
  @Mock private ChangeCreationResult changeCreationResult;
  @Mock private MoveSettings moveSettings;
  @Mock private ReorgDestination destination;
  @Mock private Promise<String> sessionPromise;
  @Mock private Promise<Void> moveSettingsPromise;
  @Mock private Promise<ChangeCreationResult> changeCreationResultPromise;
  @Mock private Promise<List<JavaProject>> projectsPromise;
  @Mock private Promise<RefactoringStatus> refactoringStatusPromise;
  @Mock private Promise<RefactoringResult> refactoringResultPromise;
  @Mock private Promise<Void> updateAfterRefactoringPromise;
  @Mock private Promise<Boolean> fileTrackingSuspendEventPromise;
  @Mock private Promise<Void> handleMovingFilesPromise;
  @Mock private PromiseError promiseError;
  @Mock private ClientServerEventService clientServerEventService;

  @Captor private ArgumentCaptor<Operation<String>> sessionOperation;
  @Captor private ArgumentCaptor<Operation<List<JavaProject>>> projectsOperation;
  @Captor private ArgumentCaptor<Operation<ChangeCreationResult>> changeResultOperation;

  @Captor
  private ArgumentCaptor<Function<Void, Promise<ChangeCreationResult>>> changeCreationFunction;

  @Captor private ArgumentCaptor<Operation<RefactoringStatus>> refactoringStatusOperation;
  @Captor private ArgumentCaptor<Operation<RefactoringResult>> refResultOperation;
  @Captor private ArgumentCaptor<Operation<PromiseError>> promiseErrorCaptor;
  @Captor private ArgumentCaptor<Operation<Boolean>> clientServerSuspendOperation;
  @Captor private ArgumentCaptor<Operation<Void>> updateAfterRefactoringOperation;

  private MovePresenter presenter;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    when(editorAgent.getActiveEditor()).thenReturn(activeEditor);
    when(dtoFactory.createDto(ElementToMove.class)).thenReturn(javaElement);
    when(dtoFactory.createDto(CreateMoveRefactoring.class)).thenReturn(moveRefactoring);
    when(dtoFactory.createDto(ReorgDestination.class)).thenReturn(destination);
    when(refactorService.createMoveRefactoring(moveRefactoring)).thenReturn(sessionPromise);
    when(sessionPromise.then(any(Operation.class))).thenReturn(sessionPromise);
    when(refactorService.applyRefactoring(session)).thenReturn(refactoringResultPromise);
    when(refactorService.setDestination(destination)).thenReturn(refactoringStatusPromise);
    when(navigationService.getProjectsAndPackages(true)).thenReturn(projectsPromise);
    when(projectsPromise.then(any(Operation.class))).thenReturn(projectsPromise);
    when(dtoFactory.createDto(MoveSettings.class)).thenReturn(moveSettings);
    when(dtoFactory.createDto(RefactoringSession.class)).thenReturn(session);
    when(changeCreationResult.getStatus()).thenReturn(refactoringStatus);

    when(clientServerEventService.sendFileTrackingSuspendEvent())
        .thenReturn(fileTrackingSuspendEventPromise);
    List<ChangeInfo> changes = new ArrayList<>();
    when(refactoringResult.getChanges()).thenReturn(changes);
    when(refactoringUpdater.handleMovingFiles(anyList())).thenReturn(handleMovingFilesPromise);
    when(refactoringUpdater.updateAfterRefactoring(anyList()))
        .thenReturn(updateAfterRefactoringPromise);

    when(refactorService.setMoveSettings(moveSettings)).thenReturn(moveSettingsPromise);
    when(moveSettingsPromise.thenPromise(
            org.mockito.ArgumentMatchers
                .<Function<Void, Promise<ChangeCreationResult>>>anyObject()))
        .thenReturn(changeCreationResultPromise);
    when(changeCreationResultPromise.then(
            org.mockito.ArgumentMatchers.<Operation<ChangeCreationResult>>anyObject()))
        .thenReturn(changeCreationResultPromise);

    presenter =
        new MovePresenter(
            moveView,
            refactoringUpdater,
            editorAgent,
            previewPresenter,
            refactorService,
            navigationService,
            dtoFactory,
            locale,
            notificationManager,
            clientServerEventService);
  }

  @Test
  public void constructorShouldBePerformed() throws Exception {
    verify(moveView).setDelegate(presenter);
  }

  @Test
  public void moveWizardShouldBeShowed() throws Exception {
    List<JavaProject> javaProjects = new ArrayList<>();
    JavaProject javaProject = Mockito.mock(JavaProject.class);
    javaProjects.add(javaProject);

    when(refactorInfo.getResources()).thenReturn(new Resource[] {file});
    when(file.getParentWithMarker(eq(SourceFolderMarker.ID))).thenReturn(Optional.of(srcFolder));
    when(file.getLocation()).thenReturn(Path.valueOf("/project/src/a/b/C.java"));
    when(file.getName()).thenReturn("C.java");
    when(file.getExtension()).thenReturn("java");
    when(srcFolder.getLocation()).thenReturn(Path.valueOf("/project/src"));
    when(file.getResourceType()).thenReturn(Resource.FILE);

    when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/project"));

    presenter.show(refactorInfo);

    verify(sessionPromise).then(sessionOperation.capture());
    sessionOperation.getValue().apply("sessionId");

    verify(projectsPromise).then(projectsOperation.capture());
    projectsOperation.getValue().apply(javaProjects);

    verify(moveView).setTreeOfDestinations(any(), any());
    verify(moveView).show(refactorInfo);
  }

  @Test
  public void showPreviewWindowWhenPreviewButtonClicked() throws Exception {
    when(moveSettings.isUpdateQualifiedNames()).thenReturn(true);
    when(changeCreationResult.isCanShowPreviewPage()).thenReturn(true);

    presenter.onPreviewButtonClicked();

    verify(moveSettings).setSessionId(nullable(String.class));
    verify(moveSettings).setUpdateReferences(anyBoolean());
    verify(moveSettings).setUpdateQualifiedNames(anyBoolean());
    verify(moveSettings).setFilePatterns(nullable(String.class));
    verify(session).setSessionId(nullable(String.class));

    verify(moveSettingsPromise).thenPromise(changeCreationFunction.capture());
    changeCreationFunction.getValue().apply(any());
    verify(refactorService).createChange(session);

    verify(changeCreationResultPromise).then(changeResultOperation.capture());
    changeResultOperation.getValue().apply(changeCreationResult);
    verify(previewPresenter).show(nullable(String.class), any());
    verify(moveView).hide();
  }

  @Test
  public void errorMessageShouldBeShownDuringShowingPreviewDialog() throws OperationException {
    presenter.onPreviewButtonClicked();

    verify(changeCreationResultPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(promiseError);

    verify(locale).showPreviewError();
    verify(promiseError).getMessage();
    verify(notificationManager)
        .notify(nullable(String.class), nullable(String.class), eq(FAIL), eq(FLOAT_MODE));
  }

  @Test
  public void showErrorMessageIfCanNotShowPreviewWindow() throws Exception {
    when(moveSettings.isUpdateQualifiedNames()).thenReturn(true);
    when(changeCreationResult.isCanShowPreviewPage()).thenReturn(false);

    presenter.onPreviewButtonClicked();

    verify(moveSettings).setSessionId(nullable(String.class));
    verify(moveSettings).setUpdateReferences(anyBoolean());
    verify(moveSettings).setUpdateQualifiedNames(anyBoolean());
    verify(moveSettings).setFilePatterns(nullable(String.class));
    verify(session).setSessionId(nullable(String.class));

    verify(moveSettingsPromise).thenPromise(changeCreationFunction.capture());
    changeCreationFunction.getValue().apply(any());
    verify(refactorService).createChange(session);

    verify(changeCreationResultPromise).then(changeResultOperation.capture());
    changeResultOperation.getValue().apply(changeCreationResult);
    verify(previewPresenter, never()).show(nullable(String.class), any());
    verify(moveView, never()).hide();
    verify(moveView).showStatusMessage(refactoringStatus);
  }

  @Test
  public void acceptButtonActionShouldShowAnErrorMessage() throws Exception {
    when(moveSettings.isUpdateQualifiedNames()).thenReturn(true);
    when(changeCreationResult.isCanShowPreviewPage()).thenReturn(false);
    when(changeCreationResult.getStatus()).thenReturn(refactoringStatus);

    presenter.onAcceptButtonClicked();

    verify(moveSettings).setSessionId(nullable(String.class));
    verify(moveSettings).setUpdateReferences(anyBoolean());
    verify(moveSettings).setUpdateQualifiedNames(anyBoolean());
    verify(moveSettings).setFilePatterns(nullable(String.class));
    verify(session).setSessionId(nullable(String.class));

    verify(moveSettingsPromise).thenPromise(changeCreationFunction.capture());
    changeCreationFunction.getValue().apply(any());
    verify(refactorService).createChange(session);

    verify(changeCreationResultPromise).then(changeResultOperation.capture());
    changeResultOperation.getValue().apply(changeCreationResult);
    verify(moveView).showErrorMessage(refactoringStatus);
  }

  @Test
  public void notificationShouldBeShownWhenSomeErrorOccursDuringAcceptMoving()
      throws OperationException {
    presenter.onAcceptButtonClicked();

    verify(changeCreationResultPromise).catchError(promiseErrorCaptor.capture());
    promiseErrorCaptor.getValue().apply(promiseError);

    verify(locale).applyMoveError();
    verify(promiseError).getMessage();
    verify(notificationManager)
        .notify(nullable(String.class), nullable(String.class), eq(FAIL), eq(FLOAT_MODE));
  }

  @Test
  public void acceptButtonActionShouldBePerformed() throws Exception {

    when(moveSettings.isUpdateQualifiedNames()).thenReturn(true);
    when(changeCreationResult.isCanShowPreviewPage()).thenReturn(true);
    when(changeCreationResult.getStatus()).thenReturn(refactoringResult);
    when(refactoringResult.getSeverity()).thenReturn(OK);

    when(refactorInfo.getResources()).thenReturn(new Resource[] {file});
    when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/project"));
    presenter.refactorInfo = refactorInfo;

    presenter.onAcceptButtonClicked();

    verify(moveSettings).setSessionId(nullable(String.class));
    verify(moveSettings).setUpdateReferences(anyBoolean());
    verify(moveSettings).setUpdateQualifiedNames(anyBoolean());
    verify(moveSettings).setFilePatterns(nullable(String.class));
    verify(session).setSessionId(nullable(String.class));

    verify(moveSettingsPromise).thenPromise(changeCreationFunction.capture());
    changeCreationFunction.getValue().apply(any());
    verify(refactorService).createChange(session);

    verify(changeCreationResultPromise).then(changeResultOperation.capture());
    changeResultOperation.getValue().apply(changeCreationResult);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(any());

    verify(refactoringResultPromise).then(refResultOperation.capture());
    refResultOperation.getValue().apply(refactoringResult);
    verify(moveView).hide();

    verify(updateAfterRefactoringPromise).then(updateAfterRefactoringOperation.capture());
    updateAfterRefactoringOperation.getValue().apply(null);
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(clientServerEventService).sendFileTrackingResumeEvent();
    verify(moveView, never()).showErrorMessage(refactoringResult);
  }

  @Test
  public void errorLabelShouldBeShowedIfRefactoringStatusIsNotOK() throws Exception {
    when(moveSettings.isUpdateQualifiedNames()).thenReturn(true);
    when(changeCreationResult.isCanShowPreviewPage()).thenReturn(true);
    when(changeCreationResult.getStatus()).thenReturn(refactoringResult);
    when(refactoringResult.getSeverity()).thenReturn(2);

    when(refactorInfo.getResources()).thenReturn(new Resource[] {file});
    when(file.getRelatedProject()).thenReturn(Optional.of(relatedProject));
    when(relatedProject.getLocation()).thenReturn(Path.valueOf("/project"));
    presenter.refactorInfo = refactorInfo;

    presenter.onAcceptButtonClicked();

    verify(moveSettings).setSessionId(nullable(String.class));
    verify(moveSettings).setUpdateReferences(anyBoolean());
    verify(moveSettings).setUpdateQualifiedNames(anyBoolean());
    verify(moveSettings).setFilePatterns(nullable(String.class));
    verify(session).setSessionId(nullable(String.class));

    verify(moveSettingsPromise).thenPromise(changeCreationFunction.capture());
    changeCreationFunction.getValue().apply(any());
    verify(refactorService).createChange(session);

    verify(changeCreationResultPromise).then(changeResultOperation.capture());
    changeResultOperation.getValue().apply(changeCreationResult);

    verify(fileTrackingSuspendEventPromise).then(clientServerSuspendOperation.capture());
    clientServerSuspendOperation.getValue().apply(any());

    verify(refactoringResultPromise).then(refResultOperation.capture());
    refResultOperation.getValue().apply(refactoringResult);
    verify(moveView, never()).hide();
    verify(refactoringUpdater, never()).updateAfterRefactoring(anyList());

    verify(moveView).showErrorMessage(refactoringResult);
    verify(refactoringUpdater).handleMovingFiles(anyList());
    verify(clientServerEventService).sendFileTrackingResumeEvent();
  }

  @Test
  public void moveDestinationPathChangedWithOKStatus() throws Exception {
    when(refactoringStatus.getSeverity()).thenReturn(OK);

    presenter.setMoveDestinationPath(DESTINATION, PROJECT_PATH);

    verify(destination).setType(ReorgDestination.DestinationType.PACKAGE);
    verify(destination).setSessionId(nullable(String.class));
    verify(destination).setProjectPath(PROJECT_PATH);
    verify(destination).setDestination(DESTINATION);

    verify(refactoringStatusPromise).then(refactoringStatusOperation.capture());
    refactoringStatusOperation.getValue().apply(refactoringStatus);
    verify(moveView).setEnableAcceptButton(true);
    verify(moveView).setEnablePreviewButton(true);
    verify(moveView).clearStatusMessage();
  }

  @Test
  public void moveDestinationPathChangedWithINFOStatus() throws Exception {
    when(refactoringStatus.getSeverity()).thenReturn(INFO);

    presenter.setMoveDestinationPath(DESTINATION, PROJECT_PATH);

    verify(destination).setType(ReorgDestination.DestinationType.PACKAGE);
    verify(destination).setSessionId(nullable(String.class));
    verify(destination).setProjectPath(PROJECT_PATH);
    verify(destination).setDestination(DESTINATION);

    verify(refactoringStatusPromise).then(refactoringStatusOperation.capture());
    refactoringStatusOperation.getValue().apply(refactoringStatus);
    verify(moveView).setEnableAcceptButton(true);
    verify(moveView).setEnablePreviewButton(true);
    verify(moveView).showStatusMessage(refactoringStatus);
  }

  @Test
  public void moveDestinationPathChangedWithWARNINGStatus() throws Exception {
    when(refactoringStatus.getSeverity()).thenReturn(WARNING);

    presenter.setMoveDestinationPath(DESTINATION, PROJECT_PATH);

    verify(destination).setType(ReorgDestination.DestinationType.PACKAGE);
    verify(destination).setSessionId(nullable(String.class));
    verify(destination).setProjectPath(PROJECT_PATH);
    verify(destination).setDestination(DESTINATION);

    verify(refactoringStatusPromise).then(refactoringStatusOperation.capture());
    refactoringStatusOperation.getValue().apply(refactoringStatus);
    verify(moveView).setEnableAcceptButton(true);
    verify(moveView).setEnablePreviewButton(true);
    verify(moveView).showStatusMessage(refactoringStatus);
  }

  @Test
  public void moveDestinationPathChangedWithERRORStatus() throws Exception {
    when(refactoringStatus.getSeverity()).thenReturn(ERROR);

    presenter.setMoveDestinationPath(DESTINATION, PROJECT_PATH);

    verify(destination).setType(ReorgDestination.DestinationType.PACKAGE);
    verify(destination).setSessionId(nullable(String.class));
    verify(destination).setProjectPath(PROJECT_PATH);
    verify(destination).setDestination(DESTINATION);

    verify(refactoringStatusPromise).then(refactoringStatusOperation.capture());
    refactoringStatusOperation.getValue().apply(refactoringStatus);
    verify(moveView).setEnableAcceptButton(true);
    verify(moveView).setEnablePreviewButton(true);

    verify(moveView).showErrorMessage(refactoringStatus);
    verify(moveView).setEnableAcceptButton(false);
    verify(moveView).setEnablePreviewButton(false);
  }

  @Test
  public void moveDestinationPathChangedWithFATALStatus() throws Exception {
    when(refactoringStatus.getSeverity()).thenReturn(FATAL);

    presenter.setMoveDestinationPath(DESTINATION, PROJECT_PATH);

    verify(destination).setType(ReorgDestination.DestinationType.PACKAGE);
    verify(destination).setSessionId(nullable(String.class));
    verify(destination).setProjectPath(PROJECT_PATH);
    verify(destination).setDestination(DESTINATION);

    verify(refactoringStatusPromise).then(refactoringStatusOperation.capture());
    refactoringStatusOperation.getValue().apply(refactoringStatus);
    verify(moveView).setEnableAcceptButton(true);
    verify(moveView).setEnablePreviewButton(true);

    verify(moveView).showErrorMessage(refactoringStatus);
    verify(moveView).setEnableAcceptButton(false);
    verify(moveView).setEnablePreviewButton(false);
  }

  @Test
  public void focusShouldBeSetAfterClosingTheEditor() throws Exception {
    presenter.onCancelButtonClicked();

    verify(activeEditor).setFocus();
  }
}
