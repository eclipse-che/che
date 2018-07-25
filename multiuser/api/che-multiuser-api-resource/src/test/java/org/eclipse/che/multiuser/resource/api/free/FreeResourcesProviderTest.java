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
package org.eclipse.che.multiuser.resource.api.free;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.spi.impl.FreeResourcesLimitImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ProvidedResourcesImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.free.FreeResourcesProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class FreeResourcesProviderTest {
  private static final String TEST_ACCOUNT_TYPE = "test";
  private static final String TEST_RESOURCE_TYPE = "testResource";
  private static final String TEST_RESOURCE_UNIT = "testResourceUnit";

  @Mock private AccountImpl account;
  @Mock private FreeResourcesLimitManager freeResourcesLimitManager;
  @Mock private AccountManager accountManager;
  @Mock private DefaultResourcesProvider defaultResourcesProvider;

  private FreeResourcesProvider provider;

  @BeforeMethod
  public void setUp() throws Exception {
    when(account.getType()).thenReturn(TEST_ACCOUNT_TYPE);

    when(defaultResourcesProvider.getAccountType()).thenReturn(TEST_ACCOUNT_TYPE);
    when(defaultResourcesProvider.getResources(any()))
        .thenReturn(singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 1020, TEST_RESOURCE_UNIT)));

    provider =
        new FreeResourcesProvider(
            freeResourcesLimitManager, accountManager, ImmutableSet.of(defaultResourcesProvider));
  }

  @Test
  public void shouldProvideDefaultResourcesIfThereAreProviderForThisAccountType() throws Exception {
    // given
    when(accountManager.getById(any())).thenReturn(account);
    when(freeResourcesLimitManager.get(any())).thenThrow(new NotFoundException("not found"));

    // when
    List<ProvidedResources> result = provider.getResources("user123");

    // then
    assertEquals(result.size(), 1);
    ProvidedResources providedResources = result.get(0);
    assertEquals(
        providedResources,
        new ProvidedResourcesImpl(
            FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
            null,
            "user123",
            -1L,
            -1L,
            singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 1020, TEST_RESOURCE_UNIT))));
    verify(freeResourcesLimitManager).get("user123");
  }

  @Test
  public void shouldRewriteDefaultResourcesWithFreeResourcesLimitIfItExists() throws Exception {
    // given
    when(accountManager.getById(any())).thenReturn(account);
    when(freeResourcesLimitManager.get(any()))
        .thenReturn(
            new FreeResourcesLimitImpl(
                "user123",
                singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 12345, TEST_RESOURCE_UNIT))));

    // when
    List<ProvidedResources> result = provider.getResources("user123");

    // then
    assertEquals(result.size(), 1);
    ProvidedResources providedResources = result.get(0);
    assertEquals(
        providedResources,
        new ProvidedResourcesImpl(
            FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
            "user123",
            "user123",
            -1L,
            -1L,
            singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 12345, TEST_RESOURCE_UNIT))));
    verify(freeResourcesLimitManager).get("user123");
  }

  @Test
  public void shouldNotProvideDefaultResourcesForAccountThatDoesNotHaveDefaultResourcesProvider()
      throws Exception {
    // given
    when(account.getType()).thenReturn("anotherTestType");
    when(accountManager.getById(any())).thenReturn(account);
    when(freeResourcesLimitManager.get(any())).thenThrow(new NotFoundException("not found"));
    doThrow(new NotFoundException("not found")).when(freeResourcesLimitManager).get(any());

    // when
    List<ProvidedResources> result = provider.getResources("account123");

    // then
    assertTrue(result.isEmpty());
  }

  @Test
  public void
      shouldNotProvideDefaultResourcesForAccountIfDefaultResourcesProviderProvidesEmptyList()
          throws Exception {
    // given
    when(accountManager.getById(any())).thenReturn(account);
    when(defaultResourcesProvider.getResources(any())).thenReturn(emptyList());
    doThrow(new NotFoundException("not found")).when(freeResourcesLimitManager).get(any());

    // when
    List<ProvidedResources> result = provider.getResources("user123");

    // then
    assertTrue(result.isEmpty());
  }

  @Test
  public void shouldProvideResourcesFromFreeResourcesLimitIfItExists() throws Exception {
    // given
    when(account.getType()).thenReturn("anotherTestType");
    when(accountManager.getById(any())).thenReturn(account);
    when(freeResourcesLimitManager.get(any()))
        .thenReturn(
            new FreeResourcesLimitImpl(
                "account123",
                singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 12345, TEST_RESOURCE_UNIT))));

    // when
    List<ProvidedResources> result = provider.getResources("account123");

    // then
    assertEquals(result.size(), 1);
    ProvidedResources providedResources = result.get(0);
    assertEquals(
        providedResources,
        new ProvidedResourcesImpl(
            FreeResourcesProvider.FREE_RESOURCES_PROVIDER,
            "account123",
            "account123",
            -1L,
            -1L,
            singletonList(new ResourceImpl(TEST_RESOURCE_TYPE, 12345, TEST_RESOURCE_UNIT))));
    verify(freeResourcesLimitManager).get("account123");
  }
}
