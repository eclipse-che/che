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
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.BUILD_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.DEBUG_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestCommandsConstants.RUN_COMMAND;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.BUILD_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.DEBUG_GOAL;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN_GOAL;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.ECLIPSE_VERTX;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
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
public class CreateWorkspaceFromEclipseVertxStackTest {

  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String VERTX_HEALTH_CHECKS_BOOSTER = "vertx-health-checks-booster";
  private static final String VERTX_HTTP_BOOSTER = "vertx-http-booster";

  private List<String> projects = ImmutableList.of(VERTX_HEALTH_CHECKS_BOOSTER, VERTX_HTTP_BOOSTER);

  @Inject private Ide ide;
  @Inject private Consoles consoles;
  @Inject private Dashboard dashboard;
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
  public void checkWorkspaceCreationFromEclipseVertxStack() {
    createWorkspaceHelper.createWorkspaceFromStackWithProjects(
        ECLIPSE_VERTX, WORKSPACE_NAME, projects);

    ide.switchToIdeAndWaitWorkspaceIsReadyToUse();

    projectExplorer.waitProjectInitialization(VERTX_HEALTH_CHECKS_BOOSTER);
    projectExplorer.waitProjectInitialization(VERTX_HTTP_BOOSTER);
  }

  @Test(priority = 1)
  public void checkVertxHealthChecksBoosterProjectCommands() {
    consoles.executeCommandFromProjectExplorer(
        VERTX_HEALTH_CHECKS_BOOSTER, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        VERTX_HEALTH_CHECKS_BOOSTER,
        RUN_GOAL,
        RUN_COMMAND,
        "[INFO] INFO: Succeeded in deploying verticle");
    consoles.checkWebElementVisibilityAtPreviewPage(By.id("_vert_x_health_check_booster"));
    consoles.closeProcessTabWithAskDialog(RUN_COMMAND);

    consoles.executeCommandFromProjectExplorer(
        VERTX_HEALTH_CHECKS_BOOSTER,
        DEBUG_GOAL,
        DEBUG_COMMAND,
        "[INFO] Listening for transport dt_socket at address: 5005");
    consoles.closeProcessTabWithAskDialog(DEBUG_COMMAND);
  }

  @Test(priority = 1)
  public void checkVertxHttpBoosterProjectCommands() {
    consoles.executeCommandFromProjectExplorer(
        VERTX_HTTP_BOOSTER, BUILD_GOAL, BUILD_COMMAND, BUILD_SUCCESS);

    consoles.executeCommandFromProjectExplorer(
        VERTX_HTTP_BOOSTER, RUN_GOAL, RUN_COMMAND, "[INFO] INFO: Succeeded in deploying verticle");
    consoles.checkWebElementVisibilityAtPreviewPage(By.id("_vert_x_health_check_booster"));
    consoles.closeProcessTabWithAskDialog(RUN_COMMAND);

    consoles.executeCommandFromProjectExplorer(
        VERTX_HTTP_BOOSTER,
        DEBUG_GOAL,
        DEBUG_COMMAND,
        "[INFO] Listening for transport dt_socket at address: 5005");
    consoles.closeProcessTabWithAskDialog(DEBUG_COMMAND);
  }
}
