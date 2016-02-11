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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.che.ide.jseditor.client.keymap.Keybinding;

/** Hold {@link org.eclipse.che.ide.jseditor.client.keymap.Keybinding} until the editor is ready to accept them. */
public class TemporaryKeybindingsManager implements HasKeybindings {

    private final List<Keybinding> bindings = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public void addKeybinding(final Keybinding keybinding) {
        this.bindings.add(keybinding);
    }

    /** {@inheritDoc} */
    @Override
    public void addKeybinding(Keybinding keybinding, String actionDescription) {
        this.bindings.add(keybinding);
    }

    /** {@inheritDoc} */
    public List<Keybinding> getbindings() {
        return this.bindings;
    }
}
