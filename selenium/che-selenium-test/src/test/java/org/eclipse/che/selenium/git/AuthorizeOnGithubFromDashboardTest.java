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
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Sources.GITHUB;
import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakFederatedIdentitiesPage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class AuthorizeOnGithubFromDashboardTest {
  private static final Logger LOG =
      LoggerFactory.getLogger(AuthorizeOnGithubFromDashboardTest.class);

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject
  @Named("che.multiuser")
  private boolean isMultiuser;

  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private TestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestGitHubServiceClient gitHubClientService;
  @Inject private KeycloakFederatedIdentitiesPage keycloakFederatedIdentitiesPage;

  @BeforeClass(groups = TestGroup.MULTIUSER)
  @AfterClass(groups = TestGroup.MULTIUSER)
  private void removeGitHubIdentity() {
    dashboard.open(); // to login
    keycloakFederatedIdentitiesPage.open();
    keycloakFederatedIdentitiesPage.ensureGithubIdentityIsAbsent();
    assertEquals(keycloakFederatedIdentitiesPage.getGitHubIdentityFieldValue(), "");
  }

  @BeforeClass
  private void revokeGithubOauthToken() {
    try {
      gitHubClientService.deleteAllGrants(gitHubUsername, gitHubPassword);
    } catch (Exception e) {
      LOG.warn("There was an error of revoking the github oauth token.", e);
    }
  }

  @Test
  public void checkAuthorizationOnGithubWhenLoadProjectList() {
    dashboard.open();

    String ideWin = seleniumWebDriver.getWindowHandle();

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();

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

    // check that repeat of getting of github projects list doesn't require authorization
    seleniumWebDriver.navigate().refresh();
    newWorkspace.waitToolbar();

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSourceTab(GITHUB);
    projectSourcePage.waitGithubProjectList();

    // check GitHub identity is present in Keycloak account management page
    if (isMultiuser) {
      keycloakFederatedIdentitiesPage.open();
      assertEquals(keycloakFederatedIdentitiesPage.getGitHubIdentityFieldValue(), gitHubUsername);
    }
  }
}
