/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.rest;

import com.google.common.io.CharStreams;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Provides helper method to send HTTP requests with JSON content.
 *
 * @author andrew00x
 */
public class HttpJsonHelper {
    @SuppressWarnings("unchecked")
    private static final Pair<String, ?>[] EMPTY = new Pair[0];

    /**
     * Implementation  HttpJsonHelper methods.
     */
    private static HttpJsonHelperImpl httpJsonHelperImpl = new HttpJsonHelperImpl();


    //==============================================================
    public static <DTO> DTO request(Class<DTO> dtoInterface, Link link, Object body, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, link.getHref(), link.getMethod(), body, parameters);
    }

    public static <DTO> DTO request(Class<DTO> dtoInterface, int timeout, Link link, Object body, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, timeout, link.getHref(), link.getMethod(), body, parameters);
    }

    public static <DTO> DTO request(Class<DTO> dtoInterface, Link link, Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        return request(dtoInterface, link, null, parameters);
    }

    public static <DTO> DTO request(Class<DTO> dtoInterface, int timeout, Link link, Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        return request(dtoInterface, timeout, link, null, parameters);
    }

    public static <DTO> DTO request(Class<DTO> dtoInterface, Link link)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, link, EMPTY);
    }

    public static <DTO> DTO request(Class<DTO> dtoInterface, int timeout, Link link)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, timeout, link, EMPTY);
    }

    public static <DTO> List<DTO> requestArray(Class<DTO> dtoInterface, Link link, Object body, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return requestArray(dtoInterface, link.getHref(), link.getMethod(), body, parameters);
    }

    public static <DTO> List<DTO> requestArray(Class<DTO> dtoInterface, int timeout, Link link, Object body, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return requestArray(dtoInterface, timeout, link.getHref(), link.getMethod(), body, parameters);
    }

    public static <DTO> List<DTO> requestArray(Class<DTO> dtoInterface, Link link, Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        return requestArray(dtoInterface, link, null, parameters);
    }

    public static <DTO> List<DTO> requestArray(Class<DTO> dtoInterface, int timeout, Link link, Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        return requestArray(dtoInterface, timeout, link, null, parameters);
    }

    public static <DTO> List<DTO> requestArray(Class<DTO> dtoInterface, Link link)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return requestArray(dtoInterface, link, EMPTY);
    }

    public static <DTO> List<DTO> requestArray(Class<DTO> dtoInterface, int timeout, Link link)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return requestArray(dtoInterface, timeout, link, EMPTY);
    }

    public static String requestString(String url,
                                       String method,
                                       Object body,
                                       Pair<String, ?>... parameters)
            throws IOException, ServerException, ForbiddenException, NotFoundException, UnauthorizedException, ConflictException {
        return httpJsonHelperImpl.requestString(url, method, body, parameters);
    }

    public static String requestString(int timeout,
                                       String url,
                                       String method,
                                       Object body,
                                       Pair<String, ?>... parameters)
            throws IOException, ServerException, ForbiddenException, NotFoundException, UnauthorizedException, ConflictException {
        return httpJsonHelperImpl.requestString(timeout, url, method, body, parameters);
    }

    public static void request(String url,
                               String method,
                               Object body,
                               Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        httpJsonHelperImpl.request(url, method, body, parameters);
    }

    /**
     * Sends HTTP request to specified {@code url}.
     * <p/>
     * <p/>
     * type of expected response. If server returns some content we try parse it and restore object of the specified type from it.
     * Specified interface must be annotated with &#064DTO.
     *
     * @param url
     *         URL to send request
     * @param method
     *         HTTP method
     * @param body
     *         body of request. Object must implements DTO interface (interface must be annotated with &#064DTO).
     * @param parameters
     *         additional query parameters.
     * @return instance of {@code dtoInterface} which represents JSON response from the server
     * @throws ServerException
     *         if server returns error response in supported JSON format, see {@link org.eclipse.che.api.core.rest.shared.dto.ServiceError}
     * @throws IOException
     *         if any other error occurs
     * @see org.eclipse.che.dto.shared.DTO
     */
    public static <DTO> DTO request(Class<DTO> dtoInterface,
                                    String url,
                                    String method,
                                    Object body,
                                    Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        return httpJsonHelperImpl.request(dtoInterface, url, method, body, parameters);
    }

    public static <DTO> DTO request(Class<DTO> dtoInterface,
                                    int timeout,
                                    String url,
                                    String method,
                                    Object body,
                                    Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        return httpJsonHelperImpl.request(dtoInterface, timeout, url, method, body, parameters);
    }

    public static <DTO> List<DTO> requestArray(Class<DTO> dtoInterface,
                                               String url,
                                               String method,
                                               Object body,
                                               Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        return httpJsonHelperImpl.requestArray(dtoInterface, url, method, body, parameters);
    }

    public static <DTO> List<DTO> requestArray(Class<DTO> dtoInterface,
                                               int timeout,
                                               String url,
                                               String method,
                                               Object body,
                                               Pair<String, ?>... parameters)
            throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
        return httpJsonHelperImpl.requestArray(dtoInterface, timeout, url, method, body, parameters);
    }

    /**
     * Sends GET request to specified {@code url}.
     *
     * @param dtoInterface
     *         type of expected response. If server returns some content we try parse it and restore object of the specified type from it.
     *         Specified interface must be annotated with &#064DTO.
     * @param url
     *         URL to send request
     * @param parameters
     *         additional query parameters.
     * @return instance of {@code dtoInterface} which represents JSON response from the server
     * @throws ServerException
     *         if server returns error response in supported JSON format, see {@link org.eclipse.che.api.core.rest.shared.dto.ServiceError}
     * @throws IOException
     *         if any other error occurs
     * @see org.eclipse.che.dto.shared.DTO
     */
    public static <DTO> DTO get(Class<DTO> dtoInterface, String url, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, url, HttpMethod.GET, null, parameters);
    }

    public static <DTO> DTO get(Class<DTO> dtoInterface, int timeout, String url, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, timeout, url, HttpMethod.GET, null, parameters);
    }

    /**
     * Sends POST request to specified {@code url}.
     *
     * @param dtoInterface
     *         type of expected response. If server returns some content we try parse it and restore object of the specified type from it.
     *         Specified interface must be annotated with &#064DTO.
     * @param url
     *         URL to send request
     * @param body
     *         body of request. Object must implements DTO interface (interface must be annotated with &#064DTO).
     * @param parameters
     *         additional query parameters.
     * @return instance of {@code dtoInterface} which represents JSON response from the server
     * @throws ServerException
     *         if server returns error response in supported JSON format, see {@link org.eclipse.che.api.core.rest.shared.dto.ServiceError}
     * @throws IOException
     *         if any other error occurs
     * @see org.eclipse.che.dto.shared.DTO
     */
    public static <DTO> DTO post(Class<DTO> dtoInterface, String url, Object body, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, url, HttpMethod.POST, body, parameters);
    }

    public static <DTO> DTO post(Class<DTO> dtoInterface, int timeout, String url, Object body, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, timeout, url, HttpMethod.POST, body, parameters);
    }

    /**
     * Sends PUT request to specified {@code url}.
     *
     * @param dtoInterface
     *         type of expected response. If server returns some content we try parse it and restore object of the specified type from it.
     *         Specified interface must be annotated with &#064DTO.
     * @param url
     *         URL to send request
     * @param body
     *         body of request. Object must implements DTO interface (interface must be annotated with &#064DTO).
     * @param parameters
     *         additional query parameters.
     * @return instance of {@code dtoInterface} which represents JSON response from the server
     * @throws ServerException
     *         if server returns error response in supported JSON format, see {@link org.eclipse.che.api.core.rest.shared.dto.ServiceError}
     * @throws IOException
     *         if any other error occurs
     * @see org.eclipse.che.dto.shared.DTO
     */
    public static <DTO> DTO put(Class<DTO> dtoInterface, String url, Object body, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, url, HttpMethod.PUT, body, parameters);
    }

    public static <DTO> DTO put(Class<DTO> dtoInterface, int timeout, String url, Object body, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, timeout, url, HttpMethod.PUT, body, parameters);
    }

    /**
     * Sends OPTIONS request to specified {@code url}.
     *
     * @param dtoInterface
     *         type of expected response. If server returns some content we try parse it and restore object of the specified type from it.
     *         Specified interface must be annotated with &#064DTO.
     * @param url
     *         URL to send request
     * @param parameters
     *         additional query parameters.
     * @return instance of {@code dtoInterface} which represents JSON response from the server
     * @throws ServerException
     *         if server returns error response in supported JSON format, see {@link org.eclipse.che.api.core.rest.shared.dto.ServiceError}
     * @throws IOException
     *         if any other error occurs
     * @see org.eclipse.che.dto.shared.DTO
     */
    public static <DTO> DTO options(Class<DTO> dtoInterface, String url, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, url, HttpMethod.OPTIONS, null, parameters);
    }

    public static <DTO> DTO options(Class<DTO> dtoInterface, int timeout, String url, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, timeout, url, HttpMethod.OPTIONS, null, parameters);
    }

    /**
     * Sends DELETE request to specified {@code url}.
     *
     * @param dtoInterface
     *         type of expected response. If server returns some content we try parse it and restore object of the specified type from it.
     *         Specified interface must be annotated with &#064DTO.
     * @param url
     *         URL to send request
     * @param parameters
     *         additional query parameters.
     * @return instance of {@code dtoInterface} which represents JSON response from the server
     * @throws ServerException
     *         if server returns error response in supported JSON format, see {@link org.eclipse.che.api.core.rest.shared.dto.ServiceError}
     * @throws IOException
     *         if any other error occurs
     * @see org.eclipse.che.dto.shared.DTO
     */
    public static <DTO> DTO delete(Class<DTO> dtoInterface, String url, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, url, HttpMethod.DELETE, null, parameters);
    }

    public static <DTO> DTO delete(Class<DTO> dtoInterface, int timeout, String url, Pair<String, ?>... parameters)
            throws IOException, ServerException, NotFoundException, ForbiddenException, UnauthorizedException, ConflictException {
        return request(dtoInterface, timeout, url, HttpMethod.DELETE, null, parameters);
    }

    private HttpJsonHelper() {
    }

    /**
     * Execute all request from HttpJsonHelper throw single method  requestString.
     */
    public static class HttpJsonHelperImpl {

        public void request(String url,
                            String method,
                            Object body,
                            Pair<String, ?>... parameters)
                throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
            request(null, -1, url, method, body, parameters);
        }

        public <DTO> DTO request(Class<DTO> dtoInterface,
                                 String url,
                                 String method,
                                 Object body,
                                 Pair<String, ?>... parameters)
                throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
            return request(dtoInterface, -1, url, method, body, parameters);
        }

        public <DTO> DTO request(Class<DTO> dtoInterface,
                                 int timeout,
                                 String url,
                                 String method,
                                 Object body,
                                 Pair<String, ?>... parameters)
                throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
            final String str = requestString(timeout, url, method, body, parameters);
            if (dtoInterface != null) {
                return DtoFactory.getInstance().createDtoFromJson(str, dtoInterface);
            }
            return null;
        }

        public <DTO> List<DTO> requestArray(Class<DTO> dtoInterface,
                                            String url,
                                            String method,
                                            Object body,
                                            Pair<String, ?>... parameters)
                throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
            return requestArray(dtoInterface, -1, url, method, body, parameters);
        }

        public <DTO> List<DTO> requestArray(Class<DTO> dtoInterface,
                                            int timeout,
                                            String url,
                                            String method,
                                            Object body,
                                            Pair<String, ?>... parameters)
                throws IOException, ServerException, UnauthorizedException, ForbiddenException, NotFoundException, ConflictException {
            final String str = requestString(timeout, url, method, body, parameters);
            if (dtoInterface != null) {
                return DtoFactory.getInstance().createListDtoFromJson(str, dtoInterface);
            }
            return null;
        }

        private String getAuthenticationToken() {
            Subject subject = EnvironmentContext.getCurrent().getSubject();
            if (subject != null) {
                return subject.getToken();
            }
            return null;
        }

        public String requestString(String url,
                                    String method,
                                    Object body,
                                    Pair<String, ?>... parameters)
                throws IOException, ServerException, ForbiddenException, NotFoundException, UnauthorizedException, ConflictException {
            return requestString(-1, url, method, body, parameters);
        }

        public String requestString(int timeout,
                                    String url,
                                    String method,
                                    Object body,
                                    Pair<String, ?>... parameters)
                throws IOException, ServerException, ForbiddenException, NotFoundException, UnauthorizedException, ConflictException {
            final String authToken = getAuthenticationToken();
            if ((parameters != null && parameters.length > 0) || authToken != null) {
                final UriBuilder ub = UriBuilder.fromUri(url);
                //remove sensitive information from url.
                ub.replaceQueryParam("token", null);

                if (parameters != null && parameters.length > 0) {
                    for (Pair<String, ?> parameter : parameters) {
                        ub.queryParam(parameter.first, parameter.second);
                    }
                }
                url = ub.build().toString();
            }
            final HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
            conn.setConnectTimeout(timeout > 0 ? timeout : 60000);
            conn.setReadTimeout(timeout > 0 ? timeout : 60000);
            try {
                conn.setRequestMethod(method);
                //drop a hint for server side that we want to receive application/json
                conn.addRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
                if (authToken != null) {
                    conn.setRequestProperty(HttpHeaders.AUTHORIZATION, authToken);
                }
                if (body != null) {
                    conn.addRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                    conn.setDoOutput(true);

                    if (HttpMethod.DELETE.equals(method)) { //to avoid jdk bug described here http://bugs.java.com/view_bug.do?bug_id=7157360
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
                        final ServiceError serviceError = DtoFactory.getInstance().createDtoFromJson(str, ServiceError.class);
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
                    throw new IOException(String.format("Failed access: %s, method: %s, response code: %d, message: %s",
                                                        UriBuilder.fromUri(url).replaceQuery("token").build(), method, responseCode, str));
                }
                final String contentType = conn.getContentType();
                if (!(contentType == null || contentType.startsWith(MediaType.APPLICATION_JSON))) {
                    throw new IOException(conn.getResponseMessage());
                }

                try (Reader reader = new InputStreamReader(conn.getInputStream())) {
                    return CharStreams.toString(reader);
                }
            } finally {
                conn.disconnect();
            }
        }
    }
}
