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

package org.eclipse.che.workspace.infrastructure.openshift.util;

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
import java.util.Optional;
import org.testng.annotations.Test;

public class RoutesTest {

  @Test
  public void shouldFindRouteWhenPortDefinedByString() {
    int portInt = 8080;
    String portString = "8080";

    Service service = createService(portString, portInt);
    Route route = createRoute(new IntOrString(portString));

    Optional<Route> foundRoute =
        Routes.findRouteForServicePort(Collections.singletonList(route), service, portInt);
    assertTrue(foundRoute.isPresent());
    assertEquals(foundRoute.get().getSpec().getHost(), "testhost");
  }

  @Test
  public void shouldFindRouteWhenPortDefinedByInt() {
    int portInt = 8080;
    String portString = "8080";

    Service service = createService(portString, portInt);
    Route route = createRoute(new IntOrString(portInt));

    Optional<Route> foundRoute =
        Routes.findRouteForServicePort(Collections.singletonList(route), service, portInt);
    assertTrue(foundRoute.isPresent());
    assertEquals(foundRoute.get().getSpec().getHost(), "testhost");
  }

  @Test
  public void shouldReturnEmptyWhenNotFoundByInt() {
    int portInt = 8080;
    String portString = "8080";

    Service service = createService(portString, portInt);
    Route route = createRoute(new IntOrString(666));

    Optional<Route> foundRoute =
        Routes.findRouteForServicePort(Collections.singletonList(route), service, portInt);
    assertFalse(foundRoute.isPresent());
  }

  @Test
  public void shouldReturnEmptyWhenNotFoundByString() {
    int portInt = 8080;
    String portString = "8080";

    Service service = createService(portString, portInt);
    Route route = createRoute(new IntOrString("666"));

    Optional<Route> foundRoute =
        Routes.findRouteForServicePort(Collections.singletonList(route), service, portInt);
    assertFalse(foundRoute.isPresent());
  }

  private Route createRoute(IntOrString port) {
    Route route = new Route();
    RouteSpec routeSpec = new RouteSpec();
    routeSpec.setPort(new RoutePort(port));
    routeSpec.setTo(new RouteTargetReference("a", "servicename", 1));
    routeSpec.setHost("testhost");
    route.setSpec(routeSpec);
    return route;
  }

  private Service createService(String portString, int portInt) {
    Service service = new Service();
    ObjectMeta metadata = new ObjectMeta();
    metadata.setName("servicename");
    service.setMetadata(metadata);
    ServiceSpec spec = new ServiceSpec();
    spec.setPorts(
        Collections.singletonList(new ServicePort(null, portString, null, portInt, "TCP", null)));
    service.setSpec(spec);
    return service;
  }
}
