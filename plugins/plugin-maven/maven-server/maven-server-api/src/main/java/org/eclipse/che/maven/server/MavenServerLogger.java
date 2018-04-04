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
package org.eclipse.che.maven.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Logger interface which server will use for logging
 *
 * @author Evgen Vidolob
 */
public interface MavenServerLogger extends Remote {

  void info(Throwable t) throws RemoteException;

  void warning(Throwable t) throws RemoteException;

  void error(Throwable t) throws RemoteException;
}
