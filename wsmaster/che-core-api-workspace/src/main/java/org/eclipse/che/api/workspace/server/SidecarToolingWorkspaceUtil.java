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
package org.eclipse.che.api.workspace.server;

import com.google.common.base.Strings;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.workspace.shared.Constants;

/**
 * Util class to deal with sidecar based workspaces.
 *
 * @author Alexander Garagatyi
 */
public class SidecarToolingWorkspaceUtil {

  /**
   * Checks whether provided workspace config attributes {@link WorkspaceConfig#getAttributes()}
   * contains configuration of sidecars. <br>
   * This indicates whether this workspace is Che6 or Che7 compatible.
   */
  public static boolean isSidecarBasedWorkspace(Map<String, String> attributes) {
    boolean hasPlugins =
        !Strings.isNullOrEmpty(
            attributes.getOrDefault(Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, null));
    boolean hasEditor =
        !Strings.isNullOrEmpty(
            attributes.getOrDefault(Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, null));
    return hasPlugins || hasEditor;
  }
}
