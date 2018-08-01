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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import java.util.Collections;
import java.util.HashMap;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link RamLimitProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class RamLimitProvisionerTest {

  public static final String POD_NAME = "web";
  public static final String CONTAINER_NAME = "app";
  public static final String MACHINE_NAME = POD_NAME + '/' + CONTAINER_NAME;
  public static final String RAM_LIMIT_ATTRIBUTE = "2147483648";

  @Mock private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity identity;
  @Mock private Pod pod;
  @Mock private Container container;
  @Mock private InternalMachineConfig internalMachineConfig;

  @Captor private ArgumentCaptor<ResourceRequirements> resourceCaptor;

  private RamLimitProvisioner ramLimitProvisioner;

  @BeforeMethod
  public void setup() {
    ramLimitProvisioner = new RamLimitProvisioner();
    when(k8sEnv.getPods()).thenReturn(ImmutableMap.of(POD_NAME, pod));
    when(k8sEnv.getMachines()).thenReturn(ImmutableMap.of(MACHINE_NAME, internalMachineConfig));
    when(internalMachineConfig.getAttributes())
        .thenReturn(ImmutableMap.of(MachineConfig.MEMORY_LIMIT_ATTRIBUTE, RAM_LIMIT_ATTRIBUTE));
    when(container.getName()).thenReturn(CONTAINER_NAME);
    final ObjectMeta podMetadata = mock(ObjectMeta.class);
    when(podMetadata.getName()).thenReturn(POD_NAME);
    when(pod.getMetadata()).thenReturn(podMetadata);
    final PodSpec podSpec = mock(PodSpec.class);
    when(podSpec.getContainers()).thenReturn(Collections.singletonList(container));
    when(pod.getSpec()).thenReturn(podSpec);
  }

  @Test
  public void testProvisionRamLimitAttributeToContainer() throws Exception {
    ramLimitProvisioner.provision(k8sEnv, identity);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("memory").getAmount(), RAM_LIMIT_ATTRIBUTE);
  }

  @Test
  public void testOverridesContainerRamLimitFromMachineAttribute() throws Exception {
    final ResourceRequirements containerResource = mock(ResourceRequirements.class);
    final HashMap<String, Quantity> limits = new HashMap<>();
    limits.put("memory", new Quantity("3221225472"));
    when(containerResource.getLimits()).thenReturn(limits);
    when(container.getResources()).thenReturn(containerResource);

    ramLimitProvisioner.provision(k8sEnv, identity);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("memory").getAmount(), RAM_LIMIT_ATTRIBUTE);
  }
}
