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
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.RUN_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestIntelligentCommandsConstants.CommandItem.BUILD_AND_RUN_COMMAND_ITEM;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.CPP;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspaceHelper;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromCppStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String CONSOLE_CPP_PROJECT = "console-cpp-simple";
  private static final String C_SIMPLE_CONSOLE_PROJECT = "c-simple-console";
  private static final String EXPECTED_MESSAGE_IN_CONSOLE = "Hello World";

  private List<String> projects = ImmutableList.of(CONSOLE_CPP_PROJECT, C_SIMPLE_CONSOLE_PROJECT);

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
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
  public void checkWorkspaceCreationFromCppStack() {
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithProjects(CPP, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(CONSOLE_CPP_PROJECT);
    projectExplorer.waitProjectInitialization(C_SIMPLE_CONSOLE_PROJECT);
  }

  @Test(priority = 1)
  public void checkConsoleCppSimpleProjectCommands() {
    consoles.executeCommandFromProjectExplorer(
        CONSOLE_CPP_PROJECT, RUN_GOAL, RUN_COMMAND, EXPECTED_MESSAGE_IN_CONSOLE);

    consoles.executeCommandFromProjectExplorer(
        CONSOLE_CPP_PROJECT,
        RUN_GOAL,
        BUILD_AND_RUN_COMMAND_ITEM.getItem(CONSOLE_CPP_PROJECT),
        EXPECTED_MESSAGE_IN_CONSOLE);
  }

  @Test(priority = 1)
  public void checkCSimpleConsoleProjectCommands() {
    consoles.executeCommandFromProjectExplorer(
        C_SIMPLE_CONSOLE_PROJECT,
        RUN_GOAL,
        BUILD_AND_RUN_COMMAND_ITEM.getItem(C_SIMPLE_CONSOLE_PROJECT),
        EXPECTED_MESSAGE_IN_CONSOLE);
  }
}
