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
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.BeforeLoadEvent.BeforeLoadHandler;

/**
 * Event fires before node children will be loaded.
 *
 * @author Vlad Zhukovskiy
 */
public class BeforeLoadEvent extends GwtEvent<BeforeLoadHandler> implements CancellableEvent {

  public interface BeforeLoadHandler extends EventHandler {
    void onBeforeLoad(BeforeLoadEvent event);
  }

  public interface HasBeforeLoadHandlers {
    public HandlerRegistration addBeforeLoadHandler(BeforeLoadHandler handler);
  }

  private static Type<BeforeLoadHandler> TYPE;

  public static Type<BeforeLoadHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private Node requestedNode;
  private boolean cancelled;

  public BeforeLoadEvent(Node requestedNode) {
    this.requestedNode = requestedNode;
  }

  @Override
  public Type<BeforeLoadHandler> getAssociatedType() {
    return TYPE;
  }

  public Node getRequestedNode() {
    return requestedNode;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  /** {@inheritDoc} */
  @Override
  public void setCancelled(boolean cancel) {
    cancelled = cancel;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(BeforeLoadHandler handler) {
    handler.onBeforeLoad(this);
  }
}
