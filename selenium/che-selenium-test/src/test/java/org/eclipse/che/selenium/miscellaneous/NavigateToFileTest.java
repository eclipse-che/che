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
package org.eclipse.che.selenium.miscellaneous;

import static java.lang.String.format;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CUSTOM;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.ASSISTANT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Assistant.NAVIGATE_TO_FILE;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SIMPLE;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.eclipse.che.selenium.core.client.TestCommandServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NavigateToFile;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.openqa.selenium.WebDriverException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Created by aleksandr shmaraev on 10.12.15 */
public class NavigateToFileTest {
  private static final String PROJECT_NAME = "NavigateFile";
  private static final String PROJECT_NAME_2 = "NavigateFile_2";
  private static final String FILE_CREATED_FROM_CONSOLE = "createdFrom.con";
  private static final String COMMAND_FOR_FILE_CREATION = "createFile";
  private static final String HIDDEN_FOLDER_NAME = ".hiddenFolder";
  private static final String HIDDEN_FILE_NAME = ".hiddenFile";
  private static final String FILE_IN_HIDDEN_FOLDER = "innerFile.css";
  private static final int MAX_TIMEOUT_FOR_UPDATING_INDEXES = 10;

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private NavigateToFile navigateToFile;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private TestCommandServiceClient testCommandServiceClient;
  @Inject private CommandsPalette commandsPalette;
  @Inject private Consoles consoles;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME, MAVEN_SIMPLE);

    testProjectServiceClient.importProject(
        workspace.getId(), Paths.get(resource.toURI()), PROJECT_NAME_2, MAVEN_SIMPLE);
    testCommandServiceClient.createCommand(
        format("touch /projects/%s/%s", PROJECT_NAME_2, FILE_CREATED_FROM_CONSOLE),
        COMMAND_FOR_FILE_CREATION,
        CUSTOM,
        workspace.getId());
    ide.open(workspace);
    consoles.waitJDTLSStartedMessage();
    ide.waitOpenedWorkspaceIsReadyToUse();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.waitItem(PROJECT_NAME_2);
  }

  @Test(dataProvider = "dataForCheckingTheSameFileInDifferentProjects")
  public void shouldNavigateToFileForFirstProject(
      String inputValueForChecking, Map<Integer, String> expectedValues) {
    // Open the project one and check function 'Navigate To File'
    launchNavigateToFileAndCheckResults(inputValueForChecking, expectedValues, 1);
  }

  @Test(dataProvider = "dataForCheckingTheSameFileInDifferentProjects")
  public void shouldDoNavigateToFileForSecondProject(
      String inputValueForChecking, Map<Integer, String> expectedValues) {
    launchNavigateToFileAndCheckResults(inputValueForChecking, expectedValues, 2);
  }

  @Test(dataProvider = "dataToNavigateToFileCreatedOutsideIDE")
  public void shouldNavigateToFileWithJustCreatedFiles(
      String inputValueForChecking, Map<Integer, String> expectedValues) throws Exception {

    String content = "NavigateToFileTest";
    testProjectServiceClient.createFileInProject(
        workspace.getId(), PROJECT_NAME, expectedValues.get(1).split(" ")[0], content);
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(COMMAND_FOR_FILE_CREATION);
    sleepQuietly(MAX_TIMEOUT_FOR_UPDATING_INDEXES);
    int randomItemFromList = ThreadLocalRandom.current().nextInt(1, 2);
    launchNavigateToFileAndCheckResults(inputValueForChecking, expectedValues, randomItemFromList);
  }

  @Test
  public void shouldNotDisplayHiddenFilesAndFoldersInDropDown() throws Exception {
    addHiddenFoldersAndFileThroughProjectService();
    launchNavigateToFileFromUIAndTypeValue(FILE_IN_HIDDEN_FOLDER);
    assertTrue(navigateToFile.getText().isEmpty());
    navigateToFile.closeNavigateToFileForm();
    launchNavigateToFileFromUIAndTypeValue(HIDDEN_FILE_NAME);
    assertTrue(navigateToFile.getText().isEmpty());
  }

  @Test(dataProvider = "dataToCheckNavigateByNameWithSpecialSymbols")
  public void shouldDisplayFilesFoundByMask(
      String inputValueForChecking, Map<Integer, String> expectedValues) {
    launchNavigateToFileFromUIAndTypeValue(inputValueForChecking);
    navigateToFile.waitSuggestedPanel();
    waitExpectedItemsInNavigateToFileDropdown(expectedValues);
    navigateToFile.closeNavigateToFileForm();
    navigateToFile.waitFormToClose();
  }

  private void addHiddenFoldersAndFileThroughProjectService() throws Exception {
    testProjectServiceClient.createFolder(
        workspace.getId(), PROJECT_NAME + "/" + HIDDEN_FOLDER_NAME);
    testProjectServiceClient.createFileInProject(
        workspace.getId(),
        PROJECT_NAME + "/" + HIDDEN_FOLDER_NAME,
        FILE_IN_HIDDEN_FOLDER,
        "contentFile1");
    testProjectServiceClient.createFileInProject(
        workspace.getId(), PROJECT_NAME_2 + "/", HIDDEN_FILE_NAME, "content-of-hidden-file");
    sleepQuietly(MAX_TIMEOUT_FOR_UPDATING_INDEXES);
  }

  private void launchNavigateToFileAndCheckResults(
      String navigatingValue,
      Map<Integer, String> expectedItems,
      final int numValueFromDropDawnList) {

    // extract the path (without opened class)
    String dropdownVerificationPath = expectedItems.get(numValueFromDropDawnList).split(" ")[1];
    String openedFileWithExtension = expectedItems.get(numValueFromDropDawnList).split(" ")[0];

    // extract the name of opened files that display in a tab (the ".java" extension are not shown
    // in tabs)
    String openedFileNameInTheTab = openedFileWithExtension.replace(".java", "");
    launchNavigateToFileFromUIAndTypeValue(navigatingValue);
    navigateToFile.waitSuggestedPanel();
    waitExpectedItemsInNavigateToFileDropdown(expectedItems);

    try {
      navigateToFile.selectFileByName(dropdownVerificationPath);
    } catch (WebDriverException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/8465", ex);
    }

    editor.waitActive();
    editor.getAssociatedPathFromTheTab(openedFileNameInTheTab);
    editor.closeFileByNameWithSaving(openedFileNameInTheTab);
  }

  private void launchNavigateToFileFromUIAndTypeValue(String navigatingValue) {
    loader.waitOnClosed();
    menu.runCommand(ASSISTANT, NAVIGATE_TO_FILE);
    navigateToFile.waitFormToOpen();
    loader.waitOnClosed();
    navigateToFile.typeSymbolInFileNameField(navigatingValue);
    loader.waitOnClosed();
  }

  private void waitExpectedItemsInNavigateToFileDropdown(Map<Integer, String> expectedItems) {
    expectedItems
        .values()
        .stream()
        .map(it -> it.toString())
        .forEach(it -> Assert.assertTrue(navigateToFile.isFilenameSuggested(it)));
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
        "R",
        ImmutableMap.of(
            1, "README.md (/NavigateFile)",
            2, "README.md (/NavigateFile_2)")
      }
    };
  }

  @DataProvider
  private Object[][] dataToNavigateToFileCreatedOutsideIDE() {
    return new Object[][] {
      {
        "c",
        ImmutableMap.of(
            1, "createdFrom.api (/NavigateFile)", 2, "createdFrom.con (/NavigateFile_2)")
      }
    };
  }

  @DataProvider
  private Object[][] dataToCheckNavigateByNameWithSpecialSymbols() {
    return new Object[][] {
      {
        "*.java",
        ImmutableMap.of(
            1, "AppController.java (/NavigateFile/src/main/java/org/eclipse/qa/examples)",
            2, "AppController.java (/NavigateFile_2/src/main/java/org/eclipse/qa/examples)")
      },
      {
        "ind*.jsp",
        ImmutableMap.of(
            1, "index.jsp (/NavigateFile/src/main/webapp)",
            2, "index.jsp (/NavigateFile_2/src/main/webapp)")
      },
      {
        "*R*.md",
        ImmutableMap.of(
            1, "README.md (/NavigateFile)",
            2, "README.md (/NavigateFile_2)")
      },
      {
        "we?.xml",
        ImmutableMap.of(
            1, "web.xml (/NavigateFile/src/main/webapp/WEB-INF)",
            2, "web.xml (/NavigateFile_2/src/main/webapp/WEB-INF)")
      },
      {
        "gu?ss_n?m.j?p",
        ImmutableMap.of(
            1, "guess_num.jsp (/NavigateFile/src/main/webapp/WEB-INF/jsp)",
            2, "guess_num.jsp (/NavigateFile_2/src/main/webapp/WEB-INF/jsp)")
      }
    };
  }
}
