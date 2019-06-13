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
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.OVERVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.SHARE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Status.STOPPED;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CheMultiuserAdminDashboard;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceShare;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.MULTIUSER, TestGroup.DOCKER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class ShareWorkspaceMemberTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String ADMIN_PERMISSIONS =
      "read, use, run, configure, setPermissions, delete";
  private static final String MEMBER_PERMISSIONS = "read, use, run, configure";
  private static final String PROJECT_NAME = "web-java-spring";
  private static final String FILE_NAME = "readme.txt";
  private static final String FILE_CONTENT = generate("", 10);

  private String systemAdminName;
  private String memberName;

  @InjectTestOrganization private TestOrganization org;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private TestUser testUser;
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private Workspaces workspaces;
  @Inject private NavigationBar navigationBar;
  @Inject private AdminTestUser adminTestUser;
  @Inject private WorkspaceShare workspaceShare;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private CheMultiuserAdminDashboard dashboard;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    org.addAdmin(adminTestUser.getId());
    org.addMember(testUser.getId());
    systemAdminName = adminTestUser.getEmail();
    memberName = testUser.getEmail();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    createWorkspace(WORKSPACE_NAME);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, adminTestUser.getName());
    org.delete();
  }

  @Test
  public void checkSharingByWorkspaceOwner() {
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(WORKSPACE_NAME);
    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);
    workspaceDetails.selectTabInWorkspaceMenu(SHARE);

    // invite a member to workspace and check permission
    workspaceShare.clickOnAddDeveloperButton();
    workspaceShare.waitInviteMemberDialog();
    workspaceShare.selectAllMembersInDialogByBulk();
    workspaceShare.clickOnShareWorkspaceButton();
    workspaceShare.waitMemberNameInShareList(systemAdminName);
    assertEquals(workspaceShare.getMemberPermissions(systemAdminName), ADMIN_PERMISSIONS);
    workspaceShare.waitMemberNameInShareList(memberName);
    assertEquals(workspaceShare.getMemberPermissions(memberName), MEMBER_PERMISSIONS);
  }

  @Test(priority = 1)
  public void checkSharingByAddedMember() {
    dashboard.logout();

    // login as developer and check that the shared workspace exists in Workspaces list
    dashboard.open(testUser.getName(), testUser.getPassword());
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME);
    workspaces.selectWorkspaceItemName(WORKSPACE_NAME);
    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);

    // check all members permission
    workspaceDetails.selectTabInWorkspaceMenu(SHARE);
    workspaceShare.waitMemberNameInShareList(memberName);
    assertEquals(workspaceShare.getMemberPermissions(memberName), MEMBER_PERMISSIONS);
    workspaceShare.waitMemberNameInShareList(systemAdminName);
    assertEquals(workspaceShare.getMemberPermissions(systemAdminName), ADMIN_PERMISSIONS);

    // try to remove the workspace owner from the members list
    workspaceShare.waitInviteMemberDialogClosed();
    workspaceShare.waitMemberNameInShareList(systemAdminName);
    workspaceShare.clickOnRemoveMemberButton(systemAdminName);
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    dashboard.waitNotificationMessage("User can't edit permissions for this instance");
    dashboard.waitNotificationIsClosed();

    // open workspace and check
    workspaceDetails.clickOpenInIdeWsBtn();
    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + FILE_NAME);
    editor.waitActive();
    editor.selectTabByName(FILE_NAME);
    editor.waitTextIntoEditor(FILE_CONTENT);

    // try to delete the workspace
    dashboard.open();
    navigationBar.waitNavigationBar();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(WORKSPACE_NAME);
    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);
    workspaceDetails.selectTabInWorkspaceMenu(OVERVIEW);

    workspaceOverview.clickOnDeleteWorkspace();
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    dashboard.waitNotificationMessage(
        "The user does not have permission to delete workspace with id ");
    dashboard.waitNotificationIsClosed();
    workspaceDetails.checkStateOfWorkspace(StateWorkspace.STOPPED);

    dashboard.logout();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME);
    workspaces.waitWorkspaceStatus(WORKSPACE_NAME, STOPPED);
  }

  private void createWorkspace(String workspaceName) {
    createWorkspaceHelper.createWorkspaceFromStackWithProject(
        Stack.JAVA_MAVEN, workspaceName, PROJECT_NAME);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(PROJECT_NAME);

    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);

    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.createNotJavaFileByName(FILE_NAME);
    editor.waitActive();
    editor.selectTabByName(FILE_NAME);
    editor.typeTextIntoEditor(FILE_CONTENT);
    editor.waitTabFileWithSavedStatus(FILE_NAME);
  }
}
