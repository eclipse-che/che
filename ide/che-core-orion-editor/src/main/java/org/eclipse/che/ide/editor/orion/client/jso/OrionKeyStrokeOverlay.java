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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

public class OrionKeyStrokeOverlay extends JavaScriptObject {

  protected OrionKeyStrokeOverlay() {}

  /**
   * Constructs a new key stroke with the given key code, modifiers and event type.
   *
   * @param keyCode the key code.
   * @param modifier1 the primary modifier (usually Command on Mac and Control on other platforms).
   * @param modifier2 the secondary modifier (usually Shift).
   * @param modifier3 the third modifier (usually Alt).
   * @param modifier4 the fourth modifier (usually Control on the Mac).
   * @param type the type of event that the key binding matches; either "keydown" or "keypress".
   */
  public static final native OrionKeyStrokeOverlay create(
      int keyCode,
      boolean modifier1,
      boolean modifier2,
      boolean modifier3,
      boolean modifier4,
      String type,
      JavaScriptObject keyBindingModule) /*-{
        return new keyBindingModule.KeyStroke(keyCode, modifier1, modifier2, modifier3, modifier4, type);
    }-*/;

  /**
   * Constructs a new key stroke with the given character, modifiers and event type.
   *
   * @param character the character of the key binding.
   * @param modifier1 the primary modifier (usually Command on Mac and Control on other platforms).
   * @param modifier2 the secondary modifier (usually Shift).
   * @param modifier3 the third modifier (usually Alt).
   * @param modifier4 the fourth modifier (usually Control on the Mac).
   * @param type the type of event that the key binding matches; either "keydown" or "keypress".
   */
  public static final native OrionKeyStrokeOverlay create(
      String character,
      boolean modifier1,
      boolean modifier2,
      boolean modifier3,
      boolean modifier4,
      String type,
      JavaScriptObject keyBindingModule) /*-{
        return new keyBindingModule.KeyStroke(character, modifier1, modifier2, modifier3, modifier4, type);
    }-*/;
}
