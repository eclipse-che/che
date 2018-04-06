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

import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.GO_BACK;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.GO_INTO;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.GitHub;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakFederatedIdentitiesPage;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
@Test(groups = TestGroup.GITHUB)
public class AuthorizeOnGithubFromImportTest {
  private static final Logger LOG = LoggerFactory.getLogger(AuthorizeOnGithubFromImportTest.class);
  private static final String PROJECT_NAME1 = NameGenerator.generate("KeepDir-", 4);
  private static final String PROJECT_NAME2 = NameGenerator.generate("ImportIntoBranch-", 4);
  private static final String PROJECT_NAME3 = NameGenerator.generate("RecursiveSubmodule-", 4);
  private static final String GITHUB_COM = "github.com";
  private static final String BRANCH_1 = "xxx";
  private static final String BRANCH_2 = "zzz";
  private static final String BRANCH_3 = "second_branch";

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
  @Inject private MachineTerminal terminal;
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
  @Inject private TestGitHubRepository testRepo1;
  @Inject private TestGitHubRepository testRepo2;
  @Inject private TestGitHubRepository testRepo3;
  @Inject private TestGitHubRepository submodule1;
  @Inject private TestGitHubRepository submodule2;
  @Inject private Git git;
  @Inject private Wizard projectWizard;
  @Inject private CodenvyEditor editor;

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

    openPreferencesVcsForm();

    if (preferences.isSshKeyIsPresent(GITHUB_COM)) {
      preferences.deleteSshKeyByHost(GITHUB_COM);
    }

    preferences.clickOnCloseBtn();
  }

  @BeforeClass
  private void initGitRepositories() throws IOException, URISyntaxException {
    Path entryPath1 = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    /*testRepo1.addContent(entryPath1);

    Path sourceProject =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());
    Path sourceBranchProject =
        Paths.get(getClass().getResource("/projects/Repo_For_Test_branch1").toURI());

    testRepo2.addContent(sourceProject);

    testRepo2.createBranchFromMaster(BRANCH_1);
    testRepo2.createBranchFromMaster(BRANCH_2);
    testRepo2.createBranchFromMaster(BRANCH_3);

    testRepo2.addContent(sourceBranchProject, BRANCH_3);*/
    String pathToFile = "/projects/GitSubmoduleForImportRecursiveTest/submodule-file-content.md";

    testRepo3.addContent(entryPath1);
    testRepo3.createSubmodule(submodule1, "/projects/GitSubmoduleForImportRecursiveTest/submodule-file-content.md");

  }

  @Test
  public void checkAuthorizationOnGitHubWhenImportProject() throws Exception {

    ide.open(ws);
    String ideWin = seleniumWebDriver.getWindowHandle();

    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    importProject.selectGitHubSourceItem();
    importProject.clickLoadRepoBtn();

    // login to github
    try {
      askDialog.waitFormToOpen(25);
    } catch (TimeoutException ex) {
      importProject.closeWithIcon();
      events.clickEventLogBtn();
      events.waitExpectedMessage("Failed to authorize application on GitHub");
      fail("Known issue https://github.com/eclipse/che/issues/8288", ex);
    }

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

    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));
    importProject.closeWithIcon();

    // check load repo if an application is authorized
    openPreferencesVcsForm();
    preferences.waitSshKeyIsPresent(GITHUB_COM);
    preferences.deleteSshKeyByHost(GITHUB_COM);
    preferences.clickOnCloseBtn();

    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    importProject.selectGitHubSourceItem();
    importProject.clickLoadRepoBtn();
    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));
    importProject.selectProjectByName("AngularJS");

    importProject.clickImportBtn();

    // check GitHub identity is present in Keycloak account management page
    if (isMultiuser) {
      keycloakFederatedIdentitiesPage.open();
      assertEquals(keycloakFederatedIdentitiesPage.getGitHubIdentityFieldValue(), gitHubUsername);
    }

    projectWizard.clickSaveButton();
    projectExplorer.waitItem("AngularJS");
  }

  @Test(priority = 1)
  public void keepDirectoryImportFromGitHub() throws Exception {
    Path entryPath1 = Paths.get(getClass().getResource("/projects/java-multimodule").getPath());
    testRepo1.addContent(entryPath1);

    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitHubSourceItem();
    loader.waitOnClosed();
    importProject.waitLoadRepoBtn();
    importProject.clickLoadRepoBtn();
    loader.waitOnClosed();
    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));

    importProject.selectProjectByName(testRepo1.getName());
    importProject.typeProjectName(PROJECT_NAME1);
    importProject.waitKeepDirectoryIsNotSelected();
    importProject.clickOnKeepDirectoryCheckbox();
    importProject.waitKeepDirectoryIsSelected();
    importProject.typeDirectoryName("my-webapp");
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME1);
    projectExplorer.waitAndSelectItemByName(PROJECT_NAME1);
    projectExplorer.openItemByPath(PROJECT_NAME1);
    loader.waitOnClosed();
    projectExplorer.waitItemInvisibility(PROJECT_NAME1 + "/my-lib");

    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME1 + "/my-webapp/src/main/webapp");

    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME1 + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_INTO);
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName("my-webapp");
    projectExplorer.waitItemInvisibility(PROJECT_NAME1);
    projectExplorer.openContextMenuByPathSelectedItem(PROJECT_NAME1 + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_BACK);
    projectExplorer.waitItem(PROJECT_NAME1);
  }

  @Test(priority = 2)
  public void checkImportProjectInBranchFromGitHub() throws Exception {
    Path sourceProject =
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI());
    Path sourceBranchProject =
        Paths.get(getClass().getResource("/projects/Repo_For_Test_branch1").toURI());

    testRepo2.addContent(sourceProject);

    testRepo2.createBranchFromMaster(BRANCH_1);
    testRepo2.createBranchFromMaster(BRANCH_2);
    testRepo2.createBranchFromMaster(BRANCH_3);

    testRepo2.addContent(sourceBranchProject, BRANCH_3);

    projectExplorer.waitProjectExplorer();
    importIntoBranchFromGitHub();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME1);
    projectExplorer.waitAndSelectItemByName(PROJECT_NAME2);
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(BRANCH_3);
    git.closeBranchesForm();
    projectExplorer.openItemByPath(PROJECT_NAME2);
    loader.waitOnClosed();
    projectExplorer.openItemByPath(PROJECT_NAME2 + "/AppController.java");
    editor.waitActive();
  }

  private void importIntoBranchFromGitHub() throws Exception {
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitHubSourceItem();
    loader.waitOnClosed();
    importProject.waitLoadRepoBtn();
    importProject.clickLoadRepoBtn();
    loader.waitOnClosed();
    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));
    importProject.selectProjectByName(testRepo2.getName());
    importProject.typeProjectName(PROJECT_NAME2);
    importProject.waitBranchIsNotSelected();
    importProject.clickBranchCheckbox();
    importProject.waitBranchIsSelected();
    importProject.typeBranchName(BRANCH_3);
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
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
