/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static java.util.Collections.emptyMap;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.RouteTlsProvisioner.TERMINATION_EDGE;
import static org.eclipse.che.workspace.infrastructure.openshift.provision.RouteTlsProvisioner.TERMINATION_POLICY_REDIRECT;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link RouteTlsProvisioner}.
 *
 * @author Ilya Buziuk
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class RouteTlsProvisionerTest {

  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  @Test
  public void doNothingWhenTlsDisabled() throws Exception {
    // given
    RouteTlsProvisioner tlsProvisioner = new RouteTlsProvisioner(false);

    // when
    tlsProvisioner.provision(osEnv, runtimeIdentity);

    // then
    verify(osEnv, never()).getRoutes();
  }

  @Test
  public void provisionTlsForRoutes() throws Exception {
    // given
    RouteTlsProvisioner tlsProvisioner = new RouteTlsProvisioner(true);
    ServerConfigImpl httpServer = new ServerConfigImpl("8080/tpc", "http", "/api", emptyMap());
    ServerConfigImpl wsServer = new ServerConfigImpl("8080/tpc", "ws", "/ws", emptyMap());

    final Map<String, Route> routes = new HashMap<>();
    Route route =
        createRoute("route", ImmutableMap.of("http-server", httpServer, "ws-server", wsServer));
    routes.put("route", route);
    when(osEnv.getRoutes()).thenReturn(routes);

    // when
    tlsProvisioner.provision(osEnv, runtimeIdentity);

    // then
    assertEquals(route.getSpec().getTls().getTermination(), TERMINATION_EDGE);
    assertEquals(
        route.getSpec().getTls().getInsecureEdgeTerminationPolicy(), TERMINATION_POLICY_REDIRECT);

    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(route.getMetadata().getAnnotations()).servers();
    assertEquals(servers.get("http-server").getProtocol(), "https");
    assertEquals(servers.get("ws-server").getProtocol(), "wss");
  }

  @Test
  public void shouldNotThrowNPE() throws Exception {
    // given
    RouteTlsProvisioner tlsProvisioner = new RouteTlsProvisioner(true);

    final Map<String, Route> routes = new HashMap<>();
    Route route =
        new RouteBuilder()
            .withNewMetadata()
            .withName("name")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    routes.put("route", route);
    when(osEnv.getRoutes()).thenReturn(routes);

    // when
    tlsProvisioner.provision(osEnv, runtimeIdentity);
  }

  private Route createRoute(String name, Map<String, ServerConfigImpl> servers) {
    return new RouteBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(Annotations.newSerializer().servers(servers).annotations())
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .build();
  }
}
