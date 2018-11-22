/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.usage.tracker;

import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;

import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * Helps to calculate amount of RAM defined in {@link Environment environment}
 *
 * @author Sergii Leschenko
 * @author Anton Korneta
 */
public class EnvironmentRamCalculator {
  private static final long BYTES_TO_MEGABYTES_DIVIDER = 1024L * 1024L;

  private final Map<String, InternalEnvironmentFactory> environmentFactories;

  @Inject
  public EnvironmentRamCalculator(Map<String, InternalEnvironmentFactory> environmentFactories) {
    this.environmentFactories = environmentFactories;
  }

  /**
   * Parses (and fetches if needed) recipe of environment and sums RAM size of all machines in
   * environment in megabytes.
   */
  public long calculate(@Nullable Environment environment) throws ServerException {
    if (environment == null) {
      return 0;
    }
    try {
      return getInternalEnvironment(environment)
              .getMachines()
              .values()
              .stream()
              .mapToLong(
                  m -> parseMemoryAttributeValue(m.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE)))
              .sum()
          / BYTES_TO_MEGABYTES_DIVIDER;
    } catch (InfrastructureException | ValidationException | NotFoundException ex) {
      throw new ServerException(ex.getMessage(), ex);
    }
  }

  /**
   * Calculates summary RAM of given {@link Runtime}.
   *
   * @return summary RAM of all machines in runtime in megabytes
   */
  public long calculate(Runtime runtime) {
    return runtime
            .getMachines()
            .values()
            .stream()
            .mapToLong(
                m -> parseMemoryAttributeValue(m.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE)))
            .sum()
        / BYTES_TO_MEGABYTES_DIVIDER;
  }

  /**
   * Parse {@link MachineConfig#MEMORY_LIMIT_ATTRIBUTE} value to {@code Long}.
   *
   * @param attributeValue value of {@link MachineConfig#MEMORY_LIMIT_ATTRIBUTE} attribute from
   *     machine config or runtime
   * @return long value parsed from provided string attribute value or {@code 0} if {@code null} is
   *     provided
   * @throws NumberFormatException if provided value is neither {@code null} nor valid stringified
   *     long
   * @see Long#parseLong(String)
   */
  private long parseMemoryAttributeValue(String attributeValue) {
    if (attributeValue == null) {
      return 0;
    } else {
      return Long.parseLong(attributeValue);
    }
  }

  private InternalEnvironment getInternalEnvironment(Environment environment)
      throws InfrastructureException, ValidationException, NotFoundException {
    final String recipeType = environment.getRecipe().getType();
    final InternalEnvironmentFactory factory = environmentFactories.get(recipeType);
    if (factory == null) {
      throw new NotFoundException(
          format("InternalEnvironmentFactory is not configured for recipe type: '%s'", recipeType));
    }
    return factory.create(environment);
  }
}
