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

import static org.eclipse.che.infrastructure.docker.client.CLibraryFactory.getCLibrary;

import com.sun.jna.LastErrorException;
import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.che.infrastructure.docker.client.CLibrary;

/** @author andrew00x */
public class UnixSocketOutputStream extends OutputStream {
  private final int fd;
  private final CLibrary cLib = getCLibrary();

  UnixSocketOutputStream(int fd) {
    this.fd = fd;
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[] {(byte) b}, 0, 1);
  }

  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    int n;
    try {
      n = cLib.send(fd, b, len, 0);
    } catch (LastErrorException e) {
      throw new IOException("error: " + cLib.strerror(e.getErrorCode()));
    }
    if (n != len) {
      throw new IOException(String.format("Failed writing %d bytes", len));
    }
  }
}
