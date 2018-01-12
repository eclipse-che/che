/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server;

import java.io.File;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenArtifactKey;
import org.eclipse.che.maven.data.MavenRemoteRepository;
import org.eclipse.che.maven.data.MavenWorkspaceCache;
import org.eclipse.che.maven.server.MavenServer;
import org.eclipse.che.maven.server.MavenServerProgressNotifier;
import org.eclipse.che.maven.server.MavenServerResult;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.che.plugin.maven.server.core.MavenProgressNotifier;
import org.eclipse.che.plugin.maven.server.rmi.RmiObjectWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
public abstract class MavenServerWrapper extends RmiObjectWrapper<MavenServer> {
  private static final Logger LOG = LoggerFactory.getLogger(MavenServerWrapper.class);
  private MavenCustomization customization;

  @Override
  protected void wrappedCreated() throws RemoteException {
    super.wrappedCreated();
    if (customization != null) {
      customizeMaven();
    }
  }

  private void customizeMaven() throws RemoteException {
    getOrCreateWrappedObject()
        .setComponents(
            customization.cache,
            customization.failOnUnresolvedDependency,
            customization.mavenTerminal,
            customization.notifier,
            customization.alwaysUpdateSnapshot);
  }

  public void customize(
      MavenWorkspaceCache cache,
      MavenTerminal mavenTerminal,
      MavenProgressNotifier notifier,
      boolean failOnUnresolvedDependency,
      boolean alwaysUpdateSnapshot) {
    if (customization != null) {
      uncustomize();
    }
    MavenTerminal mavenTerminalWrapper;
    try {
      mavenTerminalWrapper = new MavenTerminalWrapper(mavenTerminal);
      UnicastRemoteObject.exportObject(mavenTerminalWrapper, 0);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }

    MavenServerProgressNotifier wrapper;
    try {
      wrapper = new MavenServerProgressNotifierWrapper(notifier);
      UnicastRemoteObject.exportObject(wrapper, 0);
    } catch (RemoteException e) {
      throw new RuntimeException(e);
    }

    customization =
        new MavenCustomization(
            cache, mavenTerminalWrapper, wrapper, failOnUnresolvedDependency, alwaysUpdateSnapshot);
    perform(this::customizeMaven);
  }

  private void uncustomize() {
    if (customization == null) {
      return;
    }
    try {
      UnicastRemoteObject.unexportObject(customization.mavenTerminal, true);
    } catch (NoSuchObjectException e) {
      LOG.warn("Can't unexport object", e);
    }

    try {
      UnicastRemoteObject.unexportObject(customization.notifier, true);
    } catch (NoSuchObjectException e) {
      LOG.warn("Can't unexport object", e);
    }
    customization = null;
  }

  public MavenServerResult resolveProject(
      File pom, List<String> activeProfiles, List<String> inactiveProfile) {
    return perform(
        () -> getOrCreateWrappedObject().resolveProject(pom, activeProfiles, inactiveProfile));
  }

  public String getEffectivePom(
      File pom, List<String> activeProfiles, List<String> inactiveProfile) {
    return perform(
        () -> getOrCreateWrappedObject().getEffectivePom(pom, activeProfiles, inactiveProfile));
  }

  public MavenArtifact resolveArtifact(
      MavenArtifactKey artifactKey, List<MavenRemoteRepository> repositories) {
    return perform(() -> getOrCreateWrappedObject().resolveArtifact(artifactKey, repositories));
  }

  public File getLocalRepository() {
    return perform(() -> getOrCreateWrappedObject().getLocalRepository());
  }

  private <T> T perform(RunnableRemoteWithResult<T> runnable) {
    RemoteException exception = null;
    for (int i = 0; i < 2; i++) {
      try {
        return runnable.perform();
      } catch (RemoteException e) {
        exception = e;
        onError();
      }
    }
    throw new RuntimeException(exception);
  }

  private void perform(RunnableRemote runnable) {
    RemoteException exception = null;
    for (int i = 0; i < 2; i++) {
      try {
        runnable.perform();
        return;
      } catch (RemoteException e) {
        exception = e;
        onError();
      }
    }
    throw new RuntimeException(exception);
  }

  public void dispose() {
    MavenServer wrapped = getWrapped();
    if (wrapped != null) {
      try {
        wrapped.dispose();
      } catch (RemoteException e) {
        LOG.debug(e.getMessage(), e);
        onError();
      }
    }
    uncustomize();
  }

  public void reset() {
    MavenServer wrapped = getWrapped();
    if (wrapped != null) {
      try {
        wrapped.reset();
      } catch (RemoteException e) {
        LOG.debug(e.getMessage(), e);
        onError();
      }
    }

    uncustomize();
  }

  private interface RunnableRemote {
    void perform() throws RemoteException;
  }

  private interface RunnableRemoteWithResult<T> {
    T perform() throws RemoteException;
  }

  private static class MavenCustomization {
    private final MavenWorkspaceCache cache;
    private final MavenTerminal mavenTerminal;
    private final MavenServerProgressNotifier notifier;
    private final boolean failOnUnresolvedDependency;
    private final boolean alwaysUpdateSnapshot;

    public MavenCustomization(
        MavenWorkspaceCache cache,
        MavenTerminal mavenTerminal,
        MavenServerProgressNotifier notifier,
        boolean failOnUnresolvedDependency,
        boolean alwaysUpdateSnapshot) {
      this.cache = cache;
      this.mavenTerminal = mavenTerminal;
      this.notifier = notifier;
      this.failOnUnresolvedDependency = failOnUnresolvedDependency;
      this.alwaysUpdateSnapshot = alwaysUpdateSnapshot;
    }
  }

  // TODO enable notification after MavenServerProgressNotifier reworked
  private static class MavenServerProgressNotifierWrapper implements MavenServerProgressNotifier {

    private MavenProgressNotifier delegate;

    public MavenServerProgressNotifierWrapper(MavenProgressNotifier delegate) {
      //            this.delegate = delegate;
    }

    @Override
    public void setText(String text) throws RemoteException {
      //            delegate.setText(text);
    }

    @Override
    public void setPercent(double percent) throws RemoteException {
      //            delegate.setPercent(percent);
    }

    @Override
    public void setPercentUndefined(boolean undefined) throws RemoteException {
      //            delegate.setPercentUndefined(undefined);
    }

    @Override
    public boolean isCanceled() throws RemoteException {
      //            return delegate.isCanceled();
      return false;
    }
  }

  private static class MavenTerminalWrapper implements MavenTerminal {

    private MavenTerminal delegate;

    public MavenTerminalWrapper(MavenTerminal delegate) {
      this.delegate = delegate;
    }

    @Override
    public void print(int level, String message, Throwable throwable) throws RemoteException {
      delegate.print(level, message, throwable);
    }
  }
}
