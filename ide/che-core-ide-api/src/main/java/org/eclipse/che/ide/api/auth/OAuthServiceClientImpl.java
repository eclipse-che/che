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
package org.eclipse.che.ide.api.auth;

import javax.inject.Inject;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.RestContext;

/** @author Sergii Leschenko */
public class OAuthServiceClientImpl implements OAuthServiceClient {
  private final AsyncRequestFactory asyncRequestFactory;
  private final String restContext;

  @Inject
  public OAuthServiceClientImpl(
      @RestContext String restContext, AsyncRequestFactory asyncRequestFactory) {
    this.asyncRequestFactory = asyncRequestFactory;
    this.restContext = restContext + "/oauth";
  }

  @Override
  public void invalidateToken(String oauthProvider, AsyncRequestCallback<Void> callback) {
    asyncRequestFactory
        .createDeleteRequest(restContext + "/token?oauth_provider=" + oauthProvider)
        .send(callback);
  }

  @Override
  public void getToken(String oauthProvider, AsyncRequestCallback<OAuthToken> callback) {
    asyncRequestFactory
        .createGetRequest(restContext + "/token?oauth_provider=" + oauthProvider)
        .send(callback);
  }
}
