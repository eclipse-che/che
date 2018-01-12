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
package org.eclipse.che.workspace.infrastructure.docker.environment.convert;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.ComposeEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.BuildContext;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link ComposeEnvironmentConverter}.
 *
 * @author Alexander Andrienko
 */
@Listeners
public class ComposeEnvironmentConverterTest {

  private ComposeEnvironment environment;

  private ComposeEnvironmentConverter converter;

  @BeforeMethod
  public void setUp() {
    environment = createTestComposeEnv();
    converter = new ComposeEnvironmentConverter();
  }

  @Test
  public void shouldBeAbleToConvertComposeEnvironmentToDockerEnvironment() throws Exception {
    // given
    DockerEnvironment expectedEnv = createTestDockerEnv();

    // when
    DockerEnvironment cheContainersEnvironment = converter.convert(environment);

    // then
    assertEquals(cheContainersEnvironment, expectedEnv);
  }

  private ComposeEnvironment createTestComposeEnv() {
    ComposeService machine1 =
        new ComposeService()
            .withBuild(
                new BuildContext()
                    .withContext("http://host.com:port/location/of/dockerfile/or/git/repo/")
                    .withDockerfile("dockerfile/Dockerfile_alternate"))
            .withCommand(asList("tail", "-f", "/dev/null"))
            .withContainerName("some_name")
            .withDependsOn(asList("machine2", "machine3"))
            .withEntrypoint(asList("/bin/bash", "-c"))
            .withEnvironment((ImmutableMap.of("env1", "123", "env2", "345")))
            .withExpose(ImmutableSet.of("3000", "8080"))
            .withImage("eclipse/ubuntu_jdk8")
            .withLabels(
                ImmutableMap.of(
                    "com.example.department",
                    "Finance",
                    "com.example.description",
                    "Accounting webapp",
                    "com.example.label-with-empty-value",
                    ""))
            .withLinks(asList("machine1", "machine2:db"))
            .withMemLimit(2147483648L)
            .withNetworks(asList("some-network", "other-network"))
            .withPorts(asList("3000", "3000-3005"))
            .withVolumes(asList("/opt/data:/var/lib/mysql", "~/configs:/etc/configs/:ro"))
            .withVolumesFrom(asList("machine2:ro", "machine3"));
    ComposeService machine2 = new ComposeService().withImage("che/ubuntu_jdk8");
    ComposeService machine3 = new ComposeService().withImage("che/centos_jdk8");
    ComposeEnvironment composeEnv = mock(ComposeEnvironment.class);
    when(composeEnv.getServices())
        .thenReturn(
            newLinkedHashMap(
                ImmutableMap.of("machine1", machine1, "machine2", machine2, "machine3", machine3)));
    return composeEnv;
  }

  private DockerEnvironment createTestDockerEnv() {
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
    cheContainer1.setExpose(ImmutableSet.of("3000", "8080"));
    cheContainer1.setImage("eclipse/ubuntu_jdk8");
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
    cheContainer2.setImage("che/ubuntu_jdk8");
    DockerContainerConfig cheContainer3 = new DockerContainerConfig();
    cheContainer3.setImage("che/centos_jdk8");

    cheContainersEnvironment.setContainers(
        newLinkedHashMap(
            ImmutableMap.of(
                "machine1", cheContainer1,
                "machine2", cheContainer2,
                "machine3", cheContainer3)));
    return cheContainersEnvironment;
  }
}
