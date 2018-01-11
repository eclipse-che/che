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
package org.eclipse.che.multiuser.resource.api.license;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.multiuser.resource.api.ResourceAggregator;
import org.eclipse.che.multiuser.resource.model.AccountLicense;
import org.eclipse.che.multiuser.resource.spi.impl.ProvidedResourcesImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.license.AccountLicenseManager}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class AccountLicenseManagerTest {
  @Mock private ResourcesProvider resourcesProvider;
  @Mock private ResourceAggregator resourceAggregator;

  private AccountLicenseManager accountLicenseManager;

  @BeforeMethod
  public void setUp() {
    accountLicenseManager =
        new AccountLicenseManager(singleton(resourcesProvider), resourceAggregator);
  }

  @Test(
    expectedExceptions = NotFoundException.class,
    expectedExceptionsMessageRegExp = "Account with specified id was not found"
  )
  public void shouldThrowNotFoundExceptionWhenAccountWithGivenIdWasNotFound() throws Exception {
    when(resourcesProvider.getResources(eq("account123")))
        .thenThrow(new NotFoundException("Account with specified id was not found"));

    accountLicenseManager.getByAccount("account123");
  }

  @Test
  public void shouldReturnLicenseForGivenAccount() throws Exception {
    final ResourceImpl testResource = new ResourceImpl("RAM", 1000, "mb");
    final ResourceImpl reducedResource = new ResourceImpl("timeout", 2000, "m");
    final ProvidedResourcesImpl providedResource =
        new ProvidedResourcesImpl(
            "test", null, "account123", 123L, 321L, singletonList(testResource));

    when(resourcesProvider.getResources(eq("account123")))
        .thenReturn(singletonList(providedResource));
    when(resourceAggregator.aggregateByType(any()))
        .thenReturn(ImmutableMap.of(reducedResource.getType(), reducedResource));

    final AccountLicense license = accountLicenseManager.getByAccount("account123");

    verify(resourcesProvider).getResources(eq("account123"));
    verify(resourceAggregator).aggregateByType(eq(singletonList(testResource)));

    assertEquals(license.getAccountId(), "account123");
    assertEquals(license.getResourcesDetails().size(), 1);
    assertEquals(license.getResourcesDetails().get(0), providedResource);

    assertEquals(license.getTotalResources().size(), 1);
    assertEquals(license.getTotalResources().get(0), reducedResource);
  }
}
