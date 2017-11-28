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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.COMMIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.PUSH;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;

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
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Aleksandr Shmaraev
 * @author Igor Vinokur
 */
public class PushingChangesTest {
  private static final String PROJECT_NAME = NameGenerator.generate("PushingChangesTest-", 4);
  private static final String DEFAULT_COMMIT_SSH = "f99b08d23946ac4dc2749650e67875b4672e339c";
  private static final String COMMIT_MESSAGE = "edited and removed";
  private static final String REPO_NAME = "pushChangesTest";
  private static final String NEW_CONTENT_JSP = "<!-- JSP change -->";
  private static final String NEW_CONTENT_JAVA = "/* Java change */";
  private static final String PUSH_MSG = "Pushed to origin";
  private static final String PUSH_NOTHING = "Everything up-to-date";

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
  @Inject private TestGitHubServiceClient gitHubClientService;

  @BeforeClass
  public void prepare() throws Exception {
    testGitHubKeyUploader.updateGithubKey();
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    ide.open(ws);
  }

  @Test
  public void pushChangesTest() throws Exception {
    gitHubClientService.hardResetHeadToCommit(
        REPO_NAME, DEFAULT_COMMIT_SSH, gitHubUsername, gitHubPassword);

    // Clone project
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.IMPORT_PROJECT);
    importProject.waitAndTypeImporterAsGitInfo(
        "git@github.com:" + gitHubUsername + "/pushChangesTest.git", PROJECT_NAME);
    projectWizard.waitCreateProjectWizardForm();
    projectWizard.selectTypeProject(Wizard.TypeProject.MAVEN);
    loader.waitOnClosed();
    projectWizard.clickSaveButton();
    loader.waitOnClosed();
    projectWizard.waitCreateProjectWizardFormIsClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);

    // Create new file and push it.
    git.createNewFileAndPushItToGitHub(PROJECT_NAME, "new.html");
    consoles.waitProcessInProcessConsoleTree("Git push", LOADER_TIMEOUT_SEC);
    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/pushChangesTest.git");
    events.clickEventLogBtn();
    loader.waitOnClosed();
    events.waitExpectedMessage(PUSH_MSG);

    // Change contents index.jsp
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-webapp/src/main/webapp/index.jsp");
    editor.waitActiveEditor();
    editor.typeTextIntoEditor(NEW_CONTENT_JSP);
    editor.waitTabFileWithSavedStatus("index.jsp");
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving("index.jsp");
    editor.waitWhileFileIsClosed("index.jsp");

    // Edit GreetingController.java
    projectExplorer.openItemByVisibleNameInExplorer("GreetingController.java");
    editor.waitActiveEditor();
    editor.typeTextIntoEditor(Keys.DOWN.toString());
    editor.typeTextIntoEditor(NEW_CONTENT_JAVA);
    editor.waitTextIntoEditor(NEW_CONTENT_JAVA);
    editor.waitTabFileWithSavedStatus("GreetingController");
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving("GreetingController");
    editor.waitWhileFileIsClosed("GreetingController");

    // Commit changes
    projectExplorer.selectVisibleItem("GreetingController.java");
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunCommit(COMMIT_MESSAGE);
    loader.waitOnClosed();
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);

    // Push changes
    menu.runCommand(GIT, REMOTES_TOP, PUSH);
    loader.waitOnClosed();
    git.waitPushFormToOpen();
    git.clickPush();
    git.waitPushFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git push", LOADER_TIMEOUT_SEC);
    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/pushChangesTest.git");
    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);

    // Call Push again
    menu.runCommand(GIT, REMOTES_TOP, PUSH);
    loader.waitOnClosed();
    git.waitPushFormToOpen();
    git.clickPush();
    git.waitPushFormToClose();
    notifications.waitExpectedMessageOnProgressPanelAndClosed(PUSH_NOTHING);
    git.waitGitStatusBarWithMess(PUSH_NOTHING);
    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);
    events.clearAllMessages();

    // Soft reset
    gitHubClientService.hardResetHeadToCommit(
        REPO_NAME, DEFAULT_COMMIT_SSH, gitHubUsername, gitHubPassword);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.RESET);
    git.waitResetWindowOpen();
    git.selectCommitResetWindow(3);
    git.selectSoftReset();
    git.clickResetBtn();
    git.waitResetWindowClose();

    // Commit changes and push directly from commit window
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunCommitWithPush(COMMIT_MESSAGE, "origin/master");
    loader.waitOnClosed();
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.waitExpectedMessage(PUSH_MSG);

    // Amend commit
    projectExplorer.selectVisibleItem("GreetingController.java");
    menu.runCommand(GIT, COMMIT);
    git.waitAndRunAmendCommitMessage(COMMIT_MESSAGE);
    loader.waitOnClosed();
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);

    // Force push
    menu.runCommand(GIT, REMOTES_TOP, PUSH);
    loader.waitOnClosed();
    git.waitPushFormToOpen();
    git.selectForcePushCheckBox();
    git.clickPush();
    git.waitPushFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git push", LOADER_TIMEOUT_SEC);
    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/pushChangesTest.git");
    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);
  }
}
