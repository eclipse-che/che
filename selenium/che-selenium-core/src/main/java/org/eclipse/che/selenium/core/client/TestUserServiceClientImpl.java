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

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.io.IOException;
import java.net.URLEncoder;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactoryCreator;
import org.eclipse.che.selenium.core.user.TestUser;

/** @author Musienko Maxim */
public class TestUserServiceClientImpl implements TestUserServiceClient {

  private final String userServiceEndpoint;
  private final TestHttpJsonRequestFactory requestFactory;

  public TestUserServiceClientImpl(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactory userHttpJsonRequestFactory) {
    this.userServiceEndpoint = apiEndpointProvider.get().toString() + "user/";
    this.requestFactory = userHttpJsonRequestFactory;
  }

  @AssistedInject
  public TestUserServiceClientImpl(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactoryCreator userHttpJsonRequestFactoryCreator,
      @Assisted TestUser testUser) {
    this(apiEndpointProvider, userHttpJsonRequestFactoryCreator.create(testUser));
  }

  @Override
  public void create(String name, String email, String password)
      throws BadRequestException, ConflictException, ServerException {
    try {
      requestFactory
          .fromUrl(userServiceEndpoint)
          .usePostMethod()
          .setBody(newDto(UserDto.class).withEmail(email).withName(name).withPassword(password))
          .request();
    } catch (IOException | UnauthorizedException | NotFoundException | ForbiddenException ex) {
      throw new ServerException(ex);
    }
  }

  @Override
  public User getById(String id) throws NotFoundException, ServerException {
    try {
      return requestFactory
          .fromUrl(userServiceEndpoint + id)
          .useGetMethod()
          .request()
          .asDto(UserDto.class);
    } catch (IOException
        | BadRequestException
        | UnauthorizedException
        | ForbiddenException
        | ConflictException ex) {
      throw new ServerException(ex);
    }
  }

  @Override
  public User findByEmail(String email)
      throws BadRequestException, NotFoundException, ServerException {
    try {
      return requestFactory
          .fromUrl(userServiceEndpoint + "find")
          .useGetMethod()
          .addQueryParam("email", URLEncoder.encode(email, "UTF-8"))
          .request()
          .asDto(UserDto.class);
    } catch (IOException | UnauthorizedException | ForbiddenException | ConflictException ex) {
      throw new ServerException(ex);
    }
  }

  @Override
  public User findByName(String name)
      throws BadRequestException, NotFoundException, ServerException {
    try {
      return requestFactory
          .fromUrl(userServiceEndpoint + "find")
          .useGetMethod()
          .addQueryParam("name", name)
          .request()
          .asDto(UserDto.class);
    } catch (IOException | UnauthorizedException | ForbiddenException | ConflictException ex) {
      throw new ServerException(ex);
    }
  }

  @Override
  public void remove(String id) throws ConflictException, ServerException {
    try {
      requestFactory.fromUrl(userServiceEndpoint + id).useDeleteMethod().request();
    } catch (IOException
        | BadRequestException
        | NotFoundException
        | UnauthorizedException
        | ForbiddenException ex) {
      throw new ServerException(ex);
    }
  }
}
