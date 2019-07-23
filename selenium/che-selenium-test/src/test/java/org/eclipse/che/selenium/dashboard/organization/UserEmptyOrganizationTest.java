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
package org.eclipse.che.selenium.dashboard.organization;

import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClientFactory;
import org.eclipse.che.selenium.core.user.TestUser;
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
@Test(groups = {TestGroup.MULTIUSER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class UserEmptyOrganizationTest {

  private TestOrganizationServiceClient organizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private NavigationBar navigationBar;
  @Inject private Dashboard dashboard;

  @Inject private TestOrganizationServiceClientFactory organizationServiceClientFactory;

  @Inject TestUser testUser;

  @BeforeClass
  public void setUp() throws Exception {
    organizationServiceClient = organizationServiceClientFactory.create(testUser);

    assertTrue(
        organizationServiceClient.getAll().isEmpty(),
        "This test requires empty organization list inside the default user account.");
    dashboard.open(testUser.getName(), testUser.getPassword());
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
