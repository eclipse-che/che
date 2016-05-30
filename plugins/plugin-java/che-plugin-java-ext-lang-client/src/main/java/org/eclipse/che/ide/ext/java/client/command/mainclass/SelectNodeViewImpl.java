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
package org.eclipse.che.ide.ext.java.client.command.mainclass;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.api.project.node.interceptor.NodeInterceptor;
import org.eclipse.che.ide.ext.java.client.project.interceptor.JavaContentRootInterceptor;
import org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.NodeUniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.UniqueKeyProvider;
import org.eclipse.che.ide.ui.smartTree.event.SelectionChangedEvent;
import org.eclipse.che.ide.ui.window.Window;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

/**
 * Implementation of {@link SelectNodeView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectNodeViewImpl extends Window implements SelectNodeView {
    private final JavaContentRootInterceptor javaContentRootInterceptor;
    private final ClassNodeInterceptor       classNodeInterceptor;

    private Tree                     tree;
    private ActionDelegate           delegate;

    Button acceptButton;
    Button cancelButton;

    @UiField
    DockLayoutPanel treeContainer;

    interface SelectPathViewImplUiBinder extends UiBinder<Widget, SelectNodeViewImpl> {
    }

    @Inject
    public SelectNodeViewImpl(CoreLocalizationConstant locale,
                              ClassNodeInterceptor classNodeInterceptor,
                              JavaContentRootInterceptor javaContentRootInterceptor,
                              SelectPathViewImplUiBinder uiBinder) {
        this.classNodeInterceptor = classNodeInterceptor;
        this.javaContentRootInterceptor = javaContentRootInterceptor;

        setTitle(locale.selectPathWindowTitle());

        Widget widget = uiBinder.createAndBindUi(this);
        setWidget(widget);

        UniqueKeyProvider<Node> uniqueKeyProvider = new NodeUniqueKeyProvider() {
            @NotNull
            @Override
            public String getKey(@NotNull Node item) {
                if (item instanceof HasStorablePath) {
                    return ((HasStorablePath)item).getStorablePath();
                } else {
                    return String.valueOf(item.hashCode());
                }
            }
        };
        Set<NodeInterceptor> interceptors = new HashSet<>();
        interceptors.add(classNodeInterceptor);
        NodeLoader loader = new NodeLoader(interceptors);
        NodeStorage nodeStorage = new NodeStorage(uniqueKeyProvider);

        tree = new Tree(nodeStorage, loader);
        tree.setAutoSelect(true);
        tree.getSelectionModel().setSelectionMode(SINGLE);
        treeContainer.add(tree);

        tree.getSelectionModel().addSelectionChangedHandler(new SelectionChangedEvent.SelectionChangedHandler() {
            @Override
            public void onSelectionChanged(SelectionChangedEvent event) {
                if (event.getSelection().isEmpty()) {
                    return;
                }

                Node node = event.getSelection().get(0);

                if (!(node instanceof FileReferenceNode)) {
                    acceptButton.setEnabled(false);
                    return;
                }

                FileReferenceNode selectedNode = (FileReferenceNode)node;

                acceptButton.setEnabled(selectedNode.getPath().endsWith(".java"));
            }
        });

        KeyboardNavigationHandler handler = new KeyboardNavigationHandler() {
            @Override
            public void onEnter(NativeEvent evt) {
                evt.preventDefault();
                acceptButtonClicked();
            }
        };

        handler.bind(tree);

        cancelButton = createButton(locale.cancel(), "select-path-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        acceptButton = createPrimaryButton(locale.ok(), "select-path-ok-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                acceptButtonClicked();
            }
        });

        addButtonToFooter(acceptButton);
        addButtonToFooter(cancelButton);
    }

    @Override
    protected void onEnterClicked() {
        if (isWidgetFocused(acceptButton)) {
            acceptButtonClicked();
            return;
        }

        if (isWidgetFocused(cancelButton)) {
            hide();
        }
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        super.show(tree);
        if (!tree.getRootNodes().isEmpty()) {
            tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
        }
    }

    @Override
    public void setStructure(List<Node> nodes) {
        tree.getNodeStorage().clear();
        tree.getNodeLoader().getNodeInterceptors().clear();
        tree.getNodeLoader().getNodeInterceptors().add(classNodeInterceptor);
        tree.getNodeLoader().getNodeInterceptors().add(javaContentRootInterceptor);
        for (Node node : nodes) {
            tree.getNodeStorage().add(node);
        }
    }

    private void acceptButtonClicked() {
        List<Node> nodes = tree.getSelectionModel().getSelectedNodes();
        if (nodes.isEmpty()) {
            return;
        }
        Node selectedNode = nodes.get(0);
        String fqn = "";

        if (selectedNode instanceof FileReferenceNode) {
            FileReferenceNode fileNode = (FileReferenceNode)selectedNode;
            fqn = JavaSourceFolderUtil.getFQNForFile(fileNode);
        }

        delegate.setSelectedNode(((HasStorablePath)selectedNode).getStorablePath(), fqn);

        hide();
    }
}
