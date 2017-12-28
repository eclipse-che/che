/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.terminal;

import elemental.events.KeyboardEvent;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_C;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_V;

/**
 * Custom keyDown handler for {@link Terminal}. Implementation of the {@link
 * Terminal.CustomKeyDownHandler}. This handler created to support hotKeys:
 *
 * <ul>
 *   <li>"Ctrl + C" copy text in case if terminal contains selected text
 *   <li>"Ctrl + V" paste text from buffer to the terminal widget
 * </ul>
 *
 * @author Alexander Andrienko
 */
public final class TerminalCustomKeyDownHandlerImpl implements Terminal.CustomKeyDownHandler {

  private final Terminal terminal;

  public TerminalCustomKeyDownHandlerImpl(Terminal terminal) {
    this.terminal = terminal;
  }

  @Override
  public boolean keyDown(KeyboardEvent ev) {
    if (ev.isCtrlKey() && !(ev.isShiftKey() || ev.isMetaKey() || ev.isAltKey())) {

      // handle Ctrl + V
      if (ev.getKeyCode() == KEY_V) {
        return false;
      }

      // handle Ctrl + C.
      return ev.getKeyCode() != KEY_C || !terminal.hasSelection();
    }
    return true;
  }
}
