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

  private final InstallerRegistry installerRegistry;

  public InternalMachineConfig(MachineConfig originalConfig, InstallerRegistry installerRegistry)
      throws InfrastructureException {
    this.installerRegistry = installerRegistry;
    this.installers = new ArrayList<>();
    this.servers = new HashMap<>();
    this.servers.putAll(originalConfig.getServers());
    this.attributes = new HashMap<>(originalConfig.getAttributes());

    if (installerRegistry != null) initInstallers(originalConfig.getInstallers());
  }

  /** @return servers */
  public Map<String, ServerConfig> getServers() {
    return Collections.unmodifiableMap(servers);
  }

  /** @return installers */
  public List<InstallerImpl> getInstallers() {
    return installers;
  }

  /** @return attributes */
  public Map<String, String> getAttributes() {
    return attributes;
  }

  private void initInstallers(List<String> installersKeys) throws InfrastructureException {
    try {
      // TODO ensure already contains dependencies
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
