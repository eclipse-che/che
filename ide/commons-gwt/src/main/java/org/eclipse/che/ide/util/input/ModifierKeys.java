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

import elemental.events.KeyboardEvent;

import org.eclipse.che.ide.util.browser.UserAgent;


/** Modifier key constants, safe to be ORed together. */
public final class ModifierKeys {

    public static final int NONE = 0;

    /**
     * This is an abstraction for the primary modifier used for chording shortcuts
     * in Collide. To stay consistent with native OS shortcuts, this will be set
     * if CTRL is pressed on Linux or Windows, or if CMD is pressed on Mac.
     */
    public static final int ACTION = 1;

    public static final int ALT = 1 << 1;

    public static final int SHIFT = 1 << 2;

    /**
     * This will only be set on Mac. (On Windows and Linux, the
     * {@link ModifierKeys#ACTION} will be set instead.)
     */
    public static final int CTRL = 1 << 3;

    private ModifierKeys() {
        // Do nothing
    }

    /**
     * Like {@link #computeExactModifiers(KeyboardEvent)} except computes the
     * shift bit depending on {@link KeyCodeMap#needsShift(int)}.
     */
    public static int computeModifiers(SignalEvent event) {
        int modifiers = computeModifiersExceptShift(event.getMetaKey(), event.getCtrlKey(), event.getAltKey());

        // Only add shift if it isn't changing the charCode (lower to upper case).
        int keyCode = KeyCodeMap.getKeyFromEvent(event);
        if (event.getShiftKey() && !KeyCodeMap.needsShift(keyCode)) {
            modifiers |= SHIFT;
        }

        return modifiers;
    }

    /**
     * Returns an integer with the modifier bits set based on whether the modifier
     * appears in the given event. Unlike {@link #computeModifiers(SignalEvent)},
     * this does a literal translation of the shift key using
     * {@link KeyboardEvent#isShiftKey()} instead of going through our custom
     * {@link KeyCodeMap}.
     */
    public static int computeExactModifiers(KeyboardEvent event) {
        int modifiers = computeModifiersExceptShift(event.isMetaKey(), event.isCtrlKey(), event.isAltKey());
        if (event.isShiftKey()) {
            modifiers |= SHIFT;
        }

        return modifiers;
    }

    private static int computeModifiersExceptShift(boolean hasMeta, boolean hasCtrl, boolean hasAlt) {
        int modifiers = 0;

        if (hasAlt) {
            modifiers |= ALT;
        }

        if (UserAgent.isMac() && hasCtrl) {
            modifiers |= CTRL;
        }

        if (hasAction(hasCtrl, hasMeta)) {
            modifiers |= ACTION;
        }

        return modifiers;
    }

    private static boolean hasAction(boolean hasCtrl, boolean hasMeta) {
        return UserAgent.isMac() ? hasMeta : hasCtrl;
    }
}
