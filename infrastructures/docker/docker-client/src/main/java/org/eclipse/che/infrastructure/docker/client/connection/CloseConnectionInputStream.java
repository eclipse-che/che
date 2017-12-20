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

/**
 * Keeps docker connection open until stream operation is finished.
 *
 * @author Anton Korneta
 */
public class CloseConnectionInputStream extends InputStream {
  private final InputStream is;
  private final DockerConnection connection;

  public CloseConnectionInputStream(InputStream is, DockerConnection connection)
      throws IOException {
    if (is == null) {
      if (connection != null) {
        connection.close();
      }
      throw new IOException("InputStream required");
    }
    if (connection == null) {
      is.close();
      throw new IOException("DockerConnection required");
    }

    this.is = is;
    this.connection = connection;
  }

  @Override
  public int read() throws IOException {
    return is.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return is.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return is.read(b, off, len);
  }

  @Override
  public void close() throws IOException {
    try {
      is.close();
    } finally {
      connection.close();
    }
  }
}
