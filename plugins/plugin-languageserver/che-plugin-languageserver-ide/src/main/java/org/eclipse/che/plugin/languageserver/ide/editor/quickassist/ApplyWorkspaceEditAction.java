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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import elemental.util.ArrayOf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.dto.DtoClientImpls.FileEditParamsDto;
import org.eclipse.che.api.languageserver.shared.model.FileEditParams;
import org.eclipse.che.api.languageserver.shared.util.RangeComparator;
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
import org.eclipse.che.ide.project.ProjectServiceClient;
import org.eclipse.che.ide.resource.Path;
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
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

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
      PromiseProvider promiseProvider) {
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
      changes =
          edit.getDocumentChanges()
              .stream()
              .collect(
                  Collectors.toMap(
                      (TextDocumentEdit e) -> e.getTextDocument().getUri(),
                      TextDocumentEdit::getEdits));
    }

    Promise<Void> done =
        promiseHelper.forEach(
            changes.entrySet().iterator(),
            (entry) -> handleFileChange(notification, entry.getKey(), entry.getValue()),
            undos::add);

    done.then(
            v -> {
              if (edit instanceof CheWorkspaceEdit) {
                applyResourceChanges(notification, (CheWorkspaceEdit) edit)
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
                notification.setContent(localization.applyWorkspaceActionNotificationDone());
              }
            })
        .catchError(
            (error) -> {
              Log.info(getClass(), "caught error applying changes", error);
              notification.setStatus(Status.FAIL);
              notification.setContent(localization.applyWorkspaceActionNotificationUndoing());
              promiseHelper
                  .forEach(undos.iterator(), Supplier::get, (Void v) -> {})
                  .then(
                      (Void v) -> {
                        notification.setContent(
                            localization.applyWorkspaceActionNotificationUndone());
                      })
                  .catchError(
                      e -> {
                        Log.info(getClass(), "Error undoing changes", e);
                        notification.setContent(
                            localization.applyWorkspaceActionNotificationUndoFailed());
                      });
            });
  }

  private Promise<Void> applyResourceChanges(
      StatusNotification notification, CheWorkspaceEdit edit) {
    List<CheResourceChange> resourceChanges = edit.getCheResourceChanges();
    if (resourceChanges.isEmpty()) {
      return promises.resolve(null);
    }

    ArrayOf<Promise<?>> changesPromises = elemental.util.Collections.arrayOf();

    for (CheResourceChange change : resourceChanges) {
      if (change == null) {
        continue;
      }
      String newUri = change.getNewUri();
      String current = change.getCurrent();

      Path path = Path.valueOf(newUri).makeAbsolute();
      if (isNullOrEmpty(current)) {
        createResource(path, change.getResourceKind(), notification);
        continue;
      }
      Path oldPath = Path.valueOf(current).makeAbsolute();

      Container workspaceRoot = appContext.getWorkspaceRoot();
      changesPromises.push(
          workspaceRoot
              .getResource(oldPath)
              .then(
                  resourceOptional -> {
                    if (!resourceOptional.isPresent()) {
                      return;
                    }

                    Resource resource = resourceOptional.get();

                    editorAgent.saveAll(
                        new AsyncCallback<Void>() {
                          @Override
                          public void onFailure(Throwable throwable) {
                            notification.setContent("Can't save files.");
                          }

                          @Override
                          public void onSuccess(Void aVoid) {
                            final List<EditorPartPresenter> openedEditors =
                                editorAgent.getOpenedEditors();
                            for (EditorPartPresenter editor : openedEditors) {
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
                            moveResource(resource, path, notification);
                          }
                        });
                  }));
    }
    return promises.all2(changesPromises).thenPromise(ignored -> promises.resolve(null));
  }

  private void createResource(Path path, ResourceKind kind, StatusNotification notification) {
    Container workspaceRoot = appContext.getWorkspaceRoot();
    workspaceRoot
        .getResource(path)
        .then(
            resource -> {
              if (ResourceKind.FOLDER.equals(kind)) {
                projectService
                    .createFolder(path)
                    .catchError(
                        error -> {
                          notification.setStatus(Status.FAIL);
                          notification.setContent(error.getMessage());
                        });
              } else if (ResourceKind.FILE.equals(kind)) {
                projectService
                    .createFile(path, "")
                    .catchError(
                        error -> {
                          notification.setStatus(Status.FAIL);
                          notification.setContent(error.getMessage());
                        });
              }
            });
  }

  private void moveResource(Resource resource, Path path, StatusNotification notification) {
    if (resource.isProject()) {
      closeRelatedEditors(resource);
    }
    resource
        .move(path)
        .then(
            movedResource -> {
              if (movedResource.isFolder()) {
                return;
              }
              final List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();

              for (EditorPartPresenter editor : openedEditors) {
                VirtualFile file = editor.getEditorInput().getFile();
                if (movedResource.getLocation().equals(file.getLocation())) {
                  eventBus.fireEvent(FileEvent.createFileOpenedEvent(file));
                  return;
                }
              }
            })
        .catchError(
            error -> {
              notification.setStatus(Status.FAIL);
              notification.setContent(error.getMessage());
            });
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
            () -> {
              return promiseProvider.create(
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
            };
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
      document.replace(
          start.getLine(), start.getCharacter(), end.getLine(), end.getCharacter(), e.getNewText());
    }
  }
}
