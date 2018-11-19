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
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.BUILD_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.OVERVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.SHARE;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CheMultiuserAdminDashboard;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceShare;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.MULTIUSER, TestGroup.DOCKER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class ShareWorkspaceDeveloperTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String ADMIN_PERMISSIONS =
      "read, use, run, configure, setPermissions, delete";
  private static final String MEMBER_PERMISSIONS = "read, use, run, configure";
  private static final String WEB_JAVA_SPRING_PROJECT = "web-java-spring";

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
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private Consoles consoles;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;

  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private Preferences preferences;
  @Inject private AskForValueDialog askForValueDialog;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() throws Exception {
    org.addAdmin(adminTestUser.getId());
    org.addMember(testUser.getId());
    systemAdminName = adminTestUser.getEmail();
    memberName = testUser.getEmail();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
    createWorkspace(org.getName(), WORKSPACE_NAME);
  }

  @Test
  public void checkWorkspaceSharingByWItsOwner() {
    dashboard.open();
    navigationBar.waitNavigationBar();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectWorkspaceItemName(WORKSPACE_NAME);
    workspaceDetails.waitToolbarTitleName(WORKSPACE_NAME);
    workspaceDetails.selectTabInWorkspaceMenu(SHARE);

    // check workspace owner permissions
    workspaceShare.waitMemberNameInShareList(systemAdminName);
    assertEquals(workspaceShare.getMemberPermissions(systemAdminName), ADMIN_PERMISSIONS);

    // invite a member to workspace
    workspaceShare.clickOnAddDeveloperButton();
    workspaceShare.waitInviteMemberDialog();
    workspaceShare.selectAllMembersInDialogByBulk();
    workspaceShare.clickOnShareWorkspaceButton();
    workspaceShare.waitMemberNameInShareList(memberName);

    workspaceShare.waitMemberNameInShareList(memberName);
    assertEquals(workspaceShare.getMemberPermissions(memberName), MEMBER_PERMISSIONS);

    // TODO start workspace
  }

  @Test(priority = 1)
  public void checkWorkspaceSharingByMember() {
    dashboard.logout();

    // login as developer and check that the shared workspace exists in workspaces list
    dashboard.open(testUser.getName(), testUser.getPassword());
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME);
    workspaces.selectWorkspaceItemName(WORKSPACE_NAME);

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

    // TODO start and check the workspace
    workspaceDetails.clickOpenInIdeWsBtn();
    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();
    notificationsPopupPanel.waitPopupPanelsAreClosed();

    projectExplorer.openItemByPath(WEB_JAVA_SPRING_PROJECT);
    projectExplorer.openItemByPath(WEB_JAVA_SPRING_PROJECT + "/target");
    projectExplorer.waitItem(WEB_JAVA_SPRING_PROJECT + "/target/web-java-spring-1.0-SNAPSHOT");

    projectExplorer.waitAndSelectItem(WEB_JAVA_SPRING_PROJECT);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.createNotJavaFileByName("readme.txt");
    editor.waitTabIsPresent("readme.txt");

    // try to delete the workspace
    dashboard.open();
    navigationBar.waitNavigationBar();
    workspaceDetails.selectTabInWorkspaceMenu(OVERVIEW);
    workspaceOverview.clickOnDeleteWorkspace();
    workspaceDetails.clickOnDeleteButtonInDialogWindow();
    dashboard.waitNotificationMessage(
        "The user does not have permission to delete workspace with id ");
    dashboard.waitNotificationIsClosed();

    // stop workspace
    //    dashboard.open();
    //    dashboard.selectWorkspacesItemOnDashboard();
    //    workspaces.waitWorkspaceIsPresent(WORKSPACE_NAME);
    //    workspaces.clickOnWorkspaceStopStartButton(WORKSPACE_NAME);
    //    workspaces.waitWorkspaceStatus(WORKSPACE_NAME, Status.STOPPED);
  }

  private void createWorkspace(String organizationName, String workspaceName) {

    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithProject(
            JAVA, workspaceName, WEB_JAVA_SPRING_PROJECT);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(WEB_JAVA_SPRING_PROJECT);

    consoles.waitJDTLSProjectResolveFinishedMessage(WEB_JAVA_SPRING_PROJECT);
    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING_PROJECT, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);
  }
}
