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
import org.eclipse.che.ide.ui.smartTree.data.Node;
import org.eclipse.che.ide.ui.smartTree.event.StoreDataChangeEvent.StoreDataChangeHandler;

/**
 * Indicates that the items in the store have been replaced.
 *
 * @author Vlad Zhukovskiy
 */
public final class StoreDataChangeEvent extends GwtEvent<StoreDataChangeHandler> {

  public interface HasStoreDataChangeHandlers extends HasHandlers {
    HandlerRegistration addStoreDataChangeHandler(StoreDataChangeHandler handler);
  }

  public interface StoreDataChangeHandler extends EventHandler {
    void onDataChange(StoreDataChangeEvent event);
  }

  private static Type<StoreDataChangeHandler> TYPE;

  public static Type<StoreDataChangeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  private Node parent;

  public StoreDataChangeEvent(Node parent) {
    this.parent = parent;
  }

  @Override
  public Type<StoreDataChangeHandler> getAssociatedType() {
    return getType();
  }

  public Node getParent() {
    return parent;
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(StoreDataChangeHandler handler) {
    handler.onDataChange(this);
  }
}
