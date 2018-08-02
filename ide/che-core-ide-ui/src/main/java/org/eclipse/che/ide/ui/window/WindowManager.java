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
package org.eclipse.che.ide.ui.window;

import com.google.gwt.core.client.GWT;
import java.util.LinkedList;
import org.eclipse.che.ide.util.dom.DomUtils;

/**
 * Manages the opening and closing instances of {@link Window}. Class is not intended to use by the
 * third-party extensions.
 *
 * @author Vlad Zhukovskyi
 * @since 6.0.0
 * @see Window
 */
final class WindowManager {

  private static WindowManager instance;

  static WindowManager getInstance() {
    if (instance == null) {
      instance = GWT.create(WindowManager.class);
    }

    return instance;
  }

  private final LinkedList<Window> windowStack;
  private Window activeWindow;

  private WindowManager() {
    windowStack = new LinkedList<>();
  }

  Window getActive() {
    return activeWindow;
  }

  void register(Window window) {
    windowStack.add(window);
  }

  void unregister(Window window) {
    if (activeWindow == window) {
      activeWindow = null;
    }

    windowStack.removeIf(w -> w == window);

    activateLast();
  }

  void bringToFront(Window window) {
    if (window != activeWindow) {
      if (!windowStack.contains(window)) {
        windowStack.add(window);
      }

      activateWindow(window);
    } else {
      focus(window);
    }
  }

  private void focus(Window window) {
    window.focus();
  }

  private void activateLast() {
    if (windowStack.isEmpty()) {
      return;
    }

    Window lastWindow = windowStack.getLast();
    activateWindow(lastWindow);
  }

  private void activateWindow(Window window) {
    if (activeWindow == window || window == null) {
      return;
    }

    activeWindow = window;
    windowStack.forEach(
        w -> {
          if (w != window) {
            w.setActive(false);
          }
        });
    window.setActive(true);
    window.setZIndex(DomUtils.incrementAndGetTopZIndex(1));

    focus(window);
  }
}
