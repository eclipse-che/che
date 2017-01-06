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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for outputting maven messages.
 * Some implementation may use WebSocket connection to send print message to browser.
 *
 * @author Evgen Vidolob
 */
public interface MavenTerminal extends Remote {
    //copied from org.codehaus.plexus.logging.Logger
    int LEVEL_DEBUG    = 0;
    int LEVEL_INFO     = 1;
    int LEVEL_WARN     = 2;
    int LEVEL_ERROR    = 3;
    int LEVEL_FATAL    = 4;
    int LEVEL_DISABLED = 5;

    void print(int level, String message, Throwable throwable) throws RemoteException;
}
