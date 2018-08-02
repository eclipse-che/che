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
