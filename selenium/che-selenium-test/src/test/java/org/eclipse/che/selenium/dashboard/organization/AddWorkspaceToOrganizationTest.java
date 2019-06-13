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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CheMultiuserAdminDashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.MULTIUSER, TestGroup.DOCKER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class AddWorkspaceToOrganizationTest {

  private static final String WORKSPACE_FOR_ADMIN_1 = generate("workspace", 4);
  private static final String WORKSPACE_FOR_ADMIN_2 = generate("workspace", 4);
  private static final String WORKSPACE_FOR_ADMIN_3 = generate("workspace", 4);
  private static final String WORKSPACE_FOR_MEMBER_1 = generate("workspace", 4);
  private static final String WORKSPACE_FOR_MEMBER_2 = generate("workspace", 4);

  private String suborgForAdminName;
  private String suborgForMemberName;

  @InjectTestOrganization(prefix = "org1")
  private TestOrganization org1;

  @InjectTestOrganization(parentPrefix = "org1")
  private TestOrganization suborg1;

  @InjectTestOrganization(prefix = "org2")
  private TestOrganization org2;

  @InjectTestOrganization(parentPrefix = "org2")
  private TestOrganization suborg2;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private CheMultiuserAdminDashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private TestUser adminTestUser;
  @Inject private TestUser testUser;

  @BeforeClass
  public void setUp() throws Exception {
    org1.addAdmin(adminTestUser.getId());
    org2.addAdmin(adminTestUser.getId());
    org2.addMember(testUser.getId());
    suborg1.addAdmin(adminTestUser.getId());
    suborg2.addMember(testUser.getId());

    suborgForAdminName = org1.getName() + "/" + suborg1.getName();
    suborgForMemberName = org2.getName() + "/" + suborg2.getName();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @Test
  public void testCreationOfWorkspaceByAdmin() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    organizationListPage.waitOrganizationInList(org1.getName());
    organizationListPage.waitOrganizationInList(org2.getName());

    // create workspace for each organizations
    createWorkspace(org1.getName(), WORKSPACE_FOR_ADMIN_1);
    createWorkspace(org2.getName(), WORKSPACE_FOR_ADMIN_2);

    // create workspace for suborganization
    createWorkspace(suborgForAdminName, WORKSPACE_FOR_ADMIN_3);

    // check that workspaces names exist in the Workspaces list and the Recent list
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(org1.getName(), WORKSPACE_FOR_ADMIN_1);
    workspaces.waitWorkspaceIsPresent(org2.getName(), WORKSPACE_FOR_ADMIN_2);
    workspaces.waitWorkspaceIsPresent(suborgForAdminName, WORKSPACE_FOR_ADMIN_3);
    assertTrue(dashboard.isWorkspacePresentedInRecentList(WORKSPACE_FOR_ADMIN_1));
    assertTrue(dashboard.isWorkspacePresentedInRecentList(WORKSPACE_FOR_ADMIN_2));
    assertTrue(dashboard.isWorkspacePresentedInRecentList(WORKSPACE_FOR_ADMIN_3));

    // check that the Namespace link in workspace details correct
    checkNamespaceLink(org1.getName(), WORKSPACE_FOR_ADMIN_1);
    checkNamespaceLink(org2.getName(), WORKSPACE_FOR_ADMIN_2);
    checkNamespaceLink(suborgForAdminName, WORKSPACE_FOR_ADMIN_3);
  }

  @Test(priority = 1)
  public void testCreationOfWorkspaceByMember() {
    dashboard.logout();
    dashboard.open(testUser.getName(), testUser.getPassword());

    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();
    organizationListPage.waitOrganizationInList(org2.getName());

    // create a workspace for org2 and its suborganization
    createWorkspace(org2.getName(), WORKSPACE_FOR_MEMBER_1);
    createWorkspace(suborgForMemberName, WORKSPACE_FOR_MEMBER_2);

    // check that workspace name exists in the Workspaces list and the Recent list
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(org2.getName(), WORKSPACE_FOR_MEMBER_1);
    workspaces.waitWorkspaceIsPresent(suborgForMemberName, WORKSPACE_FOR_MEMBER_2);
    assertTrue(dashboard.isWorkspacePresentedInRecentList(WORKSPACE_FOR_MEMBER_1));
    assertTrue(dashboard.isWorkspacePresentedInRecentList(WORKSPACE_FOR_MEMBER_2));

    // check that workspaces names of other users are not exist in the Recent list
    assertFalse(dashboard.isWorkspacePresentedInRecentList(WORKSPACE_FOR_ADMIN_1));
    assertFalse(dashboard.isWorkspacePresentedInRecentList(WORKSPACE_FOR_ADMIN_3));

    // check that the Namespace link in workspace details correct
    checkNamespaceLink(org2.getName(), WORKSPACE_FOR_MEMBER_1);
    checkNamespaceLink(suborgForMemberName, WORKSPACE_FOR_MEMBER_2);

    // check that created workspace exists in organization Workspaces tab
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitOrganizationInList(org2.getName());
    organizationListPage.clickOnOrganization(org2.getName());
    organizationPage.clickOnWorkspacesTab();
    workspaces.waitWorkspaceIsPresent(WORKSPACE_FOR_MEMBER_1);
    workspaces.selectWorkspaceItemName(WORKSPACE_FOR_MEMBER_1);
    workspaceDetails.waitToolbarTitleName(WORKSPACE_FOR_MEMBER_1);
  }

  private void createWorkspace(String organizationName, String workspaceName) {
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitOrganizationInList(organizationName);
    organizationListPage.clickOnOrganization(organizationName);
    organizationPage.clickOnWorkspacesTab();
    organizationPage.clickOnAddWorkspaceBtn();

    newWorkspace.waitToolbar();
    newWorkspace.openOrganizationsList();
    newWorkspace.selectOrganizationFromList(organizationName);
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.typeWorkspaceName(workspaceName);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceDetails.waitToolbarTitleName(workspaceName);
  }

  private void checkNamespaceLink(String organizationName, String workspaceName) {
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaceDetails.waitToolbarTitleName(workspaceName);
    Assert.assertEquals(workspaceDetails.getOrganizationName(), organizationName);

    workspaceDetails.clickOnOpenOrganizationButton();
    organizationPage.waitOrganizationTitle(organizationName);
  }
}
