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
import com.google.gwt.event.shared.HasHandlers;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.StoreRemoveEvent.StoreRemoveHandler;

/**
 * Indicates that an element that was visible has been removed from the Store.
 *
 * @author Vlad Zhukovsiy
 */
public class StoreRemoveEvent extends GwtEvent<StoreRemoveHandler> {

  public interface HasStoreRemoveHandler extends HasHandlers {
    HandlerRegistration addStoreRemoveHandler(StoreRemoveHandler handler);
  }

  public interface StoreRemoveHandler extends EventHandler {
    void onRemove(StoreRemoveEvent event);
  }

  private static Type<StoreRemoveHandler> TYPE;

  public static Type<StoreRemoveHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private final int index;

  private final Node node;
  private Node parent;
  private List<Node> children;

  public StoreRemoveEvent(int index, Node node, Node parent, List<Node> children) {
    this.index = index;
    this.node = node;
    this.parent = parent;
    this.children = Collections.unmodifiableList(children);
  }

  @Override
  public Type<StoreRemoveHandler> getAssociatedType() {
    return getType();
  }

  public int getIndex() {
    return index;
  }

  public Node getNode() {
    return node;
  }

  public List<Node> getChildren() {
    return children;
  }

  public Node getParent() {
    return parent;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(StoreRemoveHandler handler) {
    handler.onRemove(this);
  }
}
