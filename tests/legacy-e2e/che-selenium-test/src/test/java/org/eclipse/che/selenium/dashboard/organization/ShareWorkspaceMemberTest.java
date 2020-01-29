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
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.UPDATING_PROJECT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.CONSOLE_JAVA_SIMPLE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.OVERVIEW;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.WorkspaceDetailsTab.SHARE;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces.Status.STOPPED;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import java.util.Collections;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.organization.InjectTestOrganization;
import org.eclipse.che.selenium.core.organization.TestOrganization;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.dashboard.CheMultiuserAdminDashboard;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Devfile;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceOverview;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceShare;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaEditor;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

// Known permanent failure https://github.com/eclipse/che/issues/15822
@Test(groups = {TestGroup.UNDER_REPAIR, TestGroup.MULTIUSER, TestGroup.OPENSHIFT, TestGroup.K8S})
public class ShareWorkspaceMemberTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String ADMIN_PERMISSIONS =
      "read, use, run, configure, setPermissions, delete";
  private static final String MEMBER_PERMISSIONS = "read, use, run, configure";

  private String systemAdminName;
  private String memberName;

  @InjectTestOrganization private TestOrganization org;

  @Inject private TestUser testUser;
  @Inject private Workspaces workspaces;
  @Inject private NavigationBar navigationBar;
  @Inject private AdminTestUser adminTestUser;
  @Inject private WorkspaceShare workspaceShare;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private TheiaProjectTree theiaProjectTree;
  @Inject private WorkspaceOverview workspaceOverview;
  @Inject private CheMultiuserAdminDashboard dashboard;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaEditor theiaEditor;

  @BeforeClass
  public void setUp() throws Exception {
    org.addAdmin(adminTestUser.getId());
    org.addMember(testUser.getId());
    systemAdminName = adminTestUser.getEmail();
    memberName = testUser.getEmail();

    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, adminTestUser.getName());
    org.delete();
  }

  @Test
  public void checkSharingByWorkspaceOwner() {
    createWorkspace(WORKSPACE_NAME);
    dashboard.open(adminTestUser.getName(), adminTestUser.getPassword());
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
    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitTheiaIdeTopPanel();
    theiaEditor.waitEditorTab("README.md");

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
    createWorkspaceHelper.createAndStartWorkspaceFromStack(
        Devfile.JAVA_MAVEN, workspaceName, Collections.emptyList(), null);

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitTheiaIdeTopPanel();
    theiaIde.waitAllNotificationsClosed();

    theiaProjectTree.waitFilesTab();
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitItem(CONSOLE_JAVA_SIMPLE);
    theiaIde.waitNotificationDisappearance(
        "Che Workspace: Finished importing projects.", UPDATING_PROJECT_TIMEOUT_SEC);
    theiaIde.waitAllNotificationsClosed();
    theiaProjectTree.expandItem(CONSOLE_JAVA_SIMPLE);
    theiaProjectTree.openItem(CONSOLE_JAVA_SIMPLE + "/README.md");
    theiaEditor.waitEditorTab("README.md");
  }
}
