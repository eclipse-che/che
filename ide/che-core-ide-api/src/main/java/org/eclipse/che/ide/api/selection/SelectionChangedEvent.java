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
package org.eclipse.che.ide.api.selection;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event that notifies of changed Selection
 *
 * @author Nikolay Zamosenchuk
 */
public class SelectionChangedEvent extends GwtEvent<SelectionChangedHandler> {
  public static Type<SelectionChangedHandler> TYPE = new Type<>();

  private final Selection<?> selection;

  /** @param selection new selection */
  public SelectionChangedEvent(Selection<?> selection) {
    this.selection = selection;
  }

  @Override
  public Type<SelectionChangedHandler> getAssociatedType() {
    return TYPE;
  }

  /** @return current selection */
  public Selection<?> getSelection() {
    return selection;
  }

  @Override
  protected void dispatch(SelectionChangedHandler handler) {
    handler.onSelectionChanged(this);
  }
}
