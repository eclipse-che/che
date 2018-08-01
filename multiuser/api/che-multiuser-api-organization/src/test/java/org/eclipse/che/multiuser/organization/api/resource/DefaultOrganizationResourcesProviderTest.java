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
package org.eclipse.che.multiuser.organization.api.resource;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.shared.model.Organization;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.api.type.TimeoutResourceType;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.organization.api.resource.DefaultOrganizationResourcesProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class DefaultOrganizationResourcesProviderTest {
  @Mock private OrganizationManager organizationManager;
  @Mock private Organization organization;

  private DefaultOrganizationResourcesProvider organizationResourcesProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    organizationResourcesProvider =
        new DefaultOrganizationResourcesProvider(organizationManager, "2gb", 10, 5, 10 * 60 * 1000);
    when(organizationManager.getById(anyString())).thenReturn(organization);
  }

  @Test
  public void shouldNotProvideDefaultResourcesForSuborganization() throws Exception {
    // given
    when(organization.getParent()).thenReturn("parentId");

    // when
    final List<ResourceImpl> defaultResources =
        organizationResourcesProvider.getResources("organization123");

    // then
    verify(organizationManager).getById("organization123");
    assertTrue(defaultResources.isEmpty());
  }

  @Test
  public void shouldProvideDefaultResourcesForRootOrganization() throws Exception {
    // given
    when(organization.getParent()).thenReturn(null);

    // when
    final List<ResourceImpl> defaultResources =
        organizationResourcesProvider.getResources("organization123");

    // then
    verify(organizationManager).getById("organization123");
    assertEquals(defaultResources.size(), 4);
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(TimeoutResourceType.ID, 10, TimeoutResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(RamResourceType.ID, 2048, RamResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(WorkspaceResourceType.ID, 10, WorkspaceResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(RuntimeResourceType.ID, 5, RuntimeResourceType.UNIT)));
  }
}
