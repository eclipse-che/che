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
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.*;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Igor Vnokur */
public class GitChangeMarkersTest {
  private static final String PROJECT_NAME = NameGenerator.generate("GitColors_", 4);

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private MachineTerminal terminal;
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/simple-java-project");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.PLAIN_JAVA);
    ide.open(ws);
  }

  @Test
  public void testModificationMarker() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    menu.runCommand(GIT, INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    git.waitGitStatusBarWithMess(GIT_INITIALIZED_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(GIT_INITIALIZED_SUCCESS);

    // perform init commit
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    projectExplorer.openItemByPath(PROJECT_NAME + "/src/com/company/Main.java");
    editor.waitNoGitChangeMarkers();
    editor.typeTextIntoEditor("//", 11);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.waitGitModificationMarkerInPosition(11, 13);
  }

  @Test(priority = 1)
  public void testInsertionMarker() {
    editor.setCursorToLine(16);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitGitInsertionMarkerInPosition(17, 18);
  }

  @Test(priority = 2)
  public void testDeletionMarker() {
    editor.setCursorToLine(20);
    editor.deleteCurrentLine();

    editor.waitGitDeletionMarkerInPosition(19);
  }

  @Test(priority = 3)
  public void testMarkersAfterCommit() {
    // perform  commit
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("commit");
    loader.waitOnClosed();

    editor.waitNoGitChangeMarkers();
  }

  @Test(priority = 4)
  public void testChangeMarkersAfterCommitFromTerminal() {
    // Make a change
    editor.selectTabByName("Main");
    editor.typeTextIntoEditor("//", 12);
    editor.waitGitModificationMarkerInPosition(12, 12);

    terminal.selectTerminalTab();
    terminal.typeIntoTerminal("cd " + PROJECT_NAME + Keys.ENTER);
    terminal.typeIntoTerminal("git config --global user.email \"git@email.com\"" + Keys.ENTER);
    terminal.typeIntoTerminal("git config --global user.name \"name\"" + Keys.ENTER);
    terminal.typeIntoTerminal("git commit -a -m 'Terminal commit'" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal("1 file changed, 1 insertion(+), 1 deletion(-)");

    editor.waitNoGitChangeMarkers();
  }

  @Test(priority = 5)
  public void testChangeMarkersOnUntrackedFile() {
    // Make a change
    editor.selectTabByName("Main");
    editor.typeTextIntoEditor("//", 13);
    editor.waitGitModificationMarkerInPosition(13, 13);

    // Remove file from index
    projectExplorer.selectItem(PROJECT_NAME + "/src/com/company/Main.java");
    menu.runCommand(GIT, REMOVE_FROM_INDEX);
    git.waitRemoveFromIndexFileName("Main");
    git.selectRemoveOnlyFromIndexCheckBox();
    git.confirmRemoveFromIndexForm();

    editor.waitNoGitChangeMarkers();
  }

  @Test(priority = 6)
  public void testChangeMarkersOnAddedToIndexAndUntrackedFileFromTerminal() {
    // Add file to index
    terminal.selectTerminalTab();
    terminal.typeIntoTerminal("git add src/com/company/Main.java" + Keys.ENTER);
    editor.waitGitModificationMarkerInPosition(13, 13);

    // Remove file from index
    terminal.typeIntoTerminal("git rm --cached src/com/company/Main.java" + Keys.ENTER);

    editor.waitNoGitChangeMarkers();
  }

  @Test(priority = 7)
  public void testChangeMarkersOnAddedFile() {
    // Create new file
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("newFile");
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();

    // Add file to index
    menu.runCommand(GIT, ADD_TO_INDEX);
    git.waitAddToIndexFormToOpen();
    git.confirmAddToIndexForm();

    // Make a change
    editor.selectTabByName("newFile");
    editor.typeTextIntoEditor("change", 1);

    editor.waitNoGitChangeMarkers();
  }
}
