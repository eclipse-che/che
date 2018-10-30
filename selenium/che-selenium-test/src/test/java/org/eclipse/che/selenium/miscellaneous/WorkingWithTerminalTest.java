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

import static java.lang.String.valueOf;
import static org.eclipse.che.selenium.pageobject.PanelSelector.PanelTypes.LEFT_BOTTOM_ID;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.PanelSelector;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Aleksandr Shmaraev
 * @author Alexander Andrienko
 */
public class WorkingWithTerminalTest {
  private static final String PROJECT_NAME = NameGenerator.generate("project", 4);
  private static final Logger LOG = LoggerFactory.getLogger(WorkingWithTerminalTest.class);

  private static final String[] CHECK_MC_OPENING = {
    "Left", "File", "Command", "Options", "Right", "Name", "bin", "dev", "etc", "home",
    "lib", "lib64", "bin", "1Help", "2Menu", "3View", "4Edit", "5Copy", "6RenMov", "7Mkdir",
    "8Delete", "9PullDn", "10Quit"
  };

  private static final String MESS_IN_CONSOLE =
      "Installing /projects/" + PROJECT_NAME + "/target/qa-spring-sample-1.0-SNAPSHOT.war";
  private static final String WAR_NAME = "qa-spring-sample-1.0-SNAPSHOT.war";

  private static final String BASH_SCRIPT =
      "for i in `seq 1 10`; do sleep 1; echo \"test=$i\"; done";

