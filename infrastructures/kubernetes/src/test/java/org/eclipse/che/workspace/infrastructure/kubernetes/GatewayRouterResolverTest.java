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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import static org.eclipse.che.workspace.infrastructure.kubernetes.GatewayRouterResolver.GATEWAY_CONFIGMAP_LABELS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ConfigMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.GatewayRouteConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.GatewayRouteConfigGeneratorFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GatewayRouterResolverTest {

  private static final String NAMESPACE = "che";
  private static final String WS_ID = "ws-1";

  @Mock private GatewayRouteConfigGeneratorFactory configGeneratorFactory;
  @Mock private GatewayRouteConfigGenerator configGenerator;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private InternalEnvironment internalEnvironment;

  private GatewayRouterResolver gatewayRouterResolver;

  @BeforeMethod
  public void setUp() throws InfrastructureException {
    lenient().when(configGeneratorFactory.create(NAMESPACE)).thenReturn(configGenerator);
    lenient().when(configGenerator.generate()).thenReturn(Collections.singletonMap("one", "two"));
    lenient().when(runtimeIdentity.getInfrastructureNamespace()).thenReturn(NAMESPACE);
    lenient().when(runtimeIdentity.getWorkspaceId()).thenReturn(WS_ID);
    gatewayRouterResolver = new GatewayRouterResolver(configGeneratorFactory);
  }

  @Test
  public void testRouteConfigsAreResolvedToConfigMaps() throws InfrastructureException {
    List<GatewayRouteConfig> configs =
        Arrays.asList(
            new GatewayRouteConfig("g1", "s1", "1", "/1", Collections.singletonMap("a1", "v1")),
            new GatewayRouteConfig("g2", "s2", "2", "/2", Collections.singletonMap("a2", "v2")));
    when(internalEnvironment.getGatewayRouteConfigs()).thenReturn(configs);

    List<ConfigMap> configMaps =
        gatewayRouterResolver.resolve(runtimeIdentity, internalEnvironment);

    assertEquals(configMaps.size(), 2);
    verify(configGenerator, times(2)).generate();
    configMaps.forEach(
        cm -> {
          assertEquals(cm.getMetadata().getLabels(), GATEWAY_CONFIGMAP_LABELS);
          assertTrue(cm.getMetadata().getName().startsWith(WS_ID));
          assertEquals(cm.getMetadata().getAnnotations().size(), 1);
        });
    configs.forEach(c -> verify(configGenerator).addRouteConfig(c));
  }

  @Test
  public void testNoRouteConfigsReturnsNoConfigMaps() throws InfrastructureException {
    when(internalEnvironment.getGatewayRouteConfigs()).thenReturn(Collections.emptyList());

    List<ConfigMap> configMaps =
        gatewayRouterResolver.resolve(runtimeIdentity, internalEnvironment);

    assertTrue(configMaps.isEmpty());
  }
}
