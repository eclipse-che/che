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

import static java.lang.String.format;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FileWatcherExcludeOperations.ADD_TO_FILE_WATCHER_EXCLUDES;
import static org.eclipse.che.selenium.pageobject.ProjectExplorer.FileWatcherExcludeOperations.REMOVE_FROM_FILE_WATCHER_EXCLUDES;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckFileWatcherExcludeFeatureTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String FOLDER_NAME_FOR_EXCLUDE = "src";
  private static final String FILE_WATCHER_IGNORE_FILE_NAME = "fileWatcherIgnore";

  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestWorkspace testWorkspace;
  @Inject private MachineTerminal terminal;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles consoles;
  @Inject private Loader loader;
  @Inject private Events events;
  @Inject private Menu menu;
  @Inject private Ide ide;

  @BeforeClass
  public void setUp() throws Exception {
    projectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(getClass().getResource("/projects/guess-project").toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(testWorkspace);

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
  }

  @Test
  public void testAddRemoveFileFromFileWatcherExcludeFeature() throws Exception {
    String fileNameForExcluding = "pom.xml";
    String pathToExcludedFile = PROJECT_NAME + "/" + fileNameForExcluding;

    projectExplorer.waitItem(PROJECT_NAME);
    doFileWatcherExcludeOperation(pathToExcludedFile, ADD_TO_FILE_WATCHER_EXCLUDES);
    doFileWatcherExcludeOperation(pathToExcludedFile, REMOVE_FROM_FILE_WATCHER_EXCLUDES);

    // Refresh web page(for checking that the File Watcher Exclude feature works after refresh)
    seleniumWebDriver.navigate().refresh();
    projectExplorer.waitItem(PROJECT_NAME);

    // Exclude file and check that its name is in the 'fileWatcherIgnore' file
    doFileWatcherExcludeOperation(pathToExcludedFile, ADD_TO_FILE_WATCHER_EXCLUDES);
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.SHOW_HIDE_HIDDEN_FILES);
    projectExplorer.waitItemInVisibleArea(".che");
    projectExplorer.openItemByVisibleNameInExplorer(".che");
    projectExplorer.waitItemInVisibleArea(FILE_WATCHER_IGNORE_FILE_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(fileNameForExcluding);

    // Remove the file from the File Watcher Excludes feature and check that the file name is not
    // exists in the 'fileWatcherIgnore' file
    doFileWatcherExcludeOperation(pathToExcludedFile, REMOVE_FROM_FILE_WATCHER_EXCLUDES);
    editor.clickOnCloseFileIcon(FILE_WATCHER_IGNORE_FILE_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.selectTabByName(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.waitTextNotPresentIntoEditor(fileNameForExcluding);

    editor.closeAllTabsByContextMenu();
  }

  @Test
  public void testFeatureAfterExcludingFile() {
    String fileNameForExluding = "README.md";
    String pathToExcludedFile = PROJECT_NAME + "/" + fileNameForExluding;

    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(fileNameForExluding);
    editor.waitActiveEditor();

    // Check that changes with file in the Terminal appeared in the Editor
    terminal.selectTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitTerminalIsNotEmpty();
    terminal.typeIntoTerminal(format("cd /projects/%s%s", PROJECT_NAME, ENTER));
    terminal.waitExpectedTextIntoTerminal("/projects/" + PROJECT_NAME);
    terminal.typeIntoTerminal(format("df > %s%s", fileNameForExluding, ENTER));
    editor.selectTabByName(fileNameForExluding);
    editor.waitTextIntoEditor("Filesystem");

    // Add the file to exclude and check that the file content in the Editor is not changed
    doFileWatcherExcludeOperation(pathToExcludedFile, ADD_TO_FILE_WATCHER_EXCLUDES);

    terminal.selectTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitTerminalIsNotEmpty();
    terminal.typeIntoTerminal(format("cd /projects/%s%s", PROJECT_NAME, ENTER));
    terminal.waitExpectedTextIntoTerminal("/projects/" + PROJECT_NAME);
    terminal.typeIntoTerminal(format("pwd > %s%s", fileNameForExluding, ENTER));
    editor.selectTabByName(fileNameForExluding);
    editor.waitTextNotPresentIntoEditor("/projects/" + PROJECT_NAME);

    // Close file and open again and check that the file content is changed
    editor.clickOnCloseFileIcon(fileNameForExluding);
    projectExplorer.openItemByVisibleNameInExplorer(fileNameForExluding);
    editor.waitActiveEditor();
    editor.waitTextIntoEditor("/projects/" + PROJECT_NAME);
    doFileWatcherExcludeOperation(pathToExcludedFile, REMOVE_FROM_FILE_WATCHER_EXCLUDES);

    editor.closeAllTabsByContextMenu();
  }

  @Test
  public void testFeatureAfterExcludingFolder() {
    String fileNameForExcluding = "AppController.java";
    String pathToJavaFile =
        format("/projects/%s/src/main/java/org/eclipse/qa/examples", PROJECT_NAME);

    // Exclude 'src' folder
    doFileWatcherExcludeOperation(
        PROJECT_NAME + "/" + FOLDER_NAME_FOR_EXCLUDE, ADD_TO_FILE_WATCHER_EXCLUDES);

    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(fileNameForExcluding);
    editor.waitActiveEditor();

    // Change content of file from the excluded folder
    terminal.selectTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitTerminalIsNotEmpty();
    terminal.typeIntoTerminal(format("cd %s%s", pathToJavaFile, ENTER));
    terminal.waitExpectedTextIntoTerminal(pathToJavaFile);
    terminal.typeIntoTerminal(
        format(
            "pwd | cat - %s > temp && mv temp %s%s",
            fileNameForExcluding, fileNameForExcluding, ENTER));

    // Check that content in the excluded file is not changed in the Editor
    editor.selectTabByName("AppController");
    editor.waitTextNotPresentIntoEditor(pathToJavaFile);

    // Reopen the file and check that its content changed
    editor.clickOnCloseFileIcon("AppController");
    projectExplorer.openItemByVisibleNameInExplorer(fileNameForExcluding);
    editor.waitActiveEditor();
    editor.waitTextIntoEditor(pathToJavaFile);
    doFileWatcherExcludeOperation(
        PROJECT_NAME + "/" + FOLDER_NAME_FOR_EXCLUDE, REMOVE_FROM_FILE_WATCHER_EXCLUDES);

    editor.closeAllTabsByContextMenu();
  }

  @Test
  public void testIsNotExcludeOperationEventAboutIgnoreFile() {
    projectExplorer.waitItem(PROJECT_NAME);

    // Clear event log
    events.clickEventLogBtn();
    events.clearAllMessages();
    consoles.clickOnProcessesButton();

    doFileWatcherExcludeOperation(PROJECT_NAME, ADD_TO_FILE_WATCHER_EXCLUDES);
    doFileWatcherExcludeOperation(PROJECT_NAME, REMOVE_FROM_FILE_WATCHER_EXCLUDES);

    // Check in the Events that there are not "File 'fileWatcherIgnore' is updated" messages
    events.clickEventLogBtn();
    events.waitMessageIsNotPresent("File 'fileWatcherIgnore' is updated");
    consoles.clickOnProcessesButton();
  }

  private void doFileWatcherExcludeOperation(String itemName, String typeOfOperation) {
    projectExplorer.selectItem(itemName);
    projectExplorer.openContextMenuByPathSelectedItem(itemName);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(typeOfOperation);
    loader.waitOnClosed();
  }
}
