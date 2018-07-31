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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerIngressBuilder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link IngressTlsProvisioner}.
 *
 * @author Ilya Buziuk
 * @author Sergii Leshchenko
 * @author Guy Daich
 */
@Listeners(MockitoTestNGListener.class)
public class IngressTlsProvisionerTest {

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
  private final String name = "IngressName";
  private final String serviceName = "ServiceName";
  private final String servicePort = "server-port";
  private final String host = "server-host";

  private final ExternalServerIngressBuilder externalServerIngressBuilder =
      new ExternalServerIngressBuilder();
  private final Ingress ingress =
      externalServerIngressBuilder
          .withHost(host)
          .withAnnotations(annotations)
          .withMachineName(machine)
          .withName(name)
          .withServers(servers)
          .withServiceName(serviceName)
          .withServicePort(servicePort)
          .build();

  @Test
  public void doNothingWhenTlsDisabled() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner = new IngressTlsProvisioner(false, "");

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    verify(k8sEnv, never()).getIngresses();
  }

  @Test
  public void provisionTlsForRoutesWhenTlsEnabledAndSecretProvided() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner = new IngressTlsProvisioner(true, "secretname");

    Map<String, Ingress> ingresses = new HashMap<>();
    ingresses.put("ingress", ingress);
    when(k8sEnv.getIngresses()).thenReturn(ingresses);

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    assertEquals(ingress.getSpec().getTls().size(), 1);
    assertEquals(ingress.getSpec().getTls().get(0).getHosts().size(), 1);
    assertEquals(ingress.getSpec().getTls().get(0).getHosts().get(0), host);
    assertEquals(ingress.getSpec().getTls().get(0).getSecretName(), "secretname");

    verifyIngressAndServersTLS();
  }

  @Test
  public void provisionTlsForRoutesWhenTlsEnabledAndSecretEmpty() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner = new IngressTlsProvisioner(true, "");

    Map<String, Ingress> ingresses = new HashMap<>();
    ingresses.put("ingress", ingress);
    when(k8sEnv.getIngresses()).thenReturn(ingresses);

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    assertEquals(ingress.getSpec().getTls().size(), 1);
    assertEquals(ingress.getSpec().getTls().get(0).getHosts().size(), 1);
    assertEquals(ingress.getSpec().getTls().get(0).getHosts().get(0), host);
    assertEquals(ingress.getSpec().getTls().get(0).getSecretName(), null);

    verifyIngressAndServersTLS();
  }

  @Test
  public void provisionTlsForRoutesWhenTlsEnabledAndSecretNull() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner = new IngressTlsProvisioner(true, null);

    Map<String, Ingress> ingresses = new HashMap<>();
    ingresses.put("ingress", ingress);
    when(k8sEnv.getIngresses()).thenReturn(ingresses);

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    assertEquals(ingress.getSpec().getTls().size(), 1);
    assertEquals(ingress.getSpec().getTls().get(0).getHosts().size(), 1);
    assertEquals(ingress.getSpec().getTls().get(0).getHosts().get(0), host);
    assertEquals(ingress.getSpec().getTls().get(0).getSecretName(), null);

    verifyIngressAndServersTLS();
  }

  private void verifyIngressAndServersTLS() {
    Map<String, ServerConfigImpl> ingressServers =
        Annotations.newDeserializer(ingress.getMetadata().getAnnotations()).servers();
    assertEquals(ingressServers.get("http-server").getProtocol(), "https");
    assertEquals(ingressServers.get("ws-server").getProtocol(), "wss");
  }
}
