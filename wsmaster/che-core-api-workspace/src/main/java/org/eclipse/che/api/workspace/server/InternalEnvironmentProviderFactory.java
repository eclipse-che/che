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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.workspace.server.wsnext.SidecarBasedInternalEnvironmentProvider;

/** @author Alexander Garagatyi */
public class InternalEnvironmentProviderFactory {

  private final Toggles toggles;
  private final InternalEnvironmentProvider classicProvider;
  private final SidecarBasedInternalEnvironmentProvider sidecarBasedProvider;

  public InternalEnvironmentProviderFactory(
      Toggles toggles,
      InternalEnvironmentProvider classicProvider,
      SidecarBasedInternalEnvironmentProvider sidecarBasedProvider) {
    this.toggles = toggles;
    this.classicProvider = classicProvider;
    this.sidecarBasedProvider = sidecarBasedProvider;
  }

  public InternalEnvironmentProvider create() {
    if (toggles.isEnabled("workspace-next")) {
      // Add sidecar based tooling
      return sidecarBasedProvider;
    } else {
      return classicProvider;
    }
  }

  private static class Toggles {

    public boolean isEnabled(String toggleName) {
      return true;
    }
  }
}
