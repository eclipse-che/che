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
package org.eclipse.che.ide.ext.java.client.navigation.filestructure;

import com.google.gwt.core.client.Scheduler;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
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
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Member;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;
import org.eclipse.che.ide.util.loging.Log;

/**
 * The class that manages class structure window.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class FileStructurePresenter implements FileStructure.ActionDelegate {
    private final FileStructure            view;
    private final JavaNavigationService    javaNavigationService;
    private final AppContext               context;
    private final EditorAgent              editorAgent;
    private final MessageLoader            loader;
    private final ProjectExplorerPresenter projectExplorer;
    private final JavaNodeManager          javaNodeManager;

    private TextEditorPresenter activeEditor;
    private boolean             showInheritedMembers;
    private int                 cursorOffset;

    @Inject
    public FileStructurePresenter(FileStructure view,
                                  JavaNavigationService javaNavigationService,
                                  AppContext context,
                                  EditorAgent editorAgent,
                                  LoaderFactory loaderFactory,
                                  ProjectExplorerPresenter projectExplorer,
                                  JavaNodeManager javaNodeManager) {
        this.view = view;
        this.javaNavigationService = javaNavigationService;
        this.context = context;
        this.editorAgent = editorAgent;
        this.loader = loaderFactory.newLoader();
        this.projectExplorer = projectExplorer;
        this.javaNodeManager = javaNodeManager;
        this.view.setDelegate(this);
    }

    /**
     * Shows the structure of the opened class.
     *
     * @param editorPartPresenter
     *         the active editor
     */
    public void show(EditorPartPresenter editorPartPresenter) {
        loader.show();
        view.setTitle(editorPartPresenter.getEditorInput().getFile().getName());

        if (!(editorPartPresenter instanceof TextEditorPresenter)) {
            Log.error(getClass(), "Open Declaration support only TextEditorPresenter as editor");
            return;
        }
        activeEditor = ((TextEditorPresenter)editorPartPresenter);
        cursorOffset = activeEditor.getCursorOffset();
        VirtualFile file = activeEditor.getEditorInput().getFile();

        String projectPath = file.getProject().getProjectConfig().getPath();
        String fqn = JavaSourceFolderUtil.getFQNForFile(file);

        Promise<CompilationUnit> promise = javaNavigationService.getCompilationUnit(projectPath, fqn, showInheritedMembers);
        promise.then(new Operation<CompilationUnit>() {
            @Override
            public void apply(CompilationUnit arg) throws OperationException {
                view.setStructure(arg, showInheritedMembers);
                showInheritedMembers = !showInheritedMembers;
                loader.hide();
                view.show();
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(FileStructurePresenter.class, arg.getMessage());
                loader.hide();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final Member member) {
        if (member.isBinary()) {
            javaNodeManager.getClassNode(context.getCurrentProject().getProjectConfig(), member.getLibId(), member.getRootPath())
                           .then(new Operation<Node>() {
                               @Override
                               public void apply(Node node) throws OperationException {
                                   if (node instanceof VirtualFile) {
                                       openFile((VirtualFile)node, member);
                                   }
                               }
                           });
        } else {
            projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(member.getRootPath()))
                           .then(selectNode())
                           .then(openNode(member));
        }
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                setCursorPosition(member.getFileRegion());
            }
        });
        showInheritedMembers = false;
    }

    @Override
    public void onEscapeClicked() {
        activeEditor.setFocus();
        setCursor(activeEditor, cursorOffset);
    }

    private void openFile(VirtualFile result, final Member member) {
        editorAgent.openEditor(result, new OpenEditorCallbackImpl() {
            @Override
            public void onEditorOpened(EditorPartPresenter editor) {
                setCursor(editor, member.getFileRegion().getOffset());
            }
        });
    }

    private Function<Node, Node> selectNode() {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                projectExplorer.select(node, false);

                return node;
            }
        };
    }

    private Function<Node, Node> openNode(final Member member) {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                if (node instanceof FileReferenceNode) {
                    openFile((VirtualFile)node, member);
                }

                return node;
            }
        };
    }

    private void setCursorPosition(Region region) {
        LinearRange linearRange = LinearRange.createWithStart(region.getOffset()).andLength(region.getLength());
        activeEditor.setFocus();
        activeEditor.getDocument().setSelectedRange(linearRange, true);
    }

    private void setCursor(EditorPartPresenter editor, int offset) {
        if (editor instanceof TextEditorPresenter) {
            ((TextEditorPresenter)editor).getDocument().setSelectedRange(LinearRange.createWithStart(offset).andLength(0), true);
        }
    }
}