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
package org.eclipse.che.api.core.rest;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/**
 * Defines response of {@link HttpJsonRequest}.
 *
 * @author Yevhenii Voevodin
 */
@Beta
public interface HttpJsonResponse {

  /** Returns a response code. */
  int getResponseCode();

  /**
   * Returns {@link HttpJsonRequest} response body as a string, if response doesn't contain body -
   * empty line will be returned.
   *
   * <p>Example:
   *
   * <pre>{@code
   * String recipeContent = requestFactory.fromLink(getRecipeContentLink).requestString();
   * }</pre>
   */
  String asString();

  /**
   * Returns response body as instance of {@link DTO} object.
   *
   * <p>Example:
   *
   * <pre>{@code
   * UserDto user = requestFactory.fromUri(apiEndpoint + "/user")
   *                              .useGetMethod()
   *                              .request()
   *                              .asDto(UserDto.class);
   * }</pre>
   *
   * @param dtoInterface dto interface class
   * @return response as a dto instance
   */
  <T> T asDto(@NotNull Class<T> dtoInterface);

  /**
   * Returns result as a list of {@link DTO} objects.
   *
   * <p>Example:
   *
   * <pre>{@code
   * List<UsersWorkspaceDto> workspaces = requestFactory.fromUri(apiEndpoint + "/workspace/config")
   *                                                    .useGetMethod()
   *                                                    .request()
   *                                                    .asList(UsersWorkspaceDto.class);
   * }</pre>
   *
   * @param dtoInterface dto interface class
   * @return response as list of dto instances
   */
  <T> List<T> asList(@NotNull Class<T> dtoInterface);

  /**
   * Returns response body as a string map.
   *
   * <p>Example:
   *
   * <pre>{@code
   * Map<String, String> prefs = requestFactory.fromUri(apiEndpoint + "/profile/prefs")
   *                                           .useGetRequest()
   *                                           .addQueryParam("filter", ".*che.*")
   *                                           .request()
   *                                           .asProperties();
   * }</pre>
   *
   * @return response as a {@code Map<String, String>}
   * @throws IOException when response body is not valid json
   */
  Map<String, String> asProperties() throws IOException;

  /**
   * Returns response as a given type.
   *
   * <p>Example:
   *
   * <pre>{@code
   * Set<UsersWorkspaceDto> workspaces = requestFactory.fromUri(apiEndpoint + "/workspace/config")
   *                                                   .useGetMethod()
   *                                                   .request()
   *                                                   .as(Set.class, new TypeToken<Set<UsersWorkspace>>() {}.getType());
   * }</pre>
   *
   * @param <T> raw type of the response
   * @param clazz response class
   * @param genericType generic type of the response, if needed
   * @return response parsed to the given type
   * @throws IOException when response body is not valid json
   */
  <T> T as(@NotNull Class<T> clazz, @Nullable Type genericType) throws IOException;

  /**
   * Returns an unmodifiable Map of the header fields. The Map keys are Strings that represent the
   * response-header field names. Each Map value is an unmodifiable List of Strings that represents
   * the corresponding field values.
   */
  Map<String, List<String>> getHeaders();
}
