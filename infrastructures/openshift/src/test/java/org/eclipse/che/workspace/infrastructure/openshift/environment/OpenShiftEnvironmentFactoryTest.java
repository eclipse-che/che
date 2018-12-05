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
import static org.eclipse.che.workspace.infrastructure.openshift.Warnings.CONFIG_MAP_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.openshift.Warnings.CONFIG_MAP_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.Warnings.PVC_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.openshift.Warnings.PVC_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.Warnings.ROUTES_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.Warnings.ROUTE_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.openshift.Warnings.SECRET_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.openshift.Warnings.SECRET_IGNORED_WARNING_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.DoneableKubernetesList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.dsl.KubernetesListMixedOperation;
import io.fabric8.kubernetes.client.dsl.RecreateFromServerGettable;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.environment.MemoryAttributeProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentValidator;
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
  public static final String MACHINE_NAME_1 = "machine1";
  public static final String MACHINE_NAME_2 = "machine2";

  private OpenShiftEnvironmentFactory osEnvironmentFactory;

  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private KubernetesEnvironmentValidator k8sEnvValidator;
  @Mock private OpenShiftClient client;
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
    osEnvironmentFactory =
        new OpenShiftEnvironmentFactory(
            null, null, null, clientFactory, k8sEnvValidator, memoryProvisioner);
    when(clientFactory.create()).thenReturn(client);
    when(client.lists()).thenReturn(listMixedOperation);
    when(listMixedOperation.load(any(InputStream.class))).thenReturn(serverGettable);
    when(serverGettable.get()).thenReturn(validatedObjects);
    when(internalRecipe.getContentType()).thenReturn(YAML_RECIPE);
    when(internalRecipe.getContent()).thenReturn("recipe content");
    machines = ImmutableMap.of(MACHINE_NAME_1, machineConfig1, MACHINE_NAME_2, machineConfig2);
  }

  @Test
  public void ignoreRoutesWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> objects = asList(new Route(), new Route());
    when(validatedObjects.getItems()).thenReturn(objects);

    final OpenShiftEnvironment parsed =
        osEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertTrue(parsed.getRoutes().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(ROUTE_IGNORED_WARNING_CODE, ROUTES_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void ignorePVCsWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> pvc = singletonList(new PersistentVolumeClaim());
    when(validatedObjects.getItems()).thenReturn(pvc);

    final OpenShiftEnvironment parsed =
        osEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

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

    final OpenShiftEnvironment parsed =
        osEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertTrue(parsed.getSecrets().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(SECRET_IGNORED_WARNING_CODE, SECRET_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void ignoreConfigMapsWhenRecipeContainsThem() throws Exception {
    final List<HasMetadata> recipeObjects = singletonList(new ConfigMap());
    when(validatedObjects.getItems()).thenReturn(recipeObjects);

    final KubernetesEnvironment parsed =
        osEnvironmentFactory.doCreate(internalRecipe, emptyMap(), emptyList());

    assertTrue(parsed.getConfigMaps().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(CONFIG_MAP_IGNORED_WARNING_CODE, CONFIG_MAP_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void testProvisionRamAttributesIsInvoked() throws Exception {
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

    osEnvironmentFactory.addRamAttributes(machines, pods);

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
