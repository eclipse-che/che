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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.RESTART_APACHE_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.START_APACHE_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.STOP_APACHE_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.PHP;
import static org.openqa.selenium.Keys.ENTER;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromPHPStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String WEB_PHP_PROJECT = "web-php-simple";
  private static final String WEB_PHP_GAE_PROJECT = "web-php-gae-simple";
  private static final String PHP_FILE_NAME = "index.php";

  private List<String> projects = ImmutableList.of(WEB_PHP_PROJECT, WEB_PHP_GAE_PROJECT);

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private CheTerminal terminal;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromPHPStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProjects(PHP, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(WEB_PHP_PROJECT);
    projectExplorer.waitProjectInitialization(WEB_PHP_GAE_PROJECT);
  }

  @Test(priority = 1)
  public void checkWebPhpSimpleCommands() {
    By textOnPreviewPage = By.xpath("//*[text()='Hello World!']");
    String apacheIsRunning = "/usr/sbin/apache2 -k start";

    projectExplorer.openItemByPath(WEB_PHP_PROJECT);
    projectExplorer.openItemByPath(WEB_PHP_PROJECT + "/" + PHP_FILE_NAME);

    consoles.executeCommandFromProjectExplorer(
        WEB_PHP_PROJECT, RUN_GOAL, "run php script", "Hello World!");

    consoles.executeCommandFromProjectExplorer(
        WEB_PHP_PROJECT,
        RUN_GOAL,
        START_APACHE_COMMAND,
        "Starting Apache httpd web server apache2");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    consoles.executeCommandFromProjectExplorer(
        WEB_PHP_PROJECT, RUN_GOAL, RESTART_APACHE_COMMAND, "...done");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    // start 'stop apache' command and check that apache not running
    consoles.executeCommandFromProjectExplorer(
        WEB_PHP_PROJECT, RUN_GOAL, STOP_APACHE_COMMAND, "Stopping Apache httpd web server apache2");
    consoles.selectProcessInProcessConsoleTreeByName("Terminal");
    terminal.typeIntoTerminal("ps ax");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal(apacheIsRunning);
  }

  @Test(priority = 1)
  public void checkWebPhpGaeSimpleCommands() {
    By textOnPreviewPage = By.xpath("//input[@value='Sign Guestbook']");

    consoles.executeCommandFromProjectExplorer(
        WEB_PHP_GAE_PROJECT,
        RUN_GOAL,
        RUN_COMMAND_ITEM.getItem(WEB_PHP_GAE_PROJECT),
        "Starting admin server");

    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    consoles.closeProcessTabWithAskDialog(RUN_COMMAND_ITEM.getItem(WEB_PHP_GAE_PROJECT));
  }
}
