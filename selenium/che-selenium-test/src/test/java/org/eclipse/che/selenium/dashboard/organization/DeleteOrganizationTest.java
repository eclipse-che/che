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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClientFactory;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
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
@Test(groups = {TestGroup.MULTIUSER, TestGroup.DOCKER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class DeleteOrganizationTest {
  private int initialOrgNumber;
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @InjectTestOrganization(prefix = "parentOrg")
  private TestOrganization parentOrg;

  @InjectTestOrganization(parentPrefix = "parentOrg")
  private TestOrganization childOrg;

  @Inject private TestOrganizationServiceClientFactory testOrganizationServiceClientFactory;
  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private ConfirmDialog confirmDialog;
  @Inject private Dashboard dashboard;
  @Inject private DefaultTestUser testUser;

  @BeforeClass
  public void setUp() throws Exception {
    testOrganizationServiceClient = testOrganizationServiceClientFactory.create(testUser);

    parentOrg.addAdmin(testUser.getId());
    childOrg.addAdmin(testUser.getId());
    initialOrgNumber = testOrganizationServiceClient.getAll().size();

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
