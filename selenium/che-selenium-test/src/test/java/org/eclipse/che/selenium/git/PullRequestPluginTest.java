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

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.BRANCHES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PREFERENCES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PROFILE_MENU;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.PanelSelector.PanelTypes.LEFT_RIGHT_BOTTOM_ID;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.BRANCH_PUSHED_ON_YOUR_ORIGIN;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.NEW_COMMITS_PUSHED;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.PULL_REQUEST_ISSUED;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.PULL_REQUEST_UPDATED;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.BLANK;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.PanelSelector;
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrey Chizhikov
 * @author Aleksandr Shmaraiev
 */
@Test(groups = TestGroup.GITHUB)
public class PullRequestPluginTest {
  private static final String FIRST_PROJECT_NAME = "pull-request-plugin-test";
  private static final String SECOND_PROJECT_NAME = "second-project-for-switching";
  private static final String CREATE_BRANCH = "Create new branch...";
  private static final String MAIN_BRANCH = "master";
  private static final String PULL_REQUEST_CREATED = "Your pull request has been created.";
  private static final String PUll_REQUEST_UPDATED = "Your pull request has been updated.";
  private static final String DEV_BRANCH_NAME = generate("dev-", 4);
  private static final String BASE_BRANCH_NAME = generate("contrib-", 4);
  private static final String NEW_BRANCH_NAME = generate("branch-", 8);
  private static final String TITLE = generate("Title: ", 8);
  private static final String COMMENT = generate("Comment: ", 8);
  private static final String PATH_TO_README_FILE_1ST_PROJECT = FIRST_PROJECT_NAME + "/README.md";
  private static final String PATH_TO_README_FILE_2ND_PROJECT = SECOND_PROJECT_NAME + "/README.md";

  private String mainBrowserTabHandle;
  private String firstProjectUrl;
  private String secondProjectUrl;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private Ide ide;
  @Inject private Git git;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private Wizard wizard;
  @Inject private DefaultTestUser user;
  @Inject private CodenvyEditor editor;
  @Inject private AskDialog askDialog;
  @Inject private Preferences preferences;
  @Inject private PanelSelector panelSelector;
  @Inject private TestWorkspace testWorkspace;
  @Inject private AskForValueDialog valueDialog;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TestGitHubRepository testRepo2;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private PullRequestPanel pullRequestPanel;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private ImportProjectFromLocation importWidget;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;

  @BeforeClass
  public void setUp() throws Exception {
    // preconditions
    Path entryPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath());
    testRepo.addContent(entryPath);
    testRepo2.addContent(entryPath);
    testRepo.createBranch(NEW_BRANCH_NAME);
    testRepo2.createBranch(BASE_BRANCH_NAME);

    ide.open(testWorkspace);
    mainBrowserTabHandle = seleniumWebDriver.getWindowHandle();

