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
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
 * Test validates organization creation
 *
 * @author Ann Shumilova
 */
public class CreateOrganizationTest {
  private static final String ORGANIZATION_NAME = generate("organization", 4);
  private static final String SUB_ORGANIZATION_NAME = generate("sub-organization", 4);

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
    testOrganizationServiceClient.deleteByName(ORGANIZATION_NAME);
  }

  @Test
  public void testCreateOrganization() {
    int organizationsCount = 1;

    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();

    // Create a new organization
    organizationListPage.clickAddOrganizationButton();
    addOrganization.waitAddOrganization();
    addOrganization.setOrganizationName(ORGANIZATION_NAME);
    addOrganization.checkAddOrganizationButtonEnabled();
    addOrganization.clickCreateOrganizationButton();
    addOrganization.waitAddOrganizationButtonIsNotVisible();
    organizationPage.waitOrganizationTitle(ORGANIZATION_NAME);

    // Test that created organization exists and count of organizations increased
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) >= organizationsCount);
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getOrganizationListItemCount() >= organizationsCount);
    assertTrue(organizationListPage.getValues(NAME).contains(ORGANIZATION_NAME));

    organizationListPage.clickOnOrganization(ORGANIZATION_NAME);
    organizationPage.waitOrganizationName(ORGANIZATION_NAME);

    // Create sub-organization
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForOrganizationsList();
    organizationPage.clickAddSuborganizationButton();
    addOrganization.waitAddSubOrganization();
    addOrganization.setOrganizationName(SUB_ORGANIZATION_NAME);
    addOrganization.checkAddOrganizationButtonEnabled();
    addOrganization.clickCreateOrganizationButton();
    addOrganization.waitAddOrganizationButtonIsNotVisible();
    organizationPage.waitOrganizationTitle(ORGANIZATION_NAME + "/" + SUB_ORGANIZATION_NAME);
  }
}
