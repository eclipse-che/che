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
import static java.util.Collections.singletonMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.GatewayRouterProvisioner.GATEWAY_CONFIGMAP_LABELS;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
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
  public void provisionTlsForGatewayRouteConfigmaps(
      ServerConfigImpl server, boolean tlsEnabled, String expectedProtocol) throws Exception {
    // given
    Map<String, String> composedAnnotations = new HashMap<>(annotations);
    composedAnnotations.putAll(
        Annotations.newSerializer().server("server", server).machineName(machine).annotations());
    ConfigMap routeConfigMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withLabels(GATEWAY_CONFIGMAP_LABELS)
            .withAnnotations(composedAnnotations)
            .endMetadata()
            .build();

    GatewayTlsProvisioner<KubernetesEnvironment> gatewayTlsProvisioner =
        new GatewayTlsProvisioner<>(tlsEnabled);

    when(k8sEnv.getConfigMaps()).thenReturn(singletonMap("route", routeConfigMap));

    // when
    gatewayTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(routeConfigMap.getMetadata().getAnnotations()).servers();
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
    ConfigMap routeConfigMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName("route")
            .withLabels(GATEWAY_CONFIGMAP_LABELS)
            .withAnnotations(composedAnnotations)
            .endMetadata()
            .build();

    when(k8sEnv.getConfigMaps()).thenReturn(singletonMap("route", routeConfigMap));
    GatewayTlsProvisioner<KubernetesEnvironment> gatewayTlsProvisioner =
        new GatewayTlsProvisioner<>(true);

    // when
    gatewayTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then exception
  }
}
