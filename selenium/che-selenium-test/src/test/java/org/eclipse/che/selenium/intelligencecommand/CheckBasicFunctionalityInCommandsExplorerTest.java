/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.intelligencecommand;

import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsDefaultNames;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes.MAVEN_TYPE;
import static org.eclipse.che.selenium.core.project.ProjectTemplates.MAVEN_SPRING;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Igor Ohrimenko */
public class CheckBasicFunctionalityInCommandsExplorerTest {

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private CommandsEditor commandsEditor;
  @Inject private Consoles consoles;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void prepare() throws Exception {
    String projectName = "commandsExplorerTestProject";
    URL resource = getClass().getResource("/projects/default-spring-project");
    testProjectServiceClient.importProject(
        testWorkspace.getId(), Paths.get(resource.toURI()), projectName, MAVEN_SPRING.toString());

    ide.open(testWorkspace);
    projectExplorer.waitItem(projectName);
    commandsExplorer.openCommandsExplorer();
  }

  @Test
  public void shouldCreateDifferentTypesCommandsInDifferentGoals() {
    commandsExplorer.waitCommandExplorerIsOpened();

    commandsBuilder(BUILD_GOAL, MAVEN_TYPE);
    commandsExplorer.checkCommandIsPresentInGoal(BUILD_GOAL, CommandsDefaultNames.MAVEN_NAME);
    cloneAndRemoveCommandByName(BUILD_GOAL, CommandsDefaultNames.MAVEN_NAME);

    commandsBuilder(CommandsGoals.COMMON_GOAL, CommandsTypes.JAVA_TYPE);
    commandsExplorer.checkCommandIsPresentInGoal(
        CommandsGoals.COMMON_GOAL, CommandsDefaultNames.JAVA_NAME);
    cloneAndRemoveCommandByName(CommandsGoals.COMMON_GOAL, CommandsDefaultNames.JAVA_NAME);

    commandsBuilder(CommandsGoals.DEBUG_GOAL, CommandsTypes.CUSTOM_TYPE);
    commandsExplorer.checkCommandIsPresentInGoal(
        CommandsGoals.DEBUG_GOAL, CommandsDefaultNames.CUSTOM_NAME);
    cloneAndRemoveCommandByName(CommandsGoals.DEBUG_GOAL, CommandsDefaultNames.CUSTOM_NAME);

    commandsBuilder(CommandsGoals.RUN_GOAL, CommandsTypes.GWT_TYPE);
    commandsExplorer.checkCommandIsPresentInGoal(
        CommandsGoals.RUN_GOAL, CommandsDefaultNames.GWT_NAME);
    cloneAndRemoveCommandByName(CommandsGoals.RUN_GOAL, CommandsDefaultNames.GWT_NAME);

    commandsBuilder(CommandsGoals.TEST_GOAL, CommandsTypes.GWT_SDM_FOR_CHE_TYPE);
    commandsExplorer.checkCommandIsPresentInGoal(
        CommandsGoals.TEST_GOAL, CommandsDefaultNames.GWT_SDM_FOR_CHE);
    cloneAndRemoveCommandByName(CommandsGoals.TEST_GOAL, CommandsDefaultNames.GWT_SDM_FOR_CHE);
  }

  @Test(priority = 1)
  public void shouldCreateAndRunBuildCommand() {
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsBuilder(BUILD_GOAL, MAVEN_TYPE);
    cloneAndRunByName(CommandsDefaultNames.MAVEN_NAME);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS);
  }

  @Test(priority = 2)
  public void shouldCreateAndRunCustomCommand() {
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsBuilder(CommandsGoals.RUN_GOAL, CommandsTypes.CUSTOM_TYPE);
    cloneAndRunByName(CommandsDefaultNames.CUSTOM_NAME);
    consoles.waitExpectedTextIntoConsole("hello");
  }

  private void commandsBuilder(String goalName, String commandType) {
    commandsExplorer.waitCommandExplorerIsOpened();
    commandsExplorer.clickAddCommandButton(goalName);
    commandsExplorer.chooseCommandTypeInContextMenu(commandType);
    commandsEditor.waitActive();
    commandsEditor.clickOnCancelCommandEditorButton();
  }

  private void cloneAndRemoveCommandByName(String goalName, String commandName) {
    commandsExplorer.waitCommandInExplorerByName(commandName);
    commandsExplorer.cloneCommandByName(commandName);
    commandsExplorer.waitCommandInExplorerByName(commandName + " copy");
    commandsExplorer.checkCommandIsPresentInGoal(goalName, commandName + " copy");
    commandsExplorer.deleteCommandByName(commandName + " copy");
    commandsExplorer.waitRemoveCommandFromExplorerByName(commandName + " copy");
    commandsExplorer.checkCommandIsNotPresentInGoal(goalName, commandName + " copy");
    commandsExplorer.deleteCommandByName(commandName);
    commandsExplorer.waitRemoveCommandFromExplorerByName(commandName);
    commandsExplorer.checkCommandIsNotPresentInGoal(goalName, commandName);
  }

  private void cloneAndRunByName(String commandName) {
    commandsExplorer.waitCommandInExplorerByName(commandName);
    commandsExplorer.cloneCommandByName(commandName);
    commandsExplorer.waitCommandInExplorerByName(commandName + " copy");
    commandsExplorer.runCommandByName(commandName + " copy");
  }
}
