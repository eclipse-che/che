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
package org.eclipse.che.selenium.miscellaneous;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckMacrosFeatureTest {
  // TODO In this test need add more macros for checking
  private static final String PROJECT_NAME = CheckMacrosFeatureTest.class.getSimpleName();
  private static final String COMMAND =
      "echo ${editor.current.file.name} ${editor.current.file.path}"
          + " ${editor.current.file.relpath} ${editor.current.project.name}"
          + " ${editor.current.project.type}";
  private static final String COMMAND_NAME = "firstCommand";
  private String expectedText =
      "README.md /projects/CheckMacrosFeatureTest/README.md"
          + " /CheckMacrosFeatureTest/README.md CheckMacrosFeatureTest maven";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CodenvyEditor editor;
  @Inject private Consoles console;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private CommandsEditor commandsEditor;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING.toString());
    ide.open(workspace);
  }

  @Test
  public void checkMacrosFeature() throws Exception {
    List<String> textList;
    projectExplorer.waitProjectExplorer();
    createCommand(COMMAND_NAME, COMMAND);

    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    projectExplorer.selectItem(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/README.md");
    loader.waitOnClosed();
    editor.waitActive();
    editor.waitTabIsPresent("README.md");

    projectExplorer.invokeCommandWithContextMenu(
        ProjectExplorer.CommandsGoal.COMMON, PROJECT_NAME, COMMAND_NAME);
    console.waitIsCommandConsoleOpened(20);
    console.waitExpectedTextIntoConsole(expectedText);

    textList = Arrays.asList(console.getVisibleTextFromCommandConsole().split("\n"));
    loader.waitOnClosed();
    Assert.assertTrue(textLineOncePresent(textList, expectedText));
  }

  private boolean textLineOncePresent(List<String> textList, String expectTextLine) {
    int quantity = 0;
    for (String textLine : textList) {
      if (textLine.contains(expectTextLine)) {
        quantity++;
      }
    }
    return quantity == 1;
  }

  private void createCommand(String commandName, String command) {
    commandsExplorer.openCommandsExplorer();
    commandsExplorer.waitCommandExplorerIsOpened();
    loader.waitOnClosed();
    commandsExplorer.clickAddCommandButton(
        TestIntelligentCommandsConstants.CommandsGoals.COMMON_GOAL);
    loader.waitOnClosed();
    commandsExplorer.chooseCommandTypeInContextMenu(
        TestIntelligentCommandsConstants.CommandsTypes.CUSTOM_TYPE);
    loader.waitOnClosed();
    commandsExplorer.waitCommandInExplorerByName(
        TestIntelligentCommandsConstants.CommandsDefaultNames.CUSTOM_NAME);
    commandsEditor.waitActive();
    commandsEditor.waitTabFileWithSavedStatus(
        TestIntelligentCommandsConstants.CommandsDefaultNames.CUSTOM_NAME);
    commandsEditor.clickOnCancelCommandEditorButton();
    commandsEditor.waitTabIsNotPresent(
        TestIntelligentCommandsConstants.CommandsDefaultNames.CUSTOM_NAME);

    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.selectCommandByName(
        TestIntelligentCommandsConstants.CommandsDefaultNames.CUSTOM_NAME);
    editor.waitActive();
    commandsEditor.typeTextIntoNameCommandField(commandName);
    commandsEditor.waitTextIntoNameCommandField(commandName);
    commandsEditor.waitTabCommandWithUnsavedStatus(
        TestIntelligentCommandsConstants.CommandsDefaultNames.CUSTOM_NAME);

    commandsEditor.setCursorToLine(1);
    commandsEditor.deleteAllContent();
    commandsEditor.typeTextIntoEditor(command);
    commandsEditor.waitTextIntoEditor(command);
    commandsEditor.clickOnSaveButtonInTheEditCommand();
    loader.waitOnClosed();

    projectExplorer.clickOnProjectExplorerTabInTheLeftPanel();
  }
}
