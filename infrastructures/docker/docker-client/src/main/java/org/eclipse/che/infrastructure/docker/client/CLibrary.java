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
package org.eclipse.che.infrastructure.docker.client;

import com.sun.jna.Library;
import com.sun.jna.Structure;
import com.sun.jna.ptr.LongByReference;
import java.util.Arrays;
import java.util.List;

/** @author andrew00x */
// C language functions
public interface CLibrary extends Library {
  int AF_UNIX = 1; // Defined in 'sys/socket.h'
  int SOCK_STREAM = 1; // Defined in 'sys/socket.h'

  // Defined in 'unix.h', see http://man7.org/linux/man-pages/man7/unix.7.html
  class SockAddrUn extends Structure {
    public static final int UNIX_PATH_MAX = 108;

    public short sun_family;
    public byte[] sun_path;

    public SockAddrUn(String path) {
      byte[] pathBytes = path.getBytes();
      if (pathBytes.length > UNIX_PATH_MAX) {
        throw new IllegalArgumentException(String.format("Path '%s' is too long. ", path));
      }
      sun_family = AF_UNIX;
      sun_path = new byte[pathBytes.length + 1];
      System.arraycopy(pathBytes, 0, sun_path, 0, Math.min(sun_path.length - 1, pathBytes.length));
      allocateMemory();
    }

    @Override
    protected List getFieldOrder() {
      return Arrays.asList("sun_family", "sun_path");
    }
  }

  int socket(int domain, int type, int protocol);

  int connect(int fd, SockAddrUn sock_addr, int addr_len);

  int send(int fd, byte[] buffer, int count, int flags);

  int recv(int fd, byte[] buffer, int count, int flags);

  int close(int fd);

  String strerror(int errno);

  int write(int fd, byte[] buff, int count);

  int read(int fd, byte[] buf, int count);

  int eventfd(int initval, int flag);

  int eventfd_read(int fd, LongByReference val);

  int open(String path, int mode);

  int O_RDONLY = 0x00;
  int O_WRONLY = 0x01;
}
