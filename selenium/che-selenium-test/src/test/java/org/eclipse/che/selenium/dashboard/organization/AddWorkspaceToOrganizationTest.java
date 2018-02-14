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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddWorkspaceToOrganizationTest {

  private static final String WS_NAME1 = generate("workspace", 4);
  private static final String WS_NAME2 = generate("workspace", 4);
  private static final String WS_NAME3 = generate("workspace", 4);

  @InjectTestOrganization(prefix = "organization1")
  private TestOrganization organization1;

  @InjectTestOrganization(prefix = "organization1")
  private TestOrganization organization2;

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

    createWorkspace(organization1.getName(), WS_NAME1);
    createWorkspace(organization2.getName(), WS_NAME2);

    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(organization1.getName(), WS_NAME1);
    workspaces.waitWorkspaceIsPresent(organization2.getName(), WS_NAME2);
    dashboard.checkWorkspaceNamePresentInRecentList(WS_NAME1);
    dashboard.checkWorkspaceNamePresentInRecentList(WS_NAME2);

    // check link to organization1 exists and open the organization2 page
    workspaces.selectWorkspaceItemName(WS_NAME1);
    workspaceDetails.waitToolbarTitleName(WS_NAME1);
    Assert.assertEquals(workspaceDetails.getOrganizationName(), organization1.getName());
    workspaceDetails.clickOnOpenOrganizationButton();
    organizationPage.waitOrganizationTitle(organization1.getName());

    // check link to organization2 exists and open the organization2 page
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(WS_NAME2);
    workspaceDetails.waitToolbarTitleName(WS_NAME2);
    // TODO change after issue is fixed
    Assert.assertEquals(workspaceDetails.getOrganizationName(), organization1.getName());
    workspaceDetails.clickOnOpenOrganizationButton();
    organizationPage.waitOrganizationTitle(organization1.getName());
  }

  @Test(priority = 1)
  public void checkCreateWorkspaceAsMember() {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(ORGANIZATIONS);
    organizationListPage.waitForOrganizationsToolbar();
    organizationListPage.waitForOrganizationsList();

    organizationListPage.waitOrganizationInList(organization2.getName());

    createWorkspace(organization2.getName(), WS_NAME3);

    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(organization2.getName(), WS_NAME3);
    dashboard.checkWorkspaceNamePresentInRecentList(WS_NAME3);

    // check link to organization2 exists and open the organization2 page
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(WS_NAME3);
    workspaceDetails.waitToolbarTitleName(WS_NAME3);
    // TODO change after issue is fixed
    Assert.assertEquals(workspaceDetails.getOrganizationName(), organization1.getName());
    workspaceDetails.clickOnOpenOrganizationButton();
    organizationPage.waitOrganizationTitle(organization1.getName());
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
    // TODO change after issue is fixed
    // workspaceDetails.waitToolbarTitleName(workspaceName);
    WaitUtils.sleepQuietly(4);
  }
}
