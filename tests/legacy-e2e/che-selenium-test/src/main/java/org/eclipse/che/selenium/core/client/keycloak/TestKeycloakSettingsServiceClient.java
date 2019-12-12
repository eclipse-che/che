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
package org.eclipse.che.selenium.core.client.keycloak;

import static java.lang.String.format;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
      trustAllTlsCertificates();

      return gson.fromJson(
          requestFactory.fromUrl(keycloakSettingsServiceUrl).useGetMethod().request().asString(),
          KeycloakSettings.class);
    } catch (ApiException
        | IOException
        | JsonSyntaxException
        | NoSuchAlgorithmException
        | KeyManagementException ex) {
      throw new RuntimeException("Error during retrieving Che Keycloak configuration: ", ex);
    }
  }

  /**
   * Trust all TLS certificates to be able to test Eclipse Che with self-signed TLS certificate
   * support
   *
   * @throws NoSuchAlgorithmException
   * @throws KeyManagementException
   */
  private void trustAllTlsCertificates() throws NoSuchAlgorithmException, KeyManagementException {
    TrustManager[] trustAllCerts =
        new TrustManager[] {
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
          }
        };

    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HostnameVerifier allHostsVerifier = (arg0, arg1) -> true;
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsVerifier);
  }
}
