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
package org.eclipse.che.selenium.filewatcher;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.ADD_TO_FILE_WATCHER_EXCLUDES;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuFirstLevelItems.REMOVE_FROM_FILE_WATCHER_EXCLUDES;

import com.google.inject.Inject;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuItems;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CheckFileWatcherExcludeFeatureTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final String FOLDER_NAME_FOR_EXCLUDE = "src";
  private static final String FILE_WATCHER_IGNORE_FILE_NAME = "fileWatcherIgnore";
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestProjectServiceClient projectServiceClient;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestWorkspace testWorkspace;
  @Inject private CheTerminal terminal;
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
    // We enable hidden files because in the test we use file Watcher Ignore, which is hidden.
    menu.runCommand(
        TestMenuCommandsConstants.Project.PROJECT,
        TestMenuCommandsConstants.Project.SHOW_HIDE_HIDDEN_FILES);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    doFileWatcherExcludeOperation(PROJECT_NAME + "/" + "pom.xml", ADD_TO_FILE_WATCHER_EXCLUDES);
    projectExplorer.clickOnRefreshTreeButton();
  }

  @AfterMethod
  public void closeOpenedTabs() {
    editor.closeAllTabs();
  }

  @Test
  public void checkFileWatcherIgnoreFileAfterIncludingAndExcludingFileWatching() throws Exception {
    String fileNameForExcluding = "pom.xml";
    String pathToExcludedFile = PROJECT_NAME + "/" + fileNameForExcluding;
    projectExplorer.waitVisibilityByName(FILE_WATCHER_IGNORE_FILE_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.waitActive();
    editor.waitTextIntoEditor(fileNameForExcluding);

    // Remove the file from the File Watcher Excludes feature and check that the file name is not
    // exists in the 'fileWatcherIgnore' file
    doFileWatcherExcludeOperation(pathToExcludedFile, REMOVE_FROM_FILE_WATCHER_EXCLUDES);
    editor.clickOnCloseFileIcon(FILE_WATCHER_IGNORE_FILE_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.selectTabByName(FILE_WATCHER_IGNORE_FILE_NAME);
    editor.waitTextNotPresentIntoEditor(fileNameForExcluding);
  }

  @Test
  public void testFeatureAfterExcludingFile() throws Exception {
    String fileNameForExluding = "README.md";
    String pathToExcludedFile = PROJECT_NAME + "/" + fileNameForExluding;
    String currentTimeInMillis = Long.toString(System.currentTimeMillis());
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(fileNameForExluding);
    editor.waitActive();

    // Add the file to exclude and check that the file content in the Editor is not changed
    doFileWatcherExcludeOperation(pathToExcludedFile, ADD_TO_FILE_WATCHER_EXCLUDES);
    testProjectServiceClient.updateFile(
        testWorkspace.getId(), pathToExcludedFile, currentTimeInMillis);
    editor.waitTextNotPresentIntoEditor(currentTimeInMillis);

    // Close file and open again and check that the file content is changed
    editor.clickOnCloseFileIcon(fileNameForExluding);
    projectExplorer.openItemByVisibleNameInExplorer(fileNameForExluding);
    editor.waitActive();
    editor.waitTextIntoEditor(currentTimeInMillis);
  }

  @Test
  public void testFeatureAfterExcludingFolder() throws Exception {
    String fileNameForExcluding = "AppController.java";
    String pathToJavaFile =
        format("%s/src/main/java/org/eclipse/qa/examples/AppController.java", PROJECT_NAME);
    String currentTimeInMillis = Long.toString(System.currentTimeMillis());
    // Exclude 'src' folder
    doFileWatcherExcludeOperation(
        PROJECT_NAME + "/" + FOLDER_NAME_FOR_EXCLUDE, ADD_TO_FILE_WATCHER_EXCLUDES);

    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByVisibleNameInExplorer(fileNameForExcluding);
    editor.waitActive();

    testProjectServiceClient.updateFile(testWorkspace.getId(), pathToJavaFile, currentTimeInMillis);
    editor.selectTabByName("AppController");
    editor.waitTextNotPresentIntoEditor(currentTimeInMillis);

    // Reopen the file and check that its content changed
    editor.clickOnCloseFileIcon("AppController");
    projectExplorer.openItemByVisibleNameInExplorer(fileNameForExcluding);
    editor.waitActive();
    editor.waitTextIntoEditor(currentTimeInMillis);
  }

  @Test
  public void testIsNotExcludeOperationEventAboutIgnoreFile() {
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/" + "README.md");
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

  private void doFileWatcherExcludeOperation(String itemName, ContextMenuItems typeOfOperation) {
    projectExplorer.waitAndSelectItem(itemName);
    projectExplorer.openContextMenuByPathSelectedItem(itemName);
    projectExplorer.waitContextMenu();
    projectExplorer.clickOnItemInContextMenu(typeOfOperation);
    loader.waitOnClosed();
    projectExplorer.waitContextMenuPopUpClosed();
  }
}
