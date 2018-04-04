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
package org.eclipse.che.ide.preferences;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;

/** The implementation of {@link PreferencesManager}. */
@Singleton
public class PreferencesManagerImpl implements PreferencesManager {

  private final String PREFERENCES_SERVICE_ENDPOINT;
  private final AsyncRequestFactory asyncRequestFactory;
  private final Map<String, String> persistedPreferences;
  private final Map<String, String> changedPreferences;

  @Inject
  protected PreferencesManagerImpl(AsyncRequestFactory asyncRequestFactory, AppContext appContext) {
    this.asyncRequestFactory = asyncRequestFactory;
    this.persistedPreferences = new HashMap<>();
    this.changedPreferences = new HashMap<>();

    PREFERENCES_SERVICE_ENDPOINT = appContext.getMasterApiEndpoint() + "/preferences";
  }

  @Override
  @Nullable
  public String getValue(String preference) {
    if (changedPreferences.containsKey(preference)) {
      return changedPreferences.get(preference);
    }
    return persistedPreferences.get(preference);
  }

  @Override
  public void setValue(String preference, String value) {
    changedPreferences.put(preference, value);
  }

  @Override
  public Promise<Void> flushPreferences() {
    if (changedPreferences.isEmpty()) {
      return Promises.resolve(null);
    }

    return updatePreferences(changedPreferences)
        .thenPromise(
            result -> {
              persistedPreferences.putAll(changedPreferences);
              changedPreferences.clear();
              return Promises.resolve(null);
            });
  }

  @Override
  public Promise<Map<String, String>> loadPreferences() {
    return getPreferences()
        .then(
            (Function<Map<String, String>, Map<String, String>>)
                preferences -> {
                  persistedPreferences.clear();
                  persistedPreferences.putAll(preferences);
                  return preferences;
                });
  }

  private Promise<Map<String, String>> getPreferences() {
    return asyncRequestFactory
        .createGetRequest(PREFERENCES_SERVICE_ENDPOINT)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .send(new StringMapUnmarshaller());
  }

  private Promise<Map<String, String>> updatePreferences(Map<String, String> newPreferences) {
    final String data = JsonHelper.toJson(newPreferences);

    return asyncRequestFactory
        .createPutRequest(PREFERENCES_SERVICE_ENDPOINT, null)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .data(data)
        .send(new StringMapUnmarshaller());
  }
}
