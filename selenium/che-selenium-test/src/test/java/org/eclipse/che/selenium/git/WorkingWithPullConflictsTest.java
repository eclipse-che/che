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

import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author aleksandr shmaraev */
public class WorkingWithPullConflictsTest {
  private static final String PROJECT_1 = NameGenerator.generate("first_project-", 4);
  private static final String PROJECT_2 = NameGenerator.generate("second_project-", 4);
  private static final String fileForChange = "JavaCommentsTest";
  private static final String fileForChange2 = "GitPullTest.txt";
  private static final String COMMIT_MSG = "commit_in_first_cloned";
  private static final String PUSH_MSG = "Pushed to origin";

  private static final String firstMergeConflictMessage =
      "Checkout operation failed, the following files would be overwritten by merge:\n"
          + "GitPullTest.txt\n"
          + "src/main/java/commenttest/JavaCommentsTest.java\n"
          + "Could not pull. Commit your changes before merging.";

  private static final String secondMergeConflictMessage =
      "Could not pull because a merge conflict is detected in the files:\n"
          + "GitPullTest.txt\n"
          + "src/main/java/commenttest/JavaCommentsTest.java\n"
          + "Automatic merge failed; fix conflicts and then commit the result.";

  private static final String CHANGE_STRING_1 =
      "//first_change" + String.valueOf(System.currentTimeMillis());
  private static final String CHANGE_STRING_2 = "//second_change";

  private static final String headConfPrefixConfMess =
      "<<<<<<< HEAD\n" + "//second_change\n" + "=======\n" + CHANGE_STRING_1 + "\n" + ">>>>>>>";

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
  @Inject private Consoles consoles;
  @Inject private Wizard projectWizard;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    ide.open(ws);
  }

  @Test
  public void pullConflictsTest() {
    // Preconditions and import 2 repositories in 2 projects
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);

    String cloneUri = "git@github.com:" + gitHubUsername + "/testRepo-3.git";
    importProjectFromRemoteRepo(cloneUri, PROJECT_1);
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProjectFromRemoteRepo(cloneUri, PROJECT_2);
    projectExplorer.quickExpandWithJavaScript();
    loader.waitOnClosed();

    // Change files in second project, commit and push to remote repo
    projectExplorer.openItemByPath(PROJECT_1);
    projectExplorer.openItemByPath(
        PROJECT_2 + "/src/main/java/commenttest/" + fileForChange + ".java");
    typeTextAndSaveIntoJavaClass(CHANGE_STRING_1);
    typeTextAndSaveIntoTextFile(CHANGE_STRING_1);
    projectExplorer.selectItem(PROJECT_2);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(COMMIT_MSG);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PUSH);
    git.waitPushFormToOpen();
    git.selectPushRemoteBranchName("master");
    git.clickPush();
    git.waitPushFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git push", LOADER_TIMEOUT_SEC);
    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/testRepo-3.git");
    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);

    // Open first project and change the same files
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(
        PROJECT_1 + "/src/main/java/commenttest/" + fileForChange + ".java");
    typeTextAndSaveIntoJavaClass(CHANGE_STRING_2);
    typeTextAndSaveIntoTextFile(CHANGE_STRING_2);

    // Make pull and get the first conflict
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PULL);
    git.waitPullFormToOpen();
    git.clickPull();
    git.waitPullFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git pull", LOADER_TIMEOUT_SEC);
    events.clickEventLogBtn();
    events.waitExpectedMessage(firstMergeConflictMessage);

    // Add to index and commit
    projectExplorer.selectItem(PROJECT_1);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(COMMIT_MSG);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);

    // Make pull again and get second conflict
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PULL);
    git.waitPullFormToOpen();
    git.clickPull();
    git.waitPullFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git pull", LOADER_TIMEOUT_SEC);
    events.clickEventLogBtn();
    events.waitExpectedMessage(secondMergeConflictMessage);
    loader.waitOnClosed();

    // Checking the message has present
    projectExplorer.openItemByPath(
        PROJECT_1 + "/src/main/java/commenttest/" + fileForChange + ".java");
    editor.waitTextIntoEditor(headConfPrefixConfMess);
    editor.closeFileByNameWithSaving(fileForChange);
    editor.waitWhileFileIsClosed(fileForChange);
    projectExplorer.openItemByVisibleNameInExplorer(fileForChange2);
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(headConfPrefixConfMess);
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

  private void typeTextAndSaveIntoJavaClass(String text) {
    editor.waitActiveEditor();
    editor.setCursorToLine(1);
    editor.selectLineAndDelete();
    editor.typeTextIntoEditor(text);
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(text);
    editor.waitTabFileWithSavedStatus(fileForChange);
    editor.closeFileByNameWithSaving(fileForChange);
    editor.waitWhileFileIsClosed(fileForChange);
  }

  private void typeTextAndSaveIntoTextFile(String text) {
    projectExplorer.openItemByVisibleNameInExplorer(fileForChange2);
    editor.waitActiveEditor();
    editor.selectLineAndDelete();
    editor.waitActiveEditor();
    editor.typeTextIntoEditor(text);
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(text);
    editor.waitTabFileWithSavedStatus(fileForChange2);
    editor.closeFileByNameWithSaving(fileForChange2);
    editor.waitWhileFileIsClosed(fileForChange2);
  }
}
