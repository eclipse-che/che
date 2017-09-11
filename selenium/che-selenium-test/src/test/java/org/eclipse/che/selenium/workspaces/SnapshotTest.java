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
package org.eclipse.che.selenium.workspaces;

import com.google.inject.Inject;
import java.net.URL;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.project.ProjectTemplates;
import org.eclipse.che.selenium.core.utils.WaitUtils;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.openqa.selenium.Keys;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class SnapshotTest {
  private static final String PROJECT_NAME = SnapshotTest.class.getSimpleName();
  private static final String USER_DIRECTORY = "cd ~/";
  private static final String CREATE_TEXT_FILE = ">" + PROJECT_NAME + ".txt";
  private static final String FILE_NAME = PROJECT_NAME + ".txt";
  private static final String LS_COMMAND = "ls";

  @Inject private TestWorkspace workspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private MachineTerminal terminal;
  @Inject private Consoles consoles;
  @Inject private ToastLoader toastLoader;
  @Inject private Menu menu;
  @Inject private TestProjectServiceClient testProjectServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    URL resource = SnapshotTest.this.getClass().getResource("/projects/guess-project");
    testProjectServiceClient.importProject(
        workspace.getId(),
        Paths.get(resource.toURI()),
        PROJECT_NAME,
        ProjectTemplates.MAVEN_SPRING);
    ide.open(workspace);
  }

  @Test
  public void snapshotTest() throws Exception {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    menu.runCommand(TestMenuCommandsConstants.Run.RUN_MENU, TestMenuCommandsConstants.Run.TERMINAL);
    terminal.waitTerminalConsole();
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoTerminal(USER_DIRECTORY);
    terminal.waitExpectedTextIntoTerminal(USER_DIRECTORY);
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    terminal.typeIntoTerminal(CREATE_TEXT_FILE);
    terminal.waitExpectedTextIntoTerminal(CREATE_TEXT_FILE);
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    consoles.closeTerminalIntoConsoles();
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.STOP_WORKSPACE);
    toastLoader.waitExpectedTextInToastLoader("Stopping the workspace");
    toastLoader.waitExpectedTextInToastLoader("Workspace is not running", 60);
    toastLoader.clickOnStartButton();
    toastLoader.waitExpectedTextInToastLoader("Starting workspace runtime.", 20);
    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab();
    consoles.selectProcessByTabName("Terminal");
    terminal.typeIntoTerminal(USER_DIRECTORY);
    terminal.waitExpectedTextIntoTerminal(USER_DIRECTORY);
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    terminal.typeIntoTerminal(LS_COMMAND);
    terminal.waitExpectedTextIntoTerminal(LS_COMMAND);
    terminal.typeIntoTerminal(Keys.ENTER.toString());
    WaitUtils.sleepQuietly(3);
    Assert.assertTrue(terminal.getVisibleTextFromTerminal().contains(FILE_NAME));
  }
}
