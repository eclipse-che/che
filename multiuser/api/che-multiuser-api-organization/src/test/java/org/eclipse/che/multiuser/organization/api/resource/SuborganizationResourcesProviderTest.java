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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import javax.inject.Provider;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.eclipse.che.multiuser.resource.api.usage.ResourceUsageManager;
import org.eclipse.che.multiuser.resource.model.ProvidedResources;
import org.eclipse.che.multiuser.resource.spi.impl.ProvidedResourcesImpl;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.organization.api.resource.SuborganizationResourcesProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class SuborganizationResourcesProviderTest {
  @Mock private Account account;
  @Mock private Organization organization;

  @Mock private AccountManager accountManager;
  @Mock private OrganizationManager organizationManager;
  @Mock private OrganizationResourcesDistributor resourcesDistributor;
  @Mock private Provider<OrganizationResourcesDistributor> distributorProvider;
  @Mock private Provider<ResourceUsageManager> usageManagerProvider;
  @Mock private ResourceUsageManager resourceUsageManager;

  private SuborganizationResourcesProvider suborganizationResourcesProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    when(accountManager.getById(any())).thenReturn(account);
    when(organizationManager.getById(any())).thenReturn(organization);

    when(distributorProvider.get()).thenReturn(resourcesDistributor);

    when(usageManagerProvider.get()).thenReturn(resourceUsageManager);

    suborganizationResourcesProvider =
        new SuborganizationResourcesProvider(
            accountManager, organizationManager, distributorProvider, usageManagerProvider);
  }

  @Test
  public void shouldNotProvideResourcesForNonOrganizationalAccounts() throws Exception {
    // given
    when(account.getType()).thenReturn("test");

    // when
    final List<ProvidedResources> providedResources =
        suborganizationResourcesProvider.getResources("account123");

    // then
    assertTrue(providedResources.isEmpty());
    verify(accountManager).getById("account123");
  }

  @Test
  public void shouldNotProvideResourcesForRootOrganizationalAccount() throws Exception {
    // given
    when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
    when(organization.getParent()).thenReturn(null);

    // when
    final List<ProvidedResources> providedResources =
        suborganizationResourcesProvider.getResources("organization123");

    // then
    assertTrue(providedResources.isEmpty());
    verify(accountManager).getById("organization123");
    verify(organizationManager).getById("organization123");
  }

  @Test
  public void shouldProvideResourcesForSuborganizationalAccount() throws Exception {
    // given
    when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
    when(organization.getParent()).thenReturn("parentOrg");
    final ResourceImpl parentNotCapedResource = new ResourceImpl("test", 1234, "unit");
    final ResourceImpl parentCapedResource = new ResourceImpl("caped", 20, "unit");
    final ResourceImpl parentUnlimitedCapedResource = new ResourceImpl("unlimited", -1, "unit");
    doReturn(asList(parentNotCapedResource, parentCapedResource, parentUnlimitedCapedResource))
        .when(resourceUsageManager)
        .getTotalResources(anyString());

    final ResourceImpl capedResourceCap = new ResourceImpl("caped", 10, "unit");
    final ResourceImpl unlimitedCapedResourceCap = new ResourceImpl("unlimited", 40, "unit");
    doReturn(asList(capedResourceCap, unlimitedCapedResourceCap))
        .when(resourcesDistributor)
        .getResourcesCaps(any());

    // when
    final List<ProvidedResources> providedResources =
        suborganizationResourcesProvider.getResources("organization123");

    // then
    assertEquals(providedResources.size(), 1);
    assertEquals(
        providedResources.get(0),
        new ProvidedResourcesImpl(
            SuborganizationResourcesProvider.PARENT_RESOURCES_PROVIDER,
            null,
            "organization123",
            -1L,
            -1L,
            asList(parentNotCapedResource, capedResourceCap, unlimitedCapedResourceCap)));
    verify(accountManager).getById("organization123");
    verify(organizationManager).getById("organization123");
    verify(resourcesDistributor).getResourcesCaps("organization123");
    verify(resourceUsageManager).getTotalResources("parentOrg");
  }

  @Test
  public void shouldNotProvideResourcesForOrganizationalAccountIfItDoesNotHaveDistributedResources()
      throws Exception {
    // given
    when(account.getType()).thenReturn(OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
    when(organization.getParent()).thenReturn("parentOrg");
    doReturn(emptyList()).when(resourcesDistributor).getResourcesCaps(any());
    doReturn(emptyList()).when(resourceUsageManager).getAvailableResources(anyString());

    // when
    final List<ProvidedResources> providedResources =
        suborganizationResourcesProvider.getResources("organization123");

    // then
    assertTrue(providedResources.isEmpty());
    verify(accountManager).getById("organization123");
    verify(organizationManager).getById("organization123");
    verify(resourcesDistributor, never()).getResourcesCaps("organization123");
    verify(resourceUsageManager).getTotalResources("parentOrg");
  }
}
