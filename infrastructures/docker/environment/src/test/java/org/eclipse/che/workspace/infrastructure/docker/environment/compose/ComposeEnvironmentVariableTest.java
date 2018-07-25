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

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.deserializer.EnvironmentDeserializer;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test deserialization field {@link ComposeService#environment} by {@link EnvironmentDeserializer}
 * in the {@link ComposeEnvironmentFactory}.
 *
 * @author Dmytro Nochevnov
 */
@Listeners(MockitoTestNGListener.class)
public class ComposeEnvironmentVariableTest {

  @Mock InstallerRegistry installerRegistry;
  @Mock RecipeRetriever recipeRetriever;
  @Mock MachineConfigsValidator machinesValidator;
  @Mock ComposeEnvironmentValidator composeValidator;
  @Mock ComposeServicesStartStrategy startStrategy;

  private ComposeEnvironmentFactory factory;

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

  @Test(dataProvider = "correctContentTestData")
  public void testCorrectContentParsing(String content, Map<String, String> expected)
      throws Exception {
    ComposeRecipe composeRecipe = factory.doParse(content);

    // then
    assertEquals(composeRecipe.getServices().get("dev-machine").getEnvironment(), expected);
  }

  @DataProvider
  public Object[][] correctContentTestData() {
    return new Object[][] {
      // dictionary type environment
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "    RACK_ENV: development\n"
            + "    SHOW: 'true'",
        ImmutableMap.of(
            "RACK_ENV", "development",
            "SHOW", "true")
      },

      // dictionary format, value of variable is empty
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   MYSQL_ROOT_PASSWORD: \"\"",
        ImmutableMap.of("MYSQL_ROOT_PASSWORD", "")
      },

      // dictionary format, value of variable contains colon sign
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   VAR : val:1",
        ImmutableMap.of("VAR", "val:1")
      },

      // array type environment
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   - MYSQL_ROOT_PASSWORD=root\n"
            + "   - MYSQL_DATABASE=db",
        ImmutableMap.of(
            "MYSQL_ROOT_PASSWORD", "root",
            "MYSQL_DATABASE", "db")
      },

      // array format, value of variable contains equal sign
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   - VAR=val=1",
        ImmutableMap.of("VAR", "val=1")
      },

      // array format, empty value of variable
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   - VAR= ",
        ImmutableMap.of("VAR", "")
      },

      // empty environment
      {
        "services: \n" + " dev-machine: \n" + "  image: codenvy/ubuntu_jdk8\n" + "  environment:",
        ImmutableMap.of()
      },
    };
  }

  @Test(dataProvider = "incorrectContentTestData")
  public void shouldThrowError(String content, String errorPattern) throws Exception {
    try {
      factory.doParse(content);
    } catch (ValidationException e) {
      assertTrue(
          e.getMessage().matches(errorPattern),
          format(
              "Actual error message \"%s\" doesn't match regex \"%s\" for content \"%s\"",
              e.getMessage(), errorPattern, content));
      return;
    }

    fail(format("Content \"%s\" should throw IllegalArgumentException", content));
  }

  @DataProvider
  public Object[][] incorrectContentTestData() {
    return new Object[][] {
      // unsupported type of environment
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   true",
        "Parsing of environment configuration failed. Unsupported type 'class java.lang.Boolean'\\.(?s).*"
      },

      // unsupported format of list environment
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   - MYSQL_ROOT_PASSWORD: root\n",
        "Parsing of environment configuration failed. Unsupported value '\\[\\{MYSQL_ROOT_PASSWORD=root}]'\\.(?s).*"
      },

      // dictionary format, no colon in entry
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   MYSQL_ROOT_PASSWORD",
        "Parsing of environment configuration failed. Unsupported value 'MYSQL_ROOT_PASSWORD'\\.(?s).*"
      },

      // dictionary format, value of variable contains equal sign
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   VAR=val=1",
        "Parsing of environment configuration failed. Unsupported value 'VAR=val=1'\\.(?s).*"
      },

      // array format, no equal sign in entry
      {
        "services: \n"
            + " dev-machine: \n"
            + "  image: codenvy/ubuntu_jdk8\n"
            + "  environment:\n"
            + "   - MYSQL_ROOT_PASSWORD=root\n"
            + "   - MYSQL_DATABASE\n",
        "Parsing of environment configuration failed. Unsupported value 'MYSQL_DATABASE'\\.(?s).*"
      },
    };
  }
}
