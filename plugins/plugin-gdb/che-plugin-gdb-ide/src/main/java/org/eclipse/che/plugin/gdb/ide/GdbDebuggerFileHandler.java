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
package org.eclipse.che.plugin.gdb.ide;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.plugin.debugger.ide.debug.ActiveFileHandler;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
public class GdbDebuggerFileHandler implements ActiveFileHandler {

    private final EditorAgent              editorAgent;
    private final EventBus                 eventBus;
    private final ProjectExplorerPresenter projectExplorer;
    private final AppContext               appContext;

    @Inject
    public GdbDebuggerFileHandler(EditorAgent editorAgent,
                                  ProjectExplorerPresenter projectExplorer,
                                  EventBus eventBus,
                                  AppContext appContext) {
        this.editorAgent = editorAgent;
        this.eventBus = eventBus;
        this.projectExplorer = projectExplorer;
        this.appContext = appContext;
    }

    @Override
    public void openFile(final Location location, final AsyncCallback<VirtualFile> callback) {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            return;
        }
        String filePath = currentProject.getProjectConfig().getPath() + "/" + location.getTarget();
        VirtualFile activeFile = null;
        final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor != null) {
            activeFile = activeEditor.getEditorInput().getFile();
        }

        if (activeEditor == null || !activeFile.getPath().equals(filePath)) {
            doOpenFile(filePath, location.getLineNumber(), callback);
        } else {
            scrollEditorToExecutionPoint((TextEditor)activeEditor, location.getLineNumber());
            callback.onSuccess(activeEditor.getEditorInput().getFile());
        }
    }

    private void doOpenFile(final String filePath, final int lineNumber, final AsyncCallback<VirtualFile> callback) {
        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(filePath)).then(new Operation<Node>() {
            @Override
            public void apply(final Node node) throws OperationException {
                if (!(node instanceof FileReferenceNode)) {
                    return;
                }
                handleActivatedFile((VirtualFile)node, callback, lineNumber);
                eventBus.fireEvent(new FileEvent((VirtualFile)node, OPEN));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                callback.onFailure(error.getCause());
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
