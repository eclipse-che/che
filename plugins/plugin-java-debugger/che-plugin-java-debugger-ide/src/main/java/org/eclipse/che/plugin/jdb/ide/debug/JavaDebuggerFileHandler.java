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
package org.eclipse.che.plugin.jdb.ide.debug;

import com.google.common.base.Optional;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

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
import org.eclipse.che.ide.api.event.EditorDirtyStateChangedEvent;
import org.eclipse.che.ide.api.event.EditorDirtyStateChangedHandler;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.tree.JavaNodeFactory;
import org.eclipse.che.ide.ext.java.client.tree.library.JarFileNode;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.plugin.debugger.ide.debug.ActiveFileHandler;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
public class JavaDebuggerFileHandler implements ActiveFileHandler {

    private final EditorAgent           editorAgent;
    private final EventBus              eventBus;
    private final JavaNavigationService service;
    private final AppContext            appContext;
    private final JavaNodeFactory       nodeFactory;

    private HandlerRegistration handler;

    @Inject
    public JavaDebuggerFileHandler(EditorAgent editorAgent,
                                   AppContext appContext,
                                   EventBus eventBus,
                                   JavaNavigationService service,
                                   JavaNodeFactory nodeFactory) {
        this.editorAgent = editorAgent;
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.service = service;
        this.nodeFactory = nodeFactory;
    }

    @Override
    public void openFile(Location location, AsyncCallback<VirtualFile> callback) {
        VirtualFile activeFile = null;
        String activePath = null;
        final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor != null) {
            activeFile = editorAgent.getActiveEditor().getEditorInput().getFile();
            activePath = activeFile.getLocation().toString();
        }
        if (activePath != null && !activePath.equals(location.getTarget()) && !activePath.equals(location.getResourcePath())) {
            if (location.isExternalResource()) {
                openExternalResource(location, callback);
            } else {
                doOpenFile(location, callback);
            }
        } else {
            scrollEditorToExecutionPoint((TextEditor)activeEditor, location.getLineNumber());
            callback.onSuccess(activeFile);
        }
    }

    private void doOpenFile(final Location location, final AsyncCallback<VirtualFile> callback) {
        appContext.getWorkspaceRoot().getFile(location.getResourcePath()).then(new Operation<Optional<File>>() {
            @Override
            public void apply(Optional<File> file) throws OperationException {
                if (file.isPresent()) {
                    handleActivatedFile(file.get(), callback, location.getLineNumber());
                    eventBus.fireEvent(FileEvent.createOpenFileEvent(file.get()));
                } else {
                    callback.onFailure(new IllegalStateException("File is undefined"));
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                callback.onFailure(error.getCause());
            }
        });
    }

    private void openExternalResource(final Location location, final AsyncCallback<VirtualFile> callback) {
        final String className = extractOuterClassFqn(location.getTarget());
        final int libId = location.getExternalResourceId();
        final Path projectPath = new Path(location.getResourceProjectPath());

        service.getEntry(projectPath, libId, className)
               .then(new Operation<JarEntry>() {
                   @Override
                   public void apply(final JarEntry jarEntry) throws OperationException {
                       final JarFileNode file = nodeFactory.newJarFileNode(jarEntry, libId, projectPath, null);
                       AsyncCallback<VirtualFile> downloadSourceCallback = new AsyncCallback<VirtualFile>() {
                           @Override
                           public void onSuccess(final VirtualFile result) {
                               if (file.isContentGenerated()) {
                                   handleContentGeneratedResource(file, location, callback);
                               } else {
                                   handleActivatedFile(file, callback, location.getLineNumber());
                               }
                           }

                           @Override
                           public void onFailure(Throwable caught) {
                               callback.onFailure(caught);
                           }
                       };

                       handleActivatedFile(file, downloadSourceCallback, location.getLineNumber());
                       eventBus.fireEvent(FileEvent.createOpenFileEvent(file));
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       callback.onFailure(arg.getCause());
                   }
               });
    }

    private String extractOuterClassFqn(String fqn) {
        //handle fqn in case nested classes
        if (fqn.contains("$")) {
            return fqn.substring(0, fqn.indexOf("$"));
        }
        //handle fqn in case lambda expressions
        if (fqn.contains("$$")) {
            return fqn.substring(0, fqn.indexOf("$$"));
        }
        return fqn;
    }

    private void handleContentGeneratedResource(final VirtualFile file,
                                                final Location location,
                                                final AsyncCallback<VirtualFile> callback) {
        if (handler != null) {
            handler.removeHandler();
        }
        handler = eventBus.addHandler(EditorDirtyStateChangedEvent.TYPE, new EditorDirtyStateChangedHandler() {
            @Override
            public void onEditorDirtyStateChanged(EditorDirtyStateChangedEvent event) {
                if (file.equals(event.getEditor().getEditorInput().getFile())) {
                    handleActivatedFile(file, callback, location.getLineNumber());
                    handler.removeHandler();
                }
            }
        });
    }

    public void handleActivatedFile(final VirtualFile virtualFile, final AsyncCallback<VirtualFile> callback, final int debugLine) {
        editorAgent.openEditor(virtualFile, new EditorAgent.OpenEditorCallback() {
            @Override
            public void onEditorOpened(EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        scrollEditorToExecutionPoint((TextEditor)editorAgent.getActiveEditor(), debugLine);
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onEditorActivated(EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        scrollEditorToExecutionPoint((TextEditor)editorAgent.getActiveEditor(), debugLine);
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onInitializationFailed() {
                callback.onFailure(null);
            }
        });
    }

    private void scrollEditorToExecutionPoint(TextEditor editor, int lineNumber) {
        Document document = editor.getDocument();

        if (document != null) {
            TextPosition newPosition = new TextPosition(lineNumber, 0);
            document.setCursorPosition(newPosition);
        }
    }
}
