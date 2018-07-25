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
package org.eclipse.che.ide.api;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;

/**
 * Event that describes the fact that Che browser's tab has been closed or closing now.
 *
 * @author Artem Zatsarynnyi
 */
public class WindowActionEvent extends GwtEvent<WindowActionHandler> {

  /** Type class used to register this event. */
  public static Type<WindowActionHandler> TYPE = new Type<>();

  /** Set of possible Window Actions. */
  public enum WindowAction {
    CLOSING,
    CLOSED
  }

  private final WindowAction windowAction;
  private final Window.ClosingEvent event;

  /** Creates a Window Closing Event. */
  public static WindowActionEvent createWindowClosingEvent(Window.ClosingEvent event) {
    return new WindowActionEvent(event, WindowAction.CLOSING);
  }

  /** Creates a Window Closed Event. */
  public static WindowActionEvent createWindowClosedEvent() {
    return new WindowActionEvent(null, WindowAction.CLOSED);
  }

  protected WindowActionEvent(Window.ClosingEvent event, WindowAction windowAction) {
    this.event = event;
    this.windowAction = windowAction;
  }

  @Override
  public Type<WindowActionHandler> getAssociatedType() {
    return TYPE;
  }

  /**
   * Makes sense only for {@link WindowAction#CLOSING}.
   *
   * <p>Please note, that custom messages are not supported any more. See
   * https://bugs.chromium.org/p/chromium/issues/detail?id=587940 and
   * https://www.chromestatus.com/feature/5349061406228480
   *
   * @see com.google.gwt.user.client.Window.ClosingEvent#setMessage(java.lang.String)
   */
  public void setMessage(String message) {
    event.setMessage(message);
  }

  @Override
  protected void dispatch(WindowActionHandler handler) {
    switch (windowAction) {
      case CLOSING:
        handler.onWindowClosing(this);
        break;
      case CLOSED:
        handler.onWindowClosed(this);
        break;
      default:
        break;
    }
  }
}
