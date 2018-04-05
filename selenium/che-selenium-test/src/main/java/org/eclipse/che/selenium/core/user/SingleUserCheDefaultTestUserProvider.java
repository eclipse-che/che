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

/**
 * Provides default {@link TestUser} for the Single User Eclipse Che which ought to be existed at
 * the start of test execution. All tests share the same default user.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class SingleUserCheDefaultTestUserProvider implements TestUserProvider<DefaultTestUser> {

  private final DefaultTestUser defaultTestUser;

  @Inject
  public SingleUserCheDefaultTestUserProvider(TestUserFactory testUserFactory) {
    this.defaultTestUser = testUserFactory.create("che", "che@eclipse.org", "secret", "", this);
  }

  @Override
  public DefaultTestUser get() {
    return defaultTestUser;
  }

  @Override
  public void delete(DefaultTestUser testUser) throws IOException {
    // we don't need to remove test user of Single User Eclipse Che
  }
}
