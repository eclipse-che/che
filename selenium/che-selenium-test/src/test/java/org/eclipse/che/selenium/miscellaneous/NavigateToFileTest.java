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
package org.eclipse.che.selenium.miscellaneous;

import static org.testng.AssertJUnit.assertFalse;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestGitConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NavigateToFile;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Created by aleksandr shmaraev on 10.12.15 */
public class NavigateToFileTest {
  private static final String PROJECT_NAME = "NavigateFile";
  private static final String PROJECT_NAME_2 = "NavigateFile_2";
  private static final String PATH_TO_JAVA_FILE =
      "(/NavigateFile/src/main/java/org/eclipse/qa/examples)";
  private static final String PATH_TO_JSP_FILE = "(/NavigateFile/src/main/webapp)";
  private static final String PATH_TO_README_FILE = "(/NavigateFile)";
  private static final String PATH_2_TO_JAVA_FILE =
      "(/NavigateFile_2/src/main/java/org/eclipse/qa/examples)";
  private static final String PATH_2_TO_JSP_FILE = "(/NavigateFile_2/src/main/webapp)";
  private static final String PATH_2_TO_README_FILE = "(/NavigateFile_2)";
  private static final String FILE_JAVA = "AppController.java";
  private static final String FILE_XML = "pom.xml";
  private static final String FILE_README = "README.md";
  private static final String FILE_JSP = "index.jsp";
  private static final String FILE_CREATED_FROM_API = "createdFrom.api";
  private static final String FILE_CREATED_FROM_CONSOLE = "createdFrom.con";

