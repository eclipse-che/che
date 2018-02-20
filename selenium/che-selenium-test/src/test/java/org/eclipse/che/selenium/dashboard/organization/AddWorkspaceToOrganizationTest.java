/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.pageobject.dashboard.NavigationBar.MenuItem.ORGANIZATIONS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CheMultiuserAdminDashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationListPage;
import org.eclipse.che.selenium.pageobject.dashboard.organization.OrganizationPage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddWorkspaceToOrganizationTest {

  private static final String WORKSPACE_NAME1 = generate("workspace", 4);
  private static final String WORKSPACE_NAME2 = generate("workspace", 4);
  private static final String WORKSPACE_NAME3 = generate("workspace", 4);
  private static final String WORKSPACE_NAME4 = generate("workspace", 4);
  private static final String WORKSPACE_NAME5 = generate("workspace", 4);

  private String suborgName1;
  private String suborgName2;

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
  @Inject private AdminTestUser adminTestUser;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private TestUser testUser;

  @BeforeClass
  public void setUp() throws Exception {
    org1.addAdmin(adminTestUser.getId());
    org2.addAdmin(adminTestUser.getId());
    org2.addMember(testUser.getId());
    suborg1.addAdmin(adminTestUser.getId());
    suborg2.addMember(testUser.getId());

    suborgName1 = org1.getName() + "/" + suborg1.getName();
    suborgName2 = org2.getName() + "/" + suborg2.getName();

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
    createWorkspace(org1.getName(), WORKSPACE_NAME1);
    createWorkspace(org2.getName(), WORKSPACE_NAME2);

    // create workspace for suborganization
    createWorkspace(suborgName1, WORKSPACE_NAME4);

    // check that workspaces names exist in the Workspaces list and the Recent list
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(org1.getName(), WORKSPACE_NAME1);
    workspaces.waitWorkspaceIsPresent(org2.getName(), WORKSPACE_NAME2);
    workspaces.waitWorkspaceIsPresent(suborgName1, WORKSPACE_NAME4);
    assertTrue(dashboard.isWorkspaceNamePresentInRecentList(WORKSPACE_NAME1));
    assertTrue(dashboard.isWorkspaceNamePresentInRecentList(WORKSPACE_NAME2));
    assertTrue(dashboard.isWorkspaceNamePresentInRecentList(WORKSPACE_NAME4));

    // check that the Namespace link in workspace details correct
    checkNamespaceLink(org1.getName(), WORKSPACE_NAME1);
    checkNamespaceLink(org2.getName(), WORKSPACE_NAME2);
    try {
      checkNamespaceLink(suborgName1, WORKSPACE_NAME4);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7925");
    }
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
    createWorkspace(org2.getName(), WORKSPACE_NAME3);
    createWorkspace(suborgName2, WORKSPACE_NAME5);

    // check that workspace name exists in the Workspaces list and the Recent list
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(org2.getName(), WORKSPACE_NAME3);
    workspaces.waitWorkspaceIsPresent(suborgName2, WORKSPACE_NAME5);
    assertTrue(dashboard.isWorkspaceNamePresentInRecentList(WORKSPACE_NAME3));
    assertTrue(dashboard.isWorkspaceNamePresentInRecentList(WORKSPACE_NAME5));

    // check that workspaces names of other users are not exist in the Recent list
    assertFalse(dashboard.isWorkspaceNamePresentInRecentList(WORKSPACE_NAME1));
    assertFalse(dashboard.isWorkspaceNamePresentInRecentList(WORKSPACE_NAME4));

    // check that the Namespace link in workspace details correct
    checkNamespaceLink(org2.getName(), WORKSPACE_NAME3);

    try {
      checkNamespaceLink(suborgName2, WORKSPACE_NAME5);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7925");
    }
  }

  private void createWorkspace(String organizationName, String workspaceName) {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.openOrganizationsList();
    newWorkspace.selectOrganizationFromList(organizationName);
    newWorkspace.selectStack(JAVA.getId());
    newWorkspace.typeWorkspaceName(workspaceName);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();

    try {
      workspaceDetails.waitToolbarTitleName(workspaceName);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8497");
    }
  }

  private void checkNamespaceLink(String organizationName, String workspaceName) {
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(workspaceName);
    workspaceDetails.waitToolbarTitleName(workspaceName);

    try {
      Assert.assertEquals(workspaceDetails.getOrganizationName(), organizationName);
    } catch (AssertionError ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8776");
    }

    workspaceDetails.clickOnOpenOrganizationButton();
    organizationPage.waitOrganizationTitle(organizationName);
  }
}
