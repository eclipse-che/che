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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.fill;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.MACHINE_NAME_ANNOTATION_FMT;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory.PVC_IGNORED_WARNING_CODE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory.PVC_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory.ROUTES_IGNORED_WARNING_MESSAGE;
import static org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironmentFactory.ROUTE_IGNORED_WARNING_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import io.fabric8.kubernetes.client.dsl.KubernetesListMixedOperation;
import io.fabric8.kubernetes.client.dsl.RecreateFromServerGettable;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.client.OpenShiftClient;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
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
  private static final long DEFAULT_RAM_LIMIT_MB = 2048;
  public static final String MACHINE_NAME_1 = "machine1";
  public static final String MACHINE_NAME_2 = "machine2";

  private OpenShiftEnvironmentFactory osEnvironmentFactory;

  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private OpenShiftEnvironmentValidator osEnvValidator;
  @Mock private OpenShiftClient client;
  @Mock private InternalEnvironment internalEnvironment;
  @Mock private InternalRecipe internalRecipe;
  @Mock private KubernetesListMixedOperation listMixedOperation;
  @Mock private KubernetesList validatedObjects;
  @Mock private InternalMachineConfig machineConfig1;
  @Mock private InternalMachineConfig machineConfig2;

  private Map<String, InternalMachineConfig> machines;

  @Mock
  private RecreateFromServerGettable<KubernetesList, KubernetesList, DoneableKubernetesList>
      serverGettable;

  @BeforeMethod
  public void setup() throws Exception {
    osEnvironmentFactory =
        new OpenShiftEnvironmentFactory(
            null, null, null, clientFactory, osEnvValidator, DEFAULT_RAM_LIMIT_MB);
    when(clientFactory.create()).thenReturn(client);
    when(client.lists()).thenReturn(listMixedOperation);
    when(listMixedOperation.load(any(InputStream.class))).thenReturn(serverGettable);
    when(serverGettable.get()).thenReturn(validatedObjects);
    when(internalEnvironment.getRecipe()).thenReturn(internalRecipe);
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

    assertTrue(parsed.getRoutes().isEmpty());
    assertEquals(parsed.getWarnings().size(), 1);
    assertEquals(
        parsed.getWarnings().get(0),
        new WarningImpl(PVC_IGNORED_WARNING_CODE, PVC_IGNORED_WARNING_MESSAGE));
  }

  @Test
  public void testSetsRamLimitAttributeFromOpenShiftResource() throws Exception {
    final long firstMachineRamLimit = 3072 * BYTES_IN_MB;
    final long secondMachineRamLimit = 1024 * BYTES_IN_MB;
    when(machineConfig1.getAttributes()).thenReturn(new HashMap<>());
    when(machineConfig2.getAttributes()).thenReturn(new HashMap<>());
    final Set<Pod> pods =
        ImmutableSet.of(
            mockPod(MACHINE_NAME_1, firstMachineRamLimit),
            mockPod(MACHINE_NAME_2, secondMachineRamLimit));

    osEnvironmentFactory.addRamLimitAttribute(machines, pods);

    final long[] actual = machinesRam(machines.values());
    final long[] expected = new long[] {firstMachineRamLimit, secondMachineRamLimit};
    assertTrue(Arrays.equals(actual, expected));
  }

  @Test
  public void testDoNotOverrideRamLimitAttributeWhenItAlreadyPresent() throws Exception {
    final long customRamLimit = 3072 * BYTES_IN_MB;
    final Map<String, String> attributes =
        ImmutableMap.of(MEMORY_LIMIT_ATTRIBUTE, String.valueOf(customRamLimit));
    when(machineConfig1.getAttributes()).thenReturn(attributes);
    when(machineConfig2.getAttributes()).thenReturn(attributes);
    final Pod pod1 = mockPod(MACHINE_NAME_1, 0L);
    final Pod pod2 = mockPod(MACHINE_NAME_2, 0L);
    final Set<Pod> pods = ImmutableSet.of(pod1, pod2);

    osEnvironmentFactory.addRamLimitAttribute(machines, pods);

    final long[] actual = machinesRam(machines.values());
    final long[] expected = new long[actual.length];
    fill(expected, customRamLimit);
    assertTrue(Arrays.equals(actual, expected));
  }

  @Test
  public void testAddsMachineConfIntoEnvAndSetsRamLimAttributeWhenMachinePresentOnlyInRecipe()
      throws Exception {
    final long customRamLimit = 2048 * BYTES_IN_MB;
    final Map<String, InternalMachineConfig> machines = new HashMap<>();
    final Set<Pod> pods = ImmutableSet.of(mockPod(MACHINE_NAME_2, customRamLimit));

    osEnvironmentFactory.addRamLimitAttribute(machines, pods);

    final long[] actual = machinesRam(machines.values());
    final long[] expected = new long[actual.length];
    fill(expected, customRamLimit);
    assertTrue(Arrays.equals(actual, expected));
  }

  @Test
  public void testSetsDefaultRamLimitAttributeIfRamLimitIsMissingInRecipeAndConfig()
      throws Exception {
    final long firstMachineRamLimit = 3072 * BYTES_IN_MB;
    when(machineConfig1.getAttributes()).thenReturn(new HashMap<>());
    when(machineConfig2.getAttributes()).thenReturn(new HashMap<>());
    final Set<Pod> pods =
        ImmutableSet.of(
            mockPod(MACHINE_NAME_1, firstMachineRamLimit), mockPod(MACHINE_NAME_2, null));

    osEnvironmentFactory.addRamLimitAttribute(machines, pods);

    final long[] actual = machinesRam(machines.values());
    final long[] expected = new long[] {firstMachineRamLimit, DEFAULT_RAM_LIMIT_MB * BYTES_IN_MB};
    assertTrue(Arrays.equals(actual, expected));
  }

  /** If provided {@code ramLimit} is {@code null} ram limit won't be set in POD */
  private static Pod mockPod(String machineName, Long ramLimit) {
    final String containerName = "container_" + machineName;
    final Container containerMock = mock(Container.class);
    final Pod podMock = mock(Pod.class);
    final PodSpec specMock = mock(PodSpec.class);
    final ObjectMeta metadataMock = mock(ObjectMeta.class);
    when(podMock.getSpec()).thenReturn(specMock);
    when(podMock.getMetadata()).thenReturn(metadataMock);
    if (ramLimit != null) {
      final Quantity quantityMock = mock(Quantity.class);
      final ResourceRequirements resourcesMock = mock(ResourceRequirements.class);
      when(quantityMock.getAmount()).thenReturn(String.valueOf(ramLimit));
      when(resourcesMock.getLimits()).thenReturn(ImmutableMap.of("memory", quantityMock));
      when(containerMock.getResources()).thenReturn(resourcesMock);
    }
    when(containerMock.getName()).thenReturn(containerName);
    when(metadataMock.getAnnotations())
        .thenReturn(
            ImmutableMap.of(format(MACHINE_NAME_ANNOTATION_FMT, containerName), machineName));
    when(specMock.getContainers()).thenReturn(ImmutableList.of(containerMock));
    return podMock;
  }

  private static long[] machinesRam(Collection<InternalMachineConfig> configs) {
    return configs
        .stream()
        .mapToLong(m -> Long.parseLong(m.getAttributes().get(MEMORY_LIMIT_ATTRIBUTE)))
        .toArray();
  }
}
