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

import java.rmi.RemoteException;

/**
 * This implementation of MavenProgressNotifier delegates all logic to remote MavenProgressNotifier
 * implementation
 *
 * @author Evgen Vidolob
 */
public class MavenServerProgressNotifierImpl implements MavenServerProgressNotifier {

  private final MavenServerProgressNotifier delegate;

  private boolean canceled;

  public MavenServerProgressNotifierImpl(MavenServerProgressNotifier delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setText(String text) throws RemoteException {
    delegate.setText(text);
  }

  @Override
  public void setPercent(double percent) throws RemoteException {
    delegate.setPercent(percent);
  }

  @Override
  public void setPercentUndefined(boolean undefined) throws RemoteException {
    delegate.setPercentUndefined(undefined);
  }

  @Override
  public boolean isCanceled() throws RemoteException {
    if (canceled) {
      return true;
    }

    if (delegate.isCanceled()) {
      canceled = true;
      return canceled;
    }

    return false;
  }
}
