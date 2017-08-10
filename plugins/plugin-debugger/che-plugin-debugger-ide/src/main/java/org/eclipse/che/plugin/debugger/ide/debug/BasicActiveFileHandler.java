/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.common.base.Optional;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class BasicActiveFileHandler implements ActiveFileHandler {

    private final EditorAgent editorAgent;
    private final AppContext  appContext;

    @Inject
    public BasicActiveFileHandler(EditorAgent editorAgent, AppContext appContext) {
        this.editorAgent = editorAgent;
        this.appContext = appContext;
    }

    /**
     * Tries to find file and open it.
     * To perform the operation the following sequence of methods invocation are processed:
     *
     * {@link BasicActiveFileHandler#findInOpenedEditors(Location, AsyncCallback)}
     * {@link BasicActiveFileHandler#findInProject(Location, AsyncCallback)}
     * {@link BasicActiveFileHandler#findInWorkspace(Location, AsyncCallback)}
     * {@link BasicActiveFileHandler#searchSource(Location, AsyncCallback)}
     *
     * @see ActiveFileHandler#openFile(Location, AsyncCallback)
     */
    @Override
    public void openFile(final Location location, final AsyncCallback<VirtualFile> callback) {
        findInOpenedEditors(location, new AsyncCallback<VirtualFile>() {
            @Override
            public void onSuccess(VirtualFile result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                findSourceToOpen(location, callback);
            }
        });
    }

    protected void findSourceToOpen(Location location, AsyncCallback<VirtualFile> callback) {
        findInProject(location, new AsyncCallback<VirtualFile>() {
            @Override
            public void onSuccess(VirtualFile virtualFile) {
                callback.onSuccess(virtualFile);
            }

            @Override
            public void onFailure(Throwable caught) {
                findInWorkspace(location, new AsyncCallback<VirtualFile>() {
                    @Override
                    public void onSuccess(VirtualFile virtualFile) {
                        callback.onSuccess(virtualFile);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        searchSource(location, new AsyncCallback<VirtualFile>() {
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

    protected void findInOpenedEditors(final Location location,
                                       final AsyncCallback<VirtualFile> callback) {
        for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
            VirtualFile file = editor.getEditorInput().getFile();
            String filePath = file.getLocation().toString();

            if (filePath.equals(location.getResourcePath()) || filePath.equals(location.getTarget())) {
                openFileAndScrollToLine(file, location.getLineNumber(), callback);
                return;
            }
        }

        callback.onFailure(new IllegalArgumentException("There is no opened editors for " + location));
    }

    protected void findInProject(final Location location,
                                 final AsyncCallback<VirtualFile> callback) {
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

        project.get().getFile(getPath(location)).then(file -> {

            if (file.isPresent()) {
                openFileAndScrollToLine(file.get(), location.getLineNumber(), callback);
            } else {
                callback.onFailure(new IllegalArgumentException(location + " not found."));
            }

        }).catchError(error -> {
            callback.onFailure(new IllegalArgumentException(location + " not found."));
        });
    }

    protected void findInWorkspace(final Location location,
                                   final AsyncCallback<VirtualFile> callback) {

        appContext.getWorkspaceRoot().getFile(getPath(location)).then(file -> {

            if (file.isPresent()) {
                openFileAndScrollToLine(file.get(), location.getLineNumber(), callback);
            } else {
                callback.onFailure(new IllegalArgumentException(location + " not found."));
            }

        }).catchError(error -> {
            callback.onFailure(new IllegalArgumentException(location + " not found."));
        });
    }

    protected void searchSource(final Location location, final AsyncCallback<VirtualFile> callback) {
        appContext.getWorkspaceRoot().search(getPath(location), "").then(new Operation<Resource[]>() {
            @Override
            public void apply(Resource[] resources) throws OperationException {
                if (resources.length == 0) {
                    callback.onFailure(new IllegalArgumentException(location + " not found."));
                    return;
                }

                appContext.getWorkspaceRoot().getFile(resources[0].getLocation()).then(new Operation<Optional<File>>() {
                    @Override
                    public void apply(Optional<File> file) throws OperationException {
                        if (file.isPresent()) {
                            openFileAndScrollToLine(file.get(), location.getLineNumber(), callback);
                        } else {
                            callback.onFailure(new IllegalArgumentException(location + " not found."));
                        }
                    }
                }).catchError(error -> {
                    callback.onFailure(new IllegalArgumentException(location + " not found."));
                });
            }
        }).catchError(error -> {
            callback.onFailure(new IllegalArgumentException(location + " not found."));
        });
    }

    protected void openFileAndScrollToLine(final VirtualFile virtualFile,
                                           final int scrollToLine,
                                           final AsyncCallback<VirtualFile> callback) {
        editorAgent.openEditor(virtualFile, new EditorAgent.OpenEditorCallback() {
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
                callback.onFailure(new IllegalStateException("Initialization " + virtualFile.getName() + " in the editor failed"));
            }
        });
    }

    protected void scrollToLine(EditorPartPresenter editor, int lineNumber) {
        if (editor instanceof TextEditor) {
            TextEditor textEditor = (TextEditor)editor;
            textEditor.setCursorPosition(new TextPosition(lineNumber + 1, 0));
        }
    }

    protected String getPath(Location location) {
        return location.getResourcePath() != null ? location.getResourcePath()
                                                  : location.getTarget();
    }
}
