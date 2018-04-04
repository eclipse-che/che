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
package org.eclipse.che.ide.util.input;

import elemental.events.KeyboardEvent.KeyCode;

/**
 * Support mac keys
 *
 * @author Evgen Vidolob
 */
public class MackeyMapUtil {
  public static final String ESCAPE = "\u238B";
  public static final String TAB = "\u21E5";
  public static final String TAB_BACK = "\u21E4";
  public static final String CAPS_LOCK = "\u21EA";
  public static final String SHIFT = "\u21E7";
  public static final String CONTROL = "\u2303";
  public static final String OPTION = "\u2325";
  public static final String APPLE = "\uF8FF";
  public static final String COMMAND = "\u2318";
  public static final String SPACE = "\u2423";
  public static final String RETURN = "\u23CE";
  public static final String BACKSPACE = "\u232B";
  public static final String DELETE = "\u2326";
  public static final String HOME = "\u2196";
  public static final String END = "\u2198";
  public static final String PAGE_UP = "\u21DE";
  public static final String PAGE_DOWN = "\u21DF";
  public static final String UP = "\u2191";
  public static final String DOWN = "\u2193";
  public static final String LEFT = "\u2190";
  public static final String RIGHT = "\u2192";
  public static final String CLEAR = "\u2327";
  public static final String NUMBER_LOCK = "\u21ED";
  public static final String ENTER = "\u2324";
  public static final String EJECT = "\u23CF";
  public static final String POWER3 = "\u233D";
  public static final String NUM_PAD = "\u2328";

  public static String getModifiersText(int modifiers) {
    StringBuilder buf = new StringBuilder();
    if ((modifiers & ModifierKeys.CTRL) != 0) buf.append(CONTROL);
    if ((modifiers & ModifierKeys.ALT) != 0) buf.append(OPTION);
    if ((modifiers & ModifierKeys.SHIFT) != 0) buf.append(SHIFT);
    if ((modifiers & ModifierKeys.ACTION) != 0) buf.append(COMMAND);
    return buf.toString();
  }

  public static String getKeyText(int code) {
    switch (code) {
      case KeyCode.BACKSPACE:
        return BACKSPACE;
      case KeyCode.ESC:
        return ESCAPE;
      case KeyCode.CAPS_LOCK:
        return CAPS_LOCK;
      case KeyCode.TAB:
        return TAB;
      case KeyCode.SPACE:
        return SPACE;
      case KeyCode.DELETE:
        return DELETE;
      case KeyCode.HOME:
        return HOME;
      case KeyCode.END:
        return END;
      case KeyCode.PAGE_UP:
        return PAGE_UP;
      case KeyCode.PAGE_DOWN:
        return PAGE_DOWN;
      case KeyCode.UP:
        return UP;
      case KeyCode.DOWN:
        return DOWN;
      case KeyCode.LEFT:
        return LEFT;
      case KeyCode.RIGHT:
        return RIGHT;
      case KeyCode.ENTER:
        return RETURN;
      case 0:
        return "fn";
    }
    return KeyCodeMap.getKeyText(code);
  }

  public static String getKeyStrokeText(CharCodeWithModifiers keyStroke) {
    final String modifiers = getModifiersText(keyStroke.getModifiers());
    final String key = getKeyText(keyStroke.getCharCode());
    return modifiers + key;
  }
}
