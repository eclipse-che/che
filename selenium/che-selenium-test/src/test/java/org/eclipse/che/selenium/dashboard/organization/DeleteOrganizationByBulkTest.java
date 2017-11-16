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
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
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
public class DeleteOrganizationByBulkTest {
  private static final String ORG_NAME1 = generate("organization1", 5);
  private static final String ORG_NAME2 = generate("organization2", 5);

  private List<OrganizationDto> organizations = new ArrayList<>();

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
    organizations.add(testOrganizationServiceClient.create(ORG_NAME1));
    organizations.add(testOrganizationServiceClient.create(ORG_NAME2));

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    for (OrganizationDto organization : organizations) {
      testOrganizationServiceClient.deleteById(organization.getId());
    }
  }

  @Test
  public void testOrganizationBulkDeletion() {
    int organizationsCount = organizations.size();

    // Check that created organization exist
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    assertTrue(organizationListPage.getOrganizationListItemCount() >= organizationsCount);
    assertTrue(organizationListPage.getValues(NAME).contains(ORG_NAME1));
    assertTrue(organizationListPage.getValues(NAME).contains(ORG_NAME2));

    // Tests the Bulk Delete feature
    assertFalse(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(ORG_NAME1);
    organizationListPage.clickCheckbox(ORG_NAME2);
    assertTrue(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(ORG_NAME1);
    organizationListPage.clickCheckbox(ORG_NAME2);
    assertFalse(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(ORG_NAME1);
    organizationListPage.clickCheckbox(ORG_NAME2);
    assertTrue(organizationListPage.isBulkDeleteVisible());

    // Delete all organizations by the Bulk Delete feature
    organizationListPage.clickBulkDeleteButton();
    confirmDialog.waitOpened();
    assertEquals(confirmDialog.getTitle(), "Delete organizations");
    assertEquals(
        confirmDialog.getMessage(),
        String.format("Would you like to delete these %s organizations?", organizationsCount));
    assertEquals(confirmDialog.getConfirmButtonTitle(), "Delete");
    assertEquals(confirmDialog.getCancelButtonTitle(), "Close");
    confirmDialog.clickConfirm();
    confirmDialog.waitClosed();
    organizationListPage.waitForOrganizationsList();

    // Test that all organization removed
    organizationListPage.waitForOrganizationIsRemoved(ORG_NAME1);
    organizationListPage.waitForOrganizationIsRemoved(ORG_NAME2);
    assertTrue(organizationListPage.getOrganizationListItemCount() >= 0);
    assertFalse(organizationListPage.getValues(NAME).contains(ORG_NAME1));
    assertFalse(organizationListPage.getValues(NAME).contains(ORG_NAME2));
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) >= 0);
  }
}
