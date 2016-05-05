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
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.settings.NodeSettings;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.debugger.client.debug.ActiveFileHandler;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarFileNode;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.jseditor.client.document.Document;
import org.eclipse.che.ide.jseditor.client.text.TextPosition;
import org.eclipse.che.ide.jseditor.client.texteditor.TextEditor;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;

import javax.validation.constraints.NotNull;

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
    private final EventBus                 eventBus;
    private final JavaNodeManager          javaNodeManager;
    private final ProjectExplorerPresenter projectExplorer;
    private final JavaNavigationService    javaNavigationService;
    private final AppContext               appContext;

    @Inject
    public JavaDebuggerFileHandler(DebuggerManager debuggerManager,
                                   EditorAgent editorAgent,
                                   DtoFactory dtoFactory,
                                   EventBus eventBus,
                                   JavaNodeManager javaNodeManager,
                                   ProjectExplorerPresenter projectExplorer,
                                   JavaNavigationService javaNavigationService,
                                   AppContext appContext) {
        this.debuggerManager = debuggerManager;
        this.editorAgent = editorAgent;
        this.dtoFactory = dtoFactory;
        this.eventBus = eventBus;
        this.javaNodeManager = javaNodeManager;
        this.projectExplorer = projectExplorer;
        this.javaNavigationService = javaNavigationService;
        this.appContext = appContext;
    }

    public void openFile(final String className, final int lineNumber, final AsyncCallback<VirtualFile> callback) {
        if (debuggerManager.getActiveDebugger() != debuggerManager.getDebugger(JavaDebugger.ID)) {
            callback.onFailure(null);
            return;
        }
        final ProjectConfigDto projectConfig = appContext.getCurrentProject().getProjectConfig();
        VirtualFile activeFile = null;
        final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor != null) {
            activeFile = activeEditor.getEditorInput().getFile();
        }

        final String fqn = prepareFQN(className);
        if (activeFile == null || !className.equals(activeFile.getPath())) {
            javaNavigationService.findDeclaration(projectConfig.getPath(), fqn)
                                 .then(new Operation<OpenDeclarationDescriptor>() {
                                     @Override
                                     public void apply(OpenDeclarationDescriptor declaration) throws OperationException {
                                         if (declaration.isBinary()) {
                                             openExternalResource(fqn, lineNumber, projectConfig, callback, declaration.getLibId());
                                         } else {
                                             openSourceFile(declaration.getPath(), lineNumber,  callback);
                                         }
                                     }
                                 })
                                 .catchError(new Operation<PromiseError>() {
                                     @Override
                                     public void apply(PromiseError arg) throws OperationException {
                                         openExternalResource(fqn, lineNumber, projectConfig, callback, null);
                                     }
                                 });
        } else {
            scrollEditorToExecutionPoint((TextEditor)activeEditor, lineNumber);
            callback.onSuccess(activeFile);
        }
    }

    private String prepareFQN(String fqn) {
        //handle fqn in case nested classes
        return fqn.contains("$") ? fqn.substring(0, fqn.indexOf("$")) : fqn;
    }

    /**
     * Tries to open file from the project.
     */
    private void openSourceFile(@NotNull String filePath, final int debugLine, final AsyncCallback<VirtualFile> callback) {
        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(filePath)).then(new Operation<Node>() {
            @Override
            public void apply(final Node node) throws OperationException {
                if (!(node instanceof FileReferenceNode)) {
                    return;
                }
                handleActivateFile((VirtualFile)node, callback, debugLine);
                eventBus.fireEvent(new FileEvent((VirtualFile)node, OPEN));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                callback.onFailure(arg.getCause());
            }
        });
    }

    private void openExternalResource(final String fqn,
                                      final int debugLine,
                                      final ProjectConfigDto project,
                                      final AsyncCallback<VirtualFile> callback,
                                      Integer libId) {
        NodeSettings nodeSettings = javaNodeManager.getJavaSettingsProvider().getSettings();

        JarEntry jarEntry = dtoFactory.createDto(JarEntry.class);
        jarEntry.setPath(fqn);
        jarEntry.setName(fqn.substring(fqn.lastIndexOf(".") + 1) + ".class");
        jarEntry.setType(JarEntry.JarEntryType.CLASS_FILE);

        final JarFileNode jarFileNode = javaNodeManager.getJavaNodeFactory().newJarFileNode(jarEntry, libId, project, nodeSettings);

        handleActivateFile(jarFileNode, callback, debugLine);
        eventBus.fireEvent(new FileEvent(jarFileNode, OPEN));
    }

    public void handleActivateFile(final VirtualFile virtualFile, final AsyncCallback<VirtualFile> callback, final int debugLine) {
        editorAgent.openEditor(virtualFile, new EditorAgent.OpenEditorCallback() {
            @Override
            public void onEditorOpened(final EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        scrollEditorToExecutionPoint((TextEditor)editor, debugLine);
                        callback.onSuccess(virtualFile);
                    }
                }.schedule(300);
            }

            @Override
            public void onEditorActivated(final EditorPartPresenter editor) {
                new Timer() {
                    @Override
                    public void run() {
                        scrollEditorToExecutionPoint((TextEditor)editor, debugLine);
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
