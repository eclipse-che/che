/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.maven.server;

import org.eclipse.che.maven.data.MavenModel;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Main interface for maven server.
 * Before using this interface you must configure maven server via {@link #configure(MavenServerLogger, MavenServerDownloadListener)};
 *
 * @author Evgen Vidolob
 */
public interface MavenRemoteServer extends Remote {

    void configure(MavenServerLogger logger, MavenServerDownloadListener downloadListener) throws RemoteException;

    MavenServer createServer(MavenSettings settings) throws RemoteException;

    MavenModel interpolateModel(MavenModel model, File projectDir) throws RemoteException;
}
