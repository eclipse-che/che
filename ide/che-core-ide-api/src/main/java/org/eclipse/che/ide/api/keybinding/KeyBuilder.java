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
package org.eclipse.che.ide.api.keybinding;

import org.eclipse.che.ide.util.input.CharCodeWithModifiers;
import org.eclipse.che.ide.util.input.ModifierKeys;

/**
 * A builder for {@link CharCodeWithModifiers}. It's simplify creating CharCodeWithModifiers object.
 *
 * @author <a href="mailto:evidolob@exoplatform.com">Evgen Vidolob</a>
 * @version $Id:
 */
public class KeyBuilder {
  private int modifiers;

  private int charCode;

  public KeyBuilder() {}

  /**
   * Add ACTION modifier. Action is abstraction for the primary modifier used for chording shortcuts
   * in IDE. To stay consistent with native OS shortcuts, this will be set if CTRL is pressed on
   * Linux or Windows, or if CMD is pressed on Mac.
   *
   * @return the KeyBuilder with action modifier added
   */
  public KeyBuilder action() {
    modifiers |= ModifierKeys.ACTION;
    return this;
  }

  /**
   * Add ALT modifier
   *
   * @return the KeyBuilder with ALT modifier added
   */
  public KeyBuilder alt() {
    modifiers |= ModifierKeys.ALT;
    return this;
  }

  /**
   * Add CTRL modifier. <b> This will only be set on Mac. (On Windows and Linux, the {@link
   * KeyBuilder#action()} will be set instead.) </b>
   *
   * @return the KeyBuilder with CTRL modifier added
   */
  public KeyBuilder control() {
    modifiers |= ModifierKeys.CTRL;
    return this;
  }

  /**
   * Add SHIFT modifier.
   *
   * @return the KeyBuilder with SHIFT modifier added
   */
  public KeyBuilder shift() {
    modifiers |= ModifierKeys.SHIFT;
    return this;
  }

  /**
   * Key binding has no modifier keys.
   *
   * @return the KeyBuilder with NONE modifier added
   */
  public KeyBuilder none() {
    modifiers = ModifierKeys.NONE;
    return this;
  }

  /**
   * Set char code
   *
   * @param charCode the code of the character. If the character is upper-case it means that
   *     KeyBuilder will be with the SHIFT modifier.
   * @return the KeyBuilder with char code added
   */
  public KeyBuilder charCode(int charCode) {
    this.charCode = charCode;
    return this;
  }

  /**
   * Build CharCodeWithModifiers object.
   *
   * @return new CharCodeWithModifiers object.
   */
  public CharCodeWithModifiers build() {
    return new CharCodeWithModifiers(modifiers, charCode);
  }
}
