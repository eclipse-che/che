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
import org.eclipse.che.ide.ui.smartTree.event.CollapseNodeEvent.CollapseNodeHandler;

/**
 * Event fires after an node is collapsed.
 *
 * @author Vlad Zhukovskiy
 */
public class CollapseNodeEvent extends GwtEvent<CollapseNodeHandler> {

  public interface CollapseNodeHandler extends EventHandler {
    void onCollapse(CollapseNodeEvent event);
  }

  public interface HasCollapseItemHandlers {
    HandlerRegistration addCollapseHandler(CollapseNodeHandler handler);
  }

  private static Type<CollapseNodeHandler> TYPE;

  public static Type<CollapseNodeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  @Override
  public Type<CollapseNodeHandler> getAssociatedType() {
    return TYPE;
  }

  private Node node;

  public CollapseNodeEvent(Node node) {
    this.node = node;
  }

  public Node getNode() {
    return node;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(CollapseNodeHandler handler) {
    handler.onCollapse(this);
  }
}
