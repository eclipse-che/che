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
import java.util.List;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenArtifactKey;
import org.eclipse.che.maven.data.MavenRemoteRepository;
import org.eclipse.che.maven.data.MavenWorkspaceCache;

/** @author Evgen Vidolob */
public interface MavenServer extends Remote {
  void setComponents(
      MavenWorkspaceCache cache,
      boolean failOnUnresolvedDependency,
      MavenTerminal mavenTerminal,
      MavenServerProgressNotifier notifier,
      boolean alwaysUpdateSnapshot)
      throws RemoteException;

  String getEffectivePom(File pom, List<String> activeProfiles, List<String> inactiveProfiles)
      throws RemoteException;

  MavenServerResult resolveProject(
      File pom, List<String> activeProfiles, List<String> inactiveProfiles) throws RemoteException;

  MavenArtifact resolveArtifact(
      MavenArtifactKey artifactKey, List<MavenRemoteRepository> remoteRepositories)
      throws RemoteException;

  void reset() throws RemoteException;

  void dispose() throws RemoteException;

  File getLocalRepository() throws RemoteException;
}
