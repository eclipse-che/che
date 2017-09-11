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
import java.io.IOException;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestAdminHttpJsonRequestFactory;

/**
 * @author Musienko Maxim
 * @author Dmytro Nochevnov
 */
@Singleton
public class CheTestUserServiceClient implements TestUserServiceClient {

  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public CheTestUserServiceClient(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestAdminHttpJsonRequestFactory requestFactory) {
    this.apiEndpoint = apiEndpointProvider.get().toString();
    this.requestFactory = requestFactory;
  }

  public void delete(String id) throws IOException {
    String url = apiEndpoint + "user/" + id;
    try {
      requestFactory.fromUrl(url).useDeleteMethod().request();
    } catch (ApiException e) {
      throw new IOException(
          String.format("Error of removal of user with id='%s': %s", id, e.getMessage()), e);
    }
  }

  public String create(String name, String email, String password) throws IOException {
    String url = apiEndpoint + "user";
    try {
      return requestFactory
          .fromUrl(url)
          .usePostMethod()
          .setBody(newDto(UserDto.class).withName(name).withPassword(password).withEmail(email))
          .request()
          .asDto(UserDto.class)
          .getId();
    } catch (ApiException e) {
      throw new IOException(
          String.format("Error of creation of user with name='%s': %s", name, e.getMessage()), e);
    }
  }
}
