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
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
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
  private static final String GITHUB_COM = "github.com";
  private static final String FIRST_BRANCH = "firstBranch";
  private static final String SECOND_BRANCH = "secondBranch";
  private static final String THIRD_BRANCH = "thirdBranch";
  private static final String FIRST_SUBMODULE_NAME = "Repo_For_Test";
  private static final String SECOND_SUBMODULE_NAME = "testRepo";

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
  @Inject private Git git;
  @Inject private Wizard projectWizard;
  @Inject private CodenvyEditor editor;
  @Inject private TestUser testUser;

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
  private void initGitRepositories() throws Exception {
    initRepoForFirstTest();
    initRepoForSecondTest();
    initRepoForThirdTest();
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

    loginToGitHub();

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
    importProject.typeProjectName(testRepo1.getName());
    importProject.waitKeepDirectoryIsNotSelected();
    importProject.clickOnKeepDirectoryCheckbox();
    importProject.waitKeepDirectoryIsSelected();
    importProject.typeDirectoryName("my-webapp");
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(testRepo1.getName());
    projectExplorer.waitAndSelectItemByName(testRepo1.getName());
    projectExplorer.openItemByPath(testRepo1.getName());
    loader.waitOnClosed();
    projectExplorer.waitItemInvisibility(testRepo1.getName() + "/my-lib");

    projectExplorer.expandPathInProjectExplorer(testRepo1.getName() + "/my-webapp/src/main/webapp");

    projectExplorer.openContextMenuByPathSelectedItem(testRepo1.getName() + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_INTO);
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName("my-webapp");
    projectExplorer.waitItemInvisibility(testRepo1.getName());
    projectExplorer.openContextMenuByPathSelectedItem(testRepo1.getName() + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_BACK);
    projectExplorer.waitItem(testRepo1.getName());
  }

  @Test(priority = 2)
  public void checkImportProjectInBranchFromGitHub() throws Exception {
    projectExplorer.waitProjectExplorer();
    importIntoBranchFromGitHub();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(testRepo2.getName());
    projectExplorer.waitAndSelectItemByName(testRepo2.getName());
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(THIRD_BRANCH);
    git.closeBranchesForm();
    projectExplorer.openItemByPath(testRepo2.getName());
    loader.waitOnClosed();
    projectExplorer.openItemByPath(testRepo2.getName() + "/AppController.java");
    editor.waitActive();
  }

  @Test(priority = 3)
  public void checkImportProjectSubmoduleFromGithub() throws Exception {
    projectExplorer.waitProjectExplorer();
    importRecursivelyFromGitHub(testRepo3.getName());
    openSubmoduleOne(testRepo3.getName());
    openSubmoduleTwo(testRepo3.getName());
  }

  private void initRepoForFirstTest() throws IOException {
    testRepo1.addContent(Paths.get(getClass().getResource("/projects/java-multimodule").getPath()));
  }

  private void initRepoForSecondTest() throws IOException {
    testRepo2.addContent(
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath()));

    testRepo2.createBranchFromMaster(FIRST_BRANCH);
    testRepo2.createBranchFromMaster(SECOND_BRANCH);
    testRepo2.createBranchFromMaster(THIRD_BRANCH);

    testRepo2.addContent(
        Paths.get(getClass().getResource("/projects/Repo_For_Test_branch1").getPath()),
        THIRD_BRANCH);
  }

  private void initRepoForThirdTest() throws Exception {
    testRepo3.addContent(Paths.get(getClass().getResource("/projects/java-multimodule").getPath()));
    testRepo3.addSubmodule(
        Paths.get(getClass().getResource("/projects/Repo_For_Test").getPath()),
        FIRST_SUBMODULE_NAME);
    testRepo3.addSubmodule(
        Paths.get(getClass().getResource("/projects/testRepo").getPath()), SECOND_SUBMODULE_NAME);
  }

  private void loginToGitHub() {
    try {
      askDialog.waitFormToOpen(25);
    } catch (TimeoutException ex) {
      importProject.closeWithIcon();
      events.clickEventLogBtn();
      events.waitExpectedMessage("Failed to authorize application on GitHub");
      fail("Known issue https://github.com/eclipse/che/issues/8288", ex);
    }
  }

  private void importRecursivelyFromGitHub(String projectName) throws Exception {
    loader.waitOnClosed();
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
    importProject.selectProjectByName(projectName);
    importProject.typeProjectName(projectName);
    importProject.waitImportRecursivelyIsNotSelected();
    importProject.clickOnImportRecursivelyCheckbox();
    importProject.waitImportRecursivelyIsSelected();
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
  }

  private void openSubmoduleOne(String projectName) throws Exception {
    projectExplorer.openItemByPath(projectName);
    projectExplorer.waitAndSelectItem(projectName + "/" + FIRST_SUBMODULE_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.CONVERT_TO_PROJECT);
    projectWizard.waitOpenProjectConfigForm();
    projectWizard.waitTextParentDirectoryName("/" + projectName);
    projectWizard.waitTextProjectNameInput(FIRST_SUBMODULE_NAME);
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    projectWizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitItem(projectName);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/" + FIRST_SUBMODULE_NAME + "/src/main/java/com.codenvy.example.spring",
        "GreetingController.java");
    projectExplorer.waitVisibilityByName("HelloWorld.java");
    editor.closeFileByNameWithSaving("GreetingController");
  }

  private void openSubmoduleTwo(String projectName) throws Exception {
    projectExplorer.waitAndSelectItem(projectName + "/" + SECOND_SUBMODULE_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.CONVERT_TO_PROJECT);
    projectWizard.waitOpenProjectConfigForm();
    projectWizard.waitTextParentDirectoryName("/" + projectName);
    projectWizard.waitTextProjectNameInput(SECOND_SUBMODULE_NAME);
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    projectWizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitItem(projectName);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/" + SECOND_SUBMODULE_NAME + "/src/main/java/com.company.example", "A.java");
    projectExplorer.openItemByPath(
        projectName + "/" + SECOND_SUBMODULE_NAME + "/src/main/java/commenttest");
    projectExplorer.waitVisibilityByName("GitPullTest.java");
    projectExplorer.waitVisibilityByName("JavaCommentsTest.java");
    editor.closeFileByNameWithSaving("A");
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
    importProject.typeProjectName(testRepo2.getName());
    importProject.waitBranchIsNotSelected();
    importProject.clickBranchCheckbox();
    importProject.waitBranchIsSelected();
    importProject.typeBranchName(THIRD_BRANCH);
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
