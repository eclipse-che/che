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
package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import static org.eclipse.che.ide.api.editor.events.FileEvent.FileOperation.CLOSE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.jdt.ls.extension.api.RefactoringSeverity.FATAL;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.api.editor.EditorWithAutoSave;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.editor.events.FileEvent.FileEventHandler;
import org.eclipse.che.ide.api.editor.link.HasLinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedMode;
import org.eclipse.che.ide.api.editor.link.LinkedModel;
import org.eclipse.che.ide.api.editor.link.LinkedModelData;
import org.eclipse.che.ide.api.editor.link.LinkedModelGroup;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.editor.texteditor.UndoableEditor;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.jdt.ls.extension.api.RenameKind;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatusEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.RenameSettings;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyWorkspaceEditAction;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;

/**
 * Class for rename refactoring java classes
 *
 * @author Alexander Andrienko
 * @author Valeriy Svydenko
 */
@Singleton
public class JavaRefactoringRename implements FileEventHandler {
  private final RenamePresenter renamePresenter;
  private final DtoBuildHelper dtoHelper;
  private final ApplyWorkspaceEditAction applyWorkspaceEditAction;
  private final DtoBuildHelper dtoBuildHelper;
  private final TextDocumentServiceClient textDocumentServiceClient;
  private final JavaLocalizationConstant locale;
  private final DtoFactory dtoFactory;
  private final JavaLanguageExtensionServiceClient extensionServiceClient;
  private final ClientServerEventService clientServerEventService;
  private final EventBus eventBus;
  private final DialogFactory dialogFactory;
  private final NotificationManager notificationManager;

  private boolean isActiveLinkedEditor;
  private TextEditor textEditor;
  private LinkedMode mode;
  private HasLinkedMode linkedEditor;
  private String newName;
  private TextPosition cursorPosition;

  @Inject
  public JavaRefactoringRename(
      RenamePresenter renamePresenter,
      DtoBuildHelper dtoHelper,
      ApplyWorkspaceEditAction applyWorkspaceEditAction,
      DtoBuildHelper dtoBuildHelper,
      TextDocumentServiceClient textDocumentServiceClient,
      JavaLocalizationConstant locale,
      JavaLanguageExtensionServiceClient extensionServiceClient,
      ClientServerEventService clientServerEventService,
      DtoFactory dtoFactory,
      EventBus eventBus,
      DialogFactory dialogFactory,
      NotificationManager notificationManager) {
    this.renamePresenter = renamePresenter;
    this.dtoHelper = dtoHelper;
    this.applyWorkspaceEditAction = applyWorkspaceEditAction;
    this.dtoBuildHelper = dtoBuildHelper;
    this.textDocumentServiceClient = textDocumentServiceClient;
    this.locale = locale;
    this.extensionServiceClient = extensionServiceClient;
    this.clientServerEventService = clientServerEventService;
    this.eventBus = eventBus;
    this.dialogFactory = dialogFactory;
    this.dtoFactory = dtoFactory;
    this.notificationManager = notificationManager;

    isActiveLinkedEditor = false;

    eventBus.addHandler(FileEvent.TYPE, this);
  }

  /**
   * Launch java rename refactoring process
   *
   * @param textEditorPresenter editor where user makes refactoring
   */
  public void refactor(final TextEditor textEditorPresenter) {
    if (!(textEditorPresenter instanceof HasLinkedMode)) {
      return;
    }

    if (isActiveLinkedEditor) {
      if (mode != null) {
        mode.exitLinkedMode(false);
      }
      renamePresenter.show(RefactorInfo.of(RefactoredItemType.JAVA_ELEMENT, null));
      isActiveLinkedEditor = false;
    } else {
      isActiveLinkedEditor = true;
      textEditor = textEditorPresenter;
      createLinkedRename();
    }

    linkedEditor = (HasLinkedMode) textEditorPresenter;
    textEditorPresenter.setFocus();
  }

  private void showError() {
    dialogFactory
        .createMessageDialog(locale.renameRename(), locale.renameOperationUnavailable(), null)
        .show();
    if (mode != null) {
      mode.exitLinkedMode(false);
    }
  }

  private void createLinkedRename() {
    cursorPosition = textEditor.getCursorPosition();

    Document document = textEditor.getDocument();
    TextDocumentPositionParams params =
        dtoBuildHelper.createTDPP(document, textEditor.getCursorOffset());

    extensionServiceClient
        .getLinkedModeModel(params)
        .then(
            ranges -> {
              clientServerEventService
                  .sendFileTrackingSuspendEvent()
                  .then(
                      success -> {
                        if (ranges == null || ranges.isEmpty()) {
                          showError();
                          isActiveLinkedEditor = false;
                          clientServerEventService.sendFileTrackingResumeEvent();
                          return;
                        }
                        activateLinkedModeIntoEditor(ranges);
                      });
            })
        .catchError(
            arg -> {
              isActiveLinkedEditor = false;
              showError();
            });
  }

  @Override
  public void onFileOperation(FileEvent event) {
    if (event.getOperationType() == CLOSE
        && textEditor != null
        && textEditor.getDocument() != null
        && textEditor.getDocument().getFile().getLocation().equals(event.getFile().getLocation())) {
      isActiveLinkedEditor = false;
    }
  }

  /** returns {@code true} if linked editor is activated. */
  public boolean isActiveLinkedEditor() {
    return isActiveLinkedEditor;
  }

