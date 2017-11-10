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
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class GitPullTest {
  private static final String FIRST_PROJECT_NAME = NameGenerator.generate("FirstProject-", 4);
  private static final String SECOND_PROJECT_NAME = NameGenerator.generate("SecondProject-", 4);
  private static final String REPO_NAME = "gitPullTest";
  private static final String NEW_CONTENT_JSP = "<!-- JSP change -->";
  private static final String NEW_CONTENT_JAVA = "/* Java change */";
  private static final String DEFAULT_COMMIT_SSH = "f99b08d23946ac4dc2749650e67875b4672e339c";
  private static final String COMMIT_MESSAGE = "edited and removed";
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
  public void pullTest() throws Exception {
    // Reset test repository's HEAD to default commit
    gitHubClientService.hardResetHeadToCommit(
        REPO_NAME, DEFAULT_COMMIT_SSH, gitHubUsername, gitHubPassword);
    projectExplorer.waitProjectExplorer();

    String repoUrl = "git@github.com:" + gitHubUsername + "/gitPullTest.git";
    git.importJavaApp(repoUrl, SECOND_PROJECT_NAME, Wizard.TypeProject.MAVEN);
    git.importJavaApp(repoUrl, FIRST_PROJECT_NAME, Wizard.TypeProject.MAVEN);

    projectExplorer.quickExpandWithJavaScript();

    // Change contents index.jsp
    loader.waitOnClosed();
    projectExplorer.openItemByPath(FIRST_PROJECT_NAME + "/my-webapp/src/main/webapp/index.jsp");
    editor.waitActiveEditor();
    editor.typeTextIntoEditor(NEW_CONTENT_JSP);
    editor.waitTextIntoEditor(NEW_CONTENT_JSP);
    editor.waitTabFileWithSavedStatus("index.jsp");
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving("index.jsp");
    editor.waitWhileFileIsClosed("index.jsp");

    // Change contents in java file
    projectExplorer.openItemByPath(
        FIRST_PROJECT_NAME + "/my-webapp/src/main/java/helloworld/GreetingController.java");
    editor.waitActiveEditor();
    editor.typeTextIntoEditor(Keys.DOWN.toString());
    editor.typeTextIntoEditor(NEW_CONTENT_JAVA);
    editor.waitTextIntoEditor(NEW_CONTENT_JAVA);
    editor.waitTabFileWithSavedStatus("GreetingController");
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving("GreetingController");
    editor.waitWhileFileIsClosed("GreetingController");

    // Remove web.xml from index
    projectExplorer.selectItem(FIRST_PROJECT_NAME + "/my-webapp/src/main/webapp/WEB-INF/web.xml");
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.REMOVE_FROM_INDEX);
    git.waitRemoveFromIndexFormToOpen();
    git.waitRemoveFromIndexFileName("Remove file web.xml from index?");
    git.confirmRemoveFromIndexForm();
    loader.waitOnClosed();

    // Add all files to index
    projectExplorer.waitItem(
        FIRST_PROJECT_NAME + "/my-webapp/src/main/java/helloworld/GreetingController.java");
    projectExplorer.waitItem(FIRST_PROJECT_NAME + "/my-webapp/src/main/webapp/index.jsp");
    loader.waitOnClosed();
    projectExplorer.selectItem(FIRST_PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);

    // Commit and push changes
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(COMMIT_MESSAGE);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PUSH);
    git.waitPushFormToOpen();
    git.clickPush();
    git.waitPushFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git push", LOADER_TIMEOUT_SEC);

    git.waitGitStatusBarWithMess("Successfully pushed");
    git.waitGitStatusBarWithMess("to git@github.com:" + gitHubUsername + "/gitPullTest.git");

    events.clickEventLogBtn();
    events.waitExpectedMessage(PUSH_MSG);
    projectExplorer.openItemByPath(FIRST_PROJECT_NAME);

    // Perform git pull and check the message in the second project
    projectExplorer.selectItem(SECOND_PROJECT_NAME);
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT,
        TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP,
        TestMenuCommandsConstants.Git.Remotes.PULL);
    git.waitPullFormToOpen();
    git.clickPull();
    git.waitPullFormToClose();
    loader.waitOnClosed();
    consoles.waitProcessInProcessConsoleTree("Git pull", LOADER_TIMEOUT_SEC);

    git.waitGitStatusBarWithMess("Successfully pulled");
    git.waitGitStatusBarWithMess("from git@github.com:" + gitHubUsername + "/gitPullTest");

    events.clickEventLogBtn();
    events.waitExpectedMessage("Pulled from git@github.com:" + gitHubUsername + "/gitPullTest.git");

    // check changes in the second project
    projectExplorer.openItemByPath(
        SECOND_PROJECT_NAME + "/my-webapp/src/main/java/helloworld/GreetingController.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(NEW_CONTENT_JAVA);
    projectExplorer.openItemByPath(SECOND_PROJECT_NAME + "/my-webapp/src/main/webapp/index.jsp");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(NEW_CONTENT_JSP);
    projectExplorer.waitItemIsDisappeared(
        SECOND_PROJECT_NAME + "/my-webapp/src/main/webapp/WEB-INF/web.xml");
  }
}
