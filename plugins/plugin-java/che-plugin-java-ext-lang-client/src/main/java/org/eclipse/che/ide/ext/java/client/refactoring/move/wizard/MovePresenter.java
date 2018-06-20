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
package org.eclipse.che.ide.ext.java.client.refactoring.move.wizard;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification.Status;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringActionDelegate;
import org.eclipse.che.ide.ext.java.client.refactoring.preview.PreviewPresenter;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.jdt.ls.extension.api.dto.CreateMoveParams;
import org.eclipse.che.jdt.ls.extension.api.dto.MoveSettings;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyWorkspaceEditAction;

/**
 * The class that manages Move panel widget.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class MovePresenter implements MoveView.ActionDelegate, RefactoringActionDelegate {
  private final MoveView view;
  private final ApplyWorkspaceEditAction applyWorkspaceEditAction;
  private final JavaLanguageExtensionServiceClient extensionService;
  private final EditorAgent editorAgent;
  private final PreviewPresenter previewPresenter;
  private final DtoFactory dtoFactory;
  private final JavaLocalizationConstant locale;
  private final NotificationManager notificationManager;

  private RefactorInfo refactorInfo;
  private CreateMoveParams moveParameters;
  private String destinationPath;

  @Inject
  public MovePresenter(
      MoveView view,
      ApplyWorkspaceEditAction applyWorkspaceEditAction,
      JavaLanguageExtensionServiceClient extensionService,
      EditorAgent editorAgent,
      PreviewPresenter previewPresenter,
      DtoFactory dtoFactory,
      JavaLocalizationConstant locale,
      NotificationManager notificationManager) {
    this.view = view;
    this.applyWorkspaceEditAction = applyWorkspaceEditAction;
    this.extensionService = extensionService;
    this.editorAgent = editorAgent;
    this.view.setDelegate(this);

    this.previewPresenter = previewPresenter;
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

    moveParameters = createMoveParameters();
    extensionService
        .validateMoveCommand(moveParameters)
        .then(
            arg -> {
              showProjectsAndPackages();
            })
        .catchError(
            error -> {
              notificationManager.notify(error.getMessage(), Status.FAIL, FLOAT_MODE);
            });
  }

  private CreateMoveParams createMoveParameters() {
    List<org.eclipse.che.jdt.ls.extension.api.dto.Resource> elements = new ArrayList<>();
    CreateMoveParams moveParams = dtoFactory.createDto(CreateMoveParams.class);
    Project project = null;

    for (Resource resource : refactorInfo.getResources()) {
      org.eclipse.che.jdt.ls.extension.api.dto.Resource element =
          dtoFactory.createDto(org.eclipse.che.jdt.ls.extension.api.dto.Resource.class);

      element.setUri(resource.getLocation().toString());
      if (resource instanceof Container) {
        element.setPack(true);
      } else {
        element.setPack(false);
      }

      elements.add(element);

      if (project == null) {
        project = resource.getProject();
      }
    }

    moveParams.setResources(elements);

    if (project != null) {
      moveParams.setProjectUri(project.getLocation().toString());
    }

    return moveParams;
  }

  private void showProjectsAndPackages() {
    extensionService
        .getDestinations()
        .then(
            projects -> {
              view.setTreeOfDestinations(refactorInfo, projects);
              view.show(refactorInfo);
            })
        .catchError(
            error -> {
              notificationManager.notify(
                  locale.showPackagesError(), error.getMessage(), Status.FAIL, FLOAT_MODE);
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onPreviewButtonClicked() {
    MoveSettings moveSettings = createMoveSettings();
    extensionService
        .move(moveSettings)
        .then(
            refactoringResult -> {
              previewPresenter.show(refactoringResult.getCheWorkspaceEdit(), this);
            })
        .catchError(
            error -> {
              notificationManager.notify(
                  locale.showPreviewError(), error.getMessage(), Status.FAIL, FLOAT_MODE);
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onAcceptButtonClicked() {
    MoveSettings moveSettings = createMoveSettings();
    extensionService
        .move(moveSettings)
        .then(
            refactoringResult -> {
              view.close();
              applyWorkspaceEditAction.applyWorkspaceEdit(refactoringResult.getCheWorkspaceEdit());
              setEditorFocus();
            })
        .catchError(
            error -> {
              notificationManager.notify(
                  locale.showPreviewError(), error.getMessage(), Status.FAIL, FLOAT_MODE);
            });
  }

  private void setEditorFocus() {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor instanceof TextEditor) {
      ((TextEditor) activeEditor).setFocus();
    }
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
    destinationPath = path;
    MoveSettings moveSettings = createMoveSettings();

    extensionService
        .verifyDestination(moveSettings)
        .then(
            status -> {
              view.setEnableAcceptButton(true);
              view.setEnablePreviewButton(true);

              switch (status.getRefactoringSeverity()) {
                case INFO:
                  view.showStatusMessage(status);
                  break;
                case WARNING:
                  view.showStatusMessage(status);
                  break;
                case ERROR:
                  showErrorMessage(status);
                  break;
                case FATAL:
                  showErrorMessage(status);
                  break;
                case OK:
                default:
                  view.clearStatusMessage();
                  break;
              }
            });
  }

  private MoveSettings createMoveSettings() {
    MoveSettings moveSettings = dtoFactory.createDto(MoveSettings.class);
    moveSettings.setDestination(destinationPath);
    moveSettings.setElements(moveParameters.getResources());
    moveSettings.setUpdateReferences(view.isUpdateReferences());
    moveSettings.setUpdateQualifiedNames(view.isUpdateQualifiedNames());
    if (moveSettings.isUpdateQualifiedNames()) {
      moveSettings.setFilePatterns(view.getFilePatterns());
    }
    return moveSettings;
  }

  private void showErrorMessage(RefactoringStatus status) {
    view.showErrorMessage(status);
    view.setEnableAcceptButton(false);
    view.setEnablePreviewButton(false);
  }

  @Override
  public void closeWizard() {
    view.close();
    onCancelButtonClicked();
  }
}
