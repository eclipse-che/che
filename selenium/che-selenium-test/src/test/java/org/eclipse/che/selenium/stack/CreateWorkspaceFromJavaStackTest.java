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
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.SERVER_STARTUP_IN;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.BUILD_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.BUILD_AND_RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.BUILD_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.DEBUG_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.RUN_TOMCAT_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.STOP_TOMCAT_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.DEBUG_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;
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
public class CreateWorkspaceFromJavaStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String CONSOLE_JAVA_SIMPLE = "console-java-simple";
  private static final String WEB_JAVA_SPRING = "web-java-spring";

  private List<String> projects = ImmutableList.of(CONSOLE_JAVA_SIMPLE, WEB_JAVA_SPRING);

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private CheTerminal terminal;
  @Inject private ProjectExplorer projectExplorer;
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
  public void checkWorkspaceCreationFromJavaStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProjects(JAVA, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(CONSOLE_JAVA_SIMPLE);
    projectExplorer.waitProjectInitialization(WEB_JAVA_SPRING);
  }

  @Test(priority = 1)
  public void checkConsoleJavaSimpleCommands() {
    consoles.executeCommandFromProjectExplorer(
        CONSOLE_JAVA_SIMPLE, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        CONSOLE_JAVA_SIMPLE,
        BUILD_GOAL,
        BUILD_COMMAND_ITEM.getItem(CONSOLE_JAVA_SIMPLE),
        BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        CONSOLE_JAVA_SIMPLE,
        RUN_GOAL,
        RUN_COMMAND_ITEM.getItem(CONSOLE_JAVA_SIMPLE),
        "Hello World Che!");
  }

  @Test(priority = 1)
  public void checkWebJavaSpringCommands() {
    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING, BUILD_GOAL, BUILD_COMMAND_ITEM.getItem(WEB_JAVA_SPRING), BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING,
        RUN_GOAL,
        BUILD_AND_RUN_COMMAND_ITEM.getItem(WEB_JAVA_SPRING),
        SERVER_STARTUP_IN);
    consoles.checkWebElementVisibilityAtPreviewPage(By.xpath("//span[text()='Enter your name: ']"));
    consoles.closeProcessTabWithAskDialog(BUILD_AND_RUN_COMMAND_ITEM.getItem(WEB_JAVA_SPRING));

    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING,
        RUN_GOAL,
        RUN_TOMCAT_COMMAND_ITEM.getItem(WEB_JAVA_SPRING),
        SERVER_STARTUP_IN);
    consoles.checkWebElementVisibilityAtPreviewPage(By.xpath("//span[text()='Enter your name: ']"));

    // execute 'stop tomcat' command and check that tomcat is not running
    projectExplorer.invokeCommandWithContextMenu(
        RUN_GOAL, WEB_JAVA_SPRING, STOP_TOMCAT_COMMAND_ITEM.getItem(WEB_JAVA_SPRING));
    consoles.selectProcessInProcessConsoleTreeByName("Terminal");
    terminal.typeIntoTerminal("ps ax");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("/bin/bash -c $TOMCAT_HOME/bin/catalina.sh");

    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING,
        DEBUG_GOAL,
        DEBUG_COMMAND_ITEM.getItem(WEB_JAVA_SPRING),
        LISTENING_AT_ADDRESS_8000);
  }
}
