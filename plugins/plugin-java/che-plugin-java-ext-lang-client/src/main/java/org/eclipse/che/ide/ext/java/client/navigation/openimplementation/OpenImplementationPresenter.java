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
package org.eclipse.che.ide.ext.java.client.navigation.openimplementation;

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
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.navigation.service.JavaNavigationService;
import org.eclipse.che.ide.ext.java.client.project.node.JavaNodeManager;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.ext.java.shared.dto.ImplementationsDescriptorDTO;
import org.eclipse.che.ide.ext.java.shared.dto.Region;
import org.eclipse.che.ide.ext.java.shared.dto.model.Member;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.api.editor.position.PositionConverter;
import org.eclipse.che.ide.api.editor.text.LinearRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.util.loging.Log;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * The class that manages implementations structure window.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class OpenImplementationPresenter {
    private final JavaNavigationService    service;
    private final AppContext               context;
    private final EditorAgent              editorAgent;
    private final ProjectExplorerPresenter projectExplorer;
    private final JavaNodeManager          javaNodeManager;
    private final DtoFactory               dtoFactory;
    private final JavaResources            javaResources;
    private final PopupResources           popupResources;
    private final JavaLocalizationConstant locale;

    private TextEditorPresenter activeEditor;

    @Inject
    public OpenImplementationPresenter(JavaNavigationService javaNavigationService,
                                       AppContext context,
                                       DtoFactory dtoFactory,
                                       JavaResources javaResources,
                                       PopupResources popupResources,
                                       JavaLocalizationConstant locale,
                                       EditorAgent editorAgent,
                                       ProjectExplorerPresenter projectExplorer,
                                       JavaNodeManager javaNodeManager) {
        this.service = javaNavigationService;
        this.context = context;
        this.dtoFactory = dtoFactory;
        this.javaResources = javaResources;
        this.popupResources = popupResources;
        this.locale = locale;
        this.editorAgent = editorAgent;
        this.projectExplorer = projectExplorer;
        this.javaNodeManager = javaNodeManager;
    }

    /**
     * Shows the implementations of the selected element.
     *
     * @param editorPartPresenter
     *         the active editor
     */
    public void show(final EditorPartPresenter editorPartPresenter) {
        if (!(editorPartPresenter instanceof TextEditorPresenter)) {
            Log.error(getClass(), "Open Declaration support only TextEditorPresenter as editor");
            return;
        }
        activeEditor = ((TextEditorPresenter)editorPartPresenter);
        final VirtualFile file = activeEditor.getEditorInput().getFile();

        String projectPath = file.getProject().getProjectConfig().getPath();
        String fqn = JavaSourceFolderUtil.getFQNForFile(file);

        Promise<ImplementationsDescriptorDTO> promise = service.getImplementations(projectPath, fqn, activeEditor.getCursorOffset());
        promise.then(new Operation<ImplementationsDescriptorDTO>() {
            @Override
            public void apply(ImplementationsDescriptorDTO implementationsDescriptor) throws OperationException {
                int overridingSize = implementationsDescriptor.getImplementations().size();

                String title = locale.openImplementationWindowTitle(implementationsDescriptor.getMemberName(), overridingSize);
                NoImplementationWidget noImplementationWidget = new NoImplementationWidget(popupResources,
                                                                                           javaResources,
                                                                                           locale,
                                                                                           OpenImplementationPresenter.this,
                                                                                           title);
                if (overridingSize == 1) {
                    actionPerformed(implementationsDescriptor.getImplementations().get(0));
                } else if (overridingSize > 1) {
                    openOneImplementation(implementationsDescriptor,
                                          noImplementationWidget,
                                          (TextEditorPresenter)editorPartPresenter);
                } else if (!isNullOrEmpty(implementationsDescriptor.getMemberName()) && overridingSize == 0) {
                    showNoImplementations(noImplementationWidget, (TextEditorPresenter)editorPartPresenter);
                }
            }
        }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(OpenImplementationPresenter.class, arg.getMessage());
            }
        });
    }

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
                           .then(openNode(member))
                           .then(setCursor(member.getFileRegion()));
        }
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                activeEditor.setFocus();
            }
        });
    }

    private void showNoImplementations(NoImplementationWidget noImplementationWidget, TextEditorPresenter editorPartPresenter) {
        int offset = editorPartPresenter.getCursorOffset();
        PositionConverter.PixelCoordinates coordinates = editorPartPresenter.getPositionConverter().offsetToPixel(offset);
        Type type = dtoFactory.createDto(Type.class);
        type.setFlags(-1);
        noImplementationWidget.addItem(type);
        noImplementationWidget.show(coordinates.getX(), coordinates.getY());
    }

    private void openOneImplementation(ImplementationsDescriptorDTO implementationsDescriptor,
                                       NoImplementationWidget implementationWidget,
                                       TextEditorPresenter editorPartPresenter) {
        int offset = editorPartPresenter.getCursorOffset();
        PositionConverter.PixelCoordinates coordinates = editorPartPresenter.getPositionConverter().offsetToPixel(offset);
        for (Type type : implementationsDescriptor.getImplementations()) {
            implementationWidget.addItem(type);
        }
        implementationWidget.show(coordinates.getX(), coordinates.getY());
        implementationWidget.asElement().getStyle().setWidth(600 + "px");
    }

    /**
     * Open implementation which describes by current member.
     *
     * @param member
     *         description of opened implementation
     */

    private void openFile(VirtualFile result, final Member member) {
        editorAgent.openEditor(result, new OpenEditorCallbackImpl() {
            @Override
            public void onEditorOpened(EditorPartPresenter editor) {
                setCursorPosition(member.getFileRegion());
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

    private Function<Node, Node> setCursor(final Region region) {
        return new Function<Node, Node>() {
            @Override
            public Node apply(Node node) throws FunctionException {
                setCursorPosition(region);

                return node;
            }
        };
    }

    private void setCursorPosition(final Region region) {
        if (!(editorAgent.getActiveEditor() instanceof TextEditorPresenter)) {
            return;
        }
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                TextEditorPresenter editor = (TextEditorPresenter)editorAgent.getActiveEditor();
                editor.setFocus();
                editor.getDocument().setSelectedRange(LinearRange.createWithStart(region.getOffset()).andLength(0), true);
            }
        });
    }
}