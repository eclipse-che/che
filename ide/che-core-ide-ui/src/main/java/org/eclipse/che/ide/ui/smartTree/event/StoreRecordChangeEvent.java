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
import com.google.gwt.event.shared.HasHandlers;
import org.eclipse.che.ide.ui.smartTree.event.StoreRecordChangeEvent.StoreRecordChangeHandler;

/**
 * Indicates that a Record object in the Store has been changed in some way.
 *
 * @author Vlad Zhukovskiy
 */
public final class StoreRecordChangeEvent extends GwtEvent<StoreRecordChangeHandler> {

  public interface HasStoreRecordChangeHandlers extends HasHandlers {
    HandlerRegistration addStoreRecordChangeHandler(StoreRecordChangeHandler handler);
  }

  public interface StoreRecordChangeHandler extends EventHandler {
    void onRecordChange(StoreRecordChangeEvent event);
  }

  private static Type<StoreRecordChangeHandler> TYPE;

  public static Type<StoreRecordChangeHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  // TODO support providing for passing nodes into event

  @Override
  public Type<StoreRecordChangeHandler> getAssociatedType() {
    return getType();
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(StoreRecordChangeHandler handler) {
    handler.onRecordChange(this);
  }
}
