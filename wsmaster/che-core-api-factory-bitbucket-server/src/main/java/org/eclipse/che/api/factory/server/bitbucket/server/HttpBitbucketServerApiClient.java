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
package org.eclipse.che.api.factory.server.bitbucket.server;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.time.Duration.ofSeconds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;
import com.google.common.net.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.factory.server.scm.exception.ScmBadRequestException;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmItemNotFoundException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of @{@link BitbucketServerApiClient} that is using @{@link HttpClient} to
 * communicate with Bitbucket Server.
 */
public class HttpBitbucketServerApiClient implements BitbucketServerApiClient {

  private static final ObjectMapper OM = new ObjectMapper();

  private static final Logger LOG = LoggerFactory.getLogger(HttpBitbucketServerApiClient.class);
  private static final Duration DEFAULT_HTTP_TIMEOUT = ofSeconds(10);
  private final URI serverUri;
  private final AuthorizationHeaderSupplier headerProvider;

  public HttpBitbucketServerApiClient(
      String serverUrl, AuthorizationHeaderSupplier authorizationHeaderSupplier) {
    this.serverUri = URI.create(serverUrl);
    this.headerProvider = authorizationHeaderSupplier;
  }

  @Override
  public boolean isConnected(String bitbucketServerUrl) {
    return serverUri.equals(URI.create(bitbucketServerUrl));
  }

  @Override
  public BitbucketUser getUser(Subject cheUser)
      throws ScmUnauthorizedException, ScmCommunicationException {
    try {
      Set<String> usersByName =
          getUsers(cheUser.getUserName())
              .stream()
              .map(BitbucketUser::getSlug)
              .collect(Collectors.toSet());

      Optional<BitbucketUser> currentUser = findCurrentUser(usersByName);
      if (currentUser.isPresent()) {
        return currentUser.get();
      }
      Set<String> usersAllExceptByName =
          getUsers()
              .stream()
              .map(BitbucketUser::getSlug)
              .filter(s -> !usersByName.contains(s))
              .collect(Collectors.toSet());
      currentUser = findCurrentUser(usersAllExceptByName);
      if (currentUser.isPresent()) {
        return currentUser.get();
      }
    } catch (ScmBadRequestException | ScmItemNotFoundException scmBadRequestException) {
      throw new ScmCommunicationException(
          scmBadRequestException.getMessage(), scmBadRequestException);
    }
    throw new ScmUnauthorizedException(
        "Current user not found. That is possible only if user are not authorized against "
            + serverUri);
  }

