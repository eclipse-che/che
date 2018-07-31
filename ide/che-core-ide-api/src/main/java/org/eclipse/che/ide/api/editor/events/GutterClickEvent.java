/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.events;

import com.google.gwt.event.shared.GwtEvent;
import elemental.events.MouseEvent;

/**
 * Events for clicks on the gutter element of the editor.
 *
 * @author "Mickaël Leduque"
 */
public class GutterClickEvent extends GwtEvent<GutterClickHandler> {
  /** The type instance for this event. */
  public static final Type<GutterClickHandler> TYPE = new Type<>();

  /** The line of the event. */
  private final int lineNumber;
  /** the gutter. */
  private final String gutterId;

  private final MouseEvent event;

  /**
   * @param lineNumber
   * @param gutterId
   */
  public GutterClickEvent(final int lineNumber, final String gutterId, final MouseEvent event) {
    this.lineNumber = lineNumber;
    this.gutterId = gutterId;
    this.event = event;
  }

  @Override
  public Type<GutterClickHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(final GutterClickHandler handler) {
    handler.onGutterClick(this);
  }

  /**
   * Returns the line on which the click happened.
   *
   * @return the line
   */
  public int getLineNumber() {
    return lineNumber;
  }

  /**
   * Returns the id of the gutter on which the click happened.
   *
   * @return the id of the gutter
   */
  public String getGutterId() {
    return gutterId;
  }

  /**
   * Returns the mouse event that generated the gutter event.
   *
   * @return the mouse event
   */
  public MouseEvent getEvent() {
    return event;
  }
}
