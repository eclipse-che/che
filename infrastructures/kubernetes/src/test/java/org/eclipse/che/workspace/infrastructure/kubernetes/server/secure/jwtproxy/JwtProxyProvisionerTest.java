/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy;

import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_PREFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.KubernetesServerExposer.SERVER_UNIQUE_PART_SIZE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_CONFIG_FILE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.JWT_PROXY_PUBLIC_KEY_FILE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.PUBLIC_KEY_FOOTER;
import static org.eclipse.che.workspace.infrastructure.kubernetes.server.secure.jwtproxy.JwtProxyProvisioner.PUBLIC_KEY_HEADER;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Collections;
import java.util.regex.Pattern;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link JwtProxyProvisioner}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class JwtProxyProvisionerTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final Pattern JWTPROXY_SERVICE_NAME_PATTERN =
      Pattern.compile(SERVER_PREFIX + "\\w{" + SERVER_UNIQUE_PART_SIZE + "}-jwtproxy");
  private final RuntimeIdentity runtimeId =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env123", "owner123");

  @Mock private SignatureKeyManager signatureKeyManager;
  private KeyPair keyPair;
  @Mock private PublicKey publicKey;

  private JwtProxyProvisioner jwtProxyProvisioner;
  private KubernetesEnvironment k8sEnv;

  @BeforeMethod
  public void setUp() {
    keyPair = new KeyPair(publicKey, null);
    when(signatureKeyManager.getKeyPair()).thenReturn(keyPair);
    when(publicKey.getEncoded()).thenReturn("publickey".getBytes());

    jwtProxyProvisioner = new JwtProxyProvisioner(runtimeId, signatureKeyManager);
    k8sEnv = KubernetesEnvironment.builder().build();
  }

  @Test
  public void shouldReturnGeneratedJwtProxyServiceName() {
    // when
    String jwtProxyServiceName = jwtProxyProvisioner.getServiceName();

    // then
    assertTrue(JWTPROXY_SERVICE_NAME_PATTERN.matcher(jwtProxyServiceName).matches());
  }

  @Test
  public void shouldReturnGeneratedJwtProxyConfigMapName() {
    // when
    String jwtProxyConfigMap = jwtProxyProvisioner.getConfigMapName();

    // then
    assertEquals(jwtProxyConfigMap, "jwtproxy-config-" + WORKSPACE_ID);
  }

  @Test
  public void shouldProvisionJwtProxyRelatedObjectsIntoKubernetesEnvironment() throws Exception {
    // when
    jwtProxyProvisioner.expose(k8sEnv, "terminal", 4401, "TCP", Collections.EMPTY_MAP);

    // then
    InternalMachineConfig jwtProxyMachine =
        k8sEnv.getMachines().get(JwtProxyProvisioner.JWT_PROXY_MACHINE_NAME);
    assertNotNull(jwtProxyMachine);

    ConfigMap configMap = k8sEnv.getConfigMaps().get(jwtProxyProvisioner.getConfigMapName());
    assertNotNull(configMap);
    assertEquals(
        configMap.getData().get(JWT_PROXY_PUBLIC_KEY_FILE),
        PUBLIC_KEY_HEADER
            + Base64.getEncoder().encodeToString("publickey".getBytes())
            + PUBLIC_KEY_FOOTER);
    assertNotNull(configMap.getData().get(JWT_PROXY_CONFIG_FILE));

    Pod jwtProxyPod = k8sEnv.getPods().get("che-jwtproxy");
    assertNotNull(jwtProxyPod);

    Service jwtProxyService = k8sEnv.getServices().get(jwtProxyProvisioner.getServiceName());
    assertNotNull(jwtProxyService);
  }
}
