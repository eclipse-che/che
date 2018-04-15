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

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import java.io.IOException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClientFactory;
import org.eclipse.che.selenium.core.provider.RemovableUserProvider;

/**
 * Represents admin user in a test environment.
 *
 * @author Dmytro Nochevnov
 */
public class AdminTestUser extends TestUserImpl {

  /** To instantiate admin test user with specific name, e-mail, password and offline token. */
  @AssistedInject
  public AdminTestUser(
      TestUserServiceClientFactory testUserServiceClientFactory,
      TestAuthServiceClient authServiceClient,
      @Assisted RemovableUserProvider testUserProvider,
      @Assisted("name") String name,
      @Assisted("email") String email,
      @Assisted("password") String password,
      @Assisted("offlineToken") String offlineToken)
      throws NotFoundException, ServerException, BadRequestException {
    super(
        testUserServiceClientFactory,
        authServiceClient,
        testUserProvider,
        name,
        email,
        password,
        offlineToken);
  }

  @Override
  public void delete() throws IOException {
    // we shouldn't delete admin user
  }
}
