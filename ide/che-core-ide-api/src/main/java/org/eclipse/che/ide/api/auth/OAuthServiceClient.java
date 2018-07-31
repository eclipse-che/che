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
package org.eclipse.che.ide.api.auth;

import javax.inject.Inject;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

/**
 * Serves connections to OauthAuthentication service. Allows to get OAuth tokens via callback or as
 * promise.
 *
 * @author Sergii Leschenko
 * @author Max Shaposhnyk
 */
public class OAuthServiceClient {
  private final AsyncRequestFactory asyncRequestFactory;
  private final String restContext;
  private final DtoUnmarshallerFactory unmarshallerFactory;

  @Inject
  public OAuthServiceClient(
      AppContext appContext,
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory unmarshallerFactory) {
    this.asyncRequestFactory = asyncRequestFactory;
    this.restContext = appContext.getMasterApiEndpoint() + "/oauth";
    this.unmarshallerFactory = unmarshallerFactory;
  }

  public void getToken(String oauthProvider, AsyncRequestCallback<OAuthToken> callback) {
    asyncRequestFactory
        .createGetRequest(restContext + "/token?oauth_provider=" + oauthProvider)
        .send(callback);
  }

  public Promise<OAuthToken> getToken(String oauthProvider) {
    return asyncRequestFactory
        .createGetRequest(restContext + "/token?oauth_provider=" + oauthProvider)
        .send(unmarshallerFactory.newUnmarshaller(OAuthToken.class));
  }
}
