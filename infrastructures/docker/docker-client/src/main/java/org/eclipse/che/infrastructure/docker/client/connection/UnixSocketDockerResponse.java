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
package org.eclipse.che.infrastructure.docker.client.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** @author andrew00x */
public class UnixSocketDockerResponse implements DockerResponse {
  private static final InputStream EMPTY =
      new InputStream() {
        @Override
        public int read() throws IOException {
          return -1;
        }
      };

  private final InputStream rawData;

  private InputStream data;
  private String[] headersFields;
  private int status;

  UnixSocketDockerResponse(InputStream input) {
    rawData = input;
    status = -1;
  }

  @Override
  public int getStatus() throws IOException {
    if (status != -1) {
      return status;
    }
    getInputStream();
    final String statusLine = headersFields[0];
    if (statusLine.startsWith("HTTP/1.")) {
      int startCode = statusLine.indexOf(' ');
      if (startCode > 0) {
        int endCode = statusLine.indexOf(' ', startCode + 1);
        if (endCode < 0) {
          endCode = statusLine.length();
        }

        try {
          return status = Integer.parseInt(statusLine.substring(startCode + 1, endCode));
        } catch (NumberFormatException ignored) {
        }
      }
    }
    return -1;
  }

  @Override
  public int getContentLength() throws IOException {
    final String header = getHeader("Content-Length");
    if (header != null) {
      try {
        return Integer.parseInt(header);
      } catch (NumberFormatException ignored) {
      }
    }
    return -1;
  }

  @Override
  public String getContentType() throws IOException {
    return getHeader("Content-Type");
  }

  @Override
  public String getHeader(String name) throws IOException {
    getInputStream();
    final String lowerCaseName = name.toLowerCase();
    for (String field : headersFields) {
      if (field.toLowerCase().startsWith(lowerCaseName)) {
        int colonPos = field.indexOf(':');
        if (colonPos > 0) {
          return field.substring(colonPos + 1).trim();
        }
      }
    }
    return null;
  }

  @Override
  public String[] getHeaders(String name) throws IOException {
    getInputStream();
    final String lowerCaseName = name.toLowerCase();
    final List<String> headers = new ArrayList<>(4);
    for (String field : headersFields) {
      if (field.toLowerCase().startsWith(lowerCaseName)) {
        int colonPos = field.indexOf(':');
        if (colonPos > 0) {
          headers.add(field.substring(colonPos + 1).trim());
        }
      }
    }
    return headers.toArray(new String[headers.size()]);
  }

  @Override
  public synchronized InputStream getInputStream() throws IOException {
    if (this.headersFields != null) {
      // already parsed
      return data;
    }

    final StringBuilder lineBuf = new StringBuilder();
    for (int i = 0; i < 8; i++) {
      int c = rawData.read();
      if (c == -1) {
        break;
      }
      lineBuf.append((char) c);
    }
    if (!lineBuf.toString().startsWith("HTTP/1.")) {
      throw new IOException("Invalid status line of HTTP response from docker API");
    }
    final List<String> headerFields = new ArrayList<>(4);
    for (; ; ) {
      int c = rawData.read();
      if (c == -1) {
        throw new IOException("Unexpected end of file from docker API");
      } else if (c == '\n') {
        if (lineBuf.length() == 0) {
          break;
        }
        headerFields.add(lineBuf.toString());
        lineBuf.setLength(0);
      } else if (c != '\r') {
        lineBuf.append((char) c);
      }
    }
    this.headersFields = headerFields.toArray(new String[headerFields.size()]);
    final int contentLength = getContentLength();
    if (contentLength == 0) {
      return data = EMPTY;
    }
    if (contentLength > 0) {
      return data = new LimitedInputStream(rawData, contentLength);
    }
    return data =
        "chunked".equals(getHeader("Transfer-Encoding"))
            ? new ChunkedInputStream(rawData)
            : rawData;
  }
}
