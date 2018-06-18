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
import com.google.inject.name.Named;
import java.io.IOException;
import javax.inject.Singleton;
import org.eclipse.che.selenium.core.client.keycloak.cli.KeycloakCliClient;
import org.eclipse.che.selenium.core.provider.AdminTestUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides {@link AdminTestUser} that ought to be existed at the start of test execution. All tests
 * share the same admin user.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class MultiUserCheAdminTestUserProvider implements AdminTestUserProvider {

  private static final Logger LOG =
      LoggerFactory.getLogger(MultiUserCheDefaultTestUserProvider.class);

  private AdminTestUser adminTestUser;

  @Inject private TestUserFactory<AdminTestUser> adminTestUserFactory;
  @Inject private KeycloakCliClient keycloakCliClient;

  @Inject
  @Named("che.admin.name")
  private String name;

  @Inject
  @Named("che.admin.email")
  private String email;

  @Inject
  @Named("che.admin.password")
  private String password;

  @Inject
  @Named("che.admin.offline_token")
  private String offlineToken;

  @Override
  public AdminTestUser get() {
    if (adminTestUser == null) {
      if (email == null
          || email.trim().isEmpty()
          || password == null
          || password.trim().isEmpty()) {
        throw new IllegalStateException("Admin test user credentials are unknown");
      }

      adminTestUser = adminTestUserFactory.create(name, email, password, offlineToken, this);
      keycloakCliClient.setupAdmin(adminTestUser);

      LOG.info("User name='{}', id='{}' is being used as admin", name, adminTestUser.getId());
    }

    return adminTestUser;
  }

  @Override
  public void delete() throws IOException {
    // do nothing because we don't remove admin user
  }
}
