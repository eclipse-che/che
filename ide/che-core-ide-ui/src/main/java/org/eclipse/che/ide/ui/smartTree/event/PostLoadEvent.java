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
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.Node;

/**
 * Event fires when children has been loaded and processed.
 *
 * @author Vlad Zhukovskiy
 */
public class PostLoadEvent extends GwtEvent<PostLoadEvent.PostLoadHandler> {

  public interface PostLoadHandler extends EventHandler {
    void onPostLoad(PostLoadEvent event);
  }

  public interface HasPostLoadHandlers {
    public HandlerRegistration addPostLoadHandler(PostLoadHandler handler);
  }

  private static Type<PostLoadHandler> TYPE;

  public static Type<PostLoadHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private Node requestedNode;
  private List<Node> receivedNodes;

  public PostLoadEvent(Node requestedNode, List<Node> receivedNodes) {
    this.requestedNode = requestedNode;
    this.receivedNodes = receivedNodes;
  }

  @Override
  public Type<PostLoadHandler> getAssociatedType() {
    return TYPE;
  }

  public Node getRequestedNode() {
    return requestedNode;
  }

  public List<Node> getReceivedNodes() {
    return receivedNodes;
  }

  @Override
  protected void dispatch(PostLoadHandler handler) {
    handler.onPostLoad(this);
  }
}
