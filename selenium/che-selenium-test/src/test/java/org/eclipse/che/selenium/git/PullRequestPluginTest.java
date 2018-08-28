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
import static java.util.regex.Pattern.compile;
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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.provider.TestIdeUrlProvider;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
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
  private static final String BASE_BRANCH_NAME = generate("base-", 4);
  private static final String NEW_BRANCH_NAME = generate("new-", 8);
  private static final String TITLE = generate("Title: ", 8);
  private static final String COMMENT = generate("Comment: ", 8);
  private static final String PATH_TO_README_FILE_1ST_PROJECT = FIRST_PROJECT_NAME + "/README.md";
  private static final String PATH_TO_README_FILE_2ND_PROJECT = SECOND_PROJECT_NAME + "/README.md";
  private static final String READ_FACTORY_URL_FROM_PR_DESCRIPTION_TEMPLATE =
      "\\[!\\[Review\\]\\(.*%1$sfactory/resources/factory-review.svg\\)\\]\\((.*%1$sf\\?id=factory.*)\\).*"
          + COMMENT;

  private String mainBrowserTabHandle;
  private String firstProjectUrl;
  private String secondProjectUrl;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private TestIdeUrlProvider testIdeUrlProvider;

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
  @Inject private TestWorkspaceServiceClient testWorkspaceServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Consoles consoles;

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

    // wait until jdt.ls initialized this need to avoid problem in next steps of test
    consoles.waitJDTLSStartedMessage();
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
  public void deleteFactoryWorkspace() throws Exception {
    testWorkspaceServiceClient.deleteFactoryWorkspaces(testWorkspace.getName(), user.getName());
  }

  @AfterClass
  public void restoreContributionTabPreference() throws Exception {
    testUserPreferencesServiceClient.restoreDefaultContributionTabPreference();
  }

  @Test
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

    // check the base and head branches in the pull request
    assertEquals(testRepo2.getPullRequestHeadBranchName(1), DEV_BRANCH_NAME);
    assertEquals(testRepo2.getPullRequestBaseBranchName(1), BASE_BRANCH_NAME);
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
    pullRequestPanel.clickRefreshBranchListButton();
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
  public void checkFactoryOnGitHub() throws IOException {
    // check pull request description
    assertEquals(testRepo2.getPullRequestUserName(1), gitHubUsername);
    assertEquals(testRepo2.getPullRequestTitle(1), TITLE);

    String pullRequestDescription = testRepo2.getPullRequestBody(1);

    Matcher matcher =
        compile(
                format(
                    READ_FACTORY_URL_FROM_PR_DESCRIPTION_TEMPLATE,
                    testIdeUrlProvider.get().toString()),
                Pattern.MULTILINE | Pattern.DOTALL)
            .matcher(pullRequestDescription);

    assertTrue(matcher.find(), format("Actual PR description was '%s'.", pullRequestDescription));

    // open factory from URL in pull request description
    String factoryUrlFromPrDescription = matcher.group(1);
    seleniumWebDriver.navigate().to(factoryUrlFromPrDescription);
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
}
