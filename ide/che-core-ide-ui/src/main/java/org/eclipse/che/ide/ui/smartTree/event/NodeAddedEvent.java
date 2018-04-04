/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Indicates that an element has been added to the Store.
 *
 * @author Vlad Zhukovskiy
 */
public class NodeAddedEvent extends GwtEvent<NodeAddedEvent.NodeAddedEventHandler> {

  public interface HasNodeAddedEventHandlers extends HasHandlers {
    HandlerRegistration addNodeAddedHandler(NodeAddedEventHandler handler);
  }

  public interface NodeAddedEventHandler extends EventHandler {
    void onNodeAdded(NodeAddedEvent event);
  }

  private static Type<NodeAddedEventHandler> TYPE;

  public static Type<NodeAddedEventHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private final List<Node> nodes;

  public NodeAddedEvent(List<Node> nodes) {
    this.nodes = Collections.unmodifiableList(nodes);
  }

  public NodeAddedEvent(Node node) {
    nodes = Collections.singletonList(node);
  }

  @Override
  public Type<NodeAddedEventHandler> getAssociatedType() {
    return getType();
  }

  public List<Node> getNodes() {
    return nodes;
  }

  @Override
  protected void dispatch(NodeAddedEventHandler handler) {
    handler.onNodeAdded(this);
  }
}