  private static final String MC_HELP_DIALOG =
      "This is the main help screen for GNU Midnight Commander.";
  private static final String MC_USER_MENU_DIALOG = "User menu";
  private static final String[] VIEW_BIN_FOLDER = {"bash", "chmod", "date"};

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private CheTerminal terminal;
  @Inject private Consoles consoles;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private PanelSelector panelSelector;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
    ide.waitOpenedWorkspaceIsReadyToUse();
  }

  @BeforeMethod
  private void prepareNewTerminal() {
    panelSelector.selectPanelTypeFromPanelSelector(LEFT_BOTTOM_ID);

    projectExplorer.waitItem(PROJECT_NAME);

    if (terminal.terminalIsPresent()) {
      consoles.closeTerminalIntoConsoles();
      terminal.waitTerminalIsNotPresent(1);
    }

    consoles.clickOnPlusMenuButton();
    consoles.clickOnTerminalItemInContextMenu();

    terminal.selectFirstTerminalTab();
    terminal.waitTerminalConsole();
    terminal.waitFirstTerminalIsNotEmpty();
  }

  @Test
  public void shouldLaunchCommandWithBigOutput() {
    // build the web java application
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    terminal.waitTerminalConsole(1);
    terminal.typeIntoActiveTerminal("cd /projects/" + PROJECT_NAME + Keys.ENTER);
    terminal.waitTextInFirstTerminal("/projects/" + PROJECT_NAME);
    terminal.typeIntoActiveTerminal("mvn clean install" + Keys.ENTER);
    terminal.waitTextInTerminal(
        TestBuildConstants.BUILD_SUCCESS, TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC);
    terminal.waitTextInFirstTerminal(MESS_IN_CONSOLE);

    // check the target folder
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/target");
    projectExplorer.waitItem(PROJECT_NAME + "/target/" + WAR_NAME);
  }

  @Test
  public void shouldScrollAndAppearMCDialogs() {
    terminal.typeIntoActiveTerminal("cd ~ && touch -f testfile.txt" + Keys.ENTER);

    openMC("~");
    // check END + F5
    terminal.typeIntoActiveTerminal("" + Keys.END + Keys.F5);
    terminal.waitTextInFirstTerminal("Copy file \"testfile.txt\" with source mask");
    terminal.typeIntoActiveTerminal("" + Keys.ESCAPE + Keys.ESCAPE);

    // check HOME + F6
    // we need to press END, HOME or PAGE_DOWN.. keys before functional key because click in MC
    // moves
    // selection to another file in panel
    terminal.typeIntoActiveTerminal("" + Keys.HOME + Keys.F6.toString());
    terminal.waitTextInFirstTerminal("Cannot operate on \"..\"!");
    terminal.typeIntoActiveTerminal("" + Keys.ESCAPE + Keys.ESCAPE);

    // check F7
    terminal.typeIntoActiveTerminal(Keys.F7.toString());
    terminal.waitTextInFirstTerminal("Enter directory name:");
    terminal.typeIntoActiveTerminal("" + Keys.ESCAPE + Keys.ESCAPE);

    // check PAGE_DOWN + F8
    terminal.typeIntoActiveTerminal(
        ""
            + Keys.PAGE_DOWN
            + Keys.PAGE_DOWN
            + Keys.PAGE_DOWN
            + Keys.PAGE_DOWN
            + Keys.F8.toString());
    terminal.waitTextInFirstTerminal("Delete file");
    terminal.waitTextInFirstTerminal("\"testfile.txt\"?");
    terminal.typeIntoActiveTerminal("" + Keys.ESCAPE + Keys.ESCAPE);

    // check F9 - Select menu in MC
    terminal.typeIntoActiveTerminal("" + Keys.F9 + Keys.ENTER);
    terminal.waitTextInFirstTerminal("File listing");
    terminal.typeIntoActiveTerminal("" + Keys.ESCAPE + Keys.ESCAPE);
  }

  @Test
  public void shouldResizeTerminal() {
    openMC("/");

    try {
      // check the root content of the midnight commander
      for (String partOfContent : CHECK_MC_OPENING) {
        terminal.waitTextInFirstTerminal(partOfContent);
      }
      terminal.waitNoTextInFirstTerminal(".dockerenv");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/10854", ex);
    }

    consoles.clickOnMaximizePanelIcon();
    loader.waitOnClosed();

    // check resize of the terminal
    try {
      for (String partOfContent : CHECK_MC_OPENING) {
        terminal.waitTextInFirstTerminal(partOfContent);
      }
      terminal.waitTextInFirstTerminal(".dockerenv");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/10854", ex);
    }

    consoles.clickOnMaximizePanelIcon();
    loader.waitOnClosed();

    try {
      for (String partOfContent : CHECK_MC_OPENING) {
        terminal.waitTextInFirstTerminal(partOfContent);
      }
      terminal.waitNoTextInFirstTerminal(".dockerenv");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/10854", ex);
    }
  }

  @Test
  public void shouldNavigateToMC() {
    openMC("/");

    // navigate to midnight commander tree
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoActiveTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoActiveTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoActiveTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoActiveTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoActiveTerminal(Keys.ENTER.toString());
    terminal.typeIntoActiveTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoActiveTerminal(Keys.ENTER.toString());

    try {
      // check the home content of the midnight commander
      for (String partOfContent : CHECK_MC_OPENING) {
        terminal.waitTextInFirstTerminal(partOfContent);
      }
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known random failure https://github.com/eclipse/che/issues/10854", ex);
    }

    terminal.typeIntoActiveTerminal(Keys.F10.toString());
  }

  @Test
  public void shouldCreateFileTest() {
    terminal.typeIntoActiveTerminal("cd ~" + Keys.ENTER);
    terminal.typeIntoActiveTerminal("ls" + Keys.ENTER);
    terminal.waitTextInFirstTerminal("che");
    terminal.typeIntoActiveTerminal("touch a.txt" + Keys.ENTER);

    terminal.typeIntoActiveTerminal("ls" + Keys.ENTER);
    terminal.waitTextInFirstTerminal("che");
    terminal.waitTextInFirstTerminal("a.txt");
    terminal.waitTextInFirstTerminal("tomcat8");
  }

  @Test
  public void shouldCancelProcessByCtrlC() {
    terminal.typeIntoActiveTerminal("cd /" + Keys.ENTER);

    // launch bash script
    terminal.typeIntoActiveTerminal(BASH_SCRIPT + Keys.ENTER);
    terminal.waitTextInFirstTerminal("test=1");

    // cancel script
    terminal.typeIntoActiveTerminal(Keys.CONTROL + "c");

    // wait 1 sec. If process was really stopped we should not get text "test=2"
    WaitUtils.sleepQuietly(1);

    try {
      terminal.waitNoTextInFirstTerminal("test=2");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known permanent failure https://github.com/eclipse/che/issues/8390");
    }
  }

  @Test
  public void shouldBeClear() throws ExecutionException, InterruptedException {
    terminal.typeIntoActiveTerminal("cd / && ls -l" + Keys.ENTER);

    // clear terminal
    terminal.typeIntoActiveTerminal("clear" + Keys.ENTER);
    terminal.waitNoTextInFirstTerminal("clear");
    terminal.waitTextInFirstTerminal("@");
  }

  @Test
  public void shouldBeReset() throws ExecutionException, InterruptedException {
    terminal.typeIntoActiveTerminal("cd / && ls -l" + Keys.ENTER);

    // clear terminal
    terminal.typeIntoActiveTerminal("reset" + Keys.ENTER.toString());
    terminal.waitNoTextInFirstTerminal("reset");
    terminal.waitTextInFirstTerminal("@");
  }

  @Test
  public void shouldTurnToNormalModeFromAlternativeScreenModeAndOtherwise() {
    // open MC - terminal will switch off from normal mode to alternative screen with text user
    // interface (pseudo user graphics).
    openMC("/");

    // turn back to "normal" mode
    terminal.typeIntoActiveTerminal(Keys.CONTROL + "o");
    for (String partOfContent : CHECK_MC_OPENING) {
      terminal.waitNoTextInFirstTerminal(partOfContent);
    }

    terminal.typeIntoActiveTerminal(Keys.CONTROL + "o" + Keys.ENTER);
    for (String partOfContent : CHECK_MC_OPENING) {
      terminal.waitTextInFirstTerminal(partOfContent);
    }
  }

  @Test
  public void shouldOpenMCHelpDialogAndUserMenuDialog() {
    openMC("/");

    // check "F1"
    terminal.typeIntoActiveTerminal(Keys.F1.toString());
    terminal.waitTextInFirstTerminal(MC_HELP_DIALOG);
    terminal.typeIntoActiveTerminal(Keys.F10.toString());
    terminal.waitNoTextInFirstTerminal(MC_HELP_DIALOG);

    // check "F2" key
    terminal.typeIntoActiveTerminal(Keys.F2.toString());
    terminal.waitTextInFirstTerminal(MC_USER_MENU_DIALOG);
    terminal.typeIntoActiveTerminal(Keys.F10.toString());
    terminal.waitNoTextInFirstTerminal(MC_USER_MENU_DIALOG);
  }

  @Test
  public void shouldViewFolderIntoMC() {
    terminal.waitFirstTerminalTab();
    consoles.clickOnMaximizePanelIcon();
    openMC("/");

    // select bin folder and view this folder by "F3" key
    terminal.waitTextInFirstTerminal("bin");
    terminal.typeIntoActiveTerminal(Keys.HOME.toString() + Keys.F3.toString());
    for (String partOfContent : VIEW_BIN_FOLDER) {
      terminal.waitTextInFirstTerminal(partOfContent);
    }
    terminal.typeIntoActiveTerminal("cd ~" + Keys.ENTER);
    terminal.waitTextInFirstTerminal("che");
    consoles.clickOnMaximizePanelIcon();
  }

  @Test
  public void closeTerminalByExitCommand() {
    terminal.waitTerminalConsole();
    terminal.typeIntoActiveTerminal("exit" + Keys.ENTER);
    terminal.waitTerminalIsNotPresent(1);
  }

  @Test
  public void shouldEditFileIntoMCEdit() {
    openMC("/projects/" + PROJECT_NAME);

    // check End, Home, F4, Delete keys
    terminal.typeIntoActiveTerminal(
        "" + Keys.END + Keys.ENTER + Keys.END + Keys.ARROW_UP + Keys.F4);
    // select editor
    terminal.typeIntoActiveTerminal(valueOf(1) + Keys.ENTER);

    terminal.waitTextInFirstTerminal("README.md");
    terminal.typeIntoActiveTerminal("<!-some comment->");
    terminal.typeIntoActiveTerminal(
        "" + Keys.HOME + Keys.ARROW_RIGHT + Keys.ARROW_RIGHT + Keys.ARROW_RIGHT + Keys.DELETE);
    terminal.waitTextInFirstTerminal("<!-ome comment->");
  }

  @Test
  public void checkDeleteAction() {
    // if the bug exists -> the dialog appears and the terminal lose focus
    terminal.typeIntoActiveTerminal(Keys.DELETE.toString());
    terminal.typeIntoActiveTerminal("pwd");
  }

  private void openMC(String currentLocation) {
    // launch mc from root directory
    loader.waitOnClosed();
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoActiveTerminal("cd " + currentLocation);
    terminal.typeIntoActiveTerminal(Keys.ENTER.toString());
    terminal.typeIntoActiveTerminal("mc");
    terminal.typeIntoActiveTerminal(Keys.ENTER.toString());
    terminal.waitTextInFirstTerminal("Modify time");
  }
}
