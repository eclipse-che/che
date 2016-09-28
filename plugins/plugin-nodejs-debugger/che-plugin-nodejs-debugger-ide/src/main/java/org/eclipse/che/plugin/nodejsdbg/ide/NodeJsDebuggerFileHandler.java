/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.nodejsdbg.ide;

import com.google.common.base.Optional;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

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
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.plugin.debugger.ide.debug.ActiveFileHandler;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
public class NodeJsDebuggerFileHandler implements ActiveFileHandler {

    private final EditorAgent editorAgent;
    private final EventBus    eventBus;
    private final AppContext  appContext;

    @Inject
    public NodeJsDebuggerFileHandler(EditorAgent editorAgent,
                                     EventBus eventBus,
                                     AppContext appContext) {
        this.editorAgent = editorAgent;
        this.eventBus = eventBus;
        this.appContext = appContext;
    }

    @Override
    public void openFile(final Location location, final AsyncCallback<VirtualFile> callback) {
        final Resource resource = appContext.getResource();
        if (resource == null) {
            callback.onFailure(new IllegalStateException("Resource is undefined"));
            return;
        }
        final Optional<Project> project = resource.getRelatedProject();
        if (!project.isPresent()) {
            callback.onFailure(new IllegalStateException("Project is undefined"));
            return;
        }

        findFileInProject(project.get(), location, callback);
    }

    private void findFileInProject(final Project project,
                                   final Location location,
                                   final AsyncCallback<VirtualFile> callback) {
        project.getFile(location.getTarget()).then(new Operation<Optional<File>>() {
            @Override
            public void apply(Optional<File> file) throws OperationException {
                if (file.isPresent()) {
                    openFileInEditorAndScrollToLine(file.get(), callback, location.getLineNumber());
                    callback.onSuccess(file.get());
                } else {
                    findFileInWorkspace(location, callback);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(new IllegalArgumentException("File not found." + arg.getMessage()));
            }
        });
    }

    private void findFileInWorkspace(final Location location,
                                     final AsyncCallback<VirtualFile> callback) {
        try {
            appContext.getWorkspaceRoot().getFile(location.getTarget()).then(new Operation<Optional<File>>() {
                @Override
                public void apply(Optional<File> file) throws OperationException {
                    if (file.isPresent()) {
                        openFileInEditorAndScrollToLine(file.get(), callback, location.getLineNumber());
                        callback.onSuccess(file.get());
                    } else {
                        searchSource(location, callback);
                    }
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    callback.onFailure(new IllegalArgumentException("File not found." + arg.getMessage()));
                }
            });
        } catch (IllegalStateException ignored) {
            searchSource(location, callback);
        }
    }

    private void searchSource(final Location location, final AsyncCallback<VirtualFile> callback) {
        appContext.getWorkspaceRoot().search(location.getTarget(), "").then(new Operation<Resource[]>() {
            @Override
            public void apply(Resource[] resources) throws OperationException {
                if (resources.length == 0) {
                    callback.onFailure(new IllegalArgumentException("File not found."));
                    return;
                }

                appContext.getWorkspaceRoot().getFile(resources[0].getLocation()).then(new Operation<Optional<File>>() {
                    @Override
                    public void apply(Optional<File> file) throws OperationException {
                        if (file.isPresent()) {
                            openFileInEditorAndScrollToLine(file.get(), callback, location.getLineNumber());
                            callback.onSuccess(file.get());
                        } else {
                            callback.onFailure(new IllegalArgumentException("File not found."));
                        }
                    }
                }).catchError(new Operation<PromiseError>() {
                    @Override
                    public void apply(PromiseError arg) throws OperationException {
                        callback.onFailure(new IllegalArgumentException("File not found. " + arg.getMessage()));
                    }
                });
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(new IllegalArgumentException("File not found. " + arg.getMessage()));
            }
        });
    }

    private void openFileInEditorAndScrollToLine(final VirtualFile virtualFile,
                                                 final AsyncCallback<VirtualFile> callback,
                                                 final int scrollToLine) {
        editorAgent.openEditor(virtualFile, new EditorAgent.OpenEditorCallback() {
            @Override
            public void onEditorOpened(EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        scrollEditorToExecutionPoint((TextEditor)editorAgent.getActiveEditor(), scrollToLine);
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onEditorActivated(EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        scrollEditorToExecutionPoint((TextEditor)editorAgent.getActiveEditor(), scrollToLine);
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onInitializationFailed() {
                callback.onFailure(null);
            }
        });

        eventBus.fireEvent(FileEvent.createOpenFileEvent(virtualFile));
    }

    private void scrollEditorToExecutionPoint(TextEditor editor, int lineNumber) {
        Document document = editor.getDocument();
        if (document != null) {
            TextPosition newPosition = new TextPosition(lineNumber, 0);
            document.setCursorPosition(newPosition);
        }
    }
}
