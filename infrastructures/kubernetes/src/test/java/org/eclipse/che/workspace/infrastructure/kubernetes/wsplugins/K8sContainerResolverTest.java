/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Names.MAX_CONTAINER_NAME_LENGTH;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.CheContainer;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.api.workspace.server.wsplugins.model.EnvVar;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
public class K8sContainerResolverTest {
  private static final String IMAGE = "testImage:tag";

  private CheContainer cheContainer;
  private K8sContainerResolver resolver;
  private List<ChePluginEndpoint> endpoints;

  @BeforeMethod
  public void setUp() {
    cheContainer = new CheContainer();
    endpoints = new ArrayList<>();
    resolver = new K8sContainerResolver("Always", cheContainer, endpoints);
  }

  @Test
  public void shouldSetImageFromSidecar() throws Exception {
    cheContainer.setImage(IMAGE);

    Container container = resolver.resolve();

    assertEquals(container.getImage(), IMAGE);
  }

  @Test
  public void shouldSetName() throws Exception {

    cheContainer.setName("cheContainerName");

    Container container = resolver.resolve();

    assertTrue(container.getName().startsWith((cheContainer.getName()).toLowerCase()));
  }

  @Test
  public void shouldLimitNameByMaxAllowedLength() throws Exception {

    cheContainer.setName("cheContainerNameWhichIsGreatlySucceedsMaxAllowedLengthByKubernetes");

    Container container = resolver.resolve();

    assertEquals(container.getName().length(), MAX_CONTAINER_NAME_LENGTH);
  }

  @Test
  public void shouldSetEnvVarsFromSidecar() throws Exception {
    Map<String, String> env = ImmutableMap.of("name1", "value1", "name2", "value2");
    cheContainer.setEnv(toSidecarEnvVars(env));

    Container container = resolver.resolve();

    assertEqualsNoOrder(container.getEnv().toArray(), toK8sEnvVars(env).toArray());
  }

  @Test
  public void shouldSetPortsFromContainerEndpoints() throws Exception {
    Integer[] ports = new Integer[] {3030, 10000};
    endpoints.addAll(toEndpoints(ports));

    Container container = resolver.resolve();

    assertEqualsNoOrder(container.getPorts().toArray(), toK8sPorts(ports).toArray());
  }

  @Test(dataProvider = "memLimitResourcesProvider")
  public void shouldProvisionSidecarMemoryLimitAndRequest(
      String sidecarMemLimit, ResourceRequirements resources) throws Exception {
    cheContainer.setMemoryLimit(sidecarMemLimit);
    cheContainer.setMemoryRequest(sidecarMemLimit);

    Container container = resolver.resolve();

    assertEquals(container.getResources(), resources);
  }

  @Test(dataProvider = "cpuLimitResourcesProvider")
  public void shouldProvisionSidecarCPULimitAndRequest(
      String sidecarCpuLimit, ResourceRequirements resources) throws Exception {
    cheContainer.setCpuLimit(sidecarCpuLimit);
    cheContainer.setCpuRequest(sidecarCpuLimit);

    Container container = resolver.resolve();

    assertEquals(container.getResources(), resources);
  }

  @DataProvider
  public static Object[][] memLimitResourcesProvider() {
    return new Object[][] {
      {"", null},
      {null, null},
      {"123456789", toK8sMemoryLimitRequestResources("123456789")},
      {"1Ki", toK8sMemoryLimitRequestResources("1Ki")},
      {"100M", toK8sMemoryLimitRequestResources("100M")},
    };
  }

  @DataProvider
  public static Object[][] cpuLimitResourcesProvider() {
    return new Object[][] {
      {"", null},
      {null, null},
      {"0.156", toK8sCPULimitRequestResources("0.156")},
      {"1", toK8sCPULimitRequestResources("1")},
      {"100m", toK8sCPULimitRequestResources("100m")},
    };
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp = "Sidecar memory limit field contains illegal value .*")
  public void shouldThrowExceptionIfMemoryLimitIsInIllegalFormat() throws Exception {
    cheContainer.setMemoryLimit("IllegalValue");

    resolver.resolve();
  }

  private static ResourceRequirements toK8sMemoryLimitRequestResources(String memLimit) {
    return new ResourceRequirementsBuilder()
        .addToLimits("memory", new Quantity(memLimit))
        .addToRequests("memory", new Quantity(memLimit))
        .build();
  }

  private static ResourceRequirements toK8sCPULimitRequestResources(String cpuLimit) {
    return new ResourceRequirementsBuilder()
        .addToLimits("cpu", new Quantity(cpuLimit))
        .addToRequests("cpu", new Quantity(cpuLimit))
        .build();
  }

  private List<EnvVar> toSidecarEnvVars(Map<String, String> envVars) {
    return envVars
        .entrySet()
        .stream()
        .map(entry -> new EnvVar().name(entry.getKey()).value(entry.getValue()))
        .collect(Collectors.toList());
  }

  private List<io.fabric8.kubernetes.api.model.EnvVar> toK8sEnvVars(Map<String, String> envVars) {
    return envVars
        .entrySet()
        .stream()
        .map(
            entry ->
                new io.fabric8.kubernetes.api.model.EnvVar(entry.getKey(), entry.getValue(), null))
        .collect(Collectors.toList());
  }

  private List<ContainerPort> toK8sPorts(Integer[] ports) {
    return Arrays.stream(ports).map(this::k8sPort).collect(Collectors.toList());
  }

  private ContainerPort k8sPort(Integer port) {
    ContainerPort containerPort = new ContainerPort();
    containerPort.setContainerPort(port);
    containerPort.setProtocol("TCP");
    return containerPort;
  }

  private List<ChePluginEndpoint> toEndpoints(Integer[] ports) {
    return Arrays.stream(ports)
        .map(p -> new ChePluginEndpoint().targetPort(p))
        .collect(Collectors.toList());
  }
}
