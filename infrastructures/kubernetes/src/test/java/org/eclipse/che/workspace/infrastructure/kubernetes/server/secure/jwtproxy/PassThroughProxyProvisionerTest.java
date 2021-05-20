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
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static java.util.Collections.singleton;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.external.MultiHostExternalServiceExposureStrategy.MULTI_HOST_STRATEGY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServiceExposureStrategy;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ServiceExposureStrategyProvider;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.factory.JwtProxyConfigBuilderFactory;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class PassThroughProxyProvisionerTest {

  private static final String WORKSPACE_ID = "workspace123";
  private final RuntimeIdentity runtimeId =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env123", "owner123", "infraNamespace");

  // PassThroughProxyProvisioner shares much of the codebase with the JwtProxyProvisioner. We only
  // test the different behavior here, while the majority of the tests are present in the
  // JwtProxyProvisionerTest

  @Test
  public void shouldConfigureProxyWithExcludes() throws Exception {
    // given
    KubernetesEnvironment k8sEnv = KubernetesEnvironment.builder().build();
    JwtProxyConfigBuilderFactory configBuilderFactory = mock(JwtProxyConfigBuilderFactory.class);
    JwtProxyConfigBuilder configBuilder = mock(JwtProxyConfigBuilder.class);
    when(configBuilderFactory.create(any())).thenReturn(configBuilder);

    ServiceExposureStrategyProvider exposureStrategyProvider =
        mock(ServiceExposureStrategyProvider.class);
    when(exposureStrategyProvider.get()).thenReturn(mock(ExternalServiceExposureStrategy.class));
    when(exposureStrategyProvider.getMultiHostStrategy())
        .thenReturn(mock(ExternalServiceExposureStrategy.class));

    PassThroughProxyProvisioner passThroughProxyProvisioner =
        new PassThroughProxyProvisioner(
            configBuilderFactory,
            exposureStrategyProvider,
            new CookiePathStrategy(MULTI_HOST_STRATEGY),
            new MultiHostCookiePathStrategy(),
            "eclipse/che-jwtproxy",
            "10m",
            "128mb",
            "0.02",
            "0.5",
            "Always",
            runtimeId);

    Map<String, String> attrs = new HashMap<>();
    ServerConfig.setCookiesAuthEnabled(attrs, true);
    ServerConfig.setSecure(attrs, true);
    ServerConfigImpl server1 = new ServerConfigImpl("4401/tcp", "http", "/", attrs);

    ServicePort port = new ServicePort();
    port.setTargetPort(new IntOrString(8080));

    // when
    passThroughProxyProvisioner.expose(
        k8sEnv,
        podWithName(),
        "machine",
        "terminal",
        port,
        "TCP",
        false,
        ImmutableMap.of("server1", server1));

    // then
    verify(configBuilder)
        .addVerifierProxy(
            eq(4400), eq("http://terminal:8080"), eq(singleton("/")), eq(false), eq("/"), isNull());
  }

  private static PodData podWithName() {
    ObjectMeta meta = new ObjectMeta();
    meta.setName("a-pod-name");
    return new PodData(null, meta);
  }
}
