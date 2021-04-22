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
package org.eclipse.che.api.factory.server.gitlab;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.time.Duration.ofSeconds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.function.Function;
import org.eclipse.che.api.factory.server.scm.exception.ScmBadRequestException;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmItemNotFoundException;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** GitLab API operations helper. */
public class GitlabApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(GitlabApiClient.class);

  private final HttpClient httpClient;
  private final URI serverUrl;

  private static final Duration DEFAULT_HTTP_TIMEOUT = ofSeconds(10);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public GitlabApiClient(String serverUrl) {
    this.serverUrl = URI.create(serverUrl);
    this.httpClient =
        HttpClient.newBuilder()
            .executor(
                Executors.newCachedThreadPool(
                    new ThreadFactoryBuilder()
                        .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                        .setNameFormat(GitlabApiClient.class.getName() + "-%d")
                        .setDaemon(true)
                        .build()))
            .connectTimeout(DEFAULT_HTTP_TIMEOUT)
            .build();
  }

  public GitlabUser getUser(String authenticationToken)
      throws ScmItemNotFoundException, ScmCommunicationException, ScmBadRequestException {
    final URI uri = serverUrl.resolve("/api/v4/user");
    HttpRequest request =
        HttpRequest.newBuilder(uri)
            .headers("Authorization", "Bearer " + authenticationToken)
            .timeout(DEFAULT_HTTP_TIMEOUT)
            .build();
    LOG.trace("executeRequest={}", request);
    return executeRequest(
        httpClient,
        request,
        inputStream -> {
          try {
            return OBJECT_MAPPER.readValue(inputStream, GitlabUser.class);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  public GitlabOauthTokenInfo getTokenInfo(String authenticationToken)
      throws ScmItemNotFoundException, ScmCommunicationException {
    final URI uri = serverUrl.resolve("/oauth/token/info");
    HttpRequest request =
        HttpRequest.newBuilder(uri)
            .headers("Authorization", "Bearer " + authenticationToken)
            .timeout(DEFAULT_HTTP_TIMEOUT)
            .build();
    LOG.trace("executeRequest={}", request);
    try {
      return executeRequest(
          httpClient,
          request,
          inputStream -> {
            try {
              return OBJECT_MAPPER.readValue(inputStream, GitlabOauthTokenInfo.class);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    } catch (ScmBadRequestException e) {
      throw new ScmCommunicationException(e.getMessage(), e);
    }
  }

  private <T> T executeRequest(
      HttpClient httpClient, HttpRequest request, Function<InputStream, T> bodyConverter)
      throws ScmBadRequestException, ScmItemNotFoundException, ScmCommunicationException {
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

  public boolean isConnected(String scmServerUrl) {
    return serverUrl.equals(URI.create(scmServerUrl));
  }
}
