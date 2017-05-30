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
package org.eclipse.che.rmi;

import javax.naming.Context;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

/**
 * @author Evgen Vidolob
 */
public class RmiServer {
    private static Remote remote;

    protected static void start(Remote remote) throws RemoteException {
        System.setProperty("java.rmi.server.hostname", "localhost");
        System.setProperty("java.rmi.server.disableHttp", "true");
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.eclipse.che.rmi.JNDI");
        if (RmiServer.remote != null) {
            throw new AssertionError("This server is already started!");
        }

        RmiServer.remote = remote;

        int port = -1;
        Random random = new Random();
        Registry registry = null;
        while (port == -1) {
            int tmpPort = random.nextInt(65535);
            if (tmpPort < 4000) {
                continue;
            }
            try {
                registry = LocateRegistry.createRegistry(tmpPort);
                port = tmpPort;
            } catch (ExportException ignored) {
            }

        }

        Remote exportObject = UnicastRemoteObject.exportObject(remote, port);
        String exportName = remote.getClass().getSimpleName() + Integer.toHexString(exportObject.hashCode());
        try {
            registry.bind(exportName, exportObject);
            String portName = port + "/" + exportName;
            System.out.println("Port/Name:" + portName);
            int twoMinutes = 2 * 60 * 1000;
            Object lock = new Object();
            while(true){
                synchronized (lock){
                    lock.wait(twoMinutes);
                }
                // TODO add ping
            }
        } catch (AlreadyBoundException | InterruptedException e) {
            e.printStackTrace();
            System.exit(42);
        }


    }

}
