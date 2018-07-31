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

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.TestGroup;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestUserPreferencesServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.git.GitCompare;
import org.eclipse.che.selenium.pageobject.git.GitPanel;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Mykola Morhun */
// This test is disabled because git panel isn't ready for production and hidden
// Git panel epic: https://github.com/eclipse/che/issues/5128
// Issue: https://github.com/eclipse/che/issues/7022
@Test(groups = TestGroup.GITHUB)
public class GitPanelTest {

  private static final String JAVA_PLAIN_NON_GIT_PROJECT_NAME = "non-git-java-project";
  private static final String NODE_JS_GIT_PROJECT_NAME = "node-js";
  private static final String JAVA_SPRING_GIT_PROJECT_NAME = "web-java-spring";
  private static final String NEW_PROJECT_NAME = "new-project";
  private static final String RENAMED_JAVA_SPRING_GIT_PROJECT_NAME = "java-spring";

  private static final String NODE_JS_EDITED_FILE_NAME = "app.js";
  private static final String JAVA_SPRING_EDITED_FILE1_NAME = "AppController.java";
  private static final String JAVA_SPRING_EDITED_FILE2_NAME = "web.xml";
  private static final String JAVA_SPRING_DELETED_FILE_NAME = "spring-servlet.xml";
  private static final String JAVA_SPRING_ADDED_FILE_NAME = "test.java";

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DefaultTestUser productUser;
  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private Loader loader;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private Wizard wizard;
  @Inject private Menu menu;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CodenvyEditor editor;
  @Inject private CheTerminal terminal;
  @Inject private AskDialog askDialog;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestUserPreferencesServiceClient testUserPreferencesServiceClient;
  @Inject private Git git;
  @Inject private GitCompare gitCompare;
  @Inject private GitPanel gitPanel;

  @BeforeClass
  public void prepare() throws Exception {
    testUserPreferencesServiceClient.addGitCommitter(gitHubUsername, productUser.getEmail());

    URL resource;
    resource = getClass().getResource("/projects/node-js-simple");
    projectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        NODE_JS_GIT_PROJECT_NAME,
        ProjectTemplates.NODE_JS);

