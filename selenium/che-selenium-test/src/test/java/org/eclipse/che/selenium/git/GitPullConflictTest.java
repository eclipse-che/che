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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.PULL;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.Remotes.REMOTES_TOP;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.LOADER_TIMEOUT_SEC;
import static org.eclipse.che.selenium.pageobject.Wizard.TypeProject.MAVEN;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestGitHubServiceClient;
import org.eclipse.che.selenium.core.client.TestSshServiceClient;
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
import org.eclipse.che.selenium.pageobject.git.Git;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author aleksandr shmaraev */
public class GitPullConflictTest {
  private static final String REPO_NAME = NameGenerator.generate("PullConflictTest-", 3);
  private static final String PROJECT_NAME = NameGenerator.generate("PullConflictProject-", 4);
  private static final String pathToJavaFile = "src/main/java/commenttest";
  private static final String COMMIT_MSG = "commit_changes";

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
      String.format("//first_change_%s", System.currentTimeMillis());

  private static final String headConfPrefixConfMess =
      "<<<<<<< HEAD\n" + "//second_change\n" + "=======\n" + CHANGE_STRING_1 + "\n" + ">>>>>>>";

  private GitHub gitHub;
  private GHRepository gitHubRepository;

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
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private TestSshServiceClient testSshServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestGitHubServiceClient gitHubClientService;

  @BeforeClass
  public void prepare() throws Exception {
    gitHub = GitHub.connectUsingPassword(gitHubUsername, gitHubPassword);
    gitHubRepository = gitHub.createRepository(REPO_NAME).create();
    String commitMess = String.format("add-new-content %s ", System.currentTimeMillis());
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    Path entryPath = Paths.get(getClass().getResource("/projects/git-pull-conflict").getPath());
    gitHubClientService.addContentToRepository(entryPath, commitMess, gitHubRepository);
    ide.open(ws);
  }

  @AfterClass
  public void deleteRepo() throws IOException {
    gitHubRepository.delete();
  }

  @Test
  public void pullConflictsTest() throws IOException {
    // preconditions and import the test repo
    String javaFileChange = "JavaCommentsTest";
    String textFileChange = "GitPullTest.txt";
    String changeContent_2 = "//second_change";

    projectExplorer.waitProjectExplorer();
    String repoUrl = String.format("https://github.com/%s/%s.git", gitHubUsername, REPO_NAME);
    git.importJavaApp(repoUrl, PROJECT_NAME, MAVEN);

    // change files in the test repo on GitHub
    changeContentOnGithubSide(
        String.format("%s/%s.java", pathToJavaFile, javaFileChange), CHANGE_STRING_1);
    changeContentOnGithubSide(textFileChange, CHANGE_STRING_1);

    // change the same files in the editor
    changeJavaFileForTest(javaFileChange, changeContent_2);
    changeTextFileForTest(textFileChange, changeContent_2);

    // make pull and get the first conflict
    performPull();
    events.clickEventLogBtn();
    events.waitExpectedMessage(firstMergeConflictMessage);

    commitFiles();

    // Make pull again and get second conflict
    performPull();
    events.clickEventLogBtn();
    events.waitExpectedMessage(secondMergeConflictMessage);

    // Checking the message has present
    editor.selectTabByName(javaFileChange);
    editor.waitActive();
    editor.waitTextIntoEditor(headConfPrefixConfMess);
    editor.selectTabByName(textFileChange);
    editor.waitActive();
    editor.waitTextIntoEditor(headConfPrefixConfMess);
  }

  private void changeJavaFileForTest(String fileName, String text) {
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(
        String.format("%s/%s/%s.java", PROJECT_NAME, pathToJavaFile, fileName));
    editor.waitActive();
    editor.deleteAllContent();
    editor.typeTextIntoEditor(text);
    editor.waitActive();
    editor.waitTextIntoEditor(text);
    editor.waitTabFileWithSavedStatus(fileName);
  }

  private void changeTextFileForTest(String fileName, String text) {
    projectExplorer.openItemByPath(String.format("%s/%s", PROJECT_NAME, fileName));
    editor.waitActive();
    editor.setCursorToLine(1);
    editor.selectLineAndDelete();
    editor.typeTextIntoEditor(text);
    editor.waitActive();
    editor.waitTextIntoEditor(text);
    editor.waitTabFileWithSavedStatus(fileName);
  }

  private void changeContentOnGithubSide(String pathToContent, String content) throws IOException {
    gitHubRepository
        .getFileContent(String.format("/%s", pathToContent))
        .update(content, "add " + content);
  }

  private void performPull() {
    menu.runCommand(GIT, REMOTES_TOP, PULL);
    git.waitPullFormToOpen();
    git.clickPull();
    git.waitPullFormToClose();
    consoles.waitProcessInProcessConsoleTree("Git pull", LOADER_TIMEOUT_SEC);
  }

  private void commitFiles() {
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(COMMIT_MSG);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
  }
}
