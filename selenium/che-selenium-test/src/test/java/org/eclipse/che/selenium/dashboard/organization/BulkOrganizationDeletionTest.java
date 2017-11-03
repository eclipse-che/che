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
import java.util.List;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.ConfirmDialog;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates the bulk deletion of organizations in the list.
 *
 * @author Ann Shumilova
 */
public class BulkOrganizationDeletionTest {
  private List<OrganizationDto> organizations;
  private OrganizationDto organization1;
  private OrganizationDto organization2;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private NavigationBar navigationBar;
  @Inject private ConfirmDialog confirmDialog;
  @Inject private Dashboard dashboard;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private AdminTestUser adminTestUser;

  @BeforeClass
  public void setUp() throws Exception {
    organization1 = testOrganizationServiceClient.create(generate("organization", 5));
    organization2 = testOrganizationServiceClient.create(generate("organization", 5));
    organizations = testOrganizationServiceClient.getAll();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    testOrganizationServiceClient.deleteById(organization1.getId());
    testOrganizationServiceClient.deleteById(organization2.getId());
  }

  @Test
  public void testOrganizationBulkDeletion() {
    navigationBar.waitNavigationBar();
    int organizationsCount = organizations.size();

    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
    assertTrue(organizationListPage.getValues(NAME).contains(organization1.getName()));
    assertTrue(organizationListPage.getValues(NAME).contains(organization2.getName()));

    // Tests delete:
    assertFalse(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(organization1.getName());
    organizationListPage.clickCheckbox(organization2.getName());
    assertTrue(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(organization1.getName());
    organizationListPage.clickCheckbox(organization2.getName());
    assertFalse(organizationListPage.isBulkDeleteVisible());
    organizationListPage.clickCheckbox(organization1.getName());
    organizationListPage.clickCheckbox(organization2.getName());
    assertTrue(organizationListPage.isBulkDeleteVisible());

    // Delete all organizations
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

    organizationListPage.waitForOrganizationIsRemoved(organization1.getName());
    organizationListPage.waitForOrganizationIsRemoved(organization2.getName());
    assertEquals(organizationListPage.getOrganizationListItemCount(), 0);
    assertFalse(organizationListPage.getValues(NAME).contains(organization1.getName()));
    assertFalse(organizationListPage.getValues(NAME).contains(organization2.getName()));
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), 0);
  }
}
