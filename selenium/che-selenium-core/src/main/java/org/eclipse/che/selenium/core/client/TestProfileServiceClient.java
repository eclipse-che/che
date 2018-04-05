/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.user.DefaultTestUser;

/** @author Musienko Maxim */
@Singleton
public class TestProfileServiceClient {
  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;
  private final DefaultTestUser defaultTestUser;

  @Inject
  public TestProfileServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      HttpJsonRequestFactory requestFactory,
      DefaultTestUser defaultTestUser) {
    this.apiEndpoint = apiEndpointProvider.get().toString();
    this.requestFactory = requestFactory;
    this.defaultTestUser = defaultTestUser;
  }

  public void setAttributes(Map<String, String> attributes) throws Exception {
    requestFactory
        .fromUrl(apiEndpoint + "profile/attributes")
        .usePutMethod()
        .setBody(attributes)
        .request();
  }

  public void setUserNames(String name, String lastName) throws Exception {
    Map<String, String> attributes =
        ImmutableMap.of(
            "firstName", name,
            "lastName", lastName);

    setAttributes(attributes);
  }
}
