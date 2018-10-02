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
package org.eclipse.che.selenium.git;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.BRANCHES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.GO_BACK;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.GO_INTO;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;
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
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.GitHub;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.account.KeycloakFederatedIdentitiesPage;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aleksandr Shmaraev
 * @author Ihor Okhrimenko
 */
@Test(groups = TestGroup.GITHUB)
public class ImportWizardFormTest {
  private static final Logger LOG = LoggerFactory.getLogger(ImportWizardFormTest.class);
  private static final String GITHUB_COM = "github.com";
  private static final String TEST_BRANCH = "test-branch";
  private static final String ANOTHER_TEST_BRANCH = "another-test-branch";
  private static final String BRANCH_WITH_CHANGES = "branch-with-changes";
  private static final String SPRING_SUBMODULE = "Repo_For_Test";
  private static final String REGULAR_SUBMODULE = "testRepo";
  public static final String MY_LIB_DIRECTORY = "my-lib";
  public static final String TEST_DIRECTORY = "my-lib/src/test";
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
  @Inject private Dashboard dashboard;
  @Inject private KeycloakFederatedIdentitiesPage keycloakFederatedIdentitiesPage;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TestGitHubRepository keepDirectoryRepo;
  @Inject private TestGitHubRepository importBranchRepo;
  @Inject private TestGitHubRepository multimoduleRepo;
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
    terminal.waitFirstTerminalTab();

    openPreferencesVcsForm();

    if (preferences.isSshKeyIsPresent(GITHUB_COM)) {
      preferences.deleteSshKeyByHost(GITHUB_COM);
    }

