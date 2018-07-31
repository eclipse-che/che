/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerExposerStrategy;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link JwtProxySecureServerExposer}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class JwtProxySecureServerExposerTest {

  private static final String MACHINE_SERVICE_NAME = "service123";
  private static final String MACHINE_NAME = "machine123";
  public static final String JWT_PROXY_SERVICE_NAME = "jwtProxyServiceName";

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private JwtProxyProvisioner jwtProxyProvisioner;
  @Mock private ExternalServerExposerStrategy<KubernetesEnvironment> externalServerExposer;

  private JwtProxySecureServerExposer<KubernetesEnvironment> secureServerExposer;

  @BeforeMethod
  public void setUp() {
    secureServerExposer =
        new JwtProxySecureServerExposer<>(jwtProxyProvisioner, externalServerExposer);
  }

  @Test
  public void shouldExposeSecureServersWithNewJwtProxyServicePort() throws Exception {
    // given
    ServicePort machineServicePort = new ServicePort();
    machineServicePort.setTargetPort(new IntOrString(8080));
    machineServicePort.setProtocol("TCP");
    Map<String, ServerConfig> servers =
        ImmutableMap.of(
            "server1",
            new ServerConfigImpl("8080/tcp", "http", "/api", ImmutableMap.of("secure", "true")),
            "server2",
            new ServerConfigImpl("8080/tcp", "ws", "/connect", ImmutableMap.of("secure", "true")));

    ServicePort jwtProxyServicePort = new ServicePort();
    doReturn(jwtProxyServicePort)
        .when(jwtProxyProvisioner)
        .expose(any(), anyString(), anyInt(), anyString(), anyMap());

    when(jwtProxyProvisioner.getServiceName()).thenReturn(JWT_PROXY_SERVICE_NAME);

    // when
    secureServerExposer.expose(
        k8sEnv, MACHINE_NAME, MACHINE_SERVICE_NAME, machineServicePort, servers);

    // then
    verify(jwtProxyProvisioner).expose(k8sEnv, MACHINE_SERVICE_NAME, 8080, "TCP", servers);
    verify(externalServerExposer)
        .expose(k8sEnv, MACHINE_NAME, JWT_PROXY_SERVICE_NAME, jwtProxyServicePort, servers);
  }
}
