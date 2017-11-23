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
package org.eclipse.che.selenium.filewatcher;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckFileWatcherExcludeFeatureTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String FILE_NAME_FOR_EXCLUDE = "README.md";
  private static final String FOLDER_NAME_FOR_EXCLUDE = "src";
  private static final String FILE_WATCHER_IGNORE_FILE_NAME = "fileWatcherIgnore";
  private static final String PATH_FOR_EXCLUDED_FILE = PROJECT_NAME + "/" + FILE_NAME_FOR_EXCLUDE;

  @Inject private TestWorkspace testWorkspace;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Events events;
  @Inject private Menu menu;
  @Inject private CodenvyEditor editor;
  @Inject private MachineTerminal terminal;
  @Inject private Loader loader;

  @BeforeClass
  public void setUp() throws Exception {
    projectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(getClass().getResource("/projects/guess-project").toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);
  }

  @Test
  public void checkAddRemoveFileFromFileWatcher() throws Exception {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();

    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.SHOW_HIDE_HIDDEN_FILES);
    projectExplorer.waitItemInVisibleArea(".che");
    projectExplorer.openItemByVisibleNameInExplorer(".che");
    projectExplorer.waitItemIsNotPresentVisibleArea(FILE_WATCHER_IGNORE_FILE_NAME);

    doFileWatcherExcludeOperation(
        PATH_FOR_EXCLUDED_FILE,
        ProjectExplorer.FileWatcherExcludeOperations.ADD_TO_FILE_WATCHER_EXCLUDES);
    projectExplorer.waitItemInVisibleArea(FILE_WATCHER_IGNORE_FILE_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(FILE_NAME_FOR_EXCLUDE);

    doFileWatcherExcludeOperation(
        PATH_FOR_EXCLUDED_FILE,
        ProjectExplorer.FileWatcherExcludeOperations.REMOVE_FROM_FILE_WATCHER_EXCLUDES);
    editor.selectTabByName(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.waitTextNotPresentIntoEditor(FILE_NAME_FOR_EXCLUDE);
  }

  @Test(priority = 1)
  public void checkFileWatcherFeatureBeforeExcluding() {
    projectExplorer.openItemByVisibleNameInExplorer(FILE_NAME_FOR_EXCLUDE);
    editor.waitActiveEditor();

    terminal.selectTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitTerminalIsNotEmpty();
    terminal.typeIntoTerminal("cd /projects/" + PROJECT_NAME + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal("/projects/" + PROJECT_NAME);
    terminal.typeIntoTerminal("df > " + FILE_NAME_FOR_EXCLUDE + Keys.ENTER);

    editor.selectTabByName(FILE_NAME_FOR_EXCLUDE);
    editor.waitTextIntoEditor("Filesystem");
  }

  @Test(priority = 2)
  public void checkFileInEditorAfterExluding() {
    doFileWatcherExcludeOperation(
        PATH_FOR_EXCLUDED_FILE,
        ProjectExplorer.FileWatcherExcludeOperations.ADD_TO_FILE_WATCHER_EXCLUDES);
    editor.selectTabByName(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.waitTextIntoEditor(FILE_NAME_FOR_EXCLUDE);

    terminal.selectTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitTerminalIsNotEmpty();
    terminal.typeIntoTerminal("cd /projects/" + PROJECT_NAME + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal("/projects/" + PROJECT_NAME);
    terminal.typeIntoTerminal("pwd > " + FILE_NAME_FOR_EXCLUDE + Keys.ENTER);

    editor.selectTabByName(FILE_NAME_FOR_EXCLUDE);
    editor.waitTextNotPresentIntoEditor("/projects/" + PROJECT_NAME);
    editor.clickOnCloseFileIcon(FILE_NAME_FOR_EXCLUDE);
    projectExplorer.openItemByVisibleNameInExplorer(FILE_NAME_FOR_EXCLUDE);
    editor.waitActiveEditor();
    editor.waitTextIntoEditor("/projects/" + PROJECT_NAME);

    doFileWatcherExcludeOperation(
        PATH_FOR_EXCLUDED_FILE,
        ProjectExplorer.FileWatcherExcludeOperations.REMOVE_FROM_FILE_WATCHER_EXCLUDES);
    editor.selectTabByName(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.waitTextNotPresentIntoEditor(FILE_NAME_FOR_EXCLUDE);
  }

  @Test(priority = 3)
  public void checkFeatureAfterExcludingFolder() {
    doFileWatcherExcludeOperation(
        PROJECT_NAME + "/" + FOLDER_NAME_FOR_EXCLUDE,
        ProjectExplorer.FileWatcherExcludeOperations.ADD_TO_FILE_WATCHER_EXCLUDES);
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitActiveEditor();

    terminal.selectTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitTerminalIsNotEmpty();
    terminal.typeIntoTerminal(
        "cd /projects/" + PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal(
        "/projects/" + PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    terminal.typeIntoTerminal("pwd > " + "AppController.java" + Keys.ENTER);

    editor.selectTabByName("AppController");
    editor.waitTextNotPresentIntoEditor(
        "/projects/" + PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    editor.clickOnCloseFileIcon("AppController");
    projectExplorer.openItemByVisibleNameInExplorer("AppController.java");
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(
        "/projects/" + PROJECT_NAME + "/src/main/java/org/eclipse/qa/examples");
    doFileWatcherExcludeOperation(
        PROJECT_NAME + "/" + FOLDER_NAME_FOR_EXCLUDE,
        ProjectExplorer.FileWatcherExcludeOperations.REMOVE_FROM_FILE_WATCHER_EXCLUDES);
  }

  @Test(priority = 4)
  public void checkFileWatcherExcludeFeatureAfterChangingIgnoreFile() {
    doFileWatcherExcludeOperation(
        PATH_FOR_EXCLUDED_FILE,
        ProjectExplorer.FileWatcherExcludeOperations.ADD_TO_FILE_WATCHER_EXCLUDES);
    editor.selectTabByName(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.deleteAllContent();
    editor.typeTextIntoEditor(FILE_NAME_FOR_EXCLUDE);
    doFileWatcherExcludeOperation(
        PATH_FOR_EXCLUDED_FILE,
        ProjectExplorer.FileWatcherExcludeOperations.REMOVE_FROM_FILE_WATCHER_EXCLUDES);
  }

  private void doFileWatcherExcludeOperation(String itemName, String typeOfOperation) {
    projectExplorer.selectItem(itemName);
    projectExplorer.openContextMenuByPathSelectedItem(itemName);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(typeOfOperation);
    loader.waitOnClosed();
  }
}
