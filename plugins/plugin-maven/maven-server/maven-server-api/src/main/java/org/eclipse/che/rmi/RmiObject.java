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
package org.eclipse.che.rmi;

import java.rmi.Remote;
import java.rmi.server.Unreferenced;

/**
 * Base class for all rmi objects
 *
 * @author Evgen Vidolob
 */
public class RmiObject implements Remote, Unreferenced {

  /**
   * Wraps all non "java.*" exceptions into {@link RuntimeException}
   *
   * @param t exception which need wrap
   * @return wrapped exception
   */
  public Throwable wrapException(Throwable t) {
    boolean isJavaException = false;
    Throwable ex = t;
    while (ex != null) {
      if (!ex.getClass().getName().startsWith("java.") && !isWellKnownException(ex)) {
        isJavaException = true;
      }
      ex = ex.getCause();
    }

    if (isJavaException) {
      RuntimeException runtimeException = new RuntimeException(t.getMessage());
      runtimeException.setStackTrace(t.getStackTrace());
      runtimeException.initCause(wrapException(t.getCause()));
      ex = runtimeException;
    }
    return ex;
  }

  protected boolean isWellKnownException(Throwable t) {
    return false;
  }

  @Override
  public void unreferenced() {}
}
