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
package org.eclipse.che.api.installer.server;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.installer.server.impl.LocalInstallerRegistry;

/**
 * Provides corresponding instance of {@link InstallerRegistry}.
 *
 * @author gazarenkov
 * @author Sergii Leshchenko
 */
@Singleton
public class InstallerRegistryProvider implements Provider<InstallerRegistry> {
  private final LocalInstallerRegistry localInstallerRegistry;

  @Inject
  public InstallerRegistryProvider(LocalInstallerRegistry localInstallerRegistry) {
    this.localInstallerRegistry = localInstallerRegistry;
  }

  @Override
  public InstallerRegistry get() {
    return localInstallerRegistry;
  }
}
