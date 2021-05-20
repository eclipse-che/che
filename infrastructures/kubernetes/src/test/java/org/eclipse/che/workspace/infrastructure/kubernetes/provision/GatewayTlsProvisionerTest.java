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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
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
import org.eclipse.che.workspace.infrastructure.kubernetes.server.WorkspaceExposureType;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.GatewayConfigmapLabels;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GatewayTlsProvisionerTest {

  public static final String WORKSPACE_ID = "workspace123";
  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private GatewayConfigmapLabels gatewayConfigmapLabels;
  @Mock private TlsProvisionerProvider<KubernetesEnvironment> tlsProvisionerProvider;
  @Mock private TlsProvisioner<KubernetesEnvironment> nativeTlsProvisioner;

  private final ServerConfigImpl httpServer =
      new ServerConfigImpl("8080/tpc", "http", "/api", emptyMap());
  private final ServerConfigImpl wsServer =
      new ServerConfigImpl("8080/tpc", "ws", "/ws", emptyMap());
  private final Map<String, String> annotations =
      singletonMap("annotation-key", "annotation-value");
  private final String machine = "machine";

  @BeforeMethod
  public void setUp() {
    lenient().when(gatewayConfigmapLabels.isGatewayConfig(any(ConfigMap.class))).thenReturn(true);
    when(tlsProvisionerProvider.get(eq(WorkspaceExposureType.NATIVE)))
        .thenReturn(nativeTlsProvisioner);
  }

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
            .withAnnotations(composedAnnotations)
            .endMetadata()
            .build();

    GatewayTlsProvisioner<KubernetesEnvironment> gatewayTlsProvisioner =
        new GatewayTlsProvisioner<>(tlsEnabled, gatewayConfigmapLabels, tlsProvisionerProvider);

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
            .withAnnotations(composedAnnotations)
            .endMetadata()
            .build();

    when(k8sEnv.getConfigMaps()).thenReturn(singletonMap("route", routeConfigMap));
    GatewayTlsProvisioner<KubernetesEnvironment> gatewayTlsProvisioner =
        new GatewayTlsProvisioner<>(true, gatewayConfigmapLabels, tlsProvisionerProvider);

    // when
    gatewayTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then exception
  }

  @Test
  public void nativeRoutesProvisioned() throws Exception {
    // given
    GatewayTlsProvisioner<KubernetesEnvironment> gatewayTlsProvisioner =
        new GatewayTlsProvisioner<>(true, gatewayConfigmapLabels, tlsProvisionerProvider);

    // when
    gatewayTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    verify(nativeTlsProvisioner).provision(eq(k8sEnv), eq(runtimeIdentity));
  }
}
