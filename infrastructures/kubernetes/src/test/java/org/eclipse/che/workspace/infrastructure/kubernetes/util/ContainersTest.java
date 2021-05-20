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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.ResourceRequirements;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link Containers}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class ContainersTest {

  private static final long RAM_LIMIT = 2147483648L;
  private static final long RAM_REQUEST = 1474836480L;
  private static final float CPU_LIMIT = 1.782f;
  private static final float CPU_REQUEST = 0.223f;

  @Mock private Container container;
  @Mock private ResourceRequirements resource;

  @Captor private ArgumentCaptor<ResourceRequirements> resourceCaptor;

  private final Map<String, Quantity> limits = new HashMap<>();

  @BeforeMethod
  public void setup() {
    when(container.getResources()).thenReturn(resource);

    limits.put("memory", new Quantity(String.valueOf(RAM_LIMIT)));
    limits.put("cpu", new Quantity("1.5"));
    lenient()
        .when(resource.getLimits())
        .thenReturn(
            ImmutableMap.of(
                "memory", new Quantity(String.valueOf(RAM_LIMIT)),
                "cpu", new Quantity(String.valueOf(CPU_LIMIT))));
    lenient()
        .when(resource.getRequests())
        .thenReturn(
            ImmutableMap.of(
                "memory", new Quantity(String.valueOf(RAM_REQUEST)),
                "cpu", new Quantity(String.valueOf(CPU_REQUEST))));
  }

  @Test
  public void testReturnContainerRamLimitAndRequest() {
    long limit = Containers.getRamLimit(container);
    long request = Containers.getRamRequest(container);

    assertEquals(limit, RAM_LIMIT);
    assertEquals(request, RAM_REQUEST);
  }

  @Test
  public void testReturnContainerCPULimitAndRequest() {
    float limit = Containers.getCpuLimit(container);
    float request = Containers.getCpuRequest(container);

    assertEquals(limit, CPU_LIMIT);
    assertEquals(request, CPU_REQUEST);
  }

  @Test
  public void testReturnsZeroWhenContainerResourcesIsNull() {
    when(container.getResources()).thenReturn(null);

    assertEquals(Containers.getRamLimit(container), 0);
    assertEquals(Containers.getRamRequest(container), 0);
    assertEquals(Containers.getCpuLimit(container), 0, 0.0);
    assertEquals(Containers.getCpuRequest(container), 0, 0.0);
  }

  @Test
  public void testReturnsZeroResourceWhenResourcesDoesNotContainIt() {
    when(resource.getLimits()).thenReturn(Collections.emptyMap());
    when(resource.getRequests()).thenReturn(Collections.emptyMap());

    assertEquals(Containers.getRamLimit(container), 0);
    assertEquals(Containers.getRamRequest(container), 0);
    assertEquals(Containers.getCpuLimit(container), 0, 0.0);
    assertEquals(Containers.getCpuRequest(container), 0, 0.0);
  }

  @Test
  public void testReturnsZeroContainerLimitWhenActualValueIsNull() {
    when(resource.getLimits())
        .thenReturn(ImmutableMap.of("memory", new Quantity(), "cpu", new Quantity()));

    assertEquals(Containers.getRamLimit(container), 0);
    assertEquals(Containers.getCpuLimit(container), 0, 0.0);
  }

  @Test
  public void testReturnsZeroContainerRequestWhenActualValueIsNull() {
    when(resource.getRequests())
        .thenReturn(ImmutableMap.of("memory", new Quantity(), "cpu", new Quantity()));

    assertEquals(Containers.getRamRequest(container), 0);
    assertEquals(Containers.getCpuRequest(container), 0, 0.0);
  }

  @Test
  public void testAddContainerRamLimitWhenResourceIsNull() {
    when(container.getResources()).thenReturn(null);

    Containers.addRamLimit(container, RAM_LIMIT);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("memory").getAmount(), String.valueOf(RAM_LIMIT));
  }

  @Test
  public void testAddContainerCPULimitWhenResourceIsNull() {
    when(container.getResources()).thenReturn(null);

    Containers.addCpuLimit(container, CPU_LIMIT);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("cpu").getAmount(), String.valueOf(CPU_LIMIT));
  }

  @Test
  public void testAddContainerRamRequestWhenResourceIsNull() {
    when(container.getResources()).thenReturn(null);

    Containers.addRamRequest(container, RAM_REQUEST);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getRequests().get("memory").getAmount(), String.valueOf(RAM_REQUEST));
  }

  @Test
  public void testAddContainerCPURequestWhenResourceIsNull() {
    when(container.getResources()).thenReturn(null);

    Containers.addCpuRequest(container, CPU_REQUEST);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getRequests().get("cpu").getAmount(), String.valueOf(CPU_REQUEST));
  }

  @Test
  public void testAddContainerRamLimitWhenResourceDoesNotContainAnyLimits() {
    when(resource.getLimits()).thenReturn(null);

    Containers.addRamLimit(container, RAM_LIMIT);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("memory").getAmount(), String.valueOf(RAM_LIMIT));
  }

  @Test
  public void testAddContainerCPULimitWhenResourceDoesNotContainAnyLimits() {
    when(resource.getLimits()).thenReturn(null);

    Containers.addCpuLimit(container, CPU_LIMIT);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("cpu").getAmount(), String.valueOf(CPU_LIMIT));
  }

  @Test
  public void testAddContainerRamRequestWhenResourceDoesNotContainAnyLimits() {
    when(resource.getLimits()).thenReturn(null);

    Containers.addRamRequest(container, RAM_REQUEST);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getRequests().get("memory").getAmount(), String.valueOf(RAM_REQUEST));
  }

  @Test
  public void testAddContainerCPURequestWhenResourceDoesNotContainAnyLimits() {
    when(resource.getLimits()).thenReturn(null);

    Containers.addCpuRequest(container, CPU_REQUEST);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getRequests().get("cpu").getAmount(), String.valueOf(CPU_REQUEST));
  }

  @Test(dataProvider = "k8sNotionRamLimitProvider")
  public void testAddContainerRamLimitInK8sNotion(
      String ramLimit, String amount, String format, ResourceRequirements resources) {
    when(container.getResources()).thenReturn(resources);

    Containers.addRamLimit(container, ramLimit);

    verify(container).setResources(resourceCaptor.capture());
    ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("memory").getAmount(), amount);
    assertEquals(captured.getLimits().get("memory").getFormat(), format);
  }

  @Test(dataProvider = "k8sNotionRamLimitProvider")
  public void testAddContainerRamRequestInK8sNotion(
      String ramRequest, String amount, String format, ResourceRequirements resources) {
    when(container.getResources()).thenReturn(resources);

    Containers.addRamRequest(container, ramRequest);

    verify(container).setResources(resourceCaptor.capture());
    ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getRequests().get("memory").getAmount(), amount);
    assertEquals(captured.getRequests().get("memory").getFormat(), format);
  }

  @DataProvider
  public static Object[][] k8sNotionRamLimitProvider() {
    return new Object[][] {
      {"123456789", "123456789", "", new ResourceRequirements()},
      {"1M", "1", "M", new ResourceRequirements()},
      {"10Ki", "10", "Ki", null},
      {"10G", "10", "G", null},
    };
  }

  @Test(dataProvider = "k8sNotionCpuLimitProvider")
  public void testAddContainerCPULimitInK8sNotion(
      String cpuLimit, String amount, String format, ResourceRequirements resources) {
    when(container.getResources()).thenReturn(resources);

    Containers.addCpuLimit(container, cpuLimit);

    verify(container).setResources(resourceCaptor.capture());
    ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("cpu").getAmount(), amount);
    assertEquals(captured.getLimits().get("cpu").getFormat(), format);
  }

  @Test(dataProvider = "k8sNotionCpuLimitProvider")
  public void testAddContainerCpuRequestInK8sNotion(
      String cpuRequest, String amount, String format, ResourceRequirements resources) {
    when(container.getResources()).thenReturn(resources);

    Containers.addCpuRequest(container, cpuRequest);

    verify(container).setResources(resourceCaptor.capture());
    ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getRequests().get("cpu").getAmount(), amount);
    assertEquals(captured.getRequests().get("cpu").getFormat(), format);
  }

  @DataProvider
  public static Object[][] k8sNotionCpuLimitProvider() {
    return new Object[][] {
      {"100m", "100", "m", new ResourceRequirements()},
      {"1000m", "1000", "m", new ResourceRequirements()},
      {"112m", "112", "m", null},
      {"155m", "155", "m", null},
    };
  }

  @Test
  public void testReturnContainerCPULimitAndRequestConvertedToFullCores() {
    when(resource.getLimits()).thenReturn(ImmutableMap.of("cpu", new Quantity("1000", "m")));
    when(resource.getRequests()).thenReturn(ImmutableMap.of("cpu", new Quantity("30", "m")));

    assertEquals(Containers.getCpuLimit(container), 1);
    assertEquals(Containers.getCpuRequest(container), 0.03, 0.000000001);
  }
}
