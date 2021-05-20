/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.environment.PodMerger.DEPLOYMENT_NAME_LABEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.PodMerger;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftEnvironmentFactory}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftEnvironmentFactoryTest {

  private static final String MACHINE_NAME_1 = "machine1";
  private static final String MACHINE_NAME_2 = "machine2";

  private OpenShiftEnvironmentFactory osEnvFactory;

  @Mock private OpenShiftEnvironmentValidator openShiftEnvValidator;
  @Mock private InternalRecipe internalRecipe;
  @Mock private InternalMachineConfig machineConfig1;
  @Mock private InternalMachineConfig machineConfig2;
  @Mock private KubernetesRecipeParser k8sRecipeParser;
  @Mock private PodMerger podMerger;

  private Map<String, InternalMachineConfig> machines;

  @BeforeMethod
  public void setup() throws Exception {
    osEnvFactory =
        new OpenShiftEnvironmentFactory(
            null, null, openShiftEnvValidator, k8sRecipeParser, podMerger);
    machines = ImmutableMap.of(MACHINE_NAME_1, machineConfig1, MACHINE_NAME_2, machineConfig2);
  }

  @Test
  public void shouldCreateOpenShiftEnvironmentWithServicesFromRecipe() throws Exception {
    // given
    Service service1 =
        new ServiceBuilder().withNewMetadata().withName("service1").endMetadata().build();
    Service service2 =
        new ServiceBuilder().withNewMetadata().withName("service2").endMetadata().build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(asList(service1, service2));

    // when
    KubernetesEnvironment osEnv = osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    // then
    assertEquals(osEnv.getServices().size(), 2);
    assertEquals(osEnv.getServices().get("service1"), service1);
    assertEquals(osEnv.getServices().get("service2"), service2);
  }

  @Test
  public void shouldCreateOpenShiftEnvironmentWithPVCsFromRecipe() throws Exception {
    // given
    PersistentVolumeClaim pvc1 =
        new PersistentVolumeClaimBuilder().withNewMetadata().withName("pvc1").endMetadata().build();
    PersistentVolumeClaim pvc2 =
        new PersistentVolumeClaimBuilder().withNewMetadata().withName("pvc2").endMetadata().build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(asList(pvc1, pvc2));

    // when
    OpenShiftEnvironment osEnv = osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    // then
    assertEquals(osEnv.getPersistentVolumeClaims().size(), 2);
    assertEquals(osEnv.getPersistentVolumeClaims().get("pvc1"), pvc1);
    assertEquals(osEnv.getPersistentVolumeClaims().get("pvc2"), pvc2);
  }

  @Test
  public void addRoutesWhenRecipeContainsThem() throws Exception {
    Route route = new RouteBuilder().withNewMetadata().withName("test-route").endMetadata().build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(singletonList(route));

    final OpenShiftEnvironment parsed =
        osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertEquals(parsed.getRoutes().size(), 1);
    assertEquals(
        parsed.getRoutes().get("test-route").getMetadata().getName(),
        route.getMetadata().getName());
  }

  @Test
  public void addSecretsWhenRecipeContainsThem() throws Exception {
    Secret secret =
        new SecretBuilder().withNewMetadata().withName("test-secret").endMetadata().build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(singletonList(secret));

    final OpenShiftEnvironment parsed =
        osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertEquals(parsed.getSecrets().size(), 1);
    assertEquals(
        parsed.getSecrets().get("test-secret").getMetadata().getName(),
        secret.getMetadata().getName());
  }

  @Test
  public void addConfigMapsWhenRecipeContainsThem() throws Exception {
    ConfigMap configMap =
        new ConfigMapBuilder().withNewMetadata().withName("test-configmap").endMetadata().build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(singletonList(configMap));

    final KubernetesEnvironment parsed =
        osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertEquals(parsed.getConfigMaps().size(), 1);
    assertEquals(
        parsed.getConfigMaps().values().iterator().next().getMetadata().getName(),
        configMap.getMetadata().getName());
  }

  @Test
  public void addPodsWhenRecipeContainsThem() throws Exception {
    // given
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName("pod")
            .endMetadata()
            .withSpec(new PodSpec())
            .build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(singletonList(pod));

    // when
    KubernetesEnvironment osEnv = osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    // then
    assertEquals(osEnv.getPodsCopy().size(), 1);
    assertEquals(osEnv.getPodsCopy().get("pod"), pod);

    assertEquals(osEnv.getPodsData().size(), 1);
    assertEquals(osEnv.getPodsData().get("pod").getMetadata(), pod.getMetadata());
    assertEquals(osEnv.getPodsData().get("pod").getSpec(), pod.getSpec());
  }

  @Test
  public void addDeploymentsWhenRecipeContainsThem() throws Exception {
    // given
    PodTemplateSpec podTemplate =
        new PodTemplateSpecBuilder()
            .withNewMetadata()
            .withName("deployment-pod")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("deployment-test")
            .endMetadata()
            .withNewSpec()
            .withTemplate(podTemplate)
            .endSpec()
            .build();

    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(singletonList(deployment));

    // when
    final KubernetesEnvironment osEnv =
        osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    // then
    assertEquals(osEnv.getDeploymentsCopy().size(), 1);
    assertEquals(osEnv.getDeploymentsCopy().get("deployment-test"), deployment);

    assertEquals(osEnv.getPodsData().size(), 1);
    assertEquals(
        osEnv.getPodsData().get("deployment-test").getMetadata(), podTemplate.getMetadata());
    assertEquals(osEnv.getPodsData().get("deployment-test").getSpec(), podTemplate.getSpec());
  }

  @Test
  public void shouldUseDeploymentNameAsPodTemplateNameIfItIsMissing() throws Exception {
    // given
    PodTemplateSpec podTemplate = new PodTemplateSpecBuilder().withNewSpec().endSpec().build();
    Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("deployment-test")
            .endMetadata()
            .withNewSpec()
            .withTemplate(podTemplate)
            .endSpec()
            .build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(asList(deployment));

    // when
    final KubernetesEnvironment k8sEnv =
        osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    // then
    Deployment deploymentTest = k8sEnv.getDeploymentsCopy().get("deployment-test");
    assertNotNull(deploymentTest);
    PodTemplateSpec resultPodTemplate = deploymentTest.getSpec().getTemplate();
    assertEquals(resultPodTemplate.getMetadata().getName(), "deployment-test");
  }

  @Test
  public void shouldMergeDeploymentAndPodIntoOneDeployment() throws Exception {
    // given
    PodTemplateSpec podTemplate =
        new PodTemplateSpecBuilder()
            .withNewMetadata()
            .withName("deployment-pod")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("deployment-test")
            .endMetadata()
            .withNewSpec()
            .withTemplate(podTemplate)
            .endSpec()
            .build();
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName("bare-pod")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(asList(deployment, pod));
    Deployment merged = createEmptyDeployment("merged");
    when(podMerger.merge(any())).thenReturn(merged);

    // when
    final KubernetesEnvironment k8sEnv =
        osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    // then
    verify(podMerger).merge(asList(new PodData(pod), new PodData(deployment)));
    assertEquals(k8sEnv.getPodsData().size(), 1);

    assertTrue(k8sEnv.getPodsCopy().isEmpty());

    assertEquals(k8sEnv.getDeploymentsCopy().size(), 1);
    assertEquals(k8sEnv.getDeploymentsCopy().get("merged"), merged);
  }

  @Test
  public void shouldReconfigureServiceToMatchMergedDeployment() throws Exception {
    // given
    Pod pod1 =
        new PodBuilder()
            .withNewMetadata()
            .withName("bare-pod1")
            .withLabels(ImmutableMap.of("name", "pod1"))
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    Pod pod2 =
        new PodBuilder()
            .withNewMetadata()
            .withName("bare-pod2")
            .withLabels(ImmutableMap.of("name", "pod2"))
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    Service service1 =
        new ServiceBuilder()
            .withNewMetadata()
            .withName("pod1-service")
            .endMetadata()
            .withNewSpec()
            .withSelector(ImmutableMap.of("name", "pod1"))
            .endSpec()
            .build();
    Service service2 =
        new ServiceBuilder()
            .withNewMetadata()
            .withName("pod2-service")
            .endMetadata()
            .withNewSpec()
            .withSelector(ImmutableMap.of("name", "pod2"))
            .endSpec()
            .build();
    when(k8sRecipeParser.parse(any(InternalRecipe.class)))
        .thenReturn(asList(pod1, pod2, service1, service2));
    Deployment merged = createEmptyDeployment("merged");
    when(podMerger.merge(any())).thenReturn(merged);

    // when
    final KubernetesEnvironment k8sEnv =
        osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    // then
    verify(podMerger).merge(asList(new PodData(pod1), new PodData(pod2)));
    PodData mergedPodData = k8sEnv.getPodsData().get("merged");
    assertEquals(mergedPodData.getMetadata().getLabels().get(DEPLOYMENT_NAME_LABEL), "merged");
    assertTrue(
        k8sEnv
            .getServices()
            .values()
            .stream()
            .allMatch(
                s ->
                    ImmutableMap.of(DEPLOYMENT_NAME_LABEL, "merged")
                        .equals(s.getSpec().getSelector())));
  }

  @Test(expectedExceptions = ValidationException.class)
  public void exceptionOnRecipeLoadError() throws Exception {
    when(k8sRecipeParser.parse(any(InternalRecipe.class)))
        .thenThrow(new ValidationException("Could not parse recipe"));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Environment can not contain two 'Service' objects with the same name 'db'")
  public void exceptionOnObjectsWithTheSameNameAndKind() throws Exception {
    HasMetadata object1 =
        new ServiceBuilder().withNewMetadata().withName("db").endMetadata().build();
    HasMetadata object2 =
        new ServiceBuilder().withNewMetadata().withName("db").endMetadata().build();

    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(asList(object1, object2));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Environment contains object without specified kind field")
  public void exceptionOnObjectWithNoKindSpecified() throws Exception {
    HasMetadata object = mock(HasMetadata.class);
    when(object.getKind()).thenReturn(null);
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(singletonList(object));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "MyObject metadata must not be null")
  public void exceptionOnObjectWithNoMetadataSpecified() throws Exception {
    HasMetadata object = mock(HasMetadata.class);
    when(object.getKind()).thenReturn("MyObject");
    when(object.getMetadata()).thenReturn(null);
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(singletonList(object));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "MyObject name must not be null")
  public void exceptionOnObjectWithNoNameSpecified() throws Exception {
    HasMetadata object = mock(HasMetadata.class);
    when(object.getKind()).thenReturn("MyObject");
    when(object.getMetadata()).thenReturn(new ObjectMetaBuilder().withName(null).build());
    when(k8sRecipeParser.parse(any(InternalRecipe.class))).thenReturn(singletonList(object));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  /** If provided {@code ramLimit} is {@code null} ram limit won't be set in POD */
  private static PodData createPodData(String machineName, Long ramLimit, Long ramRequest) {
    final String containerName = "container_" + machineName;
    final Container containerMock = mock(Container.class);
    final Pod podMock = mock(Pod.class);
    final PodSpec specMock = mock(PodSpec.class);
    final ObjectMeta metadataMock = mock(ObjectMeta.class);
    when(podMock.getSpec()).thenReturn(specMock);
    when(podMock.getMetadata()).thenReturn(metadataMock);
    final ResourceRequirements resourcesMock = mock(ResourceRequirements.class);
    when(containerMock.getResources()).thenReturn(resourcesMock);
    if (ramLimit != null) {
      final Quantity limitQuantityMock = mock(Quantity.class);
      when(limitQuantityMock.getAmount()).thenReturn(String.valueOf(ramLimit));
      when(resourcesMock.getLimits()).thenReturn(ImmutableMap.of("memory", limitQuantityMock));
    }
    if (ramRequest != null) {
      final Quantity requestQuantityMock = mock(Quantity.class);
      when(requestQuantityMock.getAmount()).thenReturn(String.valueOf(ramRequest));
      when(resourcesMock.getRequests()).thenReturn(ImmutableMap.of("memory", requestQuantityMock));
    }
    when(containerMock.getName()).thenReturn(containerName);
    when(metadataMock.getAnnotations())
        .thenReturn(Names.createMachineNameAnnotations(containerName, machineName));
    when(specMock.getContainers()).thenReturn(ImmutableList.of(containerMock));
    return new PodData(specMock, metadataMock);
  }

  private Deployment createEmptyDeployment(String name) {
    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withNewTemplate()
        .withNewMetadata()
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();
  }
}
