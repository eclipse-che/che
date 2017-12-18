/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.installer.server.impl.LocalInstallerRegistry;
import org.eclipse.che.api.installer.server.impl.RemoteInstallerRegistry;

/**
 * Provides corresponding instance of {@link InstallerRegistry}.
 *
 * <p>Instance of {@link RemoteInstallerRegistry} will be provided if it is configured otherwise
 * instance of {@link LocalInstallerRegistry} will be provided.
 *
 * @author gazarenkov
 * @author Sergii Leshchenko
 */
@Singleton
public class InstallerRegistryProvider implements Provider<InstallerRegistry> {
  private final LocalInstallerRegistry localInstallerRegistry;
  private final RemoteInstallerRegistry remoteInstallerRegistry;

  @Inject
  public InstallerRegistryProvider(
      LocalInstallerRegistry localInstallerRegistry,
      RemoteInstallerRegistry remoteInstallerRegistry) {
    this.localInstallerRegistry = localInstallerRegistry;
    this.remoteInstallerRegistry = remoteInstallerRegistry;
  }

  @Override
  public InstallerRegistry get() {
    return remoteInstallerRegistry.isConfigured()
        ? remoteInstallerRegistry
        : localInstallerRegistry;
  }
}
