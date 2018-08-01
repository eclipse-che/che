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
package org.eclipse.che.ide.ui.smartTree;

import static org.eclipse.che.ide.ui.smartTree.event.GoIntoStateEvent.State.ACTIVATED;
import static org.eclipse.che.ide.ui.smartTree.event.GoIntoStateEvent.State.DEACTIVATED;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.GoIntoStateEvent;
import org.eclipse.che.ide.ui.smartTree.event.GoIntoStateEvent.GoIntoStateHandler;

/**
 * Default implementation for {@link GoInto}.
 *
 * @author Vlad Zhukovskiy
 */
public class DefaultGoInto implements GoInto {
  private Tree tree;
  private boolean active;
  private Node node;
  private HandlerManager handlerManager;
  private List<Node> rootNodes;

  /** {@inheritDoc} */
  @Override
  public HandlerRegistration addGoIntoHandler(GoIntoStateHandler handler) {
    return ensureHandlers().addHandler(GoIntoStateEvent.getType(), handler);
  }

  /** {@inheritDoc} */
  public void bind(Tree tree) {
    this.tree = tree;
  }

  private HandlerManager ensureHandlers() {
    if (handlerManager == null) {
      handlerManager = new HandlerManager(this);
    }
    return handlerManager;
  }

  /** {@inheritDoc} */
  @Override
  public void fireEvent(GwtEvent<?> event) {
    if (handlerManager != null) {
      handlerManager.fireEvent(event);
    }
  }

  /** {@inheritDoc} */
  public boolean activate(Node node) {
    if (!node.supportGoInto()) {
      return false;
    }

    // save node
    this.node = node;

    // save root nodes
    rootNodes = tree.getRootNodes();

    // reset selection
    tree.getSelectionModel().deselectAll();

    Element rootContainer = tree.getContainer(null);
    rootContainer.setInnerHTML("");
    rootContainer.appendChild(tree.getNodeDescriptor(node).getRootContainer());

    // if go into node is collapsed - then we need to expand it
    if (!tree.getNodeDescriptor(node).isExpanded()) {
      tree.setExpanded(node, true);
    }

    // then select go into node
    tree.getSelectionModel().select(node, false);

    tree.update();

    fireEvent(new GoIntoStateEvent(ACTIVATED, node));

    return active = true;
  }

  /** {@inheritDoc} */
  public boolean isActive() {
    return active;
  }

  /** {@inheritDoc} */
  public void reset() {
    // reset selection
    tree.getSelectionModel().deselectAll();

    Element rootContainer = tree.getContainer(null);
    rootContainer.setInnerHTML("");

    // restore root nodes
    for (Node rootNode : rootNodes) {
      NodeDescriptor descriptor = tree.getNodeDescriptor(rootNode);
      rootContainer.appendChild(descriptor.getRootContainer());
    }

    // then re-add our go into node
    Node parent = node.getParent();
    if (parent != null) {
      tree.getNodeStorage().add(parent, node);
    }

    tree.scrollIntoView(node);
    tree.getSelectionModel().select(node, false);

    tree.update();

    active = false;

    fireEvent(new GoIntoStateEvent(DEACTIVATED, node));
  }

  /** {@inheritDoc} */
  public Node getLastUsed() {
    return node;
  }
}
