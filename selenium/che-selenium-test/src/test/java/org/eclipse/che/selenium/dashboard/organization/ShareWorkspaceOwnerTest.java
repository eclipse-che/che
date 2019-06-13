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
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.SHARE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CheMultiuserAdminDashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceShare;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.openqa.selenium.WebDriverException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(
    groups = {
      TestGroup.MULTIUSER,
      TestGroup.DOCKER,
      TestGroup.OPENSHIFT,
      TestGroup.K8S,
      TestGroup.FLAKY
    })
public class ShareWorkspaceOwnerTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String ADMIN_PERMISSIONS =
      "read, use, run, configure, setPermissions, delete";
  private static final String MEMBER_PERMISSIONS = "read, use, run, configure";

  private String systemAdminName;
  private String memberName;

  @InjectTestOrganization private TestOrganization org;

  @Inject private CheMultiuserAdminDashboard dashboard;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private NavigationBar navigationBar;
  @Inject private AdminTestUser adminTestUser;
  @Inject private NewWorkspace newWorkspace;
  @Inject private Workspaces workspaces;
  @Inject private TestUser testUser;
  @Inject private WorkspaceShare workspaceShare;

  @BeforeClass
  public void setUp() throws Exception {
    org.addAdmin(adminTestUser.getId());
    org.addMember(testUser.getId());
    systemAdminName = adminTestUser.getEmail();
    memberName = testUser.getEmail();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    createWorkspace(org.getName(), WORKSPACE_NAME);
  }

  @BeforeMethod
  public void openShareWorkspaceTab() {
    navigationBar.waitNavigationBar();

    dashboard.selectWorkspacesItemOnDashboard();

    try {
      workspaces.selectWorkspaceItemName(WORKSPACE_NAME);
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/8594");
    }

    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);
    workspaceDetails.selectTabInWorkspaceMenu(SHARE);
  }

  @Test
  public void checkShareWorkspaceTab() {
    // check workspace owner permissions
    workspaceShare.waitMemberNameInShareList(systemAdminName);
    assertEquals(workspaceShare.getMemberPermissions(systemAdminName), ADMIN_PERMISSIONS);
  }

  @Test
  public void checkMembersSelectingByCheckbox() {
    // check selecting member by checkbox
    workspaceShare.clickOnMemberCheckbox(systemAdminName);
    assertTrue(workspaceShare.isMemberCheckedInList(systemAdminName));
    workspaceShare.clickOnMemberCheckbox(systemAdminName);
    assertFalse(workspaceShare.isMemberCheckedInList(systemAdminName));

    // check selecting members by Bulk
    workspaceShare.clickOnBulkSelection();
    assertTrue(workspaceShare.isMemberCheckedInList(systemAdminName));
    workspaceShare.clickOnBulkSelection();
    assertFalse(workspaceShare.isMemberCheckedInList(systemAdminName));
  }

  @Test
  public void checkMembersFiltering() {
    // filter members by a full name
    workspaceShare.filterMembers(systemAdminName);
    workspaceShare.waitMemberNameInShareList(systemAdminName);

    // filter members by a part name
    workspaceShare.filterMembers(systemAdminName.substring(systemAdminName.length() / 2));
    workspaceShare.waitMemberNameInShareList(systemAdminName);

    // filter members by a nonexistent name
    workspaceShare.filterMembers(NameGenerator.generate("", 8));
    workspaceShare.waitMemberNameNotExistsInShareList(systemAdminName);
  }

  @Test
  public void checkSharingWorkspaceWithMember() {
    // invite a member to workspace
    workspaceShare.clickOnAddDeveloperButton();
    workspaceShare.waitInviteMemberDialog();
    workspaceShare.selectAllMembersInDialogByBulk();
    workspaceShare.clickOnShareWorkspaceButton();

    // check the added member permission
    workspaceShare.waitMemberNameInShareList(memberName);
    assertEquals(workspaceShare.getMemberPermissions(memberName), MEMBER_PERMISSIONS);

    // check the 'No members in team' dialog
    workspaceShare.clickOnAddDeveloperButton();
    workspaceShare.waitNoMembersDialog();
    workspaceDetails.clickOnCloseButtonInDialogWindow();

    // remove the added member from the members list
    workspaceShare.waitInviteMemberDialogClosed();
    workspaceShare.waitMemberNameInShareList(memberName);
    workspaceShare.clickOnRemoveMemberButton(memberName);
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    workspaceShare.waitMemberNameNotExistsInShareList(memberName);
  }

  private void createWorkspace(String organizationName, String workspaceName) {
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.openOrganizationsList();
    newWorkspace.selectOrganizationFromList(organizationName);
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.typeWorkspaceName(workspaceName);
    newWorkspace.clickOnCreateButtonAndEditWorkspace();
    workspaceDetails.waitToolbarTitleName(workspaceName);
  }
}
