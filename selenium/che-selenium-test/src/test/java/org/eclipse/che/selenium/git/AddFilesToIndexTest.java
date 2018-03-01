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
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maksim */
public class AddFilesToIndexTest {
  private static final String PROJECT_NAME = NameGenerator.generate("AddFilesToIndex_", 4);

  private static final String STATUS_MESSAGE_ONE_FILE =
      " On branch master\n"
          + " Changes to be committed:\n"
          + "  modified:   src/main/webapp/index.jsp";

  private static final String STATUS_MESSAGE_ALL_FILES =
      " On branch master\n"
          + " Changes to be committed:\n"
          + "  new file:   src/main/webapp/new.css\n"
          + " modified:   src/main/webapp/index.jsp\n"
          + " modified:   src/main/java/org/eclipse/qa/examples/AppController.java";

  private static final String STATUS_MESSAGE_AFTER_EDIT =
      " On branch master\n"
          + " Changes to be committed:\n"
          + "  new file:   src/main/webapp/new.css\n"
          + " modified:   src/main/webapp/index.jsp\n"
          + " modified:   src/main/java/org/eclipse/qa/examples/AppController.java\n"
          + " Changes not staged for commit:\n"
          + "  modified:   src/main/webapp/new.css\n"
          + " modified:   src/main/webapp/index.jsp\n"
          + " modified:   src/main/java/org/eclipse/qa/examples/AppController.java";

  private static final String STATUS_AFTER_DELETE_FILE =
      " On branch master\n"
          + " Changes to be committed:\n"
          + "  new file:   src/main/webapp/new.css\n"
          + " modified:   src/main/webapp/index.jsp\n"
          + " modified:   src/main/java/org/eclipse/qa/examples/AppController.java\n"
          + " deleted:    README.md";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private TestUser productUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @Test
  public void addFilesTest() throws InterruptedException {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_INITIALIZED_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_INITIALIZED_SUCCESS);

    // perform init commit
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    // check state of the index
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_NOTHING_TO_ADD);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_NOTHING_TO_ADD);

    // Edit index.jsp
    projectExplorer.openItemByVisibleNameInExplorer("index.jsp");
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.PAGE_DOWN.toString());
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("<!-- Testing add to index-->");
    loader.waitOnClosed();

    // Add this file to index
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/webapp/index.jsp");
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);

    // Check status
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    loader.waitOnClosed();
    git.waitGitStatusBarWithMess(STATUS_MESSAGE_ONE_FILE);

    // Edit GreetingController.java
    projectExplorer.openItemByPath(
        PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples/AppController.java");
    editor.waitActive();
    editor.setCursorToLine(16);
    editor.typeTextIntoEditor("//Testing add to index");
    loader.waitOnClosed();

    // Create new.css file
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/main/webapp");
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.CSS_FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("new");
    askForValueDialog.clickOkBtn();

    // Add all files to index
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitAddToIndexFormToOpen();
    git.waitAddToIndexFileName("Add content of folder " + PROJECT_NAME + " to index?");
    git.confirmAddToIndexForm();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);

    // Check status
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(STATUS_MESSAGE_ALL_FILES);

    // Edit GreetingController.java
    editor.selectTabByName("AppController");
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.DOWN.toString());
    editor.typeTextIntoEditor(Keys.DOWN.toString());
    editor.typeTextIntoEditor(Keys.DOWN.toString());
    editor.typeTextIntoEditor("//Testing add to index");
    loader.waitOnClosed();

    // Edit index.jsp
    editor.selectTabByName("index.jsp");
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.PAGE_DOWN.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("<!-- Testing add to index-->");
    loader.waitOnClosed();

    // Edit new.css
    editor.selectTabByName("new.css");
    editor.waitActive();
    editor.typeTextIntoEditor(Keys.PAGE_DOWN.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor("/* Testing add to index */");
    loader.waitOnClosed();

    // Check status and add to index all files
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(STATUS_MESSAGE_AFTER_EDIT);
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);

    // delete README file and add to index
    deleteFromMenuFile();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    git.waitGitStatusBarWithMess(STATUS_AFTER_DELETE_FILE);
  }

  private void deleteFromMenuFile() {
    loader.waitOnClosed();
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/README.md");
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    loader.waitOnClosed();
    askDialog.acceptDialogWithText("Delete file \"README.md\"?");
  }
}
