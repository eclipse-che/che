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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.GitHub;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class AuthorizeGithubFromPreferencesTest {

  private static final String GITHUB_COM = "github.com";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private MachineTerminal terminal;
  @Inject private Menu menu;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private Preferences preferences;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private AskDialog askDialog;
  @Inject private GitHub gitHub;
  @Inject private TestGitHubServiceClient gitHubClientService;
  @Inject private Loader loader;
  @Inject private Events events;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    ide.open(ws);
    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab();

    // delete github private ssh key if it exists
    deletePrivateSshKey();
  }

  @Test
  public void checkAuthorizationGithubFromPreferences() {
    String ideWin = seleniumWebDriver.getWindowHandle();
    preferences.waitPreferencesForm();
    preferences.clickOnGenerateAndUploadToGitHub();

    // login to github
    askDialog.waitFormToOpen(25);
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    seleniumWebDriver.switchToNoneCurrentWindow(ideWin);

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

    // check add ssh key if an application is authorized
    preferences.waitSshKeyIsPresent(GITHUB_COM);
    preferences.deleteSshKeyByHost(GITHUB_COM);

    preferences.clickOnGenerateAndUploadToGitHub();
    loader.waitOnClosed();
    preferences.waitSshKeyIsPresent(GITHUB_COM);
  }

  private void deletePrivateSshKey() {
    openPreferencesVcsForm();

    if (preferences.isSshKeyIsPresent(GITHUB_COM)) {
      preferences.deleteSshKeyByHost(GITHUB_COM);
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
