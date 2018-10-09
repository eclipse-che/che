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
import static org.eclipse.che.selenium.core.constant.TestBuildConstants.LISTENING_AT_ADDRESS;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.BUILD_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.CLEAN_BUILD_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.DEBUG_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.RUN_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.DEBUG_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.SPRING_BOOT;

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
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromSpringBootStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String SPRING_BOOT_HEALTH_CHECK_PROJECT = "spring-boot-health-check-booster";
  private static final String SPRING_BOOT_HTTP_PROJECT = "spring-boot-http-booster";

  private List<String> projects =
      ImmutableList.of(SPRING_BOOT_HEALTH_CHECK_PROJECT, SPRING_BOOT_HTTP_PROJECT);

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
  public void checkWorkspaceCreationFromSpringBootStack() {
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace =
        createWorkspaceHelper.createWorkspaceFromStackWithProjects(
            SPRING_BOOT, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();
    consoles.waitJDTLSProjectResolveFinishedMessage(
        SPRING_BOOT_HEALTH_CHECK_PROJECT, SPRING_BOOT_HTTP_PROJECT);

    projectExplorer.waitProjectInitialization(SPRING_BOOT_HEALTH_CHECK_PROJECT);
    projectExplorer.waitProjectInitialization(SPRING_BOOT_HTTP_PROJECT);
  }

  @Test(priority = 1)
  public void checkSpringBootHealthCheckBoosterProjectCommands() {
    By textOnPreviewPage = By.xpath("//h2[text()='Health Check Booster']");

    // build and run 'spring-boot-health-check-booster' project
    consoles.executeCommandFromProjectExplorer(
        SPRING_BOOT_HEALTH_CHECK_PROJECT, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        SPRING_BOOT_HEALTH_CHECK_PROJECT, BUILD_GOAL, CLEAN_BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        SPRING_BOOT_HEALTH_CHECK_PROJECT, RUN_GOAL, RUN_COMMAND, "Started BoosterApplication in");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);
    consoles.closeProcessTabWithAskDialog(RUN_COMMAND);

    consoles.executeCommandFromProcessesArea(
        "dev-machine", DEBUG_GOAL, DEBUG_COMMAND, LISTENING_AT_ADDRESS);
    consoles.closeProcessTabWithAskDialog(DEBUG_COMMAND);
  }

  @Test(priority = 1)
  public void checkSpringBooHttpBoosterProjectCommands() {
    By textOnPreviewPage = By.xpath("//h2[text()='HTTP Booster']");

    // build and run 'spring-boot-http-booster' project
    consoles.executeCommandFromProjectExplorer(
        SPRING_BOOT_HTTP_PROJECT, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        SPRING_BOOT_HTTP_PROJECT, BUILD_GOAL, CLEAN_BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        SPRING_BOOT_HTTP_PROJECT,
        RUN_GOAL,
        RUN_COMMAND,
        "INFO: Setting the server's publish address to be");
    consoles.checkWebElementVisibilityAtPreviewPage(textOnPreviewPage);
    consoles.closeProcessTabWithAskDialog(RUN_COMMAND);

    consoles.executeCommandFromProcessesArea(
        "dev-machine", DEBUG_GOAL, DEBUG_COMMAND, LISTENING_AT_ADDRESS);
    consoles.closeProcessTabWithAskDialog(DEBUG_COMMAND);
  }
}
