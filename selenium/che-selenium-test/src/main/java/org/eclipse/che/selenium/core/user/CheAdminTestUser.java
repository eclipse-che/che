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

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Named;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.configuration.ConfigurationException;

/**
 * @author Anatolii Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class CheAdminTestUser implements AdminTestUser {

  private static final String CHE_ADMIN_NAME_PROPERTY = "che.admin.name";
  private static final String CHE_ADMIN_PASSWORD_PROPERTY = "che.admin.password";

  @Inject(optional = true)
  @Named(CHE_ADMIN_NAME_PROPERTY)
  private String name;

  @Inject(optional = true)
  @Named(CHE_ADMIN_NAME_PROPERTY)
  private String password;

  @Inject private Provider<TestUserServiceClient> userServiceClientProvider;
  @Inject private TestAuthServiceClient authServiceClient;

  private AtomicReference<String> authToken = new AtomicReference<>();

  @Override
  public String getEmail() {
    return "admin@email";
  }

  @Override
  public String getPassword() {
    return ofNullable(password)
        .orElseThrow(
            () ->
                new ConfigurationException(
                    format("Properties '%s' is undefined.", CHE_ADMIN_PASSWORD_PROPERTY)));
  }

  @Override
  public String getAuthToken() {
    return ofNullable(authToken.get())
        .orElseGet(
            () ->
                authToken.updateAndGet(
                    (s) -> {
                      try {
                        return authServiceClient.login(getName(), getPassword());
                      } catch (Exception e) {
                        throw new IllegalStateException(e);
                      }
                    }));
  }

  @Override
  public String getName() {
    return ofNullable(name)
        .orElseThrow(
            () ->
                new ConfigurationException(
                    format("Properties '%s' is undefined.", CHE_ADMIN_NAME_PROPERTY)));
  }

  @Override
  public String getId() {
    return "id";
  }

  @Override
  public void delete() {}
}
