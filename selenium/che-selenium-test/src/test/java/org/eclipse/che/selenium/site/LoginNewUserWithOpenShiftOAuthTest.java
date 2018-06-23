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

import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;

import com.google.inject.Inject;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestUserServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.user.TestUser;
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
public class LoginNewUserWithOpenShiftOAuthTest {

  private static final String WORKSPACE_NAME = NameGenerator.generate("workspace", 4);
  private static final String WORKSPACE_ID_PREFIX = "workspace";

  private static final TestUser NEW_TEST_USER = getTestUser();

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
  private void removeTestUser() throws ServerException, ConflictException, BadRequestException {
    try {
      User newTestUserDto = testUserServiceClient.findByName(NEW_TEST_USER.getName());
      testUserServiceClient.remove(newTestUserDto.getId());
    } catch (NotFoundException e) {
      // ignore if test user don't exist
    }
  }

  @Test
  public void checkNewCheUserOcpProjectCreationAndRemoval() {
    // go to login page
    // we can't use dashboatd.open() here to login with OAuth
    seleniumWebDriver.navigate().to(testDashboardUrlProvider.get());

    // click on button to login with OpenShift OAuth
    cheLoginPage.loginWithOpenShiftOAuth();

    // login to OCP login page with default test user credentials
    openShiftLoginPage.login(NEW_TEST_USER.getName(), NEW_TEST_USER.getPassword());

    // authorize ocp-client to access OpenShift account
    authorizeOpenShiftAccessPage.allowPermissions();

    // fill first broker login page
    firstBrokerProfilePage.submit(NEW_TEST_USER);

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
    openShiftLoginPage.login(NEW_TEST_USER.getName(), NEW_TEST_USER.getPassword());
    openShiftProjectCatalogPage.waitProject(WORKSPACE_ID_PREFIX);

    // remove test workspace from Che Dashboard
    seleniumWebDriver.navigate().to(testDashboardUrlProvider.get());
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectAllWorkspacesByBulk();
    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();
    workspaces.waitWorkspaceIsNotPresent(WORKSPACE_NAME);

    // go to OCP and check there is no project with name starts from "workspace"
    openShiftProjectCatalogPage.open();
    openShiftProjectCatalogPage.waitProjectAbsence(WORKSPACE_ID_PREFIX);
  }

  private static TestUser getTestUser() {
    return new TestUser() {
      private final long currentTimeInMillisec = System.currentTimeMillis();
      private final String name = "user" + currentTimeInMillisec;
      private final String email = name + "@1.com";
      private final String password = String.valueOf(currentTimeInMillisec);

      @Override
      public String getEmail() {
        return email;
      }

      @Override
      public String getPassword() {
        return password;
      }

      @Override
      public String obtainAuthToken() {
        return null;
      }

      @Override
      public String getOfflineToken() {
        return null;
      }

      @Override
      public String getName() {
        return name;
      }

      @Override
      public String getId() {
        return null;
      }

      @Override
      public void delete() {}
    };
  }
}
