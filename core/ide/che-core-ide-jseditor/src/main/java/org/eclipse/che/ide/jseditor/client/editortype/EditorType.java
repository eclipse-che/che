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
package org.eclipse.che.ide.jseditor.client.editortype;

import org.eclipse.che.ide.util.loging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Value object for editor types.
 *
 * @author "Mickaël Leduque"
 */
public final class EditorType {

    /** The key for the default "classic" editor. */
    public static final String DEFAULT_EDITOR_KEY = "codemirror";

    /** The editor type key. */
    private final String editorTypeKey;


    /** The already built instances. */
    private static Map<String, EditorType> instances = new HashMap<>();

    private EditorType(final String key) {
        editorTypeKey = key;
    }

    /**
     * Retrieve an editor type instance by its key.
     *
     * @param key
     *         the editor type key
     * @return the {@link EditorType} instance
     */
    public static EditorType fromKey(final String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        final EditorType search = instances.get(key);
        if (search != null) {
            return search;
        } else {
            Log.debug(EditorType.class, "Creating new EditorType instance, key=" + key);
            final EditorType result = new EditorType(key);
            instances.put(key, result);
            return result;
        }
    }

    /**
     * Returns the editor type key.
     *
     * @return the editor type key.
     */
    public String getEditorTypeKey() {
        return editorTypeKey;
    }

    /**
     * Returns the default editor type.
     *
     * @return the default editor type.
     */
    public static EditorType getDefaultEditorType() {
        return fromKey(DEFAULT_EDITOR_KEY);
    }

    /**
     * Return all created instances.<br>
     * The list is a copy of the real one, modifying it does nothing.
     *
     * @return the instances
     */
    public static List<EditorType> getInstances() {
        return new ArrayList<>(instances.values());
    }

    /**
     * Return the instance with the given key.
     *
     * @return the instance with that key or null if there isn't one
     */
    public static EditorType getInstance(final String key) {
        return instances.get(key);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((editorTypeKey == null) ? 0 : editorTypeKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EditorType other = (EditorType)obj;
        if (editorTypeKey == null) {
            if (other.editorTypeKey != null) {
                return false;
            }
        } else if (!editorTypeKey.equals(other.editorTypeKey)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return editorTypeKey;
    }
}
