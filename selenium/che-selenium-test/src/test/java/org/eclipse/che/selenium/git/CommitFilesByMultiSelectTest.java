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
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author aleksandr shmaraev */
@Test(groups = TestGroup.GITHUB)
public class CommitFilesByMultiSelectTest {
  private static final String PROJECT_NAME = NameGenerator.generate("CommitByMultiSelect_", 4);
  private static final String PATH_FOR_EXPAND_FIRST_MODULE =
      PROJECT_NAME + "/my-lib/src/test/java/hello";
  private static final String PATH_FOR_EXPAND_SECOND_MODULE =
      PROJECT_NAME + "/my-webapp/src/main/java/helloworld";
  private static final String CHANGE_CONTENT = "***change content***";
  private static final String COMMIT_MESSAGE_1 = "first commit";
  private static final String COMMIT_MESSAGE_2 = "second commit";
  private static final String STATUS_MESSAGE_BEFORE_COMMIT =
      " On branch master\n"
          + " Changes not staged for commit:\n"
          + "  modified:   my-lib/src/test/java/hello/file.html\n"
          + " modified:   my-lib/src/test/java/hello/file.css\n"
          + " modified:   my-webapp/src/main/java/helloworld/AppController.java\n"
          + " modified:   my-lib/src/test/java/hello/SayHelloTest.java";
  private static final String STATUS_MESSAGE_AFTER_COMMIT =
      " On branch master\n"
          + " Changes not staged for commit:\n"
          + "  modified:   my-lib/src/test/java/hello/file.html\n"
          + " modified:   my-lib/src/test/java/hello/file.css";
  private static final String STATUS_MESS_AFTER_ADD_TO_INDEX =
      " On branch master\n"
          + " Changes to be committed:\n"
          + "  modified:   my-lib/src/test/java/hello/file.html\n"
          + " modified:   my-lib/src/test/java/hello/file.css\n"
          + " Changes not staged for commit:\n"
          + "  modified:   my-webapp/src/file.xml\n"
          + " modified:   my-webapp/src/file.js";
  private static final String CHANGE_TO_COMMIT_MESSAGE =
      " On branch master\n" + " nothing to commit, working directory clean";
  private static final String TEXT_GROUP_1 =
      "my-lib/src/test/java/hello\n"
          + "SayHelloTest.java\n"
          + "my-webapp/src/main/java/helloworld\n"
          + "AppController.java";
  private static final String TEXT_GROUP_2 =
      "my-lib/src/test/java/hello\n"
          + "file.css\n"
          + "file.html\n"
          + "my-webapp/src\n"
          + "file.js\n"
          + "file.xml";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private DefaultTestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    URL resource = getClass().getResource("/projects/java-project-for-multiselect");
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_JAVA_MULTIMODULE);
    ide.open(ws);
  }

  @Test
  public void commitFilesByMultiSelect() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY);
    askDialog.confirmAndWaitClosed();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_INITIALIZED_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_INITIALIZED_SUCCESS);
    loader.waitOnClosed();

    // Check the 'Cancel' button
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitCommitMainFormIsOpened();
    git.clickOnCancelBtnCommitForm();

    // perform init commit
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    // Edit SayHelloTest.java
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PATH_FOR_EXPAND_FIRST_MODULE, "SayHelloTest.java");
    loader.waitOnClosed();
    editor.setCursorToLine(16);
    editor.typeTextIntoEditor("//" + CHANGE_CONTENT);
    editor.waitTextIntoEditor("//" + CHANGE_CONTENT);

    // Edit GreetingController.java
    git.closeGitInfoPanel();
    projectExplorer.expandPathInProjectExplorerAndOpenFile(
        PATH_FOR_EXPAND_SECOND_MODULE, "AppController.java");
    loader.waitOnClosed();
    editor.setCursorToLine(16);
    editor.typeTextIntoEditor("//" + CHANGE_CONTENT);
    editor.waitTextIntoEditor("//" + CHANGE_CONTENT);

    // Edit several files in the first module
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-lib/src/test/java/hello/file.css");
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.PAGE_UP.toString());
    editor.typeTextIntoEditor(CHANGE_CONTENT);
    editor.waitTextIntoEditor(CHANGE_CONTENT);
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-lib/src/test/java/hello/file.html");
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.PAGE_UP.toString());
    editor.typeTextIntoEditor("<!" + CHANGE_CONTENT + ">");
    editor.waitTextIntoEditor("<!" + CHANGE_CONTENT + ">");

    // Check git status
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(STATUS_MESSAGE_BEFORE_COMMIT);

    // Perform the commit selected files
    projectExplorer.waitAndSelectItem(
        PROJECT_NAME + "/my-lib/src/test/java/hello/SayHelloTest.java");
    git.closeGitInfoPanel();
    projectExplorer.selectMultiFilesByCtrlKeys(
        PROJECT_NAME + "/my-webapp/src/main/java/helloworld/AppController.java");
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(COMMIT_MESSAGE_1);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(STATUS_MESSAGE_AFTER_COMMIT);
    git.closeGitInfoPanel();

    // Check the git history
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    git.waitHistoryFormToOpen();
    git.waitCommitInHistoryForm(COMMIT_MESSAGE_1);
    git.clickOnHistoryRowInСommitsList(0);
    loader.waitOnClosed();
    git.waitContentInHistoryEditor(COMMIT_MESSAGE_1);

    // check changes in git compare form
    git.clickCompareBtnGitHistory();
    checkChangesIntoCompareForm(CHANGE_CONTENT);
    git.closeGitCompareForm();
    git.waitHistoryFormToOpen();
    git.closeGitHistoryForm();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    git.waitHistoryFormToOpen();
    git.clickOnHistoryRowInСommitsList(0);
    loader.waitOnClosed();
    git.clickCompareBtnGitHistory();
    git.waitGroupGitCompareIsOpen();
    git.waitExpTextInGroupGitCompare(TEXT_GROUP_1);
    git.selectFileInChangedFilesTreePanel("AppController.java");
    git.clickOnGroupCompareButton();
    checkChangesIntoCompareForm(CHANGE_CONTENT);
    git.closeGitCompareForm();
    git.closeGroupGitCompareForm();
    git.closeGitHistoryForm();

    // Add the 'file.css' and 'file.html' to index
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    loader.waitOnClosed();
    git.closeGitInfoPanel();

    // Edit several files in the second module
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-webapp/src/file.xml");
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.PAGE_UP.toString());
    editor.typeTextIntoEditor("<!" + CHANGE_CONTENT + ">");
    editor.waitTextIntoEditor("<!" + CHANGE_CONTENT + ">");
    projectExplorer.openItemByPath(PROJECT_NAME + "/my-webapp/src/file.js");
    editor.waitActive();
    editor.typeTextIntoEditor(CHANGE_CONTENT);
    editor.waitTextIntoEditor(CHANGE_CONTENT);

    // Check git status
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(STATUS_MESS_AFTER_ADD_TO_INDEX);
    loader.waitOnClosed();
    git.closeGitInfoPanel();

    // Perform the commit and add selected files
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/my-webapp/src/file.xml");
    projectExplorer.selectMultiFilesByCtrlKeys(PROJECT_NAME + "/my-webapp/src/file.js");
    projectExplorer.selectMultiFilesByCtrlKeys(
        PROJECT_NAME + "/my-lib/src/test/java/hello/file.css");
    projectExplorer.selectMultiFilesByCtrlKeys(
        PROJECT_NAME + "/my-lib/src/test/java/hello/file.html");
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(COMMIT_MESSAGE_2);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(CHANGE_TO_COMMIT_MESSAGE);
    git.closeGitInfoPanel();

    // Check the git history
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    git.waitHistoryFormToOpen();
    git.waitCommitInHistoryForm(COMMIT_MESSAGE_2);
    git.clickOnHistoryRowInСommitsList(0);
    loader.waitOnClosed();
    git.waitContentInHistoryEditor(COMMIT_MESSAGE_2);

    // check changes in git compare form
    git.clickCompareBtnGitHistory();
    git.waitGroupGitCompareIsOpen();
    git.waitExpTextInGroupGitCompare(TEXT_GROUP_2);
    git.selectFileInChangedFilesTreePanel("file.css");
    git.clickOnGroupCompareButton();
    checkChangesIntoCompareForm(CHANGE_CONTENT);
    git.closeGitCompareForm();
    git.waitGroupGitCompareIsOpen();
    git.selectFileInChangedFilesTreePanel("file.js");
    git.clickOnGroupCompareButton();
    checkChangesIntoCompareForm(CHANGE_CONTENT);
  }

  private void checkChangesIntoCompareForm(String expText) {
    git.waitGitCompareFormIsOpen();
    git.waitExpTextIntoCompareLeftEditor(expText);
    git.waitTextNotPresentIntoCompareRightEditor(expText);
  }
}
