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

import com.google.inject.Inject;
import java.io.IOException;
import javax.annotation.PreDestroy;
import org.eclipse.che.selenium.core.client.keycloak.cli.KeycloakCliClient;
import org.eclipse.che.selenium.core.provider.AdminTestUserProvider;
import org.eclipse.che.selenium.core.provider.TestUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides new {@link TestUser} for the Multi User Eclipse Che.
 *
 * @author Dmytro Nochevnov
 */
public class MultiUserCheTestUserProvider implements TestUserProvider {

  private static final Logger LOG = LoggerFactory.getLogger(MultiUserCheTestUserProvider.class);

  private final TestUser testUser;
  private final boolean isNewUser;
  private final KeycloakCliClient keycloakCliClient;

  @Inject
  public MultiUserCheTestUserProvider(
      TestUserFactory<TestUserImpl> testUserFactory,
      KeycloakCliClient keycloakCliClient,
      AdminTestUserProvider adminTestUserProvider) {
    this.keycloakCliClient = keycloakCliClient;
    TestUserImpl testUser;
    Boolean isNewUser;
    try {
      testUser = keycloakCliClient.createUser(this);
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

      LOG.info(
          "User name='{}', id='{}' is being used for testing",
          testUser.getName(),
          testUser.getId());
    }

    this.testUser = testUser;
    this.isNewUser = isNewUser;
  }

  @Override
  public TestUser get() {
    return testUser;
  }

  @Override
  @PreDestroy
  public void delete() throws IOException {
    if (isNewUser) {
      keycloakCliClient.delete(testUser);
    }
  }
}
