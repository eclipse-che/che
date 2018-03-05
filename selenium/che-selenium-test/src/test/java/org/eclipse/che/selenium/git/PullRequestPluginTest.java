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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.IMPORT_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOAD_PAGE_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.BRANCH_PUSHED_ON_YOUR_ORIGIN;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.NEW_COMMITS_PUSHED;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.PULL_REQUEST_ISSUED;
import static org.eclipse.che.selenium.pageobject.PullRequestPanel.Status.PULL_REQUEST_UPDATED;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Andrey Chizhikov
 * @author Aleksandr Shmaraiev
 */
public class PullRequestPluginTest {
  private static final String FIRST_PROJECT_NAME = "pull-request-plugin-test";
  private static final String SECOND_PROJECT_NAME = "second-project-for-switching";
  private static final String CREATE_BRANCH = "Create new branch...";
  private static final String MAIN_BRANCH = "master";
  private static final String NAME_REPO = FIRST_PROJECT_NAME;
  private static final String PULL_REQUEST_CREATED = "Your pull request has been created.";
  private static final String PUll_REQUEST_UPDATED = "Your pull request has been updated.";
  private static final Long TIME = new Date().getTime();
  private static final String NEW_BRANCH = "branch-" + TIME;
  private static final String TITLE = "Title: " + TIME;
  private static final String COMMENT = "Comment: " + TIME;

  private WebDriverWait webDriverWait;
  private String factoryWsName;

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private TestUser user;
  @Inject private Loader loader;
  @Inject private ImportProjectFromLocation importWidget;
  @Inject private Menu menu;
  @Inject private ProjectExplorer explorer;
  @Inject private CodenvyEditor editor;
  @Inject private PullRequestPanel pullRequestPanel;
  @Inject private AskForValueDialog valueDialog;
  @Inject private Wizard wizard;
  @Inject private AskDialog askDialog;
  @Inject private Preferences preferences;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubServiceClient gitHubClientService;

  @BeforeClass
  public void setUp() throws Exception {
    webDriverWait = new WebDriverWait(seleniumWebDriver, LOAD_PAGE_TIMEOUT_SEC);
    ide.open(testWorkspace);
    // add committer info
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    // authorize application on GitHub
    menu.runCommand(
        TestMenuCommandsConstants.Profile.PROFILE_MENU,
        TestMenuCommandsConstants.Profile.PREFERENCES);
    preferences.waitPreferencesForm();
    preferences.generateAndUploadSshKeyOnGithub(gitHubUsername, gitHubPassword);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.deleteFactoryWorkspaces(testWorkspace.getName(), user.getName());

    List<String> listPullRequest =
        gitHubClientService.getNumbersOfOpenedPullRequests(
            NAME_REPO, gitHubUsername, gitHubPassword);

    if (!listPullRequest.isEmpty()) {
      gitHubClientService.closePullRequest(
          NAME_REPO, Collections.max(listPullRequest), gitHubUsername, gitHubPassword);
      gitHubClientService.deleteBranch(NAME_REPO, NEW_BRANCH, gitHubUsername, gitHubPassword);
    }
  }

  @Test(priority = 0)
  public void switchingBetweenProjects() {
    // import first project
    explorer.waitProjectExplorer();
    menu.runCommand(TestMenuCommandsConstants.Workspace.WORKSPACE, IMPORT_PROJECT);
    String firstProjectUrl =
        "https://github.com/" + gitHubUsername + "/pull-request-plugin-test.git";
    importWidget.waitAndTypeImporterAsGitInfo(firstProjectUrl, FIRST_PROJECT_NAME);
    configureTypeOfProject();
    // import second project
    explorer.waitProjectExplorer();
    menu.runCommand(TestMenuCommandsConstants.Workspace.WORKSPACE, IMPORT_PROJECT);
    String secondProjectUrl = "https://github.com/" + gitHubUsername + "/Spring_Project.git";
    importWidget.waitAndTypeImporterAsGitInfo(secondProjectUrl, SECOND_PROJECT_NAME);
    configureTypeOfProject();
    explorer.waitItem(FIRST_PROJECT_NAME);
    explorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    loader.waitOnClosed();

    // switch between project
    pullRequestPanel.clickPullRequestBtn();
    pullRequestPanel.waitRepoUrl(firstProjectUrl);
    pullRequestPanel.waitBranchName(MAIN_BRANCH);
    pullRequestPanel.waitProjectName(FIRST_PROJECT_NAME);
    explorer.waitAndSelectItem(SECOND_PROJECT_NAME);
    pullRequestPanel.waitRepoUrl(secondProjectUrl);
    pullRequestPanel.waitBranchName(MAIN_BRANCH);
    pullRequestPanel.waitProjectName(SECOND_PROJECT_NAME);
  }

