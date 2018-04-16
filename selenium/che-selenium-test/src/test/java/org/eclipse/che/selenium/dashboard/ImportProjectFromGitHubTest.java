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
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.ELEMENT_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Sources.GITHUB;
import static org.testng.AssertJUnit.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = TestGroup.GITHUB)
public class ImportProjectFromGitHubTest {
  private static final String WORKSPACE = NameGenerator.generate("ImtMvnPrjGitHub", 4);
  private static final String GITHUB_PROJECT_NAME = "AngularJS";

  private String projectName;
  private String ideWin;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private Ide ide;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    projectName = gitHubUsername + "-" + GITHUB_PROJECT_NAME;

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void checkAbilityImportProjectFromGithub() throws Exception {
    ideWin = seleniumWebDriver.getWindowHandle();

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    newWorkspace.selectStack(JAVA.getId());
    newWorkspace.typeWorkspaceName(WORKSPACE);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSourceTab(GITHUB);

    if (projectSourcePage.isConnectGitHubAccountButtonVisible()) {
      connectGithubAccount();
    }

    assertTrue(projectSourcePage.isGithubProjectsListDisplayed());
    projectSourcePage.selectProjectFromList(GITHUB_PROJECT_NAME);
    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndOpenInIDE();

    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability(ELEMENT_TIMEOUT_SEC);

    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(projectName);
    projectExplorer.waitDefinedTypeOfFolder(projectName, PROJECT_FOLDER);
  }

  private void connectGithubAccount() {
    projectSourcePage.clickOnConnectGithubAccountButton();
    seleniumWebDriverHelper.switchToNextWindow(ideWin);

    projectSourcePage.waitAuthorizationPageOpened();
    projectSourcePage.typeLogin(gitHubUsername);
    projectSourcePage.typePassword(gitHubPassword);
    projectSourcePage.clickOnSignInButton();
    seleniumWebDriver.switchTo().window(ideWin);

    if (!projectSourcePage.isGithubProjectsListDisplayed()) {
      seleniumWebDriverHelper.switchToNextWindow(ideWin);
      projectSourcePage.waitAuthorizeBtn();
      projectSourcePage.clickOnAuthorizeBtn();
      seleniumWebDriver.switchTo().window(ideWin);
    }
  }
}
