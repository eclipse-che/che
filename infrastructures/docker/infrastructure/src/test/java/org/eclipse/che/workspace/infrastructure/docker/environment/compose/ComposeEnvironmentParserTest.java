/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link ComposeEnvironmentParser}.
 *
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class ComposeEnvironmentParserTest {

  private static final String COMPOSE_CONTENT =
      "services: \n"
          + "  machine1: \n"
          + "    build: \n"
          + "      context: \"http://host.com:port/location/of/dockerfile/or/git/repo/\"\n"
          + "      dockerfile: dockerfile/Dockerfile_alternate\n"
          + "    command: \n"
          + "      - tail\n"
          + "      - \"-f\"\n"
          + "      - /dev/null\n"
          + "    container_name: some_name\n"
          + "    depends_on: \n"
          + "      - machine2\n"
          + "      - machine3\n"
          + "    entrypoint: \n"
          + "      - /bin/bash\n"
          + "      - \"-c\"\n"
          + "    environment: \n"
          + "      - env1=123\n"
          + "      - env2=345\n"
          + "    expose: \n"
          + "      - \"3000\"\n"
          + "      - \"8080\"\n"
          + "    image: codenvy/ubuntu_jdk8\n"
          + "    labels: \n"
          + "      com.example.department: Finance\n"
          + "      com.example.description: \"Accounting webapp\"\n"
          + "      com.example.label-with-empty-value: \"\"\n"
          + "    links: \n"
          + "      - machine1\n"
          + "      - \"machine2:db\"\n"
          + "    mem_limit: 2147483648\n"
          + "    networks: \n"
          + "      - some-network\n"
          + "      - other-network\n"
          + "    ports: \n"
          + "      - \"3000\"\n"
          + "      - 3000-3005\n"
          + "    volumes: \n"
          + "      - \"/opt/data:/var/lib/mysql\"\n"
          + "      - \"~/configs:/etc/configs/:ro\"\n"
          + "    volumes_from: \n"
          + "      - \"machine2:ro\"\n"
          + "      - machine3\n"
          + "  machine2: \n"
          + "    image: codenvy/ubuntu_jdk8\n"
          + "  machine3: \n"
          + "    image: codenvy/ubuntu_jdk8\n";

  @Mock private InternalEnvironment environment;
  @Mock private InternalRecipe recipe;

  @InjectMocks private ComposeEnvironmentParser parser;

  @BeforeMethod
  public void setUp() {
    when(environment.getRecipe()).thenReturn(recipe);
  }

  @Test
  public void shouldBeAbleToParseComposeEnvironmentWithApplicationXYamlContentType()
      throws Exception {
    // given
    when(recipe.getContentType()).thenReturn("application/x-yaml");
    when(recipe.getContent()).thenReturn(COMPOSE_CONTENT);
    DockerEnvironment expectedEnv = createTestEnv();

    // when
    DockerEnvironment cheContainersEnvironment = parser.parse(environment);

    // then
    assertEquals(cheContainersEnvironment, expectedEnv);
  }

  @Test
  public void shouldBeAbleToParseComposeEnvironmentWithTextYamlContentType() throws Exception {
    // given
    when(recipe.getContentType()).thenReturn("text/yaml");
    when(recipe.getContent()).thenReturn(COMPOSE_CONTENT);
    DockerEnvironment expectedEnv = createTestEnv();

    // when
    DockerEnvironment cheContainersEnvironment = parser.parse(environment);

    // then
    assertEquals(cheContainersEnvironment, expectedEnv);
  }

  @Test
  public void shouldBeAbleToParseComposeEnvironmentWithTextXYamlContentType() throws Exception {
    // given
    when(recipe.getContentType()).thenReturn("text/x-yaml");
    when(recipe.getContent()).thenReturn(COMPOSE_CONTENT);
    DockerEnvironment expectedEnv = createTestEnv();

    // when
    DockerEnvironment cheContainersEnvironment = parser.parse(environment);

    // then
    assertEquals(cheContainersEnvironment, expectedEnv);
  }

  private DockerEnvironment createTestEnv() {
    DockerEnvironment cheContainersEnvironment = new DockerEnvironment();

    DockerContainerConfig cheContainer1 = new DockerContainerConfig();
    String buildContext = "http://host.com:port/location/of/dockerfile/or/git/repo/";
    cheContainer1.setBuild(
        new DockerBuildContext()
            .setContext(buildContext)
            .setDockerfilePath("dockerfile/Dockerfile_alternate")
            .setArgs(emptyMap()));
    cheContainer1.setCommand(asList("tail", "-f", "/dev/null"));
    cheContainer1.setContainerName("some_name");
    cheContainer1.setDependsOn(asList("machine2", "machine3"));
    cheContainer1.setEntrypoint(asList("/bin/bash", "-c"));
    cheContainer1.setEnvironment(ImmutableMap.of("env1", "123", "env2", "345"));
    cheContainer1.setExpose(asList("3000", "8080"));
    cheContainer1.setImage("codenvy/ubuntu_jdk8");
    cheContainer1.setLabels(
        ImmutableMap.of(
            "com.example.department",
            "Finance",
            "com.example.description",
            "Accounting webapp",
            "com.example.label-with-empty-value",
            ""));
    cheContainer1.setLinks(asList("machine1", "machine2:db"));
    cheContainer1.setMemLimit(2147483648L);
    cheContainer1.setNetworks(asList("some-network", "other-network"));
    cheContainer1.setPorts(asList("3000", "3000-3005"));
    cheContainer1.setVolumes(asList("/opt/data:/var/lib/mysql", "~/configs:/etc/configs/:ro"));
    cheContainer1.setVolumesFrom(asList("machine2:ro", "machine3"));

    DockerContainerConfig cheContainer2 = new DockerContainerConfig();
    cheContainer2.setImage("codenvy/ubuntu_jdk8");
    DockerContainerConfig cheContainer3 = new DockerContainerConfig();
    cheContainer3.setImage("codenvy/ubuntu_jdk8");

    cheContainersEnvironment.setContainers(
        ImmutableMap.of(
            "machine1", cheContainer1,
            "machine2", cheContainer2,
            "machine3", cheContainer3));
    return cheContainersEnvironment;
  }
}
