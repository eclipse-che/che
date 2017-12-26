/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FolderTypes.PROJECT_FOLDER;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Sources.GITHUB;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.GitHub;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ImportMavenProjectFromGitHubTest {
  private final String WORKSPACE = NameGenerator.generate("ImtMvnPrjGitHub", 4);
  private static final String GITHUB_PROJECT_NAME = "AngularJS";

  private String projectName;
  private String ideWin;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private Loader loader;
  @Inject private GitHub gitHub;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private TestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
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
  public void checkAbilityImportMavenProjectTest() throws Exception {
    ideWin = seleniumWebDriver.getWindowHandle();

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    createWorkspace.selectStack(JAVA.getId());
    createWorkspace.typeWorkspaceName(WORKSPACE);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSourceTab(GITHUB);
    projectSourcePage.clickOnConnectGithubAccountButton();
    clickOnConnectYourGithubAccountButton();
    projectSourcePage.waitGithubProjectsList();
    projectSourcePage.selectProjectFromList(GITHUB_PROJECT_NAME);
    projectSourcePage.clickOnAddProjectButton();
    projectSourcePage.clickOnAddOrImportProjectButton();
    createWorkspace.clickOnCreateWorkspaceButton();

    seleniumWebDriver.switchFromDashboardIframeToIde();

    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(projectName, PROJECT_FOLDER);
  }

  private void clickOnConnectYourGithubAccountButton() {
    seleniumWebDriver.switchToNoneCurrentWindow(ideWin);

    gitHub.waitAuthorizationPageOpened();
    gitHub.typeLogin(gitHubUsername);
    gitHub.typePass(gitHubPassword);
    gitHub.clickOnSignInButton();
    gitHub.waitAuthorizeBtn();
    gitHub.clickOnAuthorizeBtn();
    loader.waitOnClosed();

    // authorize on github.com
    if (seleniumWebDriver.getWindowHandles().size() > 1) {
      loader.waitOnClosed();
      gitHub.waitAuthorizeBtn();
      gitHub.clickOnAuthorizeBtn();
      seleniumWebDriver.switchTo().window(ideWin);
    }

    seleniumWebDriver.switchTo().window(ideWin);
  }
}