    // add committer info
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, user.getEmail());

    // authorize application on GitHub
    menu.runCommand(PROFILE_MENU, PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.generateAndUploadSshKeyOnGithub(gitHubUsername, gitHubPassword);

    // import the test projects
    firstProjectUrl = testRepo.getHttpsTransportUrl();
    secondProjectUrl = testRepo2.getHttpsTransportUrl();

    importProject(firstProjectUrl, FIRST_PROJECT_NAME);
    importProject(secondProjectUrl, SECOND_PROJECT_NAME);
  }

  @AfterMethod
  public void returnToMainWindow() {
    if (seleniumWebDriver.getWindowHandles().size() > 1) {
      seleniumWebDriverHelper.closeCurrentWindowAndSwitchToAnother(mainBrowserTabHandle);
    }
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.deleteFactoryWorkspaces(testWorkspace.getName(), user.getName());
  }

  @Test()
  public void switchingBetweenProjects() {
    projectExplorer.waitItem(FIRST_PROJECT_NAME);
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    loader.waitOnClosed();

    // check the data of the first repository
    pullRequestPanel.clickPullRequestBtn();
    pullRequestPanel.waitRepoUrl(firstProjectUrl);
    pullRequestPanel.waitBranchName(MAIN_BRANCH);
    pullRequestPanel.waitProjectName(FIRST_PROJECT_NAME);

    // checkout to another branch when the PR panel is closed
    pullRequestPanel.closePanelByHideButton();
    projectExplorer.waitAndSelectItem(SECOND_PROJECT_NAME);
    checkoutToBranch("origin/" + BASE_BRANCH_NAME);
    notificationsPopupPanel.waitPopupPanelsAreClosed();

    // open PR panel by the 'Panel Selector'
    panelSelector.selectPanelTypeFromPanelSelector(LEFT_RIGHT_BOTTOM_ID);
    pullRequestPanel.waitOpenPanel();

    // check the data of the second repository
    pullRequestPanel.waitRepoUrl(secondProjectUrl);
    pullRequestPanel.waitBranchName(BASE_BRANCH_NAME);
    pullRequestPanel.waitProjectName(SECOND_PROJECT_NAME);
  }

  @Test(priority = 1)
  public void createPullRequestToNonDefaultBranch() throws Exception {
    projectExplorer.waitItem(SECOND_PROJECT_NAME);
    projectExplorer.waitAndSelectItem(SECOND_PROJECT_NAME);
    projectExplorer.openItemByPath(SECOND_PROJECT_NAME);

    // change content in README.md file
    openFileAndChangeContent(PATH_TO_README_FILE_2ND_PROJECT, generate("", 12));

    // create branch
    pullRequestPanel.waitOpenPanel();
    pullRequestPanel.selectBranch(CREATE_BRANCH);
    valueDialog.waitFormToOpen();
    valueDialog.typeAndWaitText(DEV_BRANCH_NAME);
    valueDialog.clickOkBtn();
    valueDialog.waitFormToClose();
    pullRequestPanel.enterComment(COMMENT);
    pullRequestPanel.enterTitle(TITLE);

    // commit the change create pull request
    pullRequestPanel.clickCreatePullRequestButton();
    pullRequestPanel.clickOkCommitBtn();
    pullRequestPanel.waitStatusOk(BRANCH_PUSHED_ON_YOUR_ORIGIN);
    pullRequestPanel.waitStatusOk(PULL_REQUEST_ISSUED);
    pullRequestPanel.waitMessage(PULL_REQUEST_CREATED);

    // check the pull header on the GitHub page
    pullRequestPanel.openPullRequestOnGitHub();
    seleniumWebDriverHelper.switchToNextWindow(mainBrowserTabHandle);
    checkBranchNamesOnGitHubPage();
  }

  @Test(priority = 1)
  public void createPullRequestToDefaultBranch() throws Exception {
    projectExplorer.waitItem(FIRST_PROJECT_NAME);
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    projectExplorer.openItemByPath(FIRST_PROJECT_NAME);

    // checkout to another branch when the PR panel is opened
    pullRequestPanel.waitOpenPanel();
    checkoutToBranch("origin/" + NEW_BRANCH_NAME);
    pullRequestPanel.clickPullRequestBtn();
    pullRequestPanel.clickRefreshContributionBranchButton();
    pullRequestPanel.selectBranch(NEW_BRANCH_NAME);

    // change content in README.md file
    openFileAndChangeContent(PATH_TO_README_FILE_1ST_PROJECT, generate("", 12));
    pullRequestPanel.enterComment(COMMENT);
    pullRequestPanel.enterTitle(TITLE);

    // commit the change and create pull request
    pullRequestPanel.clickCreatePullRequestButton();
    pullRequestPanel.clickOkCommitBtn();
    pullRequestPanel.waitStatusOk(BRANCH_PUSHED_ON_YOUR_ORIGIN);
    pullRequestPanel.waitStatusOk(PULL_REQUEST_ISSUED);
    pullRequestPanel.waitMessage(PULL_REQUEST_CREATED);
  }

  @Test(priority = 2)
  public void updatePullRequest() throws Exception {
    String expectedText =
        format(
            "Branch '%s:%s' is already used. Would you like to overwrite it?",
            gitHubUsername, NEW_BRANCH_NAME);

    // change content in README.md file
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    openFileAndChangeContent(PATH_TO_README_FILE_1ST_PROJECT, generate("", 12));

    // update PR and check status
    pullRequestPanel.clickUpdatePullRequestButton();
    pullRequestPanel.clickOkCommitBtn();
    askDialog.acceptDialogWithText(expectedText);
    pullRequestPanel.waitStatusOk(NEW_COMMITS_PUSHED);
    pullRequestPanel.waitStatusOk(PULL_REQUEST_UPDATED);
    pullRequestPanel.waitMessage(PUll_REQUEST_UPDATED);
  }

  @Test(priority = 3)
  public void checkFactoryOnGitHub() {
    // open and check projects page on github
    pullRequestPanel.openPullRequestOnGitHub();
    seleniumWebDriverHelper.switchToNextWindow(mainBrowserTabHandle);
    checkGitHubUserPage();

    consumeFactoryOnGitHub();
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(FIRST_PROJECT_NAME);
    projectExplorer.waitItem(SECOND_PROJECT_NAME);
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    projectExplorer.openItemByPath(FIRST_PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_README_FILE_1ST_PROJECT);
    editor.waitActive();
  }

  private void importProject(String projectUrl, String projectName) {
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importWidget.waitAndTypeImporterAsGitInfo(projectUrl, projectName);
    configureTypeOfProject();
  }

  private void configureTypeOfProject() {
    wizard.selectTypeProject(BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    loader.waitOnClosed();
    wizard.waitCreateProjectWizardFormIsClosed();
  }

  private void checkoutToBranch(String branchName) {
    menu.runCommand(GIT, BRANCHES);
    git.waitBranchInTheList(branchName);
    git.selectBranchAndClickCheckoutBtn(branchName);
    git.waitGitCompareBranchFormIsClosed();
  }

  private void openFileAndChangeContent(String filePath, String text) throws Exception {
    projectExplorer.openItemByPath(filePath);
    editor.waitActive();
    testProjectServiceClient.updateFile(testWorkspace.getId(), filePath, text);
  }

  /** check the target branches in the pull header */
  private void checkBranchNamesOnGitHubPage() {
    String pullHeaderText =
        format(
            "%s wants to merge 1 commit into %s\n" + "from %s",
            gitHubUsername, BASE_BRANCH_NAME, DEV_BRANCH_NAME);
    seleniumWebDriverHelper.waitTextContains(
        By.xpath("//div[@class='TableObject-item TableObject-item--primary']"), pullHeaderText);
  }

  /** check main elements of the GitHub user page */
  private void checkGitHubUserPage() {
    seleniumWebDriverHelper.waitVisibility(By.xpath("//h1//a[text()='" + gitHubUsername + "']"));
    seleniumWebDriverHelper.waitVisibility(
        By.xpath("//h1//a[text()='" + testRepo.getName() + "']"));
    seleniumWebDriverHelper.waitVisibility(
        By.xpath("//h1//span[contains(text(), '" + TITLE + "')]"));
    seleniumWebDriverHelper.waitVisibility(By.xpath("//p[text()='" + COMMENT + "']"));
  }

  private void consumeFactoryOnGitHub() {
    seleniumWebDriverHelper.waitAndClick(By.xpath("//a[contains(@href, 'id=factory')]"));
  }
}
