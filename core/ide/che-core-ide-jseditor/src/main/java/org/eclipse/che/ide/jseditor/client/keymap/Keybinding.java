/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jseditor.client.keymap;

/**
 * Description of a key binding.
 */
public class Keybinding {
    /** Is the control key pressed? */
    private final boolean control;
    /** Is the shift key pressed? */
    private final boolean shift;
    /** Is the alt key pressed? */
    private final boolean alt;
    /** Is the cmdkey pressed? */
    private final boolean cmd;
    /** The key code. */
    private final int keycode;
    /** The action taken on the key binding. */
    private final KeyBindingAction action;

    public Keybinding(final boolean control, final boolean shift, final boolean alt,
                      final boolean cmd, final int keycode, final KeyBindingAction action) {
        this.control = control;
        this.shift = shift;
        this.alt = alt;
        this.cmd = cmd;
        this.keycode = keycode;
        this.action = action;
    }

    /**
     * Whether the control key is hold during the key binding.
     * @return true iff the binding uses control
     */
    public boolean isControl() {
        return this.control;
    }

    /**
     * Whether the shift key is hold during the key binding.
     * @return true iff the binding uses shift
     */
    public boolean isShift() {
        return this.shift;
    }

    /**
     * Whether the alt key is hold during the key binding.
     * @return true iff the binding uses alt
     */
    public boolean isAlt() {
        return this.alt;
    }

    /**
     * Whether the cmd key is hold during the key binding.
     * @return true iff the binding uses cmd
     */
    public boolean isCmd() {
        return this.cmd;
    }

    /**
     * Returns the keycode of the bey binding.
     * @return the keycode
     */
    public int getKeyCode() {
        return this.keycode;
    }

    /**
     * Returns the triggered action.
     * @return the action
     */
    public KeyBindingAction getAction() {
        return this.action;
    }
}
