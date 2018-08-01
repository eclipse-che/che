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
package org.eclipse.che.ide.statepersistance;

import static org.eclipse.che.ide.statepersistance.AppStateConstants.APP_STATE;
import static org.eclipse.che.ide.statepersistance.AppStateConstants.WORKSPACE;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.json.JsonFactory;
import elemental.json.JsonObject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.slf4j.Logger;

/**
 * User preferences was used to storage serialized IDE state. The class provides back compatibility
 * and allows to get IDE state from preferences and clean up them.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class AppStateBackwardCompatibility {
  private static final Logger LOG = getLogger(AppStateBackwardCompatibility.class);

  private final AppContext appContext;
  private final JsonFactory jsonFactory;
  private final PreferencesManager preferencesManager;

  @Inject
  public AppStateBackwardCompatibility(
      AppContext appContext, JsonFactory jsonFactory, PreferencesManager preferencesManager) {
    this.appContext = appContext;
    this.jsonFactory = jsonFactory;
    this.preferencesManager = preferencesManager;
  }

  /**
   * Allows to get IDE state for current workspace from user preferences.
   *
   * @return IDE state of current workspace or {@code null} when this one is not found
   */
  @Nullable
  JsonObject getAppState() {
    JsonObject allWsState = getAllWorkspacesState();
    if (allWsState == null) {
      return null;
    }

    String wsId = appContext.getWorkspace().getId();
    JsonObject workspaceSettings = allWsState.getObject(wsId);

    return workspaceSettings != null ? workspaceSettings.get(WORKSPACE) : null;
  }

  /**
   * Allows to get states for all workspaces from user preferences.
   *
   * @return app states of all workspaces for current user or {@code null} when these ones are not
   *     found
   */
  @Nullable
  JsonObject getAllWorkspacesState() {
    try {
      String json = preferencesManager.getValue(APP_STATE);
      return jsonFactory.parse(json);
    } catch (Exception e) {
      return null;
    }
  }

  /** Allows to remove IDE state for current workspace from user preferences */
  void removeAppState() {
    JsonObject allWsState = getAllWorkspacesState();
    if (allWsState != null) {
      String wsId = appContext.getWorkspace().getId();
      allWsState.remove(wsId);
      writeToPreferences(allWsState);
    }
  }

  /**
   * Provide ability to write to preferences state for all workspaces. It's used to clean up user
   * preferences
   */
  private Promise<Void> writeToPreferences(JsonObject state) {
    preferencesManager.setValue(APP_STATE, state.toJson());
    return preferencesManager
        .flushPreferences()
        .catchError(
            error -> {
              LOG.error(
                  "Failed to store app's state to user's preferences: {}", error.getMessage());
            });
  }
}
