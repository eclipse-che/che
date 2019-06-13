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
package org.eclipse.che.selenium.site.ocpoauth;

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
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.ocp.AuthorizeOpenShiftAccessPage;
import org.eclipse.che.selenium.pageobject.ocp.OpenShiftLoginPage;
import org.eclipse.che.selenium.pageobject.ocp.OpenShiftProjectCatalogPage;
import org.eclipse.che.selenium.pageobject.site.CheLoginPage;
import org.eclipse.che.selenium.pageobject.site.FirstBrokerProfilePage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * This test checks user can be registered in Eclipse Che Multiuser deployed on OCP with OCP OAuth
 * server identity provider and work with workspace as own OCP resource.<br>
 * <br>
 * <b>Test environment</b>: <br>
 * Eclipse Che Multiuser deployed on OCP with support of OpenShift OAuth server.<br>
 * If you are going to deploy Eclipse Che on OCP with {@code deploy/openshift/ocp.sh} script, use
 * {@code --setup-ocp-oauth} parameter to setup OCP OAuth identity provider.<br>
 * <br>
 * <b>Test case</b>:<br>
 * - go to login page of Eclipse Che;<br>
 * - click on button to login with OpenShift OAuth;<br>
 * - login to OCP with new Eclipse Che test user credentials;<br>
 * - authorize ocp-client to access OpenShift account;<br>
 * - fill first broker profile page;<br>
 * - create and open workspace of java type;<br>
 * - switch to the Eclipse Che IDE and wait until workspace is ready to use;<br>
 * - go to OCP and check there if there is a project with name starts from "workspace";<br>
 * - remove test workspace from Eclipse Che Dashboard;<br>
 * - go to OCP and check there if there is no project with name starts from "workspace".<br>
 * <br>
 * <a href="https://github.com/eclipse/che/issues/8178">Feature reference.</a> <br>
 * <br>
 *
 * @author Dmytro Nochevnov
 */
@Test(groups = {TestGroup.OPENSHIFT, TestGroup.K8S, TestGroup.MULTIUSER})
public class LoginNewUserWithOpenShiftOAuthTest {

  private static final String WORKSPACE_NAME = NameGenerator.generate("workspace", 4);
  private static final String WORKSPACE_ID_PREFIX = "workspace";

  private static final TestUser NEW_TEST_USER = getTestUser();

  @Inject private CheLoginPage cheLoginPage;
  @Inject private OpenShiftLoginPage openShiftLoginPage;
  @Inject private FirstBrokerProfilePage firstBrokerProfilePage;
  @Inject private AuthorizeOpenShiftAccessPage authorizeOpenShiftAccessPage;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ToastLoader toastLoader;
  @Inject private Ide ide;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestUserServiceClient testUserServiceClient;
  @Inject private OpenShiftProjectCatalogPage openShiftProjectCatalogPage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestDashboardUrlProvider testDashboardUrlProvider;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

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
    // go to login page of Eclipse Che
    // (we can't use dashboard.open() here to login with OAuth)
    seleniumWebDriver.navigate().to(testDashboardUrlProvider.get());

    // click on button to login with OpenShift OAuth
    cheLoginPage.loginWithOpenShiftOAuth();

    // login to OCP from login page with new test user credentials
    openShiftLoginPage.login(NEW_TEST_USER.getName(), NEW_TEST_USER.getPassword());

    // authorize ocp-client to access OpenShift account
    authorizeOpenShiftAccessPage.waitOnOpen();
    authorizeOpenShiftAccessPage.allowPermissions();

    // fill first broker profile page
    firstBrokerProfilePage.submit(NEW_TEST_USER);

    // create and open workspace of java type
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.typeWorkspaceName(WORKSPACE_NAME);
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    // switch to the Eclipse Che IDE and wait until workspace is ready to use
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    toastLoader.waitToastLoaderAndClickStartButton();
    ide.waitOpenedWorkspaceIsReadyToUse();

    // go to OCP and check if there is a project with name starts from "workspace"
    openShiftProjectCatalogPage.open();
    openShiftLoginPage.login(NEW_TEST_USER.getName(), NEW_TEST_USER.getPassword());
    openShiftProjectCatalogPage.waitProject(WORKSPACE_ID_PREFIX);

    // remove test workspace from Eclipse Che Dashboard
    seleniumWebDriver.navigate().to(testDashboardUrlProvider.get());
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectAllWorkspacesByBulk();
    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();
    workspaces.waitWorkspaceIsNotPresent(WORKSPACE_NAME);

    // go to OCP and check if there is no project with name starts from "workspace"
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
