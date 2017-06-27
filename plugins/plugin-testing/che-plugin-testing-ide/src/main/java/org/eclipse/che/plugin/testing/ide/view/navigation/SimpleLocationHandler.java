/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.testing.ide.view.navigation;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.testing.shared.dto.SimpleLocationDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.VirtualFile;

import com.google.common.base.Optional;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 * Simple location DTO handler. It is responsible for opening the provided location.
 * 
 * @author Bartlomiej Laczkowski
 */
public class SimpleLocationHandler {

    private static final String PROJECTS_ROOT = "/projects";

    private final EditorAgent   editorAgent;
    private final AppContext    appContext;

    @Inject
    public SimpleLocationHandler(EditorAgent editorAgent, AppContext appContext) {
        this.editorAgent = editorAgent;
        this.appContext = appContext;
    }

    /**
     * Tries to open provided location.
     * 
     * @param location
     */
    public void openFile(final SimpleLocationDto location) {
        openFile(location, null);
    }

    /**
     * Tries to open provided location. Supports callback if there is a need.
     * 
     * @param location
     * @param callback
     */
    public void openFile(final SimpleLocationDto location, final AsyncCallback<VirtualFile> callback) {
        tryFindFileInWorkspace(location, new AsyncCallback<VirtualFile>() {
            @Override
            public void onSuccess(VirtualFile virtualFile) {
                if (callback != null)
                    callback.onSuccess(virtualFile);
            }

            @Override
            public void onFailure(Throwable caught) {
                if (callback != null)
                    callback.onFailure(caught);
            }
        });
    }

    private void tryFindFileInWorkspace(final SimpleLocationDto location, final AsyncCallback<VirtualFile> callback) {
        if (location == null) {
            return;
        }
        String resourcePath = location.getResourcePath();
        if (resourcePath == null || resourcePath.isEmpty()) {
            return;
        }
        if (resourcePath.startsWith(PROJECTS_ROOT))
            resourcePath = resourcePath.substring(PROJECTS_ROOT.length() + 1);
        try {
            appContext.getWorkspaceRoot().getFile(resourcePath).then(new Operation<Optional<File>>() {
                @Override
                public void apply(Optional<File> file) throws OperationException {
                    if (file.isPresent()) {
                        openFileAndScrollToLine(file.get(), location.getLineNumber(), callback);
                    } else {
                        callback.onFailure(new IllegalArgumentException(location.getResourcePath() + " not found."));
                    }
                }
            }).catchError(new Operation<PromiseError>() {
                @Override
                public void apply(PromiseError arg) throws OperationException {
                    callback.onFailure(new IllegalArgumentException(location.getResourcePath() + " not found."));
                }
            });
        } catch (IllegalStateException ignored) {
            callback.onFailure(new IllegalArgumentException(location.getResourcePath() + " not found."));
        }
    }

    private void openFileAndScrollToLine(final VirtualFile virtualFile,
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
                callback.onFailure(new IllegalStateException("Initialization " + virtualFile.getName()
                                                             + " in the editor failed"));
            }
        });
    }

    private void scrollToLine(EditorPartPresenter editor, int lineNumber) {
        if (editor instanceof TextEditor) {
            TextEditor textEditor = (TextEditor) editor;
            Document document = textEditor.getDocument();
            if (document != null) {
                TextPosition newPosition = new TextPosition(lineNumber, 0);
                document.setCursorPosition(newPosition);
            }
        }
    }
}
