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
package org.eclipse.che.ide.editor.orion.client.events;

import com.google.gwt.event.shared.GwtEvent;

/** Event for scrolls. */
public class ScrollEvent extends GwtEvent<ScrollHandler> {
  /** The type instance for this event. */
  public static final Type<ScrollHandler> TYPE = new Type<>();

  public ScrollEvent() {}

  @Override
  public Type<ScrollHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final ScrollHandler handler) {
    handler.onScroll(this);
  }
}
