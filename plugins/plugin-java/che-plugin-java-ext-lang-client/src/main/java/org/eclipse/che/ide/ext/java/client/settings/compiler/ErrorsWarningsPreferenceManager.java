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
package org.eclipse.che.ide.ext.java.client.settings.compiler;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.preferences.PreferencesManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.java.client.service.JavaLanguageExtensionServiceClient;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaCoreOptions;

/**
 * The implementation of {@link PreferencesManager} for managing Java compiler properties
 *
 * @author Alexander Andrienko
 * @author Anatolii Bazko
 */
@Singleton
public class ErrorsWarningsPreferenceManager implements PreferencesManager {

  private final JavaLanguageExtensionServiceClient service;
  private final PromiseProvider promiseProvider;
  private final Map<String, String> changedPreferences;
  private final DtoFactory dtoFactory;

  private Map<String, String> persistedPreferences;

  @Inject
  protected ErrorsWarningsPreferenceManager(
      JavaLanguageExtensionServiceClient service,
      PromiseProvider promiseProvider,
      DtoFactory dtoFactory) {
    this.service = service;
    this.promiseProvider = promiseProvider;
    this.dtoFactory = dtoFactory;

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
      return promiseProvider.resolve(null);
    }

    JavaCoreOptions javaCoreOptions = dtoFactory.createDto(JavaCoreOptions.class);
    javaCoreOptions.setOptions(changedPreferences);

    return service
        .updateJavaCoreOptions(javaCoreOptions)
        .then(
            (Function<Boolean, Void>)
                result -> {
                  persistedPreferences.putAll(changedPreferences);
                  changedPreferences.clear();
                  return null;
                });
  }

  @Override
  public Promise<Map<String, String>> loadPreferences() {
    List<String> options = new ArrayList<>();
    for (ErrorWarningsOptions option : ErrorWarningsOptions.values()) {
      options.add(option.toString());
    }

    return service
        .getJavaCoreOptions(options)
        .then(
            (Function<JavaCoreOptions, Map<String, String>>)
                javaCoreOptions -> {
                  persistedPreferences.putAll(javaCoreOptions.getOptions());
                  return javaCoreOptions.getOptions();
                });
  }
}
