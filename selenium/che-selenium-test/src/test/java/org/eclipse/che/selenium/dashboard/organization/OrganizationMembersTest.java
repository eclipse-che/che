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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.eclipse.che.selenium.core.client.TestOrganizationServiceClient;
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
public class OrganizationMembersTest {
  private static final String PRE_CREATED_ORG_NAME = generate("organization", 5);
  private static final String NEW_ORG_NAME = generate("new-org", 5);

  private OrganizationDto organization;
  private String memberEmail;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient testOrganizationServiceClient;

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
    memberEmail = testUser.getEmail();

    organization = testOrganizationServiceClient.create(PRE_CREATED_ORG_NAME);
    testOrganizationServiceClient.addAdmin(organization.getId(), adminTestUser.getId());
    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    testOrganizationServiceClient.deleteById(organization.getId());
    testOrganizationServiceClient.deleteByName(NEW_ORG_NAME);
  }

  @Test
  public void testOperationsWithMembersInExistsOrganization() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(organization.getQualifiedName());
    organizationPage.waitOrganizationName(PRE_CREATED_ORG_NAME);

    // Add members to a members list as 'Admin'
    loader.waitOnClosed();
    organizationPage.clickMembersTab();
    organizationPage.clickAddMemberButton();
    addMember.waitAddMemberWidget();
    addMember.setMembersEmail(memberEmail);
    addMember.clickAdminButton();
    addMember.clickAddButton();
    organizationPage.checkMemberExistsInMembersList(memberEmail);

    // Change the members role to 'Members'
    loader.waitOnClosed();
    addMember.clickEditPermissionsButton(memberEmail);
    addMember.clickMemberButton();
    addMember.clickSaveButton();

    // Search members from the members list
    organizationPage.clearSearchField();
    String memberName = organizationPage.getMembersNameByEmail(memberEmail);
    organizationPage.searchMembers(memberName.substring(0, (memberName.length() / 2)));
    organizationPage.checkMemberExistsInMembersList(memberEmail);
    organizationPage.clearSearchField();

    // Delete the members from the members list
    organizationPage.deleteMember(memberEmail);
  }

  @Test
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
