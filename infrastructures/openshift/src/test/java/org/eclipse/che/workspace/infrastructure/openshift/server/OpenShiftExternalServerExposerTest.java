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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.Route;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.Deserializer;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.testng.annotations.Test;

/**
 * Tests {@link RouteServerExposer}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftExternalServerExposerTest {

  private static final String LABELS = "foo=bar";
  private RouteServerExposer osExternalServerExposer = new RouteServerExposer(LABELS, null);

  @Test
  public void shouldAddRouteToEnvForExposingSpecifiedServer() {
    // given
    OpenShiftEnvironment osEnv = OpenShiftEnvironment.builder().build();
    Map<String, ServerConfig> servers = new HashMap<>();
    servers.put("server", new ServerConfigImpl());

    // when
    osExternalServerExposer.expose(
        osEnv,
        "machine123",
        "service123",
        null,
        new ServicePort(null, "servicePort", null, null, "TCP", null),
        servers);

    // then
    assertEquals(1, osEnv.getRoutes().size());
    Route route = osEnv.getRoutes().values().iterator().next();
    assertNotNull(route);

    assertEquals(route.getSpec().getTo().getName(), "service123");
    assertEquals(route.getSpec().getPort().getTargetPort().getStrVal(), "servicePort");

    Deserializer annotations = Annotations.newDeserializer(route.getMetadata().getAnnotations());
    assertEquals(annotations.machineName(), "machine123");
    assertEquals(annotations.servers(), servers);
    assertEquals(route.getMetadata().getLabels().get("foo"), "bar");
    assertNull(route.getSpec().getHost());
  }

  @Test
  public void shouldAddRouteToEnvForExposingSpecifiedServerWithSpecificHost() {
    // given
    RouteServerExposer osExternalServerExposer = new RouteServerExposer(LABELS, "open.che.org");
    OpenShiftEnvironment osEnv = OpenShiftEnvironment.builder().build();
    Map<String, ServerConfig> servers = new HashMap<>();
    servers.put("server", new ServerConfigImpl());

    // when
    osExternalServerExposer.expose(
        osEnv,
        "machine123",
        "service123",
        null,
        new ServicePort(null, "servicePort", null, null, "TCP", null),
        servers);

    // then
    assertEquals(1, osEnv.getRoutes().size());
    Route route = osEnv.getRoutes().values().iterator().next();
    assertNotNull(route);

    assertEquals(route.getSpec().getTo().getName(), "service123");
    assertEquals(route.getSpec().getPort().getTargetPort().getStrVal(), "servicePort");
    assertTrue(route.getSpec().getHost().endsWith(".open.che.org"));
    assertTrue(route.getSpec().getHost().startsWith("route"));
  }
}
