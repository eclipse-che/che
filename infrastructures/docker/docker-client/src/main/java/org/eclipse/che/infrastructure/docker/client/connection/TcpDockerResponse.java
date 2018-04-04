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
package org.eclipse.che.infrastructure.docker.client.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/** @author andrew00x */
public class TcpDockerResponse implements DockerResponse {
  private final HttpURLConnection connection;

  TcpDockerResponse(HttpURLConnection connection) {
    this.connection = connection;
  }

  @Override
  public int getStatus() throws IOException {
    return connection.getResponseCode();
  }

  @Override
  public int getContentLength() throws IOException {
    return connection.getContentLength();
  }

  @Override
  public String getContentType() throws IOException {
    return connection.getContentType();
  }

  @Override
  public String getHeader(String name) throws IOException {
    return connection.getHeaderField(name);
  }

  @Override
  public String[] getHeaders(String name) throws IOException {
    final Map<String, List<String>> allHeaders = connection.getHeaderFields();
    final List<String> headers = allHeaders.get(name);
    return headers != null ? headers.toArray(new String[headers.size()]) : new String[0];
  }

  @Override
  public InputStream getInputStream() throws IOException {
    InputStream entityStream = connection.getErrorStream();
    if (entityStream == null) {
      entityStream = connection.getInputStream();
    }
    return entityStream;
  }
}
