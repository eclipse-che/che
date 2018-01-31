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
package org.eclipse.che.multiuser.resource.api.usage;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import javax.inject.Provider;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link org.eclipse.che.multiuser.resource.api.usage.DefaultAvailableResourcesProvider}
 */
@Listeners(MockitoTestNGListener.class)
public class DefaultAvailableResourcesProviderTest {
  @Mock private Provider<ResourceManager> resourceManagerProvider;
  @Mock private ResourceManager resourceManager;
  @Mock private ResourceAggregator resourceAggregator;

  @InjectMocks private DefaultAvailableResourcesProvider defaultAvailableResourcesProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    when(resourceManagerProvider.get()).thenReturn(resourceManager);
  }

  @Test
  public void shouldReturnAvailableResourcesWhenNotAllTotalResourcesAreUsed() throws Exception {
    // given
    List<ResourceImpl> totalResources = singletonList(new ResourceImpl("test", 5000, "unit"));
    doReturn(totalResources).when(resourceManager).getTotalResources(anyString());
    List<ResourceImpl> usedResources = singletonList(new ResourceImpl("test", 2000, "unit"));
    doReturn(usedResources).when(resourceManager).getUsedResources(anyString());
    ResourceImpl availableResource = new ResourceImpl("test", 3000, "unit");
    doReturn(singletonList(availableResource))
        .when(resourceAggregator)
        .deduct(anyList(), anyList());

    // when
    List<? extends Resource> availableResources =
        defaultAvailableResourcesProvider.getAvailableResources("account123");

    // then
    assertEquals(availableResources.size(), 1);
    assertEquals(availableResources.get(0), availableResource);
    verify(resourceManager).getTotalResources("account123");
    verify(resourceManager).getUsedResources("account123");
    verify(resourceAggregator).deduct(totalResources, usedResources);
    verify(resourceAggregator, never()).excess(anyList(), anyList());
  }

  @Test
  public void shouldReturnExcessiveResourcesWhenNotOneResourceIsUsedButNotPresentInTotal()
      throws Exception {
    // given
    List<ResourceImpl> totalResources = singletonList(new ResourceImpl("test", 5000, "unit"));
    doReturn(totalResources).when(resourceManager).getTotalResources(anyString());
    List<ResourceImpl> usedResources =
        Arrays.asList(new ResourceImpl("test", 2000, "unit"), new ResourceImpl("test2", 5, "unit"));
    doReturn(usedResources).when(resourceManager).getUsedResources(anyString());
    doThrow(new NoEnoughResourcesException(emptyList(), emptyList(), emptyList()))
        .when(resourceAggregator)
        .deduct(anyList(), anyList());
    ResourceImpl excessiveResource = new ResourceImpl("test", 3000, "unit");
    doReturn(singletonList(excessiveResource))
        .when(resourceAggregator)
        .excess(anyList(), anyList());

    // when
    List<? extends Resource> availableResources =
        defaultAvailableResourcesProvider.getAvailableResources("account123");

    // then
    assertEquals(availableResources.size(), 1);
    assertEquals(availableResources.get(0), excessiveResource);
    verify(resourceManager).getTotalResources("account123");
    verify(resourceManager).getUsedResources("account123");
    verify(resourceAggregator).deduct(totalResources, usedResources);
    verify(resourceAggregator).excess(totalResources, usedResources);
  }
}
