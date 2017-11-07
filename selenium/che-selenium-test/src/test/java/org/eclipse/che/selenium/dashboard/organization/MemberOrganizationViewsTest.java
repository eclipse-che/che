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
import static org.testng.Assert.*;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test validates organization views for simple user being a member of any organization.
 *
 * @author Ann Shumilova
 */
public class MemberOrganizationViewsTest {
  private static final String PARENT_ORG_NAME = generate("organization", 5);
  private static final String CHILD_ORG_NAME = generate("organization", 5);

  private List<OrganizationDto> organizations;
  private OrganizationDto parentOrganization;
  private OrganizationDto childOrganization;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private Dashboard dashboard;
  @Inject private TestUser testUser;

  @BeforeClass
  public void setUp() throws Exception {
    parentOrganization = testOrganizationServiceClient.create(PARENT_ORG_NAME);
    childOrganization =
        testOrganizationServiceClient.create(CHILD_ORG_NAME, parentOrganization.getId());
    testOrganizationServiceClient.addMember(parentOrganization.getId(), testUser.getId());
    testOrganizationServiceClient.addMember(childOrganization.getId(), testUser.getId());
    organizations = testOrganizationServiceClient.getAll();

    dashboard.open(testUser.getName(), testUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    for (OrganizationDto organization : organizations)
      testOrganizationServiceClient.deleteById(organization.getId());
  }

  @Test
  public void testOrganizationListComponents() {
    int organizationsCount = 2;

    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    // Test UI views of organizations list
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), organizationsCount);
    assertEquals(organizationListPage.getOrganizationsToolbarTitle(), "Organizations");
    assertEquals(navigationBar.getMenuCounterValue(ORGANIZATIONS), organizationsCount);
    assertEquals(organizationListPage.getOrganizationListItemCount(), organizationsCount);
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertTrue(organizationListPage.isSearchInputVisible());

    // Check all headers are present:
    ArrayList<String> headers = organizationListPage.getOrganizationListHeaders();
    assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.NAME.getTitle()));
    assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.MEMBERS.getTitle()));
    assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.TOTAL_RAM.getTitle()));
    assertTrue(
        headers.contains(OrganizationListPage.OrganizationListHeader.AVAILABLE_RAM.getTitle()));
    assertTrue(
        headers.contains(OrganizationListPage.OrganizationListHeader.SUB_ORGANIZATIONS.getTitle()));
    assertTrue(headers.contains(OrganizationListPage.OrganizationListHeader.ACTIONS.getTitle()));

    assertTrue(
        organizationListPage
            .getValues(OrganizationListPage.OrganizationListHeader.NAME)
            .contains(parentOrganization.getQualifiedName()));
    assertTrue(
        organizationListPage
            .getValues(OrganizationListPage.OrganizationListHeader.NAME)
            .contains(childOrganization.getQualifiedName()));
  }

  @Test
  public void testParentOrganization() {
    organizationListPage.clickOnOrganization(parentOrganization.getName());
    organizationPage.waitOrganizationName(parentOrganization.getName());

    // Test UI for member of parent organization
    assertTrue(organizationPage.isOrganizationNameReadonly());
    assertTrue(organizationPage.isWorkspaceCapReadonly());
    assertTrue(organizationPage.isRunningCapReadonly());
    assertTrue(organizationPage.isRAMCapReadonly());
    assertTrue(organizationPage.isWorkspaceCapReadonly());
    assertFalse(organizationPage.isDeleteButtonVisible());

    // Test UI views of organization Members tab
    organizationPage.clickMembersTab();
    organizationPage.waitMembersList();
    assertFalse(organizationPage.isAddMemberButtonVisible());

    // Test UI views of organization Sub-Organizations tab
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForOrganizationsList();
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertFalse(organizationListPage.isAddSubOrganizationButtonVisible());
    assertTrue(
        organizationListPage
            .getValues(OrganizationListPage.OrganizationListHeader.NAME)
            .contains(childOrganization.getQualifiedName()));

    organizationPage.clickBackButton();
    organizationListPage.waitForOrganizationsList();
    assertEquals(organizationListPage.getOrganizationListItemCount(), 2);
  }

  @Test
  public void testChildOrganization() {
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(childOrganization.getQualifiedName());
    organizationPage.waitOrganizationName(childOrganization.getName());

    // Test UI for member of child organization
    assertTrue(organizationPage.isOrganizationNameReadonly());
    assertTrue(organizationPage.isWorkspaceCapReadonly());
    assertTrue(organizationPage.isRunningCapReadonly());
    assertTrue(organizationPage.isRAMCapReadonly());
    assertTrue(organizationPage.isWorkspaceCapReadonly());
    assertFalse(organizationPage.isDeleteButtonVisible());

    // Test UI views of organization Members tab
    organizationPage.clickMembersTab();
    organizationPage.waitMembersList();
    assertFalse(organizationPage.isAddMemberButtonVisible());

    // Test UI views of organization Sub-Organizations tab
    organizationPage.clickSubOrganizationsTab();
    organizationListPage.waitForSubOrganizationsEmptyList();
    assertFalse(organizationListPage.isAddOrganizationButtonVisible());
    assertFalse(organizationListPage.isAddSubOrganizationButtonVisible());

    organizationPage.clickBackButton();
    organizationPage.waitOrganizationName(parentOrganization.getName());
  }
}
