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
package org.eclipse.che.selenium.site;

import static java.lang.String.format;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.ocp.AuthorizeOpenShiftAccessPage;
import org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage;
import org.eclipse.che.selenium.pageobject.ocp.OpenShiftProjectCatalogPage;
import org.eclipse.che.selenium.pageobject.site.CheLoginPage;
import org.eclipse.che.selenium.pageobject.site.FirstBrokerProfilePage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@Test(groups = {TestGroup.OPENSHIFT, TestGroup.MULTIUSER})
public class LoginExistedUserWithOpenShiftOAuthTest {

  private static final String WORKSPACE_NAME = NameGenerator.generate("workspace", 4);

  public static final String LOGIN_TO_CHE_WITH_OPENSHIFT_OAUTH_MESSAGE_TEMPLATE =
      "Authenticate as %s to link your account with openshift-v3";

  public static final String USER_ALREADY_EXISTS_ERROR_MESSAGE_TEMPLATE =
      "User with email %s already exists. How do you want to continue?";

  @Inject private CheLoginPage cheLoginPage;
  @Inject private OpenShiftLoginPage openShiftLoginPage;
  @Inject private FirstBrokerProfilePage firstBrokerProfilePage;
  @Inject private AuthorizeOpenShiftAccessPage authorizeOpenShiftAccessPage;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private TestWorkspaceServiceClient defaultUserWorkspaceServiceClient;
  @Inject private ToastLoader toastLoader;
  @Inject private Ide ide;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestUserServiceClient testUserServiceClient;
  @Inject private OpenShiftProjectCatalogPage openShiftProjectCatalogPage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestDashboardUrlProvider testDashboardUrlProvider;

  @AfterClass
  private void removeTestWorkspace() throws Exception {
    defaultUserWorkspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkExistedCheUserOcpProjectCreationAndRemoval() throws Exception {
    // go to login page
    // we can't use dashboatd.open() here to login with OAuth
    seleniumWebDriver.navigate().to(testDashboardUrlProvider.get());

    // click on button to login with OpenShift OAuth
    cheLoginPage.loginWithOpenShiftOAuth();

    // login to OCP login page with default test user credentials
    openShiftLoginPage.login(defaultTestUser.getName(), defaultTestUser.getPassword());

    // authorize ocp-client to access OpenShift account
    authorizeOpenShiftAccessPage.allowPermissions();

    // fill first broker login page
    firstBrokerProfilePage.submit(defaultTestUser);

    // add to existed Che user account
    String expectedError =
        format(USER_ALREADY_EXISTS_ERROR_MESSAGE_TEMPLATE, defaultTestUser.getEmail());

    assertEquals(firstBrokerProfilePage.getErrorAlert(), expectedError);
    firstBrokerProfilePage.addToExistingAccount();

    // login into Che again
    String expectedInfo =
        format(LOGIN_TO_CHE_WITH_OPENSHIFT_OAUTH_MESSAGE_TEMPLATE, defaultTestUser.getName());
    assertEquals(cheLoginPage.getInfoAlert(), expectedInfo);
    cheLoginPage.loginWithPredefinedUsername(defaultTestUser.getPassword());

    // create and open workspace of java type
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.clickOnAllStacksTab();
    newWorkspace.selectStack(JAVA);
    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    // switch to the IDE and wait for workspace is ready to use
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    toastLoader.waitToastLoaderAndClickStartButton();
    ide.waitOpenedWorkspaceIsReadyToUse();

    // go to OCP and check there is a project with name starts from "workspace"
    openShiftProjectCatalogPage.open();
    openShiftLoginPage.login(defaultTestUser.getName(), defaultTestUser.getPassword());
    Workspace testWorkspace =
        defaultUserWorkspaceServiceClient.getByName(WORKSPACE_NAME, defaultTestUser.getName());
    openShiftProjectCatalogPage.waitProject(testWorkspace.getId());

    // remove test workspace from Che Dashboard
    seleniumWebDriver.navigate().to(testDashboardUrlProvider.get());
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectAllWorkspacesByBulk();
    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();
    workspaces.waitWorkspaceIsNotPresent(WORKSPACE_NAME);

    // go to OCP and check there is no project with name starts from "workspace"
    openShiftProjectCatalogPage.open();
    openShiftProjectCatalogPage.waitProjectAbsence(testWorkspace.getId());
  }
}
