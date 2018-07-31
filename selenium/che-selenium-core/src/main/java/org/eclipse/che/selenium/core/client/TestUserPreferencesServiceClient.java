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

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

/** @author Musienko Maxim */
@Singleton
public class TestUserPreferencesServiceClient {

  private final String apiEndpoint;
  private final HttpJsonRequestFactory httpRequestFactory;

  @Inject
  public TestUserPreferencesServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider, HttpJsonRequestFactory httpRequestFactory) {
    this.apiEndpoint = apiEndpointProvider.get().toString();
    this.httpRequestFactory = httpRequestFactory;
  }

  public void addGitCommitter(String committerName, String committerEmail) throws Exception {
    httpRequestFactory
        .fromUrl(apiEndpoint + "preferences")
        .usePutMethod()
        .setBody(
            ImmutableMap.of(
                "git.committer.name", committerName,
                "git.committer.email", committerEmail))
        .request();
  }

  public String getPreferences() throws Exception {
    return httpRequestFactory
        .fromUrl(apiEndpoint + "preferences")
        .useGetMethod()
        .request()
        .asString();
  }
}
