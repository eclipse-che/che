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

import org.eclipse.che.api.environment.server.compose.BuildContextImpl;
import org.eclipse.che.api.environment.server.compose.ComposeEnvironmentImpl;
import org.eclipse.che.api.environment.server.compose.ComposeFileParser;
import org.eclipse.che.api.environment.server.compose.ComposeServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
import org.eclipse.che.commons.annotation.Nullable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
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
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.api.environment.server.EnvironmentParser.SERVER_CONF_LABEL_PATH_SUFFIX;
import static org.eclipse.che.api.environment.server.EnvironmentParser.SERVER_CONF_LABEL_PREFIX;
import static org.eclipse.che.api.environment.server.EnvironmentParser.SERVER_CONF_LABEL_PROTOCOL_SUFFIX;
import static org.eclipse.che.api.environment.server.EnvironmentParser.SERVER_CONF_LABEL_REF_SUFFIX;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertNotNull;

/**
 * @author Alexander Garagatyi
 */
@Listeners(MockitoTestNGListener.class)
public class EnvironmentParserTest {
    private static String DEFAULT_MACHINE_NAME = "dev-machine";
    private static String DEFAULT_DOCKERFILE   = "FROM codenvy/ubuntu_jdk8\n";
    private static String DEFAULT_DOCKER_IMAGE = "codenvy/ubuntu_jdk8";

    @Mock
    ComposeFileParser composeFileParser;

    @Mock
    RecipeDownloader recipeDownloader;

    @InjectMocks
    EnvironmentParser parser;

    @Test
    public void shouldReturnEnvTypesCoveredByTests() throws Exception {
        // when
        List<String> environmentTypes = parser.getEnvironmentTypes();

        // then
        assertEquals(environmentTypes, asList("compose", "dockerimage", "dockerfile"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment type '.*' is not supported. " +
                                            "Supported environment types: compose, dockerimage, dockerfile")
    public void shouldThrowExceptionOnParsingUnknownEnvironmentType() throws Exception {
        parser.parse(new EnvironmentImpl(new EnvironmentRecipeImpl("unknownType",
                                                                   "text/x-dockerfile",
                                                                   "content", null),
                                         null));
    }

    @Test
    public void shouldBeAbleToParseDockerfileEnvironmentFromContent() throws Exception {
        // given
        EnvironmentImpl environment = createDockerfileEnvConfig(DEFAULT_DOCKERFILE, null, DEFAULT_MACHINE_NAME);

        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withBuild(
                new CheServiceBuildContextImpl().withDockerfileContent(DEFAULT_DOCKERFILE)));

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
    }

    @Test
    public void shouldBeAbleToParseDockerfileEnvironmentFromLocation() throws Exception {
        // given
        String recipeLocation = "http://localhost:8080/recipe/url";
        EnvironmentImpl environment = createDockerfileEnvConfig(null, recipeLocation, DEFAULT_MACHINE_NAME);

        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withBuild(
                new CheServiceBuildContextImpl().withContext(recipeLocation)));

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
    }

