/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_INTERNAL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.OIDC_PROVIDER_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.proxy.ProxyAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OIDCInfoProvider retrieves OpenID Connect (OIDC) configuration for well-known endpoint. These
 * information is useful to provide access to the Keycloak api.
 */
public class OIDCInfoProvider implements Provider<OIDCInfo> {

  private static final Logger LOG = LoggerFactory.getLogger(OIDCInfoProvider.class);

  @Inject
  @Nullable
  @Named(AUTH_SERVER_URL_SETTING)
  protected String serverURL;

  @Inject
  @Nullable
  @Named(AUTH_SERVER_URL_INTERNAL_SETTING)
  protected String serverInternalURL;

  @Inject
  @Nullable
  @Named(OIDC_PROVIDER_SETTING)
  protected String oidcProviderUrl;

  @Inject
  @Nullable
  @Named(REALM_SETTING)
  protected String realm;

  /** @return OIDCInfo with OIDC settings information. */
  @Override
  public OIDCInfo get() {
    this.validate();

    String serverAuthUrl = (serverInternalURL != null) ? serverInternalURL : serverURL;
    String wellKnownEndpoint = this.getWellKnownEndpoint(serverAuthUrl);

    LOG.info("Retrieving OpenId configuration from endpoint: {}", wellKnownEndpoint);
    ProxyAuthenticator.initAuthenticator(wellKnownEndpoint);
    try (InputStream inputStream = new URL(wellKnownEndpoint).openStream()) {
      final JsonParser parser = new JsonFactory().createParser(inputStream);
      final TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {};

      Map<String, Object> openIdConfiguration =
          new ObjectMapper().reader().readValue(parser, typeReference);

      LOG.info("openid configuration = {}", openIdConfiguration);

      String tokenPublicEndPoint = setPublicUrl((String) openIdConfiguration.get("token_endpoint"));
      String userInfoPublicEndpoint =
          setPublicUrl((String) openIdConfiguration.get("userinfo_endpoint"));
      String endSessionPublicEndpoint =
          setPublicUrl((String) openIdConfiguration.get("end_session_endpoint"));
      String jwksPublicUri = setPublicUrl((String) openIdConfiguration.get("jwks_uri"));
      String jwksUri = setInternalUrl(jwksPublicUri);
      String userInfoEndpoint = setInternalUrl(userInfoPublicEndpoint);

      return new OIDCInfo(
          tokenPublicEndPoint,
          endSessionPublicEndpoint,
          userInfoPublicEndpoint,
          userInfoEndpoint,
          jwksPublicUri,
          jwksUri,
          serverAuthUrl,
          serverURL);
    } catch (IOException e) {
      throw new RuntimeException(
          "Exception while retrieving OpenId configuration from endpoint: " + wellKnownEndpoint, e);
    } finally {
      ProxyAuthenticator.resetAuthenticator();
    }
  }

  private String getWellKnownEndpoint(String serverAuthUrl) {
    String wellKnownEndpoint = firstNonNull(oidcProviderUrl, serverAuthUrl + "/realms/" + realm);
    if (!wellKnownEndpoint.endsWith("/")) {
      wellKnownEndpoint = wellKnownEndpoint + "/";
    }
    wellKnownEndpoint += ".well-known/openid-configuration";
    return wellKnownEndpoint;
  }

  private void validate() {
    if (serverURL == null && serverInternalURL == null && oidcProviderUrl == null) {
      throw new RuntimeException(
          "Either the '"
              + AUTH_SERVER_URL_SETTING
              + "' or '"
              + AUTH_SERVER_URL_INTERNAL_SETTING
              + "' or '"
              + OIDC_PROVIDER_SETTING
              + "' property should be set");
    }

    if (oidcProviderUrl == null && realm == null) {
      throw new RuntimeException("The '" + REALM_SETTING + "' property should be set");
    }
  }

  private String setInternalUrl(String endpointUrl) {
    if (serverURL != null && serverInternalURL != null) {
      return endpointUrl.replace(serverURL, serverInternalURL);
    }
    return endpointUrl;
  }

  private String setPublicUrl(String endpointUrl) {
    if (serverURL != null && serverInternalURL != null) {
      return endpointUrl.replace(serverInternalURL, serverURL);
    }
    return endpointUrl;
  }
}
