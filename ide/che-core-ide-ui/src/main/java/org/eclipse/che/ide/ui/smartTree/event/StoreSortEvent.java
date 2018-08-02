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
import org.eclipse.che.ide.ui.smartTree.event.StoreSortEvent.StoreSortHandler;

/**
 * Indicates that the store sort properties have changed.
 *
 * @author Vlad Zhukovskiy
 */
public final class StoreSortEvent extends GwtEvent<StoreSortHandler> {

  public interface HasStoreSortHandler extends HasHandlers {
    HandlerRegistration addStoreSortHandler(StoreSortHandler handler);
  }

  public interface StoreSortHandler extends EventHandler {
    void onSort(StoreSortEvent event);
  }

  private static Type<StoreSortHandler> TYPE;

  public static Type<StoreSortHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<>();
    }
    return TYPE;
  }

  @Override
  public Type<StoreSortHandler> getAssociatedType() {
    return getType();
  }

  /** {@inheritDoc} */
  @Override
  protected void dispatch(StoreSortHandler handler) {
    handler.onSort(this);
  }
}
