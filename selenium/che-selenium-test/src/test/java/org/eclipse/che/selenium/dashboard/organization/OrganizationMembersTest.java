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

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.CheTestAdminOrganizationServiceClient;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.organization.AddMember;
import org.eclipse.che.selenium.pageobject.dashboard.organization.AddOrganization;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Sergey Skorik */
@Test(groups = {TestGroup.MULTIUSER})
public class OrganizationMembersTest {
  private static final String NEW_ORG_NAME = generate("new-org-", 5);

  @InjectTestOrganization private TestOrganization organization;

  @Inject private CheTestAdminOrganizationServiceClient adminOrganizationServiceClient;
  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private AddOrganization addOrganization;
  @Inject private AddMember addMember;
  @Inject private Loader loader;
  @Inject private Dashboard dashboard;
  @Inject private TestUser testUser;
  @Inject private AdminTestUser adminTestUser;

  @BeforeClass
  public void setUp() throws Exception {
    organization.addAdmin(adminTestUser.getId());
    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    adminOrganizationServiceClient.deleteByName(NEW_ORG_NAME);
  }

  public void testOperationsWithMembersInExistsOrganization() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(organization.getQualifiedName());
    organizationPage.waitOrganizationName(organization.getName());

    // Add members to a members list as 'Admin'
    loader.waitOnClosed();
    organizationPage.clickMembersTab();
    organizationPage.clickAddMemberButton();
    addMember.waitAddMemberWidget();
    addMember.setMembersEmail(testUser.getEmail());
    addMember.clickAdminButton();
    addMember.clickAddButton();
    organizationPage.checkMemberExistsInMembersList(testUser.getEmail());

    // Change the members role to 'Members'
    loader.waitOnClosed();
    addMember.clickEditPermissionsButton(testUser.getEmail());
    addMember.clickMemberButton();
    addMember.clickSaveButton();

    // Search members from the members list
    organizationPage.clearSearchField();
    String memberName = organizationPage.getMembersNameByEmail(testUser.getEmail());
    organizationPage.searchMembers(memberName.substring(0, (memberName.length() / 2)));
    organizationPage.checkMemberExistsInMembersList(testUser.getEmail());
    organizationPage.clearSearchField();

    organizationPage.deleteMember(testUser.getEmail());
  }

  public void testAddingMembersToNewOrganization() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    // Start to create a new organization and add a new member
    organizationListPage.clickAddOrganizationButton();
    addOrganization.waitAddOrganization();
    addOrganization.setOrganizationName(NEW_ORG_NAME);
    addOrganization.clickAddMemberButton();
    addMember.waitAddMemberWidget();
    addMember.setMembersEmail(testUser.getEmail());
    addMember.clickAddButton();

    // Check that the Cancel button in the Add Member Widget works
    addOrganization.clickAddMemberButton();
    addMember.waitAddMemberWidget();
    addMember.clickCancelButton();
    addOrganization.waitAddOrganization();
    loader.waitOnClosed();
    addOrganization.clickCreateOrganizationButton();
    addOrganization.waitAddOrganizationButtonIsNotVisible();

    // Check that organization is created and the added member exists in the Members tab
    organizationPage.waitOrganizationName(NEW_ORG_NAME);
    organizationPage.clickMembersTab();
    organizationPage.checkMemberExistsInMembersList(testUser.getEmail());
  }
}
