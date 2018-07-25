/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.infrastructure.docker.client.connection;

import java.io.IOException;
import java.io.InputStream;

/** @author andrew00x */
public interface DockerResponse {
  int getStatus() throws IOException;

  int getContentLength() throws IOException;

  String getContentType() throws IOException;

  String getHeader(String name) throws IOException;

  String[] getHeaders(String name) throws IOException;

  InputStream getInputStream() throws IOException;
}
