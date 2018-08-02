/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.yaml.ide.preferences;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.eclipse.che.plugin.yaml.ide.YamlLocalizationConstant;
import org.eclipse.che.plugin.yaml.ide.YamlServiceClient;
import org.eclipse.che.plugin.yaml.shared.PreferenceHelper;
import org.eclipse.che.plugin.yaml.shared.YamlPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The presenter for managing the YamlPreferenceCellTable in YamlExtensionManagerView.
 *
 * @author Joshua Pinkney
 */
@Singleton
public class YamlExtensionManagerPresenter extends AbstractPreferencePagePresenter
    implements YamlExtensionManagerView.ActionDelegate {

  private static final Logger LOG = LoggerFactory.getLogger(YamlExtensionManagerPresenter.class);
  private final String preferenceName = "yaml.preferences";
  private DialogFactory dialogFactory;
  private YamlExtensionManagerView view;
  private PreferencesManager preferencesManager;
  private List<YamlPreference> yamlPreferences;
  private YamlLocalizationConstant local;
  private YamlServiceClient service;
  private boolean dirty = false;

  @Inject
  public YamlExtensionManagerPresenter(
      YamlExtensionManagerView view,
      DialogFactory dialogFactory,
      PreferencesManager preferencesManager,
      YamlLocalizationConstant local,
      YamlServiceClient service) {
    super("Yaml", "Language Server Settings");
    this.view = view;
    this.dialogFactory = dialogFactory;
    this.local = local;
    this.service = service;
    this.preferencesManager = preferencesManager;
    if (preferencesManager.getValue(preferenceName) == null
        || "".equals(preferencesManager.getValue(preferenceName))
        || "{}".equals(preferencesManager.getValue(preferenceName))) {
      this.yamlPreferences = new ArrayList<YamlPreference>();
    } else {
      this.yamlPreferences =
          jsonToYamlPreference(this.preferencesManager.getValue(this.preferenceName));
    }
    this.view.setDelegate(this);
    refreshTable();
  }

  /** {@inheritDoc} */
  @Override
  public void onDeleteClicked(@NotNull final YamlPreference pairKey) {
    dialogFactory
        .createConfirmDialog(
            local.deleteUrlText(),
            local.deleteUrlLabel(),
            new ConfirmCallback() {
              @Override
              public void accepted() {
                deleteKeyFromPreferences(pairKey);
                refreshTable();
                nowDirty();
              }
            },
            getCancelCallback())
        .show();
  }

  private CancelCallback getCancelCallback() {
    return new CancelCallback() {
      @Override
      public void cancelled() {
        // for now do nothing but it need for tests
      }
    };
  }

  /**
   * Delete a preference from Yaml Preferences
   *
   * @param pref The preference you would like to delete
   */
  private void deleteKeyFromPreferences(final YamlPreference pref) {
    this.yamlPreferences.remove(pref);
  }

  /**
   * Add a url to Yaml Preferences
   *
   * @param url The url you would like to add to yaml preferences
   */
  private void addUrlToPreferences(String url) {
    YamlPreference pref = new YamlPreference(url, "/*");
    this.yamlPreferences.add(pref);
  }

  /**
   * Converts json string to list of Yaml Preferences
   *
   * @param jsonStr The json string to turn into the list of Yaml Preferences
   * @return List of Yaml Preferences
   */
  private List<YamlPreference> jsonToYamlPreference(String jsonStr) {
    ArrayList yamlPreferences = new ArrayList<YamlPreference>();
    JsonObject parsedJson = Json.parse(jsonStr);
    for (String glob : parsedJson.keys()) {
      try {
        JsonArray jsonArray = parsedJson.getArray(glob);
        for (int arrNum = 0; arrNum < jsonArray.length(); arrNum++) {
          YamlPreference newYamlPref = new YamlPreference(jsonArray.getString(arrNum), glob);
          yamlPreferences.add(newYamlPref);
        }
      } catch (Exception e) {
        LOG.debug(e.getMessage(), e);
      }
    }

    return yamlPreferences;
  }

  /** {@inheritDoc} */
  @Override
  public void onAddUrlClicked() {
    dialogFactory
        .createInputDialog(
            local.addUrlText(),
            local.addUrlLabel(),
            "",
            0,
            0,
            local.addSchemaButtonText(),
            new InputCallback() {
              @Override
              public void accepted(String url) {
                addUrlToPreferences(url);
                refreshTable();
                nowDirty();
              }
            },
            getCancelCallback())
        .show();
  }

  /** Send the schemas to the Language Server */
  private void setSchemas() {
    Map<String, List<String>> schemaMap =
        PreferenceHelper.yamlPreferenceToMap(this.yamlPreferences);

    Map<String, String> jsonSchemaMap = new HashMap<String, String>();
    for (Map.Entry<String, List<String>> entry : schemaMap.entrySet()) {
      jsonSchemaMap.put(entry.getKey(), prefListToJsonArray(entry.getValue()).toString());
    }

    if (schemaMap != null) {
      service.putSchemas(jsonSchemaMap);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isDirty() {
    return dirty;
  }

  /** {@inheritDoc} */
  public void nowDirty() {
    dirty = true;
    delegate.onDirtyChanged();
  }

  /** {@inheritDoc} */
  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
    refreshTable();
    setSchemas();
  }

  /** Refresh YamlPreferenceCellTable */
  private void refreshTable() {
    view.setPairs(this.yamlPreferences);
  }

  /**
   * Convert a list of strings to JSON
   *
   * @param yamlStringList the List of Strings you want to convert to JSON
   * @return JSONArray of yamlPreferences
   */
  private JSONArray prefListToJsonArray(List<String> yamlStringList) {
    JSONArray yamlPreferenceJsonArr = new JSONArray();
    for (int arrNum = 0; arrNum < yamlStringList.size(); arrNum++) {
      yamlPreferenceJsonArr.set(arrNum, new JSONString(yamlStringList.get(arrNum)));
    }

    return yamlPreferenceJsonArr;
  }

  /**
   * Convert YamlPreference's to JSON
   *
   * @param yamlPreferencesList
   * @return String of yamlPreferences
   */
  private String yamlPreferencesToJson(List<YamlPreference> yamlPreferencesList) {
    Map<String, List<String>> schemaMap = PreferenceHelper.yamlPreferenceToMap(yamlPreferencesList);

    JSONObject jsonObj = new JSONObject();
    for (String glob : schemaMap.keySet()) {
      jsonObj.put(glob, prefListToJsonArray(schemaMap.get(glob)));
    }

    return jsonObj.toString();
  }

  @Override
  public void storeChanges() {
    setSchemas();

    this.preferencesManager.setValue(
        this.preferenceName, yamlPreferencesToJson(this.yamlPreferences));
    this.preferencesManager.flushPreferences();
    dirty = false;
    delegate.onDirtyChanged();
  }

  @Override
  public void revertChanges() {}
}
