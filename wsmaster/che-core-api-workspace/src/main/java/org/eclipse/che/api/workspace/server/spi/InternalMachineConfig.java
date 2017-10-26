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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.shared.model.Installer;

/**
 * "pre-processed" Machine Config. To use inside infrastructure
 *
 * @author gazarenkov
 */
public class InternalMachineConfig {
  private final List<Installer> installers;
  private final Map<String, ServerConfig> servers;
  private final Map<String, String> env;
  private final Map<String, String> attributes;

  InternalMachineConfig(
      List<Installer> installers,
      Map<String, ServerConfig> servers,
      Map<String, String> env,
      Map<String, String> attributes)
      throws InfrastructureException {
    this.servers = servers;
    this.installers = installers;
    this.env = env;
    this.attributes = attributes;
  }

  /**
   * Returns unmodifiable map of servers configured in the machine.
   *
   * <p>Note that servers provided by installers in this machine are already added to this map.
   */
  public Map<String, ServerConfig> getServers() {
    return Collections.unmodifiableMap(servers);
  }

  /** Returns unmodifiable ordered list of installers configs of the machine. */
  public List<Installer> getInstallers() {
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
}
