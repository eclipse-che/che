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
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.ERROR;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.FATAL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.INFO;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.WARNING;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification.Status;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.preview.PreviewPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateMoveRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ElementToMove;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.MoveSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ReorgDestination;

/**
 * The class that manages Move panel widget.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class MovePresenter implements MoveView.ActionDelegate {
  private final MoveView view;
  private final RefactoringUpdater refactoringUpdater;
  private final EditorAgent editorAgent;
  private final PreviewPresenter previewPresenter;
  private final DtoFactory dtoFactory;
  private final RefactoringServiceClient refactorService;
  private final JavaNavigationService navigationService;
  private final JavaLocalizationConstant locale;
  private final NotificationManager notificationManager;
  private final ClientServerEventService clientServerEventService;

  protected RefactorInfo refactorInfo;
  private String refactoringSessionId;

  @Inject
  public MovePresenter(
      MoveView view,
      RefactoringUpdater refactoringUpdater,
      EditorAgent editorAgent,
      PreviewPresenter previewPresenter,
      RefactoringServiceClient refactorService,
      JavaNavigationService navigationService,
      DtoFactory dtoFactory,
      JavaLocalizationConstant locale,
      NotificationManager notificationManager,
      ClientServerEventService clientServerEventService) {
    this.view = view;
    this.refactoringUpdater = refactoringUpdater;
    this.editorAgent = editorAgent;
    this.clientServerEventService = clientServerEventService;
    this.view.setDelegate(this);

    this.previewPresenter = previewPresenter;
    this.refactorService = refactorService;
    this.navigationService = navigationService;
    this.dtoFactory = dtoFactory;
    this.locale = locale;
    this.notificationManager = notificationManager;
  }

  /**
   * Show Move panel with the special information.
   *
   * @param refactorInfo information about the move operation
   */
  public void show(final RefactorInfo refactorInfo) {
    this.refactorInfo = refactorInfo;
    view.setEnablePreviewButton(false);
    view.setEnableAcceptButton(false);
    view.clearErrorLabel();

    CreateMoveRefactoring moveRefactoring = createMoveDto();

    Promise<String> sessionIdPromise = refactorService.createMoveRefactoring(moveRefactoring);

    sessionIdPromise
        .then(
            new Operation<String>() {
              @Override
              public void apply(String sessionId) throws OperationException {
                MovePresenter.this.refactoringSessionId = sessionId;

                showProjectsAndPackages();
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(error.getMessage(), Status.FAIL, FLOAT_MODE);
              }
            });
  }

  private CreateMoveRefactoring createMoveDto() {
    List<ElementToMove> elements = new ArrayList<>();

    Project project = null;

    for (Resource resource : refactorInfo.getResources()) {
      ElementToMove element = dtoFactory.createDto(ElementToMove.class);

      if (resource instanceof Container) {
        element.setPath(resource.getLocation().toString());
        element.setPack(true);
      } else {
        element.setPath(JavaUtil.resolveFQN(resource));
        element.setPack(false);
      }

      elements.add(element);

      if (project == null) {
        project = resource.getRelatedProject().get();
      }
    }

    CreateMoveRefactoring moveRefactoring = dtoFactory.createDto(CreateMoveRefactoring.class);

    moveRefactoring.setElements(elements);

    if (project != null) {
      moveRefactoring.setProjectPath(project.getLocation().toString());
    }

    return moveRefactoring;
  }

  private void showProjectsAndPackages() {
    Promise<List<JavaProject>> projectsPromise = navigationService.getProjectsAndPackages(true);

    projectsPromise
        .then(
            new Operation<List<JavaProject>>() {
              @Override
              public void apply(List<JavaProject> projects) throws OperationException {
                List<JavaProject> currentProject = new ArrayList<>();
                for (JavaProject project : projects) {
                  currentProject.add(project);
                }
                view.setTreeOfDestinations(refactorInfo, currentProject);
                view.show(refactorInfo);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(
                    locale.showPackagesError(), error.getMessage(), Status.FAIL, FLOAT_MODE);
              }
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onPreviewButtonClicked() {
    RefactoringSession session = dtoFactory.createDto(RefactoringSession.class);
    session.setSessionId(refactoringSessionId);
    prepareMovingChanges(session)
        .then(
            new Operation<ChangeCreationResult>() {
              @Override
              public void apply(ChangeCreationResult arg) throws OperationException {
                if (arg.isCanShowPreviewPage()) {
                  previewPresenter.show(refactoringSessionId, refactorInfo);

                  view.hide();
                } else {
                  view.showStatusMessage(arg.getStatus());
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(
                    locale.showPreviewError(), error.getMessage(), Status.FAIL, FLOAT_MODE);
              }
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onAcceptButtonClicked() {
    final RefactoringSession session = dtoFactory.createDto(RefactoringSession.class);
    session.setSessionId(refactoringSessionId);
    prepareMovingChanges(session)
        .then(
            new Operation<ChangeCreationResult>() {
              @Override
              public void apply(ChangeCreationResult arg) throws OperationException {
                if (arg.isCanShowPreviewPage()) {
                  clientServerEventService
                      .sendFileTrackingSuspendEvent()
                      .then(
                          success -> {
                            applyRefactoring(session);
                          });
                } else {
                  view.showErrorMessage(arg.getStatus());
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(
                    locale.applyMoveError(), error.getMessage(), Status.FAIL, FLOAT_MODE);
              }
            });
  }

  private void applyRefactoring(RefactoringSession session) {
    refactorService
        .applyRefactoring(session)
        .then(
            refactoringResult -> {
              List<ChangeInfo> changes = refactoringResult.getChanges();
              if (refactoringResult.getSeverity() == OK) {
                view.hide();
                refactoringUpdater
                    .updateAfterRefactoring(changes)
                    .then(
                        arg -> {
                          Project project = null;
                          Resource[] resources = refactorInfo.getResources();
                          if (resources != null && resources.length == 1) {
                            project = resources[0].getProject();
                          }

                          if (project != null) {
                            refactorService.reindexProject(project.getPath());
                          }

                          refactoringUpdater
                              .handleMovingFiles(changes)
                              .then(clientServerEventService.sendFileTrackingResumeEvent());
                        });
              } else {
                view.showErrorMessage(refactoringResult);
                refactoringUpdater
                    .handleMovingFiles(changes)
                    .then(clientServerEventService.sendFileTrackingResumeEvent());
              }
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelButtonClicked() {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor instanceof TextEditor) {
      ((TextEditor) activeEditor).setFocus();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setMoveDestinationPath(String path, String projectPath) {
    ReorgDestination destination = dtoFactory.createDto(ReorgDestination.class);
    destination.setType(ReorgDestination.DestinationType.PACKAGE);
    destination.setSessionId(refactoringSessionId);
    destination.setProjectPath(projectPath);
    destination.setDestination(path);
    Promise<RefactoringStatus> promise = refactorService.setDestination(destination);
    promise.then(
        new Operation<RefactoringStatus>() {
          @Override
          public void apply(RefactoringStatus arg) throws OperationException {
            view.setEnableAcceptButton(true);
            view.setEnablePreviewButton(true);

            switch (arg.getSeverity()) {
              case INFO:
                view.showStatusMessage(arg);
                break;
              case WARNING:
                view.showStatusMessage(arg);
                break;
              case ERROR:
                showErrorMessage(arg);
                break;
              case FATAL:
                showErrorMessage(arg);
                break;
              case OK:
              default:
                view.clearStatusMessage();
                break;
            }
          }
        });
  }

  private Promise<ChangeCreationResult> prepareMovingChanges(final RefactoringSession session) {
    MoveSettings moveSettings = dtoFactory.createDto(MoveSettings.class);
    moveSettings.setSessionId(refactoringSessionId);
    moveSettings.setUpdateReferences(view.isUpdateReferences());
    moveSettings.setUpdateQualifiedNames(view.isUpdateQualifiedNames());
    if (moveSettings.isUpdateQualifiedNames()) {
      moveSettings.setFilePatterns(view.getFilePatterns());
    }

    return refactorService
        .setMoveSettings(moveSettings)
        .thenPromise(
            new Function<Void, Promise<ChangeCreationResult>>() {
              @Override
              public Promise<ChangeCreationResult> apply(Void arg) throws FunctionException {
                return refactorService.createChange(session);
              }
            });
  }

  private void showErrorMessage(RefactoringStatus arg) {
    view.showErrorMessage(arg);
    view.setEnableAcceptButton(false);
    view.setEnablePreviewButton(false);
  }
}
