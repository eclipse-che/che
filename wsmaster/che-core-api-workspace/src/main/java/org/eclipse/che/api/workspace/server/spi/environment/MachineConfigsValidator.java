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
package org.eclipse.che.api.workspace.server.spi.environment;

import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;

/** @author Sergii Leshchenko */
public class MachineConfigsValidator {
  private static final String MACHINE_NAME_REGEXP = "[a-zA-Z0-9]+([a-zA-Z0-9_/-]*[a-zA-Z0-9])?";
  private static final Pattern MACHINE_NAME_PATTERN =
      Pattern.compile("^" + MACHINE_NAME_REGEXP + "$");

  private static final Pattern SERVER_PORT_PATTERN = Pattern.compile("^[1-9]+[0-9]*(/(tcp|udp))?$");
  private static final Pattern SERVER_PROTOCOL_PATTERN = Pattern.compile("^[a-z][a-z0-9-+.]*$");

  /**
   * Validates the specified machine configs.
   *
   * @param machines machines configs to validate
   * @throws ValidationException when the specified environment is not valid
   */
  public void validate(Map<String, InternalMachineConfig> machines) throws ValidationException {
    for (Entry<String, InternalMachineConfig> machineConfigEntry : machines.entrySet()) {
      validateMachine(machineConfigEntry.getKey(), machineConfigEntry.getValue());
    }
  }

  private void validateMachine(String machineName, InternalMachineConfig machineConfig)
      throws ValidationException {

    checkArgument(
        MACHINE_NAME_PATTERN.matcher(machineName).matches(),
        "Name of machine '%s' in environment is invalid",
        machineName);

    if (machineConfig.getServers() != null) {
      for (Map.Entry<String, ? extends ServerConfig> serverEntry :
          machineConfig.getServers().entrySet()) {
        String serverName = serverEntry.getKey();
        ServerConfig server = serverEntry.getValue();

        checkArgument(
            server.getPort() != null && SERVER_PORT_PATTERN.matcher(server.getPort()).matches(),
            "Machine '%s' in environment contains server conf '%s' with invalid port '%s'",
            machineName,
            serverName,
            server.getPort());
        checkArgument(
            server.getProtocol() == null
                || SERVER_PROTOCOL_PATTERN.matcher(server.getProtocol()).matches(),
            "Machine '%s' in environment contains server conf '%s' with invalid protocol '%s'",
            machineName,
            serverName,
            server.getProtocol());
      }
    }

    if (machineConfig.getAttributes() != null) {
      String memoryLimit = machineConfig.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE);
      String memoryRequest = machineConfig.getAttributes().get(MEMORY_REQUEST_ATTRIBUTE);
      if (memoryLimit != null && memoryRequest != null) {
        checkArgument(
            Long.parseLong(memoryLimit) >= Long.parseLong(memoryRequest),
            "Machine '%s' in environment contains inconsistent memory attributes: Memory limit: '%s', Memory request: '%s'",
            machineName,
            Long.parseLong(memoryLimit),
            Long.parseLong(memoryRequest));
      }
    }
  }

  private static void checkArgument(
      boolean expression, String errorMessageTemplate, Object... errorMessageParams)
      throws ValidationException {
    if (!expression) {
      throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
    }
  }
}
