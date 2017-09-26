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

/** @author Anatolii Bazko */
@Singleton
public class OnpremAdminTestUser implements AdminTestUser {
  private static final String CODENVY_ADMIN_NAME_PROPERTY = "che.admin_user.email";
  private static final String CODENVY_ADMIN_PASSWORD_PROPERTY = "che.admin_user.password";
  private static final String CODENVY_ADMIN_INITIAL_PASSWORD_PROPERTY = "admin";

  @Inject(optional = true)
  @Named(CODENVY_ADMIN_NAME_PROPERTY)
  private String name;

  @Inject(optional = true)
  @Named(CODENVY_ADMIN_PASSWORD_PROPERTY)
  private String password;

  @Inject(optional = true)
  @Named(CODENVY_ADMIN_INITIAL_PASSWORD_PROPERTY)
  private String initialPassword;

  @Inject private TestAuthServiceClient authServiceClient;

  @Inject private Provider<TestUserServiceClient> userServiceClientProvider;

  private AtomicReference<String> authToken = new AtomicReference<>();
  private AtomicReference<String> id = new AtomicReference<>();

  @Override
  public String getEmail() {
    return name;
  }

  @Override
  public String getPassword() {
    if (password != null) {
      return password;
    }

    if (initialPassword != null) {
      return initialPassword;
    }

    throw new ConfigurationException(
        String.format(
            "Both properties '%s' and '%s' are undefined.",
            CODENVY_ADMIN_PASSWORD_PROPERTY, CODENVY_ADMIN_INITIAL_PASSWORD_PROPERTY));
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
    return ofNullable(name.split("@")[0])
        .orElseThrow(
            () ->
                new ConfigurationException(
                    format("Properties '%s' is undefined.", CODENVY_ADMIN_NAME_PROPERTY)));
  }

  @Override
  public String getId() {
    return ofNullable(id.get())
        .orElseGet(
            () ->
                id.updateAndGet(
                    (s) -> {
                      try {
                        return null; /*userServiceClientProvider.get().getUser(authToken.get()).getId();*/
                      } catch (Exception e) {
                        throw new IllegalStateException(e);
                      }
                    }));
  }

  @Override
  public void delete() {}
}
