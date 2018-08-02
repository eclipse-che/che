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
 * Event type for cursor activity.
 *
 * @author "Mickaël Leduque"
 */
public class CursorActivityEvent extends GwtEvent<CursorActivityHandler> {
  /** Type instance for the event. */
  public static final Type<CursorActivityHandler> TYPE = new Type<>();

  @Override
  public Type<CursorActivityHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final CursorActivityHandler handler) {
    handler.onCursorActivity(this);
  }
}
