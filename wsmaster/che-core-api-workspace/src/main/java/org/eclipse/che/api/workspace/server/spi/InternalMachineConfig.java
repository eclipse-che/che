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
package org.eclipse.che.api.workspace.server.spi;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.shared.model.Installer;

/**
 * "pre-processed" Machine Config. To use inside infrastructure
 *
 * @author gazarenkov
 */
public class InternalMachineConfig {

  // ordered installers to launch on start
  private final List<InstallerImpl> installers;
  // set of servers including ones configured by installers
  private final Map<String, ServerConfig> servers;
  private final Map<String, String> attributes;

  InternalMachineConfig(MachineConfig originalConfig, InstallerRegistry installerRegistry)
      throws InfrastructureException {
    this.installers = new ArrayList<>();
    this.servers = new HashMap<>(originalConfig.getServers());
    this.attributes = new HashMap<>(originalConfig.getAttributes());

    initInstallers(originalConfig.getInstallers(), installerRegistry);
  }

  /**
   * Returns unmodifiable map of servers configured in the machine.
   *
   * <p>Note that servers provided by installers in this machine are already added to this map.
   */
  public Map<String, ServerConfig> getServers() {
    return Collections.unmodifiableMap(servers);
  }

  /** Returns unmodifiable list of installers configs of the machine. */
  public List<InstallerImpl> getInstallers() {
    return Collections.unmodifiableList(installers);
  }

  /** Returns unmodifiable map of machine attributes. */
  public Map<String, String> getAttributes() {
    return Collections.unmodifiableMap(attributes);
  }

  private void initInstallers(List<String> installersKeys, InstallerRegistry installerRegistry)
      throws InfrastructureException {
    try {
      List<Installer> sortedInstallers = installerRegistry.getOrderedInstallers(installersKeys);
      for (Installer installer : sortedInstallers) {
        this.installers.add(new InstallerImpl(installer));
        for (Map.Entry<String, ? extends ServerConfig> serverEntry :
            installer.getServers().entrySet()) {
          if (servers.putIfAbsent(serverEntry.getKey(), serverEntry.getValue()) != null
              && servers.get(serverEntry.getKey()).equals(serverEntry.getValue())) {
            throw new InfrastructureException(
                format(
                    "Installer '%s' contains server '%s' conflicting with machine configuration",
                    installer.getId(), serverEntry.getKey()));
          }
        }
      }
    } catch (InstallerException e) {
      // TODO installers has circular dependency or missing, what should we throw in that case?
      throw new InfrastructureException(e.getLocalizedMessage(), e);
    }
  }
}
