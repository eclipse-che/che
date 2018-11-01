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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for obtaining active token from OSIO using login/pass credentials. As fabric8-auth service
 * does not have REST endpoint for login, the logic for obtaining active token is not so simple:
 *
 * <p>1) Access <auth.service.url>/api/login?redirect=che.openshift.io (the redirect URL doesn't
 * matter). This redirects us to redhat developer portal login page. 2) Parse that page - obtain
 * `action` attribute of the login form 3) Send login request to URL obtained in step 2) and follow
 * redirects until `token_json` appears in redirect URL.
 *
 * @author rhopp
 */
@Singleton
public class OsioKeycloakTestAuthServiceClient extends AbstractKeycloakTestAuthServiceClient {

  private static final Logger LOG =
      LoggerFactory.getLogger(OsioKeycloakTestAuthServiceClient.class);
  private static final String REFRESH_TOKEN_TEMPLATE = "{\"refresh_token\":\"%s\"}";

  private final String osioAuthEndpoint;

  @Inject
  public OsioKeycloakTestAuthServiceClient(@Named("che.osio.auth.endpoint") String authEndpoint) {
    super();
    this.osioAuthEndpoint = authEndpoint;
  }

  private KeycloakToken obtainActiveToken(String username, String password)
      throws IOException, MalformedURLException, ProtocolException, UnsupportedEncodingException {
    HttpURLConnection.setFollowRedirects(true);
    HttpURLConnection conn;
    String formPostURL = loginAndGetFormPostURL();

    HttpURLConnection.setFollowRedirects(false);

    conn = fillFormAndCreateConnection(formPostURL, username, password);

    String tokenJsonString = followRedirects(conn);

    // "token_json={}"
    String tokenJson =
        URLDecoder.decode(new URL(tokenJsonString).getQuery(), "UTF-8").substring(11);
    KeycloakToken readerToKeycloakToken = readerToKeycloakToken(new StringReader(tokenJson));
    return readerToKeycloakToken;
  }

  @Override
  public void logout(String token) throws Exception {
    // NOT SUPPORTED ON OSIO
  }

  @Override
  protected KeycloakToken loginRequest(String username, String password) {
    try {
      return obtainActiveToken(username, password);
    } catch (IOException e) {
      throw new RuntimeException("Unable to obtain active token", e);
    }
  }

  @Override
  protected KeycloakToken refreshRequest(KeycloakToken token) {
    KeycloakToken newToken = null;
    HttpURLConnection http = null;
    try {
      http = (HttpURLConnection) new URL(osioAuthEndpoint + "/api/token/refresh").openConnection();
      http.setRequestMethod(HttpMethod.POST);
      http.setAllowUserInteraction(false);
      http.setInstanceFollowRedirects(true);
      http.setRequestProperty("Content-Type", "application/json");
      http.setDoOutput(true);
      OutputStream output = http.getOutputStream();
      output.write(String.format(REFRESH_TOKEN_TEMPLATE, token.getRefreshToken()).getBytes(UTF_8));
      output.close();
      if (http.getResponseCode() != 200) {
        throw new RuntimeException(
            "Cannot get access token using the KeyCloak REST API. Server response code: "
                + osioAuthEndpoint
                + " "
                + http.getResponseCode()
                + IoUtil.readStream(http.getErrorStream()));
      }

      // request was ok. Obtain token from inputStream. Response is in format
      // {"token":{"active_token":"<token>", ...}} Need to get rid of the root "token" element.

      final BufferedReader response =
          new BufferedReader(new InputStreamReader(http.getInputStream(), UTF_8));
      String responseString = IOUtils.toString(response);
      JsonParser parser = new JsonParser();
      JsonObject parse = parser.parse(responseString).getAsJsonObject();
      JsonElement jsonElement = parse.get("token");

      newToken = readerToKeycloakToken(new StringReader(jsonElement.toString()));

    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
    } finally {
      if (http != null) {
        http.disconnect();
      }
    }

    return newToken;
  }

  private HttpURLConnection fillFormAndCreateConnection(
      String formPostURL, String username, String password)
      throws IOException, MalformedURLException, ProtocolException, UnsupportedEncodingException {
    HttpURLConnection conn;
    conn = (HttpURLConnection) new URL(formPostURL).openConnection();
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);

    Map<String, String> arguments = new HashMap<>();
    arguments.put("username", username);
    arguments.put("password", password);
    arguments.put("login", "Log+In");
    StringJoiner sj = new StringJoiner("&");
    for (Map.Entry<String, String> entry : arguments.entrySet())
      sj.add(
          URLEncoder.encode(entry.getKey(), "UTF-8")
              + "="
              + URLEncoder.encode(entry.getValue(), "UTF-8"));
    byte[] out = sj.toString().getBytes(UTF_8);
    try (OutputStream os = conn.getOutputStream()) {
      os.write(out);
    }
    return conn;
  }

  private String followRedirects(URLConnection conn) throws IOException {
    int responseCode = ((HttpURLConnection) conn).getResponseCode();
    if (responseCode > 300 && responseCode < 309) {
      String location = conn.getHeaderField("Location");
      if (location.contains("token_json")) {
        return location;
      } else {
        return followRedirects(new URL(location).openConnection());
      }
    } else {
      throw new RuntimeException("Unable to obtain active token.");
    }
  }

  private String loginAndGetFormPostURL()
      throws IOException, MalformedURLException, ProtocolException {
    CookieManager cookieManager = new CookieManager();
    CookieHandler.setDefault(cookieManager);
    HttpURLConnection conn =
        (HttpURLConnection)
            new URL(osioAuthEndpoint + "/api/login?redirect=https://che.openshift.io")
                .openConnection();
    conn.setRequestMethod("GET");

    String htmlOutput = IOUtils.toString(conn.getInputStream());
    Pattern p = Pattern.compile("action=\"(.*?)\"");
    Matcher m = p.matcher(htmlOutput);
    if (m.find()) {
      String formPostURL = StringEscapeUtils.unescapeHtml(m.group(1));
      return formPostURL;
    } else {
      LOG.error("Unable to login - didn't find URL to send login form to.");
      throw new RuntimeException("Unable to login - didn't find URL to send login form to.");
    }
  }
}
