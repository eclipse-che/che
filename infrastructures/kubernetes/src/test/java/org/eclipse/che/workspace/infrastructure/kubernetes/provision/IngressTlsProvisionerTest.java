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
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.IngressTlsProvisioner.TLS_SECRET_TYPE;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.Annotations;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.server.external.ExternalServerIngressBuilder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
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

  @BeforeMethod
  public void setUp() {
    lenient().when(runtimeIdentity.getWorkspaceId()).thenReturn(WORKSPACE_ID);
  }

  @Test
  public void doNothingWhenTlsDisabled() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner = new IngressTlsProvisioner(false, "", "", "");

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    verify(k8sEnv, never()).getIngresses();
  }

  @Test
  public void provisionTlsForIngressesWhenTlsEnabledAndSecretProvided() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner =
        new IngressTlsProvisioner(true, "secretname", "", "");

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

    verifyServersTLS(ingress.getMetadata().getAnnotations());
  }

  @Test
  public void provisionTlsForIngressesWhenTlsEnabledAndSecretEmpty() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner = new IngressTlsProvisioner(true, "", "", "");

    Map<String, Ingress> ingresses = new HashMap<>();
    ingresses.put("ingress", ingress);
    when(k8sEnv.getIngresses()).thenReturn(ingresses);

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    assertEquals(ingress.getSpec().getTls().size(), 1);
    assertEquals(ingress.getSpec().getTls().get(0).getHosts().size(), 1);
    assertEquals(ingress.getSpec().getTls().get(0).getHosts().get(0), host);
    assertNull(ingress.getSpec().getTls().get(0).getSecretName());

    verifyServersTLS(ingress.getMetadata().getAnnotations());
  }

  @Test
  public void provisionTlsForIngressesWhenTlsEnabledAndSecretNull() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner = new IngressTlsProvisioner(true, null, "", "");

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

    verifyServersTLS(ingress.getMetadata().getAnnotations());
  }

  @Test
  public void provisionTlsSecretWhenTlsCertAndKeyAreSpecified() throws Exception {
    // given
    IngressTlsProvisioner ingressTlsProvisioner =
        new IngressTlsProvisioner(true, "ws-tls-secret", "cert", "key");

    Map<String, Secret> secrets = new HashMap<>();
    when(k8sEnv.getSecrets()).thenReturn(secrets);

    // when
    ingressTlsProvisioner.provision(k8sEnv, runtimeIdentity);

    // then
    assertEquals(k8sEnv.getSecrets().size(), 1);
    Secret tlsSecret = k8sEnv.getSecrets().get(runtimeIdentity.getWorkspaceId() + "-ws-tls-secret");
    assertNotNull(tlsSecret);
    assertEquals(tlsSecret.getStringData().get("tls.crt"), "cert");
    assertEquals(tlsSecret.getStringData().get("tls.key"), "key");

    assertEquals(tlsSecret.getType(), TLS_SECRET_TYPE);
    verifyServersTLS(ingress.getMetadata().getAnnotations());
  }

  @Test(
      expectedExceptions = ConfigurationException.class,
      expectedExceptionsMessageRegExp =
          "None or both of `che.infra.kubernetes.tls_cert` and "
              + "`che.infra.kubernetes.tls_key` must be configured with non-null value\\.")
  public void shouldThrowAnExceptionIfOnlyOneIfTlsCertOrKeyIsConfigured() {
    // given
    new IngressTlsProvisioner(true, "secret", "test", "");
  }

  private void verifyServersTLS(Map<String, String> annotations) {
    Map<String, ServerConfigImpl> servers = Annotations.newDeserializer(annotations).servers();
    assertEquals(servers.get("http-server").getProtocol(), "https");
    assertEquals(servers.get("ws-server").getProtocol(), "wss");
  }
}
