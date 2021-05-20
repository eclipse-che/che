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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.IngressServerExposer;
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
  @Mock private IngressServerExposer externalServerExposer;

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
        .expose(any(), any(), anyString(), anyString(), any(), anyString(), anyBoolean(), any());

    when(jwtProxyProvisioner.getServiceName()).thenReturn(JWT_PROXY_SERVICE_NAME);

    when(externalServerExposer.getStrategyConformingServers(eq(servers))).thenReturn(servers);

    // when
    secureServerExposer.expose(
        k8sEnv, null, MACHINE_NAME, MACHINE_SERVICE_NAME, null, machineServicePort, servers);

    // then
    verify(jwtProxyProvisioner)
        .expose(
            eq(k8sEnv),
            any(),
            anyString(),
            eq(MACHINE_SERVICE_NAME),
            eq(machineServicePort),
            eq("TCP"),
            eq(false),
            any());
    verify(externalServerExposer)
        .expose(
            eq(k8sEnv),
            eq(MACHINE_NAME),
            eq(JWT_PROXY_SERVICE_NAME),
            isNull(),
            eq(jwtProxyServicePort),
            eq(servers));
  }

  @Test
  public void shouldUseMultiHostStrategyForSubdomainRequiringServers() throws Exception {
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

    Map<String, ServerConfig> conformingServers =
        Collections.singletonMap("server1", servers.get("server1"));
    Map<String, ServerConfig> subdomainServers =
        Collections.singletonMap("server2", servers.get("server2"));

    ServicePort jwtProxyServicePort = new ServicePort();
    doReturn(jwtProxyServicePort)
        .when(jwtProxyProvisioner)
        .expose(any(), any(), anyString(), anyString(), any(), anyString(), anyBoolean(), any());

    when(jwtProxyProvisioner.getServiceName()).thenReturn(JWT_PROXY_SERVICE_NAME);

    when(externalServerExposer.getStrategyConformingServers(eq(servers)))
        .thenReturn(conformingServers);
    when(externalServerExposer.getServersRequiringSubdomain(eq(servers)))
        .thenReturn(subdomainServers);

    // when
    secureServerExposer.expose(
        k8sEnv, null, MACHINE_NAME, MACHINE_SERVICE_NAME, null, machineServicePort, servers);

    // then
    verify(jwtProxyProvisioner)
        .expose(
            eq(k8sEnv),
            any(),
            anyString(),
            eq(MACHINE_SERVICE_NAME),
            eq(machineServicePort),
            eq("TCP"),
            eq(false),
            any());
    verify(jwtProxyProvisioner)
        .expose(
            eq(k8sEnv),
            any(),
            anyString(),
            eq(MACHINE_SERVICE_NAME),
            eq(machineServicePort),
            eq("TCP"),
            eq(true),
            any());
    verify(externalServerExposer)
        .expose(
            eq(k8sEnv),
            eq(MACHINE_NAME),
            eq(JWT_PROXY_SERVICE_NAME),
            isNull(),
            eq(jwtProxyServicePort),
            eq(conformingServers));
    verify(externalServerExposer)
        .expose(
            eq(k8sEnv),
            eq(MACHINE_NAME),
            eq(JWT_PROXY_SERVICE_NAME),
            isNull(),
            eq(jwtProxyServicePort),
            eq(subdomainServers));
  }
}
