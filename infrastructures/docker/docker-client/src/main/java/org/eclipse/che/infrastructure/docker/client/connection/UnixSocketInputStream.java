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

import static org.eclipse.che.infrastructure.docker.client.CLibraryFactory.getCLibrary;

import com.sun.jna.LastErrorException;
import java.io.IOException;
import java.io.InputStream;
import org.eclipse.che.infrastructure.docker.client.CLibrary;

/** @author andrew00x */
public class UnixSocketInputStream extends InputStream {
  private final int fd;
  private final CLibrary cLib = getCLibrary();

  UnixSocketInputStream(int fd) {
    this.fd = fd;
  }

  @Override
  public int read() throws IOException {
    final byte[] bytes = new byte[1];
    if (read(bytes) == 0) {
      return -1;
    }
    return bytes[0];
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    }
    if (len == 0) {
      return 0;
    }
    int n;
    try {
      n = cLib.recv(fd, b, len, 0);
    } catch (LastErrorException e) {
      throw new IOException("error: " + cLib.strerror(e.getErrorCode()));
    }
    if (n == 0) {
      return -1;
    }
    return n;
  }
}
