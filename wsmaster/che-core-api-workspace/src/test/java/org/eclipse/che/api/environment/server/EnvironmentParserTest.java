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

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConf2Impl;
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
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.environment.server.EnvironmentParser.SERVER_CONF_LABEL_PATH_SUFFIX;
import static org.eclipse.che.api.environment.server.EnvironmentParser.SERVER_CONF_LABEL_PREFIX;
import static org.eclipse.che.api.environment.server.EnvironmentParser.SERVER_CONF_LABEL_PROTOCOL_SUFFIX;
import static org.eclipse.che.api.environment.server.EnvironmentParser.SERVER_CONF_LABEL_REF_SUFFIX;
import static org.mockito.Matchers.any;
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

    private static final String TEXT                 = "to be or not to be";
    private static final String DEFAULT_MACHINE_NAME = "dev-machine";
    private static final String DEFAULT_DOCKERFILE   = "FROM codenvy/ubuntu_jdk8\n";
    private static final String DEFAULT_DOCKER_IMAGE = "codenvy/ubuntu_jdk8";

    @Mock
    private EnvironmentImpl                            environment;
    @Mock
    private EnvironmentRecipeImpl                      recipe;
    @Mock
    private ExtendedMachineImpl                        machine;
    @Mock
    private TypeSpecificEnvironmentParser              envParser;
    @Mock
    private Map<String, TypeSpecificEnvironmentParser> parsers;
    @Mock
    private CheServicesEnvironmentImpl                 cheEnv;
    @Mock
    private CheServiceImpl                             cheService1;
    @Mock
    private CheServiceImpl                             cheService2;
    @Mock
    private ExtendedMachineImpl                        extendedMachine1;
    @Mock
    private ExtendedMachineImpl                        extendedMachine2;

    private EnvironmentParser parser;

    @BeforeMethod
    public void setUp() throws ServerException {
        when(environment.getRecipe()).thenReturn(recipe);
        when(recipe.getType()).thenReturn("compose");
        when(recipe.getContent()).thenReturn(TEXT);
        when(envParser.parse(environment)).thenReturn(cheEnv);

        parser = new EnvironmentParser(ImmutableMap.of("dockerfile", envParser,
                                                       "dockerimage", envParser,
                                                       "compose", envParser));
    }

    @Test
    public void shouldReturnEnvTypesCoveredByTests() throws Exception {
        // when
        Set<String> environmentTypes = parser.getEnvironmentTypes();

        // then
        assertEqualsNoOrder(environmentTypes.toArray(), new String[]{"dockerfile", "dockerimage", "compose"});
        assertEquals(environmentTypes.size(), 3);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment type '.*' is not supported. " +
                                            "Supported environment types: .*")
    public void shouldThrowExceptionOnParsingUnknownEnvironmentType() throws Exception {
        parser.parse(new EnvironmentImpl(new EnvironmentRecipeImpl("unknownType",
                                                                   "text/x-dockerfile",
                                                                   "content", null),
                                         null));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment should not be null")
    public void environmentShouldNotBeNull() throws ServerException {
        parser.parse(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment recipe should not be null")
    public void environmentRecipeShouldNotBeNull() throws ServerException {
        when(environment.getRecipe()).thenReturn(null);

        parser.parse(environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment recipe type should not be null")
    public void recipeTypeShouldNotBeNull() throws ServerException {
        when(recipe.getType()).thenReturn(null);

        parser.parse(environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Recipe of environment must contain location or content")
    public void recipeShouldContainsContentOrLocationNotBeNull() throws ServerException {
        when(recipe.getContent()).thenReturn(null);

        parser.parse(environment);
    }

    @Test
    public void shouldBeAbleToParseEnvironment() throws Exception {
        // given
        when(envParser.parse(environment)).thenReturn(cheEnv);

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, cheEnv);
        verify(envParser).parse(environment);
    }

    @Test
    public void shouldOverrideMemoryLimitFromExtendedMachineInComposeEnv() throws Exception {
        // given
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
        CheServicesEnvironmentImpl cheEnv = new CheServicesEnvironmentImpl();

        cheEnv.getServices().put("machine1", new CheServiceImpl());
        cheEnv.getServices().put("machine2", new CheServiceImpl());
        when(envParser.parse(environment)).thenReturn(cheEnv);

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, cheEnv);
        assertEquals(cheServicesEnvironment.getServices().get("machine1").getMemLimit().longValue(), 101010L);
        assertEquals(cheServicesEnvironment.getServices().get("machine2").getMemLimit(), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Value of attribute 'memoryLimitBytes' of machine 'machine1' is illegal")
    public void shouldThrowExceptionInCaseFailedParseMemoryLimit() throws ServerException {
        HashMap<String, ExtendedMachineImpl> machines = new HashMap<>();
        machines.put("machine1", new ExtendedMachineImpl(emptyList(),
                                                         emptyMap(),
                                                         singletonMap("memoryLimitBytes", "here should be memory size number")));
        EnvironmentImpl environment = new EnvironmentImpl(new EnvironmentRecipeImpl("compose",
                                                                                    "application/x-yaml",
                                                                                    "content",
                                                                                    null),
                                                          machines);

        CheServicesEnvironmentImpl cheEnv = new CheServicesEnvironmentImpl();
        cheEnv.getServices().put("machine1", new CheServiceImpl());
        when(envParser.parse(environment)).thenReturn(cheEnv);

        // when
        parser.parse(environment);
    }

    @Test(dataProvider = "environmentWithServersProvider")
    public void shouldAddPortsAndLabelsFromExtendedMachineServers(EnvironmentImpl environment,
                                                                  CheServicesEnvironmentImpl expectedEnv,
                                                                  CheServicesEnvironmentImpl parsedCheEnv)
            throws Exception {
        when(envParser.parse(any(Environment.class))).thenReturn(parsedCheEnv);

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

        CheServicesEnvironmentImpl parsedCheEnv = new CheServicesEnvironmentImpl();
        CheServiceBuildContextImpl buildContext = new CheServiceBuildContextImpl().withDockerfileContent(DEFAULT_DOCKERFILE);
        parsedCheEnv.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withBuild(buildContext));

        return asList(environmentConfig, createExpectedEnvFromDockerfile(expectedExpose, expectedLabels), parsedCheEnv);
    }

    private static List<Object> getEntryForDockerimageEnv(Map<String, ServerConf2Impl> servers,
                                                          List<String> expectedExpose,
                                                          Map<String, String> expectedLabels) {
        EnvironmentImpl environmentConfig = createDockerimageEnvConfig();

        ExtendedMachineImpl extendedMachine = getMachine(environmentConfig);
        extendedMachine.setServers(servers);

        CheServicesEnvironmentImpl parsedCheEnv = new CheServicesEnvironmentImpl();
        parsedCheEnv.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withImage(DEFAULT_DOCKER_IMAGE));

        return asList(environmentConfig, createExpectedEnvFromImage(expectedExpose, expectedLabels), parsedCheEnv);
    }

    private static List<Object> getEntryForComposeEnv(Map<String, ServerConf2Impl> servers,
                                                      List<String> composeExpose,
                                                      Map<String, String> composeLabels,
                                                      List<String> expectedExpose,
                                                      Map<String, String> expectedLabels) {
        CheServicesEnvironmentImpl cheComposeEnv = createCheServicesEnv(new HashMap<>(composeLabels), new ArrayList<>(composeExpose));
        CheServicesEnvironmentImpl expectedEnv = createCheServicesEnv(expectedLabels, expectedExpose);

        EnvironmentImpl environmentConfig = createCompose1MachineEnvConfig();
        ExtendedMachineImpl extendedMachine = getMachine(environmentConfig);
        extendedMachine.setServers(new HashMap<>(servers));
        return asList(environmentConfig,
                      expectedEnv,
                      cheComposeEnv);
    }

    private static ExtendedMachineImpl getMachine(EnvironmentImpl environmentConfig) {
        return environmentConfig.getMachines().values().iterator().next();
    }

    private static CheServicesEnvironmentImpl createCheServicesEnv(Map<String, String> labels, List<String> expose) {
        CheServiceImpl cheService = new CheServiceImpl().withLabels(labels).withExpose(expose).withImage(DEFAULT_DOCKER_IMAGE);
        Map<String, CheServiceImpl> cheComposeEnvs = new HashMap<>();
        cheComposeEnvs.put(DEFAULT_MACHINE_NAME, cheService);

        return new CheServicesEnvironmentImpl().withServices(cheComposeEnvs);
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
                dockerfile != null ? new CheServiceBuildContextImpl(null, null, dockerfile, null) : null;
        CheServicesEnvironmentImpl environment = new CheServicesEnvironmentImpl();
        environment.getServices()
                   .put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withImage(image)
                                                                  .withLabels(expectedLabels)
                                                                  .withBuild(build)
                                                                  .withExpose(expectedExpose));
        return environment;
    }
}
