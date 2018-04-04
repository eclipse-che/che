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

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.multiuser.organization.api.OrganizationManager;
import org.eclipse.che.multiuser.organization.spi.impl.OrganizationImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link
 * org.eclipse.che.multiuser.organization.api.resource.OrganizationResourceLockKeyProvider}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationResourceLockKeyProviderTest {
  @Mock private OrganizationManager organizationManager;

  @InjectMocks private OrganizationResourceLockKeyProvider lockProvider;

  @Test
  public void shouldReturnRootOrganizationId() throws Exception {
    // given
    createOrganization("root", null);
    createOrganization("suborg", "root");
    createOrganization("subsuborg", "suborg");

    // when
    final String lockId = lockProvider.getLockKey("subsuborg");

    // then
    assertEquals(lockId, "root");
  }

  @Test
  public void shouldReturnOrganizationalReturnType() throws Exception {
    // then
    assertEquals(lockProvider.getAccountType(), OrganizationImpl.ORGANIZATIONAL_ACCOUNT);
  }

  private void createOrganization(String id, String parentId) throws Exception {
    when(organizationManager.getById(id))
        .thenReturn(new OrganizationImpl(id, id + "Name", parentId));
  }
}
