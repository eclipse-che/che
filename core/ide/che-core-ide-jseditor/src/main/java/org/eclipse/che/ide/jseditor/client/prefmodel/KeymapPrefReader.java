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
package org.eclipse.che.ide.jseditor.client.prefmodel;

import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.keymap.Keymap;
import org.eclipse.che.ide.jseditor.client.keymap.KeymapValuesHolder;

import elemental.js.util.JsArrayOfString;
import elemental.js.util.JsMapFromStringToString;

/** Utility class to read and store keymap preferences. */
public class KeymapPrefReader {

    /** The editor preferences reader. */
    private final EditorPreferenceReader preferenceReader;

    @Inject
    public KeymapPrefReader(final EditorPreferenceReader preferenceReader) {
        this.preferenceReader = preferenceReader;
    }

    /**
     * Reads the keymap preference for the given editor.
     * @param editorKey the editor key
     * @return the keymap in preference or null if none is set
     */
    public String readPref(final String editorKey) {
        final EditorPreferences editorPreferences = this.preferenceReader.getPreferences();
        if (editorPreferences.getKeymaps() == null) {
            return null;
        }
        return editorPreferences.getKeymaps().get(editorKey);
    }

    /**
     * Reads the keymap preferences and fills the {@link org.eclipse.che.ide.jseditor.client.keymap.KeymapValuesHolder} instance.
     * @param valuesHolder the object that keeps the values
     */
    public void readPref(final KeymapValuesHolder valuesHolder) {
        final EditorPreferences editorPreferences = preferenceReader.getPreferences();
        readPref(editorPreferences, valuesHolder);
    }

    /**
     * Reads the keymap preferences and fills the {@link KeymapValuesHolder} instance using an already known
     * {@link EditorPreferences} instance.
     * @param valuesHolder the object that keeps the values
     */
    public void readPref(final EditorPreferences editorPreferences,
                         final KeymapValuesHolder valuesHolder) {
        if (editorPreferences == null || editorPreferences.getKeymaps() == null) {
            return;
        }
        final JsMapFromStringToString keymaps = editorPreferences.getKeymaps();
        final JsArrayOfString entries = keymaps.keys();

        for (int i = 0; i < entries.length(); i++) {
            final String key = entries.get(i);
            final String value = keymaps.get(key);
            if (value == null) {
                continue;
            }

            EditorType editorType = null;
            Keymap keymap = null;
            
            editorType = EditorType.getInstance(key);
            keymap = Keymap.fromKey(value);

            if (editorType != null && keymap != null) {
                valuesHolder.setKeymap(editorType, keymap);
            }
        }
    }

    /**
     * Updates the keymap in preferences.
     * @param valuesHolder the object that contains the values to store
     */
    public void storePrefs(final KeymapValuesHolder valuesHolder) {
        final EditorPreferences preferences = this.preferenceReader.getPreferences();
        final JsMapFromStringToString keymaps = preferences.getKeymaps();

        for (final Entry<EditorType, Keymap> entry : valuesHolder) {
            if (entry.getKey() != null && entry.getValue() != null) {
                keymaps.put(entry.getKey().getEditorTypeKey(), entry.getValue().getKey());
            }
        }

        this.preferenceReader.setPreferences(preferences);
    }
}
