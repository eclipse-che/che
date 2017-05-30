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
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.core.shared.GWT;

import org.eclipse.che.ide.api.editor.keymap.Keymap;

/**
 * Keymaps supported by Orion.
 *
 * @author "Mickaël Leduque"
 */
public class KeyMode {

    public static Keymap DEFAULT;
    public static Keymap EMACS;
    public static Keymap VI;

    public final static void init() {
        KeymodeDisplayConstants constants = GWT.create(KeymodeDisplayConstants.class);
        DEFAULT = Keymap.newKeymap("orion_default", constants.defaultKeymap());
        EMACS = Keymap.newKeymap("Orion_emacs", constants.emacs());
        VI = Keymap.newKeymap("Orion_vim", constants.vi());
    }
}
