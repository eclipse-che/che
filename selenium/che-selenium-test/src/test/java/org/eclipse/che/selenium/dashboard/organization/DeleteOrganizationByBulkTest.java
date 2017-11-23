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
import static org.testng.Assert.fail;

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
 * Test validates the bulk deletion of organizations in the list.
 *
 * @author Ann Shumilova
 */
@Multiuser
public class DeleteOrganizationByBulkTest {
  private static final String ORG1_NAME = generate("org1-", 5);
  private static final String ORG2_NAME = generate("org2-", 5);
  private static final int TEST_ROOT_ORG_NUMBER = 2;

  private int initialOrgNumber;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private AdminTestUser adminTestUser;
  @Inject private NavigationBar navigationBar;
  @Inject private ConfirmDialog confirmDialog;
  @Inject private Dashboard dashboard;

  @BeforeClass
  public void setUp() throws Exception {
    try {
      testOrganizationServiceClient.create(ORG1_NAME);
      testOrganizationServiceClient.create(ORG2_NAME);

      dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    } catch (Exception e) {
      // remove test organizations in case of error because TestNG skips @AfterClass method here
      tearDown();
      throw e;
    }

    initialOrgNumber = testOrganizationServiceClient.getAllRoot().size();
  }

  @AfterClass
  public void tearDown() throws Exception {
    testOrganizationServiceClient.deleteByName(ORG1_NAME);
    testOrganizationServiceClient.deleteByName(ORG2_NAME);
  }

  @Test
  public void testOrganizationBulkDeletion() {
    // Check that created organization exist
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    try {
      assertEquals(organizationListPage.getOrganizationListItemCount(), initialOrgNumber);
    } catch (AssertionError a) {
      // remove try-catch block after https://github.com/eclipse/che/issues/7279 has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7279");
    }

    assertTrue(organizationListPage.getValues(NAME).contains(ORG1_NAME));
    assertTrue(organizationListPage.getValues(NAME).contains(ORG2_NAME));

    // Tests the Bulk Delete feature
    assertFalse(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(ORG1_NAME);
    organizationListPage.clickCheckbox(ORG2_NAME);
    assertTrue(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(ORG1_NAME);
    organizationListPage.clickCheckbox(ORG2_NAME);
    assertFalse(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(ORG1_NAME);
    organizationListPage.clickCheckbox(ORG2_NAME);
    assertTrue(organizationListPage.isBulkDeleteVisible());

    // Delete all organizations by the Bulk Delete feature
    organizationListPage.clickBulkDeleteButton();
    confirmDialog.waitOpened();
    assertEquals(confirmDialog.getTitle(), "Delete organizations");
    assertEquals(
        confirmDialog.getMessage(),
        String.format("Would you like to delete these %s organizations?", TEST_ROOT_ORG_NUMBER));
    assertEquals(confirmDialog.getConfirmButtonTitle(), "Delete");
    assertEquals(confirmDialog.getCancelButtonTitle(), "Close");
    confirmDialog.clickConfirm();
    confirmDialog.waitClosed();
    organizationListPage.waitForOrganizationsList();

    // Test that all organization removed
    organizationListPage.waitForOrganizationIsRemoved(ORG1_NAME);
    organizationListPage.waitForOrganizationIsRemoved(ORG2_NAME);

    try {
      assertEquals(
          organizationListPage.getOrganizationListItemCount(),
          initialOrgNumber - TEST_ROOT_ORG_NUMBER);
    } catch (AssertionError a) {
      // remove try-catch block after https://github.com/eclipse/che/issues/7279 has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7279");
    }

    assertFalse(organizationListPage.getValues(NAME).contains(ORG1_NAME));
    assertFalse(organizationListPage.getValues(NAME).contains(ORG2_NAME));
    assertEquals(
        navigationBar.getMenuCounterValue(ORGANIZATIONS), initialOrgNumber - TEST_ROOT_ORG_NUMBER);
  }
}
