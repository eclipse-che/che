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

import static org.testng.Assert.assertEquals;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.GitHub;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakFederatedIdentitiesPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class AuthorizeOnGithubFromPreferencesTest {
  private static final String GITHUB_COM = "github.com";
  private static final Logger LOG =
      LoggerFactory.getLogger(AuthorizeOnGithubFromPreferencesTest.class);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject
  @Named("che.multiuser")
  private boolean isMultiuser;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private CheTerminal terminal;
  @Inject private Menu menu;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private Preferences preferences;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private AskDialog askDialog;
  @Inject private GitHub gitHub;
  @Inject private TestGitHubServiceClient gitHubClientService;
  @Inject private Loader loader;
  @Inject private Events events;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private Dashboard dashboard;
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

  @BeforeClass
  private void deletePrivateSshKey() throws Exception {
    ide.open(ws);
    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab();

    // open Preferences Vcs Form
    openPreferencesVcsForm();
    if (preferences.isSshKeyIsPresent(GITHUB_COM)) {
      preferences.deleteSshKeyByHost(GITHUB_COM);
    }

    preferences.closeForm();
  }

  @Test
  public void checkAuthorizationOnGithubWhenUploadSshKey() throws Exception {
    String ideWin = seleniumWebDriver.getWindowHandle();

    // generate and upload github ssh key
    ide.open(ws);
    openPreferencesVcsForm();
    preferences.clickOnGenerateAndUploadToGitHub();

    // login to github
    askDialog.waitFormToOpen(25);
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    seleniumWebDriverHelper.switchToNextWindow(ideWin);

    gitHub.waitAuthorizationPageOpened();
    gitHub.typeLogin(gitHubUsername);
    gitHub.typePass(gitHubPassword);
    gitHub.clickOnSignInButton();

    // authorize on github.com
    gitHub.waitAuthorizeBtn();
    gitHub.clickOnAuthorizeBtn();
    seleniumWebDriver.switchTo().window(ideWin);
    loader.waitOnClosed();
    preferences.waitSshKeyIsPresent(GITHUB_COM);

    // check that repeat of upload of ssh-key doesn't require authorization
    preferences.deleteSshKeyByHost(GITHUB_COM);
    preferences.clickOnGenerateAndUploadToGitHub();
    loader.waitOnClosed();
    preferences.waitSshKeyIsPresent(GITHUB_COM);

    // check GitHub identity is present in Keycloak account management page
    if (isMultiuser) {
      keycloakFederatedIdentitiesPage.open();
      assertEquals(keycloakFederatedIdentitiesPage.getGitHubIdentityFieldValue(), gitHubUsername);
    }
  }

  private void openPreferencesVcsForm() {
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.waitMenuInCollapsedDropdown(Preferences.DropDownSshKeysMenu.VCS);
    preferences.selectDroppedMenuByName(Preferences.DropDownSshKeysMenu.VCS);
  }
}
