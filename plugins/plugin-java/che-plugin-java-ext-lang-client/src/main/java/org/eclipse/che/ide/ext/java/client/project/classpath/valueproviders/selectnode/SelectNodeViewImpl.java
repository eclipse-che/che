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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode;

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
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.selectnode.interceptors.ClasspathNodeInterceptor;
import org.eclipse.che.ide.project.shared.NodesResources;
import org.eclipse.che.ide.resources.tree.ResourceNode;
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
  private final JavaResources javaResources;
  private final NodesResources nodesResources;

  private Tree tree;
  private ActionDelegate delegate;
  private ClasspathNodeInterceptor interceptor;

  private Button acceptButton;
  private Button cancelButton;

  @UiField DockLayoutPanel treeContainer;

  interface SelectPathViewImplUiBinder extends UiBinder<Widget, SelectNodeViewImpl> {}

  @Inject
  public SelectNodeViewImpl(
      CoreLocalizationConstant locale,
      JavaResources javaResources,
      SelectPathViewImplUiBinder uiBinder,
      NodesResources nodesResources) {
    this.javaResources = javaResources;
    this.nodesResources = nodesResources;
    setTitle(locale.selectPathWindowTitle());

    Widget widget = uiBinder.createAndBindUi(this);
    setWidget(widget);

    Set<NodeInterceptor> interceptors = new HashSet<>();
    interceptors.add(interceptor);
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
              acceptButton.setEnabled(interceptor.isNodeValid(event.getSelection().get(0)));
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
        addFooterButton(locale.ok(), "select-path-ok-button", event -> acceptButtonClicked(), true);
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
  public void setStructure(List<Node> nodes, ClasspathNodeInterceptor nodeInterceptor) {
    this.interceptor = nodeInterceptor;
    tree.getNodeStorage().clear();
    tree.getNodeLoader().getNodeInterceptors().clear();
    tree.getNodeLoader().getNodeInterceptors().add(interceptor);
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
    delegate.setSelectedNode(((ResourceNode) selectedNode).getData().getLocation().toString());

    hide();
  }
}
