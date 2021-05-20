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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment.util;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;

/** Can be used to parse container entry-point definition specified as a YAML list of strings. */
public class EntryPointParser {
  private final YAMLMapper mapper = new YAMLMapper();

  /**
   * Parses the attributes contained in the provided machine config and produces an entry point
   * definition.
   *
   * <p>This method looks for the values of the {@link MachineConfig#CONTAINER_COMMAND_ATTRIBUTE}
   * and {@link MachineConfig#CONTAINER_ARGS_ATTRIBUTE} attributews in the provided map and
   * constructs an Entrypoint instance parsed out of the contents of those attributes.
   *
   * @param machineAttributes the attributes of a machine to extract the entry point info from
   * @return an entry point definition, never null
   * @throws InfrastructureException on failure to parse the command or arguments
   */
  public EntryPoint parse(Map<String, String> machineAttributes) throws InfrastructureException {
    String command = machineAttributes.get(MachineConfig.CONTAINER_COMMAND_ATTRIBUTE);
    String args = machineAttributes.get(MachineConfig.CONTAINER_ARGS_ATTRIBUTE);

    List<String> commandList =
        command == null
            ? emptyList()
            : parseAsList(command, MachineConfig.CONTAINER_COMMAND_ATTRIBUTE);
    List<String> argList =
        args == null ? emptyList() : parseAsList(args, MachineConfig.CONTAINER_ARGS_ATTRIBUTE);

    return new EntryPoint(commandList, argList);
  }

  /**
   * Serializes an entry (that might have been produced from {@link #parse(Map)}) back to a string
   * representation.
   *
   * @param entry the command or args entry
   * @return a serialized representation of the entry
   */
  public String serializeEntry(List<String> entry) {
    try {
      return mapper.writer().writeValueAsString(entry);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(
          format("Failed to serialize list of strings %s to YAML", entry), e);
    }
  }

  private List<String> parseAsList(String data, String attributeName)
      throws InfrastructureException {
    try {
      return mapper.readValue(data, new TypeReference<List<String>>() {});
    } catch (IOException e) {
      throw new InfrastructureException(
          format(
              "Failed to parse the attribute %s as a YAML list. The value was %s",
              attributeName, data),
          e.getCause());
    }
  }
}
