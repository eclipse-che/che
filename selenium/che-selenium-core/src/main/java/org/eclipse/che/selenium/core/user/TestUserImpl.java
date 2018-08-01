/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.user;

import static java.lang.String.format;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.io.IOException;
import javax.annotation.PreDestroy;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClientFactory;
import org.eclipse.che.selenium.core.provider.RemovableUserProvider;

/**
 * @author Anatolii Bazko
 * @author Dmytro Nochevnov
 * @author Anton Korneta
 */
public class TestUserImpl implements TestUser {
  private final String email;
  private final String password;
  private final String name;
  private final String id;
  private final String offlineToken;

  private final TestUserServiceClient userServiceClient;
  private final TestAuthServiceClient authServiceClient;
  private final RemovableUserProvider testUserProvider;

  /** To instantiate user with specific name, e-mail, password and offline token. */
  @AssistedInject
  public TestUserImpl(
      TestUserServiceClientFactory testUserServiceClientFactory,
      TestAuthServiceClient authServiceClient,
      @Assisted RemovableUserProvider testUserProvider,
      @Assisted("name") String name,
      @Assisted("email") String email,
      @Assisted("password") String password,
      @Assisted("offlineToken") String offlineToken)
      throws NotFoundException, ServerException, BadRequestException {
    this.authServiceClient = authServiceClient;
    this.testUserProvider = testUserProvider;

    this.name = name;
    this.email = email;
    this.password = password;
    this.offlineToken = offlineToken;

    this.userServiceClient = testUserServiceClientFactory.create(this);
    this.id = userServiceClient.findByEmail(email).getId();
  }

  @Override
  public String getEmail() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String obtainAuthToken() {
    try {
      return authServiceClient.login(name, password, offlineToken);
    } catch (Exception e) {
      throw new RuntimeException(format("Error of log into the product as user '%s'.", name), e);
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getOfflineToken() {
    return offlineToken;
  }

  @Override
  public String toString() {
    return format(
        "%s{name=%s, email=%s, id=%s}",
        this.getClass().getSimpleName(), this.getName(), this.getEmail(), this.getId());
  }

  @Override
  @PreDestroy
  public void delete() throws IOException {
    testUserProvider.delete();
  }
}
