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
package org.eclipse.che.api.agent.server.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;

/**
 * Adds exec installer into each agents list in {@link WorkspaceConfigDto} where terminal installer
 * is present.
 *
 * @author Alexander Garagatyi
 */
public class AddExecInstallerInEnvironmentUtil {
  public static void addExecInstaller(WorkspaceConfigDto workspaceConfig) {
    if (workspaceConfig != null) {
      Map<String, EnvironmentDto> environments = workspaceConfig.getEnvironments();
      if (environments != null) {
        for (EnvironmentDto environment : environments.values()) {
          if (environment != null && environment.getMachines() != null) {
            for (MachineConfigDto machine : environment.getMachines().values()) {
              if (machine.getInstallers() != null) {
                if (containsTerminalInstallerWithoutExec(machine.getInstallers())) {
                  ArrayList<String> updatedInstallers = new ArrayList<>(machine.getInstallers());
                  updatedInstallers.add("org.eclipse.che.exec");
                  machine.setInstallers(updatedInstallers);
                }
              }
            }
          }
        }
      }
    }
  }

  private static boolean containsTerminalInstallerWithoutExec(List<String> installers) {
    return InstallerFqn.idInKeyList("org.eclipse.che.terminal", installers)
        && !(InstallerFqn.idInKeyList("org.eclipse.che.exec", installers));
  }
}
