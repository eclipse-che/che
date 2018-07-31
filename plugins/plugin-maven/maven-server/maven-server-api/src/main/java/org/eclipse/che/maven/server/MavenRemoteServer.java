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

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import org.eclipse.che.maven.data.MavenExplicitProfiles;
import org.eclipse.che.maven.data.MavenModel;

/**
 * Main interface for maven server. Before using this interface you must configure maven server via
 * {@link #configure(MavenServerLogger, MavenServerDownloadListener)};
 *
 * @author Evgen Vidolob
 */
public interface MavenRemoteServer extends Remote {

  void configure(MavenServerLogger logger, MavenServerDownloadListener downloadListener)
      throws RemoteException;

  MavenServer createServer(MavenSettings settings) throws RemoteException;

  MavenModel interpolateModel(MavenModel model, File projectDir) throws RemoteException;

  ProfileApplicationResult applyProfiles(
      MavenModel model,
      File projectDir,
      MavenExplicitProfiles explicitProfiles,
      Collection<String> alwaysOnProfiles)
      throws RemoteException;
}
