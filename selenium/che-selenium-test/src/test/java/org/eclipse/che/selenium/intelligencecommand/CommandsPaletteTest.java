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

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
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
  private static final String PROJECT_NAME = "cop";
  private static final String customCommandName = "newCustom";

  @Inject private TestWorkspace testWorkspace;
  @Inject private CommandsPalette cop;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Consoles consoles;
  @Inject private Menu menu;
  @Inject private CommandsEditor commandsEditor;
  @Inject private CommandsExplorer commandsExplorer;
  @Inject private Loader loader;
  @Inject private AskDialog askDialog;
  @Inject private Ide ide;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private Wizard wizard;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void commandPaletteTest() {
    // Create a java spring project
    projectExplorer.waitProjectExplorer();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    wizard.selectProjectAndCreate(Wizard.SamplesName.WEB_JAVA_SPRING, PROJECT_NAME);
    notificationsPopupPanel.waitProgressPopupPanelClose();

    // Open and close COP by hot keys
    cop.openCommandPaletteByHotKeys();
    cop.closeCommandPalette();

    // Start a command by Enter key
    cop.openCommandPalette();
    cop.startCommandByEnterKey(PROJECT_NAME + ": build");
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS, 120);

    // Start a command by double click
    cop.openCommandPalette();
    cop.startCommandByDoubleClick(PROJECT_NAME + ": debug");
    consoles.waitExpectedTextIntoConsole("Server startup in", 120);

    // Start commands from list after search
    cop.openCommandPalette();
    cop.searchAndStartCommand("tomcat");
    cop.startCommandByDoubleClick(PROJECT_NAME + ": stop");
    consoles.waitTabNameProcessIsPresent(PROJECT_NAME + ": stop tomcat");

    // Select commands from keyboard navigation (arrow buttons and "Enter" button)
    cop.openCommandPalette();
    cop.moveAndStartCommand(CommandsPalette.MoveTypes.DOWN, 3);
    consoles.waitTabNameProcessIsPresent(PROJECT_NAME + ": run tomcat");
    consoles.waitExpectedTextIntoConsole("Server startup in", 120);
  }

  @Test(priority = 1)
  public void newCommandTest() {
    projectExplorer.waitProjectExplorer();
    commandsBuilder(
        TestIntelligentCommandsConstants.CommandsGoals.COMMON_GOAL,
        TestIntelligentCommandsConstants.CommandsTypes.CUSTOM_TYPE);
    cop.openCommandPaletteByHotKeys();
    cop.startCommandByDoubleClick(customCommandName);
    consoles.waitExpectedTextIntoConsole("hello", 120);
    commandDelete(customCommandName);
    cop.openCommandPalette();
    cop.commandIsNotExists(customCommandName);
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
}
