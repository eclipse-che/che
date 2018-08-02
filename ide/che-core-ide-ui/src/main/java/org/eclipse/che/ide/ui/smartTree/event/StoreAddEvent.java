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
import com.google.gwt.event.shared.HasHandlers;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.StoreAddEvent.StoreAddHandler;

/**
 * Indicates that an element has been added to the Store.
 *
 * @author Vlad Zhukovskiy
 */
public class StoreAddEvent extends GwtEvent<StoreAddHandler> {

  public interface HasStoreAddHandlers extends HasHandlers {
    HandlerRegistration addStoreAddHandler(StoreAddHandler handler);
  }

  public interface StoreAddHandler extends EventHandler {
    void onAdd(StoreAddEvent event);
  }

  private static Type<StoreAddHandler> TYPE;

  public static Type<StoreAddHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private final List<Node> nodes;
  private final int index;

  public StoreAddEvent(int index, List<Node> nodes) {
    this.index = index;
    this.nodes = Collections.unmodifiableList(nodes);
  }

  public StoreAddEvent(int index, Node node) {
    this.index = index;
    nodes = Collections.singletonList(node);
  }

  @Override
  public Type<StoreAddHandler> getAssociatedType() {
    return getType();
  }

  public int getIndex() {
    return index;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(StoreAddHandler handler) {
    handler.onAdd(this);
  }
}
