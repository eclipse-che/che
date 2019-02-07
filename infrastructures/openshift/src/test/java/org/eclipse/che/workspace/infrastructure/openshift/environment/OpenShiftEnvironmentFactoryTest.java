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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.environment.MemoryAttributeProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
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

  private static final String YAML_RECIPE = "application/x-yaml";
  private static final long BYTES_IN_MB = 1024 * 1024;
  private static final String MACHINE_NAME_1 = "machine1";
  private static final String MACHINE_NAME_2 = "machine2";

  private OpenShiftEnvironmentFactory osEnvFactory;

  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private OpenShiftEnvironmentValidator openShiftEnvValidator;
  @Mock private OpenShiftClient client;
  @Mock private InternalRecipe internalRecipe;
  @Mock private InternalMachineConfig machineConfig1;
  @Mock private InternalMachineConfig machineConfig2;
  @Mock private MemoryAttributeProvisioner memoryProvisioner;

  private Map<String, InternalMachineConfig> machines;

  @Mock
  private ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata, Boolean>
      loadedRecipe;

  @BeforeMethod
  public void setup() throws Exception {
    osEnvFactory =
        new OpenShiftEnvironmentFactory(
            null, null, null, clientFactory, openShiftEnvValidator, memoryProvisioner);
    when(clientFactory.create()).thenReturn(client);
    when(client.load(any(InputStream.class))).thenReturn(loadedRecipe);
    when(internalRecipe.getContentType()).thenReturn(YAML_RECIPE);
    when(internalRecipe.getContent()).thenReturn("recipe content");
    machines = ImmutableMap.of(MACHINE_NAME_1, machineConfig1, MACHINE_NAME_2, machineConfig2);
  }

  @Test
  public void shouldCreateOpenShiftEnvironmentWithServicesFromRecipe() throws Exception {
    // given
    Service service1 =
        new ServiceBuilder().withNewMetadata().withName("service1").endMetadata().build();
    Service service2 =
        new ServiceBuilder().withNewMetadata().withName("service2").endMetadata().build();
    when(loadedRecipe.get()).thenReturn(asList(service1, service2));

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
    when(loadedRecipe.get()).thenReturn(asList(pvc1, pvc2));

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
    when(loadedRecipe.get()).thenReturn(singletonList(route));

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
    when(loadedRecipe.get()).thenReturn(singletonList(secret));

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
    when(loadedRecipe.get()).thenReturn(singletonList(configMap));

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
    when(loadedRecipe.get()).thenReturn(singletonList(pod));

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

    when(loadedRecipe.get()).thenReturn(singletonList(deployment));

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
  public void bothPodsAndDeploymentsIncludedInPodData() throws Exception {
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
    when(loadedRecipe.get()).thenReturn(asList(deployment, pod));

    // when
    final KubernetesEnvironment osEnv =
        osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    // then
    assertEquals(osEnv.getPodsData().size(), 2);

    assertEquals(
        osEnv.getPodsData().get("deployment-test").getMetadata(), podTemplate.getMetadata());
    assertEquals(osEnv.getPodsData().get("deployment-test").getSpec(), podTemplate.getSpec());

    assertEquals(osEnv.getPodsData().get("bare-pod").getMetadata(), pod.getMetadata());
    assertEquals(osEnv.getPodsData().get("bare-pod").getSpec(), pod.getSpec());
  }

  @Test(expectedExceptions = ValidationException.class)
  public void exceptionOnRecipeLoadError() throws Exception {
    when(loadedRecipe.get()).thenThrow(new KubernetesClientException("Could not parse recipe"));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Environment contains object without specified kind field")
  public void exceptionOnObjectWithNoKindSpecified() throws Exception {
    HasMetadata object = mock(HasMetadata.class);
    when(object.getKind()).thenReturn(null);
    when(loadedRecipe.get()).thenReturn(singletonList(object));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "MyObject metadata must not be null")
  public void exceptionOnObjectWithNoMetadataSpecified() throws Exception {
    HasMetadata object = mock(HasMetadata.class);
    when(object.getKind()).thenReturn("MyObject");
    when(object.getMetadata()).thenReturn(null);
    when(loadedRecipe.get()).thenReturn(singletonList(object));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "MyObject name must not be null")
  public void exceptionOnObjectWithNoNameSpecified() throws Exception {
    HasMetadata object = mock(HasMetadata.class);
    when(object.getKind()).thenReturn("MyObject");
    when(object.getMetadata()).thenReturn(new ObjectMetaBuilder().withName(null).build());
    when(loadedRecipe.get()).thenReturn(singletonList(object));

    osEnvFactory.doCreate(internalRecipe, emptyMap(), emptyList());
  }

  @Test
  public void testProvisionRamAttributesIsInvoked() {
    final long firstMachineRamLimit = 3072 * BYTES_IN_MB;
    final long secondMachineRamLimit = 1024 * BYTES_IN_MB;
    final long firstMachineRamRequest = 1536 * BYTES_IN_MB;
    final long secondMachineRamRequest = 512 * BYTES_IN_MB;
    when(machineConfig1.getAttributes()).thenReturn(new HashMap<>());
    when(machineConfig2.getAttributes()).thenReturn(new HashMap<>());
    final Set<Pod> pods =
        ImmutableSet.of(
            mockPod(MACHINE_NAME_1, firstMachineRamLimit, firstMachineRamRequest),
            mockPod(MACHINE_NAME_2, secondMachineRamLimit, secondMachineRamRequest));

    osEnvFactory.addRamAttributes(machines, pods);

    verify(memoryProvisioner)
        .provision(eq(machineConfig1), eq(firstMachineRamLimit), eq(firstMachineRamRequest));
    verify(memoryProvisioner)
        .provision(eq(machineConfig2), eq(secondMachineRamLimit), eq(secondMachineRamRequest));
  }

  /** If provided {@code ramLimit} is {@code null} ram limit won't be set in POD */
  private static Pod mockPod(String machineName, Long ramLimit, Long ramRequest) {
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
        .thenReturn(
            ImmutableMap.of(format(MACHINE_NAME_ANNOTATION_FMT, containerName), machineName));
    when(specMock.getContainers()).thenReturn(ImmutableList.of(containerMock));
    return podMock;
  }
}
