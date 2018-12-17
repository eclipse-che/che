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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.limits.ram;

import static com.google.common.collect.ImmutableMap.of;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import io.fabric8.kubernetes.api.model.ResourceRequirementsBuilder;
import java.util.Collections;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.MemoryAttributeProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link RamLimitRequestProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class RamLimitRequestProvisionerTest {

  public static final String POD_NAME = "web";
  public static final String CONTAINER_NAME = "app";
  public static final String MACHINE_NAME = POD_NAME + '/' + CONTAINER_NAME;
  public static final String RAM_LIMIT_ATTRIBUTE = "2147483648";
  public static final String RAM_REQUEST_ATTRIBUTE = "1234567890";

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity identity;
  @Mock private Pod pod;
  @Mock private InternalMachineConfig internalMachineConfig;
  @Mock private MemoryAttributeProvisioner memoryAttributeProvisioner;

  @Captor private ArgumentCaptor<ResourceRequirements> resourceCaptor;

  private Container container;
  private RamLimitRequestProvisioner ramProvisioner;

  @BeforeMethod
  public void setup() {
    ramProvisioner = new RamLimitRequestProvisioner(memoryAttributeProvisioner);
    container = new Container();
    container.setName(CONTAINER_NAME);
    when(k8sEnv.getMachines()).thenReturn(of(MACHINE_NAME, internalMachineConfig));
    when(internalMachineConfig.getAttributes())
        .thenReturn(
            of(
                MEMORY_LIMIT_ATTRIBUTE,
                RAM_LIMIT_ATTRIBUTE,
                MEMORY_REQUEST_ATTRIBUTE,
                RAM_REQUEST_ATTRIBUTE));
    final ObjectMeta podMetadata = mock(ObjectMeta.class);
    when(podMetadata.getName()).thenReturn(POD_NAME);
    final PodSpec podSpec = mock(PodSpec.class);
    when(podSpec.getContainers()).thenReturn(Collections.singletonList(container));
    when(k8sEnv.getPodData()).thenReturn(of(POD_NAME, new PodData(podSpec, podMetadata)));
  }

  @Test
  public void testProvisionRamLimitAttributeToContainer() throws Exception {
    ramProvisioner.provision(k8sEnv, identity);

    assertEquals(
        container.getResources().getLimits().get("memory").getAmount(), RAM_LIMIT_ATTRIBUTE);
  }

  @Test
  public void testOverridesContainerRamLimitFromMachineAttribute() throws Exception {
    ResourceRequirements resourceRequirements =
        new ResourceRequirementsBuilder()
            .addToLimits(of("memory", new Quantity("3221225472")))
            .build();
    container.setResources(resourceRequirements);

    ramProvisioner.provision(k8sEnv, identity);

    assertEquals(
        container.getResources().getLimits().get("memory").getAmount(), RAM_LIMIT_ATTRIBUTE);
  }

  @Test
  public void testProvisionRamRequestAttributeToContainer() throws Exception {
    ramProvisioner.provision(k8sEnv, identity);

    assertEquals(
        container.getResources().getRequests().get("memory").getAmount(), RAM_REQUEST_ATTRIBUTE);
  }

  @Test
  public void testOverridesContainerRamRequestFromMachineAttribute() throws Exception {
    ResourceRequirements resourceRequirements =
        new ResourceRequirementsBuilder()
            .addToRequests(of("memory", new Quantity("3221225472")))
            .build();
    container.setResources(resourceRequirements);

    ramProvisioner.provision(k8sEnv, identity);

    assertEquals(
        container.getResources().getRequests().get("memory").getAmount(), RAM_REQUEST_ATTRIBUTE);
  }
}
