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

/**
 * Provides default {@link DefaultTestUser} for the Multi User Eclipse Che which ought to be existed
 * at the start of test execution. All tests share the same default user.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class MultiUserCheDefaultTestUserProvider implements TestUserProvider<DefaultTestUser> {

  private final DefaultTestUser testUser;

  private final boolean isNewUser;

  @Inject
  public MultiUserCheDefaultTestUserProvider(
      TestUserFactory testUserFactory,
      @Named("che.testuser.name") String name,
      @Named("che.testuser.email") String email,
      @Named("che.testuser.password") String password,
      @Named("che.testuser.offline_token") String offlineToken) {
    if (name == null || name.trim().isEmpty()) {
      isNewUser = true;
      // TODO create new user
    } else {
      isNewUser = false;
    }

    this.testUser = testUserFactory.create(name, email, password, offlineToken, this);
  }

  @Override
  public DefaultTestUser get() {
    return testUser;
  }

  @Override
  public void delete(DefaultTestUser testUser) throws IOException {
    if (isNewUser) {
      // TODO delete test user
    }
  }
}
