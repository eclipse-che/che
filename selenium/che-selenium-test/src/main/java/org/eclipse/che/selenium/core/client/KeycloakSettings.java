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
package org.eclipse.che.selenium.core.client;

import com.google.gson.annotations.SerializedName;

/** @author Anton Korneta */
public class KeycloakSettings {
  @SerializedName("che.keycloak.token.endpoint")
  private String keycloakTokenEndpoint;

  @SerializedName("che.keycloak.profile.endpoint")
  private String keycloakProfileEndpoint;

  @SerializedName("che.keycloak.auth_server_url")
  private String keycloakAuthServerUrl;

  @SerializedName("che.keycloak.password.endpoint")
  private String keycloakPasswordEndpoint;

  @SerializedName("che.keycloak.logout.endpoint")
  private String keycloakLogoutEndpoint;

  @SerializedName("che.keycloak.realm")
  private String keycloakRealm;

  public String getKeycloakTokenEndpoint() {
    return keycloakTokenEndpoint;
  }

  public String getKeycloakProfileEndpoint() {
    return keycloakProfileEndpoint;
  }

  public String getKeycloakAuthServerUrl() {
    return keycloakAuthServerUrl;
  }

  public String getKeycloakPasswordEndpoint() {
    return keycloakPasswordEndpoint;
  }

  public String getKeycloakLogoutEndpoint() {
    return keycloakLogoutEndpoint;
  }

  public String getKeycloakRealm() {
    return keycloakRealm;
  }
}
