// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.input;

import org.eclipse.che.ide.runtime.Assert;

/**
 * Bean that holds information describing the matching key-press.
 *
 * <p>
 *
 * <p>NOTE: Do not include {@link ModifierKeys#SHIFT} for upper case characters (A,%,?), only for
 * combinations like SHIFT+TAB.
 */
public class CharCodeWithModifiers {

  private final int modifiers;

  private final int charCode;

  private final int digest;

  public CharCodeWithModifiers(int modifiers, int charCode) {
    Assert.isTrue(
        !KeyCodeMap.needsShift(charCode) || (modifiers & ModifierKeys.SHIFT) == 0,
        "Do not include ModifierKeys.SHIFT for EventShortcuts where the "
            + "key pressed could be modified by pressing shift.");
    this.modifiers = modifiers;
    this.charCode = charCode;
    this.digest = computeKeyDigest(modifiers, charCode);
  }

  public int getModifiers() {
    return modifiers;
  }

  public int getCharCode() {
    return charCode;
  }

  public int getKeyDigest() {
    return digest;
  }

  public static int computeKeyDigest(int modifiers, int charCode) {
    return (modifiers << 16) | (0xFFFF & charCode);
  }

  /**
   * Returns an integer representing the combination of pressed modifier keys and the current text
   * key.
   *
   * @see ModifierKeys#ACTION for details on the action key abstraction
   */
  public static int computeKeyDigest(SignalEvent event) {
    return computeKeyDigest(
        ModifierKeys.computeModifiers(event), KeyCodeMap.getKeyFromEvent(event));
  }
}
