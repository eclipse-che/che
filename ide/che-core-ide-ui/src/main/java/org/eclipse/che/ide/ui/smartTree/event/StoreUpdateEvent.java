/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
import org.eclipse.che.ide.ui.smartTree.event.StoreUpdateEvent.StoreUpdateHandler;

/**
 * Event fires when nodes have been changed.
 *
 * @author Vlad Zhukovskiy
 */
public final class StoreUpdateEvent extends GwtEvent<StoreUpdateHandler> {

  public interface HasStoreUpdateHandlers extends HasHandlers {
    HandlerRegistration addStoreUpdateHandler(StoreUpdateHandler handler);
  }

  public interface StoreUpdateHandler extends EventHandler {
    void onUpdate(StoreUpdateEvent event);
  }

  private static Type<StoreUpdateHandler> TYPE;

  public static Type<StoreUpdateHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private List<Node> nodes;

  public StoreUpdateEvent(List<Node> nodes) {
    this.nodes = Collections.unmodifiableList(nodes);
  }

  @Override
  public Type<StoreUpdateHandler> getAssociatedType() {
    return getType();
  }

  public List<Node> getNodes() {
    return nodes;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(StoreUpdateHandler handler) {
    handler.onUpdate(this);
  }
}
