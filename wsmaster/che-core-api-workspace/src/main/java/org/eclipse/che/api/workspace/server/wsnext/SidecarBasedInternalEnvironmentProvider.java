/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.wsnext;

import java.util.Collection;
import java.util.Map;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.InternalEnvironmentProvider;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.wsnext.model.ChePlugin;

/**
 * Creates {@link InternalEnvironment} from provided environment configuration, then converts it
 * using {@link InternalEnvironmentConverter} and applies {@link WorkspaceNextApplier} to the
 * resulting {@code InternalEnvironment} to add sidecar tooling
 *
 * @author Oleksandr Garagatyi
 */
public class SidecarBasedInternalEnvironmentProvider extends InternalEnvironmentProvider {
  private final Map<String, WorkspaceNextApplier> workspaceNextAppliers;
  private final WorkspaceNextObjectsRetriever workspaceNextObjectsRetriever;
  private final InternalEnvironmentConverter environmentConverter;

  public SidecarBasedInternalEnvironmentProvider(
      Map<String, WorkspaceNextApplier> workspaceNextAppliers,
      WorkspaceNextObjectsRetriever workspaceNextObjectsRetriever,
      Map<String, InternalEnvironmentFactory> environmentFactories,
      InternalEnvironmentConverter environmentConverter) {
    super(environmentFactories);
    this.workspaceNextAppliers = workspaceNextAppliers;
    this.workspaceNextObjectsRetriever = workspaceNextObjectsRetriever;
    this.environmentConverter = environmentConverter;
  }

  @Override
  public InternalEnvironment create(
      Environment environment, Map<String, String> workspaceAttributes)
      throws InfrastructureException, ValidationException, NotFoundException {

    InternalEnvironment internalEnvironment = super.create(environment, workspaceAttributes);

    InternalEnvironment convertedEnvironment = environmentConverter.convert(internalEnvironment);

    applyWorkspaceNext(
        convertedEnvironment, workspaceAttributes, environment.getRecipe().getType());

    return convertedEnvironment;
  }

  private void applyWorkspaceNext(
      InternalEnvironment internalEnvironment,
      Map<String, String> workspaceAttributes,
      String recipeType)
      throws InfrastructureException {
    Collection<ChePlugin> chePlugins = workspaceNextObjectsRetriever.get(workspaceAttributes);
    if (chePlugins.isEmpty()) {
      return;
    }
    WorkspaceNextApplier wsNext = workspaceNextAppliers.get(recipeType);
    if (wsNext == null) {
      throw new InfrastructureException(
          "Workspace.Next features are not supported for recipe type " + recipeType);
    }
    wsNext.apply(internalEnvironment, chePlugins);
  }
}
