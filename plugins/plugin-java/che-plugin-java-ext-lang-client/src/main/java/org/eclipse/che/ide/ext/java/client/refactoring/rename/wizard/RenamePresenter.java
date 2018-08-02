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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.COMPILATION_UNIT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.JAVA_ELEMENT;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring.RenameType.PACKAGE;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.ERROR;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.FATAL;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.INFO;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.OK;
import static org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus.WARNING;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringUpdater;
import org.eclipse.che.ide.ext.java.client.refactoring.preview.PreviewPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenameView.ActionDelegate;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames.SimilarNamesConfigurationPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.service.RefactoringServiceClient;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeCreationResult;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ChangeInfo;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.CreateRenameRefactoring;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatus;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringStatusEntry;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameRefactoringSession;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.ValidateNewName;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;

/**
 * The class that manages Move panel widget.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class RenamePresenter implements ActionDelegate {
  private final RenameView view;
  private final SimilarNamesConfigurationPresenter similarNamesConfigurationPresenter;
  private final JavaLocalizationConstant locale;
  private final RefactoringUpdater refactoringUpdater;
  private final EditorAgent editorAgent;
  private final NotificationManager notificationManager;
  private final AppContext appContext;
  private final PreviewPresenter previewPresenter;
  private final DtoFactory dtoFactory;
  private final RefactoringServiceClient refactorService;
  private final DialogFactory dialogFactory;
  private final ClientServerEventService clientServerEventService;

  private RenameRefactoringSession renameRefactoringSession;
  private RefactorInfo refactorInfo;

  @Inject
  public RenamePresenter(
      RenameView view,
      SimilarNamesConfigurationPresenter similarNamesConfigurationPresenter,
      JavaLocalizationConstant locale,
      EditorAgent editorAgent,
      RefactoringUpdater refactoringUpdater,
      AppContext appContext,
      NotificationManager notificationManager,
      PreviewPresenter previewPresenter,
      RefactoringServiceClient refactorService,
      ClientServerEventService clientServerEventService,
      DtoFactory dtoFactory,
      DialogFactory dialogFactory) {
    this.view = view;
    this.similarNamesConfigurationPresenter = similarNamesConfigurationPresenter;
    this.locale = locale;
    this.refactoringUpdater = refactoringUpdater;
    this.editorAgent = editorAgent;
    this.notificationManager = notificationManager;
    this.clientServerEventService = clientServerEventService;
    this.view.setDelegate(this);
    this.appContext = appContext;
    this.previewPresenter = previewPresenter;
    this.refactorService = refactorService;
    this.dtoFactory = dtoFactory;
    this.dialogFactory = dialogFactory;
  }

  /**
   * Show Rename window with the special information.
   *
   * @param refactorInfo information about the rename operation
   */
  public void show(RefactorInfo refactorInfo) {
    this.refactorInfo = refactorInfo;
    final CreateRenameRefactoring createRenameRefactoring =
        createRenameRefactoringDto(refactorInfo);

    Promise<RenameRefactoringSession> createRenamePromise =
        refactorService.createRenameRefactoring(createRenameRefactoring);
    createRenamePromise
        .then(
            new Operation<RenameRefactoringSession>() {
              @Override
              public void apply(RenameRefactoringSession session) throws OperationException {
                show(session);
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(
                    locale.failedToRename(), arg.getMessage(), FAIL, FLOAT_MODE);
              }
            });
  }

  /**
   * Show Rename window with the special information.
   *
   * @param renameRefactoringSession data of current refactoring session
   */
  public void show(RenameRefactoringSession renameRefactoringSession) {
    this.renameRefactoringSession = renameRefactoringSession;
    prepareWizard();

    switch (renameRefactoringSession.getWizardType()) {
      case COMPILATION_UNIT:
        view.setTitleCaption(locale.renameCompilationUnitTitle());
        view.setVisiblePatternsPanel(true);
        view.setVisibleFullQualifiedNamePanel(true);
        view.setVisibleSimilarlyVariablesPanel(true);
        break;
      case PACKAGE:
        view.setTitleCaption(locale.renamePackageTitle());
        view.setVisiblePatternsPanel(true);
        view.setVisibleFullQualifiedNamePanel(true);
        view.setVisibleRenameSubpackagesPanel(true);
        break;
      case TYPE:
        view.setTitleCaption(locale.renameTypeTitle());
        view.setVisiblePatternsPanel(true);
        view.setVisibleFullQualifiedNamePanel(true);
        view.setVisibleSimilarlyVariablesPanel(true);
        break;
      case FIELD:
        view.setTitleCaption(locale.renameFieldTitle());
        view.setVisiblePatternsPanel(true);
        break;
      case ENUM_CONSTANT:
        view.setTitleCaption(locale.renameEnumTitle());
        view.setVisiblePatternsPanel(true);
        break;
      case TYPE_PARAMETER:
        view.setTitleCaption(locale.renameTypeVariableTitle());
        break;
      case METHOD:
        view.setTitleCaption(locale.renameMethodTitle());
        view.setVisibleKeepOriginalPanel(true);
        break;
      case LOCAL_VARIABLE:
        view.setTitleCaption(locale.renameLocalVariableTitle());
        break;
      default:
    }

    view.showDialog();
  }

  /** {@inheritDoc} */
  @Override
  public void onPreviewButtonClicked() {
    showPreview();
  }

  /** {@inheritDoc} */
  @Override
  public void onAcceptButtonClicked() {
    applyChanges();
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelButtonClicked() {
    setEditorFocus();
  }

  private void setEditorFocus() {
    EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
    if (activeEditor instanceof TextEditor) {
      ((TextEditor) activeEditor).setFocus();
    }
  }

  /** {@inheritDoc} */
  @Override
  public void validateName() {
    ValidateNewName validateNewName = dtoFactory.createDto(ValidateNewName.class);
    validateNewName.setSessionId(renameRefactoringSession.getSessionId());
    validateNewName.setNewName(view.getNewName());

    refactorService
        .validateNewName(validateNewName)
        .then(
            new Operation<RefactoringStatus>() {
              @Override
              public void apply(RefactoringStatus arg) throws OperationException {
                switch (arg.getSeverity()) {
                  case OK:
                    view.setEnableAcceptButton(true);
                    view.setEnablePreviewButton(true);
                    view.clearErrorLabel();
                    break;
                  case INFO:
                    view.setEnableAcceptButton(true);
                    view.setEnablePreviewButton(true);
                    view.showStatusMessage(arg);
                    break;
                  default:
                    view.setEnableAcceptButton(false);
                    view.setEnablePreviewButton(false);
                    view.showErrorMessage(arg);
                    break;
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(
                    locale.failedToRename(), arg.getMessage(), FAIL, FLOAT_MODE);
              }
            });
  }

  private void prepareWizard() {
    view.clearErrorLabel();
    view.setOldName(renameRefactoringSession.getOldName());
    view.setVisiblePatternsPanel(false);
    view.setVisibleFullQualifiedNamePanel(false);
    view.setVisibleKeepOriginalPanel(false);
    view.setVisibleRenameSubpackagesPanel(false);
    view.setVisibleSimilarlyVariablesPanel(false);
    view.setEnableAcceptButton(false);
    view.setEnablePreviewButton(false);
  }

  private void showPreview() {
    RefactoringSession session = dtoFactory.createDto(RefactoringSession.class);
    session.setSessionId(renameRefactoringSession.getSessionId());

    prepareRenameChanges(session)
        .then(
            new Operation<ChangeCreationResult>() {
              @Override
              public void apply(ChangeCreationResult arg) throws OperationException {
                if (arg.isCanShowPreviewPage() || arg.getStatus().getSeverity() <= 3) {
                  previewPresenter.show(renameRefactoringSession.getSessionId(), refactorInfo);
                  previewPresenter.setTitle(locale.renameItemTitle());
                  view.close();
                } else {
                  view.showErrorMessage(arg.getStatus());
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(
                    locale.failedToRename(), arg.getMessage(), FAIL, FLOAT_MODE);
              }
            });
  }

  private void applyChanges() {
    final RefactoringSession session = dtoFactory.createDto(RefactoringSession.class);
    session.setSessionId(renameRefactoringSession.getSessionId());

    prepareRenameChanges(session)
        .then(
            new Operation<ChangeCreationResult>() {
              @Override
              public void apply(ChangeCreationResult arg) throws OperationException {
                int severityCode = arg.getStatus().getSeverity();

                switch (severityCode) {
                  case WARNING:
                  case ERROR:
                    showWarningDialog(session, arg);
                    break;
                  case FATAL:
                    if (!arg.isCanShowPreviewPage()) {
                      view.showErrorMessage(arg.getStatus());
                    }
                    break;
                  default:
                    clientServerEventService
                        .sendFileTrackingSuspendEvent()
                        .then(
                            success -> {
                              applyRefactoring(session);
                            });
                }
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError arg) throws OperationException {
                notificationManager.notify(
                    locale.failedToRename(), arg.getMessage(), FAIL, FLOAT_MODE);
              }
            });
  }

  private void showWarningDialog(
      final RefactoringSession session, ChangeCreationResult changeCreationResult) {
    List<RefactoringStatusEntry> entries = changeCreationResult.getStatus().getEntries();

    ConfirmCallback confirmCallback =
        () ->
            clientServerEventService
                .sendFileTrackingSuspendEvent()
                .then(
                    success -> {
                      applyRefactoring(session);
                    });

    dialogFactory
        .createConfirmDialog(
            locale.warningOperationTitle(),
            entries.isEmpty() ? locale.warningOperationContent() : entries.get(0).getMessage(),
            locale.renameRename(),
            locale.buttonCancel(),
            confirmCallback,
            () -> {})
        .show();
  }

  private void applyRefactoring(RefactoringSession session) {
    refactorService
        .applyRefactoring(session)
        .then(
            refactoringResult -> {
              List<ChangeInfo> changes = refactoringResult.getChanges();
              if (refactoringResult.getSeverity() == OK) {
                view.close();
                updateAfterRefactoring(changes)
                    .then(
                        refactoringUpdater
                            .handleMovingFiles(changes)
                            .then(clientServerEventService.sendFileTrackingResumeEvent()));
              } else {
                view.showErrorMessage(refactoringResult);
                refactoringUpdater
                    .handleMovingFiles(changes)
                    .then(clientServerEventService.sendFileTrackingResumeEvent());
              }
            });
  }

  private Promise<Void> updateAfterRefactoring(List<ChangeInfo> changes) {
    return refactoringUpdater
        .updateAfterRefactoring(changes)
        .then(
            arg -> {
              final Resource[] resources =
                  refactorInfo != null ? refactorInfo.getResources() : null;
              Project project = null;

              final Resource resource =
                  (resources != null && resources.length == 1)
                      ? resources[0]
                      : appContext.getResource();
              if (resource != null) {
                project = resource.getProject();
              }

              if (project != null) {
                refactorService.reindexProject(project.getLocation().toString());
              }

              setEditorFocus();
            });
  }

  private Promise<ChangeCreationResult> prepareRenameChanges(final RefactoringSession session) {
    RenameSettings renameSettings = createRenameSettingsDto(session);

    return refactorService
        .setRenameSettings(renameSettings)
        .thenPromise(
            new Function<Void, Promise<ChangeCreationResult>>() {
              @Override
              public Promise<ChangeCreationResult> apply(Void arg) throws FunctionException {
                return refactorService.createChange(session);
              }
            });
  }

  private RenameSettings createRenameSettingsDto(RefactoringSession session) {
    RenameSettings renameSettings = dtoFactory.createDto(RenameSettings.class);
    renameSettings.setSessionId(session.getSessionId());
    renameSettings.setDelegateUpdating(view.isUpdateDelegateUpdating());
    if (view.isUpdateDelegateUpdating()) {
      renameSettings.setDeprecateDelegates(view.isUpdateMarkDeprecated());
    }
    renameSettings.setUpdateSubpackages(view.isUpdateSubpackages());
    renameSettings.setUpdateReferences(view.isUpdateReferences());
    renameSettings.setUpdateQualifiedNames(view.isUpdateQualifiedNames());
    if (view.isUpdateQualifiedNames()) {
      renameSettings.setFilePatterns(view.getFilePatterns());
    }
    renameSettings.setUpdateTextualMatches(view.isUpdateTextualOccurrences());
    renameSettings.setUpdateSimilarDeclarations(view.isUpdateSimilarlyVariables());
    if (view.isUpdateSimilarlyVariables()) {
      renameSettings.setMachStrategy(
          similarNamesConfigurationPresenter.getMachStrategy().getValue());
    }

    return renameSettings;
  }

  private CreateRenameRefactoring createRenameRefactoringDto(RefactorInfo refactorInfo) {
    CreateRenameRefactoring dto = dtoFactory.createDto(CreateRenameRefactoring.class);

    dto.setRefactorLightweight(false);

    if (refactorInfo == null) {
      final VirtualFile file = editorAgent.getActiveEditor().getEditorInput().getFile();

      dto.setType(JAVA_ELEMENT);
      dto.setPath(JavaUtil.resolveFQN(file));
      dto.setOffset(((TextEditor) editorAgent.getActiveEditor()).getCursorOffset());

      if (file instanceof Resource) {
        final Project project = ((Resource) file).getRelatedProject().get();

        dto.setProjectPath(project.getLocation().toString());
      }
    } else {
      final Resource[] resources = refactorInfo.getResources();

      checkState(resources != null && resources.length == 1);

      final Resource resource = resources[0];

      if (resource.getResourceType() == FILE) {
        dto.setPath(JavaUtil.resolveFQN(resource));
        dto.setType(COMPILATION_UNIT);
      } else if (resource instanceof Container) {
        dto.setPath(resource.getLocation().toString());
        dto.setType(PACKAGE);
      }

      final Project project = resource.getRelatedProject().get();

      dto.setProjectPath(project.getLocation().toString());
    }

    return dto;
  }
}
