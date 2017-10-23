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

import static com.google.common.base.Strings.isNullOrEmpty;
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
  private final Map<String, String> env;
  private final Map<String, String> attributes;

  InternalMachineConfig(MachineConfig originalConfig, InstallerRegistry installerRegistry)
      throws InfrastructureException {
    this.servers = new HashMap<>();
    if (originalConfig.getServers() != null) {
      this.servers.putAll(originalConfig.getServers());
    }

    this.env = new HashMap<>();
    if (originalConfig.getEnv() != null) {
      this.env.putAll(originalConfig.getEnv());
    }

    this.attributes = new HashMap<>();
    if (originalConfig.getAttributes() != null) {
      this.attributes.putAll(originalConfig.getAttributes());
    }

    this.installers = new ArrayList<>();
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

  /** Returns unmodifiable map of machine environment variables. */
  public Map<String, String> getEnv() {
    return Collections.unmodifiableMap(env);
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
        applyInstaller(installer);
      }
    } catch (InstallerException e) {
      throw new InfrastructureException(e.getLocalizedMessage(), e);
    }
  }

  private void applyInstaller(Installer installer) throws InfrastructureException {
    this.installers.add(new InstallerImpl(installer));
    for (Map.Entry<String, ? extends ServerConfig> serverEntry :
        installer.getServers().entrySet()) {
      if (servers.putIfAbsent(serverEntry.getKey(), serverEntry.getValue()) != null
          && !servers.get(serverEntry.getKey()).equals(serverEntry.getValue())) {
        throw new InfrastructureException(
            format(
                "Installer '%s' contains server '%s' conflicting with machine configuration",
                installer.getId(), serverEntry.getKey()));
      }
    }
    addEnvVars(installer);
  }

  private void addEnvVars(Installer installer) {
    String environment = installer.getProperties().get(Installer.ENVIRONMENT_PROPERTY);
    if (isNullOrEmpty(environment)) {
      return;
    }

    for (String env : environment.split(",")) {
      String[] items = env.split("=");
      if (items.length != 2) {
        // TODO add warning
        // LOG.warn(format("Illegal environment variable '%s' format", env));
        continue;
      }

      this.env.put(items[0], items[1]);
    }
  }
}
