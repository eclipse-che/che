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
package org.eclipse.che.selenium.authgithub;

import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Sources.GITHUB;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class AuthorizeGithubFromDashboardTest {

  private String projectName;
  private String ideWin;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

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
    dashboard.open();
  }

  @Test
  public void checkAuthorizationGithubFromDashboard() {
    ideWin = seleniumWebDriver.getWindowHandle();

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSourceTab(GITHUB);
    projectSourcePage.clickOnConnectGithubAccountButton();

    // login to github
    seleniumWebDriver.switchToNoneCurrentWindow(ideWin);
    projectSourcePage.waitAuthorizationPageOpened();
    projectSourcePage.typeLogin(gitHubUsername);
    projectSourcePage.typePassword(gitHubPassword);
    projectSourcePage.clickOnSignInButton();

    // authorize on github.com
    projectSourcePage.waitAuthorizeBtn();
    projectSourcePage.clickOnAuthorizeBtn();
    seleniumWebDriver.switchTo().window(ideWin);

    projectSourcePage.waitGithubProjectList();

    // check github projects list if an application is authorized
    seleniumWebDriver.navigate().refresh();
    createWorkspace.waitToolbar();

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSourceTab(GITHUB);
    projectSourcePage.waitGithubProjectList();
  }
}
