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
package org.eclipse.che.selenium.core.user;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Default {@link TestUser} that will be created before all tests and will be deleted after them.
 * All tests share the same default user.
 *
 * <p>To have more users per tests see {@link InjectTestUser}.
 *
 * @author Anatolii Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class DefaultTestUser implements TestUser {

  private final TestUser testUser;

  @Inject
  public DefaultTestUser(TestUser testUser) throws Exception {
    this.testUser = testUser;
  }

  @Override
  public String getEmail() {
    return testUser.getEmail();
  }

  @Override
  public String getPassword() {
    return testUser.getPassword();
  }

  @Override
  public String getAuthToken() {
    return testUser.getAuthToken();
  }

  @Override
  public String getName() {
    return testUser.getName();
  }

  @Override
  public String getId() {
    return testUser.getId();
  }

  @Override
  public void delete() {
    testUser.delete();
  }
}
