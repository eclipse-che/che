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

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Provides helper method to send HTTP requests.
 *
 * @author andrew00x
 */
public class HttpRequestHelper {

  private HttpRequestHelper() {}

  public static HttpJsonRequest createJsonRequest(String url) {
    return new DefaultHttpJsonRequest(url);
  }

  public static String requestString(
      String url, String method, Object body, Pair<String, ?>... parameters)
      throws IOException, ServerException, ForbiddenException, NotFoundException,
          UnauthorizedException, ConflictException {
    return requestString(-1, url, method, body, parameters);
  }

  public static String requestString(
      int timeout, String url, String method, Object body, Pair<String, ?>... parameters)
      throws IOException, ServerException, ForbiddenException, NotFoundException,
          UnauthorizedException, ConflictException {
    final String authToken = EnvironmentContext.getCurrent().getSubject().getToken();
    if ((parameters != null && parameters.length > 0) || authToken != null) {
      final UriBuilder ub = UriBuilder.fromUri(url);
      // remove sensitive information from url.
      ub.replaceQueryParam("token", null);

      if (parameters != null && parameters.length > 0) {
        for (Pair<String, ?> parameter : parameters) {
          ub.queryParam(parameter.first, parameter.second);
        }
      }
      url = ub.build().toString();
    }
    final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setConnectTimeout(timeout > 0 ? timeout : 60000);
    conn.setReadTimeout(timeout > 0 ? timeout : 60000);
    try {
      conn.setRequestMethod(method);
      // drop a hint for server side that we want to receive application/json
      //            conn.addRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
      if (authToken != null) {
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, authToken);
      }
      if (body != null) {
        //                conn.addRequestProperty(HttpHeaders.CONTENT_TYPE,
        // MediaType.APPLICATION_JSON);
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
        if (contentType != null && contentType.startsWith(MediaType.APPLICATION_JSON)) {
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

      //            final String contentType = conn.getContentType();

      //            if (!(contentType == null ||
      // contentType.startsWith(MediaType.APPLICATION_JSON))) {
      //                throw new IOException(conn.getResponseMessage());
      //            }

      try (Reader reader = new InputStreamReader(conn.getInputStream())) {
        return CharStreams.toString(reader);
      }
    } finally {
      conn.disconnect();
    }
  }
}
