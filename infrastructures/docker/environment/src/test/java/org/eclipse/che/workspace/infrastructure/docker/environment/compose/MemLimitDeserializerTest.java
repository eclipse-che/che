/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.deserializer.MemLimitDeserializer;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeRecipe;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test of {@link MemLimitDeserializer} functionality.
 *
 * @author Mykhailo Kuznietsov
 */
public class MemLimitDeserializerTest {
  @Mock private InstallerRegistry installerRegistry;
  @Mock private RecipeRetriever recipeRetriever;
  @Mock private MachineConfigsValidator machinesValidator;
  @Mock private ComposeEnvironmentValidator composeValidator;
  @Mock private ComposeServicesStartStrategy startStrategy;

  private ComposeEnvironmentFactory factory;

  private static final long K = 1024;
  private static final long M = 1024 * K;
  private static final long G = 1024 * M;

  private static final String RECIPE_WITHOUT_MEMORY_LIMIT =
      "services:\n" + " machine1:\n" + "  mem_limit: %s";

  @BeforeMethod
  public void setup() {
    factory =
        new ComposeEnvironmentFactory(
            installerRegistry,
            recipeRetriever,
            machinesValidator,
            composeValidator,
            startStrategy,
            2048);
  }

  @Test(dataProvider = "validValues")
  public void shouldDeserializeValues(Object memoryLimit, Object parsedLimit)
      throws ValidationException {
    ComposeRecipe recipe = factory.doParse(String.format(RECIPE_WITHOUT_MEMORY_LIMIT, memoryLimit));
    assertEquals(recipe.getServices().get("machine1").getMemLimit(), parsedLimit);
  }

  @Test(dataProvider = "invalidValues", expectedExceptions = ValidationException.class)
  public void shouldThrowExceptionOnDeseralization(Object memoryLimit) throws ValidationException {
    factory.doParse(String.format(RECIPE_WITHOUT_MEMORY_LIMIT, memoryLimit));
  }

  @DataProvider(name = "validValues")
  public Object[][] validValues() {
    return new Object[][] {
      {"2048", 2048L},
      {"1gb", 1 * G},
      {"2g", 2 * G},
      {"1073741824", 1 * G},
      {"8m", 8 * M},
      {"200mb", 200 * M},
      {"5k", 5 * K},
      {"10kb", 10 * K},
    };
  }

  @DataProvider(name = "invalidValues")
  public Object[][] invalidValues() {
    return new Object[][] {{"1m1"}, {"notanumber"}, {1024.5}};
  }
}
