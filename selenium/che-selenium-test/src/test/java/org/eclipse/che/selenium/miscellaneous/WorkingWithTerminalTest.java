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

import static java.lang.String.valueOf;
import static org.eclipse.che.selenium.pageobject.PanelSelector.PanelTypes.LEFT_BOTTOM;
import static org.openqa.selenium.Keys.PAGE_DOWN;
import static org.openqa.selenium.Keys.PAGE_UP;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestBuildConstants;
import org.eclipse.che.selenium.core.constant.TestTimeoutsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.PanelSelector;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
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
      "for i in `seq 1 10`; do sleep 3; echo \"test=$i\"; done";

  private static final String MC_HELP_DIALOG =
      "This is the main help screen for GNU Midnight Commander.";
  private static final String MC_USER_MENU_DIALOG = "User menu";
  private static final String[] VIEW_BIN_FOLDER = {"bash", "chmod", "date"};

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private MachineTerminal terminal;
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
    try {
      panelSelector.selectPanelTypeFromPanelSelector(LEFT_BOTTOM);

      projectExplorer.waitItem(PROJECT_NAME);

      if (terminal.terminalIsPresent()) {
        consoles.closeTerminalIntoConsoles();
        terminal.waitTerminalIsNotPresent(1);
      }

      consoles.clickOnPlusMenuButton();
      consoles.clickOnTerminalItemInContextMenu();

      terminal.selectTerminalTab();
      terminal.waitTerminalConsole();
      terminal.waitTerminalIsNotEmpty();
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
    }
  }

  @Test
  public void shouldLaunchCommandWithBigOutput() {
    // build the web java application
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    projectExplorer.waitItem(PROJECT_NAME);
    terminal.waitTerminalConsole(1);
    terminal.typeIntoTerminal("cd /projects/" + PROJECT_NAME + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal("/projects/" + PROJECT_NAME);
    terminal.typeIntoTerminal("mvn clean install" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal(
        TestBuildConstants.BUILD_SUCCESS, TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC);
    terminal.waitExpectedTextIntoTerminal(MESS_IN_CONSOLE);

    // check the target folder
    projectExplorer.openItemByPath(PROJECT_NAME);
    projectExplorer.openItemByPath(PROJECT_NAME + "/target");
    projectExplorer.waitItem(PROJECT_NAME + "/target/" + WAR_NAME);
  }

  @Test
  public void shouldAppearMCDialogs() {
    terminal.typeIntoTerminal("cd ~ && touch -f testfile.txt" + Keys.ENTER);

    openMC("~");
    // check F5
    terminal.typeIntoTerminal("" + Keys.END + Keys.F5);

    terminal.waitExpectedTextIntoTerminal("Copy file \"testfile.txt\" with source mask");
    terminal.typeIntoTerminal("" + Keys.ESCAPE + Keys.ESCAPE);

    // check F6
    terminal.typeIntoTerminal(Keys.F6.toString());
    terminal.waitExpectedTextIntoTerminal("Move file \"testfile.txt\" with source mask");
    terminal.typeIntoTerminal("" + Keys.ESCAPE + Keys.ESCAPE);

    // check F7
    terminal.typeIntoTerminal(Keys.F7.toString());
    terminal.waitExpectedTextIntoTerminal("Enter directory name:");
    terminal.typeIntoTerminal("" + Keys.ESCAPE + Keys.ESCAPE);

    // check F8
    terminal.typeIntoTerminal(Keys.F8.toString());
    terminal.waitExpectedTextIntoTerminal("Delete file");
    terminal.waitExpectedTextIntoTerminal("\"testfile.txt\"?");
    terminal.typeIntoTerminal("" + Keys.ESCAPE + Keys.ESCAPE);

    // check F9 - Select menu in MC
    terminal.typeIntoTerminal("" + Keys.F9 + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal("File listing");
    terminal.typeIntoTerminal("" + Keys.ESCAPE + Keys.ESCAPE);
  }

  @Test
  public void shouldScrollIntoTerminal() throws InterruptedException {
    openMC("/");

    try {
      // check scrolling of the terminal
      terminal.movePageDownListTerminal("opt");

    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che-lib/issues/57", ex);
    }

    // check scrolling by the END and HOME buttons
    terminal.moveDownListTerminal(".dockerenv");
    terminal.waitExpectedTextIntoTerminal(".dockerenv");
    terminal.moveUpListTerminal("bin");
    terminal.waitExpectedTextIntoTerminal("bin");

    // check scrolling by the Page Up and the Page Down buttons
    terminal.typeIntoTerminal(PAGE_DOWN.toString());
    terminal.typeIntoTerminal(PAGE_DOWN.toString());
    terminal.waitExpectedTextIntoTerminal(".dockerenv");
    terminal.typeIntoTerminal(PAGE_UP.toString());
    terminal.typeIntoTerminal(PAGE_UP.toString());
    terminal.waitExpectedTextIntoTerminal("bin");
  }

  @Test
  public void shouldResizeTerminal() {
    openMC("/");

    try {
      // check the root content of the midnight commander
      for (String partOfContent : CHECK_MC_OPENING) {
        terminal.waitExpectedTextIntoTerminal(partOfContent);
      }
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che-lib/issues/57", ex);
    }

    terminal.waitExpectedTextNotPresentTerminal(".dockerenv");
    consoles.clickOnMaximizePanelIcon();
    loader.waitOnClosed();

    // check resize of the terminal
    for (String partOfContent : CHECK_MC_OPENING) {
      try {
        terminal.waitExpectedTextIntoTerminal(partOfContent);
      } catch (TimeoutException ex) {
        // remove try-catch block after issue has been resolved
        fail("Known issue https://github.com/eclipse/che-lib/issues/57");
      }
    }

    terminal.waitExpectedTextIntoTerminal(".dockerenv");
    consoles.clickOnMaximizePanelIcon();
    loader.waitOnClosed();
    for (String partOfContent : CHECK_MC_OPENING) {
      terminal.waitExpectedTextIntoTerminal(partOfContent);
    }
    terminal.waitExpectedTextNotPresentTerminal(".dockerenv");
  }

  @Test
  public void shouldNavigateToMC() {
    openMC("/");

    // navigate to midnight commander tree
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    terminal.typeIntoTerminal(Keys.ARROW_DOWN.toString());
    terminal.typeIntoTerminal(Keys.ENTER.toString());

    try {
      // check the home content of the midnight commander
      for (String partOfContent : CHECK_MC_OPENING) {
        terminal.waitExpectedTextIntoTerminal(partOfContent);
      }
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che-lib/issues/57", ex);
    }

    terminal.typeIntoTerminal(Keys.F10.toString());
  }

  @Test
  public void shouldCreateFileTest() {
    terminal.typeIntoTerminal("cd ~" + Keys.ENTER);
    terminal.typeIntoTerminal("ls" + Keys.ENTER);
    terminal.waitTerminalIsNotEmpty();
    terminal.waitExpectedTextIntoTerminal("che");
    terminal.typeIntoTerminal("touch testfile0.txt" + Keys.ENTER);

    terminal.typeIntoTerminal("ls" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal("che");
    terminal.waitExpectedTextIntoTerminal("che");
    terminal.waitExpectedTextIntoTerminal("testfile0.txt");
    terminal.waitExpectedTextIntoTerminal("tomcat8");
  }

  @Test
  public void shouldCancelProcessByCtrlC() throws InterruptedException {
    terminal.typeIntoTerminal("cd /" + Keys.ENTER);

    // launch bash script
    terminal.typeIntoTerminal(BASH_SCRIPT + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal("test=1");

    // cancel script
    terminal.typeIntoTerminal(Keys.CONTROL + "c");

    // wait 3 sec. If process was really stopped we should not get text "test=2"
    WaitUtils.sleepQuietly(3);

    try {
      terminal.waitExpectedTextNotPresentTerminal("test=2");
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/8390");
    }
  }

  @Test
  public void shouldBeClear() {
    terminal.typeIntoTerminal("cd / && ls -l" + Keys.ENTER);

    // clear terminal
    terminal.typeIntoTerminal("clear" + Keys.ENTER);
    terminal.waitExpectedTextNotPresentTerminal("clear");

    terminal.waitTerminalIsNotEmpty();
    terminal.waitExpectedTextIntoTerminal("user@");
  }

  @Test
  public void shouldBeReset() {
    terminal.typeIntoTerminal("cd / && ls -l" + Keys.ENTER);

    // clear terminal
    terminal.typeIntoTerminal("reset" + Keys.ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("reset");

    terminal.waitTerminalIsNotEmpty();
    terminal.waitExpectedTextIntoTerminal("user@");
  }

  @Test
  public void shouldTurnToNormalModeFromAlternativeScreenModeAndOtherwise() {
    // open MC - terminal will switch off from normal mode to alternative screen with text user
    // interface (pseudo user graphics).
    openMC("/");

    // turn back to "normal" mode
    terminal.typeIntoTerminal(Keys.CONTROL + "o");
    for (String partOfContent : CHECK_MC_OPENING) {
      terminal.waitExpectedTextNotPresentTerminal(partOfContent);
    }

    terminal.typeIntoTerminal(Keys.CONTROL + "o" + Keys.ENTER);
    for (String partOfContent : CHECK_MC_OPENING) {
      terminal.waitExpectedTextIntoTerminal(partOfContent);
    }
  }

  @Test
  public void shouldOpenMCHelpDialogAndUserMenuDialog() {
    openMC("/");

    // check "F1"
    terminal.typeIntoTerminal(Keys.F1.toString());
    terminal.waitTerminalIsNotEmpty();
    terminal.waitExpectedTextIntoTerminal(MC_HELP_DIALOG);
    terminal.typeIntoTerminal(Keys.F10.toString());
    terminal.waitExpectedTextNotPresentTerminal(MC_HELP_DIALOG);

    // check "F2" key
    terminal.typeIntoTerminal(Keys.F2.toString());
    terminal.waitExpectedTextIntoTerminal(MC_USER_MENU_DIALOG);
    terminal.typeIntoTerminal(Keys.F10.toString());
    terminal.waitExpectedTextNotPresentTerminal(MC_USER_MENU_DIALOG);
  }

  @Test
  public void shouldViewFolderIntoMC() {
    terminal.waitTerminalTab();
    consoles.clickOnMaximizePanelIcon();
    openMC("/");

    // select bin folder and view this folder by "F3" key
    terminal.waitExpectedTextIntoTerminal("bin");
    terminal.typeIntoTerminal(Keys.HOME.toString() + Keys.F3.toString());
    for (String partOfContent : VIEW_BIN_FOLDER) {
      terminal.waitExpectedTextIntoTerminal(partOfContent);
    }
    terminal.typeIntoTerminal("cd ~" + Keys.ENTER);
    terminal.waitExpectedTextIntoTerminal(".cache");
    consoles.clickOnMaximizePanelIcon();
  }

  @Test
  public void closeTerminalByExitCommand() {
    terminal.waitTerminalConsole();
    terminal.typeIntoTerminal("exit" + Keys.ENTER);
    terminal.waitTerminalIsNotPresent(1);
  }

  @Test
  public void shouldEditFileIntoMCEdit() {
    openMC("/projects/" + PROJECT_NAME);

    // check End, Home, F4, Delete keys
    terminal.typeIntoTerminal("" + Keys.END + Keys.ENTER + Keys.END + Keys.ARROW_UP + Keys.F4);
    // select editor
    terminal.typeIntoTerminal(valueOf(1) + Keys.ENTER);

    terminal.waitExpectedTextIntoTerminal("README.md");
    terminal.typeIntoTerminal("<!-some comment->");
    terminal.typeIntoTerminal(
        "" + Keys.HOME + Keys.ARROW_RIGHT + Keys.ARROW_RIGHT + Keys.ARROW_RIGHT + Keys.DELETE);
    terminal.waitExpectedTextIntoTerminal("<!-ome comment->");
  }

  @Test
  public void checkDeleteAction() throws InterruptedException {
    // if the bug exists -> the dialog appears and the terminal lose focus
    terminal.typeIntoTerminal(Keys.DELETE.toString());
    terminal.typeIntoTerminal("pwd");
  }

  private void openMC(String currentLocation) {
    // launch mc from root directory
    loader.waitOnClosed();
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoTerminal("cd " + currentLocation);
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    terminal.typeIntoTerminal("mc");
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    terminal.waitTerminalIsNotEmpty();
  }
}
