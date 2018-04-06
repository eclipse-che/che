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

import com.google.inject.Inject;
import java.io.IOException;
import javax.inject.Singleton;
import org.eclipse.che.selenium.core.client.KeycloakAdminConsoleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides auxiliary {@link TestUser} for the Multi User Eclipse Che which ought to be existed at
 * the start of test execution. All tests share the same auxiliary user.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class MultiUserCheTestUserProvider implements TestUserProvider<TestUser> {

  private static final Logger LOG = LoggerFactory.getLogger(MultiUserCheTestUserProvider.class);

  private final TestUser testUser;
  private final boolean isNewUser;
  private final KeycloakAdminConsoleClient keycloakAdminConsoleClient;

  @Inject
  public MultiUserCheTestUserProvider(
      TestUserFactory testUserFactory,
      KeycloakAdminConsoleClient keycloakAdminConsoleClient,
      MultiUserCheAdminTestUserProvider adminTestUserProvider) {
    this.keycloakAdminConsoleClient = keycloakAdminConsoleClient;
    TestUserImpl testUser;
    Boolean isNewUser;
    try {
      testUser = keycloakAdminConsoleClient.createUser(this);
      isNewUser = true;
    } catch (IOException e) {
      LOG.warn(
          "It's impossible to create test user from tests because of error. "
              + "Is going to use admin test user as test user.",
          e);

      isNewUser = false;

      AdminTestUser adminTestUser = adminTestUserProvider.get();
      testUser =
          testUserFactory.create(
              adminTestUser.getName(),
              adminTestUser.getEmail(),
              adminTestUser.getPassword(),
              adminTestUser.getOfflineToken(),
              adminTestUserProvider);
    }

    this.testUser = testUser;
    this.isNewUser = isNewUser;

    LOG.info(
        "User name='{}', id='{}' is being used for testing", testUser.getName(), testUser.getId());
  }

  @Override
  public TestUser get() {
    return testUser;
  }

  @Override
  public void delete(TestUser testUser) throws IOException {
    if (isNewUser) {
      keycloakAdminConsoleClient.delete(testUser);
    }
  }
}
