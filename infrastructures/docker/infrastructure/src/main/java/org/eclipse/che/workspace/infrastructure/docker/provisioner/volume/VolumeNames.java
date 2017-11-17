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
