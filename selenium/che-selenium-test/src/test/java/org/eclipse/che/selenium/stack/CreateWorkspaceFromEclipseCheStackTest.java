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
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.MAVEN_BUILD_AND_RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.MAVEN_BUILD_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_CHE;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
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
public class CreateWorkspaceFromEclipseCheStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String CONSOLE_JAVA_PROJECT = "console-java-simple";

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
  @Inject private CheTerminal terminal;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private CreateWorkspaceHelper createWorkspaceHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

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
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithProject(
            ECLIPSE_CHE, WORKSPACE_NAME, CONSOLE_JAVA_PROJECT);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(CONSOLE_JAVA_PROJECT);

    projectExplorer.waitProjectInitialization(CONSOLE_JAVA_PROJECT);
    projectExplorer.waitProjectInitialization(CONSOLE_JAVA_PROJECT);
  }

  @Test(priority = 1)
  public void checkConsoleJavaSimpleProjectCommands() {
    consoles.executeCommandFromProjectExplorer(
        CONSOLE_JAVA_PROJECT,
        BUILD_GOAL,
        MAVEN_BUILD_COMMAND_ITEM.getItem(CONSOLE_JAVA_PROJECT),
        BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        CONSOLE_JAVA_PROJECT,
        RUN_GOAL,
        MAVEN_BUILD_AND_RUN_COMMAND_ITEM.getItem(CONSOLE_JAVA_PROJECT),
        "Hello World Che!");
  }
}
