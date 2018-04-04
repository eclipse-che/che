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
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Default {@link TestUser} that will be created before all tests and will be deleted after them.
 * All tests share the same default user.
 *
 * @author Anatolii Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class CheDefaultTestUser implements TestUser {

  private final TestUser delegate;

  @Inject
  public CheDefaultTestUser(
      TestUserFactory userFactory,
      @Named("che.testuser.name") String name,
      @Named("che.testuser.email") String email,
      @Named("che.testuser.password") String password,
      @Named("che.testuser.offline_token") String offlineToken)
      throws Exception {
    this.delegate = userFactory.create(name, email, password, offlineToken);
  }

  @Override
  public String getEmail() {
    return delegate.getEmail();
  }

  @Override
  public String getPassword() {
    return delegate.getPassword();
  }

  @Override
  public String getAuthToken() {
    return delegate.getAuthToken();
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public String getOfflineToken() {
    return delegate.getOfflineToken();
  }

  @Override
  public void cleanUp() {}
}
