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
  private static final String WEB_JAVA_PETCLINIC = "web-java-petclinic";

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
        JAVA_MYSQL_CENTOS, WORKSPACE_NAME, WEB_JAVA_PETCLINIC);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(WEB_JAVA_PETCLINIC);
  }

  @Test(priority = 1)
  public void checkWebJavaPetclinicProjectCommands() {
    projectExplorer.openItemByPath(WEB_JAVA_PETCLINIC);

    consoles.executeCommandFromProcessesArea(
        "db", COMMON_GOAL, "show databases", "information_schema");

    consoles.executeCommandFromProcessesArea(
        "dev-machine", COMMON_GOAL, BUILD_COMMAND, BUILD_SUCCESS);
    consoles.executeCommandFromProcessesArea(
        "dev-machine", BUILD_GOAL, BUILD_COMMAND_ITEM.getItem(WEB_JAVA_PETCLINIC), BUILD_SUCCESS);

    consoles.executeCommandFromProcessesArea(
        "dev-machine",
        RUN_GOAL,
        BUILD_AND_DEPLOY_COMMAND_ITEM.getItem(WEB_JAVA_PETCLINIC),
        "Server startup in");
    consoles.checkWebElementVisibilityAtPreviewPage(By.xpath("//h2[text()='Welcome']"));

    projectExplorer.invokeCommandWithContextMenu(
        RUN_GOAL,
        WEB_JAVA_PETCLINIC,
        STOP_TOMCAT_COMMAND_ITEM.getItem(WEB_JAVA_PETCLINIC),
        "dev-machine");
    consoles.selectProcessInProcessConsoleTreeByName("Terminal");
    terminal.typeIntoTerminal("ps ax");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("$TOMCAT_HOME/bin/catalina.sh");

    consoles.executeCommandFromProcessesArea(
        "dev-machine",
        DEBUG_GOAL,
        DEBUG_COMMAND_ITEM.getItem(WEB_JAVA_PETCLINIC),
        LISTENING_AT_ADDRESS_8000);
  }
}
