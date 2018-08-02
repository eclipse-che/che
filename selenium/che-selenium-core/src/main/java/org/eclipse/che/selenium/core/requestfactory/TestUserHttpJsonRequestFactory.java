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
package org.eclipse.che.selenium.core.requestfactory;

import static java.lang.String.format;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;

/** @author Dmytro Nochevnov */
public class TestUserHttpJsonRequestFactory extends TestHttpJsonRequestFactory {

  private final TestAuthServiceClient authServiceClient;
  private final TestUser testUser;

  @AssistedInject
  public TestUserHttpJsonRequestFactory(
      TestAuthServiceClient authServiceClient, @Assisted TestUser testUser) {
    this.authServiceClient = authServiceClient;
    this.testUser = testUser;
  }

  @Override
  protected String getAuthToken() {
    try {
      return authServiceClient.login(
          testUser.getName(), testUser.getPassword(), testUser.getOfflineToken());
    } catch (Exception ex) {
      throw new RuntimeException(
          format("Failed to get access token for user '%s'", testUser.getName()));
    }
  }
}
