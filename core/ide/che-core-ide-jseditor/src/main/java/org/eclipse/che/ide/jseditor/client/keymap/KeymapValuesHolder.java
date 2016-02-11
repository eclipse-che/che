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

import org.eclipse.che.ide.jseditor.client.editortype.EditorType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A store for editor to keymap mappings.
 *
 * @author "Mickaël Leduque"
 */
public class KeymapValuesHolder implements Iterable<Entry<EditorType, Keymap>> {

    /** the actual mapping. */
    private final Map<EditorType, Keymap> values = new HashMap<>();

    /**
     * Sets-up a keymap association for the editor type.
     *
     * @param editorType
     *         the editor type
     * @param newValue
     *         the new keymap
     */
    public void setKeymap(final EditorType editorType, final Keymap newValue) {
        this.values.put(editorType, newValue);
    }

    /**
     * Returns the keymap association for the editor type.
     *
     * @param editorType
     *         the editor type
     * @return the associated keymap or null
     */
    public Keymap getKeymap(final EditorType editorType) {
        return this.values.get(editorType);
    }

    public Map<EditorType, Keymap> getValues() {
        return values;
    }

    @Override
    public Iterator<Entry<EditorType, Keymap>> iterator() {
        return this.values.entrySet().iterator();
    }
}
