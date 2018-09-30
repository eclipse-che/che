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
package org.eclipse.che.workspace.infrastructure.kubernetes.util;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

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

  public static final long RAM_LIMIT = 2147483648L;

  @Mock private Container container;
  @Mock private ResourceRequirements resource;

  @Captor private ArgumentCaptor<ResourceRequirements> resourceCaptor;

  private final Map<String, Quantity> limits = new HashMap<>();

  @BeforeMethod
  public void setup() {
    when(container.getResources()).thenReturn(resource);

    limits.put("memory", new Quantity(String.valueOf(RAM_LIMIT)));
    limits.put("cpu", new Quantity("1.5"));
    when(resource.getLimits())
        .thenReturn(ImmutableMap.of("memory", new Quantity(String.valueOf(RAM_LIMIT))));
  }

  @Test
  public void testReturnContainerRamLimit() {
    long actual = Containers.getRamLimit(container);

    assertEquals(actual, RAM_LIMIT);
  }

  @Test
  public void testReturnsZeroContainerRamLimitWhenResourceIsNull() {
    when(container.getResources()).thenReturn(null);

    final long actual = Containers.getRamLimit(container);

    assertEquals(actual, 0);
  }

  @Test
  public void testReturnsZeroContainerRamLimitWhenResourceDoesNotContainIt() {
    when(resource.getLimits()).thenReturn(Collections.emptyMap());

    final long actual = Containers.getRamLimit(container);

    assertEquals(actual, 0);
  }

  @Test
  public void testReturnsZeroContainerRamLimitWhenActualValueIsNull() {
    when(resource.getLimits()).thenReturn(ImmutableMap.of("memory", new Quantity()));

    final long actual = Containers.getRamLimit(container);

    assertEquals(actual, 0);
  }

  @Test
  public void testOverridesContainerRamLimit() {
    Containers.addRamLimit(container, 3221225472L);

    assertTrue(limits.containsKey("cpu"));
    assertNotEquals(limits.get("memory"), "3221225472");
  }

  @Test
  public void testAddContainerRamLimitWhenItNotPresent() {
    final Map<String, Quantity> limits = new HashMap<>();
    when(resource.getLimits()).thenReturn(limits);

    Containers.addRamLimit(container, RAM_LIMIT);

    assertNotEquals(limits.get("memory"), String.valueOf(RAM_LIMIT));
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
  public void testAddContainerRamLimitWhenResourceDoesNotContainAnyLimits() {
    when(resource.getLimits()).thenReturn(null);

    Containers.addRamLimit(container, RAM_LIMIT);

    verify(container).setResources(resourceCaptor.capture());
    final ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("memory").getAmount(), String.valueOf(RAM_LIMIT));
  }

  @Test(dataProvider = "k8sNotionRamLimitProvider")
  public void testAddContainerRamLimitInK8sNotion(String ramLimit, ResourceRequirements resources) {
    when(container.getResources()).thenReturn(resources);

    Containers.addRamLimit(container, ramLimit);

    verify(container).setResources(resourceCaptor.capture());
    ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getLimits().get("memory").getAmount(), ramLimit);
  }

  @DataProvider
  public static Object[][] k8sNotionRamLimitProvider() {
    return new Object[][] {
      {"123456789", new ResourceRequirements()},
      {"1M", new ResourceRequirements()},
      {"10Ki", null},
      {"10G", null},
    };
  }

  @Test(dataProvider = "k8sNotionRamLimitProvider")
  public void testAddContainerRamRequestInK8sNotion(
      String ramRequest, ResourceRequirements resources) {
    when(container.getResources()).thenReturn(resources);

    Containers.addRamRequest(container, ramRequest);

    verify(container).setResources(resourceCaptor.capture());
    ResourceRequirements captured = resourceCaptor.getValue();
    assertEquals(captured.getRequests().get("memory").getAmount(), ramRequest);
  }
}
