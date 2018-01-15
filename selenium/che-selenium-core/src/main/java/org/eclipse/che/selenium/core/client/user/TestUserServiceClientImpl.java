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
package org.eclipse.che.selenium.core.client.user;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.requestfactory.TestUserHttpJsonRequestFactory;

/** @author Musienko Maxim */
@Singleton
public class TestUserServiceClientImpl implements TestUserServiceClient {

  private final String userServiceEndpoint;
  private final TestUserHttpJsonRequestFactory requestFactory;

  @Inject
  public TestUserServiceClientImpl(
      TestApiEndpointUrlProvider apiEndpointProvider,
      TestUserHttpJsonRequestFactory testUserHttpJsonRequestFactory) {
    this.userServiceEndpoint = apiEndpointProvider.get().toString() + "user/";
    this.requestFactory = testUserHttpJsonRequestFactory;
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
