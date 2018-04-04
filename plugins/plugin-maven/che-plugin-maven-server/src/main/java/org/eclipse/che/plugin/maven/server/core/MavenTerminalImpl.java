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
package org.eclipse.che.plugin.maven.server.core;

import com.google.inject.Singleton;
import java.rmi.RemoteException;
import org.eclipse.che.maven.server.MavenTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link MavenTerminal}, uses default logger to log messages
 *
 * @author Evgen Vidolob
 */
@Singleton
public class MavenTerminalImpl implements MavenTerminal {

  private static final Logger LOG = LoggerFactory.getLogger(MavenTerminalImpl.class);

  @Override
  public void print(int level, String message, Throwable throwable) throws RemoteException {
    switch (level) {
      case LEVEL_DEBUG:
        LOG.debug(message, throwable);
        break;
      case LEVEL_ERROR:
      case LEVEL_FATAL:
        LOG.error(message, throwable);
        break;
      case LEVEL_INFO:
        LOG.info(message, throwable);
        break;
      case LEVEL_WARN:
        LOG.warn(message, throwable);
        break;
    }
  }
}
