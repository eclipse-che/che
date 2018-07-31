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
package org.eclipse.che.workspace.infrastructure.docker;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.infrastructure.docker.auth.UserSpecificDockerRegistryCredentialsProvider;
import org.eclipse.che.infrastructure.docker.client.DockerConnector;
import org.eclipse.che.infrastructure.docker.client.params.BuildImageParams;
import org.eclipse.che.workspace.infrastructure.docker.logs.MachineLoggersFactory;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
@Listeners(MockitoTestNGListener.class)
public class ParallelDockerImagesBuilderTest {

  @Mock private RuntimeIdentity identity;
  @Mock private UserSpecificDockerRegistryCredentialsProvider dockerCredentials;
  @Mock private DockerConnector dockerConnector;
  @Mock private MachineLoggersFactory machineLoggersFactory;

  private ParallelDockerImagesBuilder dockerImagesBuilder;

  @BeforeMethod
  public void setUp() throws Exception {
    dockerImagesBuilder =
        new ParallelDockerImagesBuilder(
            identity, false, 10, dockerCredentials, dockerConnector, machineLoggersFactory);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Che container '.*' doesn't have neither build nor image fields"
  )
  void shouldThrowExceptionWhenNoBuildDataPresent() throws Throwable {
    DockerContainerConfig config = new DockerContainerConfig();
    config.setBuild(new DockerBuildContext());
    Map<String, DockerContainerConfig> input = singletonMap("machine1", config);
    dockerImagesBuilder.prepareImages(input);
  }

  @Test(
    expectedExceptions = InfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Try to build a docker machine source with an invalid location/content. It is not in the expected format"
  )
  void shouldThrowExceptionWhenImageFormatWrong() throws Throwable {
    DockerContainerConfig config = new DockerContainerConfig();
    config.setImage("**/%%");
    Map<String, DockerContainerConfig> input = singletonMap("machine1", config);
    dockerImagesBuilder.prepareImages(input);
  }

  @Test
  void shouldPullAllImages() throws Throwable {
    DockerContainerConfig config1 =
        new DockerContainerConfig().setContainerName("container1").setImage("ubuntu/jdk8");
    DockerContainerConfig config2 =
        new DockerContainerConfig().setContainerName("container2").setImage("ubuntu/jdk9");

    Map<String, DockerContainerConfig> input = new HashMap<>();
    input.put("machine1", config1);
    input.put("machine2", config2);
    when(dockerConnector.listImages(any())).thenReturn(Collections.emptyList());
    Map<String, String> result = dockerImagesBuilder.prepareImages(input);

    verify(dockerConnector, times(input.size())).pull(any(), any());
    verify(dockerConnector, times(input.size())).tag(any());
    assertEquals(result.size(), input.size());
    assertTrue(result.keySet().containsAll(input.keySet()));
    assertTrue(result.values().contains("eclipse-che/" + config1.getContainerName()));
    assertTrue(result.values().contains("eclipse-che/" + config2.getContainerName()));
  }

  @Test
  void shouldBuildAllImages() throws Throwable {

    Map<String, String> args1 = singletonMap("key1", "value1");
    DockerBuildContext context1 =
        new DockerBuildContext().setDockerfileContent("FROM ubuntu/jdk8").setArgs(args1);
    DockerContainerConfig config1 =
        new DockerContainerConfig().setBuild(context1).setMemLimit(1_024_000_000L);

    Map<String, String> args2 = singletonMap("key2", "value2");
    DockerBuildContext context2 =
        new DockerBuildContext().setDockerfileContent("FROM ubuntu/jdk9").setArgs(args2);
    DockerContainerConfig config2 =
        new DockerContainerConfig().setBuild(context2).setMemLimit(2_048_000_000L);

    Map<String, DockerContainerConfig> input = new HashMap<>();
    input.put("machine1", config1);
    input.put("machine2", config2);

    when(dockerConnector.listImages(any())).thenReturn(Collections.emptyList());
    Map<String, String> result = dockerImagesBuilder.prepareImages(input);

    ArgumentCaptor<BuildImageParams> captor = ArgumentCaptor.forClass(BuildImageParams.class);
    verify(dockerConnector, times(input.size())).buildImage(captor.capture(), any());
    assertEquals(result.size(), input.size());
    List<BuildImageParams> list = captor.getAllValues();
    assertTrue(
        list.stream()
            .map(BuildImageParams::getMemoryLimit)
            .anyMatch(l -> l.equals(config1.getMemLimit())));
    assertTrue(
        list.stream()
            .map(BuildImageParams::getMemoryLimit)
            .anyMatch(l -> l.equals(config2.getMemLimit())));
    assertTrue(list.stream().map(BuildImageParams::getBuildArgs).anyMatch(m -> m.equals(args1)));
    assertTrue(list.stream().map(BuildImageParams::getBuildArgs).anyMatch(m -> m.equals(args2)));
  }
}
