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

import java.rmi.RemoteException;
import org.eclipse.che.rmi.RmiServer;

/** @author Evgen Vidolob */
public class MavenServerMain extends RmiServer {
  public static void main(String[] args) throws RemoteException {
    start(new MavenRemoteServerImpl());
  }
}
