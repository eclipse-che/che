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

/** @author andrew00x */
public class ChunkedInputStream extends InputStream {
  private final InputStream input;
  private StringBuilder chunkSizeBuf;
  private int chunkSize;
  private int chunkPos;
  private boolean eof;

  ChunkedInputStream(InputStream input) {
    this.input = input;
    chunkSizeBuf = new StringBuilder();
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

  @Override
  public synchronized int available() {
    return (chunkSize - chunkPos);
  }

  private int doRead(byte[] b, int off, int len) throws IOException {
    if (eof) {
      return -1;
    }
    if (chunkSize == 0) {
      chunkPos = 0;
      for (; ; ) {
        int i = input.read();
        if (i < 0) {
          throw new IOException("Can't read size of chunk");
        }
        if (i == '\n') {
          break;
        }
        chunkSizeBuf.append((char) i);
      }

      int l = chunkSizeBuf.length();
      int endSize = 0;
      while (endSize < l && Character.digit(chunkSizeBuf.charAt(endSize), 16) != -1) {
        endSize++;
      }
      try {
        chunkSize = Integer.parseInt(chunkSizeBuf.substring(0, endSize), 16);
      } catch (NumberFormatException e) {
        throw new IOException("Invalid chunk size");
      }
      chunkSizeBuf.setLength(0);
      if (chunkSize == 0) {
        eof = true;
      }
    }
    final int n = input.read(b, 0, Math.min(len - off, chunkSize - chunkPos));
    chunkPos += n;
    if (chunkPos == chunkSize) {
      if ('\r' != input.read()) { // skip '\r'
        throw new IOException("CR character is missing");
      }
      if ('\n' != input.read()) { // skip '\n'
        throw new IOException("LF character is missing");
      }
      chunkSize = 0;
      chunkPos = 0;
    }
    if (eof) {
      return -1;
    }
    return n;
  }
}
