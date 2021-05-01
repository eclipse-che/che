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

package org.eclipse.che.workspace.infrastructure.openshift.server;

import static java.util.Collections.singletonList;
import static org.testng.Assert.*;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RoutePort;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.RouteTargetReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.PreviewUrlImpl;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class OpenShiftPreviewUrlExposerTest {

  private OpenShiftPreviewUrlExposer previewUrlEndpointsProvisioner;

  @BeforeMethod
  public void setUp() {
    RouteServerExposer externalServerExposer = new RouteServerExposer("a=b", null);
    previewUrlEndpointsProvisioner = new OpenShiftPreviewUrlExposer(externalServerExposer);
  }

  @Test
  public void shouldDoNothingWhenNoCommandsDefined() throws InternalInfrastructureException {
    OpenShiftEnvironment env = OpenShiftEnvironment.builder().build();

    previewUrlEndpointsProvisioner.expose(env);

    assertTrue(env.getCommands().isEmpty());
    assertTrue(env.getServices().isEmpty());
    assertTrue(env.getRoutes().isEmpty());
  }

  @Test
  public void shouldDoNothingWhenNoCommandWithPreviewUrlDefined()
      throws InternalInfrastructureException {
    CommandImpl command = new CommandImpl("a", "a", "a");
    OpenShiftEnvironment env =
        OpenShiftEnvironment.builder().setCommands(singletonList(new CommandImpl(command))).build();

    previewUrlEndpointsProvisioner.expose(env);

    assertEquals(env.getCommands().get(0), command);
    assertTrue(env.getServices().isEmpty());
    assertTrue(env.getRoutes().isEmpty());
  }

  @Test
  public void shouldNotProvisionWhenServiceAndRouteFound() throws InternalInfrastructureException {
    final int PORT = 8080;
    final String SERVER_PORT_NAME = "server-" + PORT;

    CommandImpl command =
        new CommandImpl("a", "a", "a", new PreviewUrlImpl(PORT, null), Collections.emptyMap());

    Service service = new Service();
    ObjectMeta serviceMeta = new ObjectMeta();
    serviceMeta.setName("servicename");
    service.setMetadata(serviceMeta);
    ServiceSpec serviceSpec = new ServiceSpec();
    serviceSpec.setPorts(
        singletonList(
            new ServicePort(null, SERVER_PORT_NAME, null, PORT, "TCP", new IntOrString(PORT))));
    service.setSpec(serviceSpec);

    Route route = new Route();
    RouteSpec routeSpec = new RouteSpec();
    routeSpec.setPort(new RoutePort(new IntOrString(SERVER_PORT_NAME)));
    routeSpec.setTo(new RouteTargetReference("routekind", "servicename", 1));
    route.setSpec(routeSpec);

    Map<String, Service> services = new HashMap<>();
    services.put("servicename", service);
    Map<String, Route> routes = new HashMap<>();
    routes.put("routename", route);

    OpenShiftEnvironment env =
        OpenShiftEnvironment.builder()
            .setCommands(singletonList(new CommandImpl(command)))
            .setServices(services)
            .setRoutes(routes)
            .build();

    assertEquals(env.getRoutes().size(), 1);
    previewUrlEndpointsProvisioner.expose(env);
    assertEquals(env.getRoutes().size(), 1);
  }

  @Test
  public void shouldProvisionRouteWhenNotFound() throws InternalInfrastructureException {
    final int PORT = 8080;
    final String SERVER_PORT_NAME = "server-" + PORT;
    final String SERVICE_NAME = "servicename";

    CommandImpl command =
        new CommandImpl("a", "a", "a", new PreviewUrlImpl(PORT, null), Collections.emptyMap());

    Service service = new Service();
    ObjectMeta serviceMeta = new ObjectMeta();
    serviceMeta.setName(SERVICE_NAME);
    service.setMetadata(serviceMeta);
    ServiceSpec serviceSpec = new ServiceSpec();
    serviceSpec.setPorts(
        singletonList(
            new ServicePort(null, SERVER_PORT_NAME, null, PORT, "TCP", new IntOrString(PORT))));
    service.setSpec(serviceSpec);

    Map<String, Service> services = new HashMap<>();
    services.put(SERVICE_NAME, service);

    OpenShiftEnvironment env =
        OpenShiftEnvironment.builder()
            .setCommands(singletonList(new CommandImpl(command)))
            .setServices(services)
            .setRoutes(new HashMap<>())
            .build();

    previewUrlEndpointsProvisioner.expose(env);
    assertEquals(env.getRoutes().size(), 1);
    Route provisionedRoute = env.getRoutes().values().iterator().next();
    assertEquals(provisionedRoute.getSpec().getTo().getName(), SERVICE_NAME);
    assertEquals(
        provisionedRoute.getSpec().getPort().getTargetPort().getStrVal(), SERVER_PORT_NAME);
  }

  @Test
  public void shouldProvisionServiceAndRouteWhenNotFound() throws InternalInfrastructureException {
    final int PORT = 8080;
    final String SERVER_PORT_NAME = "server-" + PORT;

    CommandImpl command =
        new CommandImpl("a", "a", "a", new PreviewUrlImpl(PORT, null), Collections.emptyMap());

    OpenShiftEnvironment env =
        OpenShiftEnvironment.builder()
            .setCommands(singletonList(new CommandImpl(command)))
            .setRoutes(new HashMap<>())
            .setServices(new HashMap<>())
            .build();

    previewUrlEndpointsProvisioner.expose(env);

    assertEquals(env.getRoutes().size(), 1);
    assertEquals(env.getServices().size(), 1);

    Service provisionedService = env.getServices().values().iterator().next();
    ServicePort provisionedServicePort = provisionedService.getSpec().getPorts().get(0);
    assertEquals(provisionedServicePort.getName(), SERVER_PORT_NAME);
    assertEquals(provisionedServicePort.getPort().intValue(), PORT);

    Route provisionedRoute = env.getRoutes().values().iterator().next();
    assertEquals(
        provisionedRoute.getSpec().getTo().getName(), provisionedService.getMetadata().getName());
    assertEquals(
        provisionedRoute.getSpec().getPort().getTargetPort().getStrVal(), SERVER_PORT_NAME);
  }
}
