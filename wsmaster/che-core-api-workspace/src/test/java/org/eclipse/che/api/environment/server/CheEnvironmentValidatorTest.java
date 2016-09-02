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

import com.google.common.base.Joiner;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.environment.server.compose.ComposeFileParser;
import org.eclipse.che.api.environment.server.compose.ComposeServicesStartStrategy;
import org.eclipse.che.api.environment.server.compose.model.BuildContextImpl;
import org.eclipse.che.api.environment.server.compose.model.ComposeEnvironmentImpl;
import org.eclipse.che.api.environment.server.compose.model.ComposeServiceImpl;
import org.eclipse.che.api.machine.server.MachineInstanceProviders;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.ServerConfDto;
import org.eclipse.che.api.workspace.server.DtoConverter;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class CheEnvironmentValidatorTest {
    @Mock
    MachineInstanceProviders     machineInstanceProviders;
    @Mock
    ComposeFileParser            composeFileParser;
    @Mock
    ComposeServicesStartStrategy startStrategy;

    @InjectMocks
    CheEnvironmentValidator  environmentValidator;

    EnvironmentDto         environment;
    ComposeEnvironmentImpl composeEnv;

    @BeforeMethod
    public void prepare() throws Exception {
        environment = spy(createEnv());
        composeEnv = spy(createComposeEnv());
        when(machineInstanceProviders.hasProvider("docker")).thenReturn(true);
        when(machineInstanceProviders.getProviderTypes()).thenReturn(asList("docker", "ssh"));
        when(composeFileParser.parse(any(Environment.class))).thenReturn(composeEnv);
    }

    @Test
    public void shouldSucceedOnValidationOfValidEnvironment() throws Exception {
        environmentValidator.validate("env", environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment name should not be neither null nor empty")
    public void shouldFailValidationIfEnvNameIsNull() throws Exception {
        // when
        environmentValidator.validate(null, environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment name should not be neither null nor empty")
    public void shouldFailValidationIfEnvNameIsEmpty() throws Exception {
        // when
        environmentValidator.validate("", environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Parsing of recipe of environment '.*' failed. Error: test exception")
    public void shouldFailIfComposeFileIsBroken() throws Exception {
        // given
        when(composeFileParser.parse(any(Environment.class)))
                .thenThrow(new IllegalArgumentException("test exception"));

        // when
        environmentValidator.validate("env", environment);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Parsing of recipe of environment '.*' failed. Error: test exception")
    public void shouldFailIfEnvironmentRecipeFetchingFails() throws Exception {
        // given
        when(composeFileParser.parse(any(Environment.class)))
                .thenThrow(new ServerException("test exception"));

        // when
        environmentValidator.validate("env", environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Compose services order of environment 'env' is not resolvable. Error: test exception")
    public void shouldFailIfServicesOrderingFails() throws Exception {
        when(startStrategy.order(any(ComposeEnvironmentImpl.class)))
                .thenThrow(new IllegalArgumentException("test exception"));

        environmentValidator.validate("env", environment);
    }

    @Test(dataProvider = "invalidEnvironmentProvider")
    public void shouldFailValidationIfEnvironmentIsBroken(EnvironmentDto env,
                                                          String expectedExceptionMessage)
            throws Exception {

        try {
            // when
            environmentValidator.validate("env", env);

            // then
            fail(format("Validation had to throw exception with message %s",
                        expectedExceptionMessage));
        } catch (IllegalArgumentException e) {
            // then
            assertEquals(e.getLocalizedMessage(), expectedExceptionMessage);
        }
    }

    @DataProvider
    public static Object[][] invalidEnvironmentProvider() {
        // InvalidEnvironmentObject | ExceptionMessage
        EnvironmentDto env;
        List<List<Object>> data = new ArrayList<>();

        data.add(asList(createEnv().withRecipe(null), "Environment recipe should not be null"));

        env = createEnv();
        env.getRecipe().setType("docker");
        data.add(asList(env, "Type 'docker' of environment 'env' is not supported. Supported types: compose"));

        env = createEnv();
        env.getRecipe().setContentType(null);
        data.add(asList(env, "Environment recipe content type should not be neither null nor empty"));

        env = createEnv();
        env.getRecipe().setContentType("");
        data.add(asList(env, "Environment recipe content type should not be neither null nor empty"));

        env = createEnv();
        env.getRecipe().withLocation(null).withContent(null);
        data.add(asList(env, "Recipe of environment 'env' must contain location or content"));

        env = createEnv();
        env.getRecipe().withLocation("location").withContent("content");
        data.add(asList(env, "Recipe of environment 'env' contains mutually exclusive fields location and content"));

        env = createEnv();
        env.setMachines(null);
        data.add(asList(env, "Environment 'env' doesn't contain machine with 'ws-agent' agent"));

        env = createEnv();
        env.setMachines(emptyMap());
        data.add(asList(env, "Environment 'env' doesn't contain machine with 'ws-agent' agent"));

        env = createEnv();
        env.getMachines().put("missingInComposeEnvMachine",
                              newDto(ExtendedMachineDto.class).withAgents(singletonList("ws-agent")));
        data.add(asList(env, "Environment 'env' contains machines that are missing in environment recipe: missingInComposeEnvMachine"));

        env = createEnv();
        env.getMachines().entrySet().forEach(entry -> entry.getValue().getAgents().add("ws-agent"));
        data.add(asList(env, "Environment 'env' should contain exactly 1 machine with ws-agent, but contains '" +
                             env.getMachines().size() + "'. " + "All machines with this agent: " +
                             Joiner.on(", ").join(env.getMachines().keySet())));

        return data.stream()
                   .map(list -> list.toArray(new Object[list.size()]))
                   .toArray(value -> new Object[data.size()][]);
    }

    @Test(dataProvider = "invalidComposeEnvironmentProvider")
    public void shouldFailValidationIfComposeEnvironmentIsBroken(ComposeEnvironmentImpl composeEnv,
                                                                 String expectedExceptionMessage)
            throws Exception {

        // given
        when(composeFileParser.parse(any(Environment.class))).thenReturn(composeEnv);

        try {
            // when
            environmentValidator.validate("env", environment);

            // then
            fail(format("Validation had to throw exception with message %s",
                        expectedExceptionMessage));
        } catch (IllegalArgumentException e) {
            // then
            assertEquals(e.getLocalizedMessage(), expectedExceptionMessage);
        }
    }

    @DataProvider
    public static Object[][] invalidComposeEnvironmentProvider() {
        // InvalidComposeEnvironmentObject | ExceptionMessage
        ComposeEnvironmentImpl env;
        List<List<Object>> data = new ArrayList<>();

        env = createComposeEnv();
        env.setServices(null);
        data.add(asList(env, "Environment 'env' should contain at least 1 compose service"));

        env = createComposeEnv();
        env.setServices(emptyMap());
        data.add(asList(env, "Environment 'env' should contain at least 1 compose service"));

        return data.stream()
                   .map(list -> list.toArray(new Object[list.size()]))
                   .toArray(value -> new Object[data.size()][]);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine name is null or empty")
    public void shouldFailValidationIfMachineNameIsNull() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.withName(null);


        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine name is null or empty")
    public void shouldFailValidationIfMachineNameIsEmpty() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.withName("");


        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' doesn't have source")
    public void shouldFailValidationIfMachineSourceIsNull() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.withSource(null);


        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Type 'null' of machine '.*' is not supported. Supported values are: docker, ssh.")
    public void shouldFailValidationIfMachineTypeIsNull() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.withType(null);


        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Type 'compose' of machine '.*' is not supported. Supported values are: docker, ssh.")
    public void shouldFailValidationIfMachineTypeIsNotDocker() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.withType("compose");


        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine .* contains server conf with invalid port .*",
          dataProvider = "invalidPortProvider")
    public void shouldFailValidationIfServerConfPortIsInvalid(String invalidPort) throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.getServers()
              .add(newDto(ServerConfDto.class).withPort(invalidPort));


        environmentValidator.validateMachine(config);
    }

    @DataProvider(name = "invalidPortProvider")
    public static Object[][] invalidPortProvider() {
        return new Object[][] {
                {"0"},
                {"0123"},
                {"012/tcp"},
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
          expectedExceptionsMessageRegExp = "Machine '.*' contains environment variable with null or empty name")
    public void shouldFailValidationIfEnvVarNameIsNull() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.getEnvVariables()
              .put(null, "value");


        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' contains environment variable with null or empty name")
    public void shouldFailValidationIfEnvVarNameIsEmpty() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.getEnvVariables()
              .put("", "value");


        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' contains environment variable 'key' with null value")
    public void shouldFailValidationIfEnvVarValueIsNull() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.getEnvVariables()
              .put("key", null);


        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Source of machine '.*' must contain location or content")
    public void shouldFailValidationIfMissingSourceLocationAndContent() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.withSource(newDto(MachineSourceDto.class).withType("dockerfile"));

        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' has invalid source location: 'localhost'")
    public void shouldFailValidationIfLocationIsInvalidUrl() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.withSource(newDto(MachineSourceDto.class).withType("dockerfile").withLocation("localhost"));

        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine '.*' has invalid source location protocol: ftp://localhost")
    public void shouldFailValidationIfLocationHasInvalidProtocol() throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.withSource(newDto(MachineSourceDto.class).withType("dockerfile")
                                                        .withLocation("ftp://localhost"));

        environmentValidator.validateMachine(config);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Machine .* contains server conf with invalid protocol .*",
          dataProvider = "invalidProtocolProvider")
    public void shouldFailValidationIfServerConfProtocolIsInvalid(String invalidProtocol) throws Exception {
        MachineConfigDto config = createMachineConfig();
        config.getServers()
              .add(newDto(ServerConfDto.class).withPort("8080/tcp")
                                              .withProtocol(invalidProtocol));


        environmentValidator.validateMachine(config);
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

    private MachineConfigDto createMachineConfig() {
        List<ServerConfDto> serversConf = new ArrayList<>(asList(newDto(ServerConfDto.class).withRef("ref1")
                                                                                            .withPort("8080/tcp")
                                                                                            .withProtocol("https")
                                                                                            .withPath("some/path"),
                                                                 newDto(ServerConfDto.class).withRef("ref2")
                                                                                            .withPort("9090/udp")
                                                                                            .withProtocol("protocol")
                                                                                            .withPath("/some/path")));
        return newDto(MachineConfigDto.class).withDev(true)
                                             .withName("machine1")
                                             .withType("docker")
                                             .withSource(newDto(MachineSourceDto.class)
                                                                                     .withLocation("http://location")
                                                                                     .withType("dockerfile"))
                                             .withServers(serversConf)
                                             .withEnvVariables(new HashMap<>(singletonMap("key1", "value1")));
    }

    private static EnvironmentDto createEnv() {
        // singletonMap, asList are wrapped into modifiable collections to ease env modifying by tests
        EnvironmentImpl env = new EnvironmentImpl();
        Map<String, ExtendedMachineImpl> machines = new HashMap<>();
        Map<String, ServerConf2Impl> servers = new HashMap<>();

        servers.put("ref1", new ServerConf2Impl("8080/tcp",
                                                "proto1",
                                                new HashMap<>(singletonMap("prop1", "propValue"))));
        servers.put("ref2", new ServerConf2Impl("8080/udp", "proto1", null));
        servers.put("ref3", new ServerConf2Impl("9090", "proto1", null));
        machines.put("dev-machine", new ExtendedMachineImpl(new ArrayList<>(asList("ws-agent", "someAgent")), servers));
        machines.put("machine2", new ExtendedMachineImpl(new ArrayList<>(asList("someAgent2", "someAgent3")), null));
        env.setRecipe(new EnvironmentRecipeImpl("compose",
                                                "application/x-yaml",
                                                "content",
                                                null));
        env.setMachines(machines);

        return DtoConverter.asDto(env);
    }

    private static ComposeEnvironmentImpl createComposeEnv() {
        ComposeEnvironmentImpl composeEnvironment = new ComposeEnvironmentImpl();
        composeEnvironment.setVersion("2");
        Map<String, ComposeServiceImpl> services = new HashMap<>();
        composeEnvironment.setServices(services);

        ComposeServiceImpl service = new ComposeServiceImpl();
        service.setMemLimit(1024L * 1024L * 1024L);
        service.setImage("codenvy/ubuntu_jdk8");
        service.setEnvironment(new HashMap<>(singletonMap("env1", "val1")));
        service.setCommand(new ArrayList<>(asList("this", "is", "command")));
        service.setContainerName("containerName");
        service.setDependsOn(new ArrayList<>(singletonList("machine2")));
        service.setEntrypoint(new ArrayList<>(asList("this", "is", "entrypoint")));
        service.setExpose(new ArrayList<>(asList("8080", "9090/tcp", "7070/udp")));
        service.setLabels(new HashMap<>(singletonMap("label1", "value1")));
        service.setLinks(new ArrayList<>(singletonList("machine2")));
//        service.setPorts(new ArrayList<>(singletonList("8080:8080"))); Forbidden
//        service.setVolumes(new ArrayList<>(singletonList("volume"))); Forbidden
        service.setVolumesFrom(new ArrayList<>(singletonList("machine2")));

        services.put("dev-machine", service);

        service = new ComposeServiceImpl();
        service.setMemLimit(100L);
        service.setBuild(new BuildContextImpl("context", "file"));
        service.setEnvironment(new HashMap<>(singletonMap("env1", "val1")));
        service.setCommand(new ArrayList<>(asList("this", "is", "command")));
        service.setContainerName("containerName2");
        service.setDependsOn(null);
        service.setEntrypoint(new ArrayList<>(asList("this", "is", "entrypoint")));
        service.setExpose(new ArrayList<>(asList("8080", "9090/tcp", "7070/udp")));
        service.setLabels(new HashMap<>(singletonMap("label1", "value1")));
        service.setLinks(new ArrayList<>(emptyList()));
//        service.setPorts(new ArrayList<>(singletonList("8080:8080"))); Forbidden
//        service.setVolumes(new ArrayList<>(singletonList("volume"))); Forbidden
        service.setVolumesFrom(null);
        services.put("machine2", service);

        return composeEnvironment;
    }
}
