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
package org.eclipse.che.maven.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for outputting maven messages. Some implementation may use WebSocket connection to send
 * print message to browser.
 *
 * @author Evgen Vidolob
 */
public interface MavenTerminal extends Remote {
  // copied from org.codehaus.plexus.logging.Logger
  int LEVEL_DEBUG = 0;
  int LEVEL_INFO = 1;
  int LEVEL_WARN = 2;
  int LEVEL_ERROR = 3;
  int LEVEL_FATAL = 4;
  int LEVEL_DISABLED = 5;

  void print(int level, String message, Throwable throwable) throws RemoteException;
}
