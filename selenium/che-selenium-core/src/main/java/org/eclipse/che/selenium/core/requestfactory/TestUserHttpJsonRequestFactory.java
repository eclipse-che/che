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
package org.eclipse.che.selenium.core.requestfactory;

import static java.lang.String.format;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;

/** @author Dmytro Nochevnov */
public class TestUserHttpJsonRequestFactory extends TestHttpJsonRequestFactory {

  private final String name;
  private final String password;
  private final String offlineToken;
  private final TestAuthServiceClient authServiceClient;

  @AssistedInject
  public TestUserHttpJsonRequestFactory(
      TestAuthServiceClient authServiceClient,
      @Assisted("name") String name,
      @Assisted("password") String password,
      @Assisted("offlineToken") String offlineToken) {
    this.authServiceClient = authServiceClient;
    this.name = name;
    this.password = password;
    this.offlineToken = offlineToken;
  }

  @Override
  protected String getAuthToken() {
    try {
      return authServiceClient.login(name, password, offlineToken);
    } catch (Exception ex) {
      throw new RuntimeException(format("Failed to get access token for user '%s'", name));
    }
  }
}
