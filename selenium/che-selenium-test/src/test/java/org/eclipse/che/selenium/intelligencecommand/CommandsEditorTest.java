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
package org.eclipse.che.selenium.intelligencecommand;

import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsDefaultNames.JAVA_NAME;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.COMMON_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.NEW_COMMAND_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.RUN_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes.JAVA_TYPE;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskForValueDialog;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraiev */
public class CommandsEditorTest {
  private static final String PROJECT_NAME = NameGenerator.generate("CommandsEditor", 4);
  private static final String NAME_COMMAND = "runApp";
  private static final String COMMAND =
      "cd ${current.project.path}\n"
          + "javac -classpath ${project.java.classpath} -sourcepath ${project.java.sourcepath} -d ${current.project.path} src/com/company/nba/MainClass.java\n"
          + "java -classpath ${project.java.classpath}${project.java.output.dir} com.company.nba.MainClass";

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private Loader loader;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private CommandsEditor commandsEditor;
  @Inject private CodenvyEditor editor;
  @Inject private AskForValueDialog askForValueDialog;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    URL resource = getClass().getResource("/projects/java-project-for-commands");
    testProjectServiceClient.importProject(
        testWorkspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.PLAIN_JAVA);
    ide.open(testWorkspace);
  }

  @Test
  public void checkComamandsEditor() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    projectExplorer.quickExpandWithJavaScript();
    projectExplorer.openItemByPath(PROJECT_NAME + "/src/com/company/nba/MainClass.java");
    projectExplorer.waitAndSelectItem(PROJECT_NAME);

    // open the 'Commands Explorer' and choose java command
    loader.waitOnClosed();
    commandsExplorer.openCommandsExplorer();
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.clickAddCommandButton(COMMON_GOAL);
    loader.waitOnClosed();
    commandsExplorer.chooseCommandTypeInContextMenu(JAVA_TYPE);
    loader.waitOnClosed();
    commandsExplorer.waitCommandInExplorerByName(JAVA_NAME);
    commandsEditor.waitActive();
    commandsEditor.waitTabFileWithSavedStatus(JAVA_NAME);
    commandsEditor.clickOnCancelCommandEditorButton();
    commandsEditor.waitTabIsNotPresent(JAVA_NAME);

    // edit name of the java command into editor
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.selectCommandByName(JAVA_NAME);
    commandsEditor.waitActive();
    commandsEditor.typeTextIntoNameCommandField(NAME_COMMAND);
    commandsEditor.waitTextIntoNameCommandField(NAME_COMMAND);
    commandsEditor.waitTabCommandWithUnsavedStatus(JAVA_NAME);

    // edit a content of command in the command editor
    commandsEditor.setCursorToLine(1);
    commandsEditor.deleteAllContent();
    commandsEditor.typeTextIntoEditor(COMMAND);
    commandsEditor.waitTextIntoEditor(COMMAND);

    // change the goal of command into editor
    commandsEditor.selectGoalNameIntoCommandEditor(RUN_GOAL);
    commandsEditor.clickOnSaveButtonInTheEditCommand();
    editor.waitTabFileWithSavedStatus(NAME_COMMAND);
    commandsExplorer.checkCommandIsPresentInGoal(RUN_GOAL, NAME_COMMAND);
    commandsExplorer.checkCommandIsNotPresentInGoal(COMMON_GOAL, NAME_COMMAND);
    commandsEditor.selectGoalNameIntoCommandEditor(NEW_COMMAND_GOAL);
    askForValueDialog.waitFormToOpen();
    askForValueDialog.typeAndWaitText("Custom");
    askForValueDialog.clickOkBtn();
    commandsEditor.waitTextIntoGoalField("Custom");
    commandsEditor.waitTabCommandWithUnsavedStatus(NAME_COMMAND);
    commandsEditor.clickOnSaveButtonInTheEditCommand();
    editor.waitTabFileWithSavedStatus(NAME_COMMAND);
    commandsExplorer.checkCommandIsNotPresentInGoal(RUN_GOAL, NAME_COMMAND);
    commandsExplorer.checkCommandIsPresentInGoal("Custom", NAME_COMMAND);

    // run application from commands editor
    commandsEditor.clickOnRunButton();
    loader.waitOnClosed();
    consoles.waitExpectedTextIntoConsole("I love this game!");
  }
}
