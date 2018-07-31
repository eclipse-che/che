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
package org.eclipse.che.multiuser.api.account.personal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.api.type.TimeoutResourceType;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link DefaultUserResourcesProvider}
 *
 * @author Sergii Leschenko
 */
public class DefaultUserResourcesProviderTest {
  private DefaultUserResourcesProvider resourcesProvider;

  @BeforeMethod
  public void setUp() throws Exception {
    resourcesProvider = new DefaultUserResourcesProvider(20 * 60 * 1000, "2gb", 10, 5);
  }

  @Test
  public void shouldReturnPersonalAccountType() throws Exception {
    // when
    final String accountType = resourcesProvider.getAccountType();

    // then
    assertEquals(accountType, UserManager.PERSONAL_ACCOUNT);
  }

  @Test
  public void shouldProvideDefaultRamResourceForUser() throws Exception {
    // when
    final List<ResourceImpl> defaultResources = resourcesProvider.getResources("user123");

    // then
    assertEquals(defaultResources.size(), 4);
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(RamResourceType.ID, 2048, RamResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(WorkspaceResourceType.ID, 10, WorkspaceResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(RuntimeResourceType.ID, 5, RuntimeResourceType.UNIT)));
    assertTrue(
        defaultResources.contains(
            new ResourceImpl(TimeoutResourceType.ID, 20, TimeoutResourceType.UNIT)));
  }
}
