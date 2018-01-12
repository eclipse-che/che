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
package org.eclipse.che.multiuser.organization.api.resource;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.commons.lang.concurrent.Unlocker;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.shared.model.OrganizationDistributedResources;
import org.eclipse.che.multiuser.organization.spi.OrganizationDistributedResourcesDao;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationDistributedResourcesImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.api.usage.ResourceUsageManager;
import org.eclipse.che.multiuser.resource.api.usage.ResourcesLocks;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.organization.api.resource.OrganizationResourcesDistributor}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationResourcesDistributorTest {
  private static final String PARENT_ORG_ID = "parentOrg123";
  private static final String ORG_ID = "organization123";

  @Mock private Unlocker lock;
  @Mock private OrganizationDistributedResourcesDao distributedResourcesDao;
  @Mock private ResourcesLocks resourcesLocks;
  @Mock private ResourceUsageManager usageManager;
  @Mock private ResourceAggregator resourceAggregator;
  @Mock private OrganizationManager organizationManager;

  @Spy @InjectMocks private OrganizationResourcesDistributor manager;

  @BeforeMethod
  public void setUp() throws Exception {
    doNothing().when(manager).checkResourcesAvailability(anyString(), any());
    when(resourcesLocks.lock(anyString())).thenReturn(lock);

    when(organizationManager.getById(ORG_ID))
        .thenReturn(new OrganizationImpl(ORG_ID, ORG_ID + "name", PARENT_ORG_ID));
    when(organizationManager.getById(PARENT_ORG_ID))
        .thenReturn(new OrganizationImpl(PARENT_ORG_ID, PARENT_ORG_ID + "name", null));
  }

  @Test
  public void shouldCapResources() throws Exception {
    List<ResourceImpl> toCap = singletonList(createTestResource(1000));

    // when
    manager.capResources(ORG_ID, toCap);

    // then
    verify(manager).checkResourcesAvailability(ORG_ID, toCap);
    verify(distributedResourcesDao).store(new OrganizationDistributedResourcesImpl(ORG_ID, toCap));
    verify(resourcesLocks).lock(ORG_ID);
    verify(lock).close();
  }

  @Test
  public void shouldRemoveResourceFromListWhenItsAmountEqualsToMinusOne() throws Exception {
    ResourceImpl toCap = new ResourceImpl("test1", 1000, "init");
    ResourceImpl toReset = new ResourceImpl("test2", -1, "init");
    List<ResourceImpl> resourcesToCap = asList(toCap, toReset);

    // when
    manager.capResources(ORG_ID, resourcesToCap);

    // then
    verify(manager).checkResourcesAvailability(ORG_ID, singletonList(toCap));
    verify(distributedResourcesDao)
        .store(new OrganizationDistributedResourcesImpl(ORG_ID, singletonList(toCap)));
    verify(resourcesLocks).lock(ORG_ID);
    verify(lock).close();
  }

  @Test
  public void shouldRemoveResourcesCapWhenInvokeCapWithEmptyList() throws Exception {
    // when
    manager.capResources(ORG_ID, Collections.emptyList());

    // then
    verify(manager, never()).checkResourcesAvailability(anyString(), any());
    verify(distributedResourcesDao).remove(ORG_ID);
    verify(resourcesLocks).lock(ORG_ID);
    verify(lock).close();
  }

  @Test(
    expectedExceptions = ConflictException.class,
    expectedExceptionsMessageRegExp = "It is not allowed to cap resources for root organization."
  )
  public void shouldThrowConflictExceptionOnCappingResourcesForRootOrganization() throws Exception {
    // when
    manager.capResources(PARENT_ORG_ID, Collections.emptyList());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnDistributionResourcesWithNullOrganizationId() throws Exception {
    // when
    manager.capResources(null, emptyList());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnDistributionNullResourcesList() throws Exception {
    // when
    manager.capResources(ORG_ID, null);
  }

  @Test
  public void shouldGetDistributedResources() throws Exception {
    // given
    final OrganizationDistributedResourcesImpl distributedResources =
        createDistributedResources(1000);
    doReturn(new Page<>(singletonList(distributedResources), 0, 10, 1))
        .when(distributedResourcesDao)
        .getByParent(anyString(), anyInt(), anyLong());

    // when
    final Page<? extends OrganizationDistributedResources> fetchedDistributedResources =
        manager.getByParent(ORG_ID, 10, 0);

    // then
    assertEquals(fetchedDistributedResources.getTotalItemsCount(), 1);
    assertEquals(fetchedDistributedResources.getItems().get(0), distributedResources);
    verify(distributedResourcesDao).getByParent(ORG_ID, 10, 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingDistributedResourcesByNullOrganizationId() throws Exception {
    // when
    manager.getByParent(null, 10, 10);
  }

  @Test
  public void shouldGetResourcesCap() throws Exception {
    // given
    final OrganizationDistributedResourcesImpl distributedResources =
        createDistributedResources(1000);
    when(distributedResourcesDao.get(anyString())).thenReturn(distributedResources);

    // when
    final List<? extends Resource> fetchedDistributedResources = manager.getResourcesCaps(ORG_ID);

    // then
    assertEquals(fetchedDistributedResources, distributedResources.getResourcesCap());
    verify(distributedResourcesDao).get(ORG_ID);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingResourcesCapByNullOrganizationId() throws Exception {
    // when
    manager.getResourcesCaps(null);
  }

  @Test
  public void shouldResourceAvailabilityCappingResourcesWhenResourceCapIsLessThanUsedOne()
      throws Exception {
    // given
    doCallRealMethod().when(manager).checkResourcesAvailability(anyString(), any());

    ResourceImpl used = createTestResource(500);
    doReturn(singletonList(used)).when(usageManager).getUsedResources(any());

    ResourceImpl toCap = createTestResource(700);
    doReturn(createTestResource(200)).when(resourceAggregator).deduct((Resource) any(), any());

    // when
    manager.checkResourcesAvailability(ORG_ID, singletonList(toCap));

    // then
    verify(usageManager).getUsedResources(ORG_ID);
    verify(resourceAggregator).deduct(toCap, used);
  }

  @Test(
    expectedExceptions = ConflictException.class,
    expectedExceptionsMessageRegExp = "Resources are currently in use. Denied."
  )
  public void shouldResourceAvailabilityCappingResourcesWhenResourceCapIsGreaterThanUsedOne()
      throws Exception {
    // given
    doCallRealMethod().when(manager).checkResourcesAvailability(anyString(), any());
    doReturn("Denied.").when(manager).getMessage(anyString());

    ResourceImpl used = createTestResource(1000);
    doReturn(singletonList(used)).when(usageManager).getUsedResources(any());

    ResourceImpl toCap = createTestResource(700);
    doThrow(new NoEnoughResourcesException(emptyList(), emptyList(), singletonList(toCap)))
        .when(resourceAggregator)
        .deduct((Resource) any(), any());

    // when
    manager.checkResourcesAvailability(ORG_ID, singletonList(toCap));

    // then
    verify(usageManager).getUsedResources(ORG_ID);
    verify(resourceAggregator).deduct(toCap, used);
  }

  private ResourceImpl createTestResource(long amount) {
    return new ResourceImpl("test", amount, "init");
  }

  private OrganizationDistributedResourcesImpl createDistributedResources(long resourceAmount) {
    return new OrganizationDistributedResourcesImpl(
        ORG_ID, singletonList(createTestResource(resourceAmount)));
  }
}
