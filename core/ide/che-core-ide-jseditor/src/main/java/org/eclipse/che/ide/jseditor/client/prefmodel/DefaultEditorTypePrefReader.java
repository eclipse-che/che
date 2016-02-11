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

import static org.eclipse.che.ide.jseditor.client.JsEditorExtension.DEFAULT_EDITOR_TYPE_INSTANCE;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.util.loging.Log;

/** Utilisty to read and store default editor preference. */
public class DefaultEditorTypePrefReader {

    /** The 'defaut default' editor, used when there is no preference set. */
    private final EditorType             defaultEditorType;
    /** The component used to read/store the editor preferences. */
    private final EditorPreferenceReader preferenceReader;

    @Inject
    public DefaultEditorTypePrefReader(final EditorPreferenceReader preferenceReader,
                                       final @Named(DEFAULT_EDITOR_TYPE_INSTANCE) EditorType defaultEditorType) {
        this.defaultEditorType = defaultEditorType;
        this.preferenceReader = preferenceReader;
    }

    /**
     * Read the default editor type value from the preferences.
     * @return the default editor type
     */
    public EditorType readPref() {
        final EditorPreferences editorPreferences = this.preferenceReader.getPreferences();
        return readPref(editorPreferences);
    }

    /**
     * Read the default editor type value from the given editor preferences object.
     * @param editorPreferences the preferences object
     * @return the default editor type
     */
    public EditorType readPref(final EditorPreferences editorPreferences) {
        if (editorPreferences == null || editorPreferences.getDefaultEditor() == null) {
            return this.defaultEditorType;
        }
        final String editorKey = editorPreferences.getDefaultEditor();
        final EditorType editorType = EditorType.getInstance(editorKey);
        if (editorType != null) {
            return editorType;
        } else {
            return this.defaultEditorType;
        }
    }

    /**
     * Store the default editor type value in the preferences
     * @param editorType the new editor type
     */
    public void storePref(final EditorType editorType) {
        final EditorPreferences editorPreferences = this.preferenceReader.getPreferences();
        storePref(editorPreferences, editorType);
    }

    /**
     * Store the default editor type value in the given editor preferences object.
     * @param editorPreferences the preferences object
     * @param editorType the new editor type
     */
    public void storePref(final EditorPreferences editorPreferences,
                          final EditorType editorType) {
        if (editorType == null) {
            Log.warn(DefaultEditorTypePrefReader.class, "Attempt to set 'null' for default editor - won't comply.");
            return;
        }

        EditorPreferences usedPreferences = editorPreferences;
        if (usedPreferences == null) {
            usedPreferences = EditorPreferences.create();
        }
        usedPreferences.setDefaultEditor(editorType.getEditorTypeKey());

        this.preferenceReader.setPreferences(usedPreferences);
    }
}
