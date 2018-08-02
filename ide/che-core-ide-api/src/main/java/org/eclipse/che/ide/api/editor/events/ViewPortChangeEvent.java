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
package org.eclipse.che.ide.api.editor.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Event for change of viewport (visible section) changes.
 *
 * @author "Mickaël Leduque"
 */
public class ViewPortChangeEvent extends GwtEvent<ViewPortChangeHandler> {
  /** The type instance for this event. */
  public static final Type<ViewPortChangeHandler> TYPE = new Type<>();

  private final int viewPortStart;
  private final int viewPortEnd;

  public ViewPortChangeEvent(int from, int to) {
    this.viewPortStart = from;
    this.viewPortEnd = to;
  }

  @Override
  public Type<ViewPortChangeHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final ViewPortChangeHandler handler) {
    handler.onViewPortChange(this);
  }

  public int getViewPortStart() {
    return viewPortStart;
  }

  public int getViewPortEnd() {
    return viewPortEnd;
  }
}
