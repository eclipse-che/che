/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.rest;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Default implementation of {@link HttpJsonResponse}.
 *
 * @author Yevhenii Voevodin
 */
public class DefaultHttpJsonResponse implements HttpJsonResponse {

  private static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();

  private final String responseBody;
  private final int responseCode;
  private final Map<String, List<String>> headers;

  protected DefaultHttpJsonResponse(String response, int responseCode) {
    this.responseBody = response;
    this.responseCode = responseCode;
    this.headers = Collections.emptyMap();
  }

  protected DefaultHttpJsonResponse(
      String response, int responseCode, Map<String, List<String>> headers) {
    this.responseBody = response;
    this.responseCode = responseCode;
    this.headers =
        unmodifiableMap(
            headers
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> unmodifiableList(e.getValue()))));
  }

  @Override
  public String asString() {
    return responseBody;
  }

  @Override
  public <T> T asDto(Class<T> dtoInterface) {
    requireNonNull(dtoInterface, "Required non-null dto interface");
    return DtoFactory.getInstance().createDtoFromJson(responseBody, dtoInterface);
  }

  @Override
  public <T> List<T> asList(Class<T> dtoInterface) {
    requireNonNull(dtoInterface, "Required non-null dto interface");
    return DtoFactory.getInstance().createListDtoFromJson(responseBody, dtoInterface);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, String> asProperties() throws IOException {
    return as(Map.class, STRING_MAP_TYPE);
  }

  @Override
  public <T> T as(Class<T> clazz, Type genericType) throws IOException {
    requireNonNull(clazz, "Required non-null class");
    try {
      return JsonHelper.fromJson(responseBody, clazz, genericType);
    } catch (JsonParseException jsonEx) {
      throw new IOException(jsonEx.getLocalizedMessage(), jsonEx);
    }
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  @Override
  public int getResponseCode() {
    return responseCode;
  }
}
