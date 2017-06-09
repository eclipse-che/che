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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyModeOverlay;
import org.eclipse.che.ide.api.editor.keymap.Keymap;

/**
 * An instance repository for keymode instances.
 * 
 * @author "Mickaël Leduque"
 */
public class KeyModeInstances {

    private Map<Keymap, OrionKeyModeOverlay> instances = new HashMap<>();

    public void add(final Keymap key, final OrionKeyModeOverlay instance) {
        this.instances.put(key, instance);
    }

    public OrionKeyModeOverlay getInstance(final Keymap keymap) {
        return instances.get(keymap);
    }
}
