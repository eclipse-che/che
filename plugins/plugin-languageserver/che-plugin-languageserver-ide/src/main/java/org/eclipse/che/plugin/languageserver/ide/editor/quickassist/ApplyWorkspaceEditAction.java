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
package org.eclipse.che.plugin.languageserver.ide.editor.quickassist;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.dto.DtoClientImpls.FileEditParamsDto;
import org.eclipse.che.api.languageserver.shared.model.FileEditParams;
import org.eclipse.che.api.languageserver.shared.util.RangeComparator;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.*;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.FileEvent;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.HandlesUndoRedo;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import org.eclipse.che.ide.api.notification.StatusNotification.Status;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.editor.EditorFileStatusNotificationOperation;
import org.eclipse.che.ide.project.ProjectServiceClient;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.jdt.ls.extension.api.ResourceKind;
import org.eclipse.che.jdt.ls.extension.api.dto.CheResourceChange;
import org.eclipse.che.jdt.ls.extension.api.dto.CheWorkspaceEdit;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerLocalization;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.service.WorkspaceServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.che.plugin.languageserver.ide.util.PromiseHelper;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

@Singleton
public class ApplyWorkspaceEditAction extends BaseAction {
  private static final Comparator<TextEdit> COMPARATOR =
      RangeComparator.transform(new RangeComparator(), TextEdit::getRange);

  private EditorAgent editorAgent;
  private DtoFactory dtoFactory;
  private DtoBuildHelper dtoHelper;
  private AppContext appContext;
  private WorkspaceServiceClient workspaceService;
  private ProjectServiceClient projectService;
  private EventBus eventBus;
  private PromiseHelper promiseHelper;
  private PromiseProvider promises;
  private TextDocumentServiceClient textDocumentService;
  private LanguageServerLocalization localization;
  private NotificationManager notificationManager;
  private PromiseProvider promiseProvider;

  private EditorFileStatusNotificationOperation fileStatusNotifier;

  @Inject
  public ApplyWorkspaceEditAction(
      EditorAgent editorAgent,
      DtoFactory dtoFactory,
      DtoBuildHelper dtoHelper,
      AppContext appContext,
      WorkspaceServiceClient workspaceService,
      ProjectServiceClient projectService,
      EventBus eventBus,
      PromiseHelper promiseHelper,
      PromiseProvider promises,
      TextDocumentServiceClient textDocumentService,
      LanguageServerLocalization localization,
      NotificationManager notificationManager,
      PromiseProvider promiseProvider,
      EditorFileStatusNotificationOperation fileStatusNotifier) {
    this.editorAgent = editorAgent;
    this.dtoFactory = dtoFactory;
    this.dtoHelper = dtoHelper;
    this.appContext = appContext;
    this.workspaceService = workspaceService;
    this.projectService = projectService;
    this.eventBus = eventBus;
    this.promiseHelper = promiseHelper;
    this.promises = promises;
    this.textDocumentService = textDocumentService;
    this.localization = localization;
    this.notificationManager = notificationManager;
    this.promiseProvider = promiseProvider;
    this.fileStatusNotifier = fileStatusNotifier;
  }

  @Override
  public void actionPerformed(ActionEvent evt) {
    QuickassistActionEvent qaEvent = (QuickassistActionEvent) evt;
    List<Object> arguments = qaEvent.getArguments();
    WorkspaceEdit edit =
        dtoFactory.createDtoFromJson(arguments.get(0).toString(), WorkspaceEdit.class);
    applyWorkspaceEdit(edit);
  }

