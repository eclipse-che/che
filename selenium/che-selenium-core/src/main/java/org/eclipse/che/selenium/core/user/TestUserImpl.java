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
package org.eclipse.che.selenium.core.user;

import static java.lang.String.format;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import javax.annotation.PreDestroy;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClientFactory;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anatolii Bazko
 * @author Dmytro Nochevnov
 * @author Anton Korneta
 */
public class TestUserImpl implements TestUser {

  private static final Logger LOG = LoggerFactory.getLogger(TestUserImpl.class);

  private final String email;
  private final String password;
  private final String name;
  private final String id;
  private final String authToken;
  private final String offlineToken;

  private final TestUserServiceClient userServiceClient;
  private final TestWorkspaceServiceClient workspaceServiceClient;

  /** To instantiate user with specific name, e-mail, password and offline token. */
  @AssistedInject
  public TestUserImpl(
      TestUserServiceClientFactory testUserServiceClientFactory,
      TestAuthServiceClient authServiceClient,
      TestWorkspaceServiceClientFactory wsServiceClientFactory,
      @Assisted("name") String name,
      @Assisted("email") String email,
      @Assisted("password") String password,
      @Assisted("offlineToken") String offlineToken)
      throws Exception {
    this.userServiceClient = testUserServiceClientFactory.create(name, password, offlineToken);
    this.email = email;
    this.password = password;
    this.name = name;
    this.offlineToken = offlineToken;
    this.authToken = authServiceClient.login(name, password, offlineToken);
    this.id = userServiceClient.findByEmail(email).getId();
    LOG.info("User name='{}', id='{}' is being used for testing", name, id);
    this.workspaceServiceClient = wsServiceClientFactory.create(email, password, offlineToken);
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
  public String getAuthToken() {
    return authToken;
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
  @PreDestroy
  public void cleanUp() {}

  @Override
  public String toString() {
    return format(
        "%s{name=%s, email=%s, password=%s}",
        this.getClass().getSimpleName(), this.getName(), this.getEmail(), getPassword());
  }
}
