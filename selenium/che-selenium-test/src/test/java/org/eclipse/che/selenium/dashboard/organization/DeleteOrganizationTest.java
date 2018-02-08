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
package org.eclipse.che.selenium.dashboard.organization;

import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.ConfirmDialog;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization deletion from the Settings tab by the Delete button
 *
 * @author Ann Shumilova
 */
@Test(groups = {TestGroup.MULTIUSER})
public class DeleteOrganizationTest {
  private int initialOrgNumber;

  @InjectTestOrganization(prefix = "parentOrg")
  private TestOrganization parentOrg;

  @InjectTestOrganization(parentPrefix = "parentOrg")
  private TestOrganization childOrg;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient adminTestOrganizationServiceClient;

  @Inject private TestOrganizationServiceClient userTestOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private ConfirmDialog confirmDialog;
  @Inject private Dashboard dashboard;
  @Inject private TestUser testUser;

  @BeforeClass
  public void setUp() throws Exception {
    parentOrg.addAdmin(testUser.getId());
    childOrg.addAdmin(testUser.getId());
    initialOrgNumber = userTestOrganizationServiceClient.getAll().size();

    dashboard.open(testUser.getName(), testUser.getPassword());
  }

  public void testSubOrganizationDeletion() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(childOrg.getQualifiedName());
    organizationPage.waitOrganizationName(childOrg.getName());

    // Delete the sub-organization from the Settings tab by the Delete button
    deleteOrganization(childOrg.getName());

    // Test that the organization deleted
    organizationListPage.waitForOrganizationsList();
    organizationListPage.waitForOrganizationIsRemoved(childOrg.getQualifiedName());
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), initialOrgNumber - 1);
    assertEquals(organizationListPage.getOrganizationListItemCount(), initialOrgNumber - 1);
  }

  @Test(priority = 1)
  public void testParentOrganizationDeletion() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(parentOrg.getName());
    organizationPage.waitOrganizationName(parentOrg.getName());

    // Delete the parent organization from the Settings tab by the Delete button
    deleteOrganization(parentOrg.getName());

    // Test that the organization deleted
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), initialOrgNumber - 2);
  }

  private void deleteOrganization(String organizationName) {
    organizationPage.clickDeleteOrganizationButton();
    confirmDialog.waitOpened();
    assertEquals(confirmDialog.getTitle(), "Delete organization");
    assertEquals(
        confirmDialog.getMessage(),
        "Would you like to delete organization '" + organizationName + "'?");
    assertEquals(confirmDialog.getConfirmButtonTitle(), "Delete");
    confirmDialog.clickConfirm();
    confirmDialog.waitClosed();
  }
}
