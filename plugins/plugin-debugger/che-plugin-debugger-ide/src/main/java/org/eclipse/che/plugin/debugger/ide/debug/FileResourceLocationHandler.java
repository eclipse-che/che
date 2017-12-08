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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.common.base.Optional;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.SearchItemReference;
import org.eclipse.che.ide.api.resources.VirtualFile;

/** @author Anatoliy Bazko */
@Singleton
public class FileResourceLocationHandler implements DebuggerLocationHandler {

  protected final EditorAgent editorAgent;
  protected final AppContext appContext;

  @Inject
  public FileResourceLocationHandler(EditorAgent editorAgent, AppContext appContext) {
    this.editorAgent = editorAgent;
    this.appContext = appContext;
  }

  @Override
  public boolean isSuitedFor(Location location) {
    return true;
  }

  /**
   * Tries to find file and open it. To perform the operation the following sequence of methods
   * invocation are processed:
   *
   * <p>{@link FileResourceLocationHandler#findInOpenedEditors(Location, AsyncCallback)} {@link
   * FileResourceLocationHandler#findInProject(Location, AsyncCallback)} {@link
   * FileResourceLocationHandler#findInWorkspace(Location, AsyncCallback)} {@link
   * FileResourceLocationHandler#searchSource(Location, AsyncCallback)}
   *
   * @see DebuggerLocationHandler#open(Location, AsyncCallback)
   */
  @Override
  public void open(final Location location, final AsyncCallback<VirtualFile> callback) {
    find(
        location,
        new AsyncCallback<VirtualFile>() {
          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(VirtualFile result) {
            openFileAndScrollToLine(result, location.getLineNumber(), callback);
          }
        });
  }

  @Override
  public void find(Location location, AsyncCallback<VirtualFile> callback) {
    try {
      doFind(location, callback);
    } catch (Exception e) {
      callback.onFailure(e);
    }
  }

  private void doFind(Location location, AsyncCallback<VirtualFile> callback) {
    findInOpenedEditors(
        location,
        new AsyncCallback<VirtualFile>() {
          @Override
          public void onSuccess(VirtualFile result) {
            callback.onSuccess(result);
          }

          @Override
          public void onFailure(Throwable caught) {
            findInProject(
                location,
                new AsyncCallback<VirtualFile>() {
                  @Override
                  public void onSuccess(VirtualFile virtualFile) {
                    callback.onSuccess(virtualFile);
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                    findInWorkspace(
                        location,
                        new AsyncCallback<VirtualFile>() {
                          @Override
                          public void onSuccess(VirtualFile virtualFile) {
                            callback.onSuccess(virtualFile);
                          }

                          @Override
                          public void onFailure(Throwable caught) {
                            searchSource(
                                location,
                                new AsyncCallback<VirtualFile>() {
                                  @Override
                                  public void onSuccess(VirtualFile result) {
                                    callback.onSuccess(result);
                                  }

                                  @Override
                                  public void onFailure(Throwable error) {
                                    callback.onFailure(error);
                                  }
                                });
                          }
                        });
                  }
                });
          }
        });
  }

  protected void findInOpenedEditors(
      final Location location, final AsyncCallback<VirtualFile> callback) {
    for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
      VirtualFile file = editor.getEditorInput().getFile();
      String filePath = file.getLocation().toString();

      if (filePath.equals(location.getTarget())) {
        callback.onSuccess(file);
        return;
      }
    }

    callback.onFailure(new IllegalArgumentException("There is no opened editors for " + location));
  }

  protected void findInProject(final Location location, final AsyncCallback<VirtualFile> callback) {
    Resource resource = appContext.getResource();
    if (resource == null) {
      callback.onFailure(new IllegalStateException("Resource is undefined"));
      return;
    }

    Optional<Project> project = resource.getRelatedProject();
    if (!project.isPresent()) {
      callback.onFailure(new IllegalStateException("Project is undefined"));
      return;
    }

    project
        .get()
        .getFile(location.getTarget())
        .then(
            file -> {
              if (file.isPresent()) {
                callback.onSuccess(file.get());
              } else {
                callback.onFailure(new IllegalArgumentException(location + " not found."));
              }
            })
        .catchError(
            error -> {
              callback.onFailure(new IllegalArgumentException(location + " not found."));
            });
  }

  protected void findInWorkspace(
      final Location location, final AsyncCallback<VirtualFile> callback) {

    appContext
        .getWorkspaceRoot()
        .getFile(location.getTarget())
        .then(
            file -> {
              if (file.isPresent()) {
                callback.onSuccess(file.get());
              } else {
                callback.onFailure(new IllegalArgumentException(location + " not found."));
              }
            })
        .catchError(
            error -> {
              callback.onFailure(new IllegalArgumentException(location + " not found."));
            });
  }

  protected void searchSource(final Location location, final AsyncCallback<VirtualFile> callback) {
    appContext
        .getWorkspaceRoot()
        .search(location.getTarget(), "")
        .then(
            result -> {
              List<SearchItemReference> resources = result.getItemReferences();
              if (resources.isEmpty()) {
                callback.onFailure(
                    new IllegalArgumentException(location.getTarget() + " not found."));
                return;
              }

              appContext
                  .getWorkspaceRoot()
                  .getFile(resources.get(0).getPath())
                  .then(
                      file -> {
                        if (file.isPresent()) {
                          callback.onSuccess(file.get());
                        } else {
                          callback.onFailure(
                              new IllegalArgumentException(location + " not found."));
                        }
                      })
                  .catchError(
                      error -> {
                        callback.onFailure(new IllegalArgumentException(location + " not found."));
                      });
            })
        .catchError(
            error -> {
              callback.onFailure(new IllegalArgumentException(location + " not found."));
            });
  }

  protected void openFileAndScrollToLine(
      final VirtualFile virtualFile,
      final int scrollToLine,
      final AsyncCallback<VirtualFile> callback) {
    editorAgent.openEditor(
        virtualFile,
        new EditorAgent.OpenEditorCallback() {
          @Override
          public void onEditorOpened(EditorPartPresenter editor) {
            new Timer() {
              @Override
              public void run() {
                scrollToLine(editorAgent.getActiveEditor(), scrollToLine);
                callback.onSuccess(virtualFile);
              }
            }.schedule(300);
          }

          @Override
          public void onEditorActivated(EditorPartPresenter editor) {
            new Timer() {
              @Override
              public void run() {
                scrollToLine(editorAgent.getActiveEditor(), scrollToLine);
                callback.onSuccess(virtualFile);
              }
            }.schedule(300);
          }

          @Override
          public void onInitializationFailed() {
            callback.onFailure(
                new IllegalStateException(
                    "Initialization " + virtualFile.getName() + " in the editor failed"));
          }
        });
  }

  protected void scrollToLine(EditorPartPresenter editor, int lineNumber) {
    if (editor instanceof TextEditor) {
      TextEditor textEditor = (TextEditor) editor;
      textEditor.setCursorPosition(new TextPosition(lineNumber - 1, 0));
    }
  }
}
