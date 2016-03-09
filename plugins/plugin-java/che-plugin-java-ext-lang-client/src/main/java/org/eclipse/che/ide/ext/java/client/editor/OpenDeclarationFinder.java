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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.OpenEditorCallbackImpl;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.ide.jseditor.client.text.LinearRange;
import org.eclipse.che.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class OpenDeclarationFinder {

    private final EditorAgent              editorAgent;
    private final JavaNavigationService    navigationService;
    private       DtoUnmarshallerFactory   factory;
    private       AppContext               context;
    private final ProjectExplorerPresenter projectExplorer;
    private final JavaNodeManager          javaNodeManager;

    @Inject
    public OpenDeclarationFinder(EditorAgent editorAgent,
                                 JavaNavigationService navigationService,
                                 DtoUnmarshallerFactory factory,
                                 AppContext context,
                                 ProjectExplorerPresenter projectExplorer,
                                 JavaNodeManager javaNodeManager) {
        this.editorAgent = editorAgent;
        this.factory = factory;
        this.navigationService = navigationService;
        this.context = context;
        this.projectExplorer = projectExplorer;
        this.javaNodeManager = javaNodeManager;
    }

    public void openDeclaration() {
        EditorPartPresenter activeEditor = editorAgent.getActiveEditor();
        if (activeEditor == null) {
            return;
        }

        if (!(activeEditor instanceof EmbeddedTextEditorPresenter)) {
            Log.error(getClass(), "Open Declaration support only EmbeddedTextEditorPresenter as editor");
            return;
        }
        EmbeddedTextEditorPresenter editor = ((EmbeddedTextEditorPresenter)activeEditor);
        int offset = editor.getCursorOffset();
        final VirtualFile file = editor.getEditorInput().getFile();
        Unmarshallable<OpenDeclarationDescriptor> unmarshaller =
                factory.newUnmarshaller(OpenDeclarationDescriptor.class);
        navigationService
                .findDeclaration(file.getProject().getProjectConfig().getPath(), JavaSourceFolderUtil.getFQNForFile(file), offset,
                                 new AsyncRequestCallback<OpenDeclarationDescriptor>(unmarshaller) {
                                     @Override
                                     protected void onSuccess(OpenDeclarationDescriptor result) {
                                         if (result != null) {
                                             handleDescriptor(result);
                                         }
                                     }

                                     @Override
                                     protected void onFailure(Throwable exception) {
                                         Log.error(OpenDeclarationFinder.class, exception);
                                     }
                                 });
    }

    private void handleDescriptor(final OpenDeclarationDescriptor descriptor) {
        Map<String, EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
        for (String s : openedEditors.keySet()) {
            if (descriptor.getPath().equals(s)) {
                EditorPartPresenter editorPartPresenter = openedEditors.get(s);
                editorAgent.activateEditor(editorPartPresenter);
                fileOpened(editorPartPresenter, descriptor.getOffset());
                return;
            }
        }


        if (descriptor.isBinary()) {
            javaNodeManager.getClassNode(context.getCurrentProject().getProjectConfig(), descriptor.getLibId(), descriptor.getPath())
                           .then(new Operation<Node>() {
                               @Override
                               public void apply(Node node) throws OperationException {
                                   if (node instanceof VirtualFile) {
                                       openFile((VirtualFile)node, descriptor);
                                   }
                               }
                           });
        } else {
            projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(descriptor.getPath()))
                           .then(selectNode())
                           .then(openNode(descriptor));
        }
    }

    protected Function<Node, Node> selectNode() {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                projectExplorer.select(node, false);

                return node;
            }
        };
    }

    protected Function<Node, Node> openNode(final OpenDeclarationDescriptor descriptor) {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                if (node instanceof FileReferenceNode) {
                    openFile((VirtualFile)node, descriptor);
                }

                return node;
            }
        };
    }

    private void openFile(VirtualFile result, final OpenDeclarationDescriptor descriptor) {
        final Map<String, EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();
        Log.info(getClass(), result.getPath());
        if (openedEditors.containsKey(result.getPath())) {
            EditorPartPresenter editorPartPresenter = openedEditors.get(result.getPath());
            editorAgent.activateEditor(editorPartPresenter);
            fileOpened(editorPartPresenter, descriptor.getOffset());
        } else {
            editorAgent.openEditor(result, new OpenEditorCallbackImpl() {
                @Override
                public void onEditorOpened(EditorPartPresenter editor) {
                    fileOpened(editor, descriptor.getOffset());
                }
            });
        }
    }

    private void fileOpened(final EditorPartPresenter editor, final int offset) {
        new Timer() { //in some reason we need here timeout otherwise it not work cursor don't set to correct position
            @Override
            public void run() {
                if (editor instanceof EmbeddedTextEditorPresenter) {
                    ((EmbeddedTextEditorPresenter)editor).getDocument().setSelectedRange(
                            LinearRange.createWithStart(offset).andLength(0), true);
                }
            }
        }.schedule(100);
    }
}
