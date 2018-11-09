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

import static org.eclipse.che.api.promises.client.js.JsPromiseError.create;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.Operation.Status.CANCELLED;
import static org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.Operation.Status.FAIL;
import static org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.Operation.Status.INITIAL;
import static org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.Operation.Status.IN_PROGRESS;
import static org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.Operation.Status.SUCCESS;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactoringActionDelegate;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.ide.ext.java.client.refactoring.preview.PreviewPresenter;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenameView.ActionDelegate;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames.SimilarNamesConfigurationPresenter;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.jdt.ls.extension.api.RefactoringSeverity;
import org.eclipse.che.jdt.ls.extension.api.RenameKind;
import org.eclipse.che.jdt.ls.extension.api.dto.CheWorkspaceEdit;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringResult;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatusEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.RenameSelectionParams;
import org.eclipse.che.jdt.ls.extension.api.dto.RenameSettings;
import org.eclipse.che.jdt.ls.extension.api.dto.RenamingElementInfo;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyWorkspaceEditAction;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;

/**
 * The class that manages Rename panel widget.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class RenamePresenter implements ActionDelegate, RefactoringActionDelegate {
  private final RenameView view;
  private final JavaLanguageExtensionServiceClient extensionServiceClient;
  private final SimilarNamesConfigurationPresenter similarNamesConfigurationPresenter;
  private final ApplyWorkspaceEditAction applyWorkspaceEditAction;
  private final JavaLocalizationConstant locale;
  private final DtoBuildHelper dtoBuildHelper;
  private final DialogFactory dialogFactory;
  private final EditorAgent editorAgent;
  private final NotificationManager notificationManager;
  private final PreviewPresenter previewPresenter;
  private final DtoFactory dtoFactory;

  private RefactorInfo refactorInfo;

  private Operation currentOperation;

  @Inject
  public RenamePresenter(
      RenameView view,
      JavaLanguageExtensionServiceClient extensionServiceClient,
      SimilarNamesConfigurationPresenter similarNamesConfigurationPresenter,
      ApplyWorkspaceEditAction applyWorkspaceEditAction,
      JavaLocalizationConstant locale,
      DtoBuildHelper dtoBuildHelper,
      DialogFactory dialogFactory,
      EditorAgent editorAgent,
      NotificationManager notificationManager,
      PreviewPresenter previewPresenter,
      DtoFactory dtoFactory) {
    this.view = view;
    this.extensionServiceClient = extensionServiceClient;
    this.similarNamesConfigurationPresenter = similarNamesConfigurationPresenter;
    this.applyWorkspaceEditAction = applyWorkspaceEditAction;
    this.locale = locale;
    this.dtoBuildHelper = dtoBuildHelper;
    this.dialogFactory = dialogFactory;
    this.editorAgent = editorAgent;
    this.notificationManager = notificationManager;
    this.view.setDelegate(this);
    this.previewPresenter = previewPresenter;
    this.dtoFactory = dtoFactory;
  }

  /**
   * Show Rename window with the special information.
   *
   * @param refactorInfo information about the rename operation
   */
  public void show(RefactorInfo refactorInfo) {
    this.refactorInfo = refactorInfo;
    TextEditor editor = (TextEditor) editorAgent.getActiveEditor();

    RenameSelectionParams params = dtoFactory.createDto(RenameSelectionParams.class);

    if (RefactoredItemType.JAVA_ELEMENT.equals(refactorInfo.getRefactoredItemType())) {
      TextPosition cursorPosition = editor.getCursorPosition();
      org.eclipse.lsp4j.Position position = dtoFactory.createDto(org.eclipse.lsp4j.Position.class);
      position.setCharacter(cursorPosition.getCharacter());
      position.setLine(cursorPosition.getLine());
      params.setPosition(position);
      String location =
          editorAgent.getActiveEditor().getEditorInput().getFile().getLocation().toString();
      params.setResourceUri(location);
      params.setRenameKind(RenameKind.JAVA_ELEMENT);
    } else {
      // get selected resource
      Resource resource = refactorInfo.getResources()[0];
      params.setResourceUri(resource.getLocation().toString());
      if (RefactoredItemType.COMPILATION_UNIT.equals(refactorInfo.getRefactoredItemType())) {
        params.setRenameKind(RenameKind.COMPILATION_UNIT);
      } else {
        params.setRenameKind(RenameKind.PACKAGE);
      }
    }

    extensionServiceClient
        .getRenameType(params)
        .then(this::showWizard)
        .catchError(
            error -> {
              notificationManager.notify(
                  locale.failedToRename(),
                  error.getMessage(),
                  StatusNotification.Status.FAIL,
                  FLOAT_MODE);
            });
  }

  private void showWizard(RenamingElementInfo elementInfo) {
    prepareWizard(elementInfo.getElementName());

    switch (elementInfo.getRenameKind()) {
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

  private void prepareWizard(String oldName) {
    currentOperation = new EmptyOperation();
    view.setLoaderVisibility(false);
    view.clearErrorLabel();
    view.setOldName(oldName);
    view.setVisiblePatternsPanel(false);
    view.setVisibleFullQualifiedNamePanel(false);
    view.setVisibleKeepOriginalPanel(false);
    view.setVisibleRenameSubpackagesPanel(false);
    view.setVisibleSimilarlyVariablesPanel(false);
    view.setEnableAcceptButton(false);
    view.setEnablePreviewButton(false);
  }

  /** {@inheritDoc} */
  @Override
  public void onPreviewButtonClicked() {
    showPreview();
  }

  /** {@inheritDoc} */
  @Override
  public void onAcceptButtonClicked() {
    Operation<RefactoringResult> getChangesOperation = new GetRefactoringChangesOperation();
    currentOperation = getChangesOperation;

    getChangesOperation
        .perform(
            () ->
                notificationManager.notify(
                    locale.renameIsCancelledTitle(),
                    locale.renameIsCancelledMessage(),
                    StatusNotification.Status.SUCCESS,
                    FLOAT_MODE))
        .then(
            refactoringResult -> {
              RefactoringSeverity severity =
                  refactoringResult.getRefactoringStatus().getRefactoringSeverity();
              switch (severity) {
                case WARNING:
                case ERROR:
                  showWarningDialog(refactoringResult);
                  break;
                case FATAL:
                  view.showErrorMessage(refactoringResult.getRefactoringStatus());
                  break;
                default:
                  applyRefactoring(refactoringResult.getCheWorkspaceEdit());
              }
            });
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelButtonClicked() {
    currentOperation.cancel();

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
    RenameSelectionParams params = dtoFactory.createDto(RenameSelectionParams.class);

    if (RefactoredItemType.JAVA_ELEMENT.equals(refactorInfo.getRefactoredItemType())) {
      TextEditor editor = (TextEditor) editorAgent.getActiveEditor();
      TextPosition cursorPosition = editor.getCursorPosition();
      org.eclipse.lsp4j.Position position = dtoFactory.createDto(org.eclipse.lsp4j.Position.class);
      position.setCharacter(cursorPosition.getCharacter());
      position.setLine(cursorPosition.getLine());
      params.setPosition(position);
      params.setRenameKind(RenameKind.JAVA_ELEMENT);
      String location =
          editorAgent.getActiveEditor().getEditorInput().getFile().getLocation().toString();
      params.setResourceUri(location);
    } else if (RefactoredItemType.COMPILATION_UNIT.equals(refactorInfo.getRefactoredItemType())) {
      params.setRenameKind(RenameKind.COMPILATION_UNIT);
      params.setResourceUri(refactorInfo.getResources()[0].getLocation().toString());
    } else if (RefactoredItemType.PACKAGE.equals(refactorInfo.getRefactoredItemType())) {
      params.setRenameKind(RenameKind.PACKAGE);
      params.setResourceUri(refactorInfo.getResources()[0].getLocation().toString());
    }

    params.setNewName(view.getNewName());

    extensionServiceClient
        .validateRenamedName(params)
        .then(
            status -> {
              if (status == null) {
                return;
              }
              switch (status.getRefactoringSeverity()) {
                case OK:
                  view.setEnableAcceptButton(true);
                  view.setEnablePreviewButton(true);
                  view.clearErrorLabel();
                  break;
                case INFO:
                  view.setEnableAcceptButton(true);
                  view.setEnablePreviewButton(true);
                  view.showStatusMessage(status);
                  break;
                default:
                  view.setEnableAcceptButton(false);
                  view.setEnablePreviewButton(false);
                  view.showErrorMessage(status);
                  break;
              }
            })
        .catchError(
            error -> {
              notificationManager.notify(
                  locale.failedToRename(),
                  error.getMessage(),
                  StatusNotification.Status.FAIL,
                  FLOAT_MODE);
            });
  }

  private void showPreview() {
    GetRefactoringChangesOperation getRefactoringChangesOperation =
        new GetRefactoringChangesOperation();
    currentOperation = getRefactoringChangesOperation;

    getRefactoringChangesOperation
        .perform()
        .then(
            refactoringResult -> {
              CheWorkspaceEdit edit = refactoringResult.getCheWorkspaceEdit();
              if (edit == null) {
                return;
              }
              previewPresenter.show(edit, this);
              previewPresenter.setTitle(locale.renameItemTitle());
            });
  }

  private void applyRefactoring(CheWorkspaceEdit workspaceEdit) {
    view.close();
    applyWorkspaceEditAction.applyWorkspaceEdit(workspaceEdit);
    setEditorFocus();
  }

  private void showWarningDialog(RefactoringResult refactoringResult) {
    List<RefactoringStatusEntry> entries =
        refactoringResult.getRefactoringStatus().getRefactoringStatusEntries();

    ConfirmCallback confirmCallback =
        () -> applyRefactoring(refactoringResult.getCheWorkspaceEdit());

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

  @Override
  public void closeWizard() {
    view.close();
    setEditorFocus();
  }

  private class GetRefactoringChangesOperation implements Operation<RefactoringResult> {

    private Status status = INITIAL;
    private CancelOperationHandler cancelOperationHandler;

    public Promise<RefactoringResult> perform(CancelOperationHandler cancelOperationHandler) {
      this.cancelOperationHandler = cancelOperationHandler;

      view.setLoaderVisibility(true);

      RenameSettings renameSettings = createRenameSettings();
      RenameParams renameParams = createRenameParams(renameSettings);
      renameSettings.setRenameParams(renameParams);

      status = IN_PROGRESS;

      return Promises.create(
          (resolve, reject) ->
              extensionServiceClient
                  .rename(renameSettings)
                  .then(
                      result -> {
                        if (status != CANCELLED) {
                          onComplete(SUCCESS);

                          resolve.apply(result);
                        }
                      })
                  .catchError(
                      arg -> {
                        if (status != CANCELLED) {
                          onComplete(FAIL);

                          notificationManager.notify(
                              locale.failedToRename(),
                              arg.getMessage(),
                              StatusNotification.Status.FAIL,
                              FLOAT_MODE);
                        }
                      }));
    }

    public Promise<Void> cancel() {
      return Promises.create(
          (resolve, reject) -> {
            if (status != SUCCESS && status != FAIL) {
              onComplete(CANCELLED);

              cancelOperationHandler.onCancelled();

              resolve.apply(null);
              return;
            }

            reject.apply(create("Can not cancel operation, current status is " + status));
          });
    }

    @Override
    public Status getStatus() {
      return status;
    }

    private void onComplete(Status status) {
      this.status = status;

      currentOperation = new EmptyOperation();

      view.setLoaderVisibility(false);
    }

    private RenameSettings createRenameSettings() {
      RenameSettings renameSettings = dtoFactory.createDto(RenameSettings.class);
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
        renameSettings.setMatchStrategy(similarNamesConfigurationPresenter.getMatchStrategy());
      }

      return renameSettings;
    }

    private RenameParams createRenameParams(RenameSettings renameSettings) {
      RenameParams renameParams = dtoFactory.createDto(RenameParams.class);
      renameParams.setNewName(view.getNewName());

      if (RefactoredItemType.JAVA_ELEMENT.equals(refactorInfo.getRefactoredItemType())) {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        VirtualFile file = activeEditor.getEditorInput().getFile();
        TextDocumentIdentifier textDocumentIdentifier = dtoBuildHelper.createTDI(file);
        renameParams.setTextDocument(textDocumentIdentifier);

        TextPosition cursorPosition = ((TextEditor) activeEditor).getCursorPosition();
        org.eclipse.lsp4j.Position position =
            dtoFactory.createDto(org.eclipse.lsp4j.Position.class);
        position.setCharacter(cursorPosition.getCharacter());
        position.setLine(cursorPosition.getLine());
        renameParams.setPosition(position);
        renameSettings.setRenameKind(RenameKind.JAVA_ELEMENT);
      } else if (RefactoredItemType.COMPILATION_UNIT.equals(refactorInfo.getRefactoredItemType())) {
        renameSettings.setRenameKind(RenameKind.COMPILATION_UNIT);
        TextDocumentIdentifier textDocumentIdentifier =
            dtoFactory.createDto(TextDocumentIdentifier.class);
        textDocumentIdentifier.setUri(refactorInfo.getResources()[0].getLocation().toString());
        renameParams.setTextDocument(textDocumentIdentifier);
      } else if (RefactoredItemType.PACKAGE.equals(refactorInfo.getRefactoredItemType())) {
        renameSettings.setRenameKind(RenameKind.PACKAGE);
        TextDocumentIdentifier textDocumentIdentifier =
            dtoFactory.createDto(TextDocumentIdentifier.class);
        textDocumentIdentifier.setUri(refactorInfo.getResources()[0].getLocation().toString());
        renameParams.setTextDocument(textDocumentIdentifier);
      }
      return renameParams;
    }
  }
}
