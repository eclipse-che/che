/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor.preferences;

import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_COMPLETE_COMMENTS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_ANGLE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_BRACES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_PARENTHESES;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_QUOTATIONS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.AUTO_PAIR_SQUARE_BRACKETS;
import static org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties.ENABLE_AUTO_SAVE;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.editor.EditorLocalizationConstants;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.editor.preferences.editorproperties.EditorProperties;

/**
 * The class contains methods to simplify the work with editor preferences.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class EditorPreferencesManager {

  /** The editor settings property name. */
  private static final String EDITOR_PREFERENCES_PROPERTY = "editorSettings";

  private static final Map<String, String> names = new HashMap<>();
  private static Map<String, JSONValue> defaultPreferences;

  private final PreferencesManager preferencesManager;

  @Inject
  public EditorPreferencesManager(
      EditorLocalizationConstants locale, PreferencesManager preferencesManager) {
    this.preferencesManager = preferencesManager;

    names.put(TAB_SIZE.toString(), locale.propertyTabSize());
    names.put(EXPAND_TAB.toString(), locale.propertyExpandTab());
    names.put(AUTO_PAIR_PARENTHESES.toString(), locale.propertyAutoPairParentheses());
    names.put(AUTO_PAIR_BRACES.toString(), locale.propertyAutoPairBraces());
    names.put(AUTO_PAIR_SQUARE_BRACKETS.toString(), locale.propertyAutoPairSquareBrackets());
    names.put(AUTO_PAIR_ANGLE_BRACKETS.toString(), locale.propertyAutoPairAngelBrackets());
    names.put(AUTO_PAIR_QUOTATIONS.toString(), locale.propertyAutoPairQuotations());
    names.put(AUTO_COMPLETE_COMMENTS.toString(), locale.propertyAutoCompleteComments());
    names.put(SMART_INDENTATION.toString(), locale.propertySmartIndentation());
    names.put(SHOW_WHITESPACES.toString(), locale.propertyShowWhitespaces());
    names.put(ENABLE_AUTO_SAVE.toString(), locale.propertyAutoSave());
    names.put(SOFT_WRAP.toString(), locale.propertySoftWrap());
    names.put(SHOW_ANNOTATION_RULER.toString(), locale.propertyShowAnnotationRuler());
    names.put(SHOW_LINE_NUMBER_RULER.toString(), locale.propertyShowLineNumberRuler());
    names.put(SHOW_FOLDING_RULER.toString(), locale.propertyShowFoldingRuler());
    names.put(SHOW_OVERVIEW_RULER.toString(), locale.propertyShowOverviewRuler());
    names.put(SHOW_ZOOM_RULER.toString(), locale.propertyShowZoomRuler());
    names.put(SHOW_OCCURRENCES.toString(), locale.propertyShowOccurrences());
    names.put(
        SHOW_CONTENT_ASSIST_AUTOMATICALLY.toString(),
        locale.propertyShowContentAssistAutomatically());

    getDefaultEditorPreferences();
  }

  /** Returns default editor preferences */
  public static Map<String, JSONValue> getDefaultEditorPreferences() {
    if (defaultPreferences != null) {
      return defaultPreferences;
    }
    defaultPreferences = new HashMap<>();

    // TextViewOptions (tabs)
    defaultPreferences.put(TAB_SIZE.toString(), new JSONNumber(4));
    defaultPreferences.put(EXPAND_TAB.toString(), JSONBoolean.getInstance(true));

    // Edit
    defaultPreferences.put(ENABLE_AUTO_SAVE.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(SOFT_WRAP.toString(), JSONBoolean.getInstance(false));

    // SourceCodeActions (typing)
    defaultPreferences.put(AUTO_PAIR_PARENTHESES.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(AUTO_PAIR_BRACES.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(AUTO_PAIR_SQUARE_BRACKETS.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(AUTO_PAIR_ANGLE_BRACKETS.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(AUTO_PAIR_QUOTATIONS.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(AUTO_COMPLETE_COMMENTS.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(SMART_INDENTATION.toString(), JSONBoolean.getInstance(true));

    // white spaces
    defaultPreferences.put(SHOW_WHITESPACES.toString(), JSONBoolean.getInstance(false));

    // editor features (rulers)
    defaultPreferences.put(SHOW_ANNOTATION_RULER.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(SHOW_LINE_NUMBER_RULER.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(SHOW_FOLDING_RULER.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(SHOW_OVERVIEW_RULER.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(SHOW_ZOOM_RULER.toString(), JSONBoolean.getInstance(true));

    // language tools
    defaultPreferences.put(SHOW_OCCURRENCES.toString(), JSONBoolean.getInstance(true));
    defaultPreferences.put(
        SHOW_CONTENT_ASSIST_AUTOMATICALLY.toString(), JSONBoolean.getInstance(true));

    return defaultPreferences;
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

  public void storeEditorPreferences(Map<String, JSONValue> editorPreferences) {
    JSONObject jsonPreferences = new JSONObject();

    editorPreferences
        .keySet()
        .forEach(property -> jsonPreferences.put(property, editorPreferences.get(property)));

    preferencesManager.setValue(EDITOR_PREFERENCES_PROPERTY, jsonPreferences.toString());
  }

  /** Returns saved preferences for editor if they exist or default preferences otherwise. */
  public Map<String, JSONValue> getEditorPreferences() {
    String jsonPreferences = preferencesManager.getValue(EDITOR_PREFERENCES_PROPERTY);
    if (jsonPreferences == null) {
      return defaultPreferences;
    }

    Map<String, JSONValue> savedPreferences = readPreferencesFromJson(jsonPreferences);
    defaultPreferences
        .keySet()
        .stream()
        .filter(property -> !savedPreferences.containsKey(property))
        .forEach(property -> savedPreferences.put(property, defaultPreferences.get(property)));
    return savedPreferences;
  }

  /**
   * Returns saved editor preferences if they exist or default preferences otherwise for given set
   * properties.
   */
  public Map<String, JSONValue> getEditorPreferencesFor(EnumSet<EditorProperties> filter) {
    Map<String, JSONValue> editorPreferences = getEditorPreferences();
    Map<String, JSONValue> result = new HashMap<>(filter.size());

    for (EditorProperties property : filter) {
      String key = property.toString();
      if (editorPreferences.containsKey(key)) {
        result.put(key, editorPreferences.get(key));
      }
    }
    return result;
  }

  /**
   * Returns all saved preferences for editor in json format if they exist or default preferences
   * otherwise.
   */
  public JSONObject getJsonEditorPreferences() {
    JSONObject jsonPreferences = new JSONObject();
    Map<String, JSONValue> editorPreferences = getEditorPreferences();

    editorPreferences
        .keySet()
        .forEach(property -> jsonPreferences.put(property, editorPreferences.get(property)));

    return jsonPreferences;
  }

  /**
   * Returns saved editor preferences in json format if they exist or default preferences otherwise
   * for given set properties.
   */
  public JSONObject getJsonEditorPreferencesFor(EnumSet<EditorProperties> filter) {
    JSONObject jsonPreferences = new JSONObject();
    Map<String, JSONValue> editorPreferences = getEditorPreferences();

    for (EditorProperties property : filter) {
      String key = property.toString();
      if (editorPreferences.containsKey(key)) {
        jsonPreferences.put(key, editorPreferences.get(key));
      }
    }
    return jsonPreferences;
  }

  public JSONValue getJsonValueFor(EditorProperties property) {
    return property != null ? getEditorPreferences().get(property.toString()) : null;
  }

  public Integer getNumberValueFor(EditorProperties property) {
    JSONValue jsonValue = getJsonValueFor(property);
    if (jsonValue == null) {
      return null;
    }

    JSONNumber jsonNumber = jsonValue.isNumber();
    if (jsonNumber == null) {
      return null;
    }

    Double result = jsonNumber.doubleValue();
    return result.intValue();
  }

  public Boolean getBooleanValueFor(EditorProperties property) {
    JSONValue jsonValue = getJsonValueFor(property);
    if (jsonValue == null) {
      return null;
    }

    JSONBoolean jsonBoolean = jsonValue.isBoolean();
    if (jsonBoolean == null) {
      return null;
    }
    return jsonBoolean.booleanValue();
  }

  private static Map<String, JSONValue> readPreferencesFromJson(String jsonPreferences) {
    Map<String, JSONValue> result = new HashMap<>();
    JSONValue parsed = JSONParser.parseStrict(jsonPreferences);

    JSONObject jsonObj = parsed.isObject();
    if (jsonObj != null) {
      jsonObj.keySet().forEach(key -> result.put(key, jsonObj.get(key)));
    }
    return result;
  }
}
