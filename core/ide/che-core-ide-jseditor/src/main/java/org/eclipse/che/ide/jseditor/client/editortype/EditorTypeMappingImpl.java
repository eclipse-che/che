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

import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.jseditor.client.inject.PlainTextFileType;
import org.eclipse.che.ide.jseditor.client.util.PrintMap;
import org.eclipse.che.ide.jseditor.client.util.PrintMap.Converter;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation for {@link EditorTypeMapping}.
 *
 * @author "Mickaël Leduque"
 */
public class EditorTypeMappingImpl implements EditorTypeMapping {

    /** The name of the property for the mappings in user preferences. */
    private static final String PREFERENCE_PROPERTY_NAME = "editorTypes";

    /** The known mappings. */
    private final Map<FileType, EditorType> contentTypeMapping = new HashMap<>();

    private final PreferencesManager preferencesManager;

    private final FileTypeRegistry fileTypeRegistry;
    private final FileType         unknownFileType;
    private final FileType         plainTextFileType;

    private boolean loaded = false;


    @Inject
    public EditorTypeMappingImpl(final FileTypeRegistry fileTypeRegistry,
                                 final @Named("defaultFileType") FileType unknownFileType,
                                 final @PlainTextFileType FileType plainTextFileType,
                                 final PreferencesManager preferencesManager) {
        this.fileTypeRegistry = fileTypeRegistry;
        this.unknownFileType = unknownFileType;
        this.plainTextFileType = plainTextFileType;
        this.preferencesManager = preferencesManager;
    }

    @Override
    public void setEditorType(final FileType contentType, final EditorType editorType) {
        ensureLoaded();
        this.contentTypeMapping.put(contentType, editorType);
        Log.debug(EditorTypeMappingImpl.class, "Mappings added - new mappings:\n" + printMapping());
    }

    @Override
    public EditorType getEditorType(final FileType contentType) {
        ensureLoaded();
        final EditorType search = this.contentTypeMapping.get(contentType);
        if (search != null) {
            return search;
        } else {
            Log.debug(EditorTypeMappingImpl.class, "No editor type mapping for " + contentType.getContentDescription()
                                                   + " - trying with text/plain.");
            // try to fall back to text/plain
            final EditorType searchTextPlain = this.contentTypeMapping.get(plainTextFileType);
            if (searchTextPlain != null) {
                return searchTextPlain;
            } else {
                Log.debug(EditorTypeMappingImpl.class, "Falling back to default editor impl.");
                // fall back to default editor
                return EditorType.getDefaultEditorType();
            }
        }
    }

    @Override
    public void loadFromPreferences() {
        final String pref = this.preferencesManager.getValue(PREFERENCE_PROPERTY_NAME);
        if (pref != null && !pref.isEmpty()) {
            final JSONObject keyMapping = JSONParser.parseStrict(pref).isObject();
            this.contentTypeMapping.clear();
            for (final String key : keyMapping.keySet()) {
                // the mime-type is stored in preferences
                final String contentType = key;
                final JSONValue value = keyMapping.get(key);
                if (value == null) {
                    Log.warn(EditorTypeMappingImpl.class,
                             "Error in preferences: filetype " + contentType + " has null editor type set.");
                    continue;
                }
                final String stringValue = value.isString().stringValue();
                final EditorType editorType = EditorType.getInstance(stringValue);
                if (editorType != null) {
                    // special case for text/plain <-> defaultPlainTextFileType
                    if (CONTENT_TYPE_TEXT_PLAIN.equals(contentType)) {
                        this.contentTypeMapping.put(plainTextFileType, editorType);
                    } else {
                        final FileType fileType = fileTypeRegistry.getFileTypeByMimeType(contentType);
                        // any unknown mime type returns the default filetype ; ignore them
                        if (!unknownFileType.equals(fileType)) {
                            this.contentTypeMapping.put(fileType, editorType);
                        }
                    }
                } else {
                    Log.warn(EditorTypeMappingImpl.class,
                             "Unknown editor type key found for filetype " + contentType + ": " + stringValue);
                }
            }
        } else {
            Log.debug(EditorTypeMappingImpl.class, "No editor type mappings found in preferences.");
        }
    }

    @Override
    public void storeInPreferences() {
        final JSONObject keyMapping = new JSONObject();
        for (final Entry<FileType, EditorType> entry : this.contentTypeMapping.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                final FileType fileType = entry.getKey();
                // special case for text/plain <-> defaultPlainTextFileType
                if (fileType.equals(plainTextFileType)) {
                    keyMapping.put(CONTENT_TYPE_TEXT_PLAIN, new JSONString(entry.getValue().getEditorTypeKey()));
                } else {
                    final List<String> mimeTypes = fileType.getMimeTypes();
                    if (mimeTypes != null && mimeTypes.size() > 0) {
                        final String firstMimeType = mimeTypes.get(0);
                        if (firstMimeType != null) {
                            keyMapping.put(firstMimeType, new JSONString(entry.getValue().getEditorTypeKey()));
                        }
                    }
                }
            }
        }

        final String pref = keyMapping.toString();
        Log.debug(EditorTypeMappingImpl.class, "Storing editor type mappings in prefs - " + pref);
        this.preferencesManager.setValue(PREFERENCE_PROPERTY_NAME, pref);
    }

    @Override
    public Iterator<Entry<FileType, EditorType>> iterator() {
        ensureLoaded();
        return this.contentTypeMapping.entrySet().iterator();
    }

    private void ensureLoaded() {
        if (!loaded) {
            loaded = true;
            Log.debug(EditorTypeMappingImpl.class, "Mappings have not yet been read from preferences - doing it.");
            loadFromPreferences();
            Log.debug(EditorTypeMappingImpl.class, "Mappings loaded - obtained:\n" + printMapping());
        }
    }

    private String printMapping() {
        return PrintMap.printMap(this.contentTypeMapping, new Converter<FileType>() {
            @Override
            public String convert(final FileType item) {
                final StringBuilder sb = new StringBuilder("[");
                if (item.getContentDescription() != null) {
                    sb.append(item.getContentDescription());
                } else {
                    sb.append("-");
                }
                sb.append("|");
                if (item.getMimeTypes() != null) {
                    String separator = "";
                    for (final String s : item.getMimeTypes()) {
                        sb.append(separator);
                        sb.append(s);
                        separator = " ";
                    }
                } else {
                    sb.append("-");
                }
                sb.append("|");
                if (item.getExtension() != null) {
                    sb.append(item.getExtension());
                } else {
                    sb.append("-");
                }
                sb.append("]");
                return sb.toString();
            }
        });
    }
}
