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
package org.eclipse.che.api.core.rest;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.server.JsonArrayImpl;
import org.eclipse.che.dto.server.JsonSerializable;
import org.eclipse.che.dto.server.JsonStringMapImpl;

/**
 * Simple implementation of {@link HttpJsonRequest} based on {@link HttpURLConnection}.
 *
 * <p>The implementation is not thread-safe, instance of this class must be created each time when
 * it's needed.
 *
 * <p>The instance of this request is reusable, which means that it is possible to call {@link
 * #request()} method more than one time per instance
 *
 * @author Yevhenii Voevodin
 * @see DefaultHttpJsonRequestFactory
 */
public class DefaultHttpJsonRequest implements HttpJsonRequest {

  private static final int DEFAULT_QUERY_PARAMS_LIST_SIZE = 5;
  private static final Object[] EMPTY_ARRAY = new Object[0];

  private final String url;

  private int timeout;
  private String method;
  private Object body;
  private List<Pair<String, ?>> queryParams;
  private List<Pair<String, String>> headers;
  private String authorizationHeaderValue;

  protected DefaultHttpJsonRequest(String url, String method) {
    this.url = requireNonNull(url, "Required non-null url");
    this.method = method;
  }

  protected DefaultHttpJsonRequest(String url) {
    this(url, HttpMethod.GET);
  }

  protected DefaultHttpJsonRequest(Link link) {
    this(requireNonNull(link, "Required non-null link").getHref(), link.getMethod());
  }

  @Override
  public HttpJsonRequest setMethod(@NotNull String method) {
    this.method = requireNonNull(method, "Required non-null http method");
    return this;
  }

  @Override
  public HttpJsonRequest setBody(@NotNull Object body) {
    this.body = requireNonNull(body, "Required non-null body");
    return this;
  }

  @Override
  public HttpJsonRequest setBody(@NotNull Map<String, String> map) {
    this.body = new JsonStringMapImpl<>(requireNonNull(map, "Required non-null body"));
    return this;
  }

  @Override
  public HttpJsonRequest setBody(@NotNull List<?> list) {
    this.body = new JsonArrayImpl<>(requireNonNull(list, "Required non-null body"));
    return this;
  }

  @Override
  public HttpJsonRequest addQueryParam(@NotNull String name, @NotNull Object value) {
    requireNonNull(name, "Required non-null query parameter name");
    requireNonNull(value, "Required non-null query parameter value");
    if (queryParams == null) {
      queryParams = new ArrayList<>(DEFAULT_QUERY_PARAMS_LIST_SIZE);
    }
    queryParams.add(Pair.of(name, value));
    return this;
  }

  public HttpJsonRequest addHeader(@NotNull String name, @NotNull String value) {
    requireNonNull(name, "Required non-null header name");
    requireNonNull(value, "Required non-null header value");
    if (headers == null) {
      headers = new ArrayList<>();
    }
    headers.add(Pair.of(name, value));
    return this;
  }

  @Override
  public HttpJsonRequest setAuthorizationHeader(@NotNull String value) {
    requireNonNull(value, "Required non-null header value");
    authorizationHeaderValue = value;
    return this;
  }

  @Override
  public HttpJsonRequest setTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  @Override
  public String getUrl() {
    final UriBuilder ub = UriBuilder.fromUri(url);
    if (queryParams != null) {
      for (Pair<String, ?> parameter : queryParams) {
        ub.queryParam(parameter.first, parameter.second);
      }
    }
    return ub.build().toString();
  }

  @Override
  public HttpJsonResponse request()
      throws IOException, ServerException, UnauthorizedException, ForbiddenException,
          NotFoundException, ConflictException, BadRequestException {
    if (method == null) {
      throw new IllegalStateException("Could not perform request, request method was not set.");
    }
    return doRequest(timeout, url, method, body, queryParams, authorizationHeaderValue, headers);
  }

