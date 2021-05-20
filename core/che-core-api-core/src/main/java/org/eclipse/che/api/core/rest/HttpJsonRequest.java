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

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import javax.ws.rs.HttpMethod;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.JsonSerializable;

/**
 * Defines simple set of methods for requesting json objects.
 *
 * <p>Unlike {@link HttpRequestHelper} - provides <i>builder-like</i> style for building requests
 * and getting responses.
 *
 * <p>Simple use-cases:
 *
 * <pre>{@code
 *  // starting new workspace
 *  requestFactory.fromUri(apiEndpoint + "/workspace/" + id + "/runtime")
 *                .setMethod("POST")
 *                .addQueryParam("envName", envName)
 *                .addQueryParam("accountId", accountId)
 *                .request();
 *
 *  // getting user preferences
 *  Map<String, String> prefs = requestFactory.fromUri(apiEndpoint + "/profile/prefs")
 *                                            .setMethod("GET")
 *                                            .request()
 *                                            .asProperties();
 *
 * // getting workspace
 * UsersWorkspaceDto workspace = requestFactory.fromLink(getWorkspaceLink)
 *                                             .request()
 *                                             .asDto(UsersWorkspaceDto.class);
 * }</pre>
 *
 * <p>Do not use this class for requesting content different from "application/json".
 *
 * @author Yevhenii Voevodin
 * @see HttpJsonRequestFactory
 */
@Beta
public interface HttpJsonRequest {

  /**
   * Sets http method to use in this request(e.g. {@link javax.ws.rs.HttpMethod#GET GET}).
   *
   * @param method http method
   * @return this request instance
   * @throws NullPointerException when {@code method} is null
   */
  HttpJsonRequest setMethod(@NotNull String method);

  /**
   * Sets request body.
   *
   * @param body should be instance of {@link JsonSerializable}
   * @return this request instance
   * @throws NullPointerException when {@code body} is null
   */
  HttpJsonRequest setBody(@NotNull Object body);

  /**
   * Sets given string map as request body.
   *
   * @param map request body
   * @return this request instance
   * @throws NullPointerException when {@code body} is null
   */
  HttpJsonRequest setBody(@NotNull Map<String, String> map);

  /**
   * Sets given list as request body.
   *
   * <p>List should contain only {@link JsonSerializable} elements
   *
   * @param list list of {@link JsonSerializable}
   * @return this request instance
   * @throws NullPointerException when {@code body} is null
   */
  HttpJsonRequest setBody(@NotNull List<?> list);

  /**
   * Adds query parameter to the request.
   *
   * @param name query parameter name
   * @param value query parameter value
   * @return this request instance
   * @throws NullPointerException when either name or value is null
   */
  HttpJsonRequest addQueryParam(@NotNull String name, @NotNull Object value);

  /**
   * Adds header to the request.
   *
   * @param name header name
   * @param value header value
   * @return this request instance
   * @throws NullPointerException when either header name or value is null
   */
  HttpJsonRequest addHeader(@NotNull String name, @NotNull String value);

  /**
   * Adds authorization header to the request.
   *
   * @param value authorization header value
   * @return this request instance
   * @throws NullPointerException when value is null
   */
  HttpJsonRequest setAuthorizationHeader(@NotNull String value);

  /**
   * Sets request timeout in milliseconds.
   *
   * @param timeoutMs request timeout in milliseconds
   * @return this request instance
   */
  HttpJsonRequest setTimeout(int timeoutMs);

  /** Returns HTTP URL built from source URL and query parameters added to this request. */
  String getUrl();

  /**
   * Makes http request with content type "application/json" and authorization headers based on
   * current {@link EnvironmentContext#getCurrent() context}.
   *
   * @return {@link HttpJsonResponse} instance which represents response of this request
   * @throws IOException when server response content type is different from "application/json"(Not
   *     acceptable)
   * @throws IOException when any io error occurs
   * @throws ServerException when response code is 500 or it is different from 400, 401, 403, 404,
   *     409
   * @throws ForbiddenException when response code is 403
   * @throws NotFoundException when response code is 404
   * @throws UnauthorizedException when response code is 401
   * @throws ConflictException when response code is 409
   * @throws BadRequestException when response code is 400
   */
  HttpJsonResponse request()
      throws IOException, ServerException, UnauthorizedException, ForbiddenException,
          NotFoundException, ConflictException, BadRequestException;

  /**
   * Uses {@link HttpMethod#GET} as a request method.
   *
   * @return this request instance
   */
  default HttpJsonRequest useGetMethod() {
    return setMethod(HttpMethod.GET);
  }

  /**
   * Uses {@link HttpMethod#OPTIONS} as a request method.
   *
   * @return this request instance
   */
  default HttpJsonRequest useOptionsMethod() {
    return setMethod(HttpMethod.OPTIONS);
  }

  /**
   * Uses {@link HttpMethod#POST} as a request method.
   *
   * @return this request instance
   */
  default HttpJsonRequest usePostMethod() {
    return setMethod(HttpMethod.POST);
  }

  /**
   * Uses {@link HttpMethod#DELETE} as a request method.
   *
   * @return this request instance
   */
  default HttpJsonRequest useDeleteMethod() {
    return setMethod(HttpMethod.DELETE);
  }

  /**
   * Uses {@link HttpMethod#PUT} as a request method.
   *
   * @return this request instance
   */
  default HttpJsonRequest usePutMethod() {
    return setMethod(HttpMethod.PUT);
  }

  /**
   * Adds set of query parameters to this request.
   *
   * @param params query parameters map
   * @return this request instance
   */
  default HttpJsonRequest addQueryParams(@NotNull Map<String, ?> params) {
    Objects.requireNonNull(params, "Non-null query parameters required");
    params.forEach(this::addQueryParam);
    return this;
  }

  /**
   * Adds set of headers to this request.
   *
   * @param headers map with headers
   * @return this request instance
   */
  default HttpJsonRequest addHeaders(@NotNull Map<String, String> headers) {
    Objects.requireNonNull(headers, "Required non-null headers");
    headers.forEach(this::addHeader);
    return this;
  }
}
