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
public class LimitedInputStream extends InputStream {
  private final InputStream input;
  private final int limit;

  private int pos;

  LimitedInputStream(InputStream input, int limit) {
    this.input = input;
    this.limit = limit;
  }

  @Override
  public synchronized int read() throws IOException {
    final byte[] b = new byte[1];
    if (doRead(b, 0, 1) == -1) {
      return -1;
    }
    return b[0];
  }

  @Override
  public synchronized int read(byte[] b) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    return doRead(b, 0, b.length);
  }

  @Override
  public synchronized int read(byte[] b, int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || len > b.length - off) {
      throw new IndexOutOfBoundsException();
    }
    if (len == 0) {
      return 0;
    }

    return doRead(b, 0, len);
  }

  private int doRead(byte[] b, int off, int len) throws IOException {
    if (pos >= limit) {
      return -1;
    }
    int n = input.read(b, 0, Math.min(len - off, limit - pos));
    pos += n;
    return n;
  }
}
