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

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.provider.TestDashboardUrlProvider;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
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
 * This test checks if existed user of Eclipse Che Multiuser deployed on OCP can log into Eclipse
 * Che by using OCP OAuth server identity provider and work with workspace as own OCP resource.<br>
 * <br>
 * <b>Test environment</b>: <br>
 * Eclipse Che Multiuser deployed on OCP with support of OpenShift OAuth server.<br>
 * If you are going to deploy Eclipse Che on OCP with {@code deploy/openshift/ocp.sh} script, use
 * {@code --setup-ocp-oauth} parameter to setup OCP OAuth identity provider.<br>
 * <br>
 * <b>Test case</b>:<br>
 * - register new Eclipse Che user;<br>
 * - go to login page of Eclipse Che;<br>
 * - click on button to login with OpenShift OAuth;<br>
 * - login to OCP with registered earlier Eclipse Che test user credentials;<br>
 * - authorize ocp-client to access OpenShift account;<br>
 * - fill first broker profile page;<br>
 * - add OCP user to existed Che user account;<br>
 * - login into Eclipse Che again;<br>
 * - create and open workspace of java type;<br>
 * - switch to the Eclipse Che IDE and wait until workspace is ready to use;<br>
 * - go to OCP and check there if there is a project with name equals to test workspace id;<br>
 * - remove test workspace from Eclipse Che Dashboard;<br>
 * - go to OCP and check there if there is no project with name equals to test workspace id.<br>
 * <br>
 * <a href="https://github.com/eclipse/che/issues/8178">Feature reference.</a> <br>
 * <br>
 *
 * @author Dmytro Nochevnov
 */
@Test(groups = {TestGroup.OPENSHIFT, TestGroup.K8S, TestGroup.MULTIUSER})
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
  @Inject private OpenShiftProjectCatalogPage openShiftProjectCatalogPage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestDashboardUrlProvider testDashboardUrlProvider;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @AfterClass
  private void removeTestWorkspace() throws Exception {
    defaultUserWorkspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkExistedCheUserOcpProjectCreationAndRemoval() throws Exception {
    // go to login page of Eclipse Che
    // (we can't use dashboard.open() here to login with OAuth)
    seleniumWebDriver.navigate().to(testDashboardUrlProvider.get());

    // click on button to login with OpenShift OAuth
    cheLoginPage.loginWithOpenShiftOAuth();

    // login to OCP from login page with default test user credentials
    openShiftLoginPage.login(defaultTestUser.getName(), defaultTestUser.getPassword());

    // authorize ocp-client to access OpenShift account
    authorizeOpenShiftAccessPage.waitOnOpen();
    authorizeOpenShiftAccessPage.allowPermissions();

    // fill first broker profile page
    firstBrokerProfilePage.submit(defaultTestUser);

    // add OCP user to existed Eclipse Che user account
    String expectedError =
        format(USER_ALREADY_EXISTS_ERROR_MESSAGE_TEMPLATE, defaultTestUser.getEmail());

    assertEquals(firstBrokerProfilePage.getErrorAlert(), expectedError);
    firstBrokerProfilePage.addToExistingAccount();

    // login into Eclipse Che again
    String expectedInfo =
        format(LOGIN_TO_CHE_WITH_OPENSHIFT_OAUTH_MESSAGE_TEMPLATE, defaultTestUser.getName());
    assertEquals(cheLoginPage.getInfoAlert(), expectedInfo);
    cheLoginPage.loginWithPredefinedUsername(defaultTestUser.getPassword());

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

    // go to OCP and check if there is a project with name equals to test workspace id
    openShiftProjectCatalogPage.open();
    openShiftLoginPage.login(defaultTestUser.getName(), defaultTestUser.getPassword());
    Workspace testWorkspace =
        defaultUserWorkspaceServiceClient.getByName(WORKSPACE_NAME, defaultTestUser.getName());
    openShiftProjectCatalogPage.waitProject(testWorkspace.getId());

    // remove test workspace from Eclipse Che Dashboard
    seleniumWebDriver.navigate().to(testDashboardUrlProvider.get());
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.selectAllWorkspacesByBulk();
    workspaces.clickOnDeleteWorkspacesBtn();
    workspaces.clickOnDeleteButtonInDialogWindow();
    workspaces.waitWorkspaceIsNotPresent(WORKSPACE_NAME);

    // go to OCP and check if there is no project with name equals to test workspace id
    openShiftProjectCatalogPage.open();
    openShiftProjectCatalogPage.waitProjectAbsence(testWorkspace.getId());
  }
}
