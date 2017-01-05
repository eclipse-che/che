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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementation of {@link MavenRemoteServer} which use Maven 3.2.*
 *
 * @author Evgen Vidolob
 */
public class MavenRemoteServerImpl extends MavenRmiObject implements MavenRemoteServer {
    @Override
    public void configure(MavenServerLogger logger, MavenServerDownloadListener downloadListener) throws RemoteException {
         MavenServerContext.setLoggerAndListener(logger, downloadListener);
    }

    @Override
    public MavenServer createServer(MavenSettings settings) throws RemoteException {
        try {
            MavenServerImpl mavenServer = new MavenServerImpl(settings);
            UnicastRemoteObject.exportObject(mavenServer, 0);
            return mavenServer;
        } catch (RemoteException e){
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
    public void unreferenced() {
        System.exit(0);
    }
}
