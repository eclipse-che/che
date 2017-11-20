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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeRecipe;

/** @author Sergii Leshchenko */
@Singleton
public class ComposeEnvironmentFactory extends InternalEnvironmentFactory<ComposeEnvironment> {

  private static final ObjectMapper YAML_PARSER = new ObjectMapper(new YAMLFactory());

  private static Set<String> YAML_CONTENT_TYPES =
      ImmutableSet.of("application/x-yaml", "text/yaml", "text/x-yaml");

  private final ComposeServicesStartStrategy startStrategy;
  private final ComposeEnvironmentValidator composeValidator;

  @Inject
  public ComposeEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      MachineConfigsValidator machinesValidator,
      ComposeEnvironmentValidator composeValidator,
      ComposeServicesStartStrategy startStrategy) {
    super(installerRegistry, recipeRetriever, machinesValidator);
    this.startStrategy = startStrategy;
    this.composeValidator = composeValidator;
  }

  @Override
  protected ComposeEnvironment doCreate(
      InternalRecipe recipe, Map<String, InternalMachineConfig> machines, List<Warning> warnings)
      throws InfrastructureException, ValidationException {
    String contentType = recipe.getContentType();
    checkNotNull(contentType, "Recipe content type should not be null");

    String recipeContent = recipe.getContent();

    if (!ComposeEnvironment.TYPE.equals(recipe.getType())) {
      throw new ValidationException(
          format("Compose environment parser doesn't support recipe type '%s'", recipe.getType()));
    }

    if (!YAML_CONTENT_TYPES.contains(contentType)) {
      throw new ValidationException(
          format(
              "Provided environment recipe content type '%s' is "
                  + "unsupported. Supported values are: %s",
              contentType, YAML_CONTENT_TYPES.stream().collect(joining(", "))));
    }
    ComposeRecipe composeRecipe = doParse(recipeContent);

    ComposeEnvironment composeEnvironment =
        new ComposeEnvironment(
            composeRecipe.getVersion(),
            startStrategy.order(composeRecipe.getServices()),
            recipe,
            machines,
            warnings);

    composeValidator.validate(composeEnvironment);

    return composeEnvironment;
  }

  @VisibleForTesting
  ComposeRecipe doParse(String recipeContent) throws ValidationException {
    ComposeRecipe composeRecipe;
    try {
      composeRecipe = YAML_PARSER.readValue(recipeContent, ComposeRecipe.class);
    } catch (IOException e) {
      throw new ValidationException(
          "Parsing of environment configuration failed. " + e.getLocalizedMessage());
    }
    return composeRecipe;
  }

  private static void checkNotNull(
      Object object, String errorMessageTemplate, Object... errorMessageParams)
      throws ValidationException {
    if (object == null) {
      throw new ValidationException(format(errorMessageTemplate, errorMessageParams));
    }
  }
}