  private static final List<String> FILES_A_SYMBOL =
      Arrays.asList(
          "AppController.java (/NavigateFile/src/main/java/org/eclipse/qa/examples)",
          "AppController.java (/NavigateFile_2/src/main/java/org/eclipse/qa/examples)");
  private static final List<String> FILES_P_SYMBOL =
      Arrays.asList("pom.xml (/NavigateFile_2)", "pom.xml (/NavigateFile)");
  private static final List<String> FILES_I_SYMBOL =
      Arrays.asList(
          "index.jsp (/NavigateFile/src/main/webapp)",
          "index.jsp (/NavigateFile_2/src/main/webapp)");
  private static final List<String> FILES_R_SYMBOL =
      Arrays.asList("README.md (/NavigateFile)", "README.md (/NavigateFile_2)");
  private static final List<String> FILES_C_SYMBOL =
      Arrays.asList(
          "classpath (/NavigateFile_2/.che)", "classpath (/NavigateFile/.che)",
          "createdFrom.con (/NavigateFile_2)", "createdFrom.api (/NavigateFile)");

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private MachineTerminal terminal;
  @Inject private CodenvyEditor editor;
  @Inject private NavigateToFile navigateToFile;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Git git;
  @Inject private AskDialog askDialog;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SIMPLE);

    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME_2,
        ProjectTemplates.MAVEN_SIMPLE);
    ide.open(workspace);
  }

  @Test
  public void checkNavigateToFileFunction() {
    // Open the project one and check function 'Navigate To File'
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    selectFileFromNavigate("A", FILE_JAVA + PATH_TO_JAVA_FILE, FILES_A_SYMBOL);
    editor.waitTabIsPresent("AppController");
    editor.waitActiveEditor();
    editor.closeFileByNameWithSaving("AppController");
    editor.waitWhileFileIsClosed("AppController");
    selectFileFromNavigate("p", FILE_XML + PATH_TO_README_FILE, FILES_P_SYMBOL);
    editor.waitTabIsPresent("qa-spring-sample");
    editor.waitActiveEditor();
    editor.closeFileByNameWithSaving("qa-spring-sample");
    editor.waitWhileFileIsClosed("qa-spring-sample");
    selectFileFromNavigate("i", FILE_JSP + PATH_TO_JSP_FILE);
    editor.waitTabIsPresent("index.jsp");
    editor.waitActiveEditor();
    editor.closeFileByNameWithSaving("index.jsp");
    editor.waitWhileFileIsClosed("index.jsp");
    selectFileFromNavigateLaunchByKeyboard("R", FILE_README + PATH_TO_README_FILE);
    editor.waitTabIsPresent("README.md");
    editor.waitActiveEditor();
    editor.closeFileByNameWithSaving("README.md");
    editor.waitWhileFileIsClosed("README.md");
    loader.waitOnClosed();

    // Open the project two and check function 'Navigate To File'
    projectExplorer.waitItem(PROJECT_NAME_2);
    projectExplorer.openItemByPath(PROJECT_NAME_2);
    selectFileFromNavigate("A", FILE_JAVA + PATH_2_TO_JAVA_FILE, FILES_A_SYMBOL);
    editor.waitTabIsPresent("AppController");
    editor.waitActiveEditor();
    selectFileFromNavigate("p", FILE_XML + PATH_2_TO_README_FILE, FILES_P_SYMBOL);
    editor.waitTabIsPresent("qa-spring-sample");
    editor.waitActiveEditor();
    selectFileFromNavigate("i", FILE_JSP + PATH_2_TO_JSP_FILE);
    editor.waitTabIsPresent("index.jsp");
    editor.waitActiveEditor();
    selectFileFromNavigateLaunchByKeyboard("R", FILE_README + PATH_2_TO_README_FILE);
    editor.waitTabIsPresent("README.md");
    editor.waitActiveEditor();
    editor.closeAllTabsByContextMenu();

    // Check that form is closed by pressing ESC button
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.NAVIGATE_TO_FILE);
    navigateToFile.waitFormToOpen();
    navigateToFile.closeNavigateToFileForm();
    navigateToFile.waitFormToClose();
  }

  @Test
  public void checkNavigateToFileFunctionWithJustCreatedFiles() throws Exception {
    String content = "NavigateToFileTest";

    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    createFileFromAPI(PROJECT_NAME, FILE_CREATED_FROM_API, content);
    terminal.waitTerminalTab();
    terminal.selectTerminalTab();
    createFileInTerminal(PROJECT_NAME_2, FILE_CREATED_FROM_CONSOLE);
    WaitUtils.sleepQuietly(10);
    selectFileFromNavigate("c", FILE_CREATED_FROM_API + PATH_TO_README_FILE, FILES_C_SYMBOL);
    editor.waitTabIsPresent(FILE_CREATED_FROM_API);
    editor.waitActiveEditor();
    editor.closeFileByNameWithSaving(FILE_CREATED_FROM_API);
    selectFileFromNavigate("c", FILE_CREATED_FROM_CONSOLE + PATH_2_TO_README_FILE, FILES_C_SYMBOL);
    editor.waitTabIsPresent(FILE_CREATED_FROM_CONSOLE);
    editor.waitActiveEditor();
    editor.closeFileByNameWithSaving(FILE_CREATED_FROM_CONSOLE);
  }

  @Test
  public void checkNavigateToFileFunctionWithFilesFromHiddenFolders() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.selectItem(PROJECT_NAME);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY);
    askDialog.acceptDialogWithText(
        "Do you want to initialize the local repository " + PROJECT_NAME + "?");
    loader.waitOnClosed();
    git.waitGitStatusBarWithMess(TestGitConstants.GIT_INITIALIZED_SUCCESS);

    // check that HEAD file from .git folder is not appear in list of found files
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.NAVIGATE_TO_FILE);
    navigateToFile.waitFormToOpen();
    navigateToFile.typeSymbolInFileNameField("H");
    loader.waitOnClosed();
    navigateToFile.waitFileNamePopUp();
    assertFalse(navigateToFile.isFilenameSuggested("HEAD (/NavigateFile/.git)"));
    navigateToFile.closeNavigateToFileForm();
    navigateToFile.waitFormToClose();
  }

  private void selectFileFromNavigate(String symbol, String pathName, List<String> files) {
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.NAVIGATE_TO_FILE);
    navigateToFile.waitFormToOpen();
    loader.waitOnClosed();
    navigateToFile.typeSymbolInFileNameField(symbol);
    loader.waitOnClosed();
    navigateToFile.waitFileNamePopUp();
    for (String listFiles : files) {
      navigateToFile.waitListOfFilesNames(listFiles);
    }
    navigateToFile.selectFileByFullName(pathName);
    navigateToFile.waitFormToClose();
  }

  private void selectFileFromNavigate(String symbol, String pathName) {
    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.NAVIGATE_TO_FILE);
    navigateToFile.waitFormToOpen();
    loader.waitOnClosed();
    navigateToFile.typeSymbolInFileNameField(symbol);
    loader.waitOnClosed();
    navigateToFile.waitFileNamePopUp();
    for (String listFiles : FILES_I_SYMBOL) {
      navigateToFile.waitListOfFilesNames(listFiles);
    }
    navigateToFile.selectFileByFullName(pathName);
    navigateToFile.waitFormToClose();
  }

  private void selectFileFromNavigateLaunchByKeyboard(String symbol, String pathName) {
    loader.waitOnClosed();
    navigateToFile.launchNavigateToFileByKeyboard();
    navigateToFile.waitFormToOpen();
    loader.waitOnClosed();
    navigateToFile.typeSymbolInFileNameField(symbol);
    loader.waitOnClosed();
    navigateToFile.waitFileNamePopUp();
    for (String listFiles : FILES_R_SYMBOL) {
      navigateToFile.waitListOfFilesNames(listFiles);
    }
    navigateToFile.selectFileByFullName(pathName);
    navigateToFile.waitFormToClose();
  }

  private void createFileFromAPI(String path, String fileName, String content) throws Exception {
    testProjectServiceClient.createFileInProject(workspace.getId(), path, fileName, content);
  }

  private void createFileInTerminal(String projectName, String fileName) {
    terminal.typeIntoTerminal("cd " + projectName + Keys.ENTER);
    terminal.typeIntoTerminal("ls -als > " + fileName + Keys.ENTER);
    terminal.typeIntoTerminal("cat " + fileName + Keys.ENTER);
    terminal.typeIntoTerminal("ls" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal(fileName);
  }
}
