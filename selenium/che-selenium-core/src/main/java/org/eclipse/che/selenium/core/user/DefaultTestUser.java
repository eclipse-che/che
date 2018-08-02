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
 * Represents default test user in a test environment.
 *
 * @author Dmytro Nochevnov
 */
public class DefaultTestUser extends TestUserImpl {

  /** To instantiate default test user with specific name, e-mail, password and offline token. */
  @AssistedInject
  public DefaultTestUser(
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
    super.delete();
  }
}
