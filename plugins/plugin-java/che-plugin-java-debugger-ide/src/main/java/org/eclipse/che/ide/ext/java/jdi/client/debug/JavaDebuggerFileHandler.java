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
package org.eclipse.che.ide.ext.java.jdi.client.debug;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.debugger.client.debug.ActiveFileHandler;
import org.eclipse.che.ide.ext.debugger.client.debug.DebuggerPresenter;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarFileNode;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
public class JavaDebuggerFileHandler implements ActiveFileHandler {

    private final DebuggerManager          debuggerManager;
    private final EditorAgent              editorAgent;
    private final DtoFactory               dtoFactory;
    private final AppContext               appContext;
    private final EventBus                 eventBus;
    private final JavaNodeManager          javaNodeManager;
    private final ProjectExplorerPresenter projectExplorer;

    @Inject
    public JavaDebuggerFileHandler(DebuggerManager debuggerManager,
                                   EditorAgent editorAgent,
                                   DtoFactory dtoFactory,
                                   AppContext appContext,
                                   EventBus eventBus,
                                   JavaNodeManager javaNodeManager,
                                   ProjectExplorerPresenter projectExplorer) {
        this.debuggerManager = debuggerManager;
        this.editorAgent = editorAgent;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.javaNodeManager = javaNodeManager;
        this.projectExplorer = projectExplorer;
    }

    @Override
    public void openFile(final List<String> filePaths,
                         final String className,
                         final int lineNumber,
                         final AsyncCallback<VirtualFile> callback) {
        if (debuggerManager.getActiveDebugger() != debuggerManager.getDebugger(JavaDebugger.ID)) {
            callback.onFailure(null);
            return;
        }

        VirtualFile activeFile = null;
        final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor != null) {
            activeFile = activeEditor.getEditorInput().getFile();
        }

        if (activeFile == null || !filePaths.contains(activeFile.getPath())) {
            openFile(className, filePaths, 0, new AsyncCallback<VirtualFile>() {
                @Override
                public void onSuccess(VirtualFile result) {
                    scrollEditorToExecutionPoint((TextEditorPresenter)editorAgent.getActiveEditor(), lineNumber);
                    callback.onSuccess(result);
                }

                @Override
                public void onFailure(Throwable caught) {
                    callback.onFailure(caught);
                }
            });
        } else {
            scrollEditorToExecutionPoint((TextEditorPresenter)activeEditor, lineNumber);
            callback.onSuccess(activeFile);
        }
    }

    /**
     * Tries to open file from the project.
     * If fails then method will try to find resource from external dependencies.
     */
    private void openFile(@NotNull final String className,
                          final List<String> filePaths,
                          final int pathNumber,
                          final AsyncCallback<VirtualFile> callback) {
        if (pathNumber == filePaths.size()) {
            Log.error(DebuggerPresenter.class, "Can't open resource " + className);
            return;
        }

        String filePath = filePaths.get(pathNumber);

        if (!filePath.startsWith("/")) {
            openExternalResource(className, callback);
            return;
        }

        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(filePath)).then(new Operation<Node>() {
            @Override
            public void apply(final Node node) throws OperationException {
                if (!(node instanceof FileReferenceNode)) {
                    return;
                }

                handleActivateFile((VirtualFile)node, callback);
                eventBus.fireEvent(new FileEvent((VirtualFile)node, OPEN));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                // try another path
                openFile(className, filePaths, pathNumber + 1, callback);
            }
        });
    }

    private void openExternalResource(String className, final AsyncCallback<VirtualFile> callback) {
        JarEntry jarEntry = dtoFactory.createDto(JarEntry.class);
        jarEntry.setPath(className);
        jarEntry.setName(className.substring(className.lastIndexOf(".") + 1) + ".class");
        jarEntry.setType(JarEntry.JarEntryType.CLASS_FILE);

        final JarFileNode jarFileNode = javaNodeManager.getJavaNodeFactory()
                                                       .newJarFileNode(jarEntry,
                                                                       null,
                                                                       appContext.getCurrentProject().getProjectConfig(),
                                                                       javaNodeManager.getJavaSettingsProvider().getSettings());

        handleActivateFile(jarFileNode, callback);
        eventBus.fireEvent(new FileEvent(jarFileNode, OPEN));
    }

    public void handleActivateFile(final VirtualFile virtualFile, final AsyncCallback<VirtualFile> callback) {
        editorAgent.openEditor(virtualFile, new EditorAgent.OpenEditorCallback() {
            @Override
            public void onEditorOpened(EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onEditorActivated(EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
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

    private void scrollEditorToExecutionPoint(TextEditorPresenter editor, int lineNumber) {
        Document document = editor.getDocument();

        if (document != null) {
            TextPosition newPosition = new TextPosition(lineNumber, 0);
            document.setCursorPosition(newPosition);
        }
    }
}
