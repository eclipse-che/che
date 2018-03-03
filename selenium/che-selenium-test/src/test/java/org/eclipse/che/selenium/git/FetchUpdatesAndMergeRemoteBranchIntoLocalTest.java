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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ImportProjectFromLocation;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author aleksandr shmaraev */
public class FetchUpdatesAndMergeRemoteBranchIntoLocalTest {

  private static final String PROJECT_1 = NameGenerator.generate("first_projectFetch_", 4);
  private static final String PROJECT_2 = NameGenerator.generate("second_projectFetch_", 4);
  private static final String FILE_FOR_CHANGE = "FetchUpdatesAndMergeRemoteBranchIntoLocalTest.txt";
  private static final String FILE_FOR_CHANGE_2 = "FetchUpdatesAndMergeRemoteBranchIntoLocalTest";
  private static final String NEW_FILE_NAME = "newFile.css";
  private static final String COMMIT_MESSAGE = String.valueOf(System.currentTimeMillis());
  private static final String PUSH_MSG = "Pushed to origin";
  private static final String MESSAGE_FOR_CHANGE_CONTENT =
      "//" + String.valueOf(System.currentTimeMillis()) + "_change_content";
  private static final String MASTER_BRANCH = "master";
  private static final String ORIGIN_MASTER = "origin/master";
  private static final String MERGE_MESSAGE_1 = "Fast-forward Merged commits:";
  private static final String MERGE_MESSAGE_2 = "New HEAD commit: ";
  private static final String MERGE_MESSAGE_3 = "Already up-to-date";

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
  @Inject private Consoles consoles;
  @Inject private CodenvyEditor editor;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private Wizard projectWizard;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubServiceClient gitHubClientService;

