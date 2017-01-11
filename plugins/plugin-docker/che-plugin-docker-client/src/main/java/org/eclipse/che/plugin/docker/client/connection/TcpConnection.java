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
package org.eclipse.che.plugin.docker.client.connection;

import com.google.common.base.Strings;

import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.plugin.docker.client.DockerCertificates;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public class TcpConnection extends DockerConnection {
    private final URI                baseUri;
    private final DockerCertificates certificates;
    private final int                connectionTimeout;
    private final int                readTimeout;

    private HttpURLConnection connection;

    public TcpConnection(URI baseUri, DockerCertificates certificates, int connectionTimeoutMs, int readTimeoutMs) {
        if ("https".equals(baseUri.getScheme())) {
            if (certificates == null) {
                throw new IllegalArgumentException("Certificates are required for https connection.");
            }
        } else if (!("http".equals(baseUri.getScheme()))) {
            throw new IllegalArgumentException(String.format("Invalid URL '%s', only http and https protocols are supported.", baseUri));
        }
        this.baseUri = baseUri;
        this.certificates = certificates;
        this.connectionTimeout = connectionTimeoutMs;
        this.readTimeout = readTimeoutMs;
    }

    @Override
    protected DockerResponse request(String method, String path, String query, List<Pair<String, ?>> headers, Entity<?> entity)
            throws IOException {
        final String requestUri = path + (Strings.isNullOrEmpty(query) ? "" : "?" + query);
        final URL url = baseUri.resolve(requestUri).toURL();
        final String protocol = url.getProtocol();
        connection = (HttpURLConnection)url.openConnection();
        connection.setConnectTimeout(connectionTimeout);
        connection.setReadTimeout(readTimeout);
        if ("https".equals(protocol)) {
            ((HttpsURLConnection)connection).setSSLSocketFactory(certificates.getSslContext().getSocketFactory());
        }
        connection.setRequestMethod(method);
        // needed to fix bug https://github.com/docker/docker/issues/12845
        connection.setRequestProperty("Connection", "close");
        for (Pair<String, ?> header : headers) {
            connection.setRequestProperty(header.first, String.valueOf(header.second));
        }
        String host = url.getHost();
        if (url.getPort() != -1) {
            host += ":" + Integer.toString(url.getPort());
        }
        // Host header is mandatory in HTTP 1.1
        connection.setRequestProperty("Host", host);
        if (entity != null) {
            connection.setDoOutput(true);
            try (OutputStream output = connection.getOutputStream()) {
                entity.writeTo(output);
            }
        }
        return new TcpDockerResponse(connection);
    }

    @Override
    public void close() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}
