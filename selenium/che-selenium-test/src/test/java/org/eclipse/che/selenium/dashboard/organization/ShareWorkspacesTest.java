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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
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
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.TabNames;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceShare;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.MULTIUSER})
public class ShareWorkspacesTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String SYSTEM_ADMIN_PERMISSIONS =
      "read, use, run, configure, setPermissions, delete";
  private static final String ADMIN_PERMISSIONS = "read, use, run, configure, setPermissions";
  private static final String MEMBER_PERMISSIONS = "read, use, run, configure";

  private String systemAdminName;
  private String adminName;
  private String memberName;

  @InjectTestOrganization(prefix = "org")
  private TestOrganization org;

  @Inject private OrganizationListPage organizationListPage;
  @Inject private CheMultiuserAdminDashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private OrganizationPage organizationPage;
  @Inject private NavigationBar navigationBar;
  @Inject private AdminTestUser adminTestUser;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private TestUser testUser;
  @Inject private TestUser testUser2;
  @Inject private WorkspaceShare workspaceShare;

  @BeforeClass
  public void setUp() throws Exception {
    org.addAdmin(adminTestUser.getId());
    org.addMember(testUser.getId());
    org.addAdmin(testUser2.getId());
    systemAdminName = adminTestUser.getEmail();
    memberName = testUser.getEmail();
    adminName = testUser2.getEmail();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    createWorkspace(org.getName(), WORKSPACE_NAME);
  }

  @Test
  public void checkShareWorkspaceTab() {
    navigationBar.waitNavigationBar();

    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(WORKSPACE_NAME);
    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);
    workspaceDetails.selectTabInWorkspaceMenu(TabNames.SHARE);

    workspaceShare.waitMemberNameInShareList(systemAdminName);
    Assert.assertEquals(
        workspaceShare.getMemberPermissions(systemAdminName), SYSTEM_ADMIN_PERMISSIONS);

    // TODO check/uncheck members
    workspaceShare.clickOnMemberCheckbox(systemAdminName);
    Assert.assertEquals(workspaceShare.isMemberCheckedInList(systemAdminName), "true");

    // add a new member
    workspaceShare.clickOnAddDeveloperButton();
    workspaceShare.waitInviteMemberDialog();
    workspaceShare.selectAllMembersInDialogByBulk();
    workspaceShare.clickOnShareWorkspaceButton();

    // check the added member permission
    workspaceShare.waitMemberNameInShareList(memberName);
    Assert.assertEquals(workspaceShare.getMemberPermissions(memberName), ADMIN_PERMISSIONS);

    // remove the member from the members list
    workspaceShare.clickOnRemoveMemberButton(memberName);
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    workspaceShare.waitMemberNameNotExistsInShareList(memberName);
  }

  @Test
  public void shareWorkspaceWithMember() {}

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
    workspaceDetails.waitToolbarTitleName(workspaceName);
  }
}
