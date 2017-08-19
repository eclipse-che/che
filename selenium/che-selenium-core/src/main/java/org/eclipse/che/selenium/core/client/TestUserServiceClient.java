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
package org.eclipse.che.selenium.core.client;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.URLEncoder;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestAdminHttpJsonRequestFactory;

/** @author Musienko Maxim */
@Singleton
public class TestUserServiceClient {
  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public TestUserServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestAdminHttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpointProvider.get().toString();
    this.requestFactory = requestFactory;
  }

  public User getByEmail(String email) throws Exception {
    String url = apiEndpoint + "user/find?email=" + URLEncoder.encode(email, "UTF-8");
    HttpJsonResponse response = requestFactory.fromUrl(url).useGetMethod().request();

    return response.asDto(UserDto.class);
  }

  public void deleteByEmail(String email) throws Exception {
    String url = apiEndpoint + "user/" + getByEmail(email).getId();
    requestFactory.fromUrl(url).useDeleteMethod().request();
  }

  public User create(String email, String password) throws Exception {
    String url = apiEndpoint + "user";
    return requestFactory
        .fromUrl(url)
        .usePostMethod()
        .setBody(
            newDto(UserDto.class)
                .withName(email.split("@")[0])
                .withPassword(password)
                .withEmail(email))
        .request()
        .asDto(UserDto.class);
  }

  public UserDto getUser(String auth) throws Exception {
    String url = apiEndpoint + "user";
    return requestFactory
        .fromUrl(url)
        .useGetMethod()
        .setAuthorizationHeader(auth)
        .request()
        .asDto(UserDto.class);
  }
}
