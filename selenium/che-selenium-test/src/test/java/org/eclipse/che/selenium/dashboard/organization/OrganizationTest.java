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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Arrays;
import java.util.List;
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
public class OrganizationTest {

  private String orgName;
  private List<String> emailsList;
  private OrganizationDto organization;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private AddOrganization addOrganization;
  @Inject private AddMember addMember;
  @Inject private Loader loader;

  @Inject
  @Named("admin")
  private TestOrganizationServiceClient organizationServiceClient;

  @Inject private Dashboard dashboard;
  @Inject private TestUser testUser1;
  @Inject private TestUser memberUser;
  @Inject private AdminTestUser adminTestUser;

  @BeforeClass
  public void setUp() throws Exception {
    emailsList = Arrays.asList(testUser1.getEmail());
    String firstName = generate("F", 7);
    String lastName = generate("L", 7);

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    orgName = generate("orgX", 6);

    organization = organizationServiceClient.create(orgName);
  }

  @AfterClass
  public void tearDown() throws Exception {
    organizationServiceClient.deleteById(organization.getId());
  }

  @Test
  public void operationsWithMembers() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickOnOrganization(organization.getQualifiedName());
    organizationPage.waitOrganizationName(orgName);

    //Add members to a members list ad 'Admin'
    loader.waitOnClosed();
    organizationPage.clickMembersTab();
    for (String email : emailsList) {
      organizationPage.clickAddMemberButton();
      addMember.waitAddMemberWidget();
      addMember.setMembersEmail(email);
      addMember.clickAdminButton();
      addMember.clickAddButton();
      organizationPage.checkMemberExistsInMembersList(email);
    }

    //Search members from the members list
    for (String email : emailsList) {
      organizationPage.clearSearchField();
      String memberName = organizationPage.getMembersNameByEmail(email);
      organizationPage.searchMembers(memberName.substring(0, (memberName.length() / 2)));
      organizationPage.checkMemberExistsInMembersList(email);
    }
    organizationPage.clearSearchField();

    //Change the members role to 'Members'
    for (String email : emailsList) {
      loader.waitOnClosed();
      addMember.clickEditPermissionsButton(email);
      addMember.clickMemberButton();
      addMember.clickSaveButton();
    }

    //Delete the members from the members list
    for (String email : emailsList) {
      organizationPage.deleteMember(email);
    }
  }

  //@Test(priority = 1)
  public void addOrganizationWithMembers() {
    String name = generate("orgY", 4);
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.clickAddOrganizationButton();
    addOrganization.waitAddOrganization();
    addOrganization.setOrganizationName(name);

    addOrganization.clickAddMemberButton();
    addMember.waitAddMemberWidget();
    addMember.setMembersEmail(memberUser.getEmail());
    addMember.clickAddButton();

    addOrganization.clickAddMemberButton();
    addMember.waitAddMemberWidget();
    addMember.clickCancelButton();
    addOrganization.waitAddOrganization();
    loader.waitOnClosed();
    addOrganization.clickCreateOrganizationButton();

    organizationPage.waitOrganizationName(name);
    organizationPage.clickMembersTab();

    organizationPage.clickSettingsTab();
  }
}
