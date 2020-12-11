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
package org.eclipse.che.multiuser.keycloak.server;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import org.eclipse.che.commons.proxy.ProxyAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OIDCInfoProvider {

  private static final Logger LOG = LoggerFactory.getLogger(OIDCInfoProvider.class);

  private String tokenEndPoint;
  private String userInfoEndpoint;
  private String JWKS_URI;
  private String endSessionEndpoint;

  public void requestInfo(String wellKnownEndpoint) {
    LOG.info("Retrieving OpenId configuration from endpoint: {}", wellKnownEndpoint);
    ProxyAuthenticator.initAuthenticator(wellKnownEndpoint);

    try (InputStream inputStream = new URL(wellKnownEndpoint).openStream()) {
      final JsonParser parser = new JsonFactory().createParser(inputStream);
      final TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {};

      Map<String, Object> openIdConfiguration =
          new ObjectMapper().reader().readValue(parser, typeReference);

      LOG.info("openid configuration = {}", openIdConfiguration);

      this.tokenEndPoint = (String) openIdConfiguration.get("token_endpoint");
      this.userInfoEndpoint = (String) openIdConfiguration.get("userinfo_endpoint");
      this.JWKS_URI = (String) openIdConfiguration.get("jwks_uri");
      this.endSessionEndpoint = (String) openIdConfiguration.get("end_session_endpoint");
    } catch (IOException e) {
      throw new RuntimeException(
          "Exception while retrieving OpenId configuration from endpoint: " + wellKnownEndpoint, e);
    } finally {
      ProxyAuthenticator.resetAuthenticator();
    }
  }

  public String getTokenEndpoint() {
    return this.tokenEndPoint;
  }

  public String getUserInfoEndpoint() {
    return this.userInfoEndpoint;
  }

  public String getJWKS_URI() {
    return this.JWKS_URI;
  }

  public String getEndSessionEndpoint() {
    return this.endSessionEndpoint;
  }
}
