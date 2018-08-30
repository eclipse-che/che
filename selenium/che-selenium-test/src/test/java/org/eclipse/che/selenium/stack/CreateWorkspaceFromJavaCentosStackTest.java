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
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA_CENTOS;
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
public class CreateWorkspaceFromJavaCentosStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String CONSOLE_JAVA_PROJECT = "console-java-simple";
  private static final String WEB_JAVA_SPRING_PROJECT = "web-java-spring";

  private List<String> projects = ImmutableList.of(CONSOLE_JAVA_PROJECT, WEB_JAVA_SPRING_PROJECT);
  private By textOnPreviewPage = By.xpath("//span[text()='Enter your name: ']");

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
  public void checkWorkspaceCreationFromJavaCentosStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProjects(
        JAVA_CENTOS, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(CONSOLE_JAVA_PROJECT);
    projectExplorer.waitProjectInitialization(WEB_JAVA_SPRING_PROJECT);
  }

  @Test(priority = 1)
  public void checkConsoleJavaSimpleProjectCommands() {

    // build and run console-java-simple project
    consoles.executeCommandFromProjectExplorer(
        CONSOLE_JAVA_PROJECT, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);
    consoles.executeCommandFromProjectExplorer(
        CONSOLE_JAVA_PROJECT,
        BUILD_GOAL,
        BUILD_COMMAND_ITEM.getItem(CONSOLE_JAVA_PROJECT),
        BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        CONSOLE_JAVA_PROJECT,
        RUN_GOAL,
        RUN_COMMAND_ITEM.getItem(CONSOLE_JAVA_PROJECT),
        "Hello World Che!");
  }

  @Test(priority = 1)
  public void checkWebJavaSpringProjectCommands() {
    String tomcatIsRunning = "/bin/bash -c $TOMCAT_HOME/bin/catalina.sh";

    // build web-java-spring project
    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING_PROJECT, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);
    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING_PROJECT,
        BUILD_GOAL,
        BUILD_COMMAND_ITEM.getItem(WEB_JAVA_SPRING_PROJECT),
        BUILD_SUCCESS);

    // build and run web-java-spring project
    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING_PROJECT,
        RUN_GOAL,
        BUILD_AND_RUN_COMMAND_ITEM.getItem(WEB_JAVA_SPRING_PROJECT),
        SERVER_STARTUP_IN);
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);
    consoles.closeProcessTabWithAskDialog(
        BUILD_AND_RUN_COMMAND_ITEM.getItem(WEB_JAVA_SPRING_PROJECT));

    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING_PROJECT,
        RUN_GOAL,
        RUN_TOMCAT_COMMAND_ITEM.getItem(WEB_JAVA_SPRING_PROJECT),
        SERVER_STARTUP_IN);
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);

    // execute 'stop tomcat' command and check that tomcat process is not running
    projectExplorer.invokeCommandWithContextMenu(
        RUN_GOAL,
        WEB_JAVA_SPRING_PROJECT,
        STOP_TOMCAT_COMMAND_ITEM.getItem(WEB_JAVA_SPRING_PROJECT));
    consoles.selectProcessInProcessConsoleTreeByName("Terminal");
    terminal.typeIntoActiveTerminal("ps ax");
    terminal.typeIntoActiveTerminal(ENTER.toString());
    terminal.waitNoTextInFirstTerminal(tomcatIsRunning);

    consoles.executeCommandFromProjectExplorer(
        WEB_JAVA_SPRING_PROJECT,
        DEBUG_GOAL,
        DEBUG_COMMAND_ITEM.getItem(WEB_JAVA_SPRING_PROJECT),
        LISTENING_AT_ADDRESS_8000);
  }
}