  /**
   * Makes this request using {@link HttpURLConnection}.
   *
   * <p>Uses {@link HttpHeaders#AUTHORIZATION} header with value from {@link EnvironmentContext}.
   * <br>
   * uses {@link HttpHeaders#ACCEPT} header with "application/json" value. <br>
   * Encodes query parameters in "UTF-8".
   *
   * @param timeout request timeout, used only if it is greater than 0
   * @param url request url
   * @param method request method
   * @param body request body, must be instance of {@link JsonSerializable}
   * @param parameters query parameters, may be null
   * @param authorizationHeaderValue value of authorization header, may be null
   * @return response to this request
   * @throws IOException when connection content type is not "application/json"
   * @throws ServerException when response code is 500 or it is different from 400, 401, 403, 404,
   *     409
   * @throws ForbiddenException when response code is 403
   * @throws NotFoundException when response code is 404
   * @throws UnauthorizedException when response code is 401
   * @throws ConflictException when response code is 409
   * @throws BadRequestException when response code is 400
   */
  protected DefaultHttpJsonResponse doRequest(
      int timeout,
      String url,
      String method,
      Object body,
      List<Pair<String, ?>> parameters,
      String authorizationHeaderValue,
      List<Pair<String, String>> headers)
      throws IOException, ServerException, ForbiddenException, NotFoundException,
          UnauthorizedException, ConflictException, BadRequestException {
    final String authToken = EnvironmentContext.getCurrent().getSubject().getToken();
    final boolean hasQueryParams = parameters != null && !parameters.isEmpty();
    if (hasQueryParams || authToken != null) {
      final UriBuilder ub = UriBuilder.fromUri(url);
      // remove sensitive information from url.
      ub.replaceQueryParam("token", EMPTY_ARRAY);

      if (hasQueryParams) {
        for (Pair<String, ?> parameter : parameters) {
          ub.queryParam(parameter.first, parameter.second);
        }
      }
      url = ub.build().toString();
    }
    final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setConnectTimeout(timeout > 0 ? timeout : 60000);
    conn.setReadTimeout(timeout > 0 ? timeout : 60000);

    final boolean hasHeaders = headers != null && !headers.isEmpty();

    if (hasHeaders) {
      for (Pair<String, String> header : headers) {
        conn.setRequestProperty(header.first, header.second);
      }
    }

    try {
      conn.setRequestMethod(method);
      // drop a hint for server side that we want to receive application/json
      conn.addRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
      if (!isNullOrEmpty(authorizationHeaderValue)) {
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
      } else if (authToken != null) {
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, authToken);
      }
      if (body != null) {
        conn.addRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        conn.setDoOutput(true);

        if (HttpMethod.DELETE.equals(method)) { // to avoid jdk bug described here
          // http://bugs.java.com/view_bug.do?bug_id=7157360
          conn.setRequestMethod(HttpMethod.POST);
          conn.setRequestProperty("X-HTTP-Method-Override", HttpMethod.DELETE);
        }

        try (OutputStream output = conn.getOutputStream()) {
          output.write(DtoFactory.getInstance().toJson(body).getBytes());
        }
      }
      final int responseCode = conn.getResponseCode();
      if ((responseCode / 100) != 2) {
        InputStream in = conn.getErrorStream();
        if (in == null) {
          in = conn.getInputStream();
        }
        final String str;
        try (Reader reader = new InputStreamReader(in)) {
          str = CharStreams.toString(reader);
        }
        final String contentType = conn.getContentType();
        if (contentType != null
            && (contentType.startsWith(MediaType.APPLICATION_JSON)
                || contentType.startsWith("application/vnd.api+json"))) {
          final ServiceError serviceError =
              DtoFactory.getInstance().createDtoFromJson(str, ServiceError.class);
          if (serviceError.getMessage() != null) {
            if (responseCode == Response.Status.FORBIDDEN.getStatusCode()) {
              throw new ForbiddenException(serviceError);
            } else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
              throw new NotFoundException(serviceError);
            } else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
              throw new UnauthorizedException(serviceError);
            } else if (responseCode == Response.Status.CONFLICT.getStatusCode()) {
              throw new ConflictException(serviceError);
            } else if (responseCode == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
              throw new ServerException(serviceError);
            } else if (responseCode == Response.Status.BAD_REQUEST.getStatusCode()) {
              throw new BadRequestException(serviceError);
            }
            throw new ServerException(serviceError);
          }
        }
        // Can't parse content as json or content has format other we expect for error.
        throw new IOException(
            String.format(
                "Failed access: %s, method: %s, response code: %d, message: %s",
                UriBuilder.fromUri(url).replaceQuery("token").build(), method, responseCode, str));
      }
      final String contentType = conn.getContentType();
      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT
          && contentType != null
          && !(contentType.startsWith(MediaType.APPLICATION_JSON)
              || contentType.startsWith("application/vnd.api+json"))) {
        throw new IOException(conn.getResponseMessage());
      }

      try (Reader reader = new InputStreamReader(conn.getInputStream())) {
        return new DefaultHttpJsonResponse(
            CharStreams.toString(reader), responseCode, conn.getHeaderFields());
      }
    } finally {
      conn.disconnect();
    }
  }

  @Override
  public String toString() {
    return "DefaultHttpJsonRequest{"
        + "url='"
        + url
        + '\''
        + ", timeout="
        + timeout
        + ", method='"
        + method
        + '\''
        + ", body="
        + body
        + ", queryParams="
        + queryParams
        + '}';
  }
}
