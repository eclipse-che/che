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
package org.eclipse.che.multiuser.keycloak.server;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.UriBuilder.fromUri;

import com.google.common.io.CharStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Implementation of {@link HttpJsonRequest} for Keycloak requests.
 *
 * @author Roman Nikitenko
 * @see KeycloakHttpJsonRequestFactory
 */
public class KeycloakHttpJsonRequest extends DefaultHttpJsonRequest {
  private static final String ERROR_FIELD = "errorMessage";
  private static final String NOT_AUTHORIZED_PATTERN = "Client .+ not authorized .+";
  private static final String NOT_ASSOCIATED_PATTERN =
      "User .+ is not associated with identity provider .+";

  protected KeycloakHttpJsonRequest(String url, String method) {
    super(url, method);
    setAuthorizationHeader("bearer " + EnvironmentContext.getCurrent().getSubject().getToken());
  }

  protected KeycloakHttpJsonRequest(String url) {
    this(url, HttpMethod.GET);
  }

  protected KeycloakHttpJsonRequest(Link link) {
    this(requireNonNull(link, "Required non-null link").getHref(), link.getMethod());
  }

  @Override
  protected void onConnectionFailed(HttpURLConnection conn)
      throws IOException, ForbiddenException, NotFoundException, UnauthorizedException,
          ConflictException, ServerException, BadRequestException {
    InputStream in = conn.getErrorStream();
    if (in == null) {
      in = conn.getInputStream();
    }

    String message;
    try (Reader reader = new InputStreamReader(in)) {
      message = CharStreams.toString(reader);
    }

    String serviceError = null;
    String contentType = conn.getContentType();
    if (contentType != null
        && (contentType.startsWith(MediaType.APPLICATION_JSON)
            || contentType.startsWith("application/vnd.api+json"))) {

      try {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(message);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.has(ERROR_FIELD)) {
          serviceError = jsonObject.get(ERROR_FIELD).getAsString();
        }
      } catch (Exception e) {
        // ignore
      }
    }

    int responseCode = conn.getResponseCode();
    if (serviceError == null) {
      // Can't parse content as json or content has format other we expect for error.
      throw new IOException(
          format(
              "Failed access: %s, method: %s, response code: %d, message: %s",
              fromUri(url).replaceQuery("token").build(), method, responseCode, message));
    }

    if (responseCode == UNAUTHORIZED.getStatusCode()
        || serviceError.matches(NOT_AUTHORIZED_PATTERN)
        || serviceError.matches(NOT_ASSOCIATED_PATTERN)) {
      throw new UnauthorizedException(serviceError);
    } else if (responseCode == NOT_FOUND.getStatusCode()) {
      throw new NotFoundException(serviceError);
    } else if (responseCode == FORBIDDEN.getStatusCode()) {
      throw new ForbiddenException(serviceError);
    } else if (responseCode == CONFLICT.getStatusCode()) {
      throw new ConflictException(serviceError);
    } else if (responseCode == INTERNAL_SERVER_ERROR.getStatusCode()) {
      throw new ServerException(serviceError);
    } else if (responseCode == BAD_REQUEST.getStatusCode()) {
      throw new BadRequestException(serviceError);
    }
    throw new ServerException(serviceError);
  }
}
