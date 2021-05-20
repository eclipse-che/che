/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.spi.environment;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.config.Volume;

/**
 * Machine Config to use inside infrastructure.
 *
 * <p>It contains:
 *
 * <ul>
 *   <li>retrieved full information about installers;
 *   <li>normalized server configs.
 * </ul>
 *
 * @author gazarenkov
 */
public class InternalMachineConfig {
  private final Map<String, ServerConfig> servers;
  private final Map<String, String> env;
  private final Map<String, String> attributes;
  private final Map<String, Volume> volumes;

  public InternalMachineConfig() {
    this.servers = new HashMap<>();
    this.env = new HashMap<>();
    this.attributes = new HashMap<>();
    this.volumes = new HashMap<>();
  }

  public InternalMachineConfig(
      Map<String, ? extends ServerConfig> servers,
      Map<String, String> env,
      Map<String, String> attributes,
      Map<String, ? extends Volume> volumes) {
    this();
    if (servers != null) {
      this.servers.putAll(servers);
    }
    if (env != null) {
      this.env.putAll(env);
    }
    if (attributes != null) {
      this.attributes.putAll(attributes);
    }
    if (volumes != null) {
      this.volumes.putAll(volumes);
    }
  }

  /** Returns modifiable map of servers configured in the machine. */
  public Map<String, ServerConfig> getServers() {
    return servers;
  }

  /** Returns modifiable map of machine environment variables. */
  public Map<String, String> getEnv() {
    return env;
  }

  /** Returns modifiable map of machine attributes. */
  public Map<String, String> getAttributes() {
    return attributes;
  }

  /** Returns modifiable map of machine volumes. */
  public Map<String, Volume> getVolumes() {
    return volumes;
  }
}
