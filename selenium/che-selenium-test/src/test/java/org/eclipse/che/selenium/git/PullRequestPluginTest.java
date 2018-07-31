/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.git;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PREFERENCES;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Profile.PROFILE_MENU;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
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
import org.eclipse.che.selenium.pageobject.Preferences;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
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
  private static final String NEW_BRANCH = generate("branch-", 8);
  private static final String TITLE = generate("Title: ", 8);
  private static final String COMMENT = generate("Comment: ", 8);
  private static final String PATH_TO_README_FILE = FIRST_PROJECT_NAME + "/README.md";

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private Ide ide;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private Wizard wizard;
  @Inject private DefaultTestUser user;
  @Inject private CodenvyEditor editor;
  @Inject private AskDialog askDialog;
  @Inject private Preferences preferences;
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

  @BeforeClass
  public void setUp() throws Exception {
    Path entryPath =
        Paths.get(getClass().getResource("/projects/default-spring-project").getPath());
    testRepo.addContent(entryPath);
    testRepo2.addContent(entryPath);

    ide.open(testWorkspace);

    // add committer info
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, user.getEmail());

    // authorize application on GitHub
    menu.runCommand(PROFILE_MENU, PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.generateAndUploadSshKeyOnGithub(gitHubUsername, gitHubPassword);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.deleteFactoryWorkspaces(testWorkspace.getName(), user.getName());
  }

  @Test(priority = 0)
  public void switchingBetweenProjects() {
    String firstProjectUrl = testRepo.getHtmlUrl() + ".git";
    String secondProjectUrl = testRepo2.getHtmlUrl() + ".git";

    // import first project
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importWidget.waitAndTypeImporterAsGitInfo(firstProjectUrl, FIRST_PROJECT_NAME);
    configureTypeOfProject();

    // import second project
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, IMPORT_PROJECT);
    importWidget.waitAndTypeImporterAsGitInfo(secondProjectUrl, SECOND_PROJECT_NAME);
    configureTypeOfProject();

    projectExplorer.waitItem(FIRST_PROJECT_NAME);
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    loader.waitOnClosed();

    // switch between projects
    pullRequestPanel.clickPullRequestBtn();
    pullRequestPanel.waitRepoUrl(firstProjectUrl);
    pullRequestPanel.waitBranchName(MAIN_BRANCH);
    pullRequestPanel.waitProjectName(FIRST_PROJECT_NAME);
    projectExplorer.waitAndSelectItem(SECOND_PROJECT_NAME);
    pullRequestPanel.waitRepoUrl(secondProjectUrl);
    pullRequestPanel.waitBranchName(MAIN_BRANCH);
    pullRequestPanel.waitProjectName(SECOND_PROJECT_NAME);
  }

  @Test(priority = 1)
  public void createPullRequest() throws Exception {
    projectExplorer.waitItem(FIRST_PROJECT_NAME);
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    projectExplorer.openItemByPath(FIRST_PROJECT_NAME);

    // change content in README.md file
    openFileAndChangeContent(PATH_TO_README_FILE, generate("", 12));

    // create branch
    pullRequestPanel.waitOpenPanel();
    pullRequestPanel.selectBranch(CREATE_BRANCH);
    valueDialog.waitFormToOpen();
    valueDialog.typeAndWaitText(NEW_BRANCH);
    valueDialog.clickOkBtn();
    valueDialog.waitFormToClose();
    pullRequestPanel.enterComment(COMMENT);
    pullRequestPanel.enterTitle(TITLE);

    // change commit and create pull request
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
            gitHubUsername, NEW_BRANCH);

    editor.closeAllTabs();
    loader.waitOnClosed();

    // change content in README.md file
    openFileAndChangeContent(PATH_TO_README_FILE, generate("", 12));

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
    String currentWindow = seleniumWebDriver.getWindowHandle();

    // open and check projects page on github
    pullRequestPanel.openPullRequestOnGitHub();
    seleniumWebDriverHelper.switchToNextWindow(currentWindow);
    checkGitHubUserPage();

    consumeFactoryOnGitHub();
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(FIRST_PROJECT_NAME);
    projectExplorer.waitItem(SECOND_PROJECT_NAME);
    projectExplorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    projectExplorer.openItemByPath(FIRST_PROJECT_NAME);
    projectExplorer.openItemByPath(PATH_TO_README_FILE);
    editor.waitActive();
  }

  private void configureTypeOfProject() {
    wizard.selectTypeProject(BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    loader.waitOnClosed();
    wizard.waitCreateProjectWizardFormIsClosed();
  }

  private void openFileAndChangeContent(String filePath, String text) throws Exception {
    projectExplorer.openItemByPath(filePath);
    editor.waitActive();
    testProjectServiceClient.updateFile(testWorkspace.getId(), filePath, text);
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
