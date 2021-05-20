/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.api.client.util.store.MemoryDataStoreFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.security.oauth.shared.User;

/** OAuth authentication for github account. */
@Singleton
public class GitHubOAuthAuthenticator extends OAuthAuthenticator {
  @Inject
  public GitHubOAuthAuthenticator(
      @Nullable @Named("che.oauth.github.clientid") String clientId,
      @Nullable @Named("che.oauth.github.clientsecret") String clientSecret,
      @Nullable @Named("che.oauth.github.redirecturis") String[] redirectUris,
      @Nullable @Named("che.oauth.github.authuri") String authUri,
      @Nullable @Named("che.oauth.github.tokenuri") String tokenUri)
      throws IOException {
    if (!isNullOrEmpty(clientId)
        && !isNullOrEmpty(clientSecret)
        && !isNullOrEmpty(authUri)
        && !isNullOrEmpty(tokenUri)
        && redirectUris != null
        && redirectUris.length != 0) {

      configure(
          clientId, clientSecret, redirectUris, authUri, tokenUri, new MemoryDataStoreFactory());
    }
  }

  @Override
  public User getUser(OAuthToken accessToken) throws OAuthAuthenticationException {
    GitHubUser user =
        getJson(
            "https://api.github.com/user?access_token=" + accessToken.getToken(), GitHubUser.class);

    GithubEmail[] result =
        getJson2(
            "https://api.github.com/user/emails?access_token=" + accessToken.getToken(),
            GithubEmail[].class,
            null);

    GithubEmail verifiedEmail = null;
    for (GithubEmail email : result) {
      if (email.isPrimary() && email.isVerified()) {
        verifiedEmail = email;
        break;
      }
    }
    if (verifiedEmail == null
        || verifiedEmail.getEmail() == null
        || verifiedEmail.getEmail().isEmpty()) {
      throw new OAuthAuthenticationException(
          "Sorry, we failed to find any verified emails associated with your GitHub account."
              + " Please, verify at least one email in your GitHub account and try to connect with GitHub again.");
    }
    user.setEmail(verifiedEmail.getEmail());
    final String email = user.getEmail();
    try {
      new InternetAddress(email).validate();
    } catch (AddressException e) {
      throw new OAuthAuthenticationException(e.getMessage());
    }
    return user;
  }

  protected <O> O getJson2(String getUserUrl, Class<O> userClass, Type type)
      throws OAuthAuthenticationException {
    HttpURLConnection urlConnection = null;
    InputStream urlInputStream = null;

    try {
      urlConnection = (HttpURLConnection) new URL(getUserUrl).openConnection();
      urlConnection.setRequestProperty("Accept", "application/vnd.github.v3.html+json");
      urlInputStream = urlConnection.getInputStream();
      return JsonHelper.fromJson(urlInputStream, userClass, type);
    } catch (JsonParseException | IOException e) {
      throw new OAuthAuthenticationException(e.getMessage(), e);
    } finally {
      if (urlInputStream != null) {
        try {
          urlInputStream.close();
        } catch (IOException ignored) {
        }
      }

      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
  }

  @Override
  public final String getOAuthProvider() {
    return "github";
  }

  @Override
  public OAuthToken getToken(String userId) throws IOException {
    final OAuthToken token = super.getToken(userId);
    if (!(token == null || token.getToken() == null || token.getToken().isEmpty())) {
      // Need to check if token which stored is valid for requests, then if valid - we returns it to
      // caller
      String tokenVerifyUrl = "https://api.github.com/?access_token=" + token.getToken();
      HttpURLConnection http = null;
      try {
        http = (HttpURLConnection) new URL(tokenVerifyUrl).openConnection();
        http.setInstanceFollowRedirects(false);
        http.setRequestMethod("GET");
        http.setRequestProperty("Accept", "application/json");

        if (http.getResponseCode() == 401) {
          return null;
        }
      } finally {
        if (http != null) {
          http.disconnect();
        }
      }

      return token;
    }
    return null;
  }

  /**
   * information for each email address indicating if the address has been verified and if it’s the
   * user’s primary email address for GitHub.
   */
  public static class GithubEmail {
    private boolean primary;
    private boolean verified;
    private String email;

    public boolean isPrimary() {
      return primary;
    }

    public void setPrimary(boolean primary) {
      this.primary = primary;
    }

    public boolean isVerified() {
      return verified;
    }

    public void setVerified(boolean verified) {
      this.verified = verified;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }
  }
}