  public void applyWorkspaceEdit(WorkspaceEdit edit) {
    List<Supplier<Promise<Void>>> undos = new ArrayList<>();

    StatusNotification notification =
        notificationManager.notify(
            localization.applyWorkspaceActionNotificationTitle(),
            Status.PROGRESS,
            DisplayMode.FLOAT_MODE);

    Map<String, List<TextEdit>> changes = null;
    if (edit.getChanges() != null) {
      changes = edit.getChanges();
    } else if (edit.getDocumentChanges() != null) {
      changes = new HashMap<>();
      for (Either<TextDocumentEdit, ResourceOperation> e : edit.getDocumentChanges()) {
        // we're not announcing resource change capabilities, so assume none present
        changes.put(e.getLeft().getTextDocument().getUri(), e.getLeft().getEdits());
      }
    }

    fileStatusNotifier.suspend();
    Promise<Void> done =
        promiseHelper.forEach(
            changes.entrySet().iterator(),
            (entry) -> handleFileChange(notification, entry.getKey(), entry.getValue()),
            undos::add);

    done.then(
            v -> {
              updateEditors()
                  .then(
                      new Operation<Void>() {
                        @Override
                        public void apply(Void arg) {
                          if (edit instanceof CheWorkspaceEdit) {
                            appResourceChanges(
                                    ((CheWorkspaceEdit) edit).getCheResourceChanges().iterator(),
                                    notification)
                                .then(
                                    ignored -> {
                                      Log.debug(getClass(), "done applying changes");
                                      notification.setStatus(Status.SUCCESS);
                                      notification.setContent(
                                          localization.applyWorkspaceActionNotificationDone());
                                    });
                          } else {
                            Log.debug(getClass(), "done applying changes");
                            notification.setStatus(Status.SUCCESS);
                            notification.setContent(
                                localization.applyWorkspaceActionNotificationDone());
                          }
                        }
                      });
            })
        .then(
            (nul) -> {
              fileStatusNotifier.resume();
            })
        .catchError(
            (error) -> {
              Log.info(getClass(), "caught error applying changes", error);
              notification.setStatus(Status.FAIL);
              notification.setContent(localization.applyWorkspaceActionNotificationUndoing());
              promiseHelper
                  .forEach(undos.iterator(), Supplier::get, (Void v) -> {})
                  .then(
                      (nul) -> {
                        notification.setContent(
                            localization.applyWorkspaceActionNotificationUndone());
                      })
                  .then(
                      (nul) -> {
                        fileStatusNotifier.resume();
                      })
                  .catchError(
                      e -> {
                        fileStatusNotifier.resume();
                        Log.info(getClass(), "Error undoing changes", e);
                        notification.setContent(
                            localization.applyWorkspaceActionNotificationUndoFailed());
                      });
            });
  };

  private Promise<Void> deleteResouce(Path oldPath) {
    return projectService.deleteItem(oldPath);
  }

  private Promise<Void> updateEditors() {
    return promises.create(
        callback ->
            Scheduler.get()
                .scheduleDeferred(
                    () -> {
                      editorAgent.getOpenedEditors().forEach(EditorPartPresenter::doSave);
                      callback.onSuccess(null);
                    }));
  }

