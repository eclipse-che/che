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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GatewayTlsProvisionerTest {

  public static final String WORKSPACE_ID = "workspace123";
  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  private final ServerConfigImpl httpServer =
      new ServerConfigImpl("8080/tpc", "http", "/api", emptyMap());
  private final ServerConfigImpl wsServer =
      new ServerConfigImpl("8080/tpc", "ws", "/ws", emptyMap());
  private final Map<String, String> annotations =
      singletonMap("annotation-key", "annotation-value");
  private final String machine = "machine";

  @Test(dataProvider = "tlsProvisionData")
  public void provisionTlsForGatewayRouteConfig(
      ServerConfigImpl server, boolean tlsEnabled, String expectedProtocol) throws Exception {
    // given
    Map<String, String> composedAnnotations = new HashMap<>(annotations);
    composedAnnotations.putAll(
        Annotations.newSerializer().server("server", server).machineName(machine).annotations());
    GatewayRouteConfig routeConfig =
        new GatewayRouteConfig("gate1", "svc1", "1234", "/hello", "http", composedAnnotations);
    GatewayTlsProvisioner<KubernetesEnvironment> gatewayTlsProvisioner =
        new GatewayTlsProvisioner<>(tlsEnabled);

    when(k8sEnv.getGatewayRouteConfigs()).thenReturn(singletonList(routeConfig));

    // when
    gatewayTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(routeConfig.getAnnotations()).servers();
    assertEquals(servers.get("server").getProtocol(), expectedProtocol);
  }

  @DataProvider
  public Object[][] tlsProvisionData() {
    return new Object[][] {
      {httpServer, true, "https"},
      {httpServer, false, "http"},
      {wsServer, true, "wss"},
      {wsServer, false, "ws"},
    };
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwExceptionWhenMultipleServersInGatewayRouteConfigAnnotations()
      throws InfrastructureException {
    // given
    Map<String, String> composedAnnotations = new HashMap<>(annotations);
    composedAnnotations.putAll(
        Annotations.newSerializer()
            .server("server1", httpServer)
            .server("server2", wsServer)
            .machineName(machine)
            .annotations());
    GatewayRouteConfig routeConfig =
        new GatewayRouteConfig("gate1", "svc1", "1234", "/hello", "http", composedAnnotations);

    when(k8sEnv.getGatewayRouteConfigs()).thenReturn(singletonList(routeConfig));
    GatewayTlsProvisioner<KubernetesEnvironment> gatewayTlsProvisioner =
        new GatewayTlsProvisioner<>(true);

    // when
    gatewayTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then exception
  }
}
