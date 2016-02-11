/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.machine.server.proxy;

import com.google.common.io.ByteStreams;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.machine.Server;
import org.eclipse.che.api.machine.server.MachineManager;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.servlet.http.HttpServletResponse.SC_BAD_GATEWAY;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

/**
 * Routes requests to extension API hosted in machine
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MachineExtensionProxyServlet extends HttpServlet {
    private static final Logger  LOG               = LoggerFactory.getLogger(MachineExtensionProxyServlet.class);
    private static final Pattern EXTENSION_API_URI = Pattern.compile(".*?/ext/[^/]+/(?<workspaceId>[^/]+)/?.*");

    private final int            extServicesPort;
    private final MachineManager machineManager;

    @Inject
    public MachineExtensionProxyServlet(@Named("machine.extension.api_port") int extServicesPort, MachineManager machineManager) {
        this.extServicesPort = extServicesPort;
        this.machineManager = machineManager;
    }

    // fixme secure request to another's machine

    // todo handle https to http

    // todo remove headers if it's name is in connection headers

    // fixme proxy should ensure that http 1.1 request contains hosts header

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HttpURLConnection conn = prepareProxyConnection(req);

            try {
                conn.connect();

                setResponse(resp, conn);
            } catch (IOException e) {
                resp.sendError(SC_BAD_GATEWAY, "Request can't be forwarded to machine. " + e.getLocalizedMessage());
            } finally {
                conn.disconnect();
            }

        } catch (NotFoundException e) {
            resp.sendError(SC_SERVICE_UNAVAILABLE, "Request can't be forwarded to machine. " + e.getLocalizedMessage());
        } catch (ServerException e) {
            resp.sendError(SC_BAD_GATEWAY, "Request can't be forwarded to machine. " + e.getLocalizedMessage());
        }
    }

    private HttpURLConnection prepareProxyConnection(HttpServletRequest req) throws NotFoundException, ServerException {
        String extensionApiUrl = getExtensionApiUrl(req);
        try {
            final HttpURLConnection conn = (HttpURLConnection)new URL(extensionApiUrl).openConnection();

            conn.setRequestMethod(req.getMethod());

            setHeaders(conn, req);

            if ("POST".equals(req.getMethod()) || "PUT".equals(req.getMethod()) || "DELETE".equals(req.getMethod())) {
                if (req.getInputStream() != null) {
                    conn.setDoOutput(true);

                    try (InputStream is = req.getInputStream();
                         OutputStream os = conn.getOutputStream()) {
                        ByteStreams.copy(is, os);
                    }
                }
            }

            return conn;
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    private String getExtensionApiUrl(HttpServletRequest req) throws NotFoundException, ServerException {
        String workspaceId;
        final Matcher matcher = EXTENSION_API_URI.matcher(req.getRequestURI());
        if (matcher.matches()) {
            workspaceId = matcher.group("workspaceId");
        } else {
            throw new NotFoundException("No workspace id is found in request.");
        }

        final Instance machine = machineManager.getDevMachine(workspaceId);
        final Server server = machine.getMetadata().getServers().get(Integer.toString(extServicesPort));
        if (server == null) {
            throw new ServerException("No extension server found in machine.");
        }

        final UriBuilder uriBuilder = UriBuilder.fromUri(server.getUrl())
                                                .replacePath(req.getRequestURI())
                                                .replaceQuery(req.getQueryString());

        return uriBuilder.build().toString();
    }

    private void setResponse(HttpServletResponse resp, HttpURLConnection conn) throws ServerException {
        try {
            final int responseCode = conn.getResponseCode();

            resp.setStatus(responseCode);

            InputStream responseStream;
            if (responseCode / 100 == 2 && responseCode != 204) {
                responseStream = conn.getInputStream();
            } else {
                responseStream = conn.getErrorStream();
            }

            // copy headers from proxy response to origin response
            for (Map.Entry<String, List<String>> header : conn.getHeaderFields().entrySet()) {
                if (!skipHeader(header.getKey())) {
                    for (String headerValue : header.getValue()) {
                        resp.addHeader(header.getKey(), headerValue);
                    }
                }
            }

            if (responseStream != null) {
                // copy content of input or error stream from destination response to output stream of origin response
                try (OutputStream os = resp.getOutputStream();
                     InputStream is = responseStream) {
                    ByteStreams.copy(is, os);
                    os.flush();
                }
            }
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    private void setHeaders(HttpURLConnection conn, HttpServletRequest request) {
        // copy headers from request
        final Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headerName = headerNames.nextElement();

            if (!skipHeader(headerName)) {
                final Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    conn.setRequestProperty(headerName, headerValues.nextElement());
                }
            }
        }

        // add forwarded host header. See http://tools.ietf.org/html/rfc7239#section-5.3
        conn.setRequestProperty("X-Forwarded-Host", request.getHeader("Host"));
    }

    /**
     * Checks if the header should not be copied by proxy.<br>
     * <a href="http://tools.ietf.org/html/rfc2616#section-13.5.1">RFC-2616 Section 13.5.1</a>
     *
     * @param headerName
     *         the header name to check.
     * @return {@code true} if the header should be skipped, false otherwise.
     */
    public static boolean skipHeader(final String headerName) {
        return headerName == null ||
               headerName.equalsIgnoreCase("Connection") ||
               headerName.equalsIgnoreCase("Keep-Alive") ||
               headerName.equalsIgnoreCase("Proxy-Authentication") ||
               headerName.equalsIgnoreCase("Proxy-Authorization") ||
               headerName.equalsIgnoreCase("TE") ||
               headerName.equalsIgnoreCase("Trailers") ||
               headerName.equalsIgnoreCase("Transfer-Encoding") ||
               headerName.equalsIgnoreCase("Upgrade");
    }
}