  @Test(priority = 1)
  public void createPullRequest() {
    explorer.waitItem(FIRST_PROJECT_NAME);
    explorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    explorer.openItemByPath(FIRST_PROJECT_NAME);
    explorer.openItemByPath(FIRST_PROJECT_NAME + "/README.md");

    // change content
    editor.waitActive();
    editor.deleteAllContent();
    editor.goToCursorPositionVisible(1, 1);
    editor.typeTextIntoEditor("Time: " + TIME);

    // create branch
    pullRequestPanel.waitOpenPanel();
    pullRequestPanel.selectBranch(CREATE_BRANCH);
    valueDialog.waitFormToOpen();
    valueDialog.typeAndWaitText(NEW_BRANCH);
    valueDialog.clickOkBtn();
    valueDialog.waitFormToClose();
    pullRequestPanel.enterComment(COMMENT);
    pullRequestPanel.enterTitle(TITLE);

    // commit change and create pull request
    pullRequestPanel.clickCreatePRBtn();
    pullRequestPanel.clickOkCommitBtn();
    pullRequestPanel.waitStatusOk(BRANCH_PUSHED_ON_YOUR_ORIGIN);
    pullRequestPanel.waitStatusOk(PULL_REQUEST_ISSUED);
    pullRequestPanel.waitMessage(PULL_REQUEST_CREATED);
  }

  @Test(priority = 2)
  public void updatePullRequest() {
    editor.closeAllTabs();
    loader.waitOnClosed();
    explorer.openItemByPath(FIRST_PROJECT_NAME + "/README.md");
    editor.waitActive();
    editor.deleteAllContent();
    editor.goToCursorPositionVisible(1, 1);
    editor.typeTextIntoEditor("Update: " + TIME);
    pullRequestPanel.clickUpdatePRBtn();
    pullRequestPanel.clickOkCommitBtn();
    String expectedText =
        "Branch '"
            + gitHubUsername
            + ":"
            + NEW_BRANCH
            + "' is already used. Would you like to overwrite it?";
    askDialog.acceptDialogWithText(expectedText);
    pullRequestPanel.waitStatusOk(NEW_COMMITS_PUSHED);
    pullRequestPanel.waitStatusOk(PULL_REQUEST_UPDATED);
    pullRequestPanel.waitMessage(PUll_REQUEST_UPDATED);
  }

  @Test(priority = 3)
  public void checkFactoryOnGitHub() {
    String currentWindow = seleniumWebDriver.getWindowHandle();
    pullRequestPanel.openPullRequestOnGitHub();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWindow);
    checkGitHubUserPage();
    consumeFactoryOnGitHub();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    factoryWsName = seleniumWebDriver.getWorkspaceNameFromBrowserUrl();
    explorer.waitProjectExplorer();
    explorer.waitItem(FIRST_PROJECT_NAME);
    explorer.waitItem(SECOND_PROJECT_NAME);
    explorer.waitAndSelectItem(FIRST_PROJECT_NAME);
    explorer.openItemByPath(FIRST_PROJECT_NAME);
    explorer.openItemByPath(FIRST_PROJECT_NAME + "/README.md");
    editor.waitActive();
  }

  private void configureTypeOfProject() {
    wizard.selectTypeProject(Wizard.TypeProject.BLANK);
    loader.waitOnClosed();
    wizard.clickSaveButton();
    loader.waitOnClosed();
    wizard.waitCreateProjectWizardFormIsClosed();
  }

  /** check main elements of the GitHub user page */
  private void checkGitHubUserPage() {
    webDriverWait.until(
        visibilityOfElementLocated(By.xpath("//h1//a[text()='" + gitHubUsername + "']")));
    webDriverWait.until(
        visibilityOfElementLocated(By.xpath("//h1//a[text()='" + FIRST_PROJECT_NAME + "']")));
    webDriverWait.until(
        visibilityOfElementLocated(By.xpath("//h1//span[contains(text(), '" + TITLE + "')]")));
    webDriverWait.until(
        visibilityOfElementLocated(By.xpath("//span[text()='" + NEW_BRANCH + "']")));
    webDriverWait.until(visibilityOfElementLocated(By.xpath("//p[text()='" + COMMENT + "']")));
  }

  private void consumeFactoryOnGitHub() {
    webDriverWait
        .until(elementToBeClickable(By.xpath("//a[contains(@href, 'id=factory')]")))
        .click();
  }
}
