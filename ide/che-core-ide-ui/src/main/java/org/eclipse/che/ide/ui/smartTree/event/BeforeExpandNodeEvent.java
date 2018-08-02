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
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.BeforeExpandNodeEvent.BeforeExpandNodeHandler;

/**
 * Event fires before node will be expanded.
 *
 * @author Vlad Zhukovsiy
 */
public class BeforeExpandNodeEvent extends GwtEvent<BeforeExpandNodeHandler>
    implements CancellableEvent {

  public interface BeforeExpandNodeHandler extends EventHandler {
    void onBeforeExpand(BeforeExpandNodeEvent event);
  }

  public interface HasBeforeExpandNodeHandlers {
    HandlerRegistration addBeforeExpandHandler(BeforeExpandNodeHandler handler);
  }

  private static Type<BeforeExpandNodeHandler> TYPE;

  public static Type<BeforeExpandNodeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private boolean cancelled;
  private Node node;

  public BeforeExpandNodeEvent(Node node) {
    this.node = node;
  }

  public Node getNode() {
    return node;
  }

  @Override
  public Type<BeforeExpandNodeHandler> getAssociatedType() {
    return TYPE;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  /** {@inheritDoc} */
  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(BeforeExpandNodeHandler handler) {
    handler.onBeforeExpand(this);
  }
}
