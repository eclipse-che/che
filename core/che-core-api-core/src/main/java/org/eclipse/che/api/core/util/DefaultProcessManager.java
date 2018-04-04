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
package org.eclipse.che.api.core.util;

/**
 * Typically may be in use for windows systems only. For *nix like system UnixProcessManager is in
 * use.
 *
 * @author andrew00x
 */
class DefaultProcessManager extends ProcessManager {
  /*
  NOTE: some methods are not implemented for other system than unix like system.
   */

  @Override
  public void kill(Process process) {
    if (isAlive(process)) {
      process.destroy();
      try {
        process.waitFor(); // wait for process death
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    }
  }

  @Override
  public boolean isAlive(Process process) {
    try {
      process.exitValue();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    }
  }

  @Override
  int system(String command) {
    throw new UnsupportedOperationException();
  }
}
