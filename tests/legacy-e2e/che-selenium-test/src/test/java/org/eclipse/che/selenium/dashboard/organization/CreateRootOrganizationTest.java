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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.CheTestAdminOrganizationServiceClient;
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
@Test(groups = {TestGroup.MULTIUSER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class CreateRootOrganizationTest {
  private static final String PARENT_ORG_NAME = generate("parent-", 4);
  private static final String CHILD_ORG_NAME = generate("child-", 4);

  private int initialOrgNumber;

  @Inject private CheTestAdminOrganizationServiceClient adminOrganizationServiceClient;
  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private AddOrganization addOrganization;
  @Inject private NavigationBar navigationBar;
  @Inject private AdminTestUser adminTestUser;
  @Inject private Dashboard dashboard;

  @BeforeClass
  public void setUp() throws Exception {
    initialOrgNumber = adminOrganizationServiceClient.getAllRoot().size();
    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    adminOrganizationServiceClient.deleteByName(CHILD_ORG_NAME);
    adminOrganizationServiceClient.deleteByName(PARENT_ORG_NAME);
  }

  public void testCreateOrganization() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();

    // Create a new organization
    organizationListPage.clickAddOrganizationButton();
    addOrganization.waitAddOrganization();
    addOrganization.setOrganizationName(PARENT_ORG_NAME);
    addOrganization.checkAddOrganizationButtonEnabled();
    addOrganization.clickCreateOrganizationButton();
    addOrganization.waitAddOrganizationButtonIsNotVisible();
    organizationPage.waitOrganizationTitle(PARENT_ORG_NAME);

    // Test that created organization exists and count of organizations increased
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), initialOrgNumber + 1);
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    assertEquals(organizationListPage.getOrganizationListItemCount(), initialOrgNumber + 1);
    assertTrue(organizationListPage.getValues(NAME).contains(PARENT_ORG_NAME));
    organizationListPage.clickOnOrganization(PARENT_ORG_NAME);
    organizationPage.waitOrganizationName(PARENT_ORG_NAME);

    // Create sub-organization
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForOrganizationsList();
    organizationPage.clickAddSuborganizationButton();
    addOrganization.waitAddSubOrganization();
    addOrganization.setOrganizationName(CHILD_ORG_NAME);
    addOrganization.checkAddOrganizationButtonEnabled();
    addOrganization.clickCreateOrganizationButton();
    addOrganization.waitAddOrganizationButtonIsNotVisible();
    organizationPage.waitOrganizationTitle(PARENT_ORG_NAME + "/" + CHILD_ORG_NAME);
  }
}
