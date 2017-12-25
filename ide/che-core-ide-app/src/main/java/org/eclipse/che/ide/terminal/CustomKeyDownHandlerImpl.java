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

public final class CustomKeyDownHandlerImpl implements Terminal.CustomKeyDownHandler {

  private final Terminal terminal;

  public CustomKeyDownHandlerImpl(Terminal terminal) {
    this.terminal = terminal;
  }

  @Override
  public boolean keyDown(KeyboardEvent ev) {
    int keyC = 67;
    int keyV = 86;
    if (ev.isCtrlKey() && !(ev.isShiftKey() || ev.isMetaKey() || ev.isAltKey())) {

      // handle Ctrl + V
      if (ev.getKeyCode() == keyV) {
        return false;
      }

      // handle Ctrl + C.
      return ev.getKeyCode() != keyC || !terminal.hasSelection();
    }
    return true;
  }
}
