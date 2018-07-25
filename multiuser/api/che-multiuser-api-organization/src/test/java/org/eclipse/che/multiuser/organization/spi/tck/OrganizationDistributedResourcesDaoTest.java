/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.organization.spi.tck;

import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.multiuser.organization.spi.OrganizationDistributedResourcesDao;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationDistributedResourcesImpl;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link OrganizationDistributedResourcesDao}
 *
 * @author Sergii Leschenko
 */
@Listeners(TckListener.class)
@Test(suiteName = OrganizationDistributedResourcesDaoTest.SUITE_NAME)
public class OrganizationDistributedResourcesDaoTest {
  public static final String SUITE_NAME = "OrganizationDistributedResourcesDaoTck";

  private static final String TEST_RESOURCE_TYPE = "Test";
  private static final int ORGANIZATION_RESOURCES_COUNT = 3;

  private OrganizationDistributedResourcesImpl[] distributedResources;
  private OrganizationImpl parentOrganization;
  private OrganizationImpl[] suborganizations;

  @Inject
  private TckRepository<OrganizationDistributedResourcesImpl> distributedResourcesRepository;

  @Inject private TckRepository<OrganizationImpl> organizationsRepository;

  @Inject private OrganizationDistributedResourcesDao distributedResourcesDao;

  @BeforeMethod
  private void setUp() throws Exception {
    parentOrganization = new OrganizationImpl("parentOrg", "parentOrgName", null);
    suborganizations = new OrganizationImpl[ORGANIZATION_RESOURCES_COUNT];
    distributedResources = new OrganizationDistributedResourcesImpl[ORGANIZATION_RESOURCES_COUNT];
    for (int i = 0; i < ORGANIZATION_RESOURCES_COUNT; i++) {
      suborganizations[i] =
          new OrganizationImpl("suborgId-" + i, "suborgName" + i, parentOrganization.getId());
      distributedResources[i] =
          new OrganizationDistributedResourcesImpl(
              suborganizations[i].getId(),
              singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, i, "test")));
    }
    organizationsRepository.createAll(Collections.singletonList(parentOrganization));
    organizationsRepository.createAll(Arrays.asList(suborganizations));
    distributedResourcesRepository.createAll(Arrays.asList(distributedResources));
  }

  @AfterMethod
  private void cleanup() throws Exception {
    distributedResourcesRepository.removeAll();
    organizationsRepository.removeAll();
  }

  @Test
  public void shouldCreateDistributedResourcesWhenStoringNotExistentOne() throws Exception {
    // given
    OrganizationDistributedResourcesImpl toStore = distributedResources[0];
    distributedResourcesDao.remove(toStore.getOrganizationId());

    // when
    distributedResourcesDao.store(toStore);

    // then
    assertEquals(distributedResourcesDao.get(toStore.getOrganizationId()), copy(toStore));
  }

  @Test
  public void shouldUpdateDistributedResourcesWhenStoringExistentOne() throws Exception {
    // given
    OrganizationDistributedResourcesImpl toStore =
        new OrganizationDistributedResourcesImpl(
            distributedResources[0].getOrganizationId(),
            singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 1000, "unit")));

    // when
    distributedResourcesDao.store(toStore);

    // then
    assertEquals(distributedResourcesDao.get(toStore.getOrganizationId()), copy(toStore));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenStoringNullableDistributedResources() throws Exception {
    // when
    distributedResourcesDao.store(null);
  }

  @Test
  public void shouldGetDistributedResourcesForSpecifiedOrganizationId() throws Exception {
    // given
    OrganizationDistributedResourcesImpl stored = distributedResources[0];

    // when
    OrganizationDistributedResourcesImpl fetched =
        distributedResourcesDao.get(stored.getOrganizationId());

    // then
    assertEquals(fetched, copy(stored));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenGettingNonExistingDistributedResources()
      throws Exception {
    // when
    distributedResourcesDao.get("account123");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingDistributedResourcesByNullOrganizationId() throws Exception {
    // when
    distributedResourcesDao.get(null);
  }

  @Test
  public void shouldGetDistributedResourcesByParent() throws Exception {
    // when
    final Page<OrganizationDistributedResourcesImpl> children =
        distributedResourcesDao.getByParent(parentOrganization.getId(), 1, 1);

    // then
    assertEquals(children.getTotalItemsCount(), 3);
    assertEquals(children.getItemsCount(), 1);
    assertTrue(
        children.getItems().contains(copy(distributedResources[0]))
            ^ children.getItems().contains(copy(distributedResources[1]))
            ^ children.getItems().contains(copy(distributedResources[2])));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingDistributedResourcesByNullParentId() throws Exception {
    // when
    distributedResourcesDao.getByParent(null, 1, 1);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldRemoveDistributedResources() throws Exception {
    // given
    OrganizationDistributedResourcesImpl distributedResource = distributedResources[0];

    // when
    distributedResourcesDao.remove(distributedResource.getOrganizationId());

    // then
    distributedResourcesDao.get(distributedResource.getOrganizationId());
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenRemovingDistributedResourcesByNullId() throws Exception {
    // when
    distributedResourcesDao.remove(null);
  }

  private OrganizationDistributedResourcesImpl copy(
      OrganizationDistributedResourcesImpl distributedResource) {
    return new OrganizationDistributedResourcesImpl(distributedResource);
  }
}
