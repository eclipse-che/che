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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import static com.google.common.collect.ImmutableMap.of;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.DoneableConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.CheInstallationLocation;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProjectFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class OpenshiftTrustedCAProvisionerTest {

  private static final String POD_NAME = "testPod";
  private static final String CONFIGMAP_NAME = "ca-certs";
  private static final String CONFIGMAP_LABELS = "foo=bar";
  private static final String CERTIFICATE_MOUNT_PATH = "/certs";
  private static final String CHE_SERVER_NAMESPACE = "testCheServerNamespace";
  private static final String INJECTED_CA_BUNDLE_KEY = "ca-bundle.crt";
  private static final String INJECTED_CA_BUNDLE_VALUE = "ca-bundle.crt.content";
  private static final String MANUAL_CA_BUNDLE_KEY = "manual-ca.crt";
  private static final String MANUAL_CA_BUNDLE_VALUE = "manual-ca.crt.content";

  @Mock CheServerKubernetesClientFactory clientFactory;

  @Mock private RuntimeIdentity runtimeID;
  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private OpenShiftProject openShiftProject;
  @Mock private OpenShiftProjectFactory openShiftProjectFactory;
  @Mock private KubernetesConfigsMaps kubernetesConfigsMaps;
  @Mock private CheInstallationLocation cheInstallationLocation;

  @Mock
  private MixedOperation<
          ConfigMap, ConfigMapList, Resource<ConfigMap>>
      cheServerConfigMapGetter1;

  @Mock
  private NonNamespaceOperation<
          ConfigMap, ConfigMapList, Resource<ConfigMap>>
      cheServerConfigMapGetter2;

  @Mock private Resource<ConfigMap> cheServerConfigMapResource;
  @Mock private ConfigMap cheServerConfigMap;
  @Mock private ObjectMeta cheServerConfigMapMetadata;

  private Map<String, String> cheServerConfigMapData =
      ImmutableMap.of(
          INJECTED_CA_BUNDLE_KEY, INJECTED_CA_BUNDLE_VALUE,
          MANUAL_CA_BUNDLE_KEY, MANUAL_CA_BUNDLE_VALUE);

  private Map<String, String> cheServerConfigMapAnnotations =
      ImmutableMap.of(
          "testCheServerConfigMapAnnotationsKey", "testCheServerConfigMapAnnotationsValue");

  @Mock private OpenShiftClient k8sClient;

  Map<String, ConfigMap> envConfigMaps = new HashMap<>();

  private OpenshiftTrustedCAProvisioner trustedCAProvisioner;

  @BeforeMethod
  public void setup() throws Exception {
    lenient().when(clientFactory.create()).thenReturn(k8sClient);
    lenient().when(openShiftProjectFactory.getOrCreate(runtimeID)).thenReturn(openShiftProject);
    lenient().when(openShiftProject.configMaps()).thenReturn(kubernetesConfigsMaps);
    lenient()
        .when(cheInstallationLocation.getInstallationLocationNamespace())
        .thenReturn(CHE_SERVER_NAMESPACE);
    lenient().when(k8sEnv.getConfigMaps()).thenReturn(envConfigMaps);
    lenient().when(k8sClient.configMaps()).thenReturn(cheServerConfigMapGetter1);
    lenient()
        .when(cheServerConfigMapGetter1.inNamespace(CHE_SERVER_NAMESPACE))
        .thenReturn(cheServerConfigMapGetter2);
    lenient()
        .when(cheServerConfigMapGetter2.withName(anyString()))
        .thenReturn(cheServerConfigMapResource);
    lenient().when(cheServerConfigMap.getData()).thenReturn(cheServerConfigMapData);
    lenient().when(cheServerConfigMap.getMetadata()).thenReturn(cheServerConfigMapMetadata);
    lenient()
        .when(cheServerConfigMapMetadata.getAnnotations())
        .thenReturn(cheServerConfigMapAnnotations);

    this.trustedCAProvisioner =
        new OpenshiftTrustedCAProvisioner(
            CONFIGMAP_NAME,
            CONFIGMAP_NAME,
            CERTIFICATE_MOUNT_PATH,
            CONFIGMAP_LABELS,
            cheInstallationLocation,
            openShiftProjectFactory,
            clientFactory);
  }

  @Test
  public void shouldDoNothingIfCAStoreIsNotInitialized() throws Exception {
    OpenshiftTrustedCAProvisioner localProvisioner =
        new OpenshiftTrustedCAProvisioner(
            null,
            CONFIGMAP_NAME,
            CERTIFICATE_MOUNT_PATH,
            CONFIGMAP_LABELS,
            cheInstallationLocation,
            openShiftProjectFactory,
            clientFactory);

    localProvisioner.provision(k8sEnv, runtimeID);
    verifyZeroInteractions(k8sEnv, openShiftProject, clientFactory, runtimeID, clientFactory);
  }

  @Test
  public void shouldProvisionTrustStoreMapAndMountIt() throws Exception {
    Pod pod = newPod();
    PodData podData = new PodData(pod.getSpec(), pod.getMetadata());
    doReturn(of(POD_NAME, podData)).when(k8sEnv).getPodsData();
    lenient().when(cheServerConfigMapResource.get()).thenReturn(cheServerConfigMap);

    trustedCAProvisioner.provision(k8sEnv, runtimeID);

    assertEquals(envConfigMaps.size(), 1);
    assertTrue(envConfigMaps.get(CONFIGMAP_NAME).getMetadata().getLabels().containsKey("foo"));
    assertEquals(envConfigMaps.get(CONFIGMAP_NAME).getMetadata().getLabels().get("foo"), "bar");
    assertEquals(
        envConfigMaps.get(CONFIGMAP_NAME).getMetadata().getAnnotations(),
        cheServerConfigMapAnnotations);
    assertEquals(envConfigMaps.get(CONFIGMAP_NAME).getData(), cheServerConfigMapData);

    PodSpec podSpec = pod.getSpec();
    assertEquals(podSpec.getVolumes().size(), 1);
    assertEquals(podSpec.getVolumes().get(0).getConfigMap().getName(), CONFIGMAP_NAME);
    assertTrue(
        podSpec
            .getContainers()
            .stream()
            .allMatch(
                c -> c.getVolumeMounts().get(0).getMountPath().equals(CERTIFICATE_MOUNT_PATH)));
  }

  private static Pod newPod() {
    return new PodBuilder()
        .withMetadata(new ObjectMetaBuilder().withName(POD_NAME).build())
        .withNewSpec()
        .withContainers(new ContainerBuilder().build(), new ContainerBuilder().build())
        .endSpec()
        .build();
  }
}
