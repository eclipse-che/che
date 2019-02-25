/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import com.google.inject.name.Named;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.*;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Igor Vnokur */
@Test(groups = TestGroup.GITHUB)
public class GitChangeMarkersTest {
  private static final String PROJECT_NAME = NameGenerator.generate("GitColors_", 4);
  private static final String PATH_TO_JAVA_FILE = "/src/com/company/Main.java";

  @Inject private TestWorkspace ws;
  @Inject private Ide ide;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private DefaultTestUser productUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private AskDialog askDialog;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private CheTerminal terminal;
  @Inject private Git git;
  @Inject private Events events;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Consoles consoles;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    URL resource = getClass().getResource("/projects/simple-java-project");
    testProjectServiceClient.importProject(
        ws.getId(), Paths.get(resource.toURI()), PROJECT_NAME, ProjectTemplates.PLAIN_JAVA);
    ide.open(ws);
    consoles.waitJDTLSProjectResolveFinishedMessage(PROJECT_NAME);
  }

  @Test
  public void testModificationMarker() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.quickRevealToItemWithJavaScript(PROJECT_NAME + PATH_TO_JAVA_FILE);
    menu.runCommand(GIT, INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    git.waitGitStatusBarWithMess(GIT_INITIALIZED_SUCCESS);
    events.clickEventLogBtn();
    events.waitExpectedMessage(GIT_INITIALIZED_SUCCESS);

    // perform init commit
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("init");
    loader.waitOnClosed();

    projectExplorer.openItemByPath(PROJECT_NAME + PATH_TO_JAVA_FILE);
    editor.waitActive();
    editor.waitNoGitChangeMarkers();
    editor.typeTextIntoEditor("//", 12);
    editor.typeTextIntoEditor(Keys.END.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());

    editor.waitGitModificationMarkerInPosition(12, 14);
  }

  @Test(priority = 1)
  public void testInsertionMarker() {
    editor.setCursorToLine(17);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitGitInsertionMarkerInPosition(18, 19);
  }

  @Test(priority = 2)
  public void testDeletionMarker() {
    editor.setCursorToLine(21);
    editor.deleteCurrentLine();

    editor.waitGitDeletionMarkerInPosition(20);
  }

  @Test(priority = 3)
  public void testMarkersAfterCommit() {
    // perform  commit
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit("commit");
    loader.waitOnClosed();

    editor.waitNoGitChangeMarkers();
  }

  @Test(priority = 4)
  public void testChangeMarkersAfterCommitFromTerminal() {
    // Make a change
    editor.selectTabByName("Main");
    editor.typeTextIntoEditor("//", 13);
    editor.waitGitModificationMarkerInPosition(13, 13);

    terminal.selectFirstTerminalTab();
    terminal.typeIntoActiveTerminal("cd " + PROJECT_NAME + Keys.ENTER);
    terminal.typeIntoActiveTerminal(
        "git config --global user.email \"git@email.com\"" + Keys.ENTER);
    terminal.typeIntoActiveTerminal("git config --global user.name \"name\"" + Keys.ENTER);
    terminal.typeIntoActiveTerminal("git commit -a -m 'Terminal commit'" + Keys.ENTER);
    terminal.waitTextInFirstTerminal("1 file changed, 1 insertion(+), 1 deletion(-)");

    editor.waitNoGitChangeMarkers();
  }

  @Test(priority = 5)
  public void testChangeMarkersOnUntrackedFile() {
    // Make a change
    editor.selectTabByName("Main");
    editor.typeTextIntoEditor("//", 14);
    editor.waitGitModificationMarkerInPosition(14, 14);

    // Remove file from index
    projectExplorer.waitAndSelectItem(PROJECT_NAME + "/src/com/company/Main.java");
    menu.runCommand(GIT, REMOVE_FROM_INDEX);
    git.waitRemoveFromIndexFileName("Main");
    git.selectRemoveOnlyFromIndexCheckBox();
    git.confirmRemoveFromIndexForm();

    editor.waitNoGitChangeMarkers();
  }

  @Test(priority = 6)
  public void testChangeMarkersOnAddedToIndexAndUntrackedFileFromTerminal() {
    // Add file to index
    terminal.selectFirstTerminalTab();
    terminal.typeIntoActiveTerminal("git add src/com/company/Main.java" + Keys.ENTER);
    editor.waitGitModificationMarkerInPosition(14, 14);

    // Remove file from index
    terminal.typeIntoActiveTerminal("git rm --cached src/com/company/Main.java" + Keys.ENTER);

    editor.waitNoGitChangeMarkers();
  }

  @Test(priority = 7)
  public void testChangeMarkersOnAddedFile() {
    // Create new file
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    menu.runCommand(PROJECT, NEW, FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("newFile");
    askForValueDialog.clickOkBtn();
    askForValueDialog.waitFormToClose();
    editor.closeFileByNameWithSaving("Main");

    // Add file to index
    menu.runCommand(GIT, ADD_TO_INDEX);
    git.waitAddToIndexFormToOpen();
    git.confirmAddToIndexForm();

    // Make a change
    editor.selectTabByName("newFile");
    editor.typeTextIntoEditor("change", 1);
    editor.typeTextIntoEditor(Keys.ENTER.toString());
    editor.waitNoGitChangeMarkers();
  }
}