  private Promise<?> appResourceChanges(
      Iterator<CheResourceChange> resourceChangesIterator, StatusNotification notification) {
    if (!resourceChangesIterator.hasNext()) {
      return promises.resolve(null);
    }
    CheResourceChange change = resourceChangesIterator.next();
    if (change == null) {
      return appResourceChanges(resourceChangesIterator, notification);
    }
    String newUri = change.getNewUri();
    String current = change.getCurrent();

    if (isNullOrEmpty(current) && isNullOrEmpty(newUri)) {
      return appResourceChanges(resourceChangesIterator, notification);
    }

    if (isNullOrEmpty(newUri)) {
      Path oldPath = Path.valueOf(current).makeAbsolute();
      return deleteResouce(oldPath)
          .then(
              (Function<Void, Promise<?>>)
                  ignored -> appResourceChanges(resourceChangesIterator, notification));
    }

    Path path = Path.valueOf(newUri).makeAbsolute();
    if (isNullOrEmpty(current)) {
      return createResource(path, change.getResourceKind(), notification)
          .then(
              (Function<ItemReference, Promise<?>>)
                  ignored -> appResourceChanges(resourceChangesIterator, notification));
    }

    Path oldPath = Path.valueOf(current).makeAbsolute();

    Container workspaceRoot = appContext.getWorkspaceRoot();

    return workspaceRoot
        .getResource(oldPath)
        .thenPromise(
            resourceOptional -> {
              if (!resourceOptional.isPresent()) {
                return promises.resolve(null);
              }
              Resource resource = resourceOptional.get();

              if (resource.isProject()) {
                closeRelatedEditors(resource);
              }

              for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
                if (resource
                    .getLocation()
                    .isPrefixOf(editor.getEditorInput().getFile().getLocation())) {
                  TextDocumentIdentifier documentId =
                      dtoHelper.createTDI(editor.getEditorInput().getFile());
                  DidCloseTextDocumentParams closeEvent =
                      dtoFactory.createDto(DidCloseTextDocumentParams.class);
                  closeEvent.setTextDocument(documentId);
                  textDocumentService.didClose(closeEvent);
                }
              }
              return moveResource(resourceChangesIterator, notification, path, resource);
            });
  }

  private Promise<Object> moveResource(
      Iterator<CheResourceChange> resourceChangesIterator,
      StatusNotification notification,
      Path path,
      Resource resource) {
    return resource
        .move(path)
        .then(
            (Function<Resource, Object>)
                movedResource -> {
                  eventBus.fireEvent(new RevealResourceEvent(movedResource.getLocation()));
                  if (movedResource.isFolder()) {
                    return appResourceChanges(resourceChangesIterator, notification);
                  }
                  final List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();

                  for (EditorPartPresenter editor : openedEditors) {
                    VirtualFile file = editor.getEditorInput().getFile();
                    if (movedResource.getLocation().equals(file.getLocation())) {
                      eventBus.fireEvent(FileEvent.createFileOpenedEvent(file));
                      return appResourceChanges(resourceChangesIterator, notification);
                    }
                  }
                  return appResourceChanges(resourceChangesIterator, notification);
                })
        .catchError(
            error -> {
              notification.setStatus(Status.FAIL);
              notification.setContent(error.getMessage());
            });
  }

  private Promise<ItemReference> createResource(
      Path path, ResourceKind kind, StatusNotification notification) {
    if (ResourceKind.FOLDER.equals(kind)) {
      return projectService
          .createFolder(path)
          .catchError(
              error -> {
                notification.setStatus(Status.FAIL);
                notification.setContent(error.getMessage());
              });
    } else {
      return projectService
          .createFile(path, "")
          .catchError(
              error -> {
                notification.setStatus(Status.FAIL);
                notification.setContent(error.getMessage());
              });
    }
  }

  private void closeRelatedEditors(Resource resource) {
    final List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();

    for (EditorPartPresenter editor : openedEditors) {
      if (resource.getLocation().isPrefixOf(editor.getEditorInput().getFile().getLocation())) {
        editorAgent.closeEditor(editor);
      }
    }
  }

  private Promise<Supplier<Promise<Void>>> handleFileChange(
      Notification notification, String uri, List<TextEdit> edits) {
    for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
      if (editor instanceof TextEditor
          && uri.endsWith(editor.getEditorInput().getFile().getLocation().toString())) {
        notification.setContent(localization.applyWorkspaceActionNotificationModifying(uri));
        TextEditor textEditor = (TextEditor) editor;
        HandlesUndoRedo undoRedo = textEditor.getEditorWidget().getUndoRedo();
        undoRedo.beginCompoundChange();
        applyTextEdits(textEditor.getDocument(), edits);
        undoRedo.endCompoundChange();

        Supplier<Promise<Void>> value =
            () ->
                promiseProvider.create(
                    Executor.create(
                        (ResolveFunction<Void> resolve, RejectFunction reject) -> {
                          try {
                            undoRedo.undo();
                            resolve.apply(null);
                          } catch (Exception e) {
                            reject.apply(
                                new PromiseError() {
                                  public String getMessage() {
                                    return "Error during undo";
                                  }

                                  public Throwable getCause() {
                                    return e;
                                  }
                                });
                          }
                        }));
        return promiseProvider.resolve(value);
      }
    }
    Promise<List<TextEdit>> undoPromise =
        workspaceService.editFile(new FileEditParamsDto(new FileEditParams(uri, edits)));
    return undoPromise.then(
        (Function<List<TextEdit>, Supplier<Promise<Void>>>)
            (List<TextEdit> undoEdits) -> {
              return () -> {
                Promise<List<TextEdit>> redoPromise =
                    workspaceService.editFile(
                        new FileEditParamsDto(new FileEditParams(uri, undoEdits)));
                return redoPromise.then(
                    (List<TextEdit> redo) -> {
                      return null;
                    });
              };
            });
  }

  public static void applyTextEdits(Document document, List<TextEdit> edits) {
    edits = new ArrayList<>(edits);
    Collections.sort(edits, COMPARATOR);
    // jdt.ls sends text edits in reverse order of application
    // see https://github.com/eclipse/eclipse.jdt.ls/issues/398
    for (int i = edits.size() - 1; i >= 0; i--) {
      TextEdit e = edits.get(i);
      Range r = e.getRange();
      Position start = r.getStart();
      Position end = r.getEnd();
      int startIndex =
          document.getIndexFromPosition(new TextPosition(start.getLine(), start.getCharacter()));
      // python ls, for example shows as end position index 0 of the line after the
      // change. If the change is on the last line, we crash
      int endIndex =
          document.getIndexFromPosition(new TextPosition(end.getLine(), end.getCharacter()));
      if (endIndex < 0) {
        endIndex = document.getContentsCharCount();
      }
      document.replace(startIndex, endIndex - startIndex, e.getNewText());
    }
  }
}
