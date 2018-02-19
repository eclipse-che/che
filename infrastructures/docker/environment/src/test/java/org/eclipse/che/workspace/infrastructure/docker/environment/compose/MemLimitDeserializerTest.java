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

  @Mock InstallerRegistry installerRegistry;
  @Mock RecipeRetriever recipeRetriever;
  @Mock MachineConfigsValidator machinesValidator;
  @Mock ComposeEnvironmentValidator composeValidator;
  @Mock ComposeServicesStartStrategy startStrategy;

  private ComposeEnvironmentFactory factory;

  private static final String RECIPE_WITHOUT_COMMAND_VALUE =
      "services:\n"
          + " machine1:\n"
          + "  image: codenvy/mysql\n"
          + "  environment:\n"
          + "   MYSQL_USER: petclinic\n"
          + "   MYSQL_PASSWORD: password\n"
          + "  mem_limit: %s\n"
          + // <- test target
          "  expose: [4403, 5502]";

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
    ComposeRecipe service =
        factory.doParse(String.format(RECIPE_WITHOUT_COMMAND_VALUE, memoryLimit));
    assertEquals(service.getServices().get("machine1").getMemLimit(), parsedLimit);
  }

  @Test(dataProvider = "invalidValues", expectedExceptions = ValidationException.class)
  public void shouldThrowExceptionOnDeseralization(Object memoryLimit) throws ValidationException {
    factory.doParse(String.format(RECIPE_WITHOUT_COMMAND_VALUE, memoryLimit));
  }

  @DataProvider(name = "validValues")
  public Object[][] validValues() {
    return new Object[][] {
      {"2048", 2048L},
      {"1gb", 1073741824L},
      {"2g", 2147483648L},
      {"1073741824", 1073741824L},
      {"8m", 8388608L},
      {"200mb", 209715200L},
      {"5k", 5120L},
      {"10kb", 10240L},
    };
  }

  @DataProvider(name = "invalidValues")
  public Object[][] invalidValues() {
    return new Object[][] {{"1m1"}, {"notanumber"}, {1024.5}};
  }
}
