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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.annotation.Multiuser;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.ConfirmDialog;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates deleting of organizations from the list of organizations.
 *
 * @author Ann Shumilova
 */
@Multiuser
public class DeleteOrganizationInListTest {

  private static final String ORG1_NAME = generate("org1-", 7);
  private static final String ORG2_NAME = generate("org2-", 7);
  private static final String ORG3_NAME = generate("org3-", 7);
  private static final String ORG4_NAME = generate("org4-", 7);
  private static final int ORGANIZATIONS_NUMBER = 4;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private NavigationBar navigationBar;
  @Inject private ConfirmDialog confirmDialog;
  @Inject private AdminTestUser adminTestUser;
  @Inject private Dashboard dashboard;

  @BeforeClass
  public void setUp() throws Exception {
    try {
      testOrganizationServiceClient.create(ORG1_NAME);
      testOrganizationServiceClient.create(ORG2_NAME);
      testOrganizationServiceClient.create(ORG3_NAME);
      testOrganizationServiceClient.create(ORG4_NAME);
      dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    } catch (Exception e) {
      // remove test organizations in case of error because TestNG skips @AfterClass method here
      tearDown();
      throw e;
    }
  }

  @AfterClass
  public void tearDown() throws Exception {
    testOrganizationServiceClient.deleteByName(ORG1_NAME);
    testOrganizationServiceClient.deleteByName(ORG2_NAME);
    testOrganizationServiceClient.deleteByName(ORG3_NAME);
    testOrganizationServiceClient.deleteByName(ORG4_NAME);
  }

  @Test
  public void testOrganizationDeletionFromList() {
    // Open the Organization list and check that created organization exist
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) == ORGANIZATIONS_NUMBER);
    assertTrue(organizationListPage.getOrganizationListItemCount() == ORGANIZATIONS_NUMBER);
    assertTrue(organizationListPage.getValues(NAME).contains(ORG1_NAME));

    // Tests the Delete organization dialog
    organizationListPage.clickOnDeleteButton(ORG1_NAME);
    confirmDialog.waitOpened();
    assertEquals(confirmDialog.getTitle(), "Delete organization");
    assertEquals(
        confirmDialog.getMessage(),
        String.format("Would you like to delete organization '%s'?", ORG1_NAME));
    assertEquals(confirmDialog.getConfirmButtonTitle(), "Delete");
    assertEquals(confirmDialog.getCancelButtonTitle(), "Close");
    confirmDialog.closeDialog();
    confirmDialog.waitClosed();
    assertTrue(organizationListPage.getOrganizationListItemCount() == ORGANIZATIONS_NUMBER);
    organizationListPage.clickOnDeleteButton(ORG1_NAME);
    confirmDialog.waitOpened();
    confirmDialog.clickCancel();
    confirmDialog.waitClosed();
    assertTrue(organizationListPage.getOrganizationListItemCount() == ORGANIZATIONS_NUMBER);

    // Delete all organizations from the Organization list
    deleteOrganization(ORG1_NAME);
    deleteOrganization(ORG2_NAME);
    deleteOrganization(ORG3_NAME);
    deleteOrganization(ORG4_NAME);

    // Check that all organization deleted
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) == 0);
  }

  private void deleteOrganization(String organizationName) {
    organizationListPage.clickOnDeleteButton(organizationName);
    confirmDialog.waitOpened();
    confirmDialog.clickConfirm();
    confirmDialog.waitClosed();
    organizationListPage.waitForOrganizationsList();

    // Test that organization deleted
    assertTrue(organizationListPage.getOrganizationListItemCount() == ORGANIZATIONS_NUMBER - 1);
    assertFalse(organizationListPage.getValues(NAME).contains(organizationName));
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) == ORGANIZATIONS_NUMBER - 1);
  }
}
