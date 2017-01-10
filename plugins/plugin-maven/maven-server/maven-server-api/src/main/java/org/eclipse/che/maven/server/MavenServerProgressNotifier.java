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
 * Notification interface, mostly used for notification of maven artifact downloading process
 *
 * @author Evgen Vidolob
 */
public interface MavenServerProgressNotifier extends Remote {

    void setText(String text) throws RemoteException;

    void setPercent(double percent) throws RemoteException;

    void setPercentUndefined(boolean undefined) throws RemoteException;

    boolean isCanceled() throws RemoteException;
}
