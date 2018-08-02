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
import org.eclipse.che.ide.ui.smartTree.event.StoreClearEvent.StoreClearHandler;

/**
 * Indicates that the data in the Store has been cleared.
 *
 * @author Vlad Zhukovskiy
 */
public final class StoreClearEvent extends GwtEvent<StoreClearHandler> {

  public interface HasStoreClearHandler extends HasHandlers {
    HandlerRegistration addStoreClearHandler(StoreClearHandler handler);
  }

  public interface StoreClearHandler extends EventHandler {
    void onClear(StoreClearEvent event);
  }

  private static Type<StoreClearHandler> TYPE;

  public static Type<StoreClearHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  @Override
  public Type<StoreClearHandler> getAssociatedType() {
    return getType();
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(StoreClearHandler handler) {
    handler.onClear(this);
  }
}
