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
package org.eclipse.che.ide.ui.window.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Event fires when a {@link Window} widget is shown.
 *
 * @author Igor Vinokur
 */
public class WindowOpenedEvent extends GwtEvent<WindowOpenedEvent.WindowOpenedHandler> {

  public static final Type<WindowOpenedHandler> TYPE = new Type<>();

  public interface WindowOpenedHandler extends EventHandler {
    void onWindowOpened(WindowOpenedEvent event);
  }

  @Override
  public Type<WindowOpenedHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(WindowOpenedHandler handler) {
    handler.onWindowOpened(this);
  }
}
