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
package org.eclipse.che.workspace.infrastructure.docker.environment.dockerfile;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;

/** @author Sergii Leshchenko */
@Singleton
public class DockerfileEnvironmentFactory
    extends InternalEnvironmentFactory<DockerfileEnvironment> {


  @Inject
  public DockerfileEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      MachineConfigsValidator machinesValidator,
      @Named("che.workspace.default_memory_limit_mb") long defaultMachineMaxMemorySizeMB,
      @Named("che.workspace.default_memory_request_mb") long defaultMachineRequestMemorySizeMB) {
    super(installerRegistry, recipeRetriever, machinesValidator, defaultMachineMaxMemorySizeMB, defaultMachineRequestMemorySizeMB);
  }

  @Override
  protected DockerfileEnvironment doCreate(
      InternalRecipe recipe, Map<String, InternalMachineConfig> machines, List<Warning> warnings)
      throws InfrastructureException, ValidationException {
    if (!DockerfileEnvironment.TYPE.equals(recipe.getType())) {
      throw new ValidationException(
          format(
              "Dockerfile environment parser doesn't support recipe type '%s'", recipe.getType()));
    }

    String dockerfile = recipe.getContent();

    checkArgument(dockerfile != null, "Dockerfile content should not be null.");

    addRamAttributes(machines);

    return new DockerfileEnvironment(dockerfile, recipe, machines, warnings);
  }

  void addRamAttributes(Map<String, InternalMachineConfig> machines) {
    // sets default ram limit and request attributes if not present
    for (InternalMachineConfig machineConfig : machines.values()) {
      initIfEmpty(machineConfig, MEMORY_LIMIT_ATTRIBUTE, defaultMachineMaxMemorySizeAttribute);
      initIfEmpty(
          machineConfig, MEMORY_REQUEST_ATTRIBUTE, defaultMachineRequestMemorySizeAttribute);
    }
  }

  private void initIfEmpty(
      InternalMachineConfig machineConfig, String attribute, String defaultValue) {
    if (isNullOrEmpty(machineConfig.getAttributes().get(attribute))) {
      machineConfig.getAttributes().put(attribute, defaultValue);
    }
  }
}
