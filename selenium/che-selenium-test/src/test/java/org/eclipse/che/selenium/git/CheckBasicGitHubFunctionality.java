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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.BRANCHES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.GO_BACK;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.GO_INTO;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Paths;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
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
public class CheckBasicGitHubFunctionality {
  private static final Logger LOG = LoggerFactory.getLogger(CheckBasicGitHubFunctionality.class);
  private static final String GITHUB_COM = "github.com";
  private static final String FIRST_BRANCH = "firstBranch";
  private static final String SECOND_BRANCH = "secondBranch";
  private static final String THIRD_BRANCH = "thirdBranch";
  private static final String FIRST_SUBMODULE_NAME = "Repo_For_Test";
  private static final String SECOND_SUBMODULE_NAME = "testRepo";
  public static final String DIRECTORY_NAME_1 = "my-lib";
  public static final String DIRECTORY_NAME_2 = "my-lib/src/test";
  private String currentProjectName;

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
  @Inject private TestGitHubRepository keepDirectoryRepo;
  @Inject private TestGitHubRepository importBranchRepo;
  @Inject private TestGitHubRepository submoduleRepo;
  @Inject private Git git;
  @Inject private Wizard projectWizard;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

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

  @Test
  public void shouldLoginToGitHubAndImportProject() throws Exception {
    currentProjectName = "AngularJS";

    initRepoForFirstTest();
    initRepoForSecondTest();
    initRepoForThirdTest();

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

    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    importProject.selectGitHubSourceItem();
    importProject.clickLoadRepoBtn();
    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));
    importProject.selectProjectByName(currentProjectName);

    importProject.clickImportBtn();

    // check GitHub identity is present in Keycloak account management page
    if (isMultiuser) {
      keycloakFederatedIdentitiesPage.open();
      assertEquals(keycloakFederatedIdentitiesPage.getGitHubIdentityFieldValue(), gitHubUsername);
    }

    projectWizard.clickSaveButton();
    projectExplorer.waitItem(currentProjectName);
  }

  @Test(priority = 1)
  public void keepDirectoryImportBySshUrlTest() throws Exception {
    currentProjectName = keepDirectoryRepo.getName() + "Ssh";
    projectExplorer.waitProjectExplorer();

    makeKeepDirectoryFromGitUrl(
        keepDirectoryRepo.getSshUrl(), currentProjectName, DIRECTORY_NAME_1);
    projectExplorer.waitItem(currentProjectName);

    projectExplorer.waitAndSelectItemByName(currentProjectName);
    projectExplorer.openItemByPath(currentProjectName);
    loader.waitOnClosed();
    projectExplorer.waitItemInvisibility(currentProjectName + "/my-webapp");
    projectExplorer.waitItem(currentProjectName + "/my-lib");
    expandDirectoryMyLib(currentProjectName);
  }

  @Test(priority = 2)
  public void keepDirectoryImportByHttpsUrlTest() throws Exception {
    currentProjectName = keepDirectoryRepo.getName() + "Https";
    projectExplorer.waitProjectExplorer();

    makeKeepDirectoryFromGitUrl(
        keepDirectoryRepo.getHtmlUrl(), currentProjectName, DIRECTORY_NAME_2);
    projectExplorer.waitItem(currentProjectName);

    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        currentProjectName + "/my-lib/src/test/java/hello", "SayHelloTest.java");

    editor.waitActive();

    projectExplorer.waitItemInvisibility(currentProjectName + "/my-lib/src/main");
    projectExplorer.waitItemInvisibility(currentProjectName + "/my-webapp");
    projectExplorer.openContextMenuByPathSelectedItem(currentProjectName + "/my-lib/src/test");
    projectExplorer.clickOnItemInContextMenu(GO_INTO);
    projectExplorer.waitDisappearItemByPath(currentProjectName + "/src/my-lib");
    projectExplorer.waitVisibilityByName("test");
    projectExplorer.waitVisibilityByName("java");
    projectExplorer.waitVisibilityByName("hello");
    projectExplorer.waitVisibilityByName("SayHelloTest.java");
    projectExplorer.openContextMenuByPathSelectedItem(currentProjectName + "/my-lib/src/test");
    projectExplorer.clickOnItemInContextMenu(GO_BACK);
    projectExplorer.waitItem(currentProjectName + "/my-lib/src");
  }

  @Test(priority = 3)
  public void keepDirectoryImportFromGitHub() throws Exception {
    currentProjectName = keepDirectoryRepo.getName();
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

    importProject.selectProjectByName(currentProjectName);
    importProject.typeProjectName(currentProjectName);
    importProject.waitKeepDirectoryIsNotSelected();
    importProject.clickOnKeepDirectoryCheckbox();
    importProject.waitKeepDirectoryIsSelected();
    importProject.typeDirectoryName("my-webapp");
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(currentProjectName);
    projectExplorer.waitAndSelectItemByName(currentProjectName);
    projectExplorer.openItemByPath(currentProjectName);
    loader.waitOnClosed();
    projectExplorer.waitItemInvisibility(currentProjectName + "/my-lib");

    projectExplorer.expandPathInProjectExplorer(currentProjectName + "/my-webapp/src/main/webapp");

    projectExplorer.openContextMenuByPathSelectedItem(currentProjectName + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_INTO);
    loader.waitOnClosed();
    projectExplorer.waitVisibilityByName("my-webapp");
    projectExplorer.waitItemInvisibility(currentProjectName);
    projectExplorer.openContextMenuByPathSelectedItem(currentProjectName + "/my-webapp");
    projectExplorer.clickOnItemInContextMenu(GO_BACK);
    projectExplorer.waitItem(currentProjectName);
  }

  @Test(priority = 4)
  public void checkImportProjectInBranchBySshUrl() throws IOException, JsonParseException {
    currentProjectName = importBranchRepo.getName() + "Ssh";
    projectExplorer.waitProjectExplorer();

    performImportIntoBranch(importBranchRepo.getSshUrl(), currentProjectName, FIRST_BRANCH);
    projectExplorer.waitItem(currentProjectName);
    projectExplorer.waitAndSelectItemByName(currentProjectName);
    loader.waitOnClosed();
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(FIRST_BRANCH);
    git.closeBranchesForm();
    projectExplorer.waitItem(currentProjectName);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        currentProjectName + "/src/main/java/org.eclipse.qa.examples", "AppController.java");
    editor.waitActive();
  }

  @Test(priority = 5)
  public void checkImportProjectInBranchByHttpsUrl() throws IOException, JsonParseException {
    currentProjectName = importBranchRepo.getName() + "Https";
    projectExplorer.waitProjectExplorer();

    performImportIntoBranch(importBranchRepo.getHtmlUrl(), currentProjectName, SECOND_BRANCH);
    projectExplorer.waitItem(currentProjectName);
    projectExplorer.waitAndSelectItemByName(currentProjectName);
    loader.waitOnClosed();
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheListWithCoState(SECOND_BRANCH);
    git.closeBranchesForm();

    projectExplorer.waitItem(currentProjectName);
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        currentProjectName + "/src/main/java/org.eclipse.qa.examples", "AppController.java");
    editor.waitActive();
  }

  @Test(priority = 6)
  public void checkImportProjectInBranchFromGitHub() throws Exception {
    currentProjectName = importBranchRepo.getName();
    projectExplorer.waitProjectExplorer();

    importIntoBranchFromGitHub();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(currentProjectName);
    projectExplorer.waitAndSelectItemByName(currentProjectName);
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(THIRD_BRANCH);
    git.closeBranchesForm();
    projectExplorer.openItemByPath(currentProjectName);
    loader.waitOnClosed();
    projectExplorer.openItemByPath(currentProjectName + "/AppController.java");
    editor.waitActive();
  }

  @Test(priority = 7)
  public void checkImportProjectSubmoduleByHttpsUrl() throws Exception {
    currentProjectName = submoduleRepo.getName() + "Https";
    projectExplorer.waitProjectExplorer();

    importRecursivelyFromGitUrl(submoduleRepo.getHtmlUrl(), currentProjectName);
    openSubmoduleOne(currentProjectName);
    openSubmoduleTwo(currentProjectName);
  }

  @Test(priority = 8)
  public void checkImportProjectSubmoduleBySshUrl() throws Exception {
    currentProjectName = submoduleRepo.getName() + "Ssh";
    projectExplorer.waitProjectExplorer();

    importRecursivelyFromGitUrl(submoduleRepo.getSshUrl(), currentProjectName);
    openSubmoduleOne(currentProjectName);
    openSubmoduleTwo(currentProjectName);
  }

  @Test(priority = 9)
  public void checkImportProjectSubmoduleFromGithub() throws Exception {
    currentProjectName = submoduleRepo.getName();
    projectExplorer.waitProjectExplorer();

    importRecursivelyFromGitHub(currentProjectName);
    openSubmoduleOne(currentProjectName);
    openSubmoduleTwo(currentProjectName);
  }

  private void importRecursivelyFromGitUrl(String url, String projectName) throws Exception {
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitSourceItem();
    loader.waitOnClosed();
    importProject.typeURi(url);
    importProject.typeProjectName(projectName);
    importProject.waitImportRecursivelyIsNotSelected();
    importProject.clickOnImportRecursivelyCheckbox();
    importProject.waitImportRecursivelyIsSelected();
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
  }

  private void makeKeepDirectoryFromGitUrl(String url, String projectName, String folderName)
      throws Exception {
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitSourceItem();
    loader.waitOnClosed();
    importProject.typeURi(url);
    importProject.typeProjectName(projectName);
    importProject.waitKeepDirectoryIsNotSelected();
    importProject.clickOnKeepDirectoryCheckbox();
    importProject.waitKeepDirectoryIsSelected();
    importProject.typeDirectoryName(folderName);
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
  }

  private void performImportIntoBranch(String url, String projectName, String branchName)
      throws IOException, JsonParseException {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitMainForm();
    loader.waitOnClosed();
    importProject.selectGitSourceItem();
    loader.waitOnClosed();
    importProject.typeURi(url);
    importProject.typeProjectName(projectName);
    importProject.waitBranchIsNotSelected();
    importProject.clickBranchCheckbox();
    importProject.waitBranchIsSelected();
    importProject.typeBranchName(branchName);
    importProject.clickImportBtn();
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
  }

  private void expandDirectoryMyLib(String projectName) throws Exception {
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/my-lib/src/main/java/hello", "SayHello.java");
    editor.waitActive();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/my-lib/src/test/java/hello", "SayHelloTest.java");
    editor.waitActive();
  }

  private void initRepoForFirstTest() throws IOException {
    keepDirectoryRepo.addContent(
        Paths.get(getClass().getResource("/projects/java-multimodule").getPath()));
  }

  private void initRepoForSecondTest() throws IOException {
    importBranchRepo.addContent(
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath()));

    importBranchRepo.createBranchFromMaster(FIRST_BRANCH);
    importBranchRepo.createBranchFromMaster(SECOND_BRANCH);
    importBranchRepo.createBranchFromMaster(THIRD_BRANCH);

    importBranchRepo.addContent(
        Paths.get(getClass().getResource("/projects/Repo_For_Test_branch1").getPath()),
        THIRD_BRANCH);
  }

  private void initRepoForThirdTest() throws Exception {
    submoduleRepo.addContent(
        Paths.get(getClass().getResource("/projects/java-multimodule").getPath()));
    submoduleRepo.addSubmodule(
        Paths.get(getClass().getResource("/projects/Repo_For_Test").getPath()),
        FIRST_SUBMODULE_NAME);
    submoduleRepo.addSubmodule(
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
    importProject.selectProjectByName(importBranchRepo.getName());
    importProject.typeProjectName(importBranchRepo.getName());
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
