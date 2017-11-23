/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.dashboard.organization;

import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.annotation.Multiuser;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization views for simple user.
 *
 * @author Ann Shumilova
 */
@Multiuser
public class UserEmptyOrganizationTest {

  @Inject private OrganizationListPage organizationListPage;
  @Inject private NavigationBar navigationBar;
  @Inject private Dashboard dashboard;

  @Inject private TestOrganizationServiceClient testOrganizationServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    assertTrue(
        testOrganizationServiceClient.getAll().isEmpty(),
        "This test requires empty organization list inside the default user account.");
    dashboard.open();
  }

  @Test
  public void testOrganizationListComponents() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsEmptyList();

    // Test UI views of organizations list for simple user
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) == 0);
    assertEquals(organizationListPage.getOrganizationsToolbarTitle(), "Organizations");
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertFalse(organizationListPage.isSearchInputVisible());
  }
}
