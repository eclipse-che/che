/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;

import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;

public class EphemeralWorkspaceUtility {

  /**
   * @param workspaceAttributes workspace config or devfile attributes to check is ephemeral mode is
   *     enabled
   * @return true if `persistVolumes` attribute exists and set to 'false'. In this case regardless
   *     of the PVC strategy, workspace volumes would be created as `emptyDir`. When a workspace Pod
   *     is removed for any reason, the data in the `emptyDir` volume is deleted forever
   */
  public static boolean isEphemeral(Map<String, String> workspaceAttributes) {
    String persistVolumes = workspaceAttributes.get(PERSIST_VOLUMES_ATTRIBUTE);
    return "false".equals(persistVolumes);
  }

  /**
   * @param workspace workspace to check is ephemeral mode is enabled
   * @return true if workspace config contains `persistVolumes` attribute which is set to false. In
   *     this case regardless of the PVC strategy, workspace volumes would be created as `emptyDir`.
   *     When a workspace Pod is removed for any reason, the data in the `emptyDir` volume is
   *     deleted forever
   */
  public static boolean isEphemeral(Workspace workspace) {
    Devfile devfile = workspace.getDevfile();
    if (devfile != null) {
      return isEphemeral(devfile.getAttributes());
    }

    return isEphemeral(workspace.getConfig().getAttributes());
  }

  /**
   * Change workspace attributes such that future calls to {@link #isEphemeral(Map)} will return
   * true.
   *
   * @param workspaceAttributes workspace config or devfile attributes to which ephemeral mode
   *     configuration should be provisioned
   */
  public static void makeEphemeral(Map<String, String> workspaceAttributes) {
    workspaceAttributes.put(PERSIST_VOLUMES_ATTRIBUTE, "false");
  }
}
