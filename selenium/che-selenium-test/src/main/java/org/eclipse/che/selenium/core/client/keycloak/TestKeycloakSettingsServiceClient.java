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
package org.eclipse.che.selenium.core.client.keycloak;

import static java.lang.String.format;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import java.io.IOException;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;

/** @author Dmytro Nochevnov */
public class TestKeycloakSettingsServiceClient {

  private final String keycloakSettingsServiceUrl;
  private final DefaultHttpJsonRequestFactory requestFactory;
  private final Gson gson;

  @Inject
  public TestKeycloakSettingsServiceClient(
      TestApiEndpointUrlProvider cheApiEndpointProvider,
      DefaultHttpJsonRequestFactory requestFactory,
      Gson gson) {
    this.keycloakSettingsServiceUrl = format("%skeycloak/settings/", cheApiEndpointProvider.get());
    this.requestFactory = requestFactory;
    this.gson = gson;
  }

  public KeycloakSettings read() {
    try {
      return gson.fromJson(
          requestFactory.fromUrl(keycloakSettingsServiceUrl).useGetMethod().request().asString(),
          KeycloakSettings.class);
    } catch (ApiException | IOException | JsonSyntaxException ex) {
      throw new RuntimeException("Error during retrieving Che Keycloak configuration: ", ex);
    }
  }
}
