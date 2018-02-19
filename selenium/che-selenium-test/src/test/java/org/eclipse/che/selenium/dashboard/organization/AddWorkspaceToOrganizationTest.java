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
  private static final String WS_NAME3 = generate("workspace", 4);
  private static final String WS_SUB_NAME1 = generate("workspace", 4);
  private static final String WS_SUB_NAME2 = generate("workspace", 4);

  private String suborgNamePath;
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
    organization1.addAdmin(adminTestUser.getId());
    organization2.addAdmin(adminTestUser.getId());
    organization2.addMember(testUser.getId());
    suborg1.addAdmin(adminTestUser.getId());
    suborg2.addMember(testUser.getId());

    suborgNamePath = organization1.getName() + "/" + suborg1.getName();
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

    // create workspace for suborganization
    createWorkspace(suborgNamePath, WS_SUB_NAME1);

    // check that workspaces names exist in the Workspaces list and the Recent list
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(organization1.getName(), WS_NAME1);
    workspaces.waitWorkspaceIsPresent(organization2.getName(), WS_NAME2);
    workspaces.waitWorkspaceIsPresent(suborgNamePath, WS_SUB_NAME1);
    assertTrue(dashboard.ifWorkspaceNamePresentInRecentList(WS_NAME1));
    assertTrue(dashboard.ifWorkspaceNamePresentInRecentList(WS_NAME2));
    assertTrue(dashboard.ifWorkspaceNamePresentInRecentList(WS_SUB_NAME1));

    // check that the Namespace link in workspace details correct
    checkNamespaceLink(WS_NAME1, organization1.getName());
    checkNamespaceLink(WS_NAME2, organization2.getName());
    try {
      checkNamespaceLink(WS_SUB_NAME1, suborgNamePath);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7925");
    }
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

    // create a workspace for organization2 and its suborganization
    createWorkspace(organization2.getName(), WS_NAME3);
    createWorkspace(suborgNameMember, WS_SUB_NAME2);

    // check that workspace name exists in the Workspaces list and the Recent list
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.waitWorkspaceIsPresent(organization2.getName(), WS_NAME3);
    workspaces.waitWorkspaceIsPresent(suborgNameMember, WS_SUB_NAME2);
    assertTrue(dashboard.ifWorkspaceNamePresentInRecentList(WS_NAME3));
    assertTrue(dashboard.ifWorkspaceNamePresentInRecentList(WS_SUB_NAME2));

    // check that workspaces names of other users are not exist in the Recent list
    assertFalse(dashboard.ifWorkspaceNamePresentInRecentList(WS_NAME1));
    assertFalse(dashboard.ifWorkspaceNamePresentInRecentList(WS_SUB_NAME1));

    // check that the Namespace link in workspace details correct
    checkNamespaceLink(WS_NAME3, organization2.getName());

    try {
      checkNamespaceLink(WS_SUB_NAME2, suborgNameMember);
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

    // TODO uncomment after https://github.com/eclipse/che/issues/8497 issue is fixed
    // workspaceDetails.waitToolbarTitleName(workspaceName);
    WaitUtils.sleepQuietly(3);
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
