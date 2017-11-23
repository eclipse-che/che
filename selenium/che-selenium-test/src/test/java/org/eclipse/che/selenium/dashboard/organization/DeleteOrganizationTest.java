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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.annotation.Multiuser;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.ConfirmDialog;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization deletion from the Settings tab by the Delete button
 *
 * @author Ann Shumilova
 */
@Multiuser
public class DeleteOrganizationTest {
  private static final String PARENT_ORG_NAME = generate("parent-org-", 5);
  private static final String CHILD_ORG_NAME = generate("child-org-", 5);
  private static final int TEST_ORG_NUMBER = 2;

  private OrganizationDto parentOrganization;
  private OrganizationDto childOrganization;

  private int initialOrgNumber;
  private int initialRootOrgNumber;

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
    try {
      parentOrganization = adminTestOrganizationServiceClient.create(PARENT_ORG_NAME);
      childOrganization = adminTestOrganizationServiceClient.create(CHILD_ORG_NAME);
      adminTestOrganizationServiceClient.addAdmin(parentOrganization.getId(), testUser.getId());
      adminTestOrganizationServiceClient.addAdmin(childOrganization.getId(), testUser.getId());

      dashboard.open(testUser.getName(), testUser.getPassword());
    } catch (Exception e) {
      // remove test organizations in case of error because TestNG skips @AfterClass method here
      tearDown();
      throw e;
    }

    initialOrgNumber = userTestOrganizationServiceClient.getAll().size();
  }

  @AfterClass
  public void tearDown() throws Exception {
    adminTestOrganizationServiceClient.deleteByName(CHILD_ORG_NAME);
    adminTestOrganizationServiceClient.deleteByName(PARENT_ORG_NAME);
  }

  @Test
  public void testSubOrganizationDeletion() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(childOrganization.getQualifiedName());
    organizationPage.waitOrganizationName(childOrganization.getName());

    // Delete the sub-organization from the Settings tab by the Delete button
    deleteOrganization(childOrganization.getName());

    // Test that the organization deleted
    organizationListPage.waitForOrganizationsList();
    organizationListPage.waitForOrganizationIsRemoved(childOrganization.getQualifiedName());
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), initialOrgNumber - 1);
    assertEquals(organizationListPage.getOrganizationListItemCount(), initialOrgNumber - 1);
  }

  @Test(priority = 1)
  public void testParentOrganizationDeletion() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(parentOrganization.getName());
    organizationPage.waitOrganizationName(parentOrganization.getName());

    // Delete the parent organization from the Settings tab by the Delete button
    deleteOrganization(parentOrganization.getName());

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
