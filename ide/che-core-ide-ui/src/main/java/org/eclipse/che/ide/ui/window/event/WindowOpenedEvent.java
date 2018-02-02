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
