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

import static org.eclipse.che.selenium.core.constant.TestGitConstants.GIT_INITIALIZED_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.*;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.FILE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.New.NEW;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Project.PROJECT;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.*;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Igor Vnokur */
public class GitColorsTest {
  private static final String PROJECT_NAME = NameGenerator.generate("GitColors_", 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.MAVEN_SPRING);
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
    events.clickProjectEventsTab();
    events.waitExpectedMessage(GIT_INITIALIZED_SUCCESS);

    // perform init commit
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    // Check file to be in default color
    projectExplorer.openItemByPath(PROJECT_NAME + "/README.md");
    projectExplorer.waitItemToBeDefaultColor(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeDefaultColor("README.md");

    // Remove file from index
    menu.runCommand(GIT, REMOVE_FROM_INDEX);
    git.waitRemoveFromIndexFileName("README.md");
    git.selectRemoveOnlyFromIndexCheckBox();
    git.confirmRemoveFromIndexForm();

    // Check file colors are yellow
    projectExplorer.waitItemToBeYellow(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeYellow("README.md");

    // Add to index
    menu.runCommand(GIT, ADD_TO_INDEX);
    git.waitAddToIndexFormToOpen();
    git.confirmAddToIndexForm();

    // Check files are in default color
    projectExplorer.waitItemToBeDefaultColor(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeDefaultColor("README.md");
  }

  @Test(priority = 1)
  public void testNewFileColor() {
    // Create new file
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("newFile");
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    editor.waitTabLabelToBeYellow("newFile");

    // check that the file color is yellow
    projectExplorer.waitItemToBeYellow(PROJECT_NAME + "/newFile");

    // add file to index
    projectExplorer.selectItem(PROJECT_NAME + "/newFile");
    menu.runCommand(GIT, ADD_TO_INDEX);
    git.waitAddToIndexFormToOpen();
    git.confirmAddToIndexForm();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_ADD_TO_INDEX_SUCCESS);

    // check that file color is green
    projectExplorer.waitItemToBeGreen(PROJECT_NAME + "/newFile");
    editor.waitTabLabelToBeGreen("newFile");
  }

  @Test(priority = 2)
  public void testModifiedFilesColor() {
    // Check file is colored in default color
    projectExplorer.waitItemToBeDefaultColor(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeDefaultColor("README.md");

    // Make a change
    editor.selectTabByName("README.md");
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitTabFileWithSavedStatus("README.md");
    projectExplorer.waitItemToBeBlue(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeBlue("README.md");

    // check that the file color is blue
    editor.typeTextIntoEditor(Keys.BACK_SPACE.toString());
    editor.waitTabFileWithSavedStatus("README.md");
    projectExplorer.waitItemToBeDefaultColor(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeDefaultColor("README.md");
  }

  @Test(priority = 3)
  public void testFileColorsAfterCommit() {
    // Make a change
    editor.selectTabByName("README.md");
    editor.typeTextIntoEditor("//change" + Keys.SPACE);
    editor.waitTabFileWithSavedStatus("README.md");

    // check that the file color is blue
    projectExplorer.waitItemToBeBlue(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeBlue("README.md");

    // Perform commit
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("commit");
    git.waitCommitFormClosed();

    // Check file is colored in default color
    projectExplorer.waitItemToBeDefaultColor(PROJECT_NAME + "/newFile");
    projectExplorer.waitItemToBeDefaultColor(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeDefaultColor("newFile");
    editor.waitTabLabelToBeDefaultColor("README.md");
  }

  @Test(priority = 4)
  public void testFileColorsAfterReset() {
    // Soft reset to previous commit
    menu.runCommand(GIT, RESET);
    git.waitResetWindowOpen();
    git.selectCommitResetWindow(2);
    git.selectSoftReset();
    git.clickResetBtn();
    git.waitResetWindowClose();

    // Check file colors
    projectExplorer.waitItemToBeGreen(PROJECT_NAME + "/newFile");
    projectExplorer.waitItemToBeBlue(PROJECT_NAME + "/README.md");
    editor.waitTabLabelToBeGreen("newFile");
    editor.waitTabLabelToBeBlue("README.md");
  }
}
