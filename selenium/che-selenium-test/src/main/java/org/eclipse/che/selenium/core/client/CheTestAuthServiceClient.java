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
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Anatolii Bazko */
@Singleton
public class CheTestAuthServiceClient implements TestAuthServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(CheTestAuthServiceClient.class);

  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public CheTestAuthServiceClient(
      String apiEndpoint, DefaultHttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpoint;
    this.requestFactory = requestFactory;
  }

  @Override
  public String login(String username, String password, String offlineToken) throws Exception {
    return username;
  }

  @Override
  public void logout(String authToken) {
    try {
      String apiUrl = apiEndpoint + "auth/logout?token=" + authToken;
      requestFactory.fromUrl(apiUrl).usePostMethod().request();
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }
}
