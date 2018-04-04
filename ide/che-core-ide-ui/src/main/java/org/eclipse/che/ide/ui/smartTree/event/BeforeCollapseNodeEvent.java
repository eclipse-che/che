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
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.BeforeCollapseNodeEvent.BeforeCollapseNodeHandler;

/**
 * Event fires before node will be collapsed.
 *
 * @author Vlad Zhukovskiy
 */
public class BeforeCollapseNodeEvent extends GwtEvent<BeforeCollapseNodeHandler>
    implements CancellableEvent {

  public interface HasBeforeCollapseItemHandlers {
    HandlerRegistration addBeforeCollapseHandler(BeforeCollapseNodeHandler handler);
  }

  public interface BeforeCollapseNodeHandler extends EventHandler {
    void onBeforeCollapse(BeforeCollapseNodeEvent event);
  }

  private static Type<BeforeCollapseNodeHandler> TYPE;

  public static Type<BeforeCollapseNodeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private boolean cancelled;
  private Node node;

  public BeforeCollapseNodeEvent(Node node) {
    this.node = node;
  }

  public Node getNode() {
    return node;
  }

  @Override
  public Type<BeforeCollapseNodeHandler> getAssociatedType() {
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
  protected void dispatch(BeforeCollapseNodeHandler handler) {
    handler.onBeforeCollapse(this);
  }
}
