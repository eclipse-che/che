/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_ORIGINAL_NAME_LABEL;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePluginEndpoint;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Oleksandr Garagatyi */
public class SidecarServicesProvisionerTest {
  private static final String POD_NAME = "testPod";
  private static final String CONFLICTING_SERVICE_NAME = "testService";

  private SidecarServicesProvisioner provisioner;
  private List<ChePluginEndpoint> endpoints;

  @BeforeMethod
  public void setUp() {
    endpoints = new ArrayList<>();
    provisioner = new SidecarServicesProvisioner(endpoints, POD_NAME);
  }

  @Test
  public void shouldAddServiceForEachEndpoint() throws Exception {
    List<ChePluginEndpoint> actualEndpoints =
        asList(
            new ChePluginEndpoint().name("testE1").targetPort(8080),
            new ChePluginEndpoint().name("testE2").targetPort(10000));
    endpoints.addAll(actualEndpoints);
    KubernetesEnvironment kubernetesEnvironment = KubernetesEnvironment.builder().build();

    provisioner.provision(kubernetesEnvironment);

    assertEquals(kubernetesEnvironment.getServices(), toK8sServices(actualEndpoints));
  }

  @Test
  public void shouldNotAddServiceForNotdDiscoverableEndpoint() throws Exception {
    List<ChePluginEndpoint> actualEndpoints =
        asList(
            new ChePluginEndpoint().name("testE1").targetPort(8080),
            new ChePluginEndpoint()
                .name("testE2")
                .targetPort(10000)
                .attributes(ImmutableMap.of("discoverable", "false")));
    endpoints.addAll(actualEndpoints);
    KubernetesEnvironment kubernetesEnvironment = KubernetesEnvironment.builder().build();

    provisioner.provision(kubernetesEnvironment);

    assertEquals(
        kubernetesEnvironment.getServices(),
        toK8sServices(ImmutableList.of(new ChePluginEndpoint().name("testE1").targetPort(8080))));
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Applying of sidecar tooling failed. Kubernetes service with name '"
              + CONFLICTING_SERVICE_NAME
              + "' already exists in the workspace environment.")
  public void shouldNotDuplicateServicesWhenThereAreConflictingEndpoints() throws Exception {
    List<ChePluginEndpoint> actualEndpoints =
        asList(
            new ChePluginEndpoint().name(CONFLICTING_SERVICE_NAME).targetPort(8080),
            new ChePluginEndpoint().name(CONFLICTING_SERVICE_NAME).targetPort(10000));
    endpoints.addAll(actualEndpoints);
    KubernetesEnvironment kubernetesEnvironment = KubernetesEnvironment.builder().build();

    provisioner.provision(kubernetesEnvironment);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Applying of sidecar tooling failed. Kubernetes service with name '"
              + CONFLICTING_SERVICE_NAME
              + "' already exists in the workspace environment.")
  public void shouldNotDuplicateServicesWhenThereIsConflictingServiceInK8sEnv() throws Exception {
    List<ChePluginEndpoint> actualEndpoints =
        singletonList(new ChePluginEndpoint().name(CONFLICTING_SERVICE_NAME).targetPort(8080));
    endpoints.addAll(actualEndpoints);
    KubernetesEnvironment kubernetesEnvironment =
        KubernetesEnvironment.builder()
            .setServices(singletonMap(CONFLICTING_SERVICE_NAME, new Service()))
            .build();

    provisioner.provision(kubernetesEnvironment);
  }

  private Map<String, Service> toK8sServices(List<ChePluginEndpoint> endpoints) {
    return endpoints
        .stream()
        .map(this::createService)
        .collect(toMap(s -> s.getMetadata().getName(), Function.identity()));
  }

  private Service createService(ChePluginEndpoint endpoint) {
    ServicePort servicePort =
        new ServicePortBuilder()
            .withPort(endpoint.getTargetPort())
            .withProtocol("TCP")
            .withNewTargetPort(endpoint.getTargetPort())
            .build();
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(endpoint.getName())
        .endMetadata()
        .withNewSpec()
        .withSelector(singletonMap(CHE_ORIGINAL_NAME_LABEL, POD_NAME))
        .withPorts(singletonList(servicePort))
        .endSpec()
        .build();
  }
}
