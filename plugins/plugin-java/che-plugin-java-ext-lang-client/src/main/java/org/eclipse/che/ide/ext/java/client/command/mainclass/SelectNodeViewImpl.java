/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.command.mainclass;

import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.resolveFQN;
import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;
import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.tree.JavaPackageConnector;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.resources.tree.SkipHiddenNodesInterceptor;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of {@link SelectNodeView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectNodeViewImpl extends Window implements SelectNodeView {
  private final ClassNodeInterceptor classNodeInterceptor;
  private final JavaPackageConnector javaPackageConnector;
  private final SkipHiddenNodesInterceptor skipHiddenNodesInterceptor;

  private Tree tree;
  private ActionDelegate delegate;

  Button acceptButton;
  Button cancelButton;

  @UiField DockLayoutPanel treeContainer;

  interface SelectPathViewImplUiBinder extends UiBinder<Widget, SelectNodeViewImpl> {}

  @Inject
  public SelectNodeViewImpl(
      CoreLocalizationConstant locale,
      ClassNodeInterceptor classNodeInterceptor,
      SelectPathViewImplUiBinder uiBinder,
      JavaPackageConnector javaPackageConnector,
      SkipHiddenNodesInterceptor skipHiddenNodesInterceptor) {
    this.classNodeInterceptor = classNodeInterceptor;
    this.javaPackageConnector = javaPackageConnector;
    this.skipHiddenNodesInterceptor = skipHiddenNodesInterceptor;

    setTitle(locale.selectPathWindowTitle());

    Widget widget = uiBinder.createAndBindUi(this);
    setWidget(widget);

    Set<NodeInterceptor> interceptors = new HashSet<>();
    interceptors.add(classNodeInterceptor);
    NodeLoader loader = new NodeLoader(interceptors);
    NodeStorage nodeStorage = new NodeStorage();

    tree = new Tree(nodeStorage, loader);
    tree.setAutoSelect(true);
    tree.getSelectionModel().setSelectionMode(SINGLE);
    treeContainer.add(tree);

    tree.getSelectionModel()
        .addSelectionChangedHandler(
            event -> {
              if (event.getSelection().isEmpty()) {
                return;
              }

              Node node = event.getSelection().get(0);

              if (!(node instanceof ResourceNode)) {
                acceptButton.setEnabled(false);
                return;
              }

              ResourceNode selectedNode = (ResourceNode) node;

              acceptButton.setEnabled(
                  selectedNode.getData().getLocation().toString().endsWith(".java"));
            });

    KeyboardNavigationHandler handler =
        new KeyboardNavigationHandler() {
          @Override
          public void onEnter(NativeEvent evt) {
            evt.preventDefault();
            acceptButtonClicked();
          }
        };

    handler.bind(tree);

    cancelButton = addFooterButton(locale.cancel(), "select-path-cancel-button", event -> hide());

    acceptButton =
        addFooterButton(locale.ok(), "select-path-ok-button", event -> acceptButtonClicked());
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    if (isWidgetOrChildFocused(acceptButton)) {
      acceptButtonClicked();
    } else if (isWidgetOrChildFocused(cancelButton)) {
      hide();
    }
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void showDialog() {
    show(tree);
  }

  @Override
  protected void onShow() {
    if (!tree.getRootNodes().isEmpty()) {
      tree.getSelectionModel().select(tree.getRootNodes().get(0), false);
    }
  }

  @Override
  public void setStructure(List<Node> nodes) {
    tree.getNodeStorage().clear();
    tree.getNodeLoader().getNodeInterceptors().clear();
    tree.getNodeLoader().getNodeInterceptors().add(classNodeInterceptor);
    tree.getNodeLoader().getNodeInterceptors().add(javaPackageConnector);
    tree.getNodeLoader().getNodeInterceptors().add(skipHiddenNodesInterceptor);
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

    if (selectedNode instanceof ResourceNode) {
      final Resource resource = ((ResourceNode) selectedNode).getData();
      delegate.setSelectedNode(resource, resolveFQN(resource));
    }

    hide();
  }
}
