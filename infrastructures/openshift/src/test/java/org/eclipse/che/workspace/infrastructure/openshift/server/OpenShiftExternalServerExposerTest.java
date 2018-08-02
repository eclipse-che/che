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

import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.openshift.api.model.Route;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations.Deserializer;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftExternalServerExposer}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftExternalServerExposerTest {

  private OpenShiftExternalServerExposer osExternalServerExposer =
      new OpenShiftExternalServerExposer();

  @Test
  public void shouldAddRouteToEnvForExposingSpecifiedServer() {
    // given
    OpenShiftEnvironment osEnv = OpenShiftEnvironment.builder().build();
    Map<String, ServerConfig> servers = new HashMap<>();

    // when
    osExternalServerExposer.expose(
        osEnv,
        "machine123",
        "service123",
        new ServicePort("servicePort", null, null, "TCP", null),
        servers);

    // then
    Route route = osEnv.getRoutes().get("service123-servicePort");
    assertNotNull(route);

    assertEquals(route.getSpec().getTo().getName(), "service123");
    assertEquals(route.getSpec().getPort().getTargetPort().getStrVal(), "servicePort");

    Deserializer annotations = Annotations.newDeserializer(route.getMetadata().getAnnotations());
    assertEquals(annotations.machineName(), "machine123");
    assertEquals(annotations.servers(), servers);
  }
}
