/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.texteditor;

import org.eclipse.che.ide.api.editor.keymap.KeyBinding;

/**
 * Interface for components that handles key bindings.
 */
public interface HasKeyBindings {

    /**
     * Adds a key bindings.
     *
     * @param keyBinding
     *         the new binding
     */
    void addKeyBinding(KeyBinding keyBinding);

    /**
     * Adds a key bindings.
     *
     * @param keyBinding
     *         the new binding
     * @param actionDescription
     *         action description
     */
    void addKeyBinding(KeyBinding keyBinding, String actionDescription);
}
