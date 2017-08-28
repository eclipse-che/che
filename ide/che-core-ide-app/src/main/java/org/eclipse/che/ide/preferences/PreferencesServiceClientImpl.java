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
package org.eclipse.che.ide.preferences;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.inject.Inject;
import java.util.Map;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.user.PreferencesServiceClient;
import org.eclipse.che.ide.json.JsonHelper;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringMapUnmarshaller;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/**
 * Default implementation of {@link PreferencesServiceClient}.
 *
 * @author Yevhenii Voevodin
 */
public class PreferencesServiceClientImpl implements PreferencesServiceClient {

  private final String PREFERENCES_PATH;
  private final LoaderFactory loaderFactory;
  private final AsyncRequestFactory asyncRequestFactory;

  @Inject
  protected PreferencesServiceClientImpl(
      AppContext appContext, LoaderFactory loaderFactory, AsyncRequestFactory asyncRequestFactory) {
    this.loaderFactory = loaderFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    PREFERENCES_PATH = appContext.getMasterEndpoint() + "/preferences";
  }

  @Override
  public Promise<Map<String, String>> getPreferences() {

    return asyncRequestFactory
        .createGetRequest(PREFERENCES_PATH)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting user's preferences..."))
        .send(new StringMapUnmarshaller());
  }

  @Override
  public Promise<Map<String, String>> updatePreferences(Map<String, String> update) {
    final String data = JsonHelper.toJson(update);
    return asyncRequestFactory
        .createPutRequest(PREFERENCES_PATH, null)
        .header(ACCEPT, APPLICATION_JSON)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .data(data)
        .loader(loaderFactory.newLoader("Updating user's preferences..."))
        .send(new StringMapUnmarshaller());
  }
}
