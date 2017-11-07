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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.AddOrganization;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization creation and actions on it in the list of organizations.
 *
 * @author Ann Shumilova
 */
public class FilterOrganizationTest {
  private static final String ORGANIZATION_NAME = generate("organization", 5);

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private AddOrganization addOrganization;
  @Inject private NavigationBar navigationBar;
  @Inject private AdminTestUser adminTestUser;
  @Inject private Dashboard dashboard;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    for (OrganizationDto organization : testOrganizationServiceClient.getAll())
      testOrganizationServiceClient.deleteById(organization.getId());
  }

  @Test
  public void testOrganizationListFiler() {
    int organizationsCount = 1;

    // Create a new organization
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.clickAddOrganizationButton();
    addOrganization.waitAddOrganization();
    addOrganization.setOrganizationName(ORGANIZATION_NAME);
    addOrganization.checkAddOrganizationButtonEnabled();
    addOrganization.clickCreateOrganizationButton();
    organizationPage.waitOrganizationTitle(ORGANIZATION_NAME);

    // Test that created organization exist and count of organizations increased
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), organizationsCount);
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
    assertTrue(
        organizationListPage
            .getValues(OrganizationListPage.OrganizationListHeader.NAME)
            .contains(ORGANIZATION_NAME));

    // Tests search organization feature
    organizationListPage.typeInSearchInput(ORGANIZATION_NAME);
    assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
    organizationListPage.typeInSearchInput(ORGANIZATION_NAME + "test");
    organizationListPage.waitForOrganizationsList();
    assertEquals(organizationListPage.getOrganizationListItemCount(), 0);
    organizationListPage.clearSearchInput();
    assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
  }
}
