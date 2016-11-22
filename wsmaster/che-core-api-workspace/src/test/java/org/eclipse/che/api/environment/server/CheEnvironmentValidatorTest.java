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
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
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
import static java.util.Collections.singleton;
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
    EnvironmentParser            environmentParser;
    @Mock
    DefaultServicesStartStrategy startStrategy;

    @InjectMocks
    CheEnvironmentValidator environmentValidator;

    EnvironmentDto             environment;
    CheServicesEnvironmentImpl cheServicesEnv;

    @BeforeMethod
    public void prepare() throws Exception {
        environment = spy(createEnv());
        cheServicesEnv = spy(createServicesEnv());
        when(machineInstanceProviders.hasProvider("docker")).thenReturn(true);
        when(machineInstanceProviders.getProviderTypes()).thenReturn(asList("docker", "ssh"));
        when(environmentParser.parse(any(Environment.class))).thenReturn(cheServicesEnv);
        when(environmentParser.getEnvironmentTypes()).thenReturn(singleton("compose"));
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
    public void shouldFailIfRecipeIsBroken() throws Exception {
        // given
        when(environmentParser.parse(any(Environment.class)))
                .thenThrow(new IllegalArgumentException("test exception"));

        // when
        environmentValidator.validate("env", environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Type 'compose' of environment 'env' is not supported. Supported types: otherType")
    public void shouldFailIfEnvTypeIsNotSupported() throws Exception {
        // given
        when(environmentParser.parse(any(Environment.class)))
                .thenThrow(new IllegalArgumentException("test exception"));
        when(environmentParser.getEnvironmentTypes()).thenReturn(singleton("otherType"));

        // when
        environmentValidator.validate("env", environment);
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "Parsing of recipe of environment '.*' failed. Error: test exception")
    public void shouldFailIfEnvironmentRecipeFetchingFails() throws Exception {
        // given
        when(environmentParser.parse(any(Environment.class)))
                .thenThrow(new ServerException("test exception"));

        // when
        environmentValidator.validate("env", environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Start order of machine in environment 'env' is not resolvable. Error: test exception")
    public void shouldFailIfServicesOrderingFails() throws Exception {
        when(startStrategy.order(any(CheServicesEnvironmentImpl.class)))
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
        Map.Entry<String, ExtendedMachineDto> machineEntry;
        List<List<Object>> data = new ArrayList<>();

        data.add(asList(createEnv().withRecipe(null), "Environment recipe should not be null"));

        env = createEnv();
        env.getRecipe().setType("docker");
        data.add(asList(env, "Type 'docker' of environment 'env' is not supported. Supported types: compose"));

        env = createEnv();
        env.getRecipe().withLocation(null).withContent(null);
        data.add(asList(env, "Recipe of environment 'env' must contain location or content"));

        env = createEnv();
        env.getRecipe().withLocation("location").withContent("content");
        data.add(asList(env, "Recipe of environment 'env' contains mutually exclusive fields location and content"));

        env = createEnv();
        env.setMachines(null);
        data.add(asList(env, "Environment 'env' doesn't contain machine with 'org.eclipse.che.ws-agent' agent"));

        env = createEnv();
        env.setMachines(emptyMap());
        data.add(asList(env, "Environment 'env' doesn't contain machine with 'org.eclipse.che.ws-agent' agent"));

        env = createEnv();
        env.getMachines().put("missingInEnvMachine",
                              newDto(ExtendedMachineDto.class).withAgents(singletonList("org.eclipse.che.ws-agent")));
        data.add(asList(env, "Environment 'env' contains machines that are missing in environment recipe: missingInEnvMachine"));

        env = createEnv();
        env.getMachines().entrySet().forEach(entry -> entry.getValue().getAgents().add("org.eclipse.che.ws-agent"));
        data.add(asList(env, "Environment 'env' should contain exactly 1 machine with agent 'org.eclipse.che.ws-agent', but contains '" +
                             env.getMachines().size() + "'. " + "All machines with this agent: " +
                             Joiner.on(", ").join(env.getMachines().keySet())));

        env = createEnv();
        env.getMachines().entrySet().forEach(entry -> entry.getValue().setAgents(null));
        data.add(asList(env,
                        "Environment 'env' should contain exactly 1 machine with agent 'org.eclipse.che.ws-agent', but contains '0'. All machines with this agent: "));

        env = createEnv();
        env.getMachines().entrySet().forEach(entry -> entry.getValue().getAgents().add(null));
        data.add(asList(env, "Machine 'machine2' in environment 'env' contains invalid agent 'null'"));

        env = createEnv();
        env.getMachines().entrySet().forEach(entry -> entry.getValue().getAgents().add(""));
        data.add(asList(env, "Machine 'machine2' in environment 'env' contains invalid agent ''"));

        env = createEnv();
        machineEntry = env.getMachines().entrySet().iterator().next();
        machineEntry.getValue().setAttributes(singletonMap("memoryLimitBytes", "0"));
        data.add(asList(env, format("Value of attribute 'memoryLimitBytes' of machine '%s' in environment 'env' is illegal",
                                    machineEntry.getKey())));

        env = createEnv();
        machineEntry = env.getMachines().entrySet().iterator().next();
        machineEntry.getValue().setAttributes(singletonMap("memoryLimitBytes", "-1"));
        data.add(asList(env, format("Value of attribute 'memoryLimitBytes' of machine '%s' in environment 'env' is illegal",
                                    machineEntry.getKey())));

        env = createEnv();
        machineEntry = env.getMachines().entrySet().iterator().next();
        machineEntry.getValue().setAttributes(singletonMap("memoryLimitBytes", ""));
        data.add(asList(env, format("Value of attribute 'memoryLimitBytes' of machine '%s' in environment 'env' is illegal",
                                    machineEntry.getKey())));

        return data.stream()
                   .map(list -> list.toArray(new Object[list.size()]))
                   .toArray(value -> new Object[data.size()][]);
    }

    @Test
    public void shouldNotFailIfExtraMachineDoesNotHaveExtendedMachineEntry() throws Exception {
        // given
        CheServicesEnvironmentImpl cheServicesEnv = createServicesEnv();
        cheServicesEnv.getServices().put("extra", createCheService("_extra", 1000000L, null, null, null));
        when(environmentParser.parse(any(Environment.class))).thenReturn(cheServicesEnv);

        // when
        environmentValidator.validate("env", environment);
    }

    @Test(dataProvider = "invalidServicesEnvironmentProvider")
    public void shouldFailValidationIfCheServicesEnvironmentIsBroken(CheServicesEnvironmentImpl cheServicesEnv,
                                                                     String expectedExceptionMessage)
            throws Exception {

        // given
        when(environmentParser.parse(any(Environment.class))).thenReturn(cheServicesEnv);

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
    public static Object[][] invalidServicesEnvironmentProvider() {
        // Format of result array:
        // [ [InvalidCheServicesEnvironmentObject, ExceptionMessage], ... ]
        CheServicesEnvironmentImpl env;
        Map.Entry<String, CheServiceImpl> serviceEntry;
        CheServiceImpl service;
        List<List<Object>> data = new ArrayList<>();

        env = createServicesEnv();
        env.setServices(null);
        data.add(asList(env, "Environment 'env' should contain at least 1 machine"));

        env = createServicesEnv();
        env.setServices(emptyMap());
        data.add(asList(env, "Environment 'env' should contain at least 1 machine"));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        env.getServices().put("invalid service name", serviceEntry.getValue());
        data.add(asList(env, "Name of machine 'invalid service name' in environment 'env' is invalid"));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setImage(null);
        service.setBuild(null);
        data.add(asList(env, format("Field 'image' or 'build.context' is required in machine '%s' in environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setImage("");
        service.setBuild(null);
        data.add(asList(env, format("Field 'image' or 'build.context' is required in machine '%s' in environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setImage(null);
        service.setBuild(new CheServiceBuildContextImpl());
        data.add(asList(env, format("Field 'image' or 'build.context' is required in machine '%s' in environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setImage("");
        service.setBuild(new CheServiceBuildContextImpl());
        data.add(asList(env, format("Field 'image' or 'build.context' is required in machine '%s' in environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setPorts(new ArrayList<>(singletonList("8080:8080")));
        data.add(asList(env, format("Ports binding is forbidden but found in machine '%s' of environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setVolumes(new ArrayList<>(singletonList("volume")));
        data.add(asList(env, format("Volumes binding is forbidden but found in machine '%s' of environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setNetworks(new ArrayList<>(singletonList("network1")));
        data.add(asList(env, format("Networks configuration is forbidden but found in machine '%s' of environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setImage(null);
        service.setBuild(new CheServiceBuildContextImpl(null, "dockerfile", null, null));
        data.add(asList(env, format("Field 'image' or 'build.context' is required in machine '%s' in environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setImage("");
        service.setBuild(new CheServiceBuildContextImpl("", "dockerfile", null, null));
        data.add(asList(env, format("Field 'image' or 'build.context' is required in machine '%s' in environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setImage("");
        service.setBuild(new CheServiceBuildContextImpl(null, null, null, null));
        data.add(asList(env, format("Field 'image' or 'build.context' is required in machine '%s' in environment 'env'", serviceEntry.getKey())));

        env = createServicesEnv();
        serviceEntry = getAnyService(env);
        service = serviceEntry.getValue();
        service.setImage("");
        service.setBuild(new CheServiceBuildContextImpl("some url",
                                                        null,
                                                        "some content",
                                                        new HashMap<String, String>() {{put("argkey","argvalue");}}));
        data.add(asList(env, format("Machine '%s' in environment 'env' contains mutually exclusive dockerfile content and build context.",
                                    serviceEntry.getKey())));

        return data.stream()
                   .map(list -> list.toArray(new Object[list.size()]))
                   .toArray(value -> new Object[data.size()][]);
    }

    private static Map.Entry<String, CheServiceImpl> getAnyService(CheServicesEnvironmentImpl env) {
        return env.getServices()
                  .entrySet()
                  .iterator()
                  .next();
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
                                                singletonMap("prop1", "propValue")));
        servers.put("ref2", new ServerConf2Impl("8080/udp", "proto1", null));
        servers.put("ref3", new ServerConf2Impl("9090", "proto1", null));
        machines.put("dev-machine", new ExtendedMachineImpl(asList("org.eclipse.che.ws-agent", "someAgent"),
                                                            servers,
                                                            singletonMap("memoryLimitBytes", "10000")));
        machines.put("machine2", new ExtendedMachineImpl(asList("someAgent2", "someAgent3"),
                                                         null,
                                                         singletonMap("memoryLimitBytes", "10000")));
        env.setRecipe(new EnvironmentRecipeImpl("compose",
                                                "application/x-yaml",
                                                "content",
                                                null));
        env.setMachines(machines);

        return DtoConverter.asDto(env);
    }

    private static CheServicesEnvironmentImpl createServicesEnv() {
        CheServicesEnvironmentImpl cheServicesEnvironment = new CheServicesEnvironmentImpl();
        Map<String, CheServiceImpl> services = new HashMap<>();
        Map<String, String> buildArgs =  new HashMap<String, String>() {{put("argkey","argvalue");}};
        cheServicesEnvironment.setServices(services);

        services.put("dev-machine", createCheService("_dev",
                                                     1024L * 1024L * 1024L,
                                                     singletonList("machine2"),
                                                     singletonList("machine2"),
                                                     singletonList("machine2")));

        CheServiceImpl service = createCheService("_machine2",
                                                  100L,
                                                  null,
                                                  emptyList(),
                                                  null);
        service.setBuild(new CheServiceBuildContextImpl("context", "file", null, buildArgs));

        services.put("machine2", service);

        return cheServicesEnvironment;
    }

    private static CheServiceImpl createCheService(String suffix,
                                                   long memLimitBytes,
                                                   List<String> links,
                                                   List<String> dependsOn,
                                                   List<String> volumesFrom) {
        CheServiceImpl service = new CheServiceImpl();
        service.setMemLimit(memLimitBytes);
        service.setImage("image_repo/image" + suffix);
        service.setEnvironment(new HashMap<>(singletonMap("env" + suffix, "val" + suffix)));
        service.setCommand(new ArrayList<>(asList("this", "is", "command" + suffix)));
        service.setContainerName("containerName" + suffix);
        service.setEntrypoint(new ArrayList<>(asList("this", "is", "entrypoint" + suffix)));
        service.setExpose(new ArrayList<>(asList("8080", "9090/tcp", "7070/udp")));
        service.setLabels(new HashMap<>(singletonMap("label" + suffix, "value" + suffix)));
        if (links != null) {
            service.setLinks(new ArrayList<>(links));
        }
        if (dependsOn != null) {
            service.setDependsOn(new ArrayList<>(dependsOn));
        }
        if (volumesFrom != null) {
            service.setVolumesFrom(new ArrayList<>(volumesFrom));
        }
//        service.setPorts(new ArrayList<>(singletonList("8080:8080"))); Forbidden
//        service.setVolumes(new ArrayList<>(singletonList("volume"))); Forbidden

        return service;
    }
}
