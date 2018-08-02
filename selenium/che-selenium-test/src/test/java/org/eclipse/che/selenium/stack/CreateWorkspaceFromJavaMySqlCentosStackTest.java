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
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.LISTENING_AT_ADDRESS_8000;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.BUILD_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.BUILD_AND_DEPLOY_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.BUILD_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.DEBUG_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.STOP_TOMCAT_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.COMMON_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.DEBUG_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_MYSQL_CENTOS;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.TestGroup;
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
@Test(groups = {TestGroup.DOCKER})
public class CreateWorkspaceFromJavaMySqlCentosStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String WEB_JAVA_PROJECT = "web-java-petclinic";
  private static final String DEV_MACHINE_NAME = "dev-machine";
  private static final String DB_MACHINE_NAME = "db";

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
  public void checkWorkspaceCreationFromJavaMySqlCentosStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProject(
        JAVA_MYSQL_CENTOS, WORKSPACE_NAME, WEB_JAVA_PROJECT);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(WEB_JAVA_PROJECT);
  }

  @Test(priority = 1)
  public void checkWebJavaPetclinicProjectCommands() {
    By textOnPreviewPage = By.xpath("//h2[text()='Welcome']");
    String tomcatIsRunning = "$TOMCAT_HOME/bin/catalina.sh";

    projectExplorer.openItemByPath(WEB_JAVA_PROJECT);

    // Select the db machine and perform 'show databases' command
    consoles.executeCommandFromProcessesArea(
        DB_MACHINE_NAME, COMMON_GOAL, "show databases", "information_schema");

    // Build and deploy the web application
    consoles.executeCommandFromProcessesArea(
        DEV_MACHINE_NAME, COMMON_GOAL, BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProcessesArea(
        DEV_MACHINE_NAME, BUILD_GOAL, BUILD_COMMAND_ITEM.getItem(WEB_JAVA_PROJECT), BUILD_SUCCESS);

    consoles.executeCommandFromProcessesArea(
        DEV_MACHINE_NAME,
        RUN_GOAL,
        BUILD_AND_DEPLOY_COMMAND_ITEM.getItem(WEB_JAVA_PROJECT),
        "Server startup in");

    // Run the application
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    // execute 'stop tomcat' command and check that tomcat process is not running
    projectExplorer.invokeCommandWithContextMenu(
        RUN_GOAL,
        WEB_JAVA_PROJECT,
        STOP_TOMCAT_COMMAND_ITEM.getItem(WEB_JAVA_PROJECT),
        DEV_MACHINE_NAME);
    consoles.selectProcessInProcessConsoleTreeByName("Terminal");
    terminal.typeIntoTerminal("ps ax");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal(tomcatIsRunning);

    consoles.executeCommandFromProcessesArea(
        DEV_MACHINE_NAME,
        DEBUG_GOAL,
        DEBUG_COMMAND_ITEM.getItem(WEB_JAVA_PROJECT),
        LISTENING_AT_ADDRESS_8000);
  }
}
