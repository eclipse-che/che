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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.volume;

/**
 * Helps to generate volume names and match existing volumes to workspace.
 *
 * @author Alexander Garagatyi
 */
public class VolumeNames {

  /** Generates unique volume name for a volume in a workspace. */
  public static String generate(String workspaceId, String originVolumeName) {
    return workspaceId + '_' + originVolumeName;
  }

  /** Check whether a volume with provided name belongs to workspace with provided ID. */
  public static boolean matches(String volumeName, String workspaceId) {
    return volumeName.startsWith(workspaceId + '_');
  }

  /**
   * Check whether a volume with provided name matches origin Che model volume of workspace with
   * provided ID.
   */
  public static boolean matches(String volumeName, String originVolumeName, String workspaceId) {
    return volumeName.equals(workspaceId + '_' + originVolumeName);
  }

  private VolumeNames() {}
}
