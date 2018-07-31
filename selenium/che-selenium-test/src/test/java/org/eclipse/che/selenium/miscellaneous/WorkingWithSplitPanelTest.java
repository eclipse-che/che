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
package org.eclipse.che.selenium.miscellaneous;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.GIT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Git.STATUS;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.COMMON_GOAL;
import static org.eclipse.che.selenium.pageobject.MultiSplitPanel.SplitPaneCommands.CLOSE_ALL_TABS;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.AskDialog;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.MultiSplitPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.git.Git;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsEditor;
import org.eclipse.che.selenium.pageobject.intelligent.CommandsExplorer;
import org.openqa.selenium.Keys;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Aleksandr Shmaraev */
public class WorkingWithSplitPanelTest {

  private static final String PROJECT_NAME = NameGenerator.generate("MultiSplitPane", 4);
  private static final String BUILD_COMM = "newMaven";
  private static final String[] checkMcTerminal = {"Left", "File", "Command", "Options", "Right"};

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Menu menu;
  @Inject private Loader loader;
  @Inject private CheTerminal terminal;
  @Inject private Consoles consoles;
  @Inject private MultiSplitPanel multiSplitPanel;
  @Inject private AskDialog askDialog;
  @Inject private Git git;
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
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void checkMultiSplitPane() {
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    terminal.waitTerminalTab();
    loader.waitOnClosed();

    // open menu of the split pane
    multiSplitPanel.waitNumberOpenSplitPanels(1);
    multiSplitPanel.clickOnIconMultiSplitPanel(1);
    multiSplitPanel.waitSplitPanelMenuIsOpen();
    multiSplitPanel.clickOnIconMultiSplitPanel(1);
    multiSplitPanel.waitSplitPanelMenuIsClosed();
    loader.waitOnClosed();

    // create a several panels
    multiSplitPanel.clickOnIconMultiSplitPanel(1);
    multiSplitPanel.waitSplitPanelMenuIsOpen();
    multiSplitPanel.selectCommandSplitPane(MultiSplitPanel.SplitPaneCommands.SPLIT_PANE_IN_COLUMNS);
    multiSplitPanel.waitNumberOpenSplitPanels(2);
    multiSplitPanel.clickOnIconMultiSplitPanel(1);
    multiSplitPanel.selectCommandSplitPane(MultiSplitPanel.SplitPaneCommands.SPLIT_PANE_IN_ROWS);
    multiSplitPanel.waitNumberOpenSplitPanels(3);
    multiSplitPanel.clickOnIconMultiSplitPanel(3);
    multiSplitPanel.waitSplitPanelMenuIsOpen();
    multiSplitPanel.selectCommandSplitPane(MultiSplitPanel.SplitPaneCommands.SPLIT_PANE_IN_ROWS);
    multiSplitPanel.waitNumberOpenSplitPanels(4);

    // close one panel from split pane menu
    multiSplitPanel.clickOnIconMultiSplitPanel(4);
    multiSplitPanel.waitProcessIsNotPresentIntoPaneMenu(
        MultiSplitPanel.SplitPaneCommands.CLOSE_PANE);
    multiSplitPanel.clickOnIconMultiSplitPanel(3);
    multiSplitPanel.waitSplitPanelMenuIsOpen();
    multiSplitPanel.selectCommandSplitPane(MultiSplitPanel.SplitPaneCommands.CLOSE_PANE);
    multiSplitPanel.waitSplitPanelMenuIsClosed();
    multiSplitPanel.waitNumberOpenSplitPanels(3);
  }

