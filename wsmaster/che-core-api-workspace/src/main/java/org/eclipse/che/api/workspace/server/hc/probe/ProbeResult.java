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
