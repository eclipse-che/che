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
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.ACTIONS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.AVAILABLE_RAM;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.MEMBERS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.NAME;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.SUB_ORGANIZATIONS;
import static org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage.OrganizationListHeader.TOTAL_RAM;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.ArrayList;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CheMultiuserAdminDashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization views for admin of the organization.
 *
 * @author Ann Shumilova
 */
public class AdminOfParentOrganizationTest {
  private static final String PARENT_ORG_NAME = generate("parent-org", 5);
  private static final String CHILD_ORG_NAME = generate("child-org", 5);

  private OrganizationDto parentOrganization;
  private OrganizationDto childOrganization;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private CheMultiuserAdminDashboard dashboard;
  @Inject private TestUser testUser;

  @BeforeClass
  public void setUp() throws Exception {
    parentOrganization = testOrganizationServiceClient.create(PARENT_ORG_NAME);
    childOrganization =
        testOrganizationServiceClient.create(CHILD_ORG_NAME, parentOrganization.getId());
    testOrganizationServiceClient.addAdmin(parentOrganization.getId(), testUser.getId());
    testOrganizationServiceClient.addMember(childOrganization.getId(), testUser.getId());

    dashboard.open(testUser.getName(), testUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    testOrganizationServiceClient.deleteById(childOrganization.getId());
    testOrganizationServiceClient.deleteById(parentOrganization.getId());
  }

  @Test
  public void testOrganizationListComponents() {
    int organizationsCount = 2;

    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    // Test UI views of organizations list
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) >= organizationsCount);
    assertEquals(organizationListPage.getOrganizationsToolbarTitle(), "Organizations");
    assertTrue(navigationBar.getMenuCounterValue(ORGANIZATIONS) >= organizationsCount);
    assertTrue(organizationListPage.getOrganizationListItemCount() >= organizationsCount);
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertTrue(organizationListPage.isSearchInputVisible());

    // Check all headers are present:
    ArrayList<String> headers = organizationListPage.getOrganizationListHeaders();
    assertTrue(headers.contains(NAME.getTitle()));
    assertTrue(headers.contains(MEMBERS.getTitle()));
    assertTrue(headers.contains(TOTAL_RAM.getTitle()));
    assertTrue(headers.contains(AVAILABLE_RAM.getTitle()));
    assertTrue(headers.contains(SUB_ORGANIZATIONS.getTitle()));
    assertTrue(headers.contains(ACTIONS.getTitle()));
    assertTrue(
        organizationListPage.getValues(NAME).contains(parentOrganization.getQualifiedName()));
    assertTrue(organizationListPage.getValues(NAME).contains(childOrganization.getQualifiedName()));
  }

  @Test
  public void testOrganizationViews() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    // Open parent organization and check admin permissions
    organizationListPage.clickOnOrganization(parentOrganization.getName());
    organizationPage.waitOrganizationName(parentOrganization.getName());
    assertFalse(organizationPage.isOrganizationNameReadonly());
    assertTrue(organizationPage.isWorkspaceCapReadonly());
    assertTrue(organizationPage.isRunningCapReadonly());
    assertTrue(organizationPage.isRAMCapReadonly());
    assertTrue(organizationPage.isWorkspaceCapReadonly());
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
    assertTrue(organizationListPage.getValues(NAME).contains(childOrganization.getQualifiedName()));

    // Create a suborganization and test admin permissions
    organizationListPage.clickOnOrganization(childOrganization.getQualifiedName());
    organizationPage.waitOrganizationTitle(childOrganization.getQualifiedName());
    assertTrue(organizationPage.isOrganizationNameReadonly());
    assertFalse(organizationPage.isWorkspaceCapReadonly());
    assertFalse(organizationPage.isRunningCapReadonly());
    assertFalse(organizationPage.isRAMCapReadonly());
    assertFalse(organizationPage.isWorkspaceCapReadonly());
    assertFalse(organizationPage.isDeleteButtonVisible());

    // Test UI views of the Members tab
    organizationPage.clickMembersTab();
    organizationPage.waitMembersList();
    assertFalse(organizationPage.isAddMemberButtonVisible());

    // Test UI views of the Sub-Organizations tab
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForSubOrganizationsEmptyList();
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertFalse(organizationListPage.isAddSubOrganizationButtonVisible());

    // Back to the parent organization
    organizationPage.clickBackButton();
    organizationPage.waitOrganizationName(parentOrganization.getName());
  }
}
