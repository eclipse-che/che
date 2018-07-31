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
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.fasterxml.jackson.dataformat.yaml.snakeyaml.reader.ReaderException;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.deserializer.CommandDeserializer;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeRecipe;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test deserialization field {@link ComposeService#command} by {@link CommandDeserializer} in the
 * {@link ComposeEnvironmentFactory}.
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class CommandDeserializerTest {

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

  private static final String RECIPE_WITHOUT_COMMAND_VALUE =
      "services:\n"
          + " machine1:\n"
          + "  image: codenvy/mysql\n"
          + "  environment:\n"
          + "   MYSQL_USER: petclinic\n"
          + "   MYSQL_PASSWORD: password\n"
          + "  mem_limit: 2147483648\n"
          + "  command: %s\n"
          + // <- test target
          "  expose: [4403, 5502]";

  @Test(dataProvider = "validCommand")
  public void composeServiceCommandShouldBeParsedSuccessfully(
      String command, List<String> commandWords, int commandNumberOfWords) throws Exception {
    String content = format(RECIPE_WITHOUT_COMMAND_VALUE, command);
    ComposeRecipe composeRecipe = factory.doParse(content);

    assertEquals(composeRecipe.getServices().size(), 1);
    ComposeService service = composeRecipe.getServices().get("machine1");
    assertEquals(service.getImage(), "codenvy/mysql");
    assertEquals(service.getMemLimit().longValue(), 2147483648L);
    Map<String, String> environment = service.getEnvironment();
    assertEquals(environment.size(), 2);
    assertEquals(environment.get("MYSQL_USER"), "petclinic");
    assertEquals(environment.get("MYSQL_PASSWORD"), "password");
    assertTrue(service.getExpose().containsAll(asList("4403/tcp", "5502/tcp")));

    assertEquals(service.getCommand(), commandWords);
  }

  @DataProvider(name = "validCommand")
  private Object[][] validCommand() {
    return new Object[][] {
      // allow command in one line
      {"service mysql start", asList("service", "mysql", "start"), 3},
      {
        "service mysql start && tail -f /dev/null",
        asList("service", "mysql", "start", "&&", "tail", "-f", "/dev/null"),
        7
      },
      {"service mysql              start", asList("service", "mysql", "start"), 3},
      {"service mysql start         ", asList("service", "mysql", "start"), 3},
      {"service mysql start         ", asList("service", "mysql", "start"), 3},

      // allow break line feature
      {"| \n" + "   service mysql\n" + "   restart", asList("service", "mysql", "restart"), 3},
      {"| \r" + "   service mysql\r" + "   restart", asList("service", "mysql", "restart"), 3},
      {"| \r\n" + "   service mysql\r\n" + "   restart", asList("service", "mysql", "restart"), 3},
      {"| \n\n" + "   service mysql\n\n" + "   restart", asList("service", "mysql", "restart"), 3},
      {
        "| \n \n" + "   service mysql\n \n" + "   restart", asList("service", "mysql", "restart"), 3
      },
      {
        "> \n \n" + "   service mysql\n \n" + "   restart", asList("service", "mysql", "restart"), 3
      },
      {"> \n \n" + "   ls -a\n \n" + "   -i -p", asList("ls", "-a", "-i", "-p"), 4},

      // allow list command words
      // first form
      {"[service, mysql, start]", asList("service", "mysql", "start"), 3},
      {
        "[service, mysql, start, '&&', tail, -f, /dev/null]",
        asList("service", "mysql", "start", "&&", "tail", "-f", "/dev/null"),
        7
      },
      // second form
      {"\n" + "   - tail\n" + "   - -f\n" + "   - /dev/null", asList("tail", "-f", "/dev/null"), 3},
      {
        "\n"
            + "   - service\n"
            + "   - mysql\n"
            + "   - start\n"
            + "   - '&&'\n"
            + "   - tail\n"
            + "   - -f\n"
            + "   - /dev/null\n",
        asList("service", "mysql", "start", "&&", "tail", "-f", "/dev/null"),
        7
      },

      // Some special symbol should be accessible in case line was wrapped by quotes
      {"\"echo ${PWD}\"", asList("echo", "${PWD}"), 2},
      {"\"(Test)\"", singletonList("(Test)"), 1},
      {"", null, 1},
    };
  }

  @Test(expectedExceptions = ValidationException.class, dataProvider = "inValidCommand")
  public void composeServiceCommandShouldBeParsedFailed(String command) throws Exception {
    String content = format(RECIPE_WITHOUT_COMMAND_VALUE, command);

    try {
      factory.doParse(content);
    } catch (Exception e) {
      System.out.println(e.getLocalizedMessage());
      throw e;
    }
  }

  @DataProvider(name = "inValidCommand")
  private Object[][] inValidCommand() {
    return new Object[][] {
      {"\n |" + "  - tail\n" + "   - -f\n" + "   - /dev/null"},
      {"\n |" + "   - tail\n" + "    -f\n" + "   - /dev/null"},
      {"\n >" + "   - tail\n" + "    -f\n" + "   - /dev/null"},
      {"{service mysql start}"},
      {"[service, mysql, {start : test}]"},
      {"[service, mysql, [start : test]]"},
      {"service mysql \nstart"},
      {"test : value"},
      {"[service mysql start"},
    };
  }

  @Test(dataProvider = "inValidSymbols")
  public void symbolsShouldBeInvalidForYaml(InvalidSymbolCommand command) throws Exception {
    String content = format(RECIPE_WITHOUT_COMMAND_VALUE, command.getCommand());
    try {
      factory.doParse(content);
      // it should fail.
      fail("The command " + command.getCommand() + " has invalid symbol and it should fail");
    } catch (ReaderException e) {
      // we're checking the exception there without throwing it, else it will print to
      // testng-results.xml file an invalid symbol, thus the xml will be invalid.
      assertEquals(e.getMessage(), "special characters are not allowed");
    }
  }

  /**
   * Valid yaml Regex by specification Yaml 1.2 (for two bytes - UTF-16):
   * (\\x09|\\x0A|\\x0D|[\\x20-\\x7E]|\\x85|[\\xA0-\\xD7FF]|[\\xE000-\\xFFFD])+
   */
  @DataProvider(name = "inValidSymbols")
  private Object[][] inValidSymbols() {
    return new Object[][] {
      {new InvalidSymbolCommand("service mysql start\uFFFE")},
      {new InvalidSymbolCommand("service mysql start\uDFFF")},
      {new InvalidSymbolCommand("service mysql start\uD800")},
      {new InvalidSymbolCommand("service mysql start\u009F")},
      {new InvalidSymbolCommand("service mysql start\u0086")},
      {new InvalidSymbolCommand("service mysql start\u0084")},
      {new InvalidSymbolCommand("service mysql start\u0084")},
      {new InvalidSymbolCommand("service mysql start\u007F")},
      {new InvalidSymbolCommand("service mysql start\u001F")},
      {new InvalidSymbolCommand("service mysql start\u000E")},
      {new InvalidSymbolCommand("service mysql start\u000C")},
      {new InvalidSymbolCommand("service mysql start\u000B")},
      {new InvalidSymbolCommand("service mysql start\u0008")},
    };
  }

  /**
   * Use of a custom class for the command, so the DataProvider is not printing the string
   * containing invalid characters in the testng-results.xml file
   */
  private class InvalidSymbolCommand {

    private final String command;

    InvalidSymbolCommand(String command) {
      this.command = command;
    }

    String getCommand() {
      return this.command;
    }
  }
}
