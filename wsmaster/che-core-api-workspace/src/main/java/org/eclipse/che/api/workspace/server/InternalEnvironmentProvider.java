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
package org.eclipse.che.api.workspace.server;

import static java.lang.String.format;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;

/**
 * Creates {@link InternalEnvironment} from provided environment configuration.
 *
 * @author Oleksandr Garagatyi
 */
public class InternalEnvironmentProvider {

  private final Map<String, InternalEnvironmentFactory> environmentFactories;

  @Inject
  public InternalEnvironmentProvider(Map<String, InternalEnvironmentFactory> environmentFactories) {
    this.environmentFactories = environmentFactories;
  }

  public Set<String> supportedRecipes() {
    return environmentFactories.keySet();
  }

  public InternalEnvironment create(
      Environment environment, Map<String, String> workspaceAttributes)
      throws InfrastructureException, ValidationException, NotFoundException {

    String recipeType = environment.getRecipe().getType();
    InternalEnvironmentFactory factory = environmentFactories.get(recipeType);
    if (factory == null) {
      throw new NotFoundException(
          format("InternalEnvironmentFactory is not configured for recipe type: '%s'", recipeType));
    }
    return factory.create(environment);
  }
}
