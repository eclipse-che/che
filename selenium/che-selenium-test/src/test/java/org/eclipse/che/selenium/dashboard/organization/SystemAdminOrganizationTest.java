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

import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.ACTIONS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.AVAILABLE_RAM;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.MEMBERS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.SUB_ORGANIZATIONS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.TOTAL_RAM;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.annotation.Multiuser;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CheMultiuserAdminDashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization views for system admin.
 *
 * @author Ann Shumilova
 */
@Multiuser
public class SystemAdminOrganizationTest {
  private int initialRootOrgNumber;

  @InjectTestOrganization(prefix = "parentOrg")
  private TestOrganization parentOrg;

  @InjectTestOrganization(parentPrefix = "parentOrg")
  private TestOrganization childOrg;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private CheMultiuserAdminDashboard dashboard;
  @Inject private AdminTestUser adminTestUser;

  @BeforeClass
  public void setUp() throws Exception {
    parentOrg.addAdmin(adminTestUser.getId());
    initialRootOrgNumber = testOrganizationServiceClient.getAllRoot().size();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @Test
  public void testOrganizationListComponents() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    // Test UI views of organizations list
    assertEquals(organizationListPage.getOrganizationsToolbarTitle(), "Organizations");

    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), initialRootOrgNumber);

    try {
      assertEquals(organizationListPage.getOrganizationListItemCount(), initialRootOrgNumber);
    } catch (AssertionError a) {
      // remove try-catch block after https://github.com/eclipse/che/issues/7279 has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7279", a);
    }

    assertTrue(organizationListPage.isAddOrganizationButtonVisible());
    assertTrue(organizationListPage.isSearchInputVisible());

    // Test that all headers are present:
    ArrayList<String> headers = organizationListPage.getOrganizationListHeaders();
    assertTrue(headers.contains(NAME.getTitle()));
    assertTrue(headers.contains(MEMBERS.getTitle()));
    assertTrue(headers.contains(TOTAL_RAM.getTitle()));
    assertTrue(headers.contains(AVAILABLE_RAM.getTitle()));
    assertTrue(headers.contains(SUB_ORGANIZATIONS.getTitle()));
    assertTrue(headers.contains(ACTIONS.getTitle()));

    assertTrue(organizationListPage.getValues(NAME).contains(parentOrg.getQualifiedName()));
  }

  @Test
  public void testOrganizationViews() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    // Open parent organization and check system admin permissions
    organizationListPage.clickOnOrganization(parentOrg.getName());
    organizationPage.waitOrganizationName(parentOrg.getName());
    assertFalse(organizationPage.isOrganizationNameReadonly());
    assertFalse(organizationPage.isWorkspaceCapReadonly());
    assertFalse(organizationPage.isRunningCapReadonly());
    assertFalse(organizationPage.isRAMCapReadonly());
    assertTrue(organizationPage.isDeleteButtonVisible());

    // Test UI views of the Members tab
    organizationPage.clickMembersTab();
    organizationPage.waitMembersList();
    assertTrue(organizationPage.isAddMemberButtonVisible());

    // Test UI views of the Sub-Organizations tab
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForOrganizationsList();
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertTrue(organizationListPage.isAddSubOrganizationButtonVisible());
    assertTrue(organizationListPage.getValues(NAME).contains(childOrg.getQualifiedName()));

    // Create a sub-organization and test system admin permissions
    organizationListPage.clickOnOrganization(childOrg.getQualifiedName());
    organizationPage.waitOrganizationTitle(childOrg.getQualifiedName());
    assertFalse(organizationPage.isOrganizationNameReadonly());
    assertFalse(organizationPage.isWorkspaceCapReadonly());
    assertFalse(organizationPage.isRunningCapReadonly());
    assertFalse(organizationPage.isRAMCapReadonly());
    assertTrue(organizationPage.isDeleteButtonVisible());

    // Test UI views of the Members tab
    organizationPage.clickMembersTab();
    organizationPage.waitMembersList();
    assertTrue(organizationPage.isAddMemberButtonVisible());

    // Test the Sub-Organization tab
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForSubOrganizationsEmptyList();
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertTrue(organizationListPage.isAddSubOrganizationButtonVisible());

    // Back to the parent organization
    organizationPage.clickBackButton();
    organizationPage.waitOrganizationName(parentOrg.getName());
  }
}
