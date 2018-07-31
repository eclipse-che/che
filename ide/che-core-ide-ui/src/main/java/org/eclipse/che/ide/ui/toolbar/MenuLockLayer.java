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
package org.eclipse.che.ide.ui.toolbar;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This Lock Layer for Popup Menu uses as root for for Popup Menus and uses for closing all visible
 * popups when user clicked outside one of them.
 *
 * @author Vitaliy Guliy
 */
public class MenuLockLayer extends AbsolutePanel {

  /** Lock Layer uses for locking of screen. Uses for hiding popups. */
  private class LockLayer extends AbsolutePanel {

    public LockLayer() {
      sinkEvents(Event.ONMOUSEDOWN);
    }

    @Override
    public void onBrowserEvent(Event event) {
      switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEDOWN:
          close();
          break;
      }
    }
  }

  /** Callback which is uses for closing Popup menu. */
  private CloseMenuHandler closeMenuCallback;

  private int topOffset = 20;

  /** Create Menu Lock Layer. */
  public MenuLockLayer() {
    this(null, 0);
  }

  /**
   * Create Menu Lock Layer.
   *
   * @param closeMenuCallback - callback which is uses for
   */
  public MenuLockLayer(CloseMenuHandler closeMenuCallback) {
    this(closeMenuCallback, 0);
  }

  public MenuLockLayer(CloseMenuHandler closeMenuCallback, int topOffset) {
    this.closeMenuCallback = closeMenuCallback;
    this.topOffset = topOffset;

    getElement().setId("menu-lock-layer-id");
    RootPanel.get().add(this, 0, topOffset);
    getElement().getStyle().setProperty("right", "0px");
    getElement().getStyle().setProperty("bottom", "0px");
    getElement().getStyle().setProperty("zIndex", (Integer.MAX_VALUE - 5) + "");

    AbsolutePanel blockMouseEventsPanel = new LockLayer();
    blockMouseEventsPanel.setStyleName("exo-lockLayer");
    blockMouseEventsPanel.getElement().getStyle().setProperty("position", "absolute");
    blockMouseEventsPanel.getElement().getStyle().setProperty("left", "0px");
    blockMouseEventsPanel.getElement().getStyle().setProperty("top", "0px");
    blockMouseEventsPanel.getElement().getStyle().setProperty("right", "0px");
    blockMouseEventsPanel.getElement().getStyle().setProperty("bottom", "0px");
    add(blockMouseEventsPanel);
  }

  public void close() {
    removeFromParent();
    if (closeMenuCallback != null) {
      closeMenuCallback.onCloseMenu();
    }
  }

  public int getTopOffset() {
    return topOffset;
  }
}
