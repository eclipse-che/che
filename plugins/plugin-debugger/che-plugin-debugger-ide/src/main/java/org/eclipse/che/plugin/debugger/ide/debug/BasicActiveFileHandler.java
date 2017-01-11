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

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;

/**
 * @author Anatoliy Bazko
 */
public class BasicActiveFileHandler implements ActiveFileHandler {

    private final EditorAgent editorAgent;
    private final AppContext  appContext;

    @Inject
    public BasicActiveFileHandler(EditorAgent editorAgent, AppContext appContext) {
        this.editorAgent = editorAgent;
        this.appContext = appContext;
    }

    /**
     * Tries to open file in the editor.
     * To perform the operation the following sequence of methods invocation are processed:
     *
     * {@link BasicActiveFileHandler#tryFindFileInProject(Location, AsyncCallback)}
     * {@link BasicActiveFileHandler#tryFindFileInWorkspace(Location, AsyncCallback)}
     * {@link BasicActiveFileHandler#trySearchSource(Location, AsyncCallback)}
     *
     * @see ActiveFileHandler#openFile(Location, AsyncCallback)
     */
    @Override
    public void openFile(final Location location, final AsyncCallback<VirtualFile> callback) {
        tryFindFileInProject(location, new AsyncCallback<VirtualFile>() {
            @Override
            public void onSuccess(VirtualFile virtualFile) {
                callback.onSuccess(virtualFile);
            }

            @Override
            public void onFailure(Throwable caught) {
                tryFindFileInWorkspace(location, new AsyncCallback<VirtualFile>() {
                    @Override
                    public void onSuccess(VirtualFile virtualFile) {
                        callback.onSuccess(virtualFile);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        trySearchSource(location, new AsyncCallback<VirtualFile>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                callback.onFailure(caught);
                            }

                            @Override
                            public void onSuccess(VirtualFile result) {
                                callback.onSuccess(result);
                            }
                        });
                    }
                });
            }
        });
    }

    protected void tryFindFileInProject(final Location location,
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

        project.get().getFile(location.getTarget()).then(new Operation<Optional<File>>() {
            @Override
            public void apply(Optional<File> file) throws OperationException {
                if (file.isPresent()) {
                    openFileAndScrollToLine(file.get(), location.getLineNumber(), callback);
                } else {
                    callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
            }
        });
    }

    protected void tryFindFileInWorkspace(final Location location,
                                          final AsyncCallback<VirtualFile> callback) {
        try {
            appContext.getWorkspaceRoot().getFile(location.getTarget()).then(new Operation<Optional<File>>() {
                @Override
                public void apply(Optional<File> file) throws OperationException {
                    if (file.isPresent()) {
                        openFileAndScrollToLine(file.get(), location.getLineNumber(), callback);
                    } else {
                        callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
                    }
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
                }
            });
        } catch (IllegalStateException ignored) {
            callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
        }
    }

    protected void trySearchSource(final Location location, final AsyncCallback<VirtualFile> callback) {
        appContext.getWorkspaceRoot().search(location.getTarget(), "").then(new Operation<Resource[]>() {
            @Override
            public void apply(Resource[] resources) throws OperationException {
                if (resources.length == 0) {
                    callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
                    return;
                }

                appContext.getWorkspaceRoot().getFile(resources[0].getLocation()).then(new Operation<Optional<File>>() {
                    @Override
                    public void apply(Optional<File> file) throws OperationException {
                        if (file.isPresent()) {
                            openFileAndScrollToLine(file.get(), location.getLineNumber(), callback);
                        } else {
                            callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
                        }
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
                    }
                });
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(new IllegalArgumentException(location.getTarget() + " not found."));
            }
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
            Document document = textEditor.getDocument();
            if (document != null) {
                TextPosition newPosition = new TextPosition(lineNumber, 0);
                document.setCursorPosition(newPosition);
            }
        }
    }
}
