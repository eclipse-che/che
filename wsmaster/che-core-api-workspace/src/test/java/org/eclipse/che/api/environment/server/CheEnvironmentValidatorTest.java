/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.environment.server;

import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.ServerConfDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class CheEnvironmentValidatorTest {
    @Mock
    MachineInstanceProviders machineInstanceProviders;
    @InjectMocks
    CheEnvironmentValidator  environmentValidator;

    @BeforeMethod
    public void prepare() throws Exception {
        when(machineInstanceProviders.hasProvider("docker")).thenReturn(true);
        when(machineInstanceProviders.getProviderTypes()).thenReturn(Arrays.asList("docker", "ssh"));
    }


    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment name should not be neither null nor empty")
    public void shouldFailValidationIfEnvNameIsNull() throws Exception {
        EnvironmentDto environment = createConfig();
        environment.setName(null);


        environmentValidator.validate(environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment name should not be neither null nor empty")
    public void shouldFailValidationIfEnvNameIsEmpty() throws Exception {
        EnvironmentDto environment = createConfig();
        environment.setName("");


        environmentValidator.validate(environment);
    }

    @Test
    public void shouldNotFailValidationIfEnvironmentRecipeTypeIsDocker() throws Exception {
        EnvironmentDto config = createConfig();
        config.withRecipe(newDto(RecipeDto.class).withType("docker"));


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' should contain at least 1 machine")
    public void shouldFailValidationIfMachinesListIsEmpty() throws Exception {
        EnvironmentDto config = createConfig();
        config.withMachineConfigs(null);


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' should contain exactly 1 dev machine, but contains '0'")
    public void shouldFailValidationIfNoDevMachineFound() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .stream()
              .filter(MachineConfigDto::isDev)
              .forEach(machine -> machine.withDev(false));


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' should contain exactly 1 dev machine, but contains '2'")
    public void shouldFailValidationIf2DevMachinesFound() throws Exception {
        EnvironmentDto config = createConfig();
        final Optional<MachineConfigDto> devMachine = config.getMachineConfigs()
                                                            .stream()
                                                            .filter(MachineConfigDto::isDev)
                                                            .findAny();
        config.getMachineConfigs()
              .add(devMachine.get().withName("other-name"));


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment .* contains machine with null or empty name")
    public void shouldFailValidationIfMachineNameIsNull() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .withName(null);


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment .* contains machine with null or empty name")
    public void shouldFailValidationIfMachineNameIsEmpty() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .withName("");


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' in environment '.*' doesn't have source")
    public void shouldFailValidationIfMachineSourceIsNull() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .withSource(null);


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Type 'null' of machine '.*' in environment '.*' is not supported. Supported values are: docker, ssh.")
    public void shouldFailValidationIfMachineTypeIsNull() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .withType(null);


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Type 'compose' of machine '.*' in environment '.*' is not supported. Supported values are: docker, ssh.")
    public void shouldFailValidationIfMachineTypeIsNotDocker() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .withType("compose");


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine .* contains server conf with invalid port .*",
          dataProvider = "invalidPortProvider")
    public void shouldFailValidationIfServerConfPortIsInvalid(String invalidPort) throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .getServers()
              .add(newDto(ServerConfDto.class).withPort(invalidPort));


        environmentValidator.validate(config);
    }

    @DataProvider(name = "invalidPortProvider")
    public static Object[][] invalidPortProvider() {
        return new Object[][] {
                {"0"},
                {"0123"},
                {"012/tcp"},
                {"8080"},
                {"8080/pct"},
                {"8080/pdu"},
                {"/tcp"},
                {"tcp"},
                {""},
                {"8080/tcp1"},
                {"8080/tcpp"},
                {"8080tcp"},
                {"8080/tc"},
                {"8080/ud"},
                {"8080/udpp"},
                {"8080/udp/"},
                {"8080/tcp/"},
                {"8080/tcp/udp"},
                {"8080/tcp/tcp"},
                {"8080/tcp/8080"},
                {null}
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine .* contains server conf with invalid protocol .*",
          dataProvider = "invalidProtocolProvider")
    public void shouldFailValidationIfServerConfProtocolIsInvalid(String invalidProtocol) throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .getServers()
              .add(newDto(ServerConfDto.class).withPort("8080/tcp")
                                              .withProtocol(invalidProtocol));


        environmentValidator.validate(config);
    }

    @DataProvider(name = "invalidProtocolProvider")
    public static Object[][] invalidProtocolProvider() {
        return new Object[][] {
                {""},
                {"http!"},
                {"2http"},
                {"http:"},
                };
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' in environment '.*' contains environment variable with null or empty name")
    public void shouldFailValidationIfEnvVarNameIsNull() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .getEnvVariables()
              .put(null, "value");


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' in environment '.*' contains environment variable with null or empty name")
    public void shouldFailValidationIfEnvVarNameIsEmpty() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .getEnvVariables()
              .put("", "value");


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' in environment '.*' contains environment variable '.*' with null value")
    public void shouldFailValidationIfEnvVarValueIsNull() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .getEnvVariables()
              .put("key", null);


        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Source of machine '.*' in environment '.*' must contain location or content")
    public void shouldFailValidationIfMissingSourceLocationAndContent() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .withSource(newDto(MachineSourceDto.class).withType("dockerfile"));

        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' contains machine '.*' with invalid source location: 'localhost'")
    public void shouldFailValidationIfLocationIsInvalidUrl() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .withSource(newDto(MachineSourceDto.class).withType("dockerfile").withLocation("localhost"));

        environmentValidator.validate(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' contains machine '.*' with invalid source location protocol: ftp://localhost")
    public void shouldFailValidationIfLocationHasInvalidProtocol() throws Exception {
        EnvironmentDto config = createConfig();
        config.getMachineConfigs()
              .get(0)
              .withSource(newDto(MachineSourceDto.class).withType("dockerfile").withLocation("ftp://localhost"));

        environmentValidator.validate(config);
    }

    private EnvironmentDto createConfig() {
        final List<ServerConfDto> serversConf =
                new ArrayList<>(Arrays.asList(newDto(ServerConfDto.class).withRef("ref1")
                                                                         .withPort("8080/tcp")
                                                                         .withProtocol("https")
                                                                         .withPath("some/path"),
                                              newDto(ServerConfDto.class).withRef("ref2")
                                                                         .withPort("9090/udp")
                                                                         .withProtocol("protocol")
                                                                         .withPath("/some/path")));
        MachineConfigDto devMachine = newDto(MachineConfigDto.class).withDev(true)
                                                                    .withName("dev-machine")
                                                                    .withType("docker")
                                                                    .withSource(newDto(MachineSourceDto.class)
                                                                                        .withLocation("http://location")
                                                                                        .withType("dockerfile"))
                                                                    .withServers(serversConf)
                                                                    .withEnvVariables(new HashMap<>(
                                                                            singletonMap("key1", "value1")));

        return newDto(EnvironmentDto.class).withName("dev-env")
                                           .withMachineConfigs(
                                                   new ArrayList<>(singletonList(devMachine)))
                                           .withRecipe(null);
    }
}
