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

import static org.eclipse.che.selenium.core.constant.TestGitConstants.GIT_INITIALIZED_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.ADD_TO_INDEX;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.DELETE_REPOSITORY;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.REMOVE_FROM_INDEX;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.RESET;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
import org.eclipse.che.selenium.pageobject.*;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Igor Vnokur */
@Test(groups = TestGroup.GITHUB)
public class GitColorsTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;
  @Inject private DefaultTestUser productUser;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private MachineTerminal terminal;
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());
    testProjectServiceClient.importProject(
        ws.getId(),
        Paths.get(getClass().getResource("/projects/default-spring-project").toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(ws);
  }

  @Test
  public void testUntrackedFileColor() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME);
    menu.runCommand(GIT, INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    git.waitGitStatusBarWithMess(GIT_INITIALIZED_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(GIT_INITIALIZED_SUCCESS);

    // Check file colors are yellow
    projectExplorer.waitYellowNode(PROJECT_NAME + "/README.md");
    projectExplorer.waitYellowNode(PROJECT_NAME + "/pom.xml");

    // perform init commit
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    // Check file to be in default color
    projectExplorer.openItemByPath(PROJECT_NAME + "/README.md");
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/README.md");
    editor.waitDefaultColorTab("README.md");

    // Remove file from index
    menu.runCommand(GIT, REMOVE_FROM_INDEX);
    git.waitRemoveFromIndexFileName("README.md");
    git.selectRemoveOnlyFromIndexCheckBox();
    git.confirmRemoveFromIndexForm();

    // Check file colors are yellow
    projectExplorer.waitYellowNode(PROJECT_NAME + "/README.md");
    editor.waitYellowTab("README.md");

    // Add to index
    menu.runCommand(GIT, ADD_TO_INDEX);
    git.waitAddToIndexFormToOpen();
    git.confirmAddToIndexForm();

    // Check files are in default color
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/README.md");
    editor.waitDefaultColorTab("README.md");
  }

  @Test(priority = 1)
  public void testUntrackedFileColorFromTerminal() {
    // Remove file from index
    terminal.selectTerminalTab();
    terminal.typeIntoTerminal("cd " + PROJECT_NAME + Keys.ENTER);
    terminal.typeIntoTerminal("git rm --cached README.md" + Keys.ENTER);

    // Check file colors are yellow
    projectExplorer.waitYellowNode(PROJECT_NAME + "/README.md");
    editor.waitYellowTab("README.md");

    // Add to index
    terminal.typeIntoTerminal("git add README.md" + Keys.ENTER);

    // Check files are in default color
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/README.md");
    editor.waitDefaultColorTab("README.md");
  }

  @Test(priority = 2)
  public void testNewFileColor() {
    // Create new file
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("newFile");
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    editor.waitYellowTab("newFile");

    // check that the file color is yellow
    projectExplorer.waitYellowNode(PROJECT_NAME + "/newFile");

    // add file to index
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/newFile");
    menu.runCommand(GIT, ADD_TO_INDEX);
    git.waitAddToIndexFormToOpen();
    git.confirmAddToIndexForm();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);

    // check that file color is green
    projectExplorer.waitGreenNode(PROJECT_NAME + "/newFile");
    editor.waitGreenTab("newFile");
  }

  @Test(priority = 3)
  public void testModifiedFilesColor() {
    // Check file is colored in default color
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/README.md");
    editor.waitDefaultColorTab("README.md");

    // Make a change
    editor.selectTabByName("README.md");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTabFileWithSavedStatus("README.md");
    projectExplorer.waitBlueNode(PROJECT_NAME + "/README.md");
    editor.waitBlueTab("README.md");

    // check that the file color is blue
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.waitTabFileWithSavedStatus("README.md");
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/README.md");
    editor.waitDefaultColorTab("README.md");
  }

  @Test(priority = 4)
  public void testFileColorsAfterCommitFromMenu() {
    // Make a change
    editor.selectTabByName("README.md");
    editor.typeTextIntoEditor("//change" + Keys.SPACE);
    editor.waitTabFileWithSavedStatus("README.md");

    // check that the file color is blue
    projectExplorer.waitBlueNode(PROJECT_NAME + "/README.md");
    editor.waitBlueTab("README.md");

    // Perform commit
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("commit");
    git.waitCommitFormClosed();

    // Check files are colored in default color
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/newFile");
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/README.md");
    editor.waitDefaultColorTab("newFile");
    editor.waitDefaultColorTab("README.md");
  }

  @Test(priority = 5)
  public void testFileColorsAfterCommitFromTerminal() {
    // Soft reset to previous commit
    menu.runCommand(GIT, RESET);
    git.waitResetWindowOpen();
    git.selectCommitResetWindow(2);
    git.selectSoftReset();
    git.clickResetBtn();
    git.waitResetWindowClose();

    loader.waitOnClosed();
    terminal.selectTerminalTab();
    terminal.typeIntoTerminal("cd " + PROJECT_NAME + Keys.ENTER);
    terminal.typeIntoTerminal("git config --global user.email \"git@email.com\"" + Keys.ENTER);
    terminal.typeIntoTerminal("git config --global user.name \"name\"" + Keys.ENTER);
    terminal.typeIntoTerminal("git commit -a -m 'Terminal commit'" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal("2 files changed, 1 insertion(+), 1 deletion(-)");

    // Check files are colored in default color
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/newFile");
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/README.md");
    editor.waitDefaultColorTab("newFile");
    editor.waitDefaultColorTab("README.md");
  }

  @Test(priority = 6)
  public void testFileColorsAfterDeleteRepository() {
    menu.runCommand(GIT, DELETE_REPOSITORY);
    askDialog.acceptDialogWithText("Are you sure you want to delete ");

    // Check files are colored in default color
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/newFile");
    projectExplorer.waitDefaultColorNode(PROJECT_NAME + "/README.md");
    editor.waitDefaultColorTab("newFile");
    editor.waitDefaultColorTab("README.md");
  }
}
