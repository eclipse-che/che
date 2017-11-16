/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.js.plugin.model.theme;

import elemental.json.Json;
import elemental.json.JsonObject;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.theme.Theme;
import org.eclipse.che.ide.api.theme.ThemeProvider;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;

/** @author Yevhen Vydolob */
public class JsThemeProvider implements ThemeProvider {

  private final String id;
  private final String description;
  private final String themePath;
  private final PromiseProvider promiseProvider;
  private final AsyncRequestFactory requestFactory;

  public JsThemeProvider(
      String id,
      String description,
      String themePath,
      PromiseProvider promiseProvider,
      AsyncRequestFactory requestFactory) {
    this.id = id;
    this.description = description;
    this.themePath = themePath;
    this.promiseProvider = promiseProvider;
    this.requestFactory = requestFactory;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public Promise<Theme> loadTheme() {
    return promiseProvider.create(
        callback -> {
          Promise<String> promise =
              requestFactory.createGetRequest(themePath).send(new StringUnmarshaller());
          promise
              .then(
                  arg -> {
                    JsonObject jsonObject = Json.parse(arg);
                    callback.onSuccess(new JsTheme(jsonObject));
                  })
              .catchError(
                  err -> {
                    callback.onFailure(err.getCause());
                  });
        });
  }
}
