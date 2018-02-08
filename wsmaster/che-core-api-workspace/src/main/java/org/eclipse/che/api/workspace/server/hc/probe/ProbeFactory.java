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
package org.eclipse.che.api.workspace.server.hc.probe;

/**
 * Produces {@link Probe} instances and holds information about probe configuration and Che
 * workspace server the probe corresponds to.
 *
 * @author Alexander Garagatyi
 */
public abstract class ProbeFactory {
  private final String workspaceId;
  private final String machineName;
  private final String serverName;
  private final ProbeConfig probeConfig;

  protected ProbeFactory(
      String workspaceId, String machineName, String serverName, ProbeConfig probeConfig) {
    this.workspaceId = workspaceId;
    this.machineName = machineName;
    this.serverName = serverName;
    this.probeConfig = probeConfig;
  }

  /** Returns an instance of a probe for a server in a workspace */
  public abstract Probe get();

  /** Returns ID of a workspace the probe corresponds to */
  public String getWorkspaceId() {
    return workspaceId;
  }

  /** Returns name of a machine the probe corresponds to */
  public String getMachineName() {
    return machineName;
  }

  /** Returns name of a workspace server the probe corresponds to */
  public String getServerName() {
    return serverName;
  }

  /** Returns configuration of a probe */
  public ProbeConfig getProbeConfig() {
    return probeConfig;
  }
}
