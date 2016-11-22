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
package org.eclipse.che.plugin.docker.compose.yaml;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Test for {@link ComposeEnvironmentParser}.
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class ComposeEnvironmentParserTest {

    private static final String TEXT            = "to be or not to be";
    private static final String COMPOSE_CONTENT = "services: \n" +
                                                  "  machine1: \n" +
                                                  "    build: \n" +
                                                  "      context: \"http://host.com:port/location/of/dockerfile/or/git/repo/\"\n" +
                                                  "      dockerfile: dockerfile/Dockerfile_alternate\n" +
                                                  "    command: \n" +
                                                  "      - tail\n" +
                                                  "      - \"-f\"\n" +
                                                  "      - /dev/null\n" +
                                                  "    container_name: some_name\n" +
                                                  "    depends_on: \n" +
                                                  "      - machine2\n" +
                                                  "      - machine3\n" +
                                                  "    entrypoint: \n" +
                                                  "      - /bin/bash\n" +
                                                  "      - \"-c\"\n" +
                                                  "    environment: \n" +
                                                  "      - env1=123\n" +
                                                  "      - env2=345\n" +
                                                  "    expose: \n" +
                                                  "      - \"3000\"\n" +
                                                  "      - \"8080\"\n" +
                                                  "    image: codenvy/ubuntu_jdk8\n" +
                                                  "    labels: \n" +
                                                  "      com.example.department: Finance\n" +
                                                  "      com.example.description: \"Accounting webapp\"\n" +
                                                  "      com.example.label-with-empty-value: \"\"\n" +
                                                  "    links: \n" +
                                                  "      - machine1\n" +
                                                  "      - \"machine2:db\"\n" +
                                                  "    mem_limit: 2147483648\n" +
                                                  "    networks: \n" +
                                                  "      - some-network\n" +
                                                  "      - other-network\n" +
                                                  "    ports: \n" +
                                                  "      - \"3000\"\n" +
                                                  "      - 3000-3005\n" +
                                                  "    volumes: \n" +
                                                  "      - \"/opt/data:/var/lib/mysql\"\n" +
                                                  "      - \"~/configs:/etc/configs/:ro\"\n" +
                                                  "    volumes_from: \n" +
                                                  "      - \"machine2:ro\"\n" +
                                                  "      - machine3\n" +
                                                  "  machine2: \n" +
                                                  "    image: codenvy/ubuntu_jdk8\n" +
                                                  "  machine3: \n" +
                                                  "    image: codenvy/ubuntu_jdk8\n";

    @Mock
    private Environment       environment;
    @Mock
    private EnvironmentRecipe recipe;
    @Mock
    private RecipeDownloader  recipeDownloader;

    @InjectMocks
    private ComposeEnvironmentParser parser;

    @BeforeMethod
    public void setUp() {
        when(environment.getRecipe()).thenReturn(recipe);
    }

    @Test
    public void shouldBeAbleToParseComposeEnvironmentWithApplicationXYamlContentType() throws Exception {
        // given
        when(recipe.getContentType()).thenReturn("application/x-yaml");
        when(recipe.getContent()).thenReturn(COMPOSE_CONTENT);
        CheServicesEnvironmentImpl expectedEnv = createTestEnv();

        //when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        //then
        assertEquals(cheServicesEnvironment, expectedEnv);
    }

    @Test
    public void shouldBeAbleToParseComposeEnvironmentWithTextYamlContentType() throws Exception {
        // given
        when(recipe.getContentType()).thenReturn("text/yaml");
        when(recipe.getContent()).thenReturn(COMPOSE_CONTENT);
        CheServicesEnvironmentImpl expectedEnv = createTestEnv();

        //when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        //then
        assertEquals(cheServicesEnvironment, expectedEnv);
    }

    @Test
    public void shouldBeAbleToParseComposeEnvironmentWithTextXYamlContentType() throws Exception {
        // given
        when(recipe.getContentType()).thenReturn("text/x-yaml");
        when(recipe.getContent()).thenReturn(COMPOSE_CONTENT);
        CheServicesEnvironmentImpl expectedEnv = createTestEnv();

        //when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        //then
        assertEquals(cheServicesEnvironment, expectedEnv);
    }

    @Test
    public void shouldBeAbleToParseComposeEnvironmentWithApplicationByLocation() throws ServerException {
        // given
        when(recipe.getContentType()).thenReturn("text/yaml");
        when(recipeDownloader.getRecipe(TEXT)).thenReturn(COMPOSE_CONTENT);
        when(recipe.getLocation()).thenReturn(TEXT);
        CheServicesEnvironmentImpl expectedEnv = createTestEnv();

        //when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        //then
        assertEquals(cheServicesEnvironment, expectedEnv);
    }

    private CheServicesEnvironmentImpl createTestEnv() {
        CheServicesEnvironmentImpl cheServicesEnvironment = new CheServicesEnvironmentImpl();

        CheServiceImpl cheService1 = new CheServiceImpl();
        String buildContext = "http://host.com:port/location/of/dockerfile/or/git/repo/";
        cheService1.setBuild(new CheServiceBuildContextImpl().withContext(buildContext)
                                                             .withDockerfilePath("dockerfile/Dockerfile_alternate")
                                                             .withArgs(emptyMap()));
        cheService1.setCommand(asList("tail", "-f", "/dev/null"));
        cheService1.setContainerName("some_name");
        cheService1.setDependsOn(asList("machine2", "machine3"));
        cheService1.setEntrypoint(asList("/bin/bash", "-c"));
        cheService1.setEnvironment(ImmutableMap.of("env1", "123",
                                                   "env2", "345"));
        cheService1.setExpose(asList("3000", "8080"));
        cheService1.setImage("codenvy/ubuntu_jdk8");
        cheService1.setLabels(ImmutableMap.of("com.example.department", "Finance",
                                              "com.example.description", "Accounting webapp",
                                              "com.example.label-with-empty-value", ""));
        cheService1.setLinks(asList("machine1", "machine2:db"));
        cheService1.setMemLimit(2147483648L);
        cheService1.setNetworks(asList("some-network", "other-network"));
        cheService1.setPorts(asList("3000", "3000-3005"));
        cheService1.setVolumes(asList("/opt/data:/var/lib/mysql", "~/configs:/etc/configs/:ro"));
        cheService1.setVolumesFrom(asList("machine2:ro", "machine3"));

        CheServiceImpl cheService2 = new CheServiceImpl();
        cheService2.setImage("codenvy/ubuntu_jdk8");
        CheServiceImpl cheService3 = new CheServiceImpl();
        cheService3.setImage("codenvy/ubuntu_jdk8");

        cheServicesEnvironment.setServices(ImmutableMap.of("machine1", cheService1,
                                                           "machine2", cheService2,
                                                           "machine3", cheService3));
        return cheServicesEnvironment;
    }
}
