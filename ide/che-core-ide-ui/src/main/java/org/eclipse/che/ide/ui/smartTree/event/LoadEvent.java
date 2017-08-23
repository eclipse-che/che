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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Event fires when node's children has been loaded.
 *
 * @author Vlad Zhukovskiy
 */
public class LoadEvent extends GwtEvent<LoadEvent.LoadHandler> {

  public interface LoadHandler extends EventHandler {
    void onLoad(LoadEvent event);
  }

  public interface HasLoadHandlers {
    public HandlerRegistration addLoadHandler(LoadHandler handler);
  }

  private static Type<LoadHandler> TYPE;

  public static Type<LoadHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private Node requestedNode;
  private List<Node> receivedNodes;
  private boolean reloadExpandedChild;

  public LoadEvent(Node requestedNode, List<Node> receivedNodes) {
    this(requestedNode, receivedNodes, false);
  }

  public LoadEvent(Node requestedNode, List<Node> receivedNodes, boolean reloadExpandedChild) {
    this.requestedNode = requestedNode;
    this.receivedNodes = receivedNodes;
    this.reloadExpandedChild = reloadExpandedChild;
  }

  @Override
  public Type<LoadHandler> getAssociatedType() {
    return TYPE;
  }

  public Node getRequestedNode() {
    return requestedNode;
  }

  public List<Node> getReceivedNodes() {
    return receivedNodes;
  }

  public boolean isReloadExpandedChild() {
    return reloadExpandedChild;
  }

  @Override
  protected void dispatch(LoadHandler handler) {
    handler.onLoad(this);
  }
}