  @Override
  public BitbucketUser getUser(String slug)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException {
    HttpClient httpClient = HttpClient.newHttpClient();
    URI uri = serverUri.resolve("/rest/api/1.0/users/" + slug);
    HttpRequest request =
        HttpRequest.newBuilder(uri)
            .headers(
                "Authorization", headerProvider.computeAuthorizationHeader("GET", uri.toString()))
            .timeout(DEFAULT_HTTP_TIMEOUT)
            .build();

    try {
      LOG.debug("executeRequest={}", request);
      return executeRequest(
          httpClient,
          request,
          inputStream -> {
            try {
              return OM.readValue(inputStream, BitbucketUser.class);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    } catch (ScmBadRequestException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }

  @Override
  public List<BitbucketUser> getUsers()
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException {
    try {
      return doGetItems(BitbucketUser.class, "/rest/api/1.0/users", null);
    } catch (ScmItemNotFoundException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }

  @Override
  public List<BitbucketUser> getUsers(String filter)
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException {
    try {
      return doGetItems(BitbucketUser.class, "/rest/api/1.0/users", filter);
    } catch (ScmItemNotFoundException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }

  @Override
  public void deletePersonalAccessTokens(String userSlug, Long tokenId)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException {
    HttpClient httpClient = HttpClient.newHttpClient();
    URI uri = serverUri.resolve("/rest/access-tokens/1.0/users/" + userSlug + "/" + tokenId);
    HttpRequest request =
        HttpRequest.newBuilder(uri)
            .DELETE()
            .headers(
                HttpHeaders.AUTHORIZATION,
                headerProvider.computeAuthorizationHeader("DELETE", uri.toString()),
                HttpHeaders.ACCEPT,
                MediaType.APPLICATION_JSON,
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON)
            .timeout(DEFAULT_HTTP_TIMEOUT)
            .build();

    try {
      LOG.debug("executeRequest={}", request);
      executeRequest(
          httpClient,
          request,
          inputStream -> {
            try {
              return OM.readValue(inputStream, String.class);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    } catch (ScmBadRequestException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }

  @Override
  public BitbucketPersonalAccessToken createPersonalAccessTokens(
      String userSlug, String tokenName, Set<String> permissions)
      throws ScmBadRequestException, ScmUnauthorizedException, ScmCommunicationException {
    HttpClient httpClient = HttpClient.newHttpClient();
    URI uri = serverUri.resolve("/rest/access-tokens/1.0/users/" + userSlug);

    try {
      HttpRequest request =
          HttpRequest.newBuilder(uri)
              .PUT(
                  HttpRequest.BodyPublishers.ofString(
                      OM.writeValueAsString(
                          new BitbucketPersonalAccessToken(tokenName, permissions))))
              .headers(
                  HttpHeaders.AUTHORIZATION,
                  headerProvider.computeAuthorizationHeader("PUT", uri.toString()),
                  HttpHeaders.ACCEPT,
                  MediaType.APPLICATION_JSON,
                  HttpHeaders.CONTENT_TYPE,
                  MediaType.APPLICATION_JSON)
              .timeout(DEFAULT_HTTP_TIMEOUT)
              .build();
      LOG.debug("executeRequest={}", request);
      return executeRequest(
          httpClient,
          request,
          inputStream -> {
            try {
              return OM.readValue(inputStream, BitbucketPersonalAccessToken.class);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    } catch (ScmItemNotFoundException | JsonProcessingException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }

  @Override
  public List<BitbucketPersonalAccessToken> getPersonalAccessTokens(String userSlug)
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException {
    try {
      return doGetItems(
          BitbucketPersonalAccessToken.class, "/rest/access-tokens/1.0/users/" + userSlug, null);
    } catch (ScmBadRequestException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }

  private Optional<BitbucketUser> findCurrentUser(Set<String> userSlugs)
      throws ScmCommunicationException, ScmUnauthorizedException, ScmItemNotFoundException {

    for (String userSlug : userSlugs) {
      BitbucketUser user = getUser(userSlug);
      try {
        getPersonalAccessTokens(userSlug);
        return Optional.of(user);
      } catch (ScmItemNotFoundException | ScmUnauthorizedException e) {
        // ok
      }
    }
    return Optional.empty();
  }

  private <T> List<T> doGetItems(Class<T> tClass, String api, String filter)
      throws ScmUnauthorizedException, ScmCommunicationException, ScmBadRequestException,
          ScmItemNotFoundException {
    List<T> result = new ArrayList<>();
    Page<T> currentPage = doGetPage(tClass, api, 0, 25, filter);
    result.addAll(currentPage.getValues());
    while (!currentPage.isLastPage()) {
      currentPage = doGetPage(tClass, api, currentPage.getNextPageStart(), 25, filter);
      result.addAll(currentPage.getValues());
    }
    return result;
  }

  private <T> Page<T> doGetPage(Class<T> tClass, String api, int start, int limit, String filter)
      throws ScmUnauthorizedException, ScmBadRequestException, ScmCommunicationException,
          ScmItemNotFoundException {
    HttpClient httpClient = HttpClient.newHttpClient();
    String suffix = api + "?start=" + start + "&limit=" + limit;
    if (!Strings.isNullOrEmpty(filter)) {
      suffix += "&filter=" + filter;
    }

    URI uri = serverUri.resolve(suffix);
    HttpRequest request =
        HttpRequest.newBuilder(uri)
            .headers(
                "Authorization", headerProvider.computeAuthorizationHeader("GET", uri.toString()))
            .timeout(DEFAULT_HTTP_TIMEOUT)
            .build();
    LOG.debug("executeRequest={}", request);
    final JavaType typeReference =
        TypeFactory.defaultInstance().constructParametricType(Page.class, tClass);
    return executeRequest(
        httpClient,
        request,
        inputStream -> {
          try {
            return OM.readValue(inputStream, typeReference);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  private <T> T executeRequest(
      HttpClient httpClient, HttpRequest request, Function<InputStream, T> bodyConverter)
      throws ScmBadRequestException, ScmItemNotFoundException, ScmCommunicationException,
          ScmUnauthorizedException {
    try {
      HttpResponse<InputStream> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
      LOG.trace("executeRequest={} response {}", request, response.statusCode());
      if (response.statusCode() == 200) {
        return bodyConverter.apply(response.body());
      } else if (response.statusCode() == 204) {
        return null;
      } else {
        String body = CharStreams.toString(new InputStreamReader(response.body(), Charsets.UTF_8));
        switch (response.statusCode()) {
          case HTTP_BAD_REQUEST:
            throw new ScmBadRequestException(body);
          case HTTP_UNAUTHORIZED:
            throw new ScmUnauthorizedException(body);
          case HTTP_NOT_FOUND:
            throw new ScmItemNotFoundException(body);
          default:
            throw new ScmCommunicationException(
                "Unexpected status code " + response.statusCode() + " " + response.toString());
        }
      }
    } catch (IOException | InterruptedException | UncheckedIOException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }
}
