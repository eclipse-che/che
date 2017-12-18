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
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubKeyUploader;
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
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev on 06.12.15. */
public class PushChangeNotUpdatedRepoTest {

  private static final String PROJECT_1 = NameGenerator.generate("PushChangeNotUpdatedRepo1-", 4);
  private static final String PROJECT_2 = NameGenerator.generate("PushChangeNotUpdatedRepo2-", 4);
  private static final String IMPORT_SUCCESS_1 = "Project " + PROJECT_1 + " imported";
  private static final String IMPORT_SUCCESS_2 = "Project " + PROJECT_2 + " imported";
  private static final String FILE_FOR_CHANGED_1 = "README.md";
  private static final String FILE_FOR_CHANGED_2 = "PushChangeNonUpdatedRepoTest.txt";
  private static final String MESSAGE_FOR_CHANGE = "//some change_" + System.currentTimeMillis();
  private static final String COMMIT_MESSAGE_1 = "commit-to-first-project";
  private static final String COMMIT_MESSAGE_2 = "commit-to-second-project";
  private static final String PUSH_MSG = "Pushed to origin";

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
  @Inject private NotificationsPopupPanel notifications;
  @Inject private Wizard projectWizard;
  @Inject private ImportProjectFromLocation importProject;
  @Inject private TestGitHubKeyUploader testGitHubKeyUploader;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    ide.open(ws);
  }

  @Test
  public void pushNoneUpdateTest() {
    // step 0 preconditions clone 2 repositories in 2 projects, add ssh keys for remote operations
    projectExplorer.waitProjectExplorer();
    String cloneUri = "git@github.com:" + gitHubUsername + "/testRepository.git";
    cloneProject(PROJECT_1, cloneUri);
    events.clickEventLogBtn();
    events.waitExpectedMessage(IMPORT_SUCCESS_1);
    cloneProject(PROJECT_2, cloneUri);
    events.waitExpectedMessage(IMPORT_SUCCESS_2);
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();

    // step 1 open project change some file commit and push
    projectExplorer.openItemByPath(PROJECT_1);
    projectExplorer.openItemByPath(PROJECT_1 + "/" + FILE_FOR_CHANGED_1);
    loader.waitOnClosed();
    editor.selectLineAndDelete();
    editor.waitEditorIsEmpty();
    editor.waitActive();
    editor.typeTextIntoEditor(MESSAGE_FOR_CHANGE);
    editor.waitTabFileWithSavedStatus(FILE_FOR_CHANGED_1);
    editor.closeFileByNameWithSaving(FILE_FOR_CHANGED_1);
    addToIndexAndCommitAll(COMMIT_MESSAGE_1, PROJECT_1);
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PUSH);
    loader.waitOnClosed();
    git.waitPushFormToOpen();
    git.clickPush();
    git.waitPushFormToClose();
    loader.waitOnClosed();
    notifications.waitProgressPopupPanelClose();
    consoles.waitProcessInProcessConsoleTree("Git push");
    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/testRepository.git");
    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);
    loader.waitOnClosed();

    // step 2 open second project change another file, add to index, commit, push
    // check conflict, pull
    projectExplorer.openItemByPath(PROJECT_2);

    // change one file in second project
    projectExplorer.openItemByPath(PROJECT_2 + "/" + FILE_FOR_CHANGED_2);
    projectExplorer.waitItem(PROJECT_2 + "/" + FILE_FOR_CHANGED_2);
    editor.waitActive();
    editor.selectLineAndDelete();
    editor.waitEditorIsEmpty();
    editor.waitActive();
    editor.typeTextIntoEditor(MESSAGE_FOR_CHANGE);
    editor.waitTabFileWithSavedStatus(FILE_FOR_CHANGED_2);
    editor.closeFileByNameWithSaving(FILE_FOR_CHANGED_2);
    addToIndexAndCommitAll(COMMIT_MESSAGE_2, PROJECT_2);

    // step 3 get conflict message
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PUSH);
    git.waitPushFormToOpen();
    git.clickPush();
    git.waitPushFormToClose();
    loader.waitOnClosed();
    notifications.waitProgressPopupPanelClose();
    consoles.waitProcessInProcessConsoleTree("Git push");
    git.waitGitStatusBarWithMess(
        "failed to push 'master -> master' to 'git@github.com:"
            + gitHubUsername
            + "/testRepository.git'."
            + " Try to merge remote changes using pull, and then push again.");
    events.clickEventLogBtn();
    events.waitExpectedMessage("Pushed to origin");

    // step 4 valid pull
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PULL);
    git.waitPullFormToOpen();
    git.clickPull();
    git.waitPullFormToClose();
    loader.waitOnClosed();

    git.waitGitStatusBarWithMess("Successfully pulled");
    git.waitGitStatusBarWithMess("from git@github.com:" + gitHubUsername + "/testRepository.git");

    events.clickEventLogBtn();
    events.waitExpectedMessage(
        "Pulled from git@github.com:" + gitHubUsername + "/testRepository.git");
    events.clearAllMessages();

    // step 5 success push
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PUSH);
    git.waitPushFormToOpen();
    git.clickPush();
    git.waitPushFormToClose();
    notifications.waitProgressPopupPanelClose();

    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/testRepository.git");

    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);
  }

  private void cloneProject(String project, String cloneURI) {
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitAndTypeImporterAsGitInfo(cloneURI, project);
    importProject.waitMainFormIsClosed();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(project);
  }

  private void addToIndexAndCommitAll(String commitMessage, String project) {
    projectExplorer.waitProjectExplorer();
    projectExplorer.selectItem(project);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    projectExplorer.selectItem(project);

    // commit
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(commitMessage);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
  }
}
