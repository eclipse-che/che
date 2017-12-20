/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.git;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Date;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class CheckoutToRemoteBranchTest {
  private static final String PROJECT_NAME =
      NameGenerator.generate("CheckoutToRemoteBranchTest-", 4);
  private static final String PROJECT_NAME2 =
      NameGenerator.generate("CheckoutToRemoteBranchTest2-", 4);
  private static final String MASTER_BRANCH = "master";
  private static final String ORIGIN_MASTER_BRANCH = "origin/master";
  private static final String ORIGIN_SECOND_BRANCH = "origin/second_branch";
  private static final String SECOND_BRANCH = "second_branch";
  private static final String NAME_REMOTE_REPO = "origin";
  private static final String GIT_STATUS_MESS =
      " On branch " + SECOND_BRANCH + "\n" + " nothing to commit, working directory clean";

  private static final String PULL_MSG = "Already up-to-date";
  private static final String PUSH_MSG = "Pushed to origin";
  private static String COMMIT_MESS = "commitchk_remote";

  private String uniqueValue;
  private String uniqueValue2;

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NotificationsPopupPanel notifications;
  @Inject private ImportProjectFromLocation importFromLocation;
  @Inject private Wizard projectWizard;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;

  @BeforeClass
  public void prepare() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    ide.open(ws);
  }

  @Test
  public void checkoutToRemoteBranchTest() throws Exception {
    // Preconditions and import project
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);

    String cloneUrl = "git@github.com:" + gitHubUsername + "/springProjectWithSeveralBranches.git";
    importProjectFromRemoteRepo(cloneUrl, PROJECT_NAME);

    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(PROJECT_NAME);
    uniqueValue = String.valueOf(System.currentTimeMillis());

    // Open branches and check it and status
    checkoutToSecondRemoteBranch();
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(GIT_STATUS_MESS);

    // Check content in package and project explorer
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/helloworld/GreetingController.java");
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/webapp/index.jsp");
    projectExplorer.waitItemInVisibleArea("GreetingController.java");
    projectExplorer.waitItemInVisibleArea("index.jsp");
    editor.closeFileByNameWithSaving("index.jsp");
    editor.waitWhileFileIsClosed("index.jsp");

    // Make pull
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PULL);
    makePullSecondRemoteBranch();
    notifications.waitProgressPopupPanelClose();
    git.waitGitStatusBarWithMess(PULL_MSG);
    events.clickEventLogBtn();
    events.waitExpectedMessage(PULL_MSG);

    // Make some change in java file
    uniqueValue2 = String.valueOf(System.currentTimeMillis());
    projectExplorer.openItemByVisibleNameInExplorer("GreetingController.java");
    editor.waitActive();
    editor.selectLineAndDelete(2);
    editor.typeTextIntoEditor("//" + uniqueValue);
    editor.waitTextIntoEditor(uniqueValue);
    loader.waitOnClosed();
    editor.waitTabFileWithSavedStatus("GreetingController");
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving("GreetingController");
    editor.waitWhileFileIsClosed("GreetingController");

    // Make some change in jsp file
    projectExplorer.openItemByVisibleNameInExplorer("index.jsp");
    loader.waitOnClosed();
    editor.waitActive();
    editor.selectLineAndDelete(1);
    editor.typeTextIntoEditor("<!" + uniqueValue2 + ">");
    editor.waitTextIntoEditor(uniqueValue2);
    editor.waitTabFileWithSavedStatus("index.jsp");
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving("index.jsp");
    editor.waitWhileFileIsClosed("index.jsp");

    // Add all files to index and commit
    projectExplorer.selectItem(PROJECT_NAME + "/src/main");
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    COMMIT_MESS = COMMIT_MESS + new Date().toString();
    git.waitAndRunCommit(COMMIT_MESS);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);

    // Make push
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PUSH);
    git.waitPushFormToOpen();
    git.selectPushRemoteBranchName(SECOND_BRANCH);
    git.clickPush();
    git.waitPushFormToClose();

    String pushedMessage =
        "Successfully pushed to git@github.com:"
            + gitHubUsername
            + "/springProjectWithSeveralBranches.git";
    git.waitGitStatusBarWithMess(pushedMessage);

    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);

    // import from hosted git repository the second project
    projectExplorer.openItemByPath(PROJECT_NAME);
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProjectFromRemoteRepo(cloneUrl, PROJECT_NAME2);
    projectExplorer.waitItem(PROJECT_NAME2);
    projectExplorer.selectItem(PROJECT_NAME2);
    checkoutToSecondRemoteBranch();

    // Checking, that present earlier changes
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT_NAME2 + "/src/main/java/helloworld", "GreetingController.java");
    loader.waitOnClosed();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PROJECT_NAME2 + "/src/main/webapp", 3, "index.jsp");
    loader.waitOnClosed();
    projectExplorer.openItemByVisibleNameInExplorer("GreetingController.java");
    editor.waitTextIntoEditor(uniqueValue);
    editor.closeFileByNameWithSaving("GreetingController");
    editor.waitWhileFileIsClosed("GreetingController");
    projectExplorer.openItemByVisibleNameInExplorer("index.jsp");
    editor.waitTextIntoEditor(uniqueValue2);
    editor.closeFileByNameWithSaving("index.jsp");
    editor.waitWhileFileIsClosed("index.jsp");

    // Call and checking show history
    projectExplorer.selectItem(PROJECT_NAME2 + "/src");
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    loader.waitOnClosed();
    git.closeGitInfoPanel();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    loader.waitOnClosed();
    git.waitHistoryFormToOpen();
    git.waitCommitInHistoryForm(COMMIT_MESS);
    git.clickOnCommitInHistoryForm(COMMIT_MESS);
    loader.waitOnClosed();

    // check the change into git compare form
    git.clickCompareBtnGitHistory();
    git.waitGroupGitCompareIsOpen();
    git.selectFileInChangedFilesTreePanel("GreetingController.java");
    checkChangesIntoCompareForm(uniqueValue);
    seleniumWebDriver.switchTo().parentFrame();
    git.closeGitCompareForm();
    git.waitGroupGitCompareIsOpen();
    git.selectFileInChangedFilesTreePanel("index.jsp");
    checkChangesIntoCompareForm(uniqueValue2);
  }

  private void importProjectFromRemoteRepo(String urlRepo, String projectName) {
    importFromLocation.waitAndTypeImporterAsGitInfo(urlRepo, projectName);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(projectName);
    loader.waitOnClosed();
  }

  private void checkoutToSecondRemoteBranch() throws Exception {
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheList(MASTER_BRANCH);
    git.waitBranchInTheList(ORIGIN_MASTER_BRANCH);
    git.waitBranchInTheList(ORIGIN_SECOND_BRANCH);
    git.selectBranchAndClickCheckoutBtn(ORIGIN_SECOND_BRANCH);
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.BRANCHES);
    git.waitBranchInTheListWithCoState(SECOND_BRANCH);
    git.closeBranchesForm();
  }

  private void makePullSecondRemoteBranch() throws InterruptedException {
    git.waitPullFormToOpen();
    git.waitPullRemoteRepository(NAME_REMOTE_REPO);
    git.waitPullRemoteBranchName(SECOND_BRANCH);
    git.waitPullLocalBranchName(SECOND_BRANCH);
    git.clickPull();
    git.waitPullFormToClose();
  }

  private void checkChangesIntoCompareForm(String expText) {
    git.clickOnGroupCompareButton();
    git.waitGitCompareFormIsOpen();
    git.waitExpTextIntoCompareLeftEditor(expText);
    git.waitTextNotPresentIntoCompareRightEditor(expText);
  }
}