  private void activateLinkedModeIntoEditor(List<Range> ranges) {
    sendCloseEvent();
    mode = linkedEditor.getLinkedMode();
    LinkedModel model = linkedEditor.createLinkedModel();
    List<LinkedModelGroup> groups = new ArrayList<>();
    LinkedModelGroup group = linkedEditor.createLinkedGroup();
    List<Position> positions = new ArrayList<>();
    for (Range range : ranges) {
      LinkedModelData modelData = linkedEditor.createLinkedModelData();
      modelData.setType("link");
      group.setData(modelData);
      positions.add(createPositionFromRange(range, textEditor.getDocument()));
    }
    group.setPositions(positions);
    groups.add(group);
    model.setGroups(groups);
    disableAutoSave();

    mode.enterLinkedMode(model);

    mode.addListener(
        new LinkedMode.LinkedModeListener() {
          @Override
          public void onLinkedModeExited(boolean successful, int start, int end) {
            boolean isSuccessful = false;
            try {
              if (successful) {
                isSuccessful = true;
                newName = textEditor.getDocument().getContentRange(start, end - start);
                performRename(newName);
              }
            } finally {
              mode.removeListener(this);

              isActiveLinkedEditor = false;

              boolean isNameChanged = start >= 0 && end >= 0;
              if (!isSuccessful && isNameChanged && textEditor.isDirty()) {
                undoChanges();
              }

              if (!isSuccessful) {
                clientServerEventService
                    .sendFileTrackingResumeEvent()
                    .then(
                        arg -> {
                          enableAutoSave();
                        });
              }
            }
          }
        });
  }

  private Position createPositionFromRange(Range range, Document document) {
    int start =
        document.getIndexFromPosition(
            new TextPosition(range.getStart().getLine(), range.getStart().getCharacter()));
    int end =
        document.getIndexFromPosition(
            new TextPosition(range.getEnd().getLine(), range.getEnd().getCharacter()));

    if (start == -1 && end == -1) {
      return new Position(0);
    }

    if (start == -1) {
      return new Position(end);
    }

    if (end == -1) {
      return new Position(start);
    }

    int length = end - start;
    if (length < 0) {
      return null;
    }
    return new Position(start, length);
  }

  private void sendCloseEvent() {
    TextDocumentIdentifier documentId = dtoHelper.createTDI(textEditor.getEditorInput().getFile());
    DidCloseTextDocumentParams closeEvent = dtoFactory.createDto(DidCloseTextDocumentParams.class);
    closeEvent.setTextDocument(documentId);
    textDocumentServiceClient.didClose(closeEvent);
  }

  private void sendOpenEvent() {
    eventBus.fireEvent(FileEvent.createFileOpenedEvent(textEditor.getEditorInput().getFile()));
  }

  private void performRename(String newName) {
    RenameSettings settings = dtoFactory.createDto(RenameSettings.class);

    RenameParams renameParams = dtoFactory.createDto(RenameParams.class);
    renameParams.setNewName(newName);
    VirtualFile file = textEditor.getEditorInput().getFile();
    TextDocumentIdentifier textDocumentIdentifier = dtoBuildHelper.createTDI(file);
    renameParams.setTextDocument(textDocumentIdentifier);

    org.eclipse.lsp4j.Position position = dtoFactory.createDto(org.eclipse.lsp4j.Position.class);
    position.setCharacter(cursorPosition.getCharacter());
    position.setLine(cursorPosition.getLine());
    renameParams.setPosition(position);

    settings.setUpdateReferences(true);
    settings.setRenameParams(renameParams);
    settings.setRenameKind(RenameKind.JAVA_ELEMENT);

    extensionServiceClient
        .rename(settings)
        .then(
            result -> {
              enableAutoSave();
              undoChanges();
              RefactoringStatus refactoringStatus = result.getRefactoringStatus();
              if (!FATAL.equals(refactoringStatus.getRefactoringSeverity())) {
                applyWorkspaceEditAction.applyWorkspaceEdit(result.getCheWorkspaceEdit());
                clientServerEventService.sendFileTrackingResumeEvent();
                sendOpenEvent();
              } else {
                notificationManager.notify(
                    locale.failedToRename(),
                    getErrorMessage(refactoringStatus.getRefactoringStatusEntries()),
                    FAIL,
                    FLOAT_MODE);
              }
            })
        .catchError(
            error -> {
              undoChanges();
              enableAutoSave();
              clientServerEventService.sendFileTrackingResumeEvent();
              notificationManager.notify(
                  locale.failedToRename(), error.getMessage(), FAIL, FLOAT_MODE);
            });
  }

  private String getErrorMessage(List<RefactoringStatusEntry> entries) {
    for (RefactoringStatusEntry entry : entries) {
      if (FATAL.equals(entry.getRefactoringSeverity())) {
        return entry.getMessage();
      }
    }
    return "";
  }

  private void enableAutoSave() {
    if (linkedEditor instanceof EditorWithAutoSave) {
      ((EditorWithAutoSave) linkedEditor).enableAutoSave();
    }
  }

  private void disableAutoSave() {
    if (linkedEditor instanceof EditorWithAutoSave) {
      ((EditorWithAutoSave) linkedEditor).disableAutoSave();
    }
  }

  private void undoChanges() {
    if (linkedEditor instanceof UndoableEditor) {
      ((UndoableEditor) linkedEditor).getUndoRedo().undo();
    }
  }
}
