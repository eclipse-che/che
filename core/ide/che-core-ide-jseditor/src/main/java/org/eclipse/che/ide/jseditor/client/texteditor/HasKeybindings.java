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
package org.eclipse.che.ide.jseditor.client.texteditor;

import org.eclipse.che.ide.jseditor.client.keymap.Keybinding;

/**
 * Interface for components that handles key bindings.
 */
public interface HasKeybindings {

    /**
     * Adds a key bindings.
     *
     * @param keybinding
     *         the new binding
     */
    void addKeybinding(Keybinding keybinding);

    /**
     * Adds a key bindings.
     *
     * @param keybinding
     *         the new binding
     * @param actionDescription
     *         action description
     */
    void addKeybinding(Keybinding keybinding, String actionDescription);
}
