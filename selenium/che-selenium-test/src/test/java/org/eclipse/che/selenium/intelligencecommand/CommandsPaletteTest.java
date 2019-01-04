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
package org.eclipse.che.selenium.intelligencecommand;

import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsGoals.COMMON_GOAL;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandsTypes.CUSTOM_TYPE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_SPRING;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsPalette;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Sergey Skorik */
public class CommandsPaletteTest {
  protected static final String PROJECT_NAME = "project";
  protected static final String COMMAND = PROJECT_NAME + ": build";
  private static final String customCommandName = "newCustom";

  @Inject private TestWorkspace testWorkspace;
  @Inject protected CommandsPalette commandsPalette;
  @Inject private ProjectExplorer projectExplorer;
  @Inject protected Consoles consoles;
  @Inject private Menu menu;
  @Inject private CommandsEditor commandsEditor;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private Loader loader;
  @Inject private AskDialog askDialog;
  @Inject private Ide ide;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject protected Wizard wizard;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void commandPaletteTest() {
    // Create a test project
    projectExplorer.waitProjectExplorer();
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    selectSampleProject();
    notificationsPopupPanel.waitProgressPopupPanelClose();
    consoles.waitJDTLSProjectResolveFinishedMessage();

    // Open and close COP by hot keys
    commandsPalette.openCommandPaletteByHotKeys();
    commandsPalette.closeCommandPalette();

    // Start a command by Enter key
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByEnterKey(COMMAND);
    consoles.waitExpectedTextIntoConsole(BUILD_SUCCESS, EXPECTED_MESS_IN_CONSOLE_SEC);

    // Start a command by double click
    commandsPalette.openCommandPalette();
    startCommandByDoubleClick();

    // Start commands from list after search
    commandsPalette.openCommandPalette();
    startCommandFromSearchList();

    // Select commands from keyboard navigation (arrow buttons and "Enter" button)
    commandsPalette.openCommandPalette();
    selectCommandByKeyboardNavigation();
  }

  @Test(priority = 1)
  public void newCommandTest() {
    projectExplorer.waitProjectExplorer();
    commandsBuilder(COMMON_GOAL, CUSTOM_TYPE);
    commandsPalette.openCommandPalette();
    commandsPalette.startCommandByDoubleClick(customCommandName);
    consoles.waitExpectedTextIntoConsole("hello", EXPECTED_MESS_IN_CONSOLE_SEC);
    commandDelete(customCommandName);
    commandsPalette.openCommandPalette();
    commandsPalette.commandIsNotExists(customCommandName);
  }

  private void commandsBuilder(String goalName, String commandType) {
    commandsExplorer.openCommandsExplorer();
    commandsExplorer.waitCommandExplorerIsOpened();
    loader.waitOnClosed();
    commandsExplorer.clickAddCommandButton(goalName);
    loader.waitOnClosed();
    commandsExplorer.chooseCommandTypeInContextMenu(commandType);
    loader.waitOnClosed();
    commandsEditor.waitActive();
    commandsEditor.clickOnCancelCommandEditorButton();
    loader.waitOnClosed();
  }

  private void commandDelete(String commandName) {
    loader.waitOnClosed();
    commandsExplorer.clickOnRemoveButtonInExplorerByName(commandName);
    askDialog.waitFormToOpen();
    askDialog.confirmAndWaitClosed();
    loader.waitOnClosed();
    commandsExplorer.waitRemoveCommandFromExplorerByName(commandName);
  }

  protected void selectSampleProject() {
    wizard.selectProjectAndCreate(WEB_JAVA_SPRING, PROJECT_NAME);
  }

  protected void startCommandByDoubleClick() {
    commandsPalette.startCommandByDoubleClick(PROJECT_NAME + ": debug");
    consoles.waitExpectedTextIntoConsole("Server startup in", EXPECTED_MESS_IN_CONSOLE_SEC);
  }

  protected void startCommandFromSearchList() {
    commandsPalette.searchAndStartCommand("tomcat");
    commandsPalette.startCommandByDoubleClick(PROJECT_NAME + ": stop");
    consoles.waitTabNameProcessIsPresent(PROJECT_NAME + ": stop tomcat");
  }

  protected void selectCommandByKeyboardNavigation() {
    commandsPalette.moveAndStartCommand(CommandsPalette.MoveTypes.DOWN, 3);
    consoles.waitTabNameProcessIsPresent(PROJECT_NAME + ": run tomcat");
    consoles.waitExpectedTextIntoConsole("Server startup in", EXPECTED_MESS_IN_CONSOLE_SEC);
  }
}
