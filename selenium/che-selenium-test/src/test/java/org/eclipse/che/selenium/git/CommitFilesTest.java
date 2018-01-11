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

import static org.testng.Assert.fail;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Refactor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author aleksandr shmaraev
 * @author igor vinokur
 */
public class CommitFilesTest {
  private static final String PROJECT_NAME = CommitFilesTest.class.getSimpleName();
  private static final String NEW_NAME_PACKAGE = "org.eclipse.dev.examples";
  private static final String PATH_TO_JAVA_FILE =
      PROJECT_NAME + "/src/main/java/org/eclipse/dev/examples/AppController.java";
  private static final String PATH_TO_JSP_FILE = PROJECT_NAME + "/src/main/webapp/index.jsp";
  private static final String MESSAGE_FOR_CHANGE_CONTENT = "<!--change content-->";
  private static final String COMMIT_MESSAGE = "first commit";
  private static final String NOTHING_TO_COMMIT_MESSAGE =
      " On branch master\n" + " nothing to commit, working directory clean";
  private static final String TEXT_GROUP =
      "src/main\n"
          + "java/org/eclipse/dev/examples\n"
          + "AppController.java\n"
          + "Hello.java\n"
          + "webapp\n"
          + "index.jsp\n"
          + "script.js";
  private static final String EXP_TEXT = "public class Hello";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private TestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private org.eclipse.che.selenium.pageobject.git.Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Refactor refactor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    URL resource = getClass().getResource("/projects/checkoutSpringSimple");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @AfterMethod
  public void closeForm() {
    if (git.isCommitWidgetOpened()) {
      git.clickOnCancelBtnCommitForm();
    }
  }

