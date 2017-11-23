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
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;

/** @author Sergii Leschenko */
public class OAuthServiceClient {
  private final AsyncRequestFactory asyncRequestFactory;
  private final String restContext;

  @Inject
  public OAuthServiceClient(AppContext appContext, AsyncRequestFactory asyncRequestFactory) {
    this.asyncRequestFactory = asyncRequestFactory;
    this.restContext = appContext.getMasterApiEndpoint() + "/oauth";
  }

  public void getToken(String oauthProvider, AsyncRequestCallback<OAuthToken> callback) {
    asyncRequestFactory
        .createGetRequest(restContext + "/token?oauth_provider=" + oauthProvider)
        .send(callback);
  }
}
