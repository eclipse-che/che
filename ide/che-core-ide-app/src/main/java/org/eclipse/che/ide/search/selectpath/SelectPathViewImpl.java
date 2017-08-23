/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.search.selectpath;

import static org.eclipse.che.ide.ui.smartTree.SelectionModel.Mode.SINGLE;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.resources.tree.ResourceNode;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;
import org.eclipse.che.ide.ui.smartTree.NodeLoader;
import org.eclipse.che.ide.ui.smartTree.NodeStorage;
import org.eclipse.che.ide.ui.smartTree.Tree;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.data.NodeInterceptor;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of {@link SelectPathView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SelectPathViewImpl extends Window implements SelectPathView {
  private Tree tree;
  private ActionDelegate delegate;

  Button acceptButton;
  Button cancelButton;

  @UiField DockLayoutPanel treeContainer;

  interface SelectPathViewImplUiBinder extends UiBinder<Widget, SelectPathViewImpl> {}

  @Inject
  public SelectPathViewImpl(CoreLocalizationConstant locale, SelectPathViewImplUiBinder uiBinder) {
    setTitle(locale.selectPathWindowTitle());

    Widget widget = uiBinder.createAndBindUi(this);
    setWidget(widget);

    FolderNodeInterceptor interceptor = new FolderNodeInterceptor();
    Set<NodeInterceptor> interceptors = new HashSet<>();
    interceptors.add(interceptor);
    NodeLoader loader = new NodeLoader(interceptors);
    NodeStorage nodeStorage = new NodeStorage();

    tree = new Tree(nodeStorage, loader);
    tree.setAutoSelect(true);
    tree.getSelectionModel().setSelectionMode(SINGLE);
    treeContainer.add(tree);

    KeyboardNavigationHandler handler =
        new KeyboardNavigationHandler() {
          @Override
          public void onEnter(NativeEvent evt) {
            evt.preventDefault();
            acceptButtonClicked();
          }
        };

    handler.bind(tree);

    cancelButton =
        createButton(
            locale.cancel(),
            "select-path-cancel-button",
            new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                hide();
              }
            });

    acceptButton =
        createPrimaryButton(
            locale.ok(),
            "select-path-ok-button",
            new ClickHandler() {
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
      delegate.setSelectedPath(((ResourceNode) selectedNode).getData().getLocation().toString());
    }

    hide();
  }
}
