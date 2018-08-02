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

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import org.eclipse.che.maven.data.MavenExplicitProfiles;
import org.eclipse.che.maven.data.MavenModel;

/**
 * Implementation of {@link MavenRemoteServer} which use Maven 3.2.*
 *
 * @author Evgen Vidolob
 */
public class MavenRemoteServerImpl extends MavenRmiObject implements MavenRemoteServer {
  @Override
  public void configure(MavenServerLogger logger, MavenServerDownloadListener downloadListener)
      throws RemoteException {
    MavenServerContext.setLoggerAndListener(logger, downloadListener);
  }

  @Override
  public MavenServer createServer(MavenSettings settings) throws RemoteException {
    try {
      MavenServerImpl mavenServer = new MavenServerImpl(settings);
      UnicastRemoteObject.exportObject(mavenServer, 0);
      return mavenServer;
    } catch (RemoteException e) {
      e.printStackTrace();
      throw getRuntimeException(e);
    }
  }

  @Override
  public MavenModel interpolateModel(MavenModel model, File projectDir) throws RemoteException {
    try {
      return MavenServerImpl.interpolateModel(model, projectDir);
    } catch (Exception e) {
      e.printStackTrace();
      throw getRuntimeException(e);
    }
  }

  @Override
  public ProfileApplicationResult applyProfiles(
      MavenModel model,
      File projectDir,
      MavenExplicitProfiles explicitProfiles,
      Collection<String> alwaysOnProfiles)
      throws RemoteException {
    try {
      return MavenServerImpl.applyProfiles(model, projectDir, explicitProfiles, alwaysOnProfiles);
    } catch (Exception e) {
      e.printStackTrace();
      throw getRuntimeException(e);
    }
  }

  @Override
  public void unreferenced() {
    System.exit(0);
  }
}
