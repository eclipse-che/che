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
package org.eclipse.che.plugin.jdb.ide.debug;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.event.EditorDirtyStateChangedEvent;
import org.eclipse.che.ide.api.event.EditorDirtyStateChangedHandler;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.data.tree.Node;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeFactory;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.project.node.jar.JarFileNode;
import org.eclipse.che.ide.ext.java.shared.JarEntry;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.plugin.debugger.ide.debug.ActiveFileHandler;

import static org.eclipse.che.ide.api.event.FileEvent.FileOperation.OPEN;
import static org.eclipse.che.ide.ext.java.shared.JarEntry.JarEntryType.CLASS_FILE;

/**
 * Responsible to open files in editor when debugger stopped at breakpoint.
 *
 * @author Anatoliy Bazko
 */
public class JavaDebuggerFileHandler implements ActiveFileHandler {

    private final EditorAgent              editorAgent;
    private final DtoFactory               dtoFactory;
    private final AppContext               appContext;
    private final EventBus                 eventBus;
    private final JavaNodeManager          javaNodeManager;
    private final ProjectExplorerPresenter projectExplorer;
    private final ProjectServiceClient     projectService;

    private HandlerRegistration handler;

    @Inject
    public JavaDebuggerFileHandler(EditorAgent editorAgent,
                                   DtoFactory dtoFactory,
                                   AppContext appContext,
                                   EventBus eventBus,
                                   JavaNodeManager javaNodeManager,
                                   ProjectExplorerPresenter projectExplorer,
                                   ProjectServiceClient projectService) {
        this.editorAgent = editorAgent;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.javaNodeManager = javaNodeManager;
        this.projectExplorer = projectExplorer;
        this.projectService = projectService;
    }

    @Override
    public void openFile(Location location, AsyncCallback<VirtualFile> callback) {
        VirtualFile activeFile = null;
        String activePath = null;
        final EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor != null) {
            activeFile = editorAgent.getActiveEditor().getEditorInput().getFile();
            activePath = activeFile.getPath();
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
        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(location.getResourcePath())).then(new Operation<Node>() {
            @Override
            public void apply(final Node node) throws OperationException {
                if (!(node instanceof FileReferenceNode)) {
                    return;
                }

                handleActivatedFile((VirtualFile)node, callback, location.getLineNumber());
                eventBus.fireEvent(new FileEvent((VirtualFile)node, OPEN));
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError error) throws OperationException {
                callback.onFailure(error.getCause());
            }
        });
    }

    private void openExternalResource(final Location location, final AsyncCallback<VirtualFile> callback) {
        final NodeSettings nodeSettings = javaNodeManager.getJavaSettingsProvider().getSettings();
        final JavaNodeFactory javaNodeFactory = javaNodeManager.getJavaNodeFactory();

        String className = extractOuterClassFqn(location.getTarget());
        final JarEntry jarEntry = dtoFactory.createDto(JarEntry.class);
        jarEntry.setPath(className);
        jarEntry.setName(className.substring(className.lastIndexOf(".") + 1) + ".class");
        jarEntry.setType(CLASS_FILE);

        final String projectPath = location.getResourceProjectPath();
        projectService.getProject(appContext.getDevMachine(), projectPath)
                      .then(new Function<ProjectConfigDto, JarFileNode>() {
                          @Override
                          public JarFileNode apply(ProjectConfigDto projectConfigDto) throws FunctionException {
                              return javaNodeFactory.newJarFileNode(jarEntry, null, projectConfigDto, nodeSettings);
                          }
                      })
                      .then(new Operation<JarFileNode>() {
                          @Override
                          public void apply(final JarFileNode jarFileNode) throws OperationException {
                              AsyncCallback<VirtualFile> downloadSourceCallback = new AsyncCallback<VirtualFile>() {
                                  @Override
                                  public void onSuccess(final VirtualFile result) {
                                      if (jarFileNode.isContentGenerated()) {
                                          handleContentGeneratedResource(result, location, callback);
                                      } else {
                                          handleActivatedFile(jarFileNode, callback, location.getLineNumber());
                                      }
                                  }

                                  @Override
                                  public void onFailure(Throwable caught) {
                                      callback.onFailure(caught);
                                  }
                              };
                              handleActivatedFile(jarFileNode, downloadSourceCallback, location.getLineNumber());
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
