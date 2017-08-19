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
package org.eclipse.che.api.workspace.server;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;

/**
 * Utility class for workspace related code that might be useful on server or GWT client.
 *
 * @author Alexander Garagatyi
 */
public class Utils {
  public static final String WSAGENT_INSTALLER = "org.eclipse.che.ws-agent";

  private Utils() {}

  /**
   * Finds dev machine name in environment definition.
   *
   * @param envConfig environment definition
   * @return dev machine name or {@code null} if dev machine is not found in environment
   */
  public static String getDevMachineName(Environment envConfig) {
    for (Map.Entry<String, ? extends MachineConfig> entry : envConfig.getMachines().entrySet()) {
      // TODO should we use server ref instead of installers?
      if (isDev(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * Checks whether given machine installers contains 'ws-agent' installer.
   *
   * @param machineConf given machine config
   * @return true if given machine config contains ws-agent installer otherwise false will be
   *     returned
   */
  public static boolean isDev(MachineConfig machineConf) {
    final List<String> installers = machineConf.getInstallers();
    return InstallerFqn.idInKeyList(WSAGENT_INSTALLER, installers);
  }
}
