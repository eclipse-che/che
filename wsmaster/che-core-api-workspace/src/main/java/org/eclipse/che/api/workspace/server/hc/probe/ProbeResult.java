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
package org.eclipse.che.api.workspace.server.hc.probe;

/**
 * Result of a probe. Should be fired in accordance with {@link ProbeConfig}, e.g. thresholds,
 * timeouts. In particular if a threshold if >1 then this result should be fired after reaching of
 * the threshold instead of on each probe check.
 *
 * @author Alexander Garagatyi
 */
public class ProbeResult {
  public enum ProbeStatus {
    PASSED,
    FAILED
  }

  private final String workspaceId;
  private final String machineName;
  private final String serverName;
  private final ProbeStatus status;

  public ProbeResult(
      String workspaceId, String machineName, String serverName, ProbeStatus status) {
    this.workspaceId = workspaceId;
    this.machineName = machineName;
    this.serverName = serverName;
    this.status = status;
  }

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

  /** Status of a probe */
  public ProbeStatus getStatus() {
    return status;
  }
}
