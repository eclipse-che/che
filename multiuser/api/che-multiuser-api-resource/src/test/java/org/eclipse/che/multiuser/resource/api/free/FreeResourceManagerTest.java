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
package org.eclipse.che.multiuser.resource.api.free;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.resource.model.FreeResourcesLimit;
import org.eclipse.che.multiuser.resource.shared.dto.FreeResourcesLimitDto;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;
import org.eclipse.che.multiuser.resource.spi.FreeResourcesLimitDao;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.free.FreeResourcesLimitManager}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class FreeResourceManagerTest {
  private static final String TEST_RESOURCE_TYPE = "Test";

  @Mock private FreeResourcesLimitDao freeResourcesLimitDao;

  @InjectMocks private FreeResourcesLimitManager manager;

  @Test
  public void shouldStoreFreeResourcesLimit() throws Exception {
    // given
    ResourceImpl resource = new ResourceImpl(TEST_RESOURCE_TYPE, 1, "unit");
    FreeResourcesLimitImpl resourcesLimitImpl =
        new FreeResourcesLimitImpl("account123", singletonList(resource));

    ResourceDto resourceDto =
        DtoFactory.newDto(ResourceDto.class)
            .withAmount(1)
            .withType(TEST_RESOURCE_TYPE)
            .withUnit("unit");
    FreeResourcesLimitDto freeResourcesLimitDto =
        DtoFactory.newDto(FreeResourcesLimitDto.class)
            .withAccountId("account123")
            .withResources(singletonList(resourceDto));

    // when
    FreeResourcesLimit storedLimit = manager.store(freeResourcesLimitDto);

    // then
    assertEquals(storedLimit, resourcesLimitImpl);
    verify(freeResourcesLimitDao).store(resourcesLimitImpl);
  }

  @Test(
      expectedExceptions = NullPointerException.class,
      expectedExceptionsMessageRegExp = "Required non-null free resources limit")
  public void shouldThrowNpeOnStoringNullableFreeResourcesLimit() throws Exception {
    // when
    manager.store(null);
  }

  @Test
  public void shouldReturnFreeResourcesLimitForSpecifiedAccount() throws Exception {
    // given
    ResourceImpl resource = new ResourceImpl(TEST_RESOURCE_TYPE, 1, "unit");
    FreeResourcesLimitImpl resourcesLimitImpl =
        new FreeResourcesLimitImpl("account123", singletonList(resource));

    when(freeResourcesLimitDao.get(any())).thenReturn(resourcesLimitImpl);

    // when
    FreeResourcesLimit fetchedLimit = manager.get("account123");

    // then
    assertEquals(fetchedLimit, resourcesLimitImpl);
    verify(freeResourcesLimitDao).get("account123");
  }

  @Test(
      expectedExceptions = NullPointerException.class,
      expectedExceptionsMessageRegExp = "Required non-null account id")
  public void shouldThrowNpeOnGettingFreeResourcesLimitByNullableAccountId() throws Exception {
    // when
    manager.get(null);
  }

  @Test
  public void shouldRemoveFreeResourcesLimitForSpecifiedAccount() throws Exception {
    // when
    manager.remove("account123");

    // then
    verify(freeResourcesLimitDao).remove("account123");
  }

  @Test(
      expectedExceptions = NullPointerException.class,
      expectedExceptionsMessageRegExp = "Required non-null account id")
  public void shouldThrowNpeOnRemovingFreeResourcesLimitByNullableAccountId() throws Exception {
    // when
    manager.remove(null);
  }

  @Test
  public void shouldReturnFreeResourcesLimits() throws Exception {
    // given
    ResourceImpl resource = new ResourceImpl(TEST_RESOURCE_TYPE, 1, "unit");
    FreeResourcesLimitImpl resourcesLimitImpl =
        new FreeResourcesLimitImpl("account123", singletonList(resource));

    when(freeResourcesLimitDao.getAll(anyInt(), anyInt()))
        .thenReturn(new Page<>(singletonList(resourcesLimitImpl), 5, 1, 9));

    // when
    Page<? extends FreeResourcesLimit> fetchedLimits = manager.getAll(1, 5);

    // then
    assertEquals(fetchedLimits.getTotalItemsCount(), 9);
    assertEquals(fetchedLimits.getSize(), 1);
    assertEquals(fetchedLimits.getItems().get(0), resourcesLimitImpl);
    verify(freeResourcesLimitDao).getAll(1, 5);
  }
}
