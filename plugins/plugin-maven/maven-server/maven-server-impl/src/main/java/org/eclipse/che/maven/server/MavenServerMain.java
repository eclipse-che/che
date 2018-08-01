/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
