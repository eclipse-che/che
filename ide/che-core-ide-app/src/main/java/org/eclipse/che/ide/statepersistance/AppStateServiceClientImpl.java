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
package org.eclipse.che.ide.statepersistance;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.statepersistance.AppStateServiceClient;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.StringUnmarshaller;

/** @author Roman Nikitenko */
@Singleton
public class AppStateServiceClientImpl implements AppStateServiceClient {
  private static final String PREFIX = "/app/state";

  private AppContext appContext;
  private AsyncRequestFactory asyncRequestFactory;

  @Inject
  public AppStateServiceClientImpl(AppContext appContext, AsyncRequestFactory asyncRequestFactory) {
    this.appContext = appContext;
    this.asyncRequestFactory = asyncRequestFactory;
  }

  @Override
  public Promise<String> loadState() {
    String userId = appContext.getCurrentUser().getId();
    String url = appContext.getWsAgentServerApiEndpoint() + PREFIX + "?userId=" + userId;
    return asyncRequestFactory
        .createGetRequest(url)
        .header(ACCEPT, APPLICATION_JSON)
        .send(new StringUnmarshaller());
  }

  @Override
  public Promise<Void> saveState(String state) {
    String userId = appContext.getCurrentUser().getId();
    String url =
        appContext.getWsAgentServerApiEndpoint() + PREFIX + "/update/" + "?userId=" + userId;

    return asyncRequestFactory
        .createPostRequest(url, state)
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .send();
  }
}