    preferences.close();
  }

  @AfterMethod
  private void deleteProject() {
    try {
      testProjectServiceClient.deleteResource(ws.getId(), currentProjectName);
    } catch (Exception e) {
      LOG.warn(e.getMessage(), e);
    }
  }

  @Test
  public void shouldLoginToGitHubAndImportProject() throws Exception {
    initRepoForLoginToGithubAndImportProject();

    currentProjectName = testRepo.getName();

    // init repos for tests
    initRepoForKeepDirectoryTest();
    initRepoForImportBranchTest();
    initRepoForMultiModuleTest();

    // open github authorization window
    ide.open(ws);
    String ideWin = seleniumWebDriver.getWindowHandle();

    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importProject.waitMainForm();
    importProject.selectGitHubSourceItem();
    importProject.clickLoadRepoBtn();
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
    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));
    importProject.closeWithIcon();

    // import project
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importProject.waitMainForm();
    importProject.selectGitHubSourceItem();
    importProject.clickLoadRepoBtn();
    importProject.selectItemInAccountList(
        gitHubClientService.getName(gitHubUsername, gitHubPassword));
    importProject.selectProjectByName(currentProjectName);

    importProject.clickImportBtn();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.clickSaveButton();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(currentProjectName);

    // check GitHub identity is present in Keycloak account management page
    if (isMultiuser) {
      keycloakFederatedIdentitiesPage.open();

      // set to lower case because it's a normal behaviour (issue:
      // https://github.com/eclipse/che/issues/10138)
      assertEquals(
          keycloakFederatedIdentitiesPage.getGitHubIdentityFieldValue(),
          gitHubUsername.toLowerCase());
      ide.open(ws);
    }
  }

  @Test(priority = 1)
  public void keepDirectoryImportBySshUrlTest() {
    projectExplorer.waitProjectExplorer();
    currentProjectName = keepDirectoryRepo.getName() + "Ssh";

    makeKeepDirectoryFromGitUrl(
        keepDirectoryRepo.getSshUrl(), currentProjectName, MY_LIB_DIRECTORY);

    projectExplorer.waitItem(currentProjectName);
    projectExplorer.waitAndSelectItemByName(currentProjectName);
    projectExplorer.openItemByPath(currentProjectName);

    loader.waitOnClosed();
    projectExplorer.waitItemInvisibility(currentProjectName + "/my-webapp");
    projectExplorer.waitItem(currentProjectName + "/my-lib");

    expandDirectoryMyLib(currentProjectName);
  }

  @Test(priority = 1)
  public void keepDirectoryImportByHttpsUrlTest() {
    projectExplorer.waitProjectExplorer();
    currentProjectName = keepDirectoryRepo.getName() + "Https";

    makeKeepDirectoryFromGitUrl(keepDirectoryRepo.getHtmlUrl(), currentProjectName, TEST_DIRECTORY);

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

  @Test(priority = 1)
  public void keepDirectoryImportFromGitHub() throws Exception {
    projectExplorer.waitProjectExplorer();
    currentProjectName = keepDirectoryRepo.getName();

    menu.runCommand(WORKSPACE, IMPORT_PROJECT);

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

  @Test(priority = 1)
  public void checkImportProjectInBranchBySshUrl() {
    projectExplorer.waitProjectExplorer();
    currentProjectName = importBranchRepo.getName() + "Ssh";

    performImportIntoBranch(importBranchRepo.getSshUrl(), currentProjectName, TEST_BRANCH);
    projectExplorer.waitItem(currentProjectName);
    projectExplorer.waitAndSelectItemByName(currentProjectName);
    loader.waitOnClosed();

    menu.runCommand(GIT, BRANCHES);

    git.waitBranchInTheListWithCoState(TEST_BRANCH);

    git.closeBranchesForm();

    projectExplorer.waitItem(currentProjectName);

    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        currentProjectName + "/src/main/java/org.eclipse.qa.examples", "AppController.java");

    editor.waitActive();
  }

  @Test(priority = 1)
  public void checkImportProjectInBranchByHttpsUrl() {
    projectExplorer.waitProjectExplorer();
    currentProjectName = importBranchRepo.getName() + "Https";

    performImportIntoBranch(importBranchRepo.getHtmlUrl(), currentProjectName, ANOTHER_TEST_BRANCH);

    projectExplorer.waitItem(currentProjectName);

    projectExplorer.waitAndSelectItemByName(currentProjectName);
    loader.waitOnClosed();

    menu.runCommand(GIT, BRANCHES);

    git.waitBranchInTheListWithCoState(ANOTHER_TEST_BRANCH);

    git.closeBranchesForm();

    projectExplorer.waitItem(currentProjectName);

    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        currentProjectName + "/src/main/java/org.eclipse.qa.examples", "AppController.java");

    editor.waitActive();
  }

  @Test(priority = 1)
  public void checkImportProjectInBranchFromGitHub() throws Exception {
    projectExplorer.waitProjectExplorer();
    currentProjectName = importBranchRepo.getName();

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

    git.waitBranchInTheListWithCoState(BRANCH_WITH_CHANGES);

    git.closeBranchesForm();
    projectExplorer.openItemByPath(currentProjectName);
    loader.waitOnClosed();
    projectExplorer.openItemByPath(currentProjectName + "/AppController.java");

    editor.waitActive();
  }

  @Test(priority = 1)
  public void checkImportProjectSubmoduleByHttpsUrl() {
    projectExplorer.waitProjectExplorer();
    currentProjectName = multimoduleRepo.getName() + "Https";

    importRecursivelyFromGitUrl(multimoduleRepo.getHtmlUrl(), currentProjectName);
    openAndCheckSpringSubmodule(currentProjectName);
    openAndCheckRegularSubmodule(currentProjectName);
  }

  @Test(priority = 1)
  public void checkImportProjectSubmoduleBySshUrl() {
    projectExplorer.waitProjectExplorer();
    currentProjectName = multimoduleRepo.getName() + "Ssh";

    importRecursivelyFromGitUrl(multimoduleRepo.getSshUrl(), currentProjectName);
    openAndCheckSpringSubmodule(currentProjectName);
    openAndCheckRegularSubmodule(currentProjectName);
  }

  @Test(priority = 1)
  public void checkImportProjectSubmoduleFromGithub() throws Exception {
    projectExplorer.waitProjectExplorer();
    currentProjectName = multimoduleRepo.getName();

    importRecursivelyFromGitHub(currentProjectName);
    openAndCheckSpringSubmodule(currentProjectName);
    openAndCheckRegularSubmodule(currentProjectName);
  }

  private void importRecursivelyFromGitUrl(String url, String projectName) {
    loader.waitOnClosed();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);

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

  private void makeKeepDirectoryFromGitUrl(String url, String projectName, String folderName) {
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);

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

  private void performImportIntoBranch(String url, String projectName, String branchName) {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);

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

  private void expandDirectoryMyLib(String projectName) {
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/my-lib/src/main/java/hello", "SayHello.java");

    editor.waitActive();

    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/my-lib/src/test/java/hello", "SayHelloTest.java");

    editor.waitActive();
  }

  private void initRepoForLoginToGithubAndImportProject() throws IOException {
    testRepo.addContent(Paths.get(getClass().getResource("/projects/testRepo").getPath()));
  }

  private void initRepoForKeepDirectoryTest() throws IOException {
    keepDirectoryRepo.addContent(
        Paths.get(getClass().getResource("/projects/java-multimodule").getPath()));
  }

  private void initRepoForImportBranchTest() throws IOException {
    importBranchRepo.addContent(
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath()));

    importBranchRepo.createBranch(TEST_BRANCH);
    importBranchRepo.createBranch(ANOTHER_TEST_BRANCH);
    importBranchRepo.createBranch(BRANCH_WITH_CHANGES);

    importBranchRepo.addContent(
        Paths.get(getClass().getResource("/projects/Repo_For_Test_branch1").getPath()),
        BRANCH_WITH_CHANGES);
  }

  private void initRepoForMultiModuleTest() throws Exception {
    multimoduleRepo.addContent(
        Paths.get(getClass().getResource("/projects/java-multimodule").getPath()));
    multimoduleRepo.addSubmodule(
        Paths.get(getClass().getResource("/projects/Repo_For_Test").getPath()), SPRING_SUBMODULE);
    multimoduleRepo.addSubmodule(
        Paths.get(getClass().getResource("/projects/testRepo").getPath()), REGULAR_SUBMODULE);
  }

  private void importRecursivelyFromGitHub(String projectName) throws Exception {
    loader.waitOnClosed();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);

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

  private void openAndCheckSpringSubmodule(String projectName) {
    projectExplorer.openItemByPath(projectName);
    projectExplorer.waitAndSelectItem(projectName + "/" + SPRING_SUBMODULE);

    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.CONVERT_TO_PROJECT);

    projectWizard.waitOpenProjectConfigForm();
    projectWizard.waitTextParentDirectoryName("/" + projectName);
    projectWizard.waitTextProjectNameInput(SPRING_SUBMODULE);

    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();

    projectWizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitItem(projectName);

    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        projectName + "/" + SPRING_SUBMODULE + "/src/main/java/com.codenvy.example.spring",
        "GreetingController.java");

    projectExplorer.waitVisibilityByName("HelloWorld.java");

    editor.closeFileByNameWithSaving("GreetingController");
  }

  private void openAndCheckRegularSubmodule(String projectName) {
    projectExplorer.waitAndSelectItem(projectName + "/" + REGULAR_SUBMODULE);

    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.CONVERT_TO_PROJECT);

    projectWizard.waitOpenProjectConfigForm();
    projectWizard.waitTextParentDirectoryName("/" + projectName);
    projectWizard.waitTextProjectNameInput(REGULAR_SUBMODULE);

    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();

    projectWizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitItem(projectName);

    try {
      projectExplorer.expandPathInProjectExplorerAndOpenFile(
          projectName + "/" + REGULAR_SUBMODULE + "/src/main/java/com.company.example", "A.java");
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/10012", ex);
    }
    editor.closeFileByNameWithSaving("A");
  }

  private void importIntoBranchFromGitHub() throws Exception {
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);

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

    importProject.typeBranchName(BRANCH_WITH_CHANGES);
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
