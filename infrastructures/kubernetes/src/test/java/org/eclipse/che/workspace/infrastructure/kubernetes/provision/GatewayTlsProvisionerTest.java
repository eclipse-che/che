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
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesInfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
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
  private final Map<String, ServerConfig> servers =
      ImmutableMap.of("http-server", httpServer, "ws-server", wsServer);
  private final Map<String, String> annotations =
      singletonMap("annotation-key", "annotation-value");
  private final String machine = "machine";

  private final ConfigMap configMap =
      new ConfigMapBuilder()
          .withNewMetadata()
          .withName("gatewayConfig")
          .withAnnotations(annotations)
          .endMetadata()
          .build();

  @Test
  public void provisionTlsForConfigMap() throws Exception {
    // given
    GatewayTlsProvisioner<KubernetesEnvironment> ingressTlsProvisioner =
        new GatewayTlsProvisioner<>(true);

    configMap
        .getMetadata()
        .getAnnotations()
        .putAll(Annotations.newSerializer().servers(servers).machineName(machine).annotations());

    Map<String, ConfigMap> configMaps = new HashMap<>();
    configMaps.put("cm", configMap);
    when(k8sEnv.getConfigMaps()).thenReturn(configMaps);

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(configMap.getMetadata().getAnnotations()).servers();
    assertEquals(servers.get("http-server").getProtocol(), "https");
    assertEquals(servers.get("ws-server").getProtocol(), "wss");
  }

  @Test
  public void shouldNotChangeProtocolWhenTlsDisabled() throws KubernetesInfrastructureException {
    // given
    GatewayTlsProvisioner<KubernetesEnvironment> ingressTlsProvisioner =
        new GatewayTlsProvisioner<>(false);

    configMap
        .getMetadata()
        .getAnnotations()
        .putAll(Annotations.newSerializer().servers(servers).machineName(machine).annotations());

    Map<String, ConfigMap> configMaps = new HashMap<>();
    configMaps.put("cm", configMap);
    when(k8sEnv.getConfigMaps()).thenReturn(configMaps);

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    Map<String, ServerConfigImpl> servers =
        Annotations.newDeserializer(configMap.getMetadata().getAnnotations()).servers();
    assertEquals(servers.get("http-server").getProtocol(), "http");
    assertEquals(servers.get("ws-server").getProtocol(), "ws");
  }
}
