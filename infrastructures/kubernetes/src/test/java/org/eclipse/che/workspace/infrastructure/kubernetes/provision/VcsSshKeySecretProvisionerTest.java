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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Secret;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link VcsSshKeysProvisioner}.
 *
 * @author Vitalii Parfonov
 */
@Listeners(MockitoTestNGListener.class)
public class VcsSshKeySecretProvisionerTest {

  private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  @Mock private SshManager sshManager;

  @Mock private Pod pod;

  @Mock private PodSpec podSpec;

  private final String someUser = "someuser";

  private VcsSshKeysProvisioner vcsSshKeysProvisioner;

  @BeforeMethod
  public void setup() {
    when(runtimeIdentity.getOwnerId()).thenReturn(someUser);
    when(runtimeIdentity.getWorkspaceId()).thenReturn("wksp");
    k8sEnv = KubernetesEnvironment.builder().build();
    ObjectMeta podMeta = new ObjectMetaBuilder().withName("wksp").build();
    when(pod.getMetadata()).thenReturn(podMeta);
    when(pod.getSpec()).thenReturn(podSpec);
    k8sEnv.addPod(pod);
    vcsSshKeysProvisioner = new VcsSshKeysProvisioner(sshManager);
  }

  @Test
  public void doNotDoAnythingIfNoSshKeys() throws Exception {
    when(sshManager.getPairs(someUser, "vcs")).thenReturn(Collections.emptyList());

    vcsSshKeysProvisioner.provision(k8sEnv, runtimeIdentity);

    assertTrue(k8sEnv.getSecrets().isEmpty());
    verifyZeroInteractions(podSpec);
  }

  @Test
  public void addSshKeysConfigInPod() throws Exception {
    String keyName1 = UUID.randomUUID().toString();
    String keyName2 = "default-" + UUID.randomUUID().toString();
    String keyName3 = "github.com";
    when(sshManager.getPairs(someUser, "vcs"))
        .thenReturn(
            ImmutableList.of(
                new SshPairImpl(someUser, "vcs", keyName1, "public", "private"),
                new SshPairImpl(someUser, "vcs", keyName2, "public", "private"),
                new SshPairImpl(someUser, "vcs", keyName3, "public", "private")));

    vcsSshKeysProvisioner.provision(k8sEnv, runtimeIdentity);

    verify(podSpec, times(4)).getVolumes();
    verify(podSpec, times(4)).getContainers();

    Secret secret = k8sEnv.getSecrets().get("wksp-" + keyName1);
    assertNotNull(secret);
    assertEquals(secret.getType(), "kubernetes.io/ssh-auth");

    String key = secret.getData().get("ssh-privatekey");
    assertNotNull(key);

    Map<String, ConfigMap> configMaps = k8sEnv.getConfigMaps();
    assertNotNull(configMaps);
    assertTrue(configMaps.containsKey("wksp-sshconfigmap"));

    ConfigMap sshConfigMap = configMaps.get("wksp-sshconfigmap");
    assertNotNull(sshConfigMap);

    Map<String, String> mapData = sshConfigMap.getData();
    assertNotNull(mapData);
    assertTrue(mapData.containsKey("ssh_config"));

    String sshConfig = mapData.get("ssh_config");
    assertTrue(sshConfig.contains("host " + keyName1));
    assertTrue(sshConfig.contains("IdentityFile " + "/etc/ssh/" + keyName1 + "/ssh-privatekey"));

    assertTrue(sshConfig.contains("host *"));
    assertTrue(sshConfig.contains("IdentityFile " + "/etc/ssh/" + keyName2 + "/ssh-privatekey"));

    assertTrue(sshConfig.contains("host github.com"));
    assertTrue(sshConfig.contains("IdentityFile /etc/ssh/github-com/ssh-privatekey"));
  }
}
