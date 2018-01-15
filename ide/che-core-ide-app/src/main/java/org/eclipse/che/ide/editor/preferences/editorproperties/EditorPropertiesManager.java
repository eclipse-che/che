/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.preferences.editorproperties;

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_COMPLETE_COMMENTS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_ANGLE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_BRACES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_PARENTHESES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_QUOTATIONS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_SQUARE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.EXPAND_TAB;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_ANNOTATION_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_CONTENT_ASSIST_AUTOMATICALLY;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_FOLDING_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_LINE_NUMBER_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_OCCURRENCES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_OVERVIEW_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_WHITESPACES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SHOW_ZOOM_RULER;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SMART_INDENTATION;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.SOFT_WRAP;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.TAB_SIZE;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;
import org.eclipse.che.ide.api.preferences.PreferencesManager;

/**
 * The class contains methods to simplify the work with editor properties.
 *
 * @deprecated in favor of {@link org.eclipse.che.ide.editor.preferences.EditorPreferencesManager}
 * @author Roman Nikitenko
 */
@Singleton
@Deprecated
public class EditorPropertiesManager {

  /** The editor settings property name. */
  private static final String EDITOR_SETTINGS_PROPERTY = "editorSettings";

  private static final Map<String, String> names = new HashMap<>();
  private static Map<String, JSONValue> defaultProperties;

  private PreferencesManager preferencesManager;

  @Inject
  public EditorPropertiesManager(
      EditorLocalizationConstants locale, PreferencesManager preferencesManager) {
    this.preferencesManager = preferencesManager;

    names.put(TAB_SIZE.toString(), locale.propertyTabSize());
    names.put(EXPAND_TAB.toString(), locale.propertyExpandTab());
    names.put(SOFT_WRAP.toString(), locale.propertySoftWrap());
    names.put(AUTO_PAIR_PARENTHESES.toString(), locale.propertyAutoPairParentheses());
    names.put(AUTO_PAIR_BRACES.toString(), locale.propertyAutoPairBraces());
    names.put(AUTO_PAIR_SQUARE_BRACKETS.toString(), locale.propertyAutoPairSquareBrackets());
    names.put(AUTO_PAIR_ANGLE_BRACKETS.toString(), locale.propertyAutoPairAngelBrackets());
    names.put(AUTO_PAIR_QUOTATIONS.toString(), locale.propertyAutoPairQuotations());
    names.put(AUTO_COMPLETE_COMMENTS.toString(), locale.propertyAutoCompleteComments());
    names.put(SMART_INDENTATION.toString(), locale.propertySmartIndentation());
    names.put(SHOW_WHITESPACES.toString(), locale.propertyShowWhitespaces());
    names.put(SHOW_ANNOTATION_RULER.toString(), locale.propertyShowAnnotationRuler());
    names.put(SHOW_LINE_NUMBER_RULER.toString(), locale.propertyShowLineNumberRuler());
    names.put(SHOW_FOLDING_RULER.toString(), locale.propertyShowFoldingRuler());
    names.put(SHOW_OVERVIEW_RULER.toString(), locale.propertyShowOverviewRuler());
    names.put(SHOW_ZOOM_RULER.toString(), locale.propertyShowZoomRuler());
    names.put(SHOW_OCCURRENCES.toString(), locale.propertyShowOccurrences());
    names.put(
        SHOW_CONTENT_ASSIST_AUTOMATICALLY.toString(),
        locale.propertyShowContentAssistAutomatically());
  }

  /** Returns default settings for editor */
  public static Map<String, JSONValue> getDefaultEditorProperties() {
    if (defaultProperties != null) {
      return defaultProperties;
    }
    defaultProperties = new HashMap<>();

    // TextViewOptions (tabs)
    defaultProperties.put(TAB_SIZE.toString(), new JSONNumber(4));
    defaultProperties.put(EXPAND_TAB.toString(), JSONBoolean.getInstance(true));

    // Soft wrap
    defaultProperties.put(SOFT_WRAP.toString(), JSONBoolean.getInstance(false));

    // SourceCodeActions (typing)
    defaultProperties.put(AUTO_PAIR_PARENTHESES.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(AUTO_PAIR_BRACES.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(AUTO_PAIR_SQUARE_BRACKETS.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(AUTO_PAIR_ANGLE_BRACKETS.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(AUTO_PAIR_QUOTATIONS.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(AUTO_COMPLETE_COMMENTS.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(SMART_INDENTATION.toString(), JSONBoolean.getInstance(true));

    // white spaces
    defaultProperties.put(SHOW_WHITESPACES.toString(), JSONBoolean.getInstance(false));

    // editor features (rulers)
    defaultProperties.put(SHOW_ANNOTATION_RULER.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(SHOW_LINE_NUMBER_RULER.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(SHOW_FOLDING_RULER.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(SHOW_OVERVIEW_RULER.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(SHOW_ZOOM_RULER.toString(), JSONBoolean.getInstance(true));

    // language tools
    defaultProperties.put(SHOW_OCCURRENCES.toString(), JSONBoolean.getInstance(true));
    defaultProperties.put(
        SHOW_CONTENT_ASSIST_AUTOMATICALLY.toString(), JSONBoolean.getInstance(true));

    return defaultProperties;
  }

  /**
   * Returns property name using special id. Note: method can return {@code null} if name not found.
   *
   * @param propertyId id for which name will be returned
   * @return name of the property
   */
  @Nullable
  public String getPropertyNameById(@NotNull String propertyId) {
    return names.get(propertyId);
  }

  public void storeEditorProperties(Map<String, JSONValue> editorProperties) {
    JSONObject jsonProperties = new JSONObject();
    for (String property : editorProperties.keySet()) {
      jsonProperties.put(property, editorProperties.get(property));
    }
    preferencesManager.setValue(EDITOR_SETTINGS_PROPERTY, jsonProperties.toString());
  }

  /** Returns saved settings for editor if they exist or default settings otherwise. */
  public Map<String, JSONValue> getEditorProperties() {
    String properties = preferencesManager.getValue(EDITOR_SETTINGS_PROPERTY);
    if (properties == null) {
      return getDefaultEditorProperties();
    }
    return readPropertiesFromJson(properties);
  }

  /**
   * Returns saved settings for editor in json format if they exist or default settings otherwise.
   */
  public JSONObject getJsonEditorProperties() {
    JSONObject jsonProperties = new JSONObject();

    Map<String, JSONValue> editorProperties = getEditorProperties();
    for (String property : editorProperties.keySet()) {
      jsonProperties.put(property, editorProperties.get(property));
    }
    return jsonProperties;
  }

  private static Map<String, JSONValue> readPropertiesFromJson(String jsonProperties) {
    Map<String, JSONValue> result = new HashMap<>();
    JSONValue parsed = JSONParser.parseStrict(jsonProperties);

    JSONObject jsonObj = parsed.isObject();
    if (jsonObj != null) {
      for (String key : jsonObj.keySet()) {
        JSONValue jsonValue = jsonObj.get(key);
        result.put(key, jsonValue);
      }
    }
    return result;
  }
}