  @Test
  public void testCheckBoxSelections() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_INITIALIZED_SUCCESS);

    // unselect folder and check that all child nodes are also unselected
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.clickItemCheckBoxInCommitWindow("webapp");
    git.waitItemCheckBoxToBeIndeterminateInCommitWindow("src/main");
    git.waitItemCheckBoxToBeUnSelectedInCommitWindow(
        "webapp", "WEB-INF", "jsp", "guess_num.jsp", "web.xml", "spring-servlet.xml", "index.jsp");

    // select folder and check that all child nodes are also selected
    git.clickItemCheckBoxInCommitWindow("webapp");
    git.waitItemCheckBoxToBeSelectedInCommitWindow(
        "src/main",
        "webapp",
        "WEB-INF",
        "jsp",
        "guess_num.jsp",
        "web.xml",
        "spring-servlet.xml",
        "index.jsp");

    // unselect folder, select nested file, check that all parent folders of the file are also
    // selected
    git.clickItemCheckBoxInCommitWindow("webapp");
    git.clickItemCheckBoxInCommitWindow("guess_num.jsp");
    git.waitItemCheckBoxToBeIndeterminateInCommitWindow("src/main", "webapp", "WEB-INF");
    git.waitItemCheckBoxToBeSelectedInCommitWindow("jsp", "guess_num.jsp");
    git.waitItemCheckBoxToBeUnSelectedInCommitWindow("web.xml", "spring-servlet.xml", "index.jsp");

    // unselect nested file, check that all parent folders of the file are also unselected
    git.clickItemCheckBoxInCommitWindow("guess_num.jsp");
    git.waitItemCheckBoxToBeUnSelectedInCommitWindow(
        "webapp", "WEB-INF", "jsp", "guess_num.jsp", "web.xml", "spring-servlet.xml", "index.jsp");

    // select parent folder, unselect nested file, check that all nodes are selected except
    // unselected file and his folder
    git.clickItemCheckBoxInCommitWindow("webapp");
    git.clickItemCheckBoxInCommitWindow("guess_num.jsp");
    git.waitItemCheckBoxToBeIndeterminateInCommitWindow("webapp", "WEB-INF");
    git.waitItemCheckBoxToBeUnSelectedInCommitWindow("jsp", "guess_num.jsp");
    git.waitItemCheckBoxToBeSelectedInCommitWindow("web.xml", "spring-servlet.xml", "index.jsp");

    git.clickOnCancelBtnCommitForm();
    git.waitCommitFormClosed();
  }

  @Test(priority = 1)
  public void testFoldersStructureAfterRename() {
    projectExplorer.expandPathInProjectExplorer(PROJECT_NAME + "/src/main/java/");
    projectExplorer.selectItem(PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.Refactoring.REFACTORING,
        TestMenuCommandsConstants.Assistant.Refactoring.RENAME);
    refactor.waitRenamePackageFormIsOpen();
    refactor.setAndWaitStateUpdateReferencesCheckbox(true);
    loader.waitOnClosed();
    typeAndWaitNewName(NEW_NAME_PACKAGE);

    refactor.clickOkButtonRefactorForm();
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitCommitMainFormIsOpened();

    try {
      git.waitItemCheckBoxToBeSelectedInCommitWindow(
          "src/main",
          "java/org/eclipse/dev/examples",
          "AppController.java",
          "webapp",
          "WEB-INF",
          "jsp",
          "guess_num.jsp",
          "web.xml",
          "spring-servlet.xml",
          "index.jsp",
          "pom.xml");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8042", ex);
    }

    git.clickOnCancelBtnCommitForm();
    git.waitCommitFormClosed();
  }

  @Test(priority = 2)
  public void commitFilesTest() {
    // perform init commit without one folder
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);

    try {
      git.clickItemCheckBoxInCommitWindow("java/org/eclipse/dev/examples");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8042", ex);
    }

    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    // check git status
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(
        "new file:   src/main/java/org/eclipse/dev/examples/AppController.java");
    git.closeGitInfoPanel();
    loader.waitOnClosed();

    // perform commit of the folder
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    // check git status
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(NOTHING_TO_COMMIT_MESSAGE);
    git.closeGitInfoPanel();
    loader.waitOnClosed();

    // change content in AppController.java
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/main/java/org/eclipse/dev/examples");
    projectExplorer.openItemByPath(PATH_TO_JAVA_FILE);
    editor.waitActive();
    editor.setCursorToLine(12);
    editor.typeTextIntoEditor("//" + MESSAGE_FOR_CHANGE_CONTENT);
    editor.waitTextIntoEditor("//" + MESSAGE_FOR_CHANGE_CONTENT);
    editor.waitTabFileWithSavedStatus("AppController");
    editor.closeFileByNameWithSaving("AppController");
    editor.waitWhileFileIsClosed("AppController");

    // change content in index.jsp
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PATH_TO_JSP_FILE);
    editor.waitActive();
    loader.waitOnClosed();
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.PAGE_UP.toString());
    editor.typeTextIntoEditor(MESSAGE_FOR_CHANGE_CONTENT);
    editor.waitTextIntoEditor(MESSAGE_FOR_CHANGE_CONTENT);
    editor.waitTabFileWithSavedStatus("index.jsp");
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving("index.jsp");
    editor.waitWhileFileIsClosed("index.jsp");

    // Create Hello.java class
    projectExplorer.selectItem(PROJECT_NAME + "/src/main/java/org/eclipse/dev/examples");
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVA_CLASS);
    askForValueDialog.waitNewJavaClassOpen();
    askForValueDialog.typeTextInFieldName("Hello");
    askForValueDialog.clickOkBtnNewJavaClass();
    askForValueDialog.waitNewJavaClassClose();
    loader.waitOnClosed();
    projectExplorer.waitItemInVisibleArea("Hello.java");
    editor.waitTabIsPresent("Hello");
    loader.waitOnClosed();
    editor.closeFileByNameWithSaving("Hello");
    editor.waitWhileFileIsClosed("Hello");

    // Create script.js file
    projectExplorer.selectItem(PROJECT_NAME + "/src/main/webapp");
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.JAVASCRIPT_FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("script");
    askForValueDialog.clickOkBtn();
    loader.waitOnClosed();
    askForValueDialog.waitFormToClose();

    // Commit to repository and check status
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(COMMIT_MESSAGE);
    git.waitGitStatusBarWithMess(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.COMMIT_MESSAGE_SUCCESS);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(NOTHING_TO_COMMIT_MESSAGE);

    // View git history
    projectExplorer.selectItem(PROJECT_NAME + "/src/main");
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.SHOW_HISTORY);
    loader.waitOnClosed();
    git.waitTextInHistoryForm(COMMIT_MESSAGE);
    loader.waitOnClosed();
    git.clickOnHistoryRowInСommitsList(0);
    git.waitContentInHistoryEditor(COMMIT_MESSAGE);

    // Check diff in the git compare
    git.clickCompareBtnGitHistory();
    git.waitGroupGitCompareIsOpen();
    git.waitExpTextInGroupGitCompare(TEXT_GROUP);
    git.selectFileInChangedFilesTreePanel("AppController.java");
    checkChangesIntoCompareForm(MESSAGE_FOR_CHANGE_CONTENT);
    git.waitGroupGitCompareIsOpen();
    git.selectFileInChangedFilesTreePanel("Hello.java");
    checkChangesIntoCompareForm(EXP_TEXT);
  }

  private void checkChangesIntoCompareForm(String expText) {
    git.clickOnGroupCompareButton();
    git.waitGitCompareFormIsOpen();
    git.waitExpTextIntoCompareLeftEditor(expText);
    git.waitTextNotPresentIntoCompareRightEditor(expText);
    git.closeGitCompareForm();
  }

  private void typeAndWaitNewName(String newName) {
    try {
      refactor.typeAndWaitNewName(newName);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7500");
    }
  }
}