  @Test(priority = 1)
  public void checkTerminalAndBuild() {
    // make build, open terminal and check tabs
    multiSplitPanel.selectSplitPanel(3);
    commandsBuilder(
        TestIntelligentCommandsConstants.CommandsGoals.COMMON_GOAL,
        TestIntelligentCommandsConstants.CommandsTypes.MAVEN_TYPE);
    projectExplorer.invokeCommandWithContextMenu(COMMON_GOAL, PROJECT_NAME, BUILD_COMM);
    consoles.waitExpectedTextIntoConsole(TestBuildConstants.BUILD_SUCCESS, 120);
    multiSplitPanel.waitTabProcessIsPresent(3, BUILD_COMM);
    multiSplitPanel.selectSplitPanel(1);
    consoles.clickOnPlusMenuButton();
    consoles.clickOnTerminalItemInContextMenu();
    consoles.startTerminalFromProcessesArea("dev-machine");
    multiSplitPanel.waitTabProcessIsPresent(1, "Terminal-2");
    terminal.waitTerminalIsNotEmpty();
    loader.waitOnClosed();
    terminal.typeIntoTerminal("mc");
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    terminal.waitTerminalIsNotEmpty();
    loader.waitOnClosed();
    for (String partOfContent : checkMcTerminal) {
      terminal.waitExpectedTextIntoTerminal(partOfContent);
    }
    multiSplitPanel.waitTabProcessIsNotPresent(2, BUILD_COMM);
  }

  @Test(priority = 2)
  public void checkTabsOnSplitPanel() {
    // check tabs on split panels
    multiSplitPanel.selectSplitPanel(2);
    menu.runCommand(
        TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.INITIALIZE_REPOSITORY);
    askDialog.confirmAndWaitClosed();
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.STATUS);
    multiSplitPanel.waitTabProcessIsPresent(2, "Git status");
    multiSplitPanel.waitTabProcessIsPresent(2, "Git init");
    multiSplitPanel.waitMesageIntoSplitGitPanel(
        2, " On branch master\n" + " Changes not staged for commit");
    multiSplitPanel.clickOnIconMultiSplitPanel(2);
    multiSplitPanel.waitSplitPanelMenuIsOpen();
    multiSplitPanel.selectCommandSplitPane("Git status");
    multiSplitPanel.waitTabNameProcessIsFocused("Git status");
    multiSplitPanel.waitSplitPanelMenuIsClosed();
    multiSplitPanel.closeProcessByTabName("Git status");
    multiSplitPanel.waitTabProcessIsNotPresent(2, "Git status");
    multiSplitPanel.clickOnIconMultiSplitPanel(2);
    multiSplitPanel.waitSplitPanelMenuIsOpen();
    multiSplitPanel.waitProcessIsNotPresentIntoPaneMenu("Git status");
    multiSplitPanel.closeProcessIntoPaneMenu("Git init");
    multiSplitPanel.waitSplitPanelMenuIsClosed();
    multiSplitPanel.waitTabProcessIsNotPresent(2, "Git init");
    menu.runCommand(TestMenuCommandsConstants.Git.GIT, TestMenuCommandsConstants.Git.ADD_TO_INDEX);
    git.confirmAddToIndexForm();
    multiSplitPanel.waitTabProcessIsPresent(2, "Git add to index");
  }

  @Test(priority = 3)
  public void checkSwitchingTabsAndPanels() {
    // switch tabs and panels
    consoles.clickOnMaximizePanelIcon();
    multiSplitPanel.selectSplitPanel(1);
    menu.runCommand(GIT, STATUS);
    multiSplitPanel.waitTabProcessIsPresent(1, "Git status");
    multiSplitPanel.waitTabNameProcessIsFocused("Git status");
    multiSplitPanel.waitMesageIntoSplitGitPanel(
        1, " On branch master\n" + " Changes to be committed");
    multiSplitPanel.selectProcessByTabName(2, "Git add to index");
    multiSplitPanel.waitTabNameProcessIsFocused("Git add to index");
    multiSplitPanel.waitMesageIntoSplitGitPanel(2, "Git index updated");
    consoles.clickOnPlusMenuButton();
    consoles.clickOnServerItemInContextMenu();
    multiSplitPanel.waitTabProcessIsPresent(1, "Servers");
    multiSplitPanel.clickOnIconMultiSplitPanel(1);
    multiSplitPanel.selectCommandSplitPane(CLOSE_ALL_TABS);
    multiSplitPanel.waitSplitPanelMenuIsClosed();
    multiSplitPanel.waitTabProcessIsNotPresent(1, BUILD_COMM);
    multiSplitPanel.waitTabProcessIsNotPresent(1, "Terminal-2");
    multiSplitPanel.waitTabProcessIsNotPresent(1, "Git status");
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
    projectExplorer.clickOnProjectExplorerTab();
  }
}
