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
 * Provides admin {@link DefaultTestUser} that ought to be existed at the start of test execution.
 * All tests share the same admin user.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class MultiUserCheAdminTestUserProvider implements TestUserProvider<AdminTestUser> {

  private final AdminTestUser testUser;

  @Inject
  public MultiUserCheAdminTestUserProvider(
      TestUserFactory testUserFactory,
      @Named("che.admin.name") String name,
      @Named("che.admin.email") String email,
      @Named("che.admin.password") String password,
      @Named("che.admin.offline_token") String offlineToken) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalStateException("Admin test user credentials are unknown");
    }

    this.testUser = testUserFactory.create(name, email, password, offlineToken, this);
  }

  @Override
  public AdminTestUser get() {
    return testUser;
  }

  @Override
  public void delete(AdminTestUser testUser) throws IOException {
    // do nothing because we don't remove admin user
  }
}
