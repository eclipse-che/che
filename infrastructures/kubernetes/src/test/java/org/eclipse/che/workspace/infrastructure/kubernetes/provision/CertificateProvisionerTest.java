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
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner.CA_CERT_FILE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner.CERT_MOUNT_PATH;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner.CHE_SELF_SIGNED_CERT_SECRET_SUFFIX;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.CertificateProvisioner.CHE_SELF_SIGNED_CERT_VOLUME;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretVolumeSource;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link CertificateProvisioner}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class CertificateProvisionerTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String EXPECTED_CERT_NAME =
      WORKSPACE_ID + CHE_SELF_SIGNED_CERT_SECRET_SUFFIX;

  public static final String CERT_CONTENT = "--BEGIN FJASBNDF END";
  @Mock private RuntimeIdentity runtimeId;
  private CertificateProvisioner provisioner;
  private KubernetesEnvironment k8sEnv;

  @BeforeMethod
  public void setUp() {
    when(runtimeId.getWorkspaceId()).thenReturn(WORKSPACE_ID);

    provisioner = new CertificateProvisioner("--BEGIN FJASBNDF END");
    k8sEnv = KubernetesEnvironment.builder().build();
  }

  @Test
  public void shouldReturnFalseIfCertificateIsNotSpecifiedOnIsConfiguredInvoking() {
    // given
    provisioner = new CertificateProvisioner("");

    // when
    boolean configured = provisioner.isConfigured();

    // then
    assertFalse(configured);
  }

  @Test
  public void shouldReturnTrueIfCertificateIsSpecifiedOnIsConfiguredInvoking() {
    // given
    provisioner = new CertificateProvisioner(CERT_CONTENT);

    // when
    boolean configured = provisioner.isConfigured();

    // then
    assertTrue(configured);
  }

  @Test
  public void shouldReturnCertPathFile() {
    // when
    String certPath = provisioner.getCertPath();

    // then
    assertEquals(certPath, "/tmp/che/secret/ca.crt");
  }

  @Test
  public void shouldAddSecretWithCertificateIntoEnvironment() throws Exception {
    // when
    provisioner.provision(k8sEnv, runtimeId);

    // then
    Map<String, Secret> secrets = k8sEnv.getSecrets();
    assertEquals(secrets.size(), 1);

    Secret certSecret = secrets.get(EXPECTED_CERT_NAME);
    assertNotNull(certSecret);
    assertEquals(certSecret.getMetadata().getName(), EXPECTED_CERT_NAME);
    assertEquals(certSecret.getStringData().get(CA_CERT_FILE), CERT_CONTENT);
  }

  @Test
  public void shouldAddVolumeAndVolumeMountsToPodsAndContainersInEnvironment() throws Exception {
    // given
    k8sEnv.addPod(createPod("pod"));
    k8sEnv.addPod(createPod("pod2"));

    // when
    provisioner.provision(k8sEnv, runtimeId);

    // then
    for (Pod pod : k8sEnv.getPodsCopy().values()) {
      verifyVolumeIsPresent(pod);

      for (Container container : pod.getSpec().getInitContainers()) {
        verifyVolumeMountIsPresent(container);
      }

      for (Container container : pod.getSpec().getContainers()) {
        verifyVolumeMountIsPresent(container);
      }
    }
  }

  @Test
  public void
      shouldNotAddVolumeAndVolumeMountsToPodsAndContainersInEnvironmentIfCertIsNotConfigured()
          throws Exception {
    // given
    provisioner = new CertificateProvisioner("");
    k8sEnv.addPod(createPod("pod"));
    k8sEnv.addPod(createPod("pod2"));

    // when
    provisioner.provision(k8sEnv, runtimeId);

    // then
    for (Pod pod : k8sEnv.getPodsCopy().values()) {
      assertTrue(pod.getSpec().getVolumes().isEmpty());
      for (Container container : pod.getSpec().getContainers()) {
        assertTrue(container.getVolumeMounts().isEmpty());
      }
    }
  }

  @Test
  public void shouldNotAddVolumeButAddMountToInjectablePod() throws Exception {
    // given
    provisioner = new CertificateProvisioner(CERT_CONTENT);
    k8sEnv.addPod(createPod("pod"));
    k8sEnv.addInjectablePod("r", "i", createPod("injected"));

    // when
    provisioner.provision(k8sEnv, runtimeId);

    // then
    Pod pod = k8sEnv.getPodsCopy().get("pod");
    Pod injected = k8sEnv.getInjectablePodsCopy().getOrDefault("r", emptyMap()).get("i");

    assertNotNull(pod);
    assertNotNull(injected);

    verifyVolumeIsPresent(pod);
    pod.getSpec().getContainers().forEach(this::verifyVolumeMountIsPresent);

    assertTrue(injected.getSpec().getVolumes().isEmpty());
    injected.getSpec().getContainers().forEach(this::verifyVolumeMountIsPresent);
  }

  private void verifyVolumeIsPresent(Pod pod) {
    List<Volume> podVolumes = pod.getSpec().getVolumes();
    assertEquals(podVolumes.size(), 1);
    Volume certVolume = podVolumes.get(0);
    assertEquals(certVolume.getName(), CHE_SELF_SIGNED_CERT_VOLUME);
    SecretVolumeSource volumeSecret = certVolume.getSecret();
    assertNotNull(volumeSecret);
    assertEquals(volumeSecret.getSecretName(), EXPECTED_CERT_NAME);
  }

  private void verifyVolumeMountIsPresent(Container container) {
    List<VolumeMount> volumeMounts = container.getVolumeMounts();
    assertEquals(volumeMounts.size(), 1);
    VolumeMount volumeMount = volumeMounts.get(0);
    assertEquals(volumeMount.getName(), CHE_SELF_SIGNED_CERT_VOLUME);
    assertTrue(volumeMount.getReadOnly());
    assertEquals(volumeMount.getMountPath(), CERT_MOUNT_PATH);
  }

  private Pod createPod(String podName) {
    return new PodBuilder()
        .withNewMetadata()
        .withName(podName)
        .endMetadata()
        .withNewSpec()
        .withInitContainers(new ContainerBuilder().build())
        .withContainers(new ContainerBuilder().build(), new ContainerBuilder().build())
        .endSpec()
        .build();
  }
}