  @BeforeClass
  public void prepare() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    ide.open(ws);
  }

  @AfterMethod
  public void tearDown() throws Exception {
    // Restore file for next same test
    // Perform the hard reset HEAD on the remote repository
    gitHubClientService.hardResetHeadToCommit(
        "testRepo-1", "e50be4bbcc4b126fb3af29e29c32a95c7131eb31", gitHubUsername, gitHubPassword);
  }

  @Test
  public void fetchUpdatesAndMergeRemoteBranch() {
    // preconditions clone 2 repositories in 2 projects
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);

    String cloneUri = "git@github.com:" + gitHubUsername + "/testRepo-1.git";
    importProjectFromRemoteRepo(cloneUri, PROJECT_1);

    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProjectFromRemoteRepo(cloneUri, PROJECT_2);
    loader.waitOnClosed();

    // Open project_1, change text file and java file
    projectExplorer.openItemByPath(PROJECT_1);
    loader.waitOnClosed();
    typeTextAndSaveIntoTextFile(PROJECT_1, MESSAGE_FOR_CHANGE_CONTENT, FILE_FOR_CHANGE);
    loader.waitOnClosed();
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(
        PROJECT_1 + "/src/main/java/commenttest/" + FILE_FOR_CHANGE_2 + ".java");
    typeTextAndSaveIntoJavaClass(MESSAGE_FOR_CHANGE_CONTENT, FILE_FOR_CHANGE_2);

    // Add to index
    projectExplorer.waitAndSelectItem(PROJECT_1);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);

    // Remove file css from index
    projectExplorer.waitVisibilityByName(NEW_FILE_NAME);
    projectExplorer.waitAndSelectItem(PROJECT_1 + "/" + NEW_FILE_NAME);
    menu.runAndWaitCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.REMOVE_FROM_INDEX);
    git.waitRemoveFromIndexFormToOpen();
    git.waitRemoveFromIndexFileName("Remove file newFile.css from index?");
    git.confirmRemoveFromIndexForm();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_REMOVE_FROM_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_REMOVE_FROM_INDEX_SUCCESS);
    projectExplorer.waitItemInvisibility(PROJECT_1 + "/" + NEW_FILE_NAME);

    // Commit changes into master branch with specific comment
    projectExplorer.waitAndSelectItem(PROJECT_1);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(COMMIT_MESSAGE);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    projectExplorer.waitAndSelectItem(PROJECT_1);

    // Push changes to "master" branch of test remote repository
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PUSH);
    git.waitPushFormToOpen();
    loader.waitOnClosed();
    git.selectPushRemoteBranchName(MASTER_BRANCH);
    git.clickPush();
    git.waitPushFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git push");
    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/testRepo-1.git");
    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);

    // Open second project and fetch changes from master remote branch of test remote repository to
    // master local branch.
    projectExplorer.openItemByPath(PROJECT_1);
    loader.waitOnClosed();
    projectExplorer.waitAndSelectItem(PROJECT_2);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.FETCH);
    git.waitFetchFormOpened();
    git.clickOnFetchButton();
    git.waitFetchFormClosed();
    git.waitGitStatusBarWithMess("Fetched from " + cloneUri);
    events.clickEventLogBtn();
    events.waitExpectedMessage("Fetched from " + cloneUri);

    // Open changed in first project files file_1, file_2 and removed file
    projectExplorer.openItemByPath(PROJECT_2 + "/" + FILE_FOR_CHANGE);
    editor.waitActive();
    editor.waitTextNotPresentIntoEditor(MESSAGE_FOR_CHANGE_CONTENT);
    projectExplorer.openItemByPath(
        PROJECT_2 + "/src/main/java/commenttest/" + FILE_FOR_CHANGE_2 + ".java");
    editor.waitTextNotPresentIntoEditor("//" + MESSAGE_FOR_CHANGE_CONTENT);
    projectExplorer.waitVisibilityByName(NEW_FILE_NAME);

    // Open "Git > Merge..." window, choose remote branch master and merge
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.MERGE);
    mergeRemoteBranch(ORIGIN_MASTER);
    git.waitGitStatusBarWithMess(MERGE_MESSAGE_1);
    git.waitGitStatusBarWithMess(MERGE_MESSAGE_2);
    events.clickEventLogBtn();
    events.waitExpectedMessage(MERGE_MESSAGE_1);
    events.waitExpectedMessage(MERGE_MESSAGE_2);
    editor.closeAllTabs(); // TODO clarify the behaviour of the 'git merge'

    // Checking merging
    consoles.closeProcessesArea();
    projectExplorer.openItemByPath(PROJECT_2 + "/" + FILE_FOR_CHANGE);
    editor.waitActive();
    editor.waitTextIntoEditor(MESSAGE_FOR_CHANGE_CONTENT);
    projectExplorer.openItemByPath(
        PROJECT_2 + "/src/main/java/commenttest/" + FILE_FOR_CHANGE_2 + ".java");
    editor.waitTextIntoEditor(MESSAGE_FOR_CHANGE_CONTENT);
    projectExplorer.waitItemInvisibility(PROJECT_2 + "/" + NEW_FILE_NAME);

    // Merge remote branch master again
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.MERGE);
    mergeRemoteBranch(ORIGIN_MASTER);
    git.waitGitStatusBarWithMess(MERGE_MESSAGE_3);
    events.clickEventLogBtn();
    events.waitExpectedMessage(MERGE_MESSAGE_3);

    // View and check git history
    consoles.closeProcessesArea();
    projectExplorer.waitAndSelectItem(PROJECT_2);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    git.waitTextInHistoryForm(COMMIT_MESSAGE);
    git.clickOnHistoryRowInСommitsList(0);
    loader.waitOnClosed();
    git.waitContentInHistoryEditor(COMMIT_MESSAGE);
  }

  private void importProjectFromRemoteRepo(String urlRepo, String projectName) {
    importProject.waitAndTypeImporterAsGitInfo(urlRepo, projectName);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitItem(projectName);
    loader.waitOnClosed();
  }

  private void typeTextAndSaveIntoTextFile(String projectName, String text, String fileName) {
    projectExplorer.openItemByPath(projectName + "/" + fileName);
    loader.waitOnClosed();
    editor.waitActive();
    editor.selectLineAndDelete();
    editor.waitActive();
    editor.typeTextIntoEditor(text);
    editor.waitActive();
    editor.waitTextIntoEditor(text);
    editor.waitTabFileWithSavedStatus(fileName);
    editor.closeFileByNameWithSaving(fileName);
    editor.waitWhileFileIsClosed(fileName);
  }

  private void typeTextAndSaveIntoJavaClass(String text, String className) {
    editor.setCursorToLine(1);
    editor.selectLineAndDelete();
    editor.waitActive();
    editor.typeTextIntoEditor(text);
    editor.waitActive();
    editor.waitTextIntoEditor(text);
    editor.waitTabFileWithSavedStatus(className);
    editor.closeFileByNameWithSaving(className);
    editor.waitWhileFileIsClosed(className);
  }

  private void mergeRemoteBranch(String nameRemoteBranch) {
    git.waitMergeView();
    git.waitMergeReferencePanel();
    loader.waitOnClosed();
    git.waitMergeExpandRemoteBranchIcon();
    git.clickMergeExpandRemoteBranchIcon();
    git.waitItemInMergeList(nameRemoteBranch);
    git.clickItemInMergeList(nameRemoteBranch);
    git.clickMergeBtn();
    loader.waitOnClosed();
    git.waitMergeViewClosed();
  }
}
