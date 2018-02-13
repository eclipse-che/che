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
package org.eclipse.che.ide.ui.window;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import org.eclipse.che.ide.ui.button.ButtonAlignment;
import org.eclipse.che.ide.ui.smartTree.KeyboardNavigationHandler;

/**
 * Interface for the implementing a view representation an instance of {@link Window}. Uses as
 * "bridge" between window controlling interface and visual representation.
 *
 * @author Vlad Zhukovskyi
 * @see Window
 * @since 6.0.0
 */
public interface WindowView {

  /**
   * Provide the title for the window.
   *
   * @param title window title
   */
  void setTitle(String title);

  /**
   * Provide user widget which will be displayed to user in body content.
   *
   * @param widget widget to display in body content
   */
  void setContentWidget(Widget widget);

  /**
   * Returns the body content widget.
   *
   * @return widget to display in body content
   */
  Widget getContentWidget();

  /**
   * Provide the debug ID for the window and nested descendants.
   *
   * @param debugId debug identifier
   */
  void setDebugId(String debugId);

  /**
   * Returns the widget to be focused.
   *
   * @return the widget to be focused
   */
  Widget getFocusWidget();

  /**
   * Provide the widget to be focused when window is active.
   *
   * @param focusWidget the focus widget
   */
  void setFocusWidget(Widget focusWidget);

  /**
   * Provide property on current window to be able to close by pressing Escape key.
   *
   * @param closeOnEscape {@code true} if window is allow to be closed by pressing Escape key
   */
  void setCloseOnEscape(boolean closeOnEscape);

  /**
   * Returns the policy of processing Escape key press.
   *
   * @return {@code true} if window is allowed to be closed by pressing Escape key
   */
  boolean isCloseOnEscape();

  /**
   * Provide specific z-index value for the window to be able to display or activate on the top of
   * the viewport.
   *
   * @param zIndex z-index value
   */
  void setZIndex(int zIndex);

  /**
   * Provide window active state. Window become inactive, when it lost focus or another window
   * became an active.
   *
   * @param active window state
   */
  void setActive(boolean active);

  /**
   * Provide property to set up current window as modal. By default, window is modal.
   *
   * @param modal {@code true} if window has to be modal
   */
  void setModal(boolean modal);

  /**
   * Provide a proxy browser event handler to be able to pass browser event to implementor's of the
   * {@link Window}.
   *
   * @param handler browser event handler
   */
  void addBrowserEventHandler(BrowserEventHandler handler);

  /**
   * Provide a proxy window close event handler. Notifies the {@link Window} class that user clicked
   * on the close button.
   *
   * @param handler window close handler
   */
  void addWindowCloseEventHandler(WindowCloseEventHandler handler);

  /**
   * Uses to create and attach the window object to the root window. In other way, to display the
   * window.
   */
  void attach();

  /**
   * Uses to destroy and detach the window object from the root window. In other way, to hide the
   * window.
   */
  void detach();

  /**
   * Provide a button with user parameters to the button bar. If no button is provided, then button
   * bar will be hidden.
   *
   * @param text button caption
   * @param debugId debug identifier
   * @param clickHandler button click handler
   * @param primary button style, primary or not
   * @param alignment button alignment, left or right
   * @return newly created button and placed to the button bar
   */
  Button addButtonBarControl(
      String text,
      String debugId,
      ClickHandler clickHandler,
      boolean primary,
      ButtonAlignment alignment);

  /**
   * Provide a custom widget to the button bar. By default, custom widget places to the left side of
   * the button bar. If at least one widget provided to the button bar, than button bar will be
   * automatically shown.
   *
   * @param widget widget to show
   */
  void addButtonBarWidget(Widget widget);

  /**
   * Provide a custom keyboard navigation handler to allow window implementation do something on key
   * press.
   *
   * @param handler keyboard navigation handler
   */
  void addKeyboardNavigationHandler(KeyboardNavigationHandler handler);

  /** Interface to proxy the browser events. */
  interface BrowserEventHandler {

    /**
     * Performs an action when widget receives a browser event.
     *
     * @param event browser event
     */
    void onBrowserEvent(Event event);
  }

  /** Interface to proxy window close event. */
  interface WindowCloseEventHandler {

    /** Performs an action when window requests a closing. */
    void onClose();
  }
}
