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
package org.eclipse.che.ide.ext.java.client.settings.compiler;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.ext.java.client.settings.service.SettingsServiceClient;

/**
 * The implementation of {@link PreferencesManager} for managing Java compiler properties
 *
 * @author Alexander Andrienko
 */
@Singleton
public class ErrorsWarningsPreferenceManager implements PreferencesManager {

  private final SettingsServiceClient service;
  private final Map<String, String> changedPreferences;

  private Map<String, String> persistedPreferences;

  @Inject
  protected ErrorsWarningsPreferenceManager(SettingsServiceClient service) {
    this.service = service;

    this.persistedPreferences = new HashMap<>();
    this.changedPreferences = new HashMap<>();
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

    return service
        .applyCompileParameters(changedPreferences)
        .then(
            new Operation<Void>() {
              @Override
              public void apply(Void aVoid) throws OperationException {
                persistedPreferences.putAll(changedPreferences);
                changedPreferences.clear();
              }
            });
  }

  @Override
  public Promise<Map<String, String>> loadPreferences() {
    return service
        .getCompileParameters()
        .then(
            new Function<Map<String, String>, Map<String, String>>() {
              @Override
              public Map<String, String> apply(Map<String, String> properties)
                  throws FunctionException {
                persistedPreferences.putAll(properties);
                return properties;
              }
            });
  }
}