    @Test
    public void shouldSetMemoryLimitFromExtendedMachineInDockerfileEnv() throws Exception {
        // given
        String recipeLocation = "http://localhost:8080/recipe/url";
        EnvironmentImpl environment = createDockerfileEnvConfig(null, recipeLocation, DEFAULT_MACHINE_NAME);
        environment.getMachines().get(DEFAULT_MACHINE_NAME).getAttributes().put("memoryLimitBytes", "111111");

        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withBuild(
                new CheServiceBuildContextImpl().withContext(recipeLocation))
                                                                             .withMemLimit(111111L));

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment of type '.*' doesn't support multiple machines, but contains machines: .*")
    public void shouldThrowExceptionOnParseOfDockerfileEnvWithSeveralExtendedMachines() throws Exception {
        EnvironmentImpl environment = createDockerfileEnvConfig();
        environment.getMachines().put("anotherMachine", new ExtendedMachineImpl(emptyList(), emptyMap(), emptyMap()));

        // when
        parser.parse(environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Content type '.*' of recipe of environment is unsupported." +
                                            " Supported values are: text/x-dockerfile")
    public void shouldThrowExceptionOnParseOfDockerfileEnvWithIllegalContentType() throws Exception {
        EnvironmentImpl environment = createDockerfileEnvConfig();
        environment.getRecipe().setContentType("dockerfile");

        // when
        parser.parse(environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment of type '.*' doesn't support multiple machines, but contains machines: .*")
    public void shouldThrowExceptionOnParseOfDockerimageEnvWithSeveralExtendedMachines() throws Exception {
        EnvironmentImpl environment = createDockerimageEnvConfig();
        environment.getMachines().put("anotherMachine", new ExtendedMachineImpl(emptyList(), emptyMap(), emptyMap()));

        // when
        parser.parse(environment);
    }

    @Test
    public void shouldBeAbleToParseDockerimageEnvironment() throws Exception {
        // given
        EnvironmentImpl environment = createDockerimageEnvConfig(DEFAULT_DOCKER_IMAGE, DEFAULT_MACHINE_NAME);

        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withImage(DEFAULT_DOCKER_IMAGE));

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
    }

    @Test
    public void shouldSetMemoryLimitFromExtendedMachineInDockerimageEnv() throws Exception {
        // given
        EnvironmentImpl environment = createDockerimageEnvConfig(DEFAULT_DOCKER_IMAGE, DEFAULT_MACHINE_NAME);
        environment.getMachines().get(DEFAULT_MACHINE_NAME).getAttributes().put("memoryLimitBytes", "111112");

        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withImage(DEFAULT_DOCKER_IMAGE)
                                                                             .withMemLimit(111112L));

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
    }

    @Test
    public void shouldBeAbleToParseComposeEnvironmentFromContent() throws Exception {
        // given
        HashMap<String, ExtendedMachineImpl> machines = new HashMap<>();
        machines.put("machine1", new ExtendedMachineImpl(emptyList(),
                                                         emptyMap(),
                                                         emptyMap()));
        machines.put("machine2", new ExtendedMachineImpl(emptyList(),
                                                         emptyMap(),
                                                         emptyMap()));
        EnvironmentImpl environment = new EnvironmentImpl(new EnvironmentRecipeImpl("compose",
                                                                                    "application/x-yaml",
                                                                                    "content",
                                                                                    null),
                                                          machines);
        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put("machine1", createCheService(false));
        expected.getServices().put("machine2", createCheService(true));
        ComposeEnvironmentImpl composeEnvironment = toCompose(expected);
        when(composeFileParser.parse(eq(environment.getRecipe().getContent()),
                                     eq("application/x-yaml"))).thenReturn(composeEnvironment);

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
        verify(composeFileParser).parse(eq(environment.getRecipe().getContent()), eq("application/x-yaml"));
    }

    @Test
    public void shouldBeAbleToParseComposeEnvironmentFromLocation() throws Exception {
        // given
        HashMap<String, ExtendedMachineImpl> machines = new HashMap<>();
        machines.put("machine1", new ExtendedMachineImpl(emptyList(),
                                                         emptyMap(),
                                                         emptyMap()));
        machines.put("machine2", new ExtendedMachineImpl(emptyList(),
                                                         emptyMap(),
                                                         emptyMap()));
        EnvironmentImpl environment = new EnvironmentImpl(new EnvironmentRecipeImpl("compose",
                                                                                    "application/x-yaml",
                                                                                    null,
                                                                                    "location"),
                                                          machines);
        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put("machine1", createCheService(false));
        expected.getServices().put("machine2", createCheService(true));
        ComposeEnvironmentImpl composeEnvironment = toCompose(expected);
        when(recipeDownloader.getRecipe(eq(environment.getRecipe().getLocation()))).thenReturn("content");
        when(composeFileParser.parse(eq("content"), eq("application/x-yaml"))).thenReturn(composeEnvironment);

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
        verify(composeFileParser).parse(eq("content"), eq("application/x-yaml"));
    }

    @Test
    public void shouldOverrideMemoryLimitFromExtendedMachineInComposeEnv() throws Exception {
        HashMap<String, ExtendedMachineImpl> machines = new HashMap<>();
        machines.put("machine1", new ExtendedMachineImpl(emptyList(),
                                                         emptyMap(),
                                                         singletonMap("memoryLimitBytes", "101010")));
        machines.put("machine2", new ExtendedMachineImpl(emptyList(),
                                                         emptyMap(),
                                                         emptyMap()));
        EnvironmentImpl environment = new EnvironmentImpl(new EnvironmentRecipeImpl("compose",
                                                                                    "application/x-yaml",
                                                                                    "content",
                                                                                    null),
                                                          machines);
        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put("machine1", createCheService(false).withMemLimit(101010L));
        expected.getServices().put("machine2", createCheService(true));
        ComposeEnvironmentImpl composeEnvironment = toCompose(expected);
        when(composeFileParser.parse(eq(environment.getRecipe().getContent()),
                                     eq("application/x-yaml"))).thenReturn(composeEnvironment);

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
    }

    @Test(dataProvider = "environmentWithServersProvider")
    public void shouldAddPortsAndLabelsFromExtendedMachineServers(EnvironmentImpl environment,
                                                                  CheServicesEnvironmentImpl expectedEnv,
                                                                  @Nullable ComposeEnvironmentImpl composeEnvironment)
            throws Exception {
        when(composeFileParser.parse(anyString(), anyString())).thenReturn(composeEnvironment);

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        // prevent failures because of reordered entries of expose field
        assertEquals(cheServicesEnvironment.getServices().size(), expectedEnv.getServices().size());
        cheServicesEnvironment.getServices()
                              .entrySet()
                              .forEach(entry -> {
                                  CheServiceImpl actual = entry.getValue();
                                  CheServiceImpl expected = expectedEnv.getServices().get(entry.getKey());

                                  assertNotNull(expected);
                                  // order of values does not matter
                                  assertEqualsNoOrder(actual.getExpose().toArray(),
                                                      expected.getExpose().toArray(),
                                                      format("Expose fields differ. Actual:%s. Expected:%s",
                                                             actual.getExpose(), expected.getExpose()));
                                  expected.setExpose(null);
                                  actual.setExpose(null);
                              });
        assertEquals(cheServicesEnvironment, expectedEnv);
    }

    @DataProvider(name = "environmentWithServersProvider")
    public static Object[][] environmentWithServersProvider() {
        // Format of result array:
        // [ [environment object, expected che services environment object, @Nullable compose representation of environment], ... ]
        List<List<Object>> data = new ArrayList<>();

        data.add(getEntryForDockerfileEnv(emptyMap(), emptyList(), emptyMap()));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "http", emptyMap())),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080/tcp", "http", emptyMap())),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080/udp", "http", emptyMap())),
                singletonList("8080/udp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", emptyMap())),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "/some/path"))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX,
                                  "/some/path")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path"))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", ""))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", null))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", null, emptyMap())),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", splitOnPairsAsMap("some", "value"))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerfileEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", splitOnPairsAsMap("some", "value",
                                                                                            "path", "some"))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some")));

        data.add(getEntryForDockerfileEnv(
                serversMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path")),
                           "ref2", new ServerConf2Impl("9090", "http", singletonMap("path", "/some/path"))),
                asList("8080/tcp", "9090/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref2",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX,
                                  "/some/path")));

        data.add(getEntryForDockerfileEnv(
                serversMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path")),
                           "ref2", new ServerConf2Impl("8080/udp", "http", singletonMap("path", "/some/path"))),
                asList("8080/tcp", "8080/udp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref2",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PATH_SUFFIX, "/some/path")));

        data.add(getEntryForDockerimageEnv(emptyMap(), emptyList(), emptyMap()));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "http", emptyMap())),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080/tcp", "http", emptyMap())),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080/udp", "http", emptyMap())),
                singletonList("8080/udp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", emptyMap())),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "/some/path"))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX,
                                  "/some/path")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path"))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", ""))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", null))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", null, emptyMap())),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", splitOnPairsAsMap("some", "value"))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForDockerimageEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", splitOnPairsAsMap("some", "value",
                                                                                            "path", "some"))),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some")));

        data.add(getEntryForDockerimageEnv(
                serversMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path")),
                           "ref2", new ServerConf2Impl("9090", "http", singletonMap("path", "/some/path"))),
                asList("8080/tcp", "9090/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref2",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX,
                                  "/some/path")));

        data.add(getEntryForDockerimageEnv(
                serversMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path")),
                           "ref2", new ServerConf2Impl("8080/udp", "http", singletonMap("path", "/some/path"))),
                asList("8080/tcp", "8080/udp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref2",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PATH_SUFFIX, "/some/path")));

        data.add(getEntryForComposeEnv(emptyMap(), emptyList(), emptyMap(), emptyList(), emptyMap()));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "http", emptyMap())),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080/tcp", "http", emptyMap())),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080/udp", "http", emptyMap())),
                emptyList(),
                emptyMap(),
                singletonList("8080/udp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", emptyMap())),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "/some/path"))),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX,
                                  "/some/path")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path"))),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", ""))),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", null))),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", null, emptyMap())),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", splitOnPairsAsMap("some", "value"))),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1")));

        data.add(getEntryForComposeEnv(
                singletonMap("ref1", new ServerConf2Impl("8080", "https", splitOnPairsAsMap("some", "value",
                                                                                            "path", "some"))),
                emptyList(),
                emptyMap(),
                singletonList("8080/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some")));

        data.add(getEntryForComposeEnv(
                serversMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path")),
                           "ref2", new ServerConf2Impl("9090", "http", singletonMap("path", "/some/path"))),
                emptyList(),
                emptyMap(),
                asList("8080/tcp", "9090/tcp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref2",
                                  SERVER_CONF_LABEL_PREFIX + "9090/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX,
                                  "/some/path")));

        data.add(getEntryForComposeEnv(
                serversMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path")),
                           "ref2", new ServerConf2Impl("8080/udp", "http", singletonMap("path", "/some/path"))),
                emptyList(),
                emptyMap(),
                asList("8080/tcp", "8080/udp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref2",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PATH_SUFFIX, "/some/path")));

        data.add(getEntryForComposeEnv(
                serversMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path")),
                           "ref2", new ServerConf2Impl("8080/udp", "http", singletonMap("path", "/some/path"))),
                asList("9090/tcp", "9090/udp", "7070", "7070/udp"),
                emptyMap(),
                asList("8080/tcp", "8080/udp", "9090/udp", "9090/tcp", "7070/tcp", "7070/udp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref2",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PATH_SUFFIX, "/some/path")));

        data.add(getEntryForComposeEnv(
                serversMap("ref1", new ServerConf2Impl("8080", "https", singletonMap("path", "some/path")),
                           "ref2", new ServerConf2Impl("8080/udp", "http", singletonMap("path", "/some/path"))),
                emptyList(),
                splitOnPairsAsMap("label1", "value1",
                                  "label2", "value2"),
                asList("8080/tcp", "8080/udp"),
                splitOnPairsAsMap(SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "https",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref1",
                                  SERVER_CONF_LABEL_PREFIX + "8080/tcp" + SERVER_CONF_LABEL_PATH_SUFFIX, "some/path",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PROTOCOL_SUFFIX, "http",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_REF_SUFFIX, "ref2",
                                  SERVER_CONF_LABEL_PREFIX + "8080/udp" + SERVER_CONF_LABEL_PATH_SUFFIX, "/some/path",
                                  "label1", "value1",
                                  "label2", "value2")));

        return data.stream()
                   .map(list -> list.toArray(new Object[list.size()]))
                   .toArray(value -> new Object[data.size()][]);
    }

    private static Map<String, String> splitOnPairsAsMap(String... args) {
        //noinspection unchecked
        return (Map<String, String>)splitOnPairsAsMap((Object[])args);
    }

    private static Map<String, ServerConf2Impl> serversMap(Object... args) {
        //noinspection unchecked
        return (Map<String, ServerConf2Impl>)splitOnPairsAsMap(args);
    }

    private static Map splitOnPairsAsMap(Object[] args) {
        HashMap<Object, Object> result = new HashMap<>(args.length);

        for (int i = 0; i < args.length; i += 2) {
            Object key = args[i];
            if (result.containsKey(key)) {
                throw new IllegalStateException("Map already contains key " + key);
            }
            result.put(key, args[i + 1]);
        }

        return result;
    }

    private static List<Object> getEntryForDockerfileEnv(Map<String, ServerConf2Impl> servers,
                                                         List<String> expectedExpose,
                                                         Map<String, String> expectedLabels) {
        EnvironmentImpl environmentConfig = createDockerfileEnvConfig();
        ExtendedMachineImpl extendedMachine = getMachine(environmentConfig);
        extendedMachine.setServers(servers);
        return asList(environmentConfig, createExpectedEnvFromDockerfile(expectedExpose, expectedLabels), null);
    }

    private static List<Object> getEntryForDockerimageEnv(Map<String, ServerConf2Impl> servers,
                                                          List<String> expectedExpose,
                                                          Map<String, String> expectedLabels) {
        EnvironmentImpl environmentConfig = createDockerimageEnvConfig();
        ExtendedMachineImpl extendedMachine = getMachine(environmentConfig);
        extendedMachine.setServers(servers);
        return asList(environmentConfig, createExpectedEnvFromImage(expectedExpose, expectedLabels), null);
    }

    private static List<Object> getEntryForComposeEnv(Map<String, ServerConf2Impl> servers,
                                                      List<String> composeExpose,
                                                      Map<String, String> composeLabels,
                                                      List<String> expectedExpose,
                                                      Map<String, String> expectedLabels) {
        ComposeEnvironmentImpl composeEnvironment = create1ServiceComposeEnv();
        ComposeServiceImpl composeService = getService(composeEnvironment);
        composeService.withExpose(new ArrayList<>(composeExpose));
        composeService.withLabels(new HashMap<>(composeLabels));

        EnvironmentImpl environmentConfig = createCompose1MachineEnvConfig();
        ExtendedMachineImpl extendedMachine = getMachine(environmentConfig);
        extendedMachine.setServers(new HashMap<>(servers));
        return asList(environmentConfig,
                      createExpectedEnvFromCompose(composeEnvironment, expectedExpose, expectedLabels),
                      composeEnvironment);
    }

    private static EnvironmentImpl createDockerfileEnvConfig() {
        return createDockerfileEnvConfig(DEFAULT_DOCKERFILE, null, DEFAULT_MACHINE_NAME);
    }

    private static EnvironmentImpl createDockerfileEnvConfig(String recipeContent,
                                                             String recipeLocation,
                                                             String machineName) {
        return new EnvironmentImpl(new EnvironmentRecipeImpl("dockerfile",
                                                             "text/x-dockerfile",
                                                             recipeContent,
                                                             recipeLocation),
                                   singletonMap(machineName,
                                                new ExtendedMachineImpl(emptyList(),
                                                                        emptyMap(),
                                                                        emptyMap())));
    }

    private static EnvironmentImpl createDockerimageEnvConfig() {
        return createDockerimageEnvConfig(DEFAULT_DOCKER_IMAGE, DEFAULT_MACHINE_NAME);
    }

    private static EnvironmentImpl createDockerimageEnvConfig(String image, String machineName) {
        return new EnvironmentImpl(new EnvironmentRecipeImpl("dockerimage",
                                                             null,
                                                             null,
                                                             image),
                                   singletonMap(machineName,
                                                new ExtendedMachineImpl(emptyList(),
                                                                        emptyMap(),
                                                                        emptyMap())));
    }

    private static EnvironmentImpl createCompose1MachineEnvConfig() {
        Map<String, ExtendedMachineImpl> machines = new HashMap<>();
        machines.put(DEFAULT_MACHINE_NAME, new ExtendedMachineImpl(emptyList(),
                                                         emptyMap(),
                                                         emptyMap()));
        return new EnvironmentImpl(new EnvironmentRecipeImpl("compose",
                                                             "application/x-yaml",
                                                             "content",
                                                             null),
                                   machines);
    }

    private static CheServicesEnvironmentImpl createExpectedEnvFromDockerfile(List<String> expectedExpose,
                                                                              Map<String, String> expectedLabels) {
        return createExpectedEnv(null, DEFAULT_DOCKERFILE, expectedExpose, expectedLabels);
    }

    private static CheServicesEnvironmentImpl createExpectedEnvFromImage(List<String> expectedExpose,
                                                                         Map<String, String> expectedLabels) {
        return createExpectedEnv(DEFAULT_DOCKER_IMAGE, null, expectedExpose, expectedLabels);
    }

    private static CheServicesEnvironmentImpl createExpectedEnv(String image,
                                                                String dockerfile,
                                                                List<String> expectedExpose,
                                                                Map<String, String> expectedLabels) {
        CheServiceBuildContextImpl build =
                dockerfile == null ? null : new CheServiceBuildContextImpl(null, null, dockerfile, null);
        CheServicesEnvironmentImpl environment = new CheServicesEnvironmentImpl();
        environment.getServices()
                   .put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withImage(image)
                                                                  .withLabels(expectedLabels)
                                                                  .withBuild(build)
                                                                  .withExpose(expectedExpose));
        return environment;
    }

    private static CheServicesEnvironmentImpl createExpectedEnvFromCompose(ComposeEnvironmentImpl composeEnvironment,
                                                                           List<String> expectedExpose,
                                                                           Map<String, String> expectedLabels) {
        CheServicesEnvironmentImpl cheServicesEnvironment = fromCompose(composeEnvironment);

        CheServiceImpl cheService = cheServicesEnvironment.getServices().values().iterator().next();
        cheService.setExpose(expectedExpose);
        cheService.setLabels(expectedLabels);

        return cheServicesEnvironment;
    }

    private static ComposeEnvironmentImpl create1ServiceComposeEnv() {
        ComposeEnvironmentImpl composeEnvironment = new ComposeEnvironmentImpl();
        ComposeServiceImpl composeService = new ComposeServiceImpl();
        composeService.withImage(DEFAULT_DOCKER_IMAGE);
        composeEnvironment.getServices().put(DEFAULT_MACHINE_NAME, composeService);

        return composeEnvironment;
    }

    private static ComposeServiceImpl getService(ComposeEnvironmentImpl composeEnvironment) {
        return composeEnvironment.getServices().values().iterator().next();
    }

    private static ExtendedMachineImpl getMachine(EnvironmentImpl environmentConfig) {
        return environmentConfig.getMachines().values().iterator().next();
    }

    private static ComposeEnvironmentImpl toCompose(CheServicesEnvironmentImpl environment) {
        ComposeEnvironmentImpl composeEnvironment = new ComposeEnvironmentImpl();
        Map<String, ComposeServiceImpl> services = environment.getServices()
                                                              .entrySet()
                                                              .stream()
                                                              .collect(toMap(Map.Entry::getKey,
                                                                             entry -> toCompose(entry.getValue())));

        return composeEnvironment.withServices(services);
    }

    private static ComposeServiceImpl toCompose(CheServiceImpl service) {
        BuildContextImpl buildContext = null;
        if (service.getBuild() != null) {
            buildContext = new BuildContextImpl().withContext(service.getBuild().getContext())
                                                 .withDockerfile(service.getBuild().getDockerfilePath())
                                                 .withArgs(service.getBuild().getArgs());
        }

        return new ComposeServiceImpl().withBuild(buildContext)
                                       .withCommand(service.getCommand())
                                       .withContainerName(service.getContainerName())
                                       .withDependsOn(service.getDependsOn())
                                       .withEntrypoint(service.getEntrypoint())
                                       .withEnvironment(service.getEnvironment())
                                       .withExpose(service.getExpose())
                                       .withImage(service.getImage())
                                       .withLabels(service.getLabels())
                                       .withLinks(service.getLinks())
                                       .withMemLimit(service.getMemLimit())
                                       .withNetworks(service.getNetworks())
                                       .withPorts(service.getPorts())
                                       .withVolumes(service.getVolumes())
                                       .withVolumesFrom(service.getVolumesFrom());
    }

    private static CheServicesEnvironmentImpl fromCompose(ComposeEnvironmentImpl environment) {
        CheServicesEnvironmentImpl cheServicesEnvironment = new CheServicesEnvironmentImpl();
        Map<String, CheServiceImpl> services = environment.getServices()
                                                          .entrySet()
                                                          .stream()
                                                          .collect(toMap(Map.Entry::getKey,
                                                                         entry -> fromCompose(entry.getValue())));

        return cheServicesEnvironment.withServices(services);
    }

    private static CheServiceImpl fromCompose(ComposeServiceImpl service) {
        CheServiceBuildContextImpl buildContext = null;
        if (service.getBuild() != null) {
            buildContext = new CheServiceBuildContextImpl().withContext(service.getBuild().getContext())
                                                           .withDockerfilePath(service.getBuild().getDockerfile())
                                                           .withArgs(service.getBuild().getArgs());
        }

        return new CheServiceImpl().withBuild(buildContext)
                                   .withCommand(service.getCommand())
                                   .withContainerName(service.getContainerName())
                                   .withDependsOn(service.getDependsOn())
                                   .withEntrypoint(service.getEntrypoint())
                                   .withEnvironment(service.getEnvironment())
                                   .withExpose(service.getExpose())
                                   .withImage(service.getImage())
                                   .withLabels(service.getLabels())
                                   .withLinks(service.getLinks())
                                   .withMemLimit(service.getMemLimit())
                                   .withNetworks(service.getNetworks())
                                   .withPorts(service.getPorts())
                                   .withVolumes(service.getVolumes())
                                   .withVolumesFrom(service.getVolumesFrom());
    }

    private static CheServiceImpl createCheService(boolean isImageBased) {
        return new CheServiceImpl()
                .withBuild(isImageBased ? null : new CheServiceBuildContextImpl("some url",
                                                                                "some path",
                                                                                null,
                                                                                new HashMap<String, String>()
                                                                                {{put("argkey","argvalue");}}))
                .withCommand(asList("some", "command"))
                .withContainerName("con name")
                .withDependsOn(asList("depends1", "depends2"))
                .withEntrypoint(asList("some", "entrypoint"))
                .withEnvironment(singletonMap("envKey", "envValue"))
                .withExpose(asList("8080/udp", "9090/tcp"))
                .withImage(isImageBased ? DEFAULT_DOCKER_IMAGE : null)
                .withLabels(singletonMap("labelKey", "labelValue"))
                .withLinks(asList("link1", "link2"))
                .withMemLimit(123456L)
                .withNetworks(asList("net1", "net2"))
                .withPorts(asList("port1", "port2"))
                .withVolumes(asList("volume1", "volume2"))
                .withVolumesFrom(asList("volumeFrom1", "volumeFrom2"));
    }
}
