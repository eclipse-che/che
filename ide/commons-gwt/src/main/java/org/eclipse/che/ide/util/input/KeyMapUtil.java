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
package org.eclipse.che.ide.util.input;

import org.eclipse.che.ide.util.StringUtils;
import org.eclipse.che.ide.util.browser.UserAgent;

/** @author Evgen Vidolob */
public class KeyMapUtil {
  public static String getShortcutText(CharCodeWithModifiers shortcut) {
    String s = null;
    String acceleratorText = getKeystrokeText(shortcut);
    if (!acceleratorText.isEmpty()) {
      s = acceleratorText;
    }
    return s;
  }

  public static String getKeystrokeText(CharCodeWithModifiers accelerator) {
    if (accelerator == null) return "";
    if (UserAgent.isMac()) {
      return MackeyMapUtil.getKeyStrokeText(accelerator);
    }
    String acceleratorText = "";
    int modifiers = accelerator.getModifiers();
    final int code = accelerator.getCharCode();
    String keyText = KeyCodeMap.getKeyText(code);
    if (StringUtils.isUpperCase((char) accelerator.getCharCode())) {
      modifiers |= ModifierKeys.SHIFT;
    }
    keyText = keyText.toUpperCase();
    if (modifiers > 0) {
      acceleratorText = getModifiersText(modifiers);
    }

    acceleratorText += keyText;
    return acceleratorText.trim();
  }

  private static String getModifiersText(int modifiers) {
    final String keyModifiersText = getKeyModifiersText(modifiers);
    if (keyModifiersText.isEmpty()) {
      return keyModifiersText;
    } else {
      return keyModifiersText + "+";
    }
  }

  /**
   * Returns a <code>String</code> describing the modifier key(s), such as "Shift", or "Ctrl+Shift".
   */
  public static String getKeyModifiersText(int modifiers) {
    StringBuilder buf = new StringBuilder();

    if ((modifiers & ModifierKeys.ACTION) != 0) {
      if (UserAgent.isMac()) {
        buf.append("Cmd");
      } else {
        buf.append("Ctrl");
      }
      buf.append("+");
    }
    if ((modifiers & ModifierKeys.CTRL) != 0) {
      buf.append("Control");
      buf.append("+");
    }
    if ((modifiers & ModifierKeys.ALT) != 0) {
      buf.append("Alt");
      buf.append("+");
    }
    if ((modifiers & ModifierKeys.SHIFT) != 0) {
      buf.append("Shift");
      buf.append("+");
    }
    if (buf.length() > 0) {
      buf.setLength(buf.length() - 1); // remove trailing '+'
    }
    return buf.toString();
  }
}
