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
package org.eclipse.che.infrastructure.docker.client;

import com.sun.jna.Native;
import org.eclipse.che.api.core.util.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author andrew00x */
public class CLibraryFactory {
  private static final Logger LOG = LoggerFactory.getLogger(CLibraryFactory.class);

  private static final CLibrary C_LIBRARY;

  static {
    CLibrary tmp = null;
    if (SystemInfo.isLinux()) {
      try {
        tmp = ((CLibrary) Native.loadLibrary("c", CLibrary.class));
      } catch (Exception e) {
        LOG.error("Cannot load native library", e);
      }
    }
    C_LIBRARY = tmp;
  }

  public static CLibrary getCLibrary() {
    checkCLibrary();
    return C_LIBRARY;
  }

  private static void checkCLibrary() {
    if (C_LIBRARY == null) {
      throw new IllegalStateException("Can't load native library. Not linux system?");
    }
  }

  private CLibraryFactory() {}
}
