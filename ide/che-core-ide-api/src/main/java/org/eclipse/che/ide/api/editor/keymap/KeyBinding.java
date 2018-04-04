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
package org.eclipse.che.ide.api.editor.keymap;

/** Description of a key binding. */
public class KeyBinding {
  /** Is the control key pressed? */
  private final boolean control;
  /** Is the shift key pressed? */
  private final boolean shift;
  /** Is the alt key pressed? */
  private final boolean alt;
  /** Is the cmd key pressed? */
  private final boolean cmd;
  /** The key code of this key binding. */
  private final Integer keyCodeNumber;
  /** The character of this key binding. */
  private final String character;
  /** The key event type. */
  private final String type;
  /** The action taken on the key binding. */
  private final KeyBindingAction action;

  public KeyBinding(
      final boolean control,
      final boolean shift,
      final boolean alt,
      final boolean cmd,
      final int keycode,
      final KeyBindingAction action) {
    this.control = control;
    this.shift = shift;
    this.alt = alt;
    this.cmd = cmd;
    this.keyCodeNumber = keycode;
    this.character = null;
    this.action = action;
    this.type = "keydown";
  }

  public KeyBinding(
      final boolean control,
      final boolean shift,
      final boolean alt,
      final boolean cmd,
      final char character,
      final KeyBindingAction action) {
    this.control = control;
    this.shift = shift;
    this.alt = alt;
    this.cmd = cmd;
    this.keyCodeNumber = null;
    this.character = String.valueOf(character);
    this.action = action;
    this.type = "keypress";
  }

  /**
   * Whether the control key is hold during the key binding.
   *
   * @return true iff the binding uses control
   */
  public boolean isControl() {
    return this.control;
  }

  /**
   * Whether the shift key is hold during the key binding.
   *
   * @return true iff the binding uses shift
   */
  public boolean isShift() {
    return this.shift;
  }

  /**
   * Whether the alt key is hold during the key binding.
   *
   * @return true iff the binding uses alt
   */
  public boolean isAlt() {
    return this.alt;
  }

  /**
   * Whether the cmd key is hold during the key binding.
   *
   * @return true iff the binding uses cmd
   */
  public boolean isCmd() {
    return this.cmd;
  }

  /**
   * Returns the keycode of the key binding.
   *
   * @return the keycode
   */
  public Integer getKeyCodeNumber() {
    return this.keyCodeNumber;
  }

  /**
   * Returns the keycode of the key binding.
   *
   * @return the keycode
   */
  public String getCharacter() {
    return this.character;
  }

  /**
   * Whether the key binding is character based.
   *
   * @return true iff the binding uses a character
   */
  public boolean isCharacterBinding() {
    return getCharacter() != null;
  }

  /**
   * Returns the event type of the key binding.
   *
   * @return the event type
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the triggered action.
   *
   * @return the action
   */
  public KeyBindingAction getAction() {
    return this.action;
  }
}
