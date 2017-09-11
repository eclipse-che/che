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
package org.eclipse.che.selenium.core.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Named;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

/** @author Dmytro Nochevnov */
@Singleton
public class CheTestKeycloakProvider implements TestKeycloakProvider {

  @Inject
  @Named("sys.protocol")
  private String protocol;

  @Inject
  @Named("sys.host")
  private String host;

  @Inject AdminTestUser admin;

  @Override
  public Keycloak get() {
    String authServerUrl = null;
    try {
      authServerUrl = new URL(protocol, host, 5050, "/auth").toString();
    } catch (MalformedURLException e) {
      throw new RuntimeException("There is an error of construction of url to auth service.", e);
    }

    return KeycloakBuilder.builder()
        .serverUrl(authServerUrl)
        .realm("master")
        .username(admin.getName())
        .password(admin.getPassword())
        .clientId("admin-cli")
        .build();
  }
}
