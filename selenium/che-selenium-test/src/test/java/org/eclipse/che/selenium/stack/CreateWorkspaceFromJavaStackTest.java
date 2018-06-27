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
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.DEBUG;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.JAVA;
import static org.openqa.selenium.Keys.ENTER;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.CheTerminal;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromJavaStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String CONSOLE_JAVA_SIMPLE = "console-java-simple";
  private static final String WEB_JAVA_SPRING = "web-java-spring";

  private ArrayList<String> projects = new ArrayList<>();

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
    projects.add(CONSOLE_JAVA_SIMPLE);
    projects.add(WEB_JAVA_SPRING);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromJavaStack() {
    String currentWindow;

    createWorkspaceHelper.createWorkspaceFromStackWithProjects(JAVA, WORKSPACE_NAME, projects);

    currentWindow = ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(CONSOLE_JAVA_SIMPLE);
    projectExplorer.waitProjectInitialization(WEB_JAVA_SPRING);

    consoles.startCommandAndCheckResult(CONSOLE_JAVA_SIMPLE, BUILD, "build", BUILD_SUCCESS);
    consoles.startCommandAndCheckResult(
        CONSOLE_JAVA_SIMPLE, BUILD, "console-java-simple:build", BUILD_SUCCESS);
    consoles.startCommandAndCheckResult(
        CONSOLE_JAVA_SIMPLE, RUN, "console-java-simple:run", "Hello World Che!");

    consoles.startCommandAndCheckResult(WEB_JAVA_SPRING, BUILD, "build", BUILD_SUCCESS);
    consoles.startCommandAndCheckResult(
        WEB_JAVA_SPRING, BUILD, "web-java-spring:build", BUILD_SUCCESS);

    consoles.startCommandAndCheckResult(
        WEB_JAVA_SPRING, RUN, "web-java-spring:build and run", "Server startup in");
    consoles.startCommandAndCheckApp(currentWindow, "//span[text()='Enter your name: ']");
    consoles.closeProcessTabWithAskDialog("web-java-spring:build and run");
    consoles.startCommandAndCheckResult(
        WEB_JAVA_SPRING, RUN, "web-java-spring:run tomcat", "Server startup in");
    consoles.startCommandAndCheckApp(currentWindow, "//span[text()='Enter your name: ']");
    // start 'stop apache' command and check that apache not running
    projectExplorer.invokeCommandWithContextMenu(
        RUN, WEB_JAVA_SPRING, "web-java-spring:stop tomcat");
    consoles.selectProcessInProcessConsoleTreeByName("Terminal");
    terminal.typeIntoTerminal("ps ax");
    terminal.typeIntoTerminal(ENTER.toString());
    terminal.waitExpectedTextNotPresentTerminal("/bin/bash -c $TOMCAT_HOME/bin/catalina.sh");

    consoles.startCommandAndCheckResult(
        WEB_JAVA_SPRING,
        DEBUG,
        "web-java-spring:debug",
        "Listening for transport dt_socket at address: 8000");
  }
}
