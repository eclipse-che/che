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
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.utils.WaitUtils;
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

  private static final String WS_NAME1 = generate("workspace", 4);
  private static final String WS_NAME2 = generate("workspace", 4);
  private static final String WS_NAME_SUB1 = generate("workspace", 4);

  private static final String WS_NAME3 = generate("workspace", 4);
  private static final String WS_NAME_SUB2 = generate("workspace", 4);

  private String suborgNameAdmin;
  private String suborgNameMember;

  @InjectTestOrganization(prefix = "organization1")
  private TestOrganization organization1;

  @InjectTestOrganization(parentPrefix = "organization1")
  private TestOrganization suborg1;

  @InjectTestOrganization(prefix = "organization2")
  private TestOrganization organization2;

  @InjectTestOrganization(parentPrefix = "organization2")
  private TestOrganization suborg2;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private CheMultiuserAdminDashboard dashboard;
  @Inject private AdminTestUser adminTestUser;
  @Inject private TestUser testUser;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private WorkspaceDetails workspaceDetails;

  @BeforeClass
  public void setUp() throws Exception {
    organization1.addAdmin(adminTestUser.getId());
    organization2.addAdmin(adminTestUser.getId());
    organization2.addMember(testUser.getId());
    suborg1.addAdmin(adminTestUser.getId());
    suborg2.addMember(testUser.getId());
    suborgNameAdmin = organization1.getName() + "/" + suborg1.getName();
    suborgNameMember = organization2.getName() + "/" + suborg2.getName();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @Test
  public void checkCreateWorkspaceAsAdmin() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    organizationListPage.waitOrganizationInList(organization1.getName());
    organizationListPage.waitOrganizationInList(organization2.getName());

    // create workspace for each organizations
    createWorkspace(organization1.getName(), WS_NAME1);
    createWorkspace(organization2.getName(), WS_NAME2);
    createWorkspace(suborgNameAdmin, WS_NAME_SUB1);

    // check that workspaces names exist in the Workspaces list and the Recent list
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(organization1.getName(), WS_NAME1);
    workspaces.waitWorkspaceIsPresent(organization2.getName(), WS_NAME2);
    workspaces.waitWorkspaceIsPresent(suborgNameAdmin, WS_NAME_SUB1);
    dashboard.waitWorkspaceNamePresentInRecentList(WS_NAME1);
    dashboard.waitWorkspaceNamePresentInRecentList(WS_NAME2);
    dashboard.waitWorkspaceNamePresentInRecentList(WS_NAME_SUB1);

    // check that the Namespace links are correct
    checkNamespaceLink(WS_NAME1, organization1.getName());
    checkNamespaceLink(WS_NAME2, organization2.getName());
  }

  @Test(priority = 1)
  public void checkCreateWorkspaceAsMember() {
    dashboard.logout();
    dashboard.open(testUser.getName(), testUser.getPassword());

    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    organizationListPage.waitOrganizationInList(organization2.getName());

    // create a workspace for organization2
    createWorkspace(organization2.getName(), WS_NAME3);
    createWorkspace(suborgNameMember, WS_NAME_SUB2);

    // check that workspace name exists in the Workspaces list and the Recent list
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(organization2.getName(), WS_NAME3);
    workspaces.waitWorkspaceIsPresent(suborgNameMember, WS_NAME_SUB2);
    dashboard.waitWorkspaceNamePresentInRecentList(WS_NAME3);
    dashboard.waitWorkspaceNamePresentInRecentList(WS_NAME_SUB2);

    // TODO check that this member cannot see other workspaces

    // check that the Namespace link is correct
    checkNamespaceLink(WS_NAME3, organization2.getName());

    try {
      checkNamespaceLink(WS_NAME_SUB2, suborgNameMember);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7925");
    }
  }

  private void createWorkspace(String organizationName, String workspaceName) {
    // create a new workspace for parenOrg organization2
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.openOrganizationsList();
    newWorkspace.selectOrganizationFromList(organizationName);
    newWorkspace.selectStack(JAVA.getId());
    newWorkspace.typeWorkspaceName(workspaceName);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    // TODO change after https://github.com/eclipse/che/issues/8497 issue is fixed
    // workspaceDetails.waitToolbarTitleName(workspaceName);
    WaitUtils.sleepQuietly(2);
  }

  private void checkNamespaceLink(String workspaceName, String organizationName) {
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