    resource = getClass().getResource("/projects/ProjectWithDifferentTypeOfFiles");
    projectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        JAVA_SPRING_GIT_PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    resource = getClass().getResource("/projects/plain-java-project");
    projectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        JAVA_PLAIN_NON_GIT_PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);

    ide.open(workspace);

    // make two projects to be under git
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(JAVA_PLAIN_NON_GIT_PROJECT_NAME);
    projectExplorer.waitItem(NODE_JS_GIT_PROJECT_NAME);
    projectExplorer.waitItem(JAVA_SPRING_GIT_PROJECT_NAME);

    createGitRepositoryInProject(NODE_JS_GIT_PROJECT_NAME);
    commitAllChangesInProject(NODE_JS_GIT_PROJECT_NAME, "initial commit");

    createGitRepositoryInProject(JAVA_SPRING_GIT_PROJECT_NAME);
    commitAllChangesInProject(JAVA_SPRING_GIT_PROJECT_NAME, "initial commit");
  }

  @Test(priority = 1, enabled = false)
  public void checkProjectsInGitPanel() {
    gitPanel.openPanel();
    // check that only projects under git is shown
    gitPanel.waitRepositories(2);
    assertTrue(gitPanel.isRepositoryPresent(NODE_JS_GIT_PROJECT_NAME));
    assertTrue(gitPanel.isRepositoryPresent(JAVA_SPRING_GIT_PROJECT_NAME));
    assertFalse(gitPanel.isRepositoryPresent(JAVA_PLAIN_NON_GIT_PROJECT_NAME));
  }

  @Test(priority = 2, enabled = false)
  public void checkChangesNumber() {
    projectExplorer.openAndWait();

    projectExplorer.quickExpandWithJavaScript();
    editFile(NODE_JS_EDITED_FILE_NAME);
    editFile(JAVA_SPRING_EDITED_FILE1_NAME);
    editFile(JAVA_SPRING_EDITED_FILE2_NAME);
    deleteFile(JAVA_SPRING_DELETED_FILE_NAME);
    createFileAndAddToIndex(JAVA_SPRING_GIT_PROJECT_NAME, JAVA_SPRING_ADDED_FILE_NAME);

    gitPanel.openPanel();

    gitPanel.waitRepositoryToHaveChanges(NODE_JS_GIT_PROJECT_NAME, 1);
    gitPanel.waitRepositoryToHaveChanges(JAVA_SPRING_GIT_PROJECT_NAME, 4);
  }

  @Test(priority = 3, enabled = false)
  public void shouldUpdateLabelsDynamically() {
    // check label update
    final String JAVA_SPRING_EDITED_FILE1_NAME_FILENAME =
        toEditorTabName(JAVA_SPRING_EDITED_FILE1_NAME);
    editor.selectTabByName(JAVA_SPRING_EDITED_FILE1_NAME_FILENAME);
    editor.waitActiveTabFileName(JAVA_SPRING_EDITED_FILE1_NAME_FILENAME);

    assertEquals(gitPanel.getRepositoryChanges(JAVA_SPRING_GIT_PROJECT_NAME), 4);
    editor.typeTextIntoEditor("\b");
    gitPanel.waitRepositoryToHaveChanges(JAVA_SPRING_GIT_PROJECT_NAME, 3);
    editor.typeTextIntoEditor(" ");
    gitPanel.waitRepositoryToHaveChanges(JAVA_SPRING_GIT_PROJECT_NAME, 4);

    // check label disappearance and appearance
    editor.selectTabByName(NODE_JS_EDITED_FILE_NAME);
    editor.waitActiveTabFileName(NODE_JS_EDITED_FILE_NAME);

    assertEquals(gitPanel.getRepositoryChanges(NODE_JS_GIT_PROJECT_NAME), 1);
    editor.typeTextIntoEditor("\b");
    gitPanel.waitRepositoryToBeClean(NODE_JS_GIT_PROJECT_NAME);
    editor.typeTextIntoEditor(" ");
    gitPanel.waitRepositoryToHaveChanges(NODE_JS_GIT_PROJECT_NAME, 1);
  }

  @Test(priority = 4, enabled = false)
  public void checkChangedFilesList() {
    gitPanel.selectRepository(NODE_JS_GIT_PROJECT_NAME);
    assertEquals(gitPanel.getRepositoryChanges(NODE_JS_GIT_PROJECT_NAME), 1);
    gitPanel.waitFileInChangesList(NODE_JS_EDITED_FILE_NAME);

    gitPanel.selectRepository(JAVA_SPRING_GIT_PROJECT_NAME);
    assertEquals(gitPanel.getRepositoryChanges(JAVA_SPRING_GIT_PROJECT_NAME), 4);
    gitPanel.waitFileInChangesList(JAVA_SPRING_EDITED_FILE1_NAME);
    gitPanel.waitFileInChangesList(JAVA_SPRING_EDITED_FILE2_NAME);
    gitPanel.waitFileInChangesList(JAVA_SPRING_DELETED_FILE_NAME);
    gitPanel.waitFileInChangesList(JAVA_SPRING_ADDED_FILE_NAME);
  }

  @Test(priority = 5, enabled = false)
  public void shouldShowDiffOfChangedItem() {
    gitPanel.selectRepository(JAVA_SPRING_GIT_PROJECT_NAME);

    gitPanel.openDiffForChangedFileWithDoubleClick(JAVA_SPRING_EDITED_FILE1_NAME);
    gitCompare.waitGitCompareFormIsOpen();
    gitCompare.clickOnCompareCloseButton();
    gitCompare.waitGitCompareFormIsClosed();

    gitPanel.openDiffForChangedFileWithEnterKey(JAVA_SPRING_EDITED_FILE2_NAME);
    gitCompare.waitGitCompareFormIsOpen();
    gitCompare.clickOnCompareCloseButton();
    gitCompare.waitGitCompareFormIsClosed();
  }

  @Test(priority = 6, enabled = false)
  public void shouldAddNewRepositoryIntoPanelWhenNewProjectUnderGitCreated() {
    assertEquals(gitPanel.countRepositories(), 2);
    projectExplorer.openAndWait();

    createProject(NEW_PROJECT_NAME, Wizard.SamplesName.CONSOLE_JAVA_SIMPLE);
    projectExplorer.waitAndSelectItem(NEW_PROJECT_NAME);

    gitPanel.openPanel();
    gitPanel.waitRepositories(3);
    assertTrue(gitPanel.isRepositoryPresent(NEW_PROJECT_NAME));
  }

  @Test(priority = 7, enabled = false)
  public void shouldRemoveRepositoryFromPanelWhenProjectUnderGitDeleted() {
    projectExplorer.openAndWait();

    projectExplorer.waitAndSelectItem(NEW_PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
    projectExplorer.waitDisappearItemByPath(NEW_PROJECT_NAME);

    gitPanel.openPanel();
    gitPanel.waitRepositories(2);
    assertFalse(gitPanel.isRepositoryPresent(NEW_PROJECT_NAME));
  }

  @Test(priority = 8, enabled = false)
  public void shouldRemoveRepositoryFromPanelWhenGitRepositoryDeletedFromProject() {
    projectExplorer.openAndWait();

    projectExplorer.waitAndSelectItem(NODE_JS_GIT_PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.DELETE_REPOSITORY);
    askDialog.acceptDialogWithText(
        "Are you sure you want to delete " + NODE_JS_GIT_PROJECT_NAME + '?');

    gitPanel.openPanel();
    gitPanel.waitRepositories(1);
    assertFalse(gitPanel.isRepositoryPresent(NODE_JS_GIT_PROJECT_NAME));
    assertTrue(gitPanel.isRepositoryPresent(JAVA_SPRING_GIT_PROJECT_NAME));
  }

  @Test(priority = 9, enabled = false)
  public void shouldAddNewRepositoryIntoPanelWhenProjectAddedUnderGit() {
    assertEquals(gitPanel.countRepositories(), 1);
    projectExplorer.openAndWait();

    projectExplorer.waitAndSelectItem(NODE_JS_GIT_PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY);
    askDialog.acceptDialogWithText(
        "Do you want to initialize the local repository " + NODE_JS_GIT_PROJECT_NAME + '?');

    gitPanel.openPanel();
    gitPanel.waitRepositories(2);
    assertTrue(gitPanel.isRepositoryPresent(NODE_JS_GIT_PROJECT_NAME));
    assertTrue(gitPanel.isRepositoryPresent(JAVA_SPRING_GIT_PROJECT_NAME));
  }

  @Test(priority = 10, enabled = false)
  public void shouldRenameRepositoryWhenProjectUnderGitRenamed() {
    projectExplorer.openAndWait();

    projectExplorer.waitAndSelectItem(JAVA_SPRING_GIT_PROJECT_NAME);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.RENAME);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.clearInput();
    askForValueDialog.typeAndWaitText(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME);
    askForValueDialog.clickOkBtn();
    projectExplorer.quickExpandWithJavaScript();

    gitPanel.openPanel();
    gitPanel.waitRepositories(2);
    gitPanel.waitRepositoryPresent(NODE_JS_GIT_PROJECT_NAME);
    gitPanel.waitRepositoryPresent(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME);
    assertFalse(gitPanel.isRepositoryClean(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME));
    assertEquals(gitPanel.getRepositoryChanges(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME), 4);
  }

  @Test(priority = 11, enabled = false)
  public void shouldDisplayCleanRepositoryAfterCommit() {
    assertFalse(gitPanel.isRepositoryClean(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME));
    projectExplorer.openAndWait();

    commitAllChangesInProject(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME, "Some changes");

    gitPanel.openPanel();
    gitPanel.waitRepositoryToBeClean(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME);
  }

  @Test(priority = 12, enabled = false)
  public void shouldUpdatePanelIfFilesChangedFromExternalSource() {
    projectExplorer.openAndWait();

    // change two files from editor and checkout with force from terminal
    editFile(JAVA_SPRING_EDITED_FILE1_NAME);
    editFile(JAVA_SPRING_EDITED_FILE2_NAME);

    gitPanel.openPanel();
    assertEquals(gitPanel.countRepositories(), 2);
    gitPanel.waitRepositoryToHaveChanges(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME, 2);

    terminal.selectTerminalTab();
    terminal.sendCommandIntoTerminal("cd /projects/" + RENAMED_JAVA_SPRING_GIT_PROJECT_NAME);
    terminal.sendCommandIntoTerminal("git checkout -f HEAD");

    assertEquals(gitPanel.countRepositories(), 2);
    gitPanel.waitRepositoryToBeClean(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME);
    gitPanel.waitFileGoneInChangesList(JAVA_SPRING_EDITED_FILE1_NAME);
    gitPanel.waitFileGoneInChangesList(JAVA_SPRING_EDITED_FILE2_NAME);

    // change a file from terminal
    terminal.sendCommandIntoTerminal("echo \"New content\" > " + JAVA_SPRING_ADDED_FILE_NAME);

    assertEquals(gitPanel.countRepositories(), 2);
    gitPanel.waitRepositoryToHaveChanges(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME, 1);
    gitPanel.waitFileInChangesList(JAVA_SPRING_ADDED_FILE_NAME);
  }

  @Test(priority = 13, enabled = false)
  public void shouldOpenGitPanelWithHotKey() {
    assertEquals(gitPanel.countRepositories(), 2);

    projectExplorer.openAndWait();
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitProjectExplorer();
    seleniumWebDriver.findElement(By.id("gwt-debug-projectTree")).sendKeys(Keys.ALT + "g");
    gitPanel.waitRepositories(2);
    gitPanel.waitRepositoryToBeClean(NODE_JS_GIT_PROJECT_NAME);
    gitPanel.waitRepositoryToHaveChanges(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME, 1);

    projectExplorer.openAndWait();
    projectExplorer.waitProjectExplorer();
    seleniumWebDriver.findElement(By.id("gwt-debug-projectTree")).sendKeys(Keys.ALT + "g");
    gitPanel.waitRepositories(2);
    gitPanel.waitRepositoryToBeClean(NODE_JS_GIT_PROJECT_NAME);
    gitPanel.waitRepositoryToHaveChanges(RENAMED_JAVA_SPRING_GIT_PROJECT_NAME, 1);
  }

  /** Creates git repository for non-under-git project */
  private void createGitRepositoryInProject(String projectName) {
    projectExplorer.waitAndSelectItemByName(projectName);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY);
    askDialog.waitFormToOpen();
    askDialog.clickOkBtn();
    askDialog.waitFormToClose();
  }

  /** Commits all changes with given message for specified project. */
  private void commitAllChangesInProject(String projectName, String message) {
    projectExplorer.waitAndSelectItemByName(projectName);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.COMMIT);
    git.waitAndRunCommit(message);
  }

  /** Opens given file by name and inserts a space into beginning of the file. */
  private void editFile(String visibleFileName) {
    projectExplorer.openItemByVisibleNameInExplorer(visibleFileName);
    editor.waitActiveTabFileName(toEditorTabName(visibleFileName));
    editor.typeTextIntoEditor(" ");
  }

  private void deleteFile(String visibleFileName) {
    projectExplorer.waitAndSelectItemByName(visibleFileName);
    menu.runCommand(TestMenuCommandsConstants.Edit.EDIT, TestMenuCommandsConstants.Edit.DELETE);
    askDialog.acceptDialogWithText("Delete file \"" + visibleFileName + "\"?");
  }

  private void createFileAndAddToIndex(String projectName, String fileName) {
    // create file
    projectExplorer.waitAndSelectItem(projectName);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.New.NEW,
        TestMenuCommandsConstants.Project.New.FILE);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText(fileName);
    askForValueDialog.clickOkBtn();

    // add the file into index
    projectExplorer.waitAndSelectItemByName(fileName);
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.waitAddToIndexFormToOpen();
    git.waitAddToIndexFileName("Add file " + fileName + " to index?");
    git.confirmAddToIndexForm();
  }

  private void createProject(String projectName, String sampleName) {
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.waitCreateProjectWizardForm();
    wizard.typeProjectNameOnWizard(projectName);
    wizard.selectSample(sampleName);
    wizard.clickCreateButton();
    loader.waitOnClosed();
    wizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
  }

  /**
   * Converts file name to editor tab title for java projects.
   *
   * @param filename name of file
   * @return tab title
   */
  private String toEditorTabName(String filename) {
    if (filename.endsWith(".java")) {
      return filename.substring(0, filename.length() - 5);
    }
    return filename;
  }
}
