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
package org.eclipse.che.workspace.infrastructure.docker.provisioner.installer;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.docker.provisioner.installer.InstallerConfigApplier.PROPERTIES.ENVIRONMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.docker.Labels;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.slf4j.Logger;

/**
 * Applies docker specific properties of the installers to {@link DockerContainerConfig} or {@link
 * DockerEnvironment}. Dependencies between installers are respected. This class must be called
 * before machines represented by {@link DockerContainerConfig} is started, otherwise changing
 * configuration has no effect. </br> The list of supported properties are:
 * <li>environment The {@code environment} property contains command separated environment variables
 *     to set respecting the following format: "name=value".
 *
 * @author Anatolii Bazko
 * @author Alexander Garagatyi
 * @see Installer#getProperties()
 * @see DockerContainerConfig#getEnvironment()
 * @see DockerContainerConfig#getPorts()
 * @see DockerContainerConfig#getLabels()
 */
public class InstallerConfigApplier {
  private static final Logger LOG = getLogger(InstallerConfigApplier.class);

  private final InstallerRegistry installerRegistry;

  @Inject
  public InstallerConfigApplier(InstallerRegistry installerRegistry) {
    this.installerRegistry = installerRegistry;
  }

  /**
   * Applies docker specific properties to an environment of machines.
   *
   * @param envConfig environment config with the list of installers that should be injected into
   *     machine
   * @param dockerEnvironment affected environment of machines
   * @throws InstallerException if any error occurs
   */
  public void apply(Environment envConfig, DockerEnvironment dockerEnvironment)
      throws InstallerException {
    for (Map.Entry<String, ? extends MachineConfig> machineEntry :
        envConfig.getMachines().entrySet()) {
      String machineName = machineEntry.getKey();
      MachineConfig machineConf = machineEntry.getValue();
      DockerContainerConfig dockerContainer = dockerEnvironment.getContainers().get(machineName);

      apply(machineConf, dockerContainer);
    }
  }

  /**
   * Applies docker specific properties to a machine.
   *
   * @param machineConf machine config with the list of installer that should be injected into
   *     machine
   * @param machine affected machine
   * @throws InstallerException if any error occurs
   */
  public void apply(@Nullable MachineConfig machineConf, DockerContainerConfig machine)
      throws InstallerException {
    if (machineConf != null) {
      for (Installer installer :
          installerRegistry.getOrderedInstallers(machineConf.getInstallers())) {
        addEnv(machine, installer.getProperties());
        addExposedPorts(machine, installer.getServers());
        addLabels(machine, installer.getServers());
      }
    }
  }

  private void addLabels(
      DockerContainerConfig container, Map<String, ? extends ServerConfig> servers) {
    container.getLabels().putAll(Labels.newSerializer().servers(servers).labels());
  }

  private void addEnv(DockerContainerConfig container, Map<String, String> properties) {
    String environment = properties.get(ENVIRONMENT.toString());
    if (isNullOrEmpty(environment)) {
      return;
    }

    Map<String, String> newEnv = new HashMap<>();
    if (container.getEnvironment() != null) {
      newEnv.putAll(container.getEnvironment());
    }

    for (String env : environment.split(",")) {
      String[] items = env.split("=");
      if (items.length != 2) {
        LOG.warn(format("Illegal environment variable '%s' format", env));
        continue;
      }
      String var = items[0];
      String name = items[1];

      newEnv.put(var, name);
    }

    container.setEnvironment(newEnv);
  }

  private void addExposedPorts(
      DockerContainerConfig container, Map<String, ? extends ServerConfig> servers) {
    for (ServerConfig server : servers.values()) {
      container.getExpose().add(server.getPort());
    }
  }

  public enum PROPERTIES {
    ENVIRONMENT("environment");

    private final String value;

    PROPERTIES(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
