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

import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import java.util.Map.Entry;

/**
 * Utility class to read and store keymap preferences.
 *
 * @author "Mickaël Leduque"
 */
public class KeymapPrefReader {

    private static final String KEYMAP_PREF_KEY = "keymap";

    /**
     * Reads the keymap preference for the given editor.
     *
     * @param preferencesManager
     *         the preferences manager
     * @param editorKey
     *         the editor key
     * @return the keymap in preference or null if none is set
     */
    public static String readPref(final PreferencesManager preferencesManager, final String editorKey) {
        final String keymapPrefAsJson = preferencesManager.getValue(KEYMAP_PREF_KEY);
        if (keymapPrefAsJson == null || keymapPrefAsJson.isEmpty()) {
            return null;
        }
        JSONValue propertyObject;
        try {
            final JSONValue parseResult = JSONParser.parseStrict(keymapPrefAsJson);
            propertyObject = parseResult.isObject().get(editorKey);
        } catch (final RuntimeException e) {
            Log.error(KeymapPrefReader.class, "Error during preference parsing.", e);
            return null;
        }
        if (propertyObject == null) {
            return null;
        }
        String propertyValue;
        try {
            propertyValue = propertyObject.isString().stringValue();
        } catch (final RuntimeException e) {
            Log.error(KeymapPrefReader.class, "Invalid value for keymap preference.", e);
            return null;
        }
        return propertyValue;
    }

    /**
     * Reads the keymap preferences and fills the {@link KeymapValuesHolder} instance.
     *
     * @param preferencesManager
     *         the preferences manager
     * @param valuesHolder
     *         the object that keeps the values
     */
    public static void readPref(final PreferencesManager preferencesManager, final KeymapValuesHolder valuesHolder) {
        final String keymapPrefAsJson = preferencesManager.getValue(KEYMAP_PREF_KEY);
        if (keymapPrefAsJson == null || keymapPrefAsJson.isEmpty()) {
            return;
        }
        JSONObject propertyObject;
        try {
            final JSONValue parseResult = JSONParser.parseStrict(keymapPrefAsJson);
            propertyObject = parseResult.isObject();
        } catch (final RuntimeException e) {
            Log.error(KeymapPrefReader.class, "Error during preference parsing.", e);
            return;
        }
        for (final String key : propertyObject.keySet()) {
            final JSONValue value = propertyObject.get(key);
            if (value == null) {
                continue;
            }
            String valueString = null;
            try {
                valueString = value.isString().stringValue();
            } catch (final ClassCastException e) {
                Log.warn(KeymapPrefReader.class, "Incorrect value type for keymap preference for editor " + key + ": " + value);
                continue;
            }
            if (valueString != null) {
                EditorType editorType = null;
                Keymap keymap = null;
                try {
                    editorType = EditorType.fromKey(key);
                    keymap = Keymap.fromKey(valueString);
                } catch (final RuntimeException e) {
                    Log.error(KeymapPrefReader.class, "Invalid value for keymap preference.", e);
                    continue;
                }
                if (editorType != null && keymap != null) {
                    valuesHolder.setKeymap(editorType, keymap);
                }
            }
        }
    }

    /**
     * Updates the keymap in preferences.
     *
     * @param preferencesManager
     *         the preferences manager
     * @param valuesHolder
     *         the object that contains the values to store
     */
    public static void storePrefs(final PreferencesManager preferencesManager, final KeymapValuesHolder valuesHolder) {
        final String keymapPrefAsJson = preferencesManager.getValue(KEYMAP_PREF_KEY);

        JSONObject prefObject;
        if (keymapPrefAsJson == null) {
            prefObject = new JSONObject();
        } else {
            final JSONValue parseResult = JSONParser.parseStrict(keymapPrefAsJson);
            prefObject = parseResult.isObject();
        }

        for (final Entry<EditorType, Keymap> entry : valuesHolder) {
            if (entry.getKey() != null && entry.getValue() != null) {
                prefObject.put(entry.getKey().getEditorTypeKey(), new JSONString(entry.getValue().getKey()));
            }
        }

        final String newJson = prefObject.toString();
        preferencesManager.setValue(KEYMAP_PREF_KEY, newJson);
    }
}
