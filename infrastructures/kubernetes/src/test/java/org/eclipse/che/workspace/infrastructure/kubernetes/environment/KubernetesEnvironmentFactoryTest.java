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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.INGRESSES_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.INGRESSES_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.PVC_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.PVC_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.SECRET_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Warnings.SECRET_IGNORED_WARNING_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.DoneableKubernetesList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpec;
import io.fabric8.kubernetes.api.model.PodTemplateSpecBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.KubernetesListMixedOperation;
import io.fabric8.kubernetes.client.dsl.RecreateFromServerGettable;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.environment.MemoryAttributeProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesEnvironmentFactory}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesEnvironmentFactoryTest {

  private static final String YAML_RECIPE = "application/x-yaml";
  public static final String MACHINE_NAME_1 = "machine1";
  public static final String MACHINE_NAME_2 = "machine2";

  private KubernetesEnvironmentFactory k8sEnvironmentFactory;

  @Mock private KubernetesClientFactory clientFactory;
  @Mock private KubernetesEnvironmentValidator k8sEnvValidator;
  @Mock private KubernetesClient client;
  @Mock private InternalEnvironment internalEnvironment;
  @Mock private InternalRecipe internalRecipe;
  @Mock private KubernetesListMixedOperation listMixedOperation;
  @Mock private KubernetesList validatedObjects;
  @Mock private InternalMachineConfig machineConfig1;
  @Mock private InternalMachineConfig machineConfig2;
  @Mock private MemoryAttributeProvisioner memoryProvisioner;

  private Map<String, InternalMachineConfig> machines;

  @Mock
  private RecreateFromServerGettable<KubernetesList, KubernetesList, DoneableKubernetesList>
      serverGettable;

  @BeforeMethod
  public void setup() throws Exception {
    k8sEnvironmentFactory =
        new KubernetesEnvironmentFactory(
            null, null, null, clientFactory, k8sEnvValidator, memoryProvisioner);
    when(clientFactory.create()).thenReturn(client);
    when(client.lists()).thenReturn(listMixedOperation);
    when(listMixedOperation.load(any(InputStream.class))).thenReturn(serverGettable);
    when(serverGettable.get()).thenReturn(validatedObjects);
    lenient().when(internalEnvironment.getRecipe()).thenReturn(internalRecipe);
    when(internalRecipe.getContentType()).thenReturn(YAML_RECIPE);
    when(internalRecipe.getContent()).thenReturn("recipe content");
    machines = ImmutableMap.of(MACHINE_NAME_1, machineConfig1, MACHINE_NAME_2, machineConfig2);
  }

  @Test
  public void ignoreIgressesWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> objects = asList(new Ingress(), new Ingress());
    when(validatedObjects.getItems()).thenReturn(objects);

    final KubernetesEnvironment parsed =
        k8sEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertTrue(parsed.getIngresses().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(INGRESSES_IGNORED_WARNING_CODE, INGRESSES_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void ignorePVCsWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> pvc = singletonList(new PersistentVolumeClaim());
    when(validatedObjects.getItems()).thenReturn(pvc);

    final KubernetesEnvironment parsed =
        k8sEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertTrue(parsed.getPersistentVolumeClaims().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(PVC_IGNORED_WARNING_CODE, PVC_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void ignoreSecretsWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> recipeObjects = singletonList(new Secret());
    when(validatedObjects.getItems()).thenReturn(recipeObjects);

    final KubernetesEnvironment parsed =
        k8sEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertTrue(parsed.getSecrets().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(SECRET_IGNORED_WARNING_CODE, SECRET_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void addConfigMapsWhenRecipeContainsThem() throws Exception {
    ConfigMap configMap =
        new ConfigMapBuilder().withNewMetadata().withName("test-configmap").endMetadata().build();
    final List<HasMetadata> recipeObjects = singletonList(configMap);
    when(validatedObjects.getItems()).thenReturn(recipeObjects);

    final KubernetesEnvironment parsed =
        k8sEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertEquals(parsed.getConfigMaps().size(), 1);
    assertEquals(
        parsed.getConfigMaps().values().iterator().next().getMetadata().getName(),
        configMap.getMetadata().getName());
  }

  @Test
  public void addPodsWhenRecipeContainsThem() throws Exception {
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName("pod-test")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();

    final List<HasMetadata> recipeObjects = singletonList(pod);
    when(validatedObjects.getItems()).thenReturn(recipeObjects);

    final KubernetesEnvironment parsed =
        k8sEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertEquals(parsed.getPodsCopy().size(), 1);
    assertEquals(
        parsed.getPodsCopy().values().iterator().next().getMetadata().getName(),
        pod.getMetadata().getName());
    assertEquals(parsed.getPodData().size(), 1);
    assertEquals(
        parsed.getPodData().values().iterator().next().getMetadata().getName(),
        pod.getMetadata().getName());
  }

  @Test
  public void addDeploymentsWhenRecipeContainsThem() throws Exception {
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

    final List<HasMetadata> recipeObjects = singletonList(deployment);
    when(validatedObjects.getItems()).thenReturn(recipeObjects);

    final KubernetesEnvironment parsed =
        k8sEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertEquals(parsed.getDeploymentsCopy().size(), 1);
    assertEquals(
        parsed.getDeploymentsCopy().values().iterator().next().getMetadata().getName(),
        deployment.getMetadata().getName());
    assertEquals(parsed.getPodData().size(), 1);
    assertEquals(
        parsed.getPodData().values().iterator().next().getMetadata().getName(),
        podTemplate.getMetadata().getName());
  }

  @Test
  public void bothPodsAndDeploymentsIncludedInPodData() throws Exception {
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
    final List<HasMetadata> recipeObjects = asList(deployment, pod);
    when(validatedObjects.getItems()).thenReturn(recipeObjects);

    final KubernetesEnvironment parsed =
        k8sEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertEquals(parsed.getPodData().size(), 2);
    assertTrue(
        parsed
            .getPodData()
            .values()
            .stream()
            .allMatch(
                podData -> {
                  String name = podData.getMetadata().getName();
                  return name.equals(podTemplate.getMetadata().getName())
                      || name.equals(pod.getMetadata().getName());
                }));
  }

  @Test
  public void testProvisionRamAttributesIsInvoked() throws Exception {
    final long firstMachineRamLimit = 3072;
    final long firstMachineRamRequest = 1536;
    final long secondMachineRamLimit = 1024;
    final long secondMachineRamRequest = 512;
    when(machineConfig1.getAttributes()).thenReturn(new HashMap<>());
    when(machineConfig2.getAttributes()).thenReturn(new HashMap<>());
    final Set<Pod> pods =
        ImmutableSet.of(
            mockPod(MACHINE_NAME_1, firstMachineRamLimit, firstMachineRamRequest),
            mockPod(MACHINE_NAME_2, secondMachineRamLimit, secondMachineRamRequest));

    k8sEnvironmentFactory.addRamAttributes(machines, pods);

    verify(memoryProvisioner)
        .provision(eq(machineConfig1), eq(firstMachineRamLimit), eq(firstMachineRamRequest));
    verify(memoryProvisioner)
        .provision(eq(machineConfig2), eq(secondMachineRamLimit), eq(secondMachineRamRequest));
  }

  private static Pod mockPod(String machineName, long ramLimit, long ramRequest) {
    final String containerName = "container_" + machineName;
    final Container containerMock = mock(Container.class);
    final ResourceRequirements resourcesMock = mock(ResourceRequirements.class);
    final Quantity limitQuantityMock = mock(Quantity.class);
    final Quantity requestQuantityMock = mock(Quantity.class);
    final Pod podMock = mock(Pod.class);
    final PodSpec specMock = mock(PodSpec.class);
    final ObjectMeta metadataMock = mock(ObjectMeta.class);
    when(podMock.getSpec()).thenReturn(specMock);
    when(podMock.getMetadata()).thenReturn(metadataMock);
    when(limitQuantityMock.getAmount()).thenReturn(String.valueOf(ramLimit));
    when(requestQuantityMock.getAmount()).thenReturn(String.valueOf(ramRequest));
    when(resourcesMock.getLimits()).thenReturn(ImmutableMap.of("memory", limitQuantityMock));
    when(resourcesMock.getRequests()).thenReturn(ImmutableMap.of("memory", requestQuantityMock));
    when(containerMock.getName()).thenReturn(containerName);
    when(containerMock.getResources()).thenReturn(resourcesMock);
    when(metadataMock.getAnnotations())
        .thenReturn(
            ImmutableMap.of(format(MACHINE_NAME_ANNOTATION_FMT, containerName), machineName));
    when(specMock.getContainers()).thenReturn(ImmutableList.of(containerMock));
    return podMock;
  }
}
