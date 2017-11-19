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

import static java.lang.Math.random;
import static org.testng.AssertJUnit.assertFalse;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestCommandsConstants;
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
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Created by aleksandr shmaraev on 10.12.15 */
public class NavigateToFileTest {
  private static final String PROJECT_NAME = "NavigateFile";
  private static final String PROJECT_NAME_2 = "NavigateFile_2";
  private static final String FILE_CREATED_FROM_CONSOLE = "createdFrom.con";
  private static final String COMMAND_FOR_FILE_CREATION = "create-file";

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
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;

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
    testCommandServiceClient.createCommand(
        "cd " + "/projects/" + PROJECT_NAME_2 + "&& touch " + FILE_CREATED_FROM_CONSOLE,
        COMMAND_FOR_FILE_CREATION,
        TestCommandsConstants.CUSTOM,
        workspace.getId());
    ide.open(workspace);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME_2);
  }

  @Test(dataProvider = "dataForSearching")
  public void checkNavigateToFileWitFirstProject(
      String inputValueForChecking, Map<Integer, String> expectedValues) {
    // Open the project one and check function 'Navigate To File'
    launchNavigateToFileAndCheckResults(inputValueForChecking, expectedValues, 1);
  }

  @Test(dataProvider = "dataForSearching")
  public void checkNavigateToFileWitSecondProject(
      String inputValueForChecking, Map<Integer, String> expectedValues) {
    launchNavigateToFileAndCheckResults(inputValueForChecking, expectedValues, 2);
  }

  @Test(dataProvider = "dataForCheckingFilesCreatedWithoutIDE")
  public void checkNavigateToFileFunctionWithJustCreatedFiles(
      String inputValueForChecking, Map<Integer, String> expectedValues) throws Exception {
    final int maxTimeoutForUpdatingIndexes = 10;
    String content = "NavigateToFileTest";
    testProjectServiceClient.createFileInProject(
        workspace.getId(), PROJECT_NAME, expectedValues.get(1).split(" ")[0], content);
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(COMMAND_FOR_FILE_CREATION);
    WaitUtils.sleepQuietly(maxTimeoutForUpdatingIndexes);
    int randomItemFromList = (int) random() * 2 + 1;
    launchNavigateToFileAndCheckResults(inputValueForChecking, expectedValues, randomItemFromList);
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

  private void launchNavigateToFileAndCheckResults(
      String navigatingValue,
      Map<Integer, String> expectedItems,
      final int numValueFromDropDawnList) {

    // extract the path (without opened class)
    String pathFromDropDawnForChecking = expectedItems.get(numValueFromDropDawnList).split(" ")[1];

    String nameOfTheOpenedFileWithExtension =
        expectedItems.get(numValueFromDropDawnList).split(" ")[0];

    // extract the name of opened files that display in a tab (the ".java" extension are not shown
    // in tabs)
    String nameOfTheOpenedFileInTheTab = nameOfTheOpenedFileWithExtension.replace(".java", "");

    loader.waitOnClosed();
    menu.runCommand(
        TestMenuCommandsConstants.Assistant.ASSISTANT,
        TestMenuCommandsConstants.Assistant.NAVIGATE_TO_FILE);
    navigateToFile.waitFormToOpen();
    loader.waitOnClosed();
    navigateToFile.typeSymbolInFileNameField(navigatingValue);
    loader.waitOnClosed();
    waitExpectedItemsInNavigateToFileDropdawn(expectedItems);
    navigateToFile.selectFileByName(pathFromDropDawnForChecking);
    editor.waitActiveEditor();
    editor.getAssociatedPathFromTheTab(nameOfTheOpenedFileInTheTab);
    editor.closeFileByNameWithSaving(nameOfTheOpenedFileInTheTab);
  }

  private void waitExpectedItemsInNavigateToFileDropdawn(Map<Integer, String> expectedItems) {
    expectedItems.forEach((k, v) -> navigateToFile.waitListOfFilesNames(v.toString()));
  }


  @DataProvider
  private Object[][] dataForCheckingTheSameFileInDifferentProjects() {
    return new Object[][] {
      {
        "A",
        ImmutableMap.of(
            1, "AppController.java (/NavigateFile/src/main/java/org/eclipse/qa/examples)",
            2, "AppController.java (/NavigateFile_2/src/main/java/org/eclipse/qa/examples)")
      },
      {
        "i",
        ImmutableMap.of(
            1, "index.jsp (/NavigateFile/src/main/webapp)",
            2, "index.jsp (/NavigateFile_2/src/main/webapp)")
      },
      {
        "R",
        ImmutableMap.of(
            1, "README.md (/NavigateFile)",
            2, "README.md (/NavigateFile_2)")
      }
    };
  }

  @DataProvider
  private Object[][] dataForCheckingFilesCreatedWithoutIDE() {
    return new Object[][] {
      {
        "c",
        ImmutableMap.of(
            1, "createdFrom.api (/NavigateFile)", 2, "createdFrom.con (/NavigateFile_2)")
      }
    };
  }
}
